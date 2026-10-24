# ADR-001: Recibir los SMS de los sensores mediante un webhook HTTP

## Estatus
Aceptada

## Fecha
2026-10-24

## Contexto
Los 8 sensores de humedad envían sus lecturas por SMS, en lotes cada hora, a un celular que hoy un técnico revisa a mano (RT-02, RT-03). El hardware no se puede reemplazar ni modificar (RT-01) y no hay presupuesto para infraestructura adicional (RA-02). El caso no explica cómo pasa un SMS del celular a un sistema de software; es el supuesto que la sección 3.1 del DAS deja por confirmar con el cliente.

Fuerzas en juego:
- AC-02 (Eficiencia de desempeño, prioridad Alta): el sistema debe procesar y evaluar cada lectura en 5 segundos o menos desde que la recibe.
- AC-05 (Confiabilidad, prioridad Alta): no hay turno de guardia nocturno (RA-03), así que la recepción no puede depender de que una persona revise el celular.
- AC-07 (Mantenibilidad, prioridad Alta): el mecanismo de entrada debe poder cambiar sin tocar las reglas de negocio ni el esquema de la base de datos.
- RA-02: no se puede gastar en infraestructura adicional.

Alternativas consideradas:
1. **Webhook HTTP alimentado por el celular receptor actual.** Una aplicación de reenvío de SMS instalada en ese celular manda cada mensaje, como una petición HTTPS, a la API.
2. **Módem GSM USB conectado al servidor.** La SIM del celular pasaría al módem y la API leería los SMS con comandos AT. Obliga a comprar el módem y a mantener un puerto serial y su controlador en el servidor.
3. **Gateway de SMS de pago en la nube.** El proveedor entrega cada SMS por HTTP, pero los sensores tendrían que enviar al número del proveedor: habría que reconfigurar los 8 sensores, algo que el caso no permite asumir (RT-01), y se paga por mensaje (RA-02).
4. **Seguir con la revisión manual del técnico.** Se descarta: es el problema que el caso quiere resolver (RA-03).

## Decisión
Decidimos recibir los SMS mediante un webhook HTTP (`POST /api/lecturas/sms`) al que el celular receptor existente reenvía cada mensaje, porque:
- mantiene el canal SMS y el número de los sensores sin tocar su hardware (RT-01, RT-02);
- no tiene costo, a diferencia del módem y del gateway (RA-02);
- hace que el SMS entre al sistema en el momento en que llega al celular, sin depender de una persona (RA-03), lo que permite cumplir los 5 segundos de AC-02 y la entrega sin guardia de AC-05;
- deja la recepción aislada en un solo controlador: si después se pasa a módem o gateway, solo cambia quién llama a ese endpoint y no las reglas de negocio (AC-07).

## Consecuencias

### Positivas
- Costo cero: no se compra ni se instala hardware.
- En desarrollo el SMS se simula con Postman usando el mismo contrato HTTP que usará el celular.
- Los reintentos son seguros: si la aplicación de reenvío manda dos veces el mismo mensaje, la restricción UNIQUE (sensor_id, fecha_hora) rechaza el segundo con 409 y no se duplica la lectura.
- Cambiar de mecanismo de entrada no afecta la capa de negocio.

### Negativas y deuda técnica asumida
- El celular receptor sigue siendo un punto único de falla (batería, cobertura, internet, aplicación cerrada por Android). Esta decisión no lo elimina. Mitigación parcial: la consulta de estado de sensores (HU-03) muestra qué sensor dejó de reportar.
- Depende de una aplicación de terceros instalada en el celular, cuyos reintentos y formato de envío no controlamos.
- Si la API está caída cuando llega el SMS y la aplicación no reintenta, la lectura se pierde: no hay cola de mensajes ni broker (descartados en la sección 2.1 por costo y por puntos de falla adicionales).
- Supuestos sin confirmar con el cliente: que el celular admita una aplicación de reenvío y tenga conexión a internet, y cuál es el formato exacto del SMS (sensor, fecha y hora, humedad). Si el cliente no los confirma, este ADR se reemplaza por uno nuevo (módem o gateway).
- La API queda expuesta a internet, por lo que el webhook necesita autenticación (ADR-002) y HTTPS en producción.
