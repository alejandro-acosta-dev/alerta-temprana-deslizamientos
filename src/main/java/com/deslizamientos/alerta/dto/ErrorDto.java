package com.deslizamientos.alerta.dto;

/**
 * Cuerpo de las respuestas de error. Todos los errores de la API usan esta forma
 * para que el cliente (web o Postman) los lea siempre igual.
 */
public record ErrorDto(String estado, String mensaje) {
}
