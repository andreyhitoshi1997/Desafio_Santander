package com.example.desafio_santander.service;

import com.example.desafio_santander.dto.agencia.DistanciaResponseDTO;

public interface DistanciaUseCase {
    DistanciaResponseDTO calcularDistancias(int userPosX, int userPosY);
}