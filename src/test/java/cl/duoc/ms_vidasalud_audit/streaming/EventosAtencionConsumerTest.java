package cl.duoc.ms_vidasalud_audit.streaming;

import cl.duoc.ms_vidasalud_audit.streaming.consumer.EventosAtencionConsumer;
import cl.duoc.ms_vidasalud_audit.streaming.dto.EntradaAuditoria;
import cl.duoc.ms_vidasalud_audit.streaming.exception.EventoInvalidoException;
import jakarta.validation.Validation;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class EventosAtencionConsumerTest {

    private final PublicadorTimeline publicador = mock(PublicadorTimeline.class);
    private final EventosAtencionConsumer consumer = new EventosAtencionConsumer(
            new LectorJson(JsonMapper.builder().build(), Validation.buildDefaultValidatorFactory().getValidator()),
            publicador);

    @Test
    void publicaQuienQueCuandoYDesdeDonde() {
        consumer.recibir(registro("""
                {"version":1,"eventId":"e-1","type":"APPOINTMENT_STATUS_CHANGED","timestamp":"2026-11-16T13:00:00Z",
                 "traceId":"abc",
                 "payload":{"atencionId":42,"estadoAnterior":"SOLICITADA","estado":"CONFIRMADA",
                            "actor":{"id":"oid-op","nombre":"Olga","rol":"Operador"},
                            "origen":{"servicio":"ms-vidasalud-appointments","ip":"190.20.30.40"}}}
                """));

        ArgumentCaptor<EntradaAuditoria> captor = ArgumentCaptor.forClass(EntradaAuditoria.class);
        verify(publicador).publicar(captor.capture());
        assertThat(captor.getValue()).isEqualTo(new EntradaAuditoria(1, "e-1", "APPOINTMENT_STATUS_CHANGED",
                Instant.parse("2026-11-16T13:00:00Z"), 42L, "SOLICITADA", "CONFIRMADA", "oid-op", "Olga",
                "Operador", "ms-vidasalud-appointments", "190.20.30.40", "abc"));
    }

    @Test
    void unEventoSinActorNiOrigenIgualSeAudita() {
        consumer.recibir(registro("""
                {"eventId":"e-2","type":"APPOINTMENT_STATUS_CHANGED","timestamp":"2026-11-16T13:00:00Z",
                 "payload":{"atencionId":42,"estado":"CERRADA","paciente":"campo antiguo, se ignora"}}
                """));

        ArgumentCaptor<EntradaAuditoria> captor = ArgumentCaptor.forClass(EntradaAuditoria.class);
        verify(publicador).publicar(captor.capture());
        assertThat(captor.getValue().actorId()).isNull();
        assertThat(captor.getValue().origenIp()).isNull();
    }

    @Test
    void unPayloadInvalidoNoSePublica() {
        assertThatThrownBy(() -> consumer.recibir(registro("{\"eventId\":\"e-3\"}")))
                .isInstanceOf(EventoInvalidoException.class)
                .hasMessageContaining("payload");
        verifyNoInteractions(publicador);
    }

    private static ConsumerRecord<String, byte[]> registro(String json) {
        return new ConsumerRecord<>("appointments.events", 0, 0L, "42", json.getBytes(StandardCharsets.UTF_8));
    }
}
