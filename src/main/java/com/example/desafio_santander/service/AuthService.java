package com.example.desafio_santander.service;

import com.example.desafio_santander.dto.auth.AuthResponseDTO;
import com.example.desafio_santander.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthService(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponseDTO authenticate(String username, String password) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            String token = jwtUtil.generateToken(username);
            return new AuthResponseDTO("Bearer " + token);
        } catch (AuthenticationException e) {
            return new AuthResponseDTO(null); // Or handle error as needed
        }
    }
}
