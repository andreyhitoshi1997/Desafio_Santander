package com.example.desafio_santander.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
    }

    @Test
    void testGenerateToken() {
        String username = "testuser";

        String token = jwtUtil.generateToken(username);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testGetUsernameFromToken() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        String extractedUsername = jwtUtil.getUsernameFromToken(token);

        assertEquals(username, extractedUsername);
    }

    @Test
    void testValidateTokenValid() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        boolean isValid = jwtUtil.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    void testValidateTokenInvalid() {
        String invalidToken = "invalid.token.here";

        boolean isValid = jwtUtil.validateToken(invalidToken);

        assertFalse(isValid);
    }

    @Test
    void testIsTokenExpiredFresh() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        boolean isExpired = jwtUtil.isTokenExpired(token);

        assertFalse(isExpired);
    }

    @Test
    void testIsTokenExpiredInvalidToken() {
        String invalidToken = "invalid.token.here";

        assertThrows(Exception.class, () -> {
            jwtUtil.isTokenExpired(invalidToken);
        });
    }
}
