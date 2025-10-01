package com.example.desafio_santander.service;

import com.example.desafio_santander.model.Agencia;
import com.example.desafio_santander.dto.agencia.AgenciaDTO;
import com.example.desafio_santander.dto.agencia.AgenciaRequestDTO;
import com.example.desafio_santander.dto.agencia.AgenciaResponseDTO;
import com.example.desafio_santander.repository.AgenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class AgenciaService {
    @Autowired
    private AgenciaRepository agenciaRepository;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY = "agencias_cache";
    private static final String COUNTER_KEY = "consultas_counter";
    private static final int MAX_CONSULTAS = 10;
    private static final int CACHE_TIMEOUT_MINUTES = 5;

    // Cache em memória como fallback
    private List<AgenciaDTO> memoryCache = null;
    private long memoryCacheTime = 0;
    private long consultasCounter = 0;

    public String cadastrarAgencia(AgenciaRequestDTO dto) {
        Agencia agencia = new Agencia(null, dto.getPosX(), dto.getPosY());
        Agencia savedAgencia = agenciaRepository.save(agencia);

        // Limpar cache (Redis ou memória)
        if (redisTemplate != null) {
            redisTemplate.delete(CACHE_KEY);
            redisTemplate.delete(COUNTER_KEY);
        } else {
            memoryCache = null;
            consultasCounter = 0;
        }

        return String.format("Agência cadastrada com sucesso, ID: %s", savedAgencia.getId());
    }

    public AgenciaResponseDTO consultarAgencias() {
        if (redisTemplate != null) {
            return consultarComRedis();
        } else {
            return consultarSemRedis();
        }
    }

    private AgenciaResponseDTO consultarComRedis() {
        Long consultas = redisTemplate.opsForValue().increment(COUNTER_KEY);
        if (consultas != null && consultas == 1) {
            redisTemplate.expire(COUNTER_KEY, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        }

        boolean cacheRenovado = false;
        List<AgenciaDTO> agencias;

        if (consultas != null && consultas >= MAX_CONSULTAS || !redisTemplate.hasKey(CACHE_KEY)) {
            agencias = renovarCache();
            cacheRenovado = true;
            redisTemplate.delete(COUNTER_KEY);
        } else {
            @SuppressWarnings("unchecked")
            List<AgenciaDTO> cachedAgencias = (List<AgenciaDTO>) redisTemplate.opsForValue().get(CACHE_KEY);
            agencias = cachedAgencias != null ? cachedAgencias : renovarCache();
        }

        String message = String.format("Consulta realizada às %s - %s agências encontradas",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
            agencias.size());

        return new AgenciaResponseDTO(agencias, message, cacheRenovado);
    }

    private AgenciaResponseDTO consultarSemRedis() {
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

        String message = String.format("Consulta realizada às %s - %s agências encontradas (sem Redis)",
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
            agencias.size());

        return new AgenciaResponseDTO(agencias, message, cacheRenovado);
    }

    private List<AgenciaDTO> renovarCache() {
        List<Agencia> agenciasList = agenciaRepository.findAll();
        List<AgenciaDTO> agencias = agenciasList.stream()
            .map(a -> new AgenciaDTO(a.getId(), a.getPosX(), a.getPosY()))
            .collect(Collectors.toList());

        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(CACHE_KEY, agencias, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
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
