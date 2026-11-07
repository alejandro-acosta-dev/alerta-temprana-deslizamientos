package com.deslizamientos.alerta.repository;

import com.deslizamientos.alerta.model.Alerta;
import com.deslizamientos.alerta.model.TipoAlerta;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Capa de datos: aquí vive el filtro "activo = TRUE" (ADR-003), para que ninguna
 * otra capa tenga que acordarse de él. Spring Data arma el SQL a partir del nombre
 * de cada método.
 */
public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    /** Las alertas activas, de la más reciente a la más antigua (usa idx_alerta_activas_recientes). */
    List<Alerta> findByActivoTrueOrderByGeneradaEnDesc();

    Optional<Alerta> findByIdAndActivoTrue(Long id);

    /**
     * No filtra por activo a propósito: el UNIQUE (lectura_id, tipo) de la tabla
     * también cuenta las filas desactivadas (ADR-003), así que la pregunta debe ser la misma.
     */
    boolean existsByLecturaIdAndTipo(Long lecturaId, TipoAlerta tipo);
}
