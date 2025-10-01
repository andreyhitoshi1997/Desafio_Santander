package com.example.desafio_santander.service;

import com.example.desafio_santander.dto.agencia.DistanciaResponseDTO;
import com.example.desafio_santander.exception.DistanciaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Primary
public class CachedDistanciaDecorator implements DistanciaUseCase {

    private static final String PREFIX = "distancias_cache:";
    private static final int MAX_CONSULTAS = 10;
    private static final int CACHE_TIMEOUT_MINUTES = 5;

    private final DistanciaCoreService delegate;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    private volatile Map<String, DistanciaResponseDTO> memCache;
    private final AtomicInteger memCounter = new AtomicInteger(0);
    private volatile long memExpiresAtMs = 0L;

    public CachedDistanciaDecorator(DistanciaCoreService delegate) {
        this.delegate = delegate;
    }

    @Override
    public DistanciaResponseDTO calcularDistancias(int userPosX, int userPosY) {
        String key = PREFIX + userPosX + ":" + userPosY;

        if (redisTemplate != null) {
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

            DistanciaResponseDTO fresh = safeDelegate(userPosX, userPosY);
            redisTemplate.opsForValue().set(key, fresh, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
            if (renovarPorHits) {
                redisTemplate.delete(countKey);
            }
            return fresh;
        }

        long now = System.currentTimeMillis();
        boolean ttlValido = now < memExpiresAtMs;

        if (memCache != null && ttlValido && memCounter.incrementAndGet() < MAX_CONSULTAS) {
            DistanciaResponseDTO hit = memCache.get(key);
            if (hit != null) return hit;
        }

        DistanciaResponseDTO fresh = safeDelegate(userPosX, userPosY);

        if (memCache == null) {
            memCache = new java.util.concurrent.ConcurrentHashMap<>();
        }
        memCache.put(key, fresh);

        memExpiresAtMs = now + (CACHE_TIMEOUT_MINUTES * 60_000L);
        memCounter.set(1);

        return fresh;
    }

    private DistanciaResponseDTO safeDelegate(int userPosX, int userPosY) {
        try {
            return delegate.calcularDistancias(userPosX, userPosY);
        } catch (Exception e) {
            throw new DistanciaException("Erro ao calcular distâncias: " + e.getMessage(), e);
        }
    }
}
