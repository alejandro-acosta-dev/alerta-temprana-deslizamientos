package com.deslizamientos.alerta.exception;

import com.deslizamientos.alerta.dto.ErrorDto;
import java.util.stream.Collectors;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Manejo centralizado de errores: un solo lugar traduce las excepciones a
 * respuestas HTTP, todas con la forma de ErrorDto. Los controladores y servicios
 * lanzan excepciones y no arman respuestas de error.
 * Cubre 400, 404, 409 y 503; el 401 y el 403 llegan con la seguridad (JWT).
 * No hay un manejador genérico de Exception: convertiría los 404 de rutas
 * inexistentes en 500.
 */
@RestControllerAdvice
public class ManejadorGlobalDeErrores {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> datosInvalidos(MethodArgumentNotValidException ex) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        return respuesta(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Datos inválidos: " + detalle);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDto> cuerpoIlegible(HttpMessageNotReadableException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                "El cuerpo no es un JSON válido o trae un valor no permitido (revise tipo y nivel)");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDto> parametroConTipoIncorrecto(MethodArgumentTypeMismatchException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                "El parámetro '" + ex.getName() + "' tiene un valor que no corresponde");
    }

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ResponseEntity<ErrorDto> solicitudInvalida(SolicitudInvalidaException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage());
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorDto> noEncontrado(RecursoNoEncontradoException ex) {
        return respuesta(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ErrorDto> conflicto(ConflictoException ex) {
        return respuesta(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage());
    }

    /** Red de seguridad: si dos peticiones iguales llegan a la vez, el UNIQUE de la base de datos también responde 409. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorDto> integridadViolada(DataIntegrityViolationException ex) {
        return respuesta(HttpStatus.CONFLICT, "CONFLICT",
                "La operación viola una restricción de integridad de la base de datos");
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorDto> baseDeDatosNoDisponible(DataAccessException ex) {
        return respuesta(HttpStatus.SERVICE_UNAVAILABLE, "DOWN", "No hay conexión con la base de datos");
    }

    private ResponseEntity<ErrorDto> respuesta(HttpStatus estado, String codigo, String mensaje) {
        return ResponseEntity.status(estado).body(new ErrorDto(codigo, mensaje));
    }
}
