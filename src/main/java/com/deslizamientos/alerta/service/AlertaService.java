package com.deslizamientos.alerta.service;

import com.deslizamientos.alerta.dto.AlertaActualizarDto;
import com.deslizamientos.alerta.dto.AlertaCrearDto;
import com.deslizamientos.alerta.dto.AlertaDto;
import com.deslizamientos.alerta.exception.ConflictoException;
import com.deslizamientos.alerta.exception.RecursoNoEncontradoException;
import com.deslizamientos.alerta.exception.SolicitudInvalidaException;
import com.deslizamientos.alerta.mapper.AlertaMapper;
import com.deslizamientos.alerta.model.Alerta;
import com.deslizamientos.alerta.repository.AlertaRepository;
import com.deslizamientos.alerta.repository.LecturaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Capa de negocio: las reglas del CRUD de Alerta. No sabe nada de HTTP: lanza
 * excepciones y el manejador global decide el código de respuesta.
 * Cada método es una transacción: o se guarda todo o no se guarda nada.
 */
@Service
public class AlertaService {

    private final AlertaRepository alertas;
    private final LecturaRepository lecturas;
    private final AlertaMapper mapper;

    public AlertaService(AlertaRepository alertas, LecturaRepository lecturas, AlertaMapper mapper) {
        this.alertas = alertas;
        this.lecturas = lecturas;
        this.mapper = mapper;
    }

    @Transactional
    public AlertaDto crear(AlertaCrearDto dto) {
        if (!lecturas.existsByIdAndActivoTrue(dto.lecturaId())) {
            throw new SolicitudInvalidaException("La lectura " + dto.lecturaId() + " no existe");
        }
        if (alertas.existsByLecturaIdAndTipo(dto.lecturaId(), dto.tipo())) {
            throw new ConflictoException(
                    "Ya existe una alerta " + dto.tipo() + " para la lectura " + dto.lecturaId());
        }
        Alerta guardada = alertas.save(mapper.aEntidad(dto));
        return mapper.aDto(guardada);
    }

    @Transactional(readOnly = true)
    public List<AlertaDto> listar() {
        return alertas.findByActivoTrueOrderByGeneradaEnDesc().stream()
                .map(mapper::aDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public AlertaDto obtener(Long id) {
        return mapper.aDto(buscarActiva(id));
    }

    @Transactional
    public AlertaDto actualizar(Long id, AlertaActualizarDto dto) {
        Alerta alerta = buscarActiva(id);
        alerta.actualizar(dto.nivel(), dto.mensaje().trim());
        return mapper.aDto(alerta);
    }

    /** Borrado lógico: la alerta queda con activo = false y deja de aparecer en la API. */
    @Transactional
    public void eliminar(Long id) {
        buscarActiva(id).desactivar();
    }

    private Alerta buscarActiva(Long id) {
        return alertas.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la alerta " + id));
    }
}
