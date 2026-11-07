package com.deslizamientos.alerta.model;

/**
 * Rol de un usuario. Coincide con el CHECK ck_usuario_rol de la tabla.
 * ADMINISTRADOR configura el sistema (HU-02); TECNICO consulta alertas (HU-03).
 */
public enum Rol {
    ADMINISTRADOR,
    TECNICO
}
