# ADR-003: Usar borrado lógico con un campo activo en todas las tablas

## Estatus
Aceptada

## Fecha
2026-10-24

## Contexto
Cuando el sistema "elimina" un registro (un sensor retirado, un umbral que se deja de usar, un usuario que sale) hay que decidir si la fila desaparece o queda marcada. La plantilla del curso exige borrado lógico en toda entidad de negocio (sección 5.2), pero además hay razones propias del caso:
- La detección de tendencia (RF-05) necesita el histórico de lecturas de cada sensor (RF-03); borrarlas debilita el análisis.
- La trazabilidad de quién y cuándo cambió un umbral (HU-02, AC-06) y el historial de alertas emitidas (RF-09) son evidencia útil ante un deslizamiento.
- Los errores humanos de borrado deben poder deshacerse.

Alternativas consideradas:
1. **Borrado físico (DELETE).** Es lo más simple y no deja filas inactivas, pero se pierde el historial y no se puede deshacer.
2. **Borrado lógico con una columna `activo BOOLEAN NOT NULL DEFAULT TRUE`.** La fila se conserva y queda marcada como inactiva.
3. **Borrado físico más una tabla de archivo o auditoría.** Conserva el historial en otra tabla, pero exige más tablas y disparadores (triggers) para copiar cada fila antes de borrarla.

## Decisión
Decidimos usar borrado lógico con la columna `activo` en las cinco tablas (usuario, sensor, lectura, umbral y alerta), porque conserva el histórico que alimenta la tendencia (RF-03, RF-05) y la trazabilidad (RF-09, AC-06), permite reactivar un registro borrado por error y cumple la sección 5.2 de la plantilla. Las llaves foráneas usan `ON DELETE RESTRICT`, de modo que un borrado físico accidental de una fila referenciada también falla. En la API, la operación DELETE hace un `UPDATE ... SET activo = FALSE` y responde 204.

## Consecuencias

### Positivas
- El historial de lecturas y alertas se conserva completo.
- Un sensor o umbral desactivado se puede reactivar sin perder su historia.
- La prueba del corte vertical es directa: DELETE devuelve 204 y la fila sigue en la base con `activo = FALSE`.

### Negativas y deuda técnica asumida
- Toda consulta debe filtrar `activo = TRUE`; olvidarlo muestra datos "borrados". Se centraliza en los repositorios para que no dependa de cada consulta. El índice parcial `idx_alerta_activas_recientes` (WHERE activo) ya atiende la consulta del técnico (HU-03).
- Las restricciones UNIQUE siguen contando las filas inactivas: un umbral desactivado no se puede volver a crear con el mismo sensor y nivel, se reactiva o se edita. Alternativa si estorbara: un índice único parcial (WHERE activo), con la migración correspondiente.
- La base crece sin límite. Con una lectura por sensor por hora serían unas 70 000 filas al año en `lectura` (8 sensores por 24 horas por 365 días), un volumen pequeño; si creciera, habría que definir una política de archivo.
- Los datos de los usuarios no se eliminan físicamente. El caso no identifica normativa de protección de datos (sección 1.4); si apareciera una, habría que agregar anonimización o borrado físico para esa tabla.
