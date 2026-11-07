-- SOLO PARA PRUEBAS: deja la tabla alerta vacia y reinicia su contador de ids.
-- Hace falta antes de ejecutar la coleccion de Postman otra vez: el borrado de la API
-- es logico, y el UNIQUE (lectura_id, tipo) tambien cuenta las alertas desactivadas
-- (ADR-003), asi que repetir la coleccion daria 409 en lugar de 201.
-- Con Docker, desde la raiz del repositorio:
--   Get-Content db\pruebas\reiniciar_alertas.sql | docker exec -i alerta-db psql -U alerta_app -d alerta_deslizamientos

TRUNCATE TABLE alerta RESTART IDENTITY;
