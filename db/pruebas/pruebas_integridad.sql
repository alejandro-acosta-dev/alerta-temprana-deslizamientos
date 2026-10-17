-- Pruebas de integridad del esquema (sección 5.2 del DAS)
-- Ejecutar sobre una base de datos recién migrada (V1__esquema_inicial.sql) y vacía.
-- Ejemplo con psql: psql -d nombre_de_la_base -f db/pruebas/pruebas_integridad.sql
-- Primero lista tablas, restricciones e índices; luego carga datos válidos (deben
-- insertarse) y por último intenta datos inválidos (cada uno debe mostrar un ERROR).
-- Los errores son lo esperado: demuestran que la base de datos hace cumplir las reglas.

\set ON_ERROR_STOP off
\pset pager off
\echo '== Tablas creadas =='
\dt
\echo '== Restricciones por tabla (p=PK, f=FK, u=UNIQUE, c=CHECK) =='
SELECT conrelid::regclass AS tabla, contype AS tipo, conname AS restriccion
FROM pg_constraint
WHERE connamespace = 'public'::regnamespace
ORDER BY conrelid::regclass::text, contype, conname;
\echo '== Indices =='
SELECT tablename AS tabla, indexname AS indice FROM pg_indexes WHERE schemaname = 'public' ORDER BY tablename, indexname;

\echo '== Datos validos: deben insertarse =='
INSERT INTO usuario (username, password_hash, rol) VALUES ('admin', 'hash-de-prueba', 'ADMINISTRADOR');
INSERT INTO sensor (codigo, ubicacion) VALUES ('S-01', 'Vereda El Roble, parte alta');
INSERT INTO umbral (sensor_id, nivel, valor, modificado_por) VALUES (1, 'CRITICO', 85.00, 1);
INSERT INTO lectura (sensor_id, humedad, fecha_hora) VALUES (1, 91.50, '2026-10-17 03:00:00-05');
INSERT INTO alerta (lectura_id, tipo, nivel, mensaje) VALUES (1, 'UMBRAL', 'CRITICO', 'Humedad por encima del umbral critico');

\echo '== T1 rol invalido: debe fallar (CHECK) =='
INSERT INTO usuario (username, password_hash, rol) VALUES ('x', 'h', 'INVITADO');
\echo '== T2 humedad fuera de rango: debe fallar (CHECK) =='
INSERT INTO lectura (sensor_id, humedad, fecha_hora) VALUES (1, 150, '2026-10-17 04:00:00-05');
\echo '== T3 lectura repetida del mismo sensor y hora: debe fallar (UNIQUE, el 409) =='
INSERT INTO lectura (sensor_id, humedad, fecha_hora) VALUES (1, 92.00, '2026-10-17 03:00:00-05');
\echo '== T4 sensor inexistente: debe fallar (FK) =='
INSERT INTO lectura (sensor_id, humedad, fecha_hora) VALUES (999, 50, '2026-10-17 05:00:00-05');
\echo '== T5 tipo de alerta invalido: debe fallar (CHECK) =='
INSERT INTO alerta (lectura_id, tipo, nivel, mensaje) VALUES (1, 'OTRO', 'CRITICO', 'm');
\echo '== T6 alerta repetida para la misma lectura y tipo: debe fallar (UNIQUE) =='
INSERT INTO alerta (lectura_id, tipo, nivel, mensaje) VALUES (1, 'UMBRAL', 'ADVERTENCIA', 'm');
\echo '== T7 codigo de sensor repetido: debe fallar (UNIQUE) =='
INSERT INTO sensor (codigo, ubicacion) VALUES ('S-01', 'otra');
\echo '== T8 borrado fisico de un sensor con lecturas: debe fallar (FK RESTRICT) =='
DELETE FROM sensor WHERE id = 1;
\echo '== T9 borrado logico: debe funcionar y la fila sigue en la tabla =='
UPDATE sensor SET activo = FALSE WHERE id = 1;
SELECT id, codigo, activo FROM sensor;
\echo '== T10 el indice parcial puede atender la consulta del tecnico (se fuerza porque la tabla de prueba es diminuta) =='
SET enable_seqscan = off;
EXPLAIN (COSTS OFF) SELECT id, tipo, nivel FROM alerta WHERE activo ORDER BY generada_en DESC LIMIT 20;
