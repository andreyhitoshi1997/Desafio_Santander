package com.example.desafio_santander.service;

import com.example.desafio_santander.dto.agencia.AgenciaDTO;
import com.example.desafio_santander.dto.agencia.DistanciaResponseDTO;
import com.example.desafio_santander.exception.DistanciaException;
import com.example.desafio_santander.model.Agencia;
import com.example.desafio_santander.repository.AgenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class DistanciaService {

    private static final Logger logger = LoggerFactory.getLogger(DistanciaService.class);

    @Autowired
    private AgenciaRepository agenciaRepository;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    private static final String DISTANCE_CACHE_KEY = "distancias_cache";
    private static final String DISTANCE_COUNTER_KEY = "distancias_consultas_counter";
    private static final int MAX_CONSULTAS = 10;
    private static final int CACHE_TIMEOUT_MINUTES = 5;

    // Cache em memória como fallback
    private List<AgenciaDTO> memoryCache = null;
    private long memoryCacheTime = 0;
    private long consultasCounter = 0;

    public DistanciaResponseDTO calcularDistancias(int userPosX, int userPosY) {
        try {
            logger.info("Calculando distâncias para posição: X={}, Y={}", userPosX, userPosY);

            if (redisTemplate != null) {
                return calcularComRedis(userPosX, userPosY);
            } else {
                return calcularSemRedis(userPosX, userPosY);
            }
        } catch (Exception e) {
            logger.error("Erro ao calcular distâncias: ", e);
            throw new DistanciaException("Erro interno ao calcular distâncias: " + e.getMessage());
        }
    }

    private DistanciaResponseDTO calcularComRedis(int userPosX, int userPosY) {
        Long consultas = redisTemplate.opsForValue().increment(DISTANCE_COUNTER_KEY);
        if (consultas != null && consultas == 1) {
            redisTemplate.expire(DISTANCE_COUNTER_KEY, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        }

        boolean cacheRenovado = false;
        List<AgenciaDTO> agencias;

        if (consultas != null && consultas >= MAX_CONSULTAS || !redisTemplate.hasKey(DISTANCE_CACHE_KEY)) {
            agencias = renovarCache();
            cacheRenovado = true;
            redisTemplate.delete(DISTANCE_COUNTER_KEY);
        } else {
            @SuppressWarnings("unchecked")
            List<AgenciaDTO> cachedAgencias = (List<AgenciaDTO>) redisTemplate.opsForValue().get(DISTANCE_CACHE_KEY);
            agencias = cachedAgencias != null ? cachedAgencias : renovarCache();
        }

        if (agencias.isEmpty()) {
            logger.warn("Nenhuma agência encontrada para calcular distâncias");
            throw new DistanciaException("Nenhuma agência encontrada para calcular distâncias");
        }

        Map<String, String> distancias = calcularEOrdenarDistancias(agencias, userPosX, userPosY);

        return new DistanciaResponseDTO(distancias);
    }

    private DistanciaResponseDTO calcularSemRedis(int userPosX, int userPosY) {
        consultasCounter++;
        boolean cacheRenovado = false;
        List<AgenciaDTO> agencias;

        // Verificar se cache em memória expirou (5 minutos)
        long currentTime = System.currentTimeMillis();
        boolean cacheExpirado = (currentTime - memoryCacheTime) > (CACHE_TIMEOUT_MINUTES * 60 * 1000);

        if (consultasCounter >= MAX_CONSULTAS || memoryCache == null || cacheExpirado) {
            agencias = renovarCacheMemoria();
            cacheRenovado = true;
            consultasCounter = 0;
        } else {
            agencias = memoryCache;
        }

        if (agencias.isEmpty()) {
            logger.warn("Nenhuma agência encontrada para calcular distâncias (cache em memória)");
            throw new DistanciaException("Nenhuma agência encontrada para calcular distâncias");
        }

        Map<String, String> distancias = calcularEOrdenarDistancias(agencias, userPosX, userPosY);

        return new DistanciaResponseDTO(distancias);
    }

    private Map<String, String> calcularEOrdenarDistancias(List<AgenciaDTO> agencias, int userPosX, int userPosY) {
        return agencias.stream()
            .collect(Collectors.toMap(
                agencia -> "AGENCIA_" + agencia.getId(),
                agencia -> {
                    double distancia = calcularDistancia(userPosX, userPosY, agencia.getPosX(), agencia.getPosY());
                    return String.format("distância = %.2f", distancia);
                },
                (existing, replacement) -> existing,
                LinkedHashMap::new
            ))
            .entrySet()
            .stream()
            .sorted((entry1, entry2) -> {
                double dist1 = extrairDistancia(entry1.getValue());
                double dist2 = extrairDistancia(entry2.getValue());
                return Double.compare(dist1, dist2);
            })
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (existing, replacement) -> existing,
                LinkedHashMap::new
            ));
    }

    private double calcularDistancia(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    private double extrairDistancia(String distanciaString) {
        // Extrai o valor numérico da string "distância = X.XX"
        return Double.parseDouble(distanciaString.split(" = ")[1]);
    }

    private List<AgenciaDTO> renovarCache() {
        List<Agencia> agenciasList = agenciaRepository.findAll();
        List<AgenciaDTO> agencias = agenciasList.stream()
            .map(a -> new AgenciaDTO(a.getId(), a.getPosX(), a.getPosY()))
            .collect(Collectors.toList());

        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(DISTANCE_CACHE_KEY, agencias, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        }

        return agencias;
    }

    private List<AgenciaDTO> renovarCacheMemoria() {
        List<Agencia> agenciasList = agenciaRepository.findAll();
        List<AgenciaDTO> agencias = agenciasList.stream()
            .map(a -> new AgenciaDTO(a.getId(), a.getPosX(), a.getPosY()))
            .collect(Collectors.toList());

        memoryCache = agencias;
        memoryCacheTime = System.currentTimeMillis();

        return agencias;
    }
}
