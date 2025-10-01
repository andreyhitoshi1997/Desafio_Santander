package com.example.desafio_santander.service;

import com.example.desafio_santander.dto.auth.AuthResponseDTO;
import com.example.desafio_santander.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public AuthResponseDTO authenticate(String username, String password) {
        if ("usuario".equals(username) && "senha123".equals(password)) {
            String token = jwtUtil.generateToken(username);
            return new AuthResponseDTO("Bearer " + token);
        } else {
            throw new RuntimeException("Credenciais inválidas");
        }
    }
}
