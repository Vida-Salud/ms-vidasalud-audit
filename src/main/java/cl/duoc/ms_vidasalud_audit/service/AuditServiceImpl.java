package cl.duoc.ms_vidasalud_audit.service;

import cl.duoc.ms_vidasalud_audit.dto.EntradaAuditoriaResponse;
import cl.duoc.ms_vidasalud_audit.exception.DatoInvalidoException;
import cl.duoc.ms_vidasalud_audit.model.AuditEvento;
import cl.duoc.ms_vidasalud_audit.repository.AuditEventoRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

@Service
public class AuditServiceImpl implements AuditService {

    /** Las consultas devuelven como máximo 500 filas: la auditoría crece sin límite. */
    private static final int MAXIMO_FILAS = 500;
    private static final ZoneId ZONA = ZoneId.of("America/Santiago");
    /** Límites cuando el filtro de fechas no se indica. */
    private static final Instant SIN_DESDE = Instant.EPOCH;
    private static final Instant SIN_HASTA = Instant.parse("9999-12-31T00:00:00Z");

    private final AuditEventoRepository repository;
    private final Clock clock;

    public AuditServiceImpl(AuditEventoRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    @Override
    public boolean registrar(NuevaEntrada e) {
        if (repository.existsById(e.eventId())) {
            return false;
        }
        repository.save(new AuditEvento(e.eventId(), e.tipo(), e.ocurridoEn(), e.atencionId(), e.estadoAnterior(),
                e.estado(), e.actorId(), e.actorNombre(), e.actorRol(), e.origenServicio(), e.origenIp(),
                e.traceId(), Instant.now(clock)));
        return true;
    }

    @Transactional(readOnly = true)
    @Override
    public List<EntradaAuditoriaResponse> buscar(String usuario, String tipo, Long atencionId,
                                                 LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && hasta.isBefore(desde)) {
            throw new DatoInvalidoException("La fecha 'to' no puede ser anterior a 'from'");
        }
        String usuarioBuscado = vacioANull(usuario);
        return repository.buscar(
                        usuarioBuscado,
                        usuarioBuscado == null ? null : usuarioBuscado.toLowerCase(Locale.ROOT),
                        vacioANull(tipo),
                        atencionId,
                        desde == null ? SIN_DESDE : desde.atStartOfDay(ZONA).toInstant(),
                        hasta == null ? SIN_HASTA : hasta.plusDays(1).atStartOfDay(ZONA).toInstant(),   // "to" incluye ese día
                        PageRequest.of(0, MAXIMO_FILAS))
                .stream().map(AuditServiceImpl::aResponse).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<EntradaAuditoriaResponse> timeline(Long atencionId) {
        return repository.findByAtencionIdOrderByOcurridoEnAsc(atencionId)
                .stream().map(AuditServiceImpl::aResponse).toList();
    }

    private static String vacioANull(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private static EntradaAuditoriaResponse aResponse(AuditEvento e) {
        return new EntradaAuditoriaResponse(e.getEventId(), e.getTipo(), e.getOcurridoEn(), e.getAtencionId(),
                e.getEstadoAnterior(), e.getEstado(), e.getActorId(), e.getActorNombre(), e.getActorRol(),
                e.getOrigenServicio(), e.getOrigenIp(), e.getTraceId());
    }
}
