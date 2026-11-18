package cl.duoc.ms_vidasalud_audit.streaming.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Desde dónde: el servicio que emitió el evento y la IP del cliente. Los eventos anteriores a la EP5 no lo traen. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrigenEvento(String servicio, String ip) {
}
