package com.example.desafio_santander.controller.agencia;

import com.example.desafio_santander.dto.agencia.AgenciaRequestDTO;
import com.example.desafio_santander.dto.agencia.AgenciaResponseDTO;
import com.example.desafio_santander.dto.agencia.DistanciaResponseDTO;
import com.example.desafio_santander.service.AgenciaService;
import com.example.desafio_santander.service.DistanciaUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/desafio")
public class AgenciaController {

    private static final Logger logger = LoggerFactory.getLogger(AgenciaController.class);

    @Autowired
    private AgenciaService agenciaService;

    @Autowired
    private DistanciaUseCase distanciaService;

    @PostMapping("/cadastrar")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<AgenciaResponseDTO> cadastrarAgencia(@Valid @RequestBody AgenciaRequestDTO agenciaRequest) {
        logger.info("Recebido request para cadastrar agência: posX={}, posY={}", agenciaRequest.getPosX(), agenciaRequest.getPosY());

        try {
            AgenciaResponseDTO response = agenciaService.cadastrarAgencia(agenciaRequest);
            logger.info("Agência cadastrada com sucesso: {}", response.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erro ao cadastrar agência: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/distancia")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<DistanciaResponseDTO> consultarDistancias(
            @RequestParam("posX") Integer posX,
            @RequestParam("posY") Integer posY) {
        logger.info("Recebido request para consultar distâncias: posX={}, posY={}", posX, posY);

        try {
            DistanciaResponseDTO distanciaResponse = distanciaService.calcularDistancias(posX, posY);
            logger.info("Distâncias calculadas com sucesso: {} agências processadas",
                distanciaResponse.getDistancias().size());
            return ResponseEntity.ok(distanciaResponse);
        } catch (Exception e) {
            logger.error("Erro ao calcular distâncias: {}", e.getMessage(), e);
            throw e;
        }
    }
}
