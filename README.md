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
En desarrollo. El DAS vigente (v0.5) está en `docs/DAS.pdf`; los ADRs, en `docs/adr/`. La API es hoy un esqueleto: arranca, aplica la migración de la base de datos y responde `GET /api/estado`. El CRUD de alertas (corte vertical) se agrega en las siguientes entregas.

## Requisitos
- JDK 21 (`java -version` debe mostrar 21).
- Una base PostgreSQL 18: con Docker (opción A, la más corta) o instalada en el equipo (opción B).
- No hace falta instalar Maven: el proyecto trae el Maven Wrapper (`mvnw`). La primera vez descarga Maven, por lo que se necesita internet.

## Instalación y ejecución

Todos los comandos se ejecutan desde la carpeta raíz del repositorio.

**1. Crear el archivo de variables de entorno**

- Windows (PowerShell): `Copy-Item .env.example .env`
- Linux o macOS: `cp .env.example .env`

Abrir `.env` y cambiar `DB_PASSWORD` por una clave propia. El archivo `.env` no se sube a Git.

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

Al arrancar, Flyway aplica `db/migrations/V1__esquema_inicial.sql` y crea las 5 tablas. La API queda escuchando en el puerto 8080.

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

También responde `http://localhost:8080/actuator/health` con `{"status":"UP"}`. Si la base está apagada, `/api/estado` responde 503 y `/actuator/health` responde `{"status":"DOWN"}`.

## Pruebas
- Pruebas unitarias (no necesitan la base de datos): `.\mvnw.cmd test` en Windows o `./mvnw test` en Linux y macOS.
- La colección de Postman y las pruebas de integración del corte vertical se agregan en las siguientes entregas (`postman/`).

## Problemas frecuentes
- `Could not resolve placeholder 'DB_PASSWORD'` (o `DB_USER`, `DB_NAME`): falta el archivo `.env` o le falta esa línea (paso 1).
- `Connection refused` o `Connection to localhost:5432 refused`: la base no está arriba (paso 2).
- `password authentication failed`: la clave del `.env` no es la que tiene el rol en PostgreSQL. Si usó Docker y cambió la clave después de la primera vez, ejecutar `docker compose down -v` y volver a levantar la base (borra los datos de desarrollo).

## Estructura del repositorio
- `docs/`: DAS (Word y PDF), `adr/` con los registros de decisiones y `diagramas/` con las fuentes `.puml` y los `.png`.
- `db/migrations/`: migraciones de Flyway (`V1__esquema_inicial.sql`). `db/pruebas/`: pruebas de integridad del esquema.
- `src/main/java/com/deslizamientos/alerta/`: código de la API, una carpeta por capa del patrón N-Capas: `controller` y `dto` (presentación), `service` (negocio), `repository` (datos) y `exception` (manejo centralizado de errores). Las carpetas `model` y `mapper` se crean junto con las entidades.
- `src/main/resources/application.yml`: configuración; los valores sensibles se leen de variables de entorno.
- `src/test/java/`: pruebas con JUnit y Mockito.
- `docker-compose.yml`: base de datos PostgreSQL 18 para desarrollo.
- `.env.example`: nombres de las variables de entorno (sin valores reales).
- `postman/`: colección de pruebas (pendiente).

## Variables de entorno
Los nombres están en `.env.example`; la explicación de cada una está en la sección 3.5 del DAS. Las que la API usa hoy son `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` y `SERVER_PORT`.
