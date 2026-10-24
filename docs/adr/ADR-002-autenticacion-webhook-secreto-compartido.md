# ADR-002: Autenticar el webhook con un secreto compartido en la cabecera X-Webhook-Secret

## Estatus
Aceptada

## Fecha
2026-10-24

## Contexto
El webhook del ADR-001 no lo llama una persona sino un dispositivo (el celular receptor con su aplicación de reenvío). El resto de la API se protege con JWT, que se obtiene iniciando sesión con usuario y contraseña y vence; un dispositivo desatendido no puede hacer ese inicio de sesión.

Si el webhook no se protegiera, cualquiera que conozca la URL podría enviar lecturas falsas: provocar una alerta falsa que desgaste la confianza en el sistema o esconder una lectura real detrás de datos inventados. La Seguridad (AC-06) tiene prioridad Baja en la Ficha de Drivers, así que la protección debe ser de bajo costo, pero la consecuencia de un dato falso sobre riesgo de vida es grave.

Fuerzas en juego:
- AC-06 (Seguridad): solo el celular autorizado puede inyectar lecturas.
- AC-07 (Mantenibilidad): cambiar o rotar la credencial no debe exigir cambios de código.
- RA-02 y la restricción de tiempo (sección 1.4): sin software ni servicios adicionales.
- Supuesto: la aplicación de reenvío permite configurar la URL y cabeceras HTTP fijas, pero no calcular firmas.

Alternativas consideradas:
1. **Secreto compartido estático en una cabecera HTTP.** El celular lo envía en cada petición y la API lo compara con el valor guardado en una variable de entorno.
2. **JWT como el de las personas.** El token vence y para renovarlo hay que iniciar sesión; la aplicación de reenvío no puede. Un JWT de larga duración sería, en la práctica, otro secreto estático pero más complejo.
3. **Firma HMAC del cuerpo con marca de tiempo.** Es más segura porque una petición capturada no se puede repetir, pero la aplicación de reenvío tendría que calcular la firma: habría que programar una aplicación propia o un intermediario.
4. **Lista blanca de direcciones IP.** El celular sale por datos móviles con una IP que cambia; no sirve.
5. **Sin autenticación.** Se descarta por el riesgo descrito arriba.

## Decisión
Decidimos autenticar el webhook con un secreto compartido largo y aleatorio (al menos 32 caracteres) enviado en la cabecera `X-Webhook-Secret`, porque es lo único que una aplicación de reenvío común puede mandar sin código propio (restricción de tiempo y RA-02) y protege la entrada contra lecturas falsas (AC-06). El valor vive en la variable de entorno `WEBHOOK_SECRET` y nunca en el repositorio; la API lo compara en tiempo constante y responde 401 si la cabecera falta o no coincide (flujo alterno 1 de la sección 3.4). Rotarlo es cambiar la variable y reconfigurar la aplicación del celular, sin tocar el código (AC-07).

## Consecuencias

### Positivas
- Implementación mínima: una comparación antes de procesar el mensaje.
- Fácil de verificar con Postman: sin cabecera o con cabecera errónea responde 401; con la cabecera correcta responde 201.
- Rotación sin cambios de código.

### Negativas y deuda técnica asumida
- Es una credencial estática: si se filtra, sirve hasta que se rote.
- Solo es segura sobre HTTPS; en HTTP viaja en claro. En desarrollo (localhost) se usa HTTP y en producción se debe poner un terminador TLS (proxy inverso o certificado). Eso aún no está implementado en el esqueleto.
- No impide que alguien con una petición capturada la repita. Lo mitiga la restricción UNIQUE (sensor_id, fecha_hora): repetir la misma lectura devuelve 409 y no la duplica, pero una lectura nueva falsificada necesita el secreto.
- Todos los dispositivos comparten el mismo secreto: si algún día hubiera más de un celular receptor, no se distinguiría cuál envió el mensaje.
- Si la aplicación de reenvío que se elija no permite cabeceras personalizadas, este ADR se reemplaza (por ejemplo, el secreto en la URL o en el cuerpo).
