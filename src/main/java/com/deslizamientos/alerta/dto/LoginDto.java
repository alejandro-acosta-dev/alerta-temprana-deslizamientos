package com.deslizamientos.alerta.dto;

import jakarta.validation.constraints.NotBlank;

/** Cuerpo del POST /api/auth/login. */
public record LoginDto(
        @NotBlank(message = "es obligatorio") String username,
        @NotBlank(message = "es obligatorio") String password) {
}
