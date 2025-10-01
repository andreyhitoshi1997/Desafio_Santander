package com.example.desafio_santander.dto.agencia;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgenciaResponseDTO {
    private List<AgenciaDTO> agencias;
    private String message;
    private boolean cacheRenovado;
}
