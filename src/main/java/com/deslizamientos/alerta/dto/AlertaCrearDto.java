package com.deslizamientos.alerta.dto;

import com.deslizamientos.alerta.model.NivelAlerta;
import com.deslizamientos.alerta.model.TipoAlerta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo del POST /api/alertas. Si algún campo incumple una regla, Spring
 * responde 400 antes de que la petición llegue al servicio.
 */
public record AlertaCrearDto(
        @NotNull(message = "es obligatorio") Long lecturaId,
        @NotNull(message = "es obligatorio (UMBRAL o TENDENCIA)") TipoAlerta tipo,
        @NotNull(message = "es obligatorio (ADVERTENCIA o CRITICO)") NivelAlerta nivel,
        @NotBlank(message = "es obligatorio")
        @Size(max = 255, message = "no puede pasar de 255 caracteres") String mensaje) {
}
