package com.deslizamientos.alerta.exception;

/** El recurso pedido no existe (o está desactivado). El manejador la convierte en 404. */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
