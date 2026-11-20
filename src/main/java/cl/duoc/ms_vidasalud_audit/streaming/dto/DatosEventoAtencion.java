package cl.duoc.ms_vidasalud_audit.streaming.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

/** {@code payload} del evento: la atención, quién hizo el cambio y desde dónde. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DatosEventoAtencion(
        @NotNull @Positive Long atencionId,
        String servicio,
        String box,
        LocalDateTime fechaHora,
        @NotBlank String estado,
        String estadoAnterior,
        ActorEvento actor,
        OrigenEvento origen) {
}
