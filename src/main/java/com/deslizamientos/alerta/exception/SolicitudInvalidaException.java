package com.deslizamientos.alerta.exception;

/** El JSON es válido pero no tiene sentido para el negocio (por ejemplo, una lectura que no existe). Se convierte en 400. */
public class SolicitudInvalidaException extends RuntimeException {

    public SolicitudInvalidaException(String mensaje) {
        super(mensaje);
    }
}
