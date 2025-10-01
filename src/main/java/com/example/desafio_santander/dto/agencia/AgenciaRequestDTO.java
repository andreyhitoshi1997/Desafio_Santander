package com.example.desafio_santander.dto.agencia;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Negative;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgenciaRequestDTO {
    @NotNull(message = "Posição X é obrigatória")
    @Min(value = -1000, message = "Posição X deve ser maior que -1000")
    @Max(value = 1000, message = "Posição X deve ser menor que 1000")
    private Integer posX;

    @NotNull(message = "Posição Y é obrigatória")
    @Min(value = -1000, message = "Posição Y deve ser maior que -1000")
    @Max(value = 1000, message = "Posição Y deve ser menor que 1000")
    private Integer posY;
}
