package com.deslizamientos.alerta.dto;

/**
 * Respuesta de GET /api/estado: dice si la API está arriba y si la base de datos
 * responde y tiene el esquema creado por la migración V1.
 */
public record EstadoDto(String aplicacion, String estado, BaseDatosDto baseDatos) {

    /**
     * @param estado           UP si el esquema está completo; ESQUEMA_INCOMPLETO si faltan tablas
     * @param motor            motor y versión, por ejemplo "PostgreSQL 18.0"
     * @param nombre           nombre de la base a la que se conectó la API
     * @param tablasDelEsquema cuántas de las 5 tablas de la V1 existen
     */
    public record BaseDatosDto(String estado, String motor, String nombre, int tablasDelEsquema) {
    }
}
