package cl.duoc.ms_vidasalud_audit.streaming.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Evento v1 de {@code appointments.events} (contrato en infra/docs/eventos-v1.md), del lado consumidor.
 * Copia propia del contrato: este servicio no depende de las clases del productor. Ignora los campos que
 * no conoce: el productor puede agregar campos opcionales sin cambiar la versión.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EventoAtencion(
        Integer version,
        @NotBlank String eventId,
        @NotBlank String type,
        @NotNull Instant timestamp,
        String correlationId,
        String traceId,
        @NotNull @Valid DatosEventoAtencion payload) {
}
