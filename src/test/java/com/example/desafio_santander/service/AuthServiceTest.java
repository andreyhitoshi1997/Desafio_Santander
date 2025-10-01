package com.example.desafio_santander.service;

import com.example.desafio_santander.dto.auth.AuthResponseDTO;
import com.example.desafio_santander.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void testAuthenticateValidCredentials() {
        String validUser = "usuario";
        String validPassword = "senha123";
        String mockToken = "mock-jwt-token";

        when(jwtUtil.generateToken(anyString())).thenReturn(mockToken);

        AuthResponseDTO result = authService.authenticate(validUser, validPassword);

        assertNotNull(result);
        assertNotNull(result.getBearer());
        assertTrue(result.getBearer().startsWith("Bearer "));
        assertTrue(result.getBearer().contains(mockToken));
    }

    @Test
    void testAuthenticateInvalidUser() {
        assertThrows(RuntimeException.class, () -> {
            authService.authenticate("invalid", "senha123");
        });
    }

    @Test
    void testAuthenticateInvalidPassword() {
        assertThrows(RuntimeException.class, () -> {
            authService.authenticate("usuario", "invalid");
        });
    }

    @Test
    void testAuthenticateInvalidCredentials() {
        assertThrows(RuntimeException.class, () -> {
            authService.authenticate("invalid", "invalid");
        });
    }
}
