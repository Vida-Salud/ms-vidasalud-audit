package cl.duoc.ms_vidasalud_audit.controller;

import cl.duoc.ms_vidasalud_audit.dto.EntradaAuditoriaResponse;
import cl.duoc.ms_vidasalud_audit.service.AuditService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** Timeline de auditoría, solo lectura (Admin y Auditor). Filtros del caso: usuario, fechas y tipo de evento. */
@RestController
@RequestMapping("/api/{version}/audit")
@PreAuthorize("hasAnyAuthority('APPROLE_Admin', 'APPROLE_Auditor')")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public List<EntradaAuditoriaResponse> buscar(
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long atencionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return auditService.buscar(usuario, type, atencionId, from, to);
    }

    @GetMapping("/appointments/{atencionId}")
    public List<EntradaAuditoriaResponse> timeline(@PathVariable Long atencionId) {
        return auditService.timeline(atencionId);
    }
}
