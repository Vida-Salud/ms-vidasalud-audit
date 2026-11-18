package cl.duoc.ms_vidasalud_audit.service;

import java.time.Instant;

/** Una entrada del timeline, sin nada de Kafka. */
public record NuevaEntrada(String eventId, String tipo, Instant ocurridoEn, Long atencionId,
                           String estadoAnterior, String estado, String actorId, String actorNombre,
                           String actorRol, String origenServicio, String origenIp, String traceId) {
}
