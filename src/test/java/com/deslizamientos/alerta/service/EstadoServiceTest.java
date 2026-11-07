package com.deslizamientos.alerta.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.deslizamientos.alerta.dto.EstadoDto;
import com.deslizamientos.alerta.repository.BaseDatosRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Prueba de la capa de negocio sin base de datos: el repositorio se reemplaza
 * por un doble (Mockito). Que se pueda probar así es lo que muestra que las
 * capas están separadas (AC-07).
 */
@ExtendWith(MockitoExtension.class)
class EstadoServiceTest {

    private static final String VERSION_COMPLETA =
            "PostgreSQL 18.0 (Debian 18.0-1) on x86_64-pc-linux-gnu, compiled by gcc, 64-bit";

    @Mock
    private BaseDatosRepository repositorio;

    @InjectMocks
    private EstadoService servicio;

    @Test
    void con_las_cinco_tablas_el_estado_es_UP() {
        when(repositorio.versionDelMotor()).thenReturn(VERSION_COMPLETA);
        when(repositorio.nombreDeLaBase()).thenReturn("alerta_deslizamientos");
        when(repositorio.contarTablasDelEsquema()).thenReturn(EstadoService.TABLAS_ESPERADAS);

        EstadoDto respuesta = servicio.consultar();

        assertEquals("UP", respuesta.estado());
        assertEquals("UP", respuesta.baseDatos().estado());
        assertEquals("PostgreSQL 18.0 (Debian 18.0-1)", respuesta.baseDatos().motor());
        assertEquals("alerta_deslizamientos", respuesta.baseDatos().nombre());
        assertEquals(5, respuesta.baseDatos().tablasDelEsquema());
    }

    @Test
    void con_tablas_faltantes_el_estado_es_DEGRADADO() {
        when(repositorio.versionDelMotor()).thenReturn(VERSION_COMPLETA);
        when(repositorio.nombreDeLaBase()).thenReturn("alerta_deslizamientos");
        when(repositorio.contarTablasDelEsquema()).thenReturn(3);

        EstadoDto respuesta = servicio.consultar();

        assertEquals("DEGRADADO", respuesta.estado());
        assertEquals("ESQUEMA_INCOMPLETO", respuesta.baseDatos().estado());
        assertEquals(3, respuesta.baseDatos().tablasDelEsquema());
    }
}
