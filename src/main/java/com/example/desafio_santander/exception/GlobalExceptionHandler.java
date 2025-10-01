package com.example.desafio_santander.exception;

import com.example.desafio_santander.dto.agencia.AgenciaResponseDTO;
import com.example.desafio_santander.dto.agencia.DistanciaResponseDTO;
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

    @ExceptionHandler(AgenciaCadastroException.class)
    public ResponseEntity<AgenciaResponseDTO> handleAgenciaCadastroException(AgenciaCadastroException e) {
        AgenciaResponseDTO response = new AgenciaResponseDTO(e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(AgenciaNotFoundException.class)
    public ResponseEntity<DistanciaResponseDTO> handleAgenciaNotFoundException(AgenciaNotFoundException e) {
        DistanciaResponseDTO response = new DistanciaResponseDTO(new LinkedHashMap<>());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(DistanciaException.class)
    public ResponseEntity<DistanciaResponseDTO> handleDistanciaException(DistanciaException e) {
        DistanciaResponseDTO response = new DistanciaResponseDTO(new LinkedHashMap<>());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AgenciaResponseDTO> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        AgenciaResponseDTO response = new AgenciaResponseDTO(errors.toString());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<AgenciaResponseDTO> handleAccessDeniedException(AccessDeniedException e) {
        String message = e.getMessage() != null ? e.getMessage() : "Acesso negado";
        AgenciaResponseDTO response = new AgenciaResponseDTO(message);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AgenciaResponseDTO> handleGenericException(Exception e) {
        String message = e.getMessage() != null ? e.getMessage() : "Erro interno do servidor";
        AgenciaResponseDTO response = new AgenciaResponseDTO(message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
