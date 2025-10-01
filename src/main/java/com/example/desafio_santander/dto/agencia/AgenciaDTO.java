package com.example.desafio_santander.dto.agencia;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgenciaDTO {
    private Long id;
    private int posX;
    private int posY;
}
