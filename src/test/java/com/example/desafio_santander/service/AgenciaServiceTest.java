package com.example.desafio_santander.service;

import com.example.desafio_santander.dto.agencia.AgenciaRequestDTO;
import com.example.desafio_santander.dto.agencia.AgenciaResponseDTO;
import com.example.desafio_santander.model.Agencia;
import com.example.desafio_santander.repository.AgenciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgenciaServiceTest {

    @Mock
    private AgenciaRepository agenciaRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private AgenciaService agenciaService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testCadastrarAgenciaSuccess() {
        AgenciaRequestDTO dto = new AgenciaRequestDTO(10, -5);
        Agencia savedAgencia = new Agencia(1L, 10, -5);

        when(agenciaRepository.save(any(Agencia.class))).thenReturn(savedAgencia);

        String result = agenciaService.cadastrarAgencia(dto);

        assertEquals("Agência cadastrada com sucesso, ID: 1", result);
        verify(agenciaRepository).save(any(Agencia.class));
    }

    @Test
    void testConsultarAgenciasFirstTime() {
        List<Agencia> agenciasList = Arrays.asList(
            new Agencia(1L, 10, -5),
            new Agencia(2L, 20, 15)
        );

        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(redisTemplate.hasKey("agencias_cache")).thenReturn(false);
        when(agenciaRepository.findAll()).thenReturn(agenciasList);

        AgenciaResponseDTO result = agenciaService.consultarAgencias();

        assertNotNull(result);
        assertEquals(2, result.getAgencias().size());
        assertTrue(result.getMessage().contains("2 agências encontradas"));
        assertTrue(result.isCacheRenovado());
    }

    @Test
    void testConsultarAgenciasWithCache() {
        when(valueOperations.increment(anyString())).thenReturn(5L);
        when(redisTemplate.hasKey("agencias_cache")).thenReturn(true);
        when(valueOperations.get("agencias_cache")).thenReturn(Collections.emptyList());

        AgenciaResponseDTO result = agenciaService.consultarAgencias();

        assertNotNull(result);
        assertFalse(result.isCacheRenovado());
    }

    @Test
    void testConsultarAgenciasRenovateAfterMaxQueries() {
        List<Agencia> agenciasList = Collections.singletonList(new Agencia(1L, 10, -5));

        when(valueOperations.increment(anyString())).thenReturn(10L);
        when(agenciaRepository.findAll()).thenReturn(agenciasList);

        AgenciaResponseDTO result = agenciaService.consultarAgencias();

        assertNotNull(result);
        assertTrue(result.isCacheRenovado());
    }
}
