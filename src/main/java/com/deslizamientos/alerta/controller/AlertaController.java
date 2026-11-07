package com.deslizamientos.alerta.controller;

import com.deslizamientos.alerta.dto.AlertaActualizarDto;
import com.deslizamientos.alerta.dto.AlertaCrearDto;
import com.deslizamientos.alerta.dto.AlertaDto;
import com.deslizamientos.alerta.service.AlertaService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Capa de presentación: recibe la petición HTTP, valida el JSON y se lo pasa al
 * servicio. No tiene reglas de negocio ni SQL.
 *   POST   /api/alertas       crea (201 y cabecera Location)
 *   GET    /api/alertas       lista las activas
 *   GET    /api/alertas/{id}  una alerta (404 si no existe)
 *   PUT    /api/alertas/{id}  cambia nivel y mensaje
 *   DELETE /api/alertas/{id}  borrado lógico (204)
 */
@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    private final AlertaService servicio;

    public AlertaController(AlertaService servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public ResponseEntity<AlertaDto> crear(@Valid @RequestBody AlertaCrearDto dto) {
        AlertaDto creada = servicio.crear(dto);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creada.id())
                .toUri();
        return ResponseEntity.created(ubicacion).body(creada);
    }

    @GetMapping
    public List<AlertaDto> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public AlertaDto obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @PutMapping("/{id}")
    public AlertaDto actualizar(@PathVariable Long id, @Valid @RequestBody AlertaActualizarDto dto) {
        return servicio.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }
}
