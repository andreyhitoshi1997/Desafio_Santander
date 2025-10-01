package com.example.desafio_santander.service;

import com.example.desafio_santander.dto.agencia.DistanciaResponseDTO;
import com.example.desafio_santander.exception.DistanciaException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Primary
@RequiredArgsConstructor
public class CachedDistanciaDecorator implements DistanciaUseCase {

    private static final String PREFIX = "distancias_cache:";
    private static final int MAX_CONSULTAS = 10;
    private static final int CACHE_TIMEOUT_MINUTES = 5;

    private final DistanciaCoreService delegate; // serviço “puro”
    private final RedisTemplate<String, Object> redisTemplate; // pode ser null (opcional)

    // Fallback em memória por chave (x:y) → resposta
    private volatile Map<String, DistanciaResponseDTO> memCache; // pode ser ConcurrentHashMap
    private final AtomicInteger memCounter = new AtomicInteger(0);
    private volatile long memExpiresAtMs = 0L;

    @Override
    public DistanciaResponseDTO calcularDistancias(int userPosX, int userPosY) {
        String key = PREFIX + userPosX + ":" + userPosY;

        // Se Redis disponível, priorize Redis
        if (redisTemplate != null) {
            // Contador de consultas (por chave)
            String countKey = key + ":counter";
            Long consultas = redisTemplate.opsForValue().increment(countKey);
            if (consultas != null && consultas == 1) {
                redisTemplate.expire(countKey, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
            }

            boolean renovarPorHits = consultas != null && consultas >= MAX_CONSULTAS;
            boolean temCache = Boolean.TRUE.equals(redisTemplate.hasKey(key));

            if (temCache && !renovarPorHits) {
                Object val = redisTemplate.opsForValue().get(key);
                if (val instanceof DistanciaResponseDTO dto) {
                    return dto;
                }
            }

            // Recalcula e renova cache
            DistanciaResponseDTO fresh = safeDelegate(userPosX, userPosY);
            redisTemplate.opsForValue().set(key, fresh, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
            // zera contador quando renovar por hits
            if (renovarPorHits) {
                redisTemplate.delete(countKey);
            }
            return fresh;
        }

        // --------- Fallback: Cache em memória ---------
        long now = System.currentTimeMillis();
        boolean ttlValido = now < memExpiresAtMs;

        if (memCache != null && ttlValido && memCounter.incrementAndGet() < MAX_CONSULTAS) {
            DistanciaResponseDTO hit = memCache.get(key);
            if (hit != null) return hit;
        }

        DistanciaResponseDTO fresh = safeDelegate(userPosX, userPosY);

        // inicializa mapa se nulo (evitar NPE)
        if (memCache == null) {
            memCache = new java.util.concurrent.ConcurrentHashMap<>();
        }
        memCache.put(key, fresh);

        // renova TTL e contador
        memExpiresAtMs = now + TimeUnit.MINUTES.toMillis(CACHE_TIMEOUT_MINUTES);
        if (memCounter.get() >= MAX_CONSULTAS) memCounter.set(0);

        return fresh;
    }

    private DistanciaResponseDTO safeDelegate(int x, int y) {
        DistanciaResponseDTO dto = delegate.calcularDistancias(x, y);
        if (dto == null || dto.getDistancias() == null || dto.getDistancias().isEmpty()) {
            throw new DistanciaException("Nenhuma agência encontrada para calcular distâncias");
        }
        return dto;
    }
}
