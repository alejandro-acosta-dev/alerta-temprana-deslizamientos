# alerta-temprana-deslizamientos

## Descripción
Sistema de procesamiento y alerta temprana de riesgo de deslizamiento, a partir de una red de 8 sensores de humedad ya existente, sin modificar su hardware, priorizando disponibilidad y tiempo de respuesta.

## Contexto académico
- **Materia:** Arquitectura de Software — Uniremington
- **Docente:** Gloria Amparo Lora Patiño
- **Estudiante:** Alejandro Acosta
- **Entregable:** Documento de Arquitectura de Software (DAS) — arc42 simplificado + C4 + ADRs + Corte Vertical
- **Caso asignado:** Caso 7 — Alerta temprana de deslizamientos

## Estado del proyecto
DAS v1.0 en `docs/DAS.pdf` (y `docs/DAS.docx`); los ADRs, en `docs/adr/`.

**Implementado (corte vertical, sección 5 del DAS):** base de datos de 5 tablas con Flyway, CRUD de la entidad Alerta con borrado lógico y errores 400, 404 y 409, y autenticación con JWT con dos roles (ADMINISTRADOR y TECNICO; errores 401 y 403).

**Diseñado en el DAS, todavía sin implementar:** webhook `POST /api/lecturas/sms`, evaluación de umbral y tendencia, notificaciones por correo y Telegram, configuración de umbrales e interfaz web (React). El DAS los marca como «Pendiente» (secciones 1.1 y 3.5) y evalúa su efecto en la sección 6.

## Requisitos
- JDK 21 (`java -version` debe mostrar 21).
- Una base PostgreSQL 18: con Docker (opción A, la más corta) o instalada en el equipo (opción B).
- No hace falta instalar Maven: el proyecto trae el Maven Wrapper (`mvnw`). La primera vez descarga Maven, por lo que se necesita internet.
- Postman, para ejecutar la colección de pruebas.

## Instalación y ejecución

Todos los comandos se ejecutan desde la carpeta raíz del repositorio.

**1. Crear el archivo de variables de entorno**

- Windows (PowerShell): `Copy-Item .env.example .env`
- Linux o macOS: `cp .env.example .env`

Abrir `.env` y cambiar `DB_PASSWORD` por una clave propia y `JWT_SECRET` por un valor propio de al menos 32 caracteres (sin él la API no arranca). El archivo `.env` no se sube a Git.

**2. Levantar la base de datos PostgreSQL 18** (elegir una opción)

- Opción A, con Docker: `docker compose up -d db`
- Opción B, con PostgreSQL 18 instalado: crear el rol y la base con los mismos valores del `.env` (como usuario administrador de PostgreSQL):

```sql
CREATE ROLE alerta_app LOGIN PASSWORD 'la_misma_clave_del_env';
CREATE DATABASE alerta_deslizamientos OWNER alerta_app;
```

**3. Arrancar la API**

- Windows (PowerShell): `.\mvnw.cmd spring-boot:run`
- Linux o macOS: `./mvnw spring-boot:run` (si no tiene permiso de ejecución: `sh mvnw spring-boot:run`)

Al arrancar, Flyway aplica `db/migrations/V1__esquema_inicial.sql` y crea las 5 tablas. La API queda escuchando en el puerto 8080 (si está ocupado, defina `SERVER_PORT` en el `.env`, por ejemplo `SERVER_PORT=8081`).

**4. Verificar el servidor y la conexión a la base de datos**

Abrir en el navegador `http://localhost:8080/api/estado`. La respuesta esperada es:

```json
{
  "aplicacion": "alerta-temprana-deslizamientos",
  "estado": "UP",
  "baseDatos": {
    "estado": "UP",
    "motor": "PostgreSQL 18.0 (...)",
    "nombre": "alerta_deslizamientos",
    "tablasDelEsquema": 5
  }
}
```

También responde `http://localhost:8080/actuator/health` con `{"status":"UP"}`. Si la base está apagada, `/api/estado` responde 503 y `/actuator/health` responde `{"status":"DOWN"}`. Estas dos rutas son públicas; el resto de la API pide token.

**5. Cargar los datos de demostración** (con la API ya arrancada, para que las tablas existan)

Son un sensor con dos lecturas (el CRUD de Alerta necesita una lectura existente) y dos usuarios de demostración, uno por rol. Es seguro repetirlo.

- Opción A, con Docker, en Windows (PowerShell):

```powershell
Get-Content db\pruebas\datos_demo.sql | docker exec -i alerta-db psql -U alerta_app -d alerta_deslizamientos
Get-Content db\pruebas\usuarios_demo.sql | docker exec -i alerta-db psql -U alerta_app -d alerta_deslizamientos
```

- Opción A, con Docker, en Linux o macOS:

```bash
docker exec -i alerta-db psql -U alerta_app -d alerta_deslizamientos < db/pruebas/datos_demo.sql
docker exec -i alerta-db psql -U alerta_app -d alerta_deslizamientos < db/pruebas/usuarios_demo.sql
```

- Opción B, con PostgreSQL instalado:

```bash
psql -U alerta_app -d alerta_deslizamientos -f db/pruebas/datos_demo.sql
psql -U alerta_app -d alerta_deslizamientos -f db/pruebas/usuarios_demo.sql
```

Usuarios de demostración (solo para pruebas; se guardan como hash BCrypt):

| Usuario | Contraseña | Rol | Puede |
|---|---|---|---|
| `admin` | `Admin2026*` | ADMINISTRADOR | Consultar, crear, modificar y borrar alertas |
| `tecnico` | `Tecnico2026*` | TECNICO | Solo consultar alertas |

## Usar la API

