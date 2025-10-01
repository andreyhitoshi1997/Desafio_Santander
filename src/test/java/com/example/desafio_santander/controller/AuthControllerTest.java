package com.example.desafio_santander.controller;

import com.example.desafio_santander.controller.auth.AuthController;
import com.example.desafio_santander.dto.auth.AuthRequestDTO;
import com.example.desafio_santander.dto.auth.AuthResponseDTO;
import com.example.desafio_santander.service.AuthService;
import com.example.desafio_santander.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testAuthenticateSuccess() throws Exception {
        AuthRequestDTO requestDTO = new AuthRequestDTO("usuario", "senha123");
        AuthResponseDTO responseDTO = new AuthResponseDTO("Bearer mock-token");

        when(authService.authenticate(anyString(), anyString())).thenReturn(responseDTO);

        mockMvc.perform(post("/autenticacao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bearer").value("Bearer mock-token"));
    }

    @Test
    void testAuthenticateInvalidCredentials() throws Exception {
        AuthRequestDTO requestDTO = new AuthRequestDTO("invalid", "invalid");

        when(authService.authenticate(anyString(), anyString()))
            .thenThrow(new RuntimeException("Credenciais inválidas"));

        mockMvc.perform(post("/autenticacao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testAuthenticateEmptyRequest() throws Exception {
        AuthRequestDTO requestDTO = new AuthRequestDTO(null, null);
        AuthResponseDTO responseDTO = new AuthResponseDTO("Bearer mock-token");

        when(authService.authenticate(anyString(), anyString())).thenReturn(responseDTO);

        mockMvc.perform(post("/autenticacao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk());
    }
}
