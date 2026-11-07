package com.deslizamientos.alerta.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Capa de datos: la entidad Alerta, mapeada a la tabla "alerta" de la migración V1.
 * Cada atributo tiene el mismo nombre que su columna (diagrama de clases, 3.3).
 * La lectura y el tipo no cambian después de crear la alerta; el nivel y el
 * mensaje sí. Nunca se borra la fila: desactivar() marca activo = false (ADR-003).
 */
@Entity
@Table(name = "alerta")
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lectura_id", nullable = false, updatable = false)
    private Long lecturaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 10)
    private TipoAlerta tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private NivelAlerta nivel;

    @Column(nullable = false, length = 255)
    private String mensaje;

    @Column(name = "generada_en", nullable = false, updatable = false)
    private Instant generadaEn;

    @Column(nullable = false)
    private boolean activo = true;

    /** Lo exige JPA para poder crear la entidad al leer de la base de datos. */
    protected Alerta() {
    }

    public Alerta(Long lecturaId, TipoAlerta tipo, NivelAlerta nivel, String mensaje) {
        this.lecturaId = lecturaId;
        this.tipo = tipo;
        this.nivel = nivel;
        this.mensaje = mensaje;
        this.generadaEn = Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    /** Cambia lo único editable de una alerta: su nivel y su mensaje. */
    public void actualizar(NivelAlerta nivel, String mensaje) {
        this.nivel = nivel;
        this.mensaje = mensaje;
    }

    /** Borrado lógico: la fila se conserva con activo = false. */
    public void desactivar() {
        this.activo = false;
    }

    public Long getId() {
        return id;
    }

    public Long getLecturaId() {
        return lecturaId;
    }

    public TipoAlerta getTipo() {
        return tipo;
    }

    public NivelAlerta getNivel() {
        return nivel;
    }

    public String getMensaje() {
        return mensaje;
    }

    public Instant getGeneradaEn() {
        return generadaEn;
    }

    public boolean isActivo() {
        return activo;
    }
}
