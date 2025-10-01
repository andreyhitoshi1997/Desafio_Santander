package com.example.desafio_santander.service;

import com.example.desafio_santander.dto.agencia.AgenciaDTO;
import com.example.desafio_santander.dto.agencia.DistanciaResponseDTO;
import com.example.desafio_santander.exception.DistanciaException;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DistanciaServiceTest {

    @Mock
    private AgenciaRepository agenciaRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private DistanciaService distanciaService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testCalcularDistanciasComRedis_PrimeiraConsulta() {
        // Arrange
        List<Agencia> agenciasList = Arrays.asList(
            new Agencia(1L, 5, 10),
            new Agencia(2L, -3, 7),
            new Agencia(3L, 0, 0)
        );

        when(valueOperations.increment("distancias_consultas_counter")).thenReturn(1L);
        when(redisTemplate.hasKey("distancias_cache")).thenReturn(false);
        when(agenciaRepository.findAll()).thenReturn(agenciasList);

        // Act
        DistanciaResponseDTO result = distanciaService.calcularDistancias(-10, 5);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getDistancias());
        assertEquals(3, result.getDistancias().size());
        assertTrue(result.getDistancias().containsKey("AGENCIA_1"));
        assertTrue(result.getDistancias().containsKey("AGENCIA_2"));
        assertTrue(result.getDistancias().containsKey("AGENCIA_3"));

        verify(agenciaRepository, times(1)).findAll();
        verify(valueOperations, times(1)).increment("distancias_consultas_counter");
    }

    @Test
    void testCalcularDistanciasComRedis_UsandoCache() {
        // Arrange
        List<AgenciaDTO> cachedAgencias = Arrays.asList(
            new AgenciaDTO(1L, 5, 10),
            new AgenciaDTO(2L, -3, 7)
        );

        when(valueOperations.increment("distancias_consultas_counter")).thenReturn(5L);
        when(redisTemplate.hasKey("distancias_cache")).thenReturn(true);
        when(valueOperations.get("distancias_cache")).thenReturn(cachedAgencias);

        // Act
        DistanciaResponseDTO result = distanciaService.calcularDistancias(0, 0);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getDistancias().size());
        verify(agenciaRepository, never()).findAll(); // Cache foi usado, não consultou DB
    }

    @Test
    void testCalcularDistanciasComRedis_RenovarAposMaxConsultas() {
        // Arrange
        List<Agencia> agenciasList = Arrays.asList(
            new Agencia(1L, 10, 15)
        );

        when(valueOperations.increment("distancias_consultas_counter")).thenReturn(10L);
        when(agenciaRepository.findAll()).thenReturn(agenciasList);

        // Act
        DistanciaResponseDTO result = distanciaService.calcularDistancias(0, 0);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getDistancias().size());
        verify(agenciaRepository).findAll(); // Cache foi renovado
        verify(redisTemplate).delete("distancias_consultas_counter");
    }

    @Test
    void testCalcularDistancias_NenhumaAgenciaEncontrada() {
        // Arrange
        when(valueOperations.increment("distancias_consultas_counter")).thenReturn(1L);
        when(redisTemplate.hasKey("distancias_cache")).thenReturn(false);
        when(agenciaRepository.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        DistanciaException exception = assertThrows(DistanciaException.class, () -> {
            distanciaService.calcularDistancias(0, 0);
        });

        assertTrue(exception.getMessage().contains("Nenhuma agência encontrada"));
    }

    @Test
    void testCalcularEOrdenarDistancias_OrdemCorreta() {
        // Arrange
        List<Agencia> agenciasList = Arrays.asList(
            new Agencia(1L, 10, 0),  // Distância = 10 de (0,0)
            new Agencia(2L, 3, 4),   // Distância = 5 de (0,0)
            new Agencia(3L, 0, 0)    // Distância = 0 de (0,0)
        );

        when(valueOperations.increment("distancias_consultas_counter")).thenReturn(1L);
        when(redisTemplate.hasKey("distancias_cache")).thenReturn(false);
        when(agenciaRepository.findAll()).thenReturn(agenciasList);

        // Act
        DistanciaResponseDTO result = distanciaService.calcularDistancias(0, 0);

        // Assert
        assertNotNull(result.getDistancias());
        Map<String, String> distancias = result.getDistancias();

        // Verificar se estão ordenadas por distância (mais próxima primeiro)
        String[] keys = distancias.keySet().toArray(new String[0]);
        assertEquals("AGENCIA_3", keys[0]); // Distância 0.00
        assertEquals("AGENCIA_2", keys[1]); // Distância 5.00
        assertEquals("AGENCIA_1", keys[2]); // Distância 10.00

        assertTrue(distancias.get("AGENCIA_3").contains("0.00"));
        assertTrue(distancias.get("AGENCIA_2").contains("5.00"));
        assertTrue(distancias.get("AGENCIA_1").contains("10.00"));
    }

    @Test
    void testCalcularDistancias_ComPosicaoNegativa() {
        // Arrange
        List<Agencia> agenciasList = Arrays.asList(
            new Agencia(1L, -5, -5)
        );

        when(valueOperations.increment("distancias_consultas_counter")).thenReturn(1L);
        when(redisTemplate.hasKey("distancias_cache")).thenReturn(false);
        when(agenciaRepository.findAll()).thenReturn(agenciasList);

        // Act
        DistanciaResponseDTO result = distanciaService.calcularDistancias(-10, -10);

        // Assert
        assertNotNull(result.getDistancias());
        assertTrue(result.getDistancias().containsKey("AGENCIA_1"));

        // Distância entre (-10,-10) e (-5,-5) = sqrt(25+25) = sqrt(50) ≈ 7.07
        String distancia = result.getDistancias().get("AGENCIA_1");
        assertTrue(distancia.contains("7.07"));
    }

    @Test
    void testCalcularDistancias_ErroInternoCapturado() {
        // Arrange
        when(valueOperations.increment(anyString())).thenThrow(new RuntimeException("Erro do Redis"));

        // Act & Assert
        DistanciaException exception = assertThrows(DistanciaException.class, () -> {
            distanciaService.calcularDistancias(0, 0);
        });

        assertTrue(exception.getMessage().contains("Erro interno ao calcular distâncias"));
    }

    @Test
    void testCalcularDistancias_CacheComAgenciasVazias() {
        // Arrange - testar quando cache retorna lista vazia mas deveria renovar
        when(valueOperations.increment("distancias_consultas_counter")).thenReturn(5L);
        when(redisTemplate.hasKey("distancias_cache")).thenReturn(true);
        when(valueOperations.get("distancias_cache")).thenReturn(null); // Cache retorna null, forçando renovação
        when(agenciaRepository.findAll()).thenReturn(Arrays.asList(new Agencia(1L, 0, 0)));

        // Act
        DistanciaResponseDTO result = distanciaService.calcularDistancias(0, 0);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getDistancias().size());
        verify(agenciaRepository).findAll(); // Renovação do cache quando não havia dados válidos
    }
}
