package com.example.desafio_santander.controller;

import com.example.desafio_santander.controller.agencia.AgenciaController;
import com.example.desafio_santander.dto.agencia.AgenciaRequestDTO;
import com.example.desafio_santander.dto.agencia.AgenciaResponseDTO;
import com.example.desafio_santander.dto.agencia.DistanciaResponseDTO;
import com.example.desafio_santander.service.AgenciaService;
import com.example.desafio_santander.service.DistanciaUseCase;
import com.example.desafio_santander.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AgenciaController.class)
@ActiveProfiles("test")
class AgenciaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AgenciaService agenciaService;

    @MockBean
    private DistanciaUseCase distanciaService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "USER")
    void testCadastrarAgencia() throws Exception {
        AgenciaRequestDTO requestDTO = new AgenciaRequestDTO(10, 20);
        AgenciaResponseDTO responseDTO = new AgenciaResponseDTO("Agência cadastrada com sucesso, 1");

        when(agenciaService.cadastrarAgencia(any(AgenciaRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/desafio/cadastrar")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Agência cadastrada com sucesso, 1"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testConsultarDistancias() throws Exception {
        DistanciaResponseDTO responseDTO = new DistanciaResponseDTO(
            Map.of("AGENCIA_1", "distância = 10.00", "AGENCIA_2", "distância = 20.00")
        );

        when(distanciaService.calcularDistancias(anyInt(), anyInt())).thenReturn(responseDTO);

        mockMvc.perform(get("/desafio/distancia")
                .param("posX", "0")
                .param("posY", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.distancias").exists());
    }

    @Test
    void testCadastrarAgenciaSemAutenticacao() throws Exception {
        AgenciaRequestDTO requestDTO = new AgenciaRequestDTO(10, 20);

        mockMvc.perform(post("/desafio/cadastrar")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isUnauthorized());
    }
}
