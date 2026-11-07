-- Usuarios de DEMOSTRACION para probar el login y los roles (corte vertical, 5.3).
-- No son credenciales reales: solo existen para la demo y las pruebas en Postman.
-- Las contrasenas se guardan como hash BCrypt, nunca en texto:
--   admin    / Admin2026*     (rol ADMINISTRADOR: puede crear, modificar y borrar alertas)
--   tecnico  / Tecnico2026*   (rol TECNICO: solo puede consultar alertas)
-- Es seguro repetirlo: si el usuario ya existe, no lo duplica.
-- Con Docker, desde la raiz del repositorio:
--   Get-Content db\pruebas\usuarios_demo.sql | docker exec -i alerta-db psql -U alerta_app -d alerta_deslizamientos

INSERT INTO usuario (username, password_hash, rol)
VALUES ('admin', '$2b$10$bvJYAQk5LKO.XKUFmC0ay.wB/SBFzRDEkfo4GGAsK3aJTmW/nKFcm', 'ADMINISTRADOR')
ON CONFLICT (username) DO NOTHING;

INSERT INTO usuario (username, password_hash, rol)
VALUES ('tecnico', '$2b$10$VECDE/RITqk294SpAVoYou4YZJiTYLgyfisLpdK00XXzYkmyxYpjq', 'TECNICO')
ON CONFLICT (username) DO NOTHING;

SELECT id, username, rol, activo FROM usuario ORDER BY id;
