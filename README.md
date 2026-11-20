# VidaSalud - Microservicio de Auditoría

Arma el timeline de auditoría (quién / qué / cuándo / desde dónde) con dos consumidores Kafka:

1. Grupo `vidasalud-audit`: lee `appointments.events` y publica la entrada en `audit.timeline` (clave = atencionId).
2. Grupo `vidasalud-audit-timeline`: lee `audit.timeline` y la guarda en Oracle (`AUD_EVENTO`).

| | |
| --- | --- |
| Puerto | 8096 |
| API (solo lectura, Admin y Auditor) | `GET /api/v1/audit?usuario=&type=&atencionId=&from=&to=` · `GET /api/v1/audit/appointments/{id}` |
| Kafka | consume `appointments.events` y `audit.timeline` · produce `audit.timeline` · DLT `<tópico>.audit.DLT` |
| Serializers | por tópico (`DelegatingByTopicSerializer`): JSON para `audit.timeline`, bytes originales para los DLT |

La publicación en `audit.timeline` es síncrona: si Kafka no confirma, el error handler reintenta y el offset
de `appointments.events` no avanza, así ninguna entrada se pierde en silencio.

Variables: `AZURE_TENANT_ID`, `AZURE_CLIENT_ID`, `DB_*`, `KAFKA_BOOTSTRAP_SERVERS` (ver `.env.example`).
