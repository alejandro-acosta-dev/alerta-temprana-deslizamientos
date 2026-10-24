package com.deslizamientos.alerta.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Capa de datos: consultas de diagnóstico a la base. Es la única clase del
 * esqueleto que habla con PostgreSQL. Los repositorios de las entidades
 * (Spring Data JPA) se agregan junto con el corte vertical.
 */
@Repository
public class BaseDatosRepository {

    private final JdbcTemplate jdbc;

    public BaseDatosRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Texto completo que devuelve PostgreSQL, por ejemplo "PostgreSQL 18.0 (Debian ...) on x86_64...". */
    public String versionDelMotor() {
        return jdbc.queryForObject("SELECT version()", String.class);
    }

    public String nombreDeLaBase() {
        return jdbc.queryForObject("SELECT current_database()", String.class);
    }

    /** Cuenta cuántas de las 5 tablas de la migración V1 existen en el esquema public. */
    public int contarTablasDelEsquema() {
        Integer cantidad = jdbc.queryForObject("""
                SELECT count(*)
                FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_name IN ('usuario', 'sensor', 'lectura', 'umbral', 'alerta')
                """, Integer.class);
        return cantidad == null ? 0 : cantidad;
    }
}
