package com.example.desafio_santander.exception;

public class DistanciaException extends RuntimeException {
    public DistanciaException(String message) {
        super(message);
    }

    public DistanciaException(String message, Throwable cause) {
        super(message, cause);
    }
}
