package com.deslizamientos.alerta.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Los errores de seguridad ocurren en un filtro, antes de llegar a un controlador,
 * así que el manejador global no los ve. Esta clase los responde con la misma forma
 * JSON que el resto de errores de la API (estado y mensaje).
 *   401: falta el token, está vencido o no es válido.
 *   403: el token es válido, pero el rol no tiene permiso.
 */
@Component
public class RespuestasDeSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(HttpServletRequest peticion, HttpServletResponse respuesta,
                         AuthenticationException ex) throws IOException {
        escribir(respuesta, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED",
                "Falta el token o no es válido. Inicie sesión en /api/auth/login");
    }

    @Override
    public void handle(HttpServletRequest peticion, HttpServletResponse respuesta,
                       AccessDeniedException ex) throws IOException {
        escribir(respuesta, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN",
                "Su rol no tiene permiso para esta operación");
    }

    private void escribir(HttpServletResponse respuesta, int estado, String codigo, String mensaje)
            throws IOException {
        respuesta.setStatus(estado);
        respuesta.setContentType("application/json;charset=UTF-8");
        respuesta.getWriter().write("{\"estado\":\"" + codigo + "\",\"mensaje\":\"" + mensaje + "\"}");
    }
}
