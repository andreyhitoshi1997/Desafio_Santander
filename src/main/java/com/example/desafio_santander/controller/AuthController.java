package com.example.desafio_santander.controller;

import com.example.desafio_santander.dto.AuthRequestDTO;
import com.example.desafio_santander.dto.AuthResponseDTO;
import com.example.desafio_santander.security.JwtUtil;
import com.example.desafio_santander.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class AuthController {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AuthService authService;

    @PostMapping("autenticacao")
    public AuthResponseDTO authenticate(@RequestBody AuthRequestDTO request) {
        return authService.authenticate(request.getUsuario(), request.getSenha());
    }
}
