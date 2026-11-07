package com.deslizamientos.alerta.model;

/**
 * Por qué se generó la alerta. Coincide con el CHECK ck_alerta_tipo de la tabla:
 * UMBRAL (la lectura superó un límite) o TENDENCIA (las lecturas previas suben).
 */
public enum TipoAlerta {
    UMBRAL,
    TENDENCIA
}
