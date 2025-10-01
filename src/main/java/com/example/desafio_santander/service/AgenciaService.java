package com.example.desafio_santander.service;

import com.example.desafio_santander.model.Agencia;
import com.example.desafio_santander.dto.agencia.AgenciaDTO;
import com.example.desafio_santander.dto.agencia.AgenciaRequestDTO;
import com.example.desafio_santander.dto.agencia.AgenciaResponseDTO;
import com.example.desafio_santander.exception.AgenciaCadastroException;
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

    private List<AgenciaDTO> memoryCache = null;
    private long memoryCacheTime = 0;
    private long consultasCounter = 0;

    public AgenciaResponseDTO cadastrarAgencia(AgenciaRequestDTO dto) {
        try {
            Agencia agencia = new Agencia(null, dto.getPosX(), dto.getPosY());
            Agencia savedAgencia = agenciaRepository.save(agencia);

            if (redisTemplate != null) {
                redisTemplate.delete(CACHE_KEY);
                redisTemplate.delete(COUNTER_KEY);
            } else {
                memoryCache = null;
                consultasCounter = 0;
            }

            String message = String.format("Agência cadastrada com sucesso, %s", savedAgencia.getId());
            return new AgenciaResponseDTO(message);
        } catch (Exception e) {
            throw new AgenciaCadastroException("Erro ao cadastrar agência: " + e.getMessage());
        }
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
        if (consultas == 1) {
            redisTemplate.expire(COUNTER_KEY, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        }

        boolean renovarPorHits = consultas >= MAX_CONSULTAS;
        boolean temCache = Boolean.TRUE.equals(redisTemplate.hasKey(CACHE_KEY));

        if (temCache && !renovarPorHits) {
            Object cachedData = redisTemplate.opsForValue().get(CACHE_KEY);
            if (cachedData instanceof List<?> list) {
                @SuppressWarnings("unchecked")
                List<AgenciaDTO> agencias = (List<AgenciaDTO>) list;
                String message = String.format("Consulta realizada às %s - %d agências encontradas (cache)",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")), agencias.size());
                return new AgenciaResponseDTO(message);
            }
        }

        List<Agencia> agenciasList = agenciaRepository.findAll();
        List<AgenciaDTO> agencias = agenciasList.stream()
                .map(a -> new AgenciaDTO(a.getId(), a.getPosX(), a.getPosY()))
                .collect(Collectors.toList());

        redisTemplate.opsForValue().set(CACHE_KEY, agencias, CACHE_TIMEOUT_MINUTES, TimeUnit.MINUTES);

        if (renovarPorHits) {
            redisTemplate.delete(COUNTER_KEY);
        }

        String message = String.format("Consulta realizada às %s - %d agências encontradas (fresh + cache)",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")), agencias.size());
        return new AgenciaResponseDTO(message);
    }

    private AgenciaResponseDTO consultarSemRedis() {
        long now = System.currentTimeMillis();
        boolean cacheValido = memoryCache != null && (now - memoryCacheTime) < (CACHE_TIMEOUT_MINUTES * 60 * 1000) && consultasCounter < MAX_CONSULTAS;

        if (cacheValido) {
            consultasCounter++;
            String message = String.format("Consulta realizada às %s - %d agências encontradas (cache em memória)",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")), memoryCache.size());
            return new AgenciaResponseDTO(message);
        }

        List<Agencia> agenciasList = agenciaRepository.findAll();
        memoryCache = agenciasList.stream()
                .map(a -> new AgenciaDTO(a.getId(), a.getPosX(), a.getPosY()))
                .collect(Collectors.toList());

        memoryCacheTime = now;
        consultasCounter = 1;

        String message = String.format("Consulta realizada às %s - %d agências encontradas (sem Redis)",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")), memoryCache.size());
        return new AgenciaResponseDTO(message);
    }
}
