package cl.duoc.ms_vidasalud_audit.dto;

import java.time.Instant;

public record EntradaAuditoriaResponse(
        String eventId,
        String tipo,
        Instant ocurridoEn,
        Long atencionId,
        String estadoAnterior,
        String estado,
        String actorId,
        String actorNombre,
        String actorRol,
        String origenServicio,
        String origenIp,
        String traceId) {
}
