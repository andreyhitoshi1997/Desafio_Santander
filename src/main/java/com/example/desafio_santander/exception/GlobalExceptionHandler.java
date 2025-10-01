package com.example.desafio_santander.exception;

import com.example.desafio_santander.dto.agencia.AgenciaResponseDTO;
import com.example.desafio_santander.dto.agencia.DistanciaResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AgenciaCadastroException.class)
    public ResponseEntity<AgenciaResponseDTO> handleAgenciaCadastroException(AgenciaCadastroException e) {
        logger.error("Erro no cadastro de agência: {}", e.getMessage());
        AgenciaResponseDTO response = new AgenciaResponseDTO(e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(AgenciaNotFoundException.class)
    public ResponseEntity<DistanciaResponseDTO> handleAgenciaNotFoundException(AgenciaNotFoundException e) {
        logger.error("Agência não encontrada: {}", e.getMessage());
        DistanciaResponseDTO response = new DistanciaResponseDTO(new LinkedHashMap<>());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(DistanciaException.class)
    public ResponseEntity<DistanciaResponseDTO> handleDistanciaException(DistanciaException e) {
        logger.error("Erro no cálculo de distâncias: {}", e.getMessage());
        DistanciaResponseDTO response = new DistanciaResponseDTO(new LinkedHashMap<>());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AgenciaResponseDTO> handleValidationExceptions(MethodArgumentNotValidException e) {
        logger.error("Erro de validação: {}", e.getMessage());
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        AgenciaResponseDTO response = new AgenciaResponseDTO("Erro de validação: " + errors.toString());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<AgenciaResponseDTO> handleAccessDeniedException(AccessDeniedException e) {
        logger.error("Acesso negado: {}", e.getMessage());
        AgenciaResponseDTO response = new AgenciaResponseDTO("Acesso negado");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AgenciaResponseDTO> handleGenericException(Exception e) {
        logger.error("Erro interno do servidor: {}", e.getMessage(), e);
        AgenciaResponseDTO response = new AgenciaResponseDTO("Erro interno do servidor: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
