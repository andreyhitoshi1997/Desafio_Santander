package com.example.desafio_santander.exception;

public class AgenciaNotFoundException extends RuntimeException {
    public AgenciaNotFoundException(String message) {
        super(message);
    }
}
