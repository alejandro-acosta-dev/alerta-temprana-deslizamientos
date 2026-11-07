package com.deslizamientos.alerta.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deslizamientos.alerta.dto.AlertaCrearDto;
import com.deslizamientos.alerta.dto.AlertaDto;
import com.deslizamientos.alerta.exception.ConflictoException;
import com.deslizamientos.alerta.exception.RecursoNoEncontradoException;
import com.deslizamientos.alerta.exception.SolicitudInvalidaException;
import com.deslizamientos.alerta.mapper.AlertaMapper;
import com.deslizamientos.alerta.model.Alerta;
import com.deslizamientos.alerta.model.NivelAlerta;
import com.deslizamientos.alerta.model.TipoAlerta;
import com.deslizamientos.alerta.repository.AlertaRepository;
import com.deslizamientos.alerta.repository.LecturaRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Pruebas de las reglas del CRUD de Alerta sin base de datos ni HTTP: los
 * repositorios se reemplazan por dobles (Mockito). Cada excepción que se prueba
 * aquí es la que el manejador global convierte en 400, 404 o 409.
 */
@ExtendWith(MockitoExtension.class)
class AlertaServiceTest {

    @Mock
    private AlertaRepository alertas;

    @Mock
    private LecturaRepository lecturas;

    private AlertaService servicio;

    @BeforeEach
    void preparar() {
        servicio = new AlertaService(alertas, lecturas, new AlertaMapper());
    }

    private AlertaCrearDto datosValidos() {
        return new AlertaCrearDto(1L, TipoAlerta.UMBRAL, NivelAlerta.CRITICO, "  Humedad sobre el umbral crítico ");
    }

    @Test
    void crear_con_datos_validos_guarda_y_devuelve_la_alerta() {
        when(lecturas.existsByIdAndActivoTrue(1L)).thenReturn(true);
        when(alertas.existsByLecturaIdAndTipo(1L, TipoAlerta.UMBRAL)).thenReturn(false);
        when(alertas.save(any(Alerta.class))).thenAnswer(llamada -> llamada.getArgument(0));

        AlertaDto creada = servicio.crear(datosValidos());

        assertEquals(1L, creada.lecturaId());
        assertEquals(TipoAlerta.UMBRAL, creada.tipo());
        assertEquals(NivelAlerta.CRITICO, creada.nivel());
        assertEquals("Humedad sobre el umbral crítico", creada.mensaje());
    }

    @Test
    void crear_con_una_lectura_que_no_existe_es_solicitud_invalida_400() {
        when(lecturas.existsByIdAndActivoTrue(1L)).thenReturn(false);

        assertThrows(SolicitudInvalidaException.class, () -> servicio.crear(datosValidos()));
        verify(alertas, never()).save(any(Alerta.class));
    }

    @Test
    void crear_una_alerta_repetida_para_la_misma_lectura_y_tipo_es_conflicto_409() {
        when(lecturas.existsByIdAndActivoTrue(1L)).thenReturn(true);
        when(alertas.existsByLecturaIdAndTipo(1L, TipoAlerta.UMBRAL)).thenReturn(true);

        assertThrows(ConflictoException.class, () -> servicio.crear(datosValidos()));
        verify(alertas, never()).save(any(Alerta.class));
    }

    @Test
    void obtener_una_alerta_inexistente_es_no_encontrado_404() {
        when(alertas.findByIdAndActivoTrue(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.obtener(99L));
    }

    @Test
    void eliminar_hace_borrado_logico_y_no_borra_la_fila() {
        Alerta alerta = new Alerta(1L, TipoAlerta.UMBRAL, NivelAlerta.CRITICO, "mensaje");
        when(alertas.findByIdAndActivoTrue(5L)).thenReturn(Optional.of(alerta));

        servicio.eliminar(5L);

        assertFalse(alerta.isActivo());
        verify(alertas, never()).delete(any(Alerta.class));
        verify(alertas, never()).deleteById(any(Long.class));
    }
}
