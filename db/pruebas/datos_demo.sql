-- Datos minimos para probar el CRUD de Alerta en Postman (corte vertical, 5.3).
-- El CRUD de Alerta necesita una lectura existente (FK lectura_id), y las lecturas
-- las escribira el webhook de SMS mas adelante. Mientras tanto se cargan a mano.
-- Es seguro repetirlo: si los datos ya existen, no los duplica.
-- Se ejecuta sobre la base ya migrada. Con Docker, desde la raiz del repositorio:
--   Get-Content db\pruebas\datos_demo.sql | docker exec -i alerta-db psql -U alerta_app -d alerta_deslizamientos

INSERT INTO sensor (codigo, ubicacion)
VALUES ('S-01', 'Vereda El Roble, parte alta')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO lectura (sensor_id, humedad, fecha_hora)
SELECT id, 91.50, '2026-11-06 03:00:00-05' FROM sensor WHERE codigo = 'S-01'
ON CONFLICT (sensor_id, fecha_hora) DO NOTHING;

INSERT INTO lectura (sensor_id, humedad, fecha_hora)
SELECT id, 78.00, '2026-11-06 04:00:00-05' FROM sensor WHERE codigo = 'S-01'
ON CONFLICT (sensor_id, fecha_hora) DO NOTHING;

SELECT id, sensor_id, humedad, fecha_hora FROM lectura ORDER BY id;
