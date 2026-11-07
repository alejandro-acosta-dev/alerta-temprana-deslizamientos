package com.deslizamientos.alerta.dto;

import com.deslizamientos.alerta.model.Rol;
import java.time.Instant;

/**
 * Respuesta del login. El cliente envía el token en cada petición con la
 * cabecera "Authorization: Bearer <token>".
 */
public record TokenDto(String token, String tipo, Instant expiraEn, Rol rol) {
}
