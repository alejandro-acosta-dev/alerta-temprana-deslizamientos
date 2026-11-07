package com.deslizamientos.alerta.repository;

import com.deslizamientos.alerta.model.Lectura;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Capa de datos: por ahora solo responde si una lectura activa existe.
 */
public interface LecturaRepository extends JpaRepository<Lectura, Long> {

    boolean existsByIdAndActivoTrue(Long id);
}