1. `POST /api/auth/login` con `{"username": "admin", "password": "Admin2026*"}` devuelve 200 y un `token`.
2. En las demás peticiones se envía la cabecera `Authorization: Bearer <token>`.
3. `POST /api/alertas` con `{"lecturaId": 1, "tipo": "UMBRAL", "nivel": "CRITICO", "mensaje": "Humedad sobre el umbral crítico"}` devuelve 201 con la alerta y la cabecera `Location`.
4. `GET /api/alertas/{id}` devuelve 200 con la alerta, y `GET /api/alertas` lista las activas.
5. La fila queda en la base: `SELECT id, lectura_id, tipo, nivel, activo FROM alerta;`.

| Método y ruta | Quién puede | Respuestas |
|---|---|---|
| `POST /api/auth/login` | Cualquiera | 200, 400, 401 |
| `POST /api/alertas` | ADMINISTRADOR | 201, 400, 401, 403, 409 |
| `GET /api/alertas` y `GET /api/alertas/{id}` | ADMINISTRADOR y TECNICO | 200, 401, 404 |
| `PUT /api/alertas/{id}` | ADMINISTRADOR | 200, 400, 401, 403, 404 |
| `DELETE /api/alertas/{id}` | ADMINISTRADOR | 204 (borrado lógico), 401, 403, 404 |

Todos los errores tienen la forma `{"estado": "CONFLICT", "mensaje": "..."}`. El detalle de cada capa y de la seguridad está en la sección 5.3 del DAS.

## Pruebas

**Pruebas unitarias** (10 pruebas con JUnit y Mockito; no necesitan la base de datos): `.\mvnw.cmd test` en Windows o `./mvnw test` en Linux y macOS. Deben terminar en `BUILD SUCCESS`.

**Colección de Postman** (16 peticiones con 32 pruebas automáticas, sección 5.4 del DAS):

1. Con la base y la API en marcha y los datos de demostración cargados (paso 5).
2. En Postman: *Import* y elegir `postman/alerta-temprana.postman_collection.json`.
3. La colección apunta a `http://localhost:8081` (variable `baseUrl`). Si la API corre en el puerto 8080, cambiar esa variable al puerto real.
4. Dejar las alertas vacías (ver abajo) y ejecutar la colección con *Run collection* (Collection Runner), en el orden en que están las peticiones. Resultado esperado: 16 peticiones y 32 pruebas en verde.

El borrado de la API es lógico y el `UNIQUE (lectura_id, tipo)` también cuenta las alertas borradas, así que **antes de repetir la ejecución** hay que vaciar las alertas (solo para pruebas); si no, la petición 04 responde 409 en lugar de 201:

- Con Docker, en Windows (PowerShell): `Get-Content db\pruebas\reiniciar_alertas.sql | docker exec -i alerta-db psql -U alerta_app -d alerta_deslizamientos`
- Con Docker, en Linux o macOS: `docker exec -i alerta-db psql -U alerta_app -d alerta_deslizamientos < db/pruebas/reiniciar_alertas.sql`
- Con PostgreSQL instalado: `psql -U alerta_app -d alerta_deslizamientos -f db/pruebas/reiniciar_alertas.sql`

## Problemas frecuentes
- `Could not resolve placeholder 'DB_PASSWORD'` (o `DB_USER`, `DB_NAME`, `JWT_SECRET`): falta el archivo `.env` o le falta esa línea (paso 1).
- `JWT_SECRET debe tener al menos 32 caracteres`: el secreto del `.env` es más corto (paso 1).
- `Connection refused` o `Connection to localhost:5432 refused`: la base no está arriba (paso 2). Con Docker, abrir primero Docker Desktop.
- `password authentication failed`: la clave del `.env` no es la que tiene el rol en PostgreSQL. Si usó Docker y cambió la clave después de la primera vez, ejecutar `docker compose down -v` y volver a levantar la base (borra los datos de desarrollo).
- `Port 8080 was already in use`: otro programa usa el puerto; defina `SERVER_PORT` en el `.env` (paso 3).
- 401 en todas las peticiones: falta la cabecera `Authorization: Bearer <token>` o el token venció (dura 60 minutos); iniciar sesión otra vez.
- 400 al crear una alerta con `lecturaId` 1: no se cargaron los datos de demostración (paso 5).

## Estructura del repositorio
- `docs/`: DAS (Word y PDF), `adr/` con los registros de decisiones y `diagramas/` con las fuentes `.puml` y los `.png`.
- `db/migrations/`: migraciones de Flyway (`V1__esquema_inicial.sql`). `db/pruebas/`: pruebas de integridad del esquema, datos de demostración (`datos_demo.sql`, `usuarios_demo.sql`) y `reiniciar_alertas.sql`.
- `src/main/java/com/deslizamientos/alerta/`: código de la API, una carpeta por capa del patrón N-Capas: `controller` y `dto` (presentación), `service` y `mapper` (negocio), `repository` y `model` (datos). Aparte, `exception` (manejo centralizado de errores) y `security` (token y roles), que atraviesan las capas.
- `src/main/resources/application.yml`: configuración; los valores sensibles se leen de variables de entorno.
- `src/test/java/`: pruebas unitarias con JUnit y Mockito.
- `docker-compose.yml`: base de datos PostgreSQL 18 para desarrollo.
- `.env.example`: nombres de las variables de entorno (sin valores reales).
- `postman/`: colección de pruebas con tests automáticos.

## Variables de entorno
Los nombres están en `.env.example`; la explicación de cada una está en la sección 3.5 del DAS. Las que la API usa hoy son `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `SERVER_PORT`, `JWT_SECRET` y `JWT_EXPIRACION_MINUTOS` (opcional, 60 por defecto). `WEBHOOK_SECRET`, `SMTP_*` y `TELEGRAM_*` están reservadas para funciones aún pendientes.
