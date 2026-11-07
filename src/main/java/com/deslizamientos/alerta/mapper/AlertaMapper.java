package com.deslizamientos.alerta.mapper;

import com.deslizamientos.alerta.dto.AlertaCrearDto;
import com.deslizamientos.alerta.dto.AlertaDto;
import com.deslizamientos.alerta.model.Alerta;
import org.springframework.stereotype.Component;

/**
 * Capa de negocio: traduce entre los DTO (lo que viaja por HTTP) y la entidad
 * (lo que se guarda). Así la API puede cambiar su JSON sin tocar la tabla, y al revés.
 */
@Component
public class AlertaMapper {

    public Alerta aEntidad(AlertaCrearDto dto) {
        return new Alerta(dto.lecturaId(), dto.tipo(), dto.nivel(), dto.mensaje().trim());
    }

    public AlertaDto aDto(Alerta alerta) {
        return new AlertaDto(
                alerta.getId(),
                alerta.getLecturaId(),
                alerta.getTipo(),
                alerta.getNivel(),
                alerta.getMensaje(),
                alerta.getGeneradaEn());
    }
}
