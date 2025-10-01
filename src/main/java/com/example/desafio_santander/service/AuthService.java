package com.example.desafio_santander.service;

import com.example.desafio_santander.dto.AuthResponseDTO;
import com.example.desafio_santander.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtil jwtUtil;

    public AuthResponseDTO authenticate(String username, String password) {
        try {
            Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            String token = jwtUtil.generateToken(username);
            return new AuthResponseDTO("Bearer " + token);
        } catch (AuthenticationException e) {
            return new AuthResponseDTO(null); // Or handle error as needed
        }
    }
}
