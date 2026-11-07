package com.deslizamientos.alerta.controller;

import com.deslizamientos.alerta.dto.EstadoDto;
import com.deslizamientos.alerta.service.EstadoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Capa de presentación: recibe la petición HTTP y la pasa al servicio.
 * GET /api/estado comprueba que la API está arriba y que la base de datos responde.
 */
@RestController
@RequestMapping("/api")
public class EstadoController {

    private final EstadoService servicio;

    public EstadoController(EstadoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/estado")
    public EstadoDto estado() {
        return servicio.consultar();
    }
}
