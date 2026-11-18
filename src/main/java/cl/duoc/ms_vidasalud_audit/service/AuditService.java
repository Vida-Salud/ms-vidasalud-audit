package cl.duoc.ms_vidasalud_audit.service;

import cl.duoc.ms_vidasalud_audit.dto.EntradaAuditoriaResponse;

import java.time.LocalDate;
import java.util.List;

public interface AuditService {

    /** Guarda la entrada. Devuelve false si ya existía (mismo eventId). */
    boolean registrar(NuevaEntrada entrada);

    List<EntradaAuditoriaResponse> buscar(String usuario, String tipo, Long atencionId, LocalDate desde, LocalDate hasta);

    List<EntradaAuditoriaResponse> timeline(Long atencionId);
}
