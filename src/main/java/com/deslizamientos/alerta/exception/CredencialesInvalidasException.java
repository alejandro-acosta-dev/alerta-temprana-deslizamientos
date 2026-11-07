package com.deslizamientos.alerta.exception;

/** Usuario o contraseña incorrectos en el login. El manejador la convierte en 401. */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
