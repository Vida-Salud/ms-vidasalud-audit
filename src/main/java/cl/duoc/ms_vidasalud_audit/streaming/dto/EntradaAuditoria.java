package cl.duoc.ms_vidasalud_audit.streaming.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

/**
 * Entrada v1 de {@code audit.timeline}: quién, qué, cuándo y desde dónde. Clave del registro = atencionId,
 * para que la compactación del tópico conserve el último estado conocido de cada atención.
 * Contrato en infra/docs/eventos-v1.md.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EntradaAuditoria(
        Integer version,
        @NotBlank String eventId,
        @NotBlank String type,
        @NotNull Instant ocurridoEn,
        @NotNull @Positive Long atencionId,
        String estadoAnterior,
        @NotBlank String estado,
        String actorId,
        String actorNombre,
        String actorRol,
        String origenServicio,
        String origenIp,
        String traceId) {

    public static final int VERSION_ACTUAL = 1;
}
