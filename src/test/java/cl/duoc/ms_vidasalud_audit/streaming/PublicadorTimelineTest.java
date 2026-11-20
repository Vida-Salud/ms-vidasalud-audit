package cl.duoc.ms_vidasalud_audit.streaming;

import cl.duoc.ms_vidasalud_audit.streaming.config.KafkaConsumidorProperties;
import cl.duoc.ms_vidasalud_audit.streaming.dto.EntradaAuditoria;
import org.apache.kafka.common.KafkaException;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PublicadorTimelineTest {

    @SuppressWarnings("unchecked")
    private final KafkaTemplate<String, Object> template = mock(KafkaTemplate.class);
    private final PublicadorTimeline publicador = new PublicadorTimeline(template, new KafkaConsumidorProperties(
            "appointments.events", "audit.timeline", "vidasalud-audit-timeline", ".audit.DLT",
            Duration.ofMillis(200), new KafkaConsumidorProperties.Reintentos(3, Duration.ofSeconds(1), 2.0,
            Duration.ofSeconds(10))));

    /** Si Kafka no confirma, el error sube: el error handler reintenta y el offset no avanza. */
    @Test
    void sinConfirmacionDeKafkaElErrorSePropaga() {
        CompletableFuture<SendResult<String, Object>> fallido = CompletableFuture.failedFuture(new KafkaException("caído"));
        EntradaAuditoria entrada = entrada();
        when(template.send("audit.timeline", "42", entrada)).thenReturn(fallido);

        assertThatThrownBy(() -> publicador.publicar(entrada)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void unaEsperaVencidaTambienSePropaga() {
        EntradaAuditoria entrada = entrada();
        when(template.send("audit.timeline", "42", entrada)).thenReturn(new CompletableFuture<>());

        assertThatThrownBy(() -> publicador.publicar(entrada)).isInstanceOf(IllegalStateException.class);
    }

    /** La clave del registro es el atencionId (42), no el eventId ("e-1"): por eso los envíos esperan "42". */
    private static EntradaAuditoria entrada() {
        return new EntradaAuditoria(1, "e-1", "APPOINTMENT_STATUS_CHANGED", Instant.parse("2026-11-16T13:00:00Z"), 42L,
                "SOLICITADA", "CONFIRMADA", "oid-op", "Olga", "Operador", "ms-vidasalud-appointments", null, "abc");
    }
}
