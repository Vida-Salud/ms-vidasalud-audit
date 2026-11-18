package cl.duoc.ms_vidasalud_audit.service;

import cl.duoc.ms_vidasalud_audit.exception.DatoInvalidoException;
import cl.duoc.ms_vidasalud_audit.repository.AuditEventoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class AuditServiceTest {

    @Autowired AuditEventoRepository repository;
    @Autowired AuditService service;

    @BeforeEach
    void datos() {
        repository.deleteAll();
        service.registrar(entrada("e-1", 1L, "APPOINTMENT_CREATED", "oid-ana", "Ana Paciente", "2026-11-16T12:00:00Z"));
        service.registrar(entrada("e-2", 1L, "APPOINTMENT_STATUS_CHANGED", "oid-op", "Olga Operadora", "2026-11-16T12:30:00Z"));
        service.registrar(entrada("e-3", 2L, "APPOINTMENT_STATUS_CHANGED", "oid-op", "Olga Operadora", "2026-11-17T09:00:00Z"));
    }

    @Test
    void unaEntradaRepetidaNoSeDuplica() {
        assertThat(service.registrar(entrada("e-1", 1L, "APPOINTMENT_CREATED", "oid-ana", "Ana", "2026-11-16T12:00:00Z")))
                .isFalse();
        assertThat(repository.count()).isEqualTo(3);
    }

    @Test
    void filtraPorUsuarioTipoYFechas() {
        assertThat(service.buscar("olga", null, null, null, null)).hasSize(2);
        assertThat(service.buscar("oid-ana", null, null, null, null)).hasSize(1);
        assertThat(service.buscar(null, "APPOINTMENT_STATUS_CHANGED", null, null, null)).hasSize(2);
        assertThat(service.buscar(null, null, null, LocalDate.of(2026, 11, 17), LocalDate.of(2026, 11, 17)))
                .extracting("eventId").containsExactly("e-3");
    }

    @Test
    void elTimelineDeUnaAtencionVaEnOrdenCronologico() {
        assertThat(service.timeline(1L)).extracting("eventId").containsExactly("e-1", "e-2");
    }

    @Test
    void fechasInvertidasSeRechazan() {
        assertThatThrownBy(() -> service.buscar(null, null, null, LocalDate.of(2026, 11, 17), LocalDate.of(2026, 11, 16)))
                .isInstanceOf(DatoInvalidoException.class);
    }

    private static NuevaEntrada entrada(String eventId, Long atencionId, String tipo, String actorId,
                                        String actorNombre, String cuando) {
        return new NuevaEntrada(eventId, tipo, Instant.parse(cuando), atencionId, null, "CONFIRMADA",
                actorId, actorNombre, "Operador", "ms-vidasalud-appointments", "190.20.30.40", "trace-" + eventId);
    }
}
