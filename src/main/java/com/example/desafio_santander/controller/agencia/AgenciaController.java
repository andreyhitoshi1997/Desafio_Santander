package com.example.desafio_santander.controller.agencia;

import com.example.desafio_santander.dto.agencia.AgenciaRequestDTO;
import com.example.desafio_santander.dto.agencia.AgenciaResponseDTO;
import com.example.desafio_santander.dto.agencia.DistanciaResponseDTO;
import com.example.desafio_santander.service.AgenciaService;
import com.example.desafio_santander.service.DistanciaService;
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

    @Autowired
    private DistanciaService distanciaService;

    @PostMapping("/cadastrar")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<AgenciaResponseDTO> cadastrarAgencia(@Valid @RequestBody AgenciaRequestDTO agenciaRequest) {
        AgenciaResponseDTO response = agenciaService.cadastrarAgencia(agenciaRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/distancia")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<DistanciaResponseDTO> consultarDistancias(
            @RequestParam("posX") Integer posX,
            @RequestParam("posY") Integer posY) {
        DistanciaResponseDTO distanciaResponse = distanciaService.calcularDistancias(posX, posY);
        return ResponseEntity.ok(distanciaResponse);
    }
}
