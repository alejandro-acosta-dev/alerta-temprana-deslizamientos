package com.deslizamientos.alerta.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Capa de datos: la entidad Lectura (tabla "lectura"). Por ahora solo se lee:
 * el CRUD de Alerta la usa para comprobar que la lectura existe antes de crear
 * una alerta. Sus filas las escribirá el webhook de SMS (HU-01).
 */
@Entity
@Table(name = "lectura")
public class Lectura {

    @Id
    private Long id;

    @Column(name = "sensor_id", nullable = false)
    private Long sensorId;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal humedad;

    @Column(name = "fecha_hora", nullable = false)
    private Instant fechaHora;

    @Column(name = "recibida_en", nullable = false)
    private Instant recibidaEn;

    @Column(nullable = false)
    private boolean activo;

    /** Lo exige JPA para poder crear la entidad al leer de la base de datos. */
    protected Lectura() {
    }

    public Long getId() {
        return id;
    }

    public Long getSensorId() {
        return sensorId;
    }

    public BigDecimal getHumedad() {
        return humedad;
    }

    public Instant getFechaHora() {
        return fechaHora;
    }

    public Instant getRecibidaEn() {
        return recibidaEn;
    }

    public boolean isActivo() {
        return activo;
    }
}
