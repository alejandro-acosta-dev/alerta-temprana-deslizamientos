package com.deslizamientos.alerta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de arranque de la API del Sistema de Alerta Temprana de Deslizamientos.
 *
 * Capas del patrón N-Capas (DAS, sección 2.1), una por paquete:
 *   presentación: controller y dto
 *   negocio:      service (y mapper cuando existan entidades)
 *   datos:        repository (y model cuando existan entidades)
 */
@SpringBootApplication
public class AlertaApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlertaApplication.class, args);
    }
}
