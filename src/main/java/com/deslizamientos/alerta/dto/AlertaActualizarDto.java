package com.deslizamientos.alerta.dto;

import com.deslizamientos.alerta.model.NivelAlerta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo del PUT /api/alertas/{id}. Solo se puede cambiar el nivel y el mensaje:
 * la lectura y el tipo identifican a la alerta y no se editan.
 */
public record AlertaActualizarDto(
        @NotNull(message = "es obligatorio (ADVERTENCIA o CRITICO)") NivelAlerta nivel,
        @NotBlank(message = "es obligatorio")
        @Size(max = 255, message = "no puede pasar de 255 caracteres") String mensaje) {
}
