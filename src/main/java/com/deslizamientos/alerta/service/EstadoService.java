package com.deslizamientos.alerta.service;

import com.deslizamientos.alerta.dto.EstadoDto;
import com.deslizamientos.alerta.repository.BaseDatosRepository;
import org.springframework.stereotype.Service;

/**
 * Capa de negocio: decide qué significa "la base de datos está bien". No sabe
 * nada de HTTP ni de SQL: le pide los datos al repositorio y arma la respuesta.
 */
@Service
public class EstadoService {

    /** Tablas que crea la migración V1: usuario, sensor, lectura, umbral y alerta. */
    static final int TABLAS_ESPERADAS = 5;

    private final BaseDatosRepository repositorio;

    public EstadoService(BaseDatosRepository repositorio) {
        this.repositorio = repositorio;
    }

    public EstadoDto consultar() {
        String motor = recortarVersion(repositorio.versionDelMotor());
        String nombre = repositorio.nombreDeLaBase();
        int tablas = repositorio.contarTablasDelEsquema();

        boolean esquemaCompleto = tablas == TABLAS_ESPERADAS;
        String estadoBaseDatos = esquemaCompleto ? "UP" : "ESQUEMA_INCOMPLETO";
        String estadoGeneral = esquemaCompleto ? "UP" : "DEGRADADO";

        return new EstadoDto(
                "alerta-temprana-deslizamientos",
                estadoGeneral,
                new EstadoDto.BaseDatosDto(estadoBaseDatos, motor, nombre, tablas));
    }

    /** Deja solo la parte inicial de version(): "PostgreSQL 18.0 (Debian ...)". */
    private String recortarVersion(String versionCompleta) {
        int corte = versionCompleta.indexOf(" on ");
        return corte > 0 ? versionCompleta.substring(0, corte) : versionCompleta;
    }
}
