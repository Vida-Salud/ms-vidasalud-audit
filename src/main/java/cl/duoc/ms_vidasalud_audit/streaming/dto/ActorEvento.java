package cl.duoc.ms_vidasalud_audit.streaming.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Quién: claim oid, nombre y rol del token de Azure AD con que se hizo la acción en appointments. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ActorEvento(String id, String nombre, String rol) {
}
