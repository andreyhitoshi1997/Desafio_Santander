package com.example.desafio_santander.controller.auth;

import com.example.desafio_santander.dto.auth.AuthRequestDTO;
import com.example.desafio_santander.dto.auth.AuthResponseDTO;
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
    private final JwtUtil jwtUtil;
    private final AuthService authService;

    @Autowired
    public AuthController(JwtUtil jwtUtil, AuthService authService) {
        this.jwtUtil = jwtUtil;
        this.authService = authService;
    }

    @PostMapping("autenticacao")
    public AuthResponseDTO authenticate(@RequestBody AuthRequestDTO request) {
        return authService.authenticate(request.getUsuario(), request.getSenha());
    }
}
