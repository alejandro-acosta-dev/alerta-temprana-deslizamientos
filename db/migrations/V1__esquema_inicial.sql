-- Migración inicial del esquema de base de datos
-- Sistema de Alerta Temprana de Deslizamientos (Caso 7)
-- Motor: PostgreSQL 18. Migraciones versionadas con Flyway.
--
-- Este script materializa el diagrama de clases de la sección 3.3 del DAS:
-- cada clase es una tabla y cada atributo es una columna con el mismo nombre.
-- Integridad: PK en toda tabla, FK con REFERENCES, CHECK y UNIQUE.
-- Borrado lógico: toda tabla tiene la columna "activo" (nunca se borran filas).
-- Las FK usan ON DELETE RESTRICT: un borrado físico de una fila con hijos falla.

-- Usuario: quien entra al sistema con JWT. El rol define sus permisos (HU-02, HU-03).
CREATE TABLE usuario (
    id             BIGINT GENERATED ALWAYS AS IDENTITY,
    username       VARCHAR(50)  NOT NULL,
    password_hash  VARCHAR(100) NOT NULL,
    rol            VARCHAR(15)  NOT NULL,
    activo         BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_usuario          PRIMARY KEY (id),
    CONSTRAINT uq_usuario_username UNIQUE (username),
    CONSTRAINT ck_usuario_rol      CHECK (rol IN ('ADMINISTRADOR', 'TECNICO'))
);

-- Sensor: cada uno de los 8 sensores de humedad (HU-03, RF-10).
CREATE TABLE sensor (
    id         BIGINT GENERATED ALWAYS AS IDENTITY,
    codigo     VARCHAR(20)  NOT NULL,
    ubicacion  VARCHAR(100) NOT NULL,
    activo     BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_sensor        PRIMARY KEY (id),
    CONSTRAINT uq_sensor_codigo UNIQUE (codigo)
);

-- Lectura: cada medición de humedad recibida por SMS; es el histórico (HU-01, RF-03).
-- El UNIQUE sobre (sensor_id, fecha_hora) impide registrar dos veces la misma
-- medición: es el origen del 409 cuando el SMS llega repetido.
CREATE TABLE lectura (
    id           BIGINT GENERATED ALWAYS AS IDENTITY,
    sensor_id    BIGINT       NOT NULL,
    humedad      NUMERIC(5,2) NOT NULL,
    fecha_hora   TIMESTAMPTZ  NOT NULL,
    recibida_en  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    activo       BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_lectura        PRIMARY KEY (id),
    CONSTRAINT fk_lectura_sensor FOREIGN KEY (sensor_id)
        REFERENCES sensor (id) ON DELETE RESTRICT,
    CONSTRAINT uq_lectura_sensor_fecha UNIQUE (sensor_id, fecha_hora),
    CONSTRAINT ck_lectura_humedad CHECK (humedad BETWEEN 0 AND 100)
);

-- Umbral: límite de riesgo de un sensor, por nivel (HU-02, RF-08).
-- Un sensor tiene como máximo un umbral por nivel. Para cambiar un límite se
-- edita el valor (o se reactiva el umbral); no se crea uno repetido.
CREATE TABLE umbral (
    id              BIGINT GENERATED ALWAYS AS IDENTITY,
    sensor_id       BIGINT       NOT NULL,
    nivel           VARCHAR(12)  NOT NULL,
    valor           NUMERIC(5,2) NOT NULL,
    modificado_por  BIGINT       NOT NULL,
    modificado_en   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_umbral        PRIMARY KEY (id),
    CONSTRAINT fk_umbral_sensor FOREIGN KEY (sensor_id)
        REFERENCES sensor (id) ON DELETE RESTRICT,
    CONSTRAINT fk_umbral_usuario FOREIGN KEY (modificado_por)
        REFERENCES usuario (id) ON DELETE RESTRICT,
    CONSTRAINT uq_umbral_sensor_nivel UNIQUE (sensor_id, nivel),
    CONSTRAINT ck_umbral_nivel CHECK (nivel IN ('ADVERTENCIA', 'CRITICO')),
    CONSTRAINT ck_umbral_valor CHECK (valor BETWEEN 0 AND 100)
);

-- Alerta: lo que se genera cuando una lectura supera un umbral o muestra
-- tendencia de riesgo (HU-01, RF-06, RF-09). Es la entidad del CRUD del corte vertical.
-- El UNIQUE sobre (lectura_id, tipo) evita generar dos veces el mismo tipo de
-- alerta para la misma lectura: es el origen del 409 en un POST repetido.
CREATE TABLE alerta (
    id           BIGINT GENERATED ALWAYS AS IDENTITY,
    lectura_id   BIGINT       NOT NULL,
    tipo         VARCHAR(10)  NOT NULL,
    nivel        VARCHAR(12)  NOT NULL,
    mensaje      VARCHAR(255) NOT NULL,
    generada_en  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    activo       BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_alerta         PRIMARY KEY (id),
    CONSTRAINT fk_alerta_lectura FOREIGN KEY (lectura_id)
        REFERENCES lectura (id) ON DELETE RESTRICT,
    CONSTRAINT uq_alerta_lectura_tipo UNIQUE (lectura_id, tipo),
    CONSTRAINT ck_alerta_tipo  CHECK (tipo IN ('UMBRAL', 'TENDENCIA')),
    CONSTRAINT ck_alerta_nivel CHECK (nivel IN ('ADVERTENCIA', 'CRITICO'))
);

-- Índices justificados.
-- Los UNIQUE de lectura (sensor_id, fecha_hora), umbral (sensor_id, nivel) y
-- alerta (lectura_id, tipo) ya crean un índice cuya primera columna es la FK, así
-- que las FK sensor_id y lectura_id quedan indexadas sin índices adicionales.
-- El de lectura también atiende la consulta de las últimas lecturas de un sensor
-- (tendencia, RF-05), que debe cumplir el máximo de 5 s (AC-02).
-- Solo falta uno: la lista de alertas activas, de la más reciente a la más antigua,
-- que consulta el técnico (HU-03). Es parcial (solo activas) para que sea más pequeño.
CREATE INDEX idx_alerta_activas_recientes
    ON alerta (generada_en DESC)
    WHERE activo;
