package com.deslizamientos.alerta.exception;

/** La operación choca con algo que ya existe (por ejemplo, una alerta repetida). Se convierte en 409. */
public class ConflictoException extends RuntimeException {

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
