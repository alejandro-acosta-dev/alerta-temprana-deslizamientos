package com.deslizamientos.alerta.exception;

import com.deslizamientos.alerta.dto.ErrorDto;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejo centralizado de errores: un solo lugar traduce las excepciones a
 * respuestas HTTP. Por ahora cubre la caída de la base de datos (503); los
 * errores 400, 401, 404 y 409 se agregan con el corte vertical.
 */
@RestControllerAdvice
public class ManejadorGlobalDeErrores {

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorDto> baseDeDatosNoDisponible(DataAccessException ex) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorDto("DOWN", "No hay conexión con la base de datos"));
    }
}
