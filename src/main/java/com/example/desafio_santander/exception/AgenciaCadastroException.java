package com.example.desafio_santander.exception;

public class AgenciaCadastroException extends RuntimeException {
    public AgenciaCadastroException(String message) {
        super(message);
    }

    public AgenciaCadastroException(String message, Throwable cause) {
        super(message, cause);
    }
}
