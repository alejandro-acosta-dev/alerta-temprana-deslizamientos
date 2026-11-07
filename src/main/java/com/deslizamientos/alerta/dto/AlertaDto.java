package com.deslizamientos.alerta.dto;

import com.deslizamientos.alerta.model.NivelAlerta;
import com.deslizamientos.alerta.model.TipoAlerta;
import java.time.Instant;

/**
 * Lo que la API devuelve de una alerta. Es un DTO y no la entidad para que el
 * contrato JSON no dependa de cómo está mapeada la tabla.
 */
public record AlertaDto(
        Long id,
        Long lecturaId,
        TipoAlerta tipo,
        NivelAlerta nivel,
        String mensaje,
        Instant generadaEn) {
}
