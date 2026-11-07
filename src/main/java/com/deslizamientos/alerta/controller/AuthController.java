package com.deslizamientos.alerta.controller;

import com.deslizamientos.alerta.dto.LoginDto;
import com.deslizamientos.alerta.dto.TokenDto;
import com.deslizamientos.alerta.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Capa de presentación: POST /api/auth/login recibe usuario y contraseña y
 * devuelve un token JWT (200) o 401 si las credenciales son incorrectas.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService servicio;

    public AuthController(AuthService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/login")
    public TokenDto iniciarSesion(@Valid @RequestBody LoginDto dto) {
        return servicio.iniciarSesion(dto);
    }
}
