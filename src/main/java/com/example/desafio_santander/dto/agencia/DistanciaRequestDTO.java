package com.example.desafio_santander.dto.agencia;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistanciaRequestDTO {
    @NotNull(message = "posX é obrigatório")
    private Integer posX;

    @NotNull(message = "posY é obrigatório")
    private Integer posY;
}
