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
    public ResponseEntity<String> cadastrarAgencia(@Valid @RequestBody AgenciaRequestDTO agenciaRequest) {
        try {
            String message = agenciaService.cadastrarAgencia(agenciaRequest);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao cadastrar agência: " + e.getMessage());
        }
    }

    @GetMapping("/consultar")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<AgenciaResponseDTO> consultarAgencias() {
        try {
            AgenciaResponseDTO agenciaResponse = agenciaService.consultarAgencias();
            return ResponseEntity.ok(agenciaResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
