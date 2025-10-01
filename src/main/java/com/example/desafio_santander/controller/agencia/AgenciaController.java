package com.example.desafio_santander.controller.agencia;

import com.example.desafio_santander.dto.agencia.AgenciaRequestDTO;
import com.example.desafio_santander.dto.agencia.AgenciaResponseDTO;
import com.example.desafio_santander.service.AgenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/desafio")
public class AgenciaController {
    @Autowired
    private AgenciaService agenciaService;

    @PostMapping("/cadastrar")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> cadastrarAgencia(@Valid @RequestBody AgenciaRequestDTO dto) {
        String message = agenciaService.cadastrarAgencia(dto);
        return ResponseEntity.ok(message);
    }
}
