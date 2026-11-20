package cl.duoc.ms_vidasalud_audit.streaming.consumer;

import cl.duoc.ms_vidasalud_audit.streaming.LectorJson;
import cl.duoc.ms_vidasalud_audit.streaming.PublicadorTimeline;
import cl.duoc.ms_vidasalud_audit.streaming.dto.ActorEvento;
import cl.duoc.ms_vidasalud_audit.streaming.dto.DatosEventoAtencion;
import cl.duoc.ms_vidasalud_audit.streaming.dto.EntradaAuditoria;
import cl.duoc.ms_vidasalud_audit.streaming.dto.EventoAtencion;
import cl.duoc.ms_vidasalud_audit.streaming.dto.OrigenEvento;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Etapa 1 del pipeline de auditoría, grupo {@code vidasalud-audit}: lee {@code appointments.events}
 * y publica la entrada aplanada (quién / qué / cuándo / desde dónde) en {@code audit.timeline}.
 * <p>
 * Tiene su propio grupo, distinto del de reportería: los dos servicios reciben TODOS los eventos
 * (publish-subscribe), y dentro de cada grupo las particiones se reparten entre instancias.
 */
@Component
public class EventosAtencionConsumer {

    private final LectorJson lector;
    private final PublicadorTimeline publicador;

    public EventosAtencionConsumer(LectorJson lector, PublicadorTimeline publicador) {
        this.lector = lector;
        this.publicador = publicador;
    }

    @KafkaListener(id = "audit-eventos-atencion",
            topics = "${vidasalud.kafka.topico-eventos}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void recibir(ConsumerRecord<String, byte[]> registro) {
        try {
            EventoAtencion evento = lector.leer(registro, EventoAtencion.class, EventoAtencion::version);
            if (evento.traceId() != null) {
                MDC.put("traceId", evento.traceId());
            }
            publicador.publicar(aEntrada(evento));
        } finally {
            MDC.remove("traceId");
        }
    }

    static EntradaAuditoria aEntrada(EventoAtencion evento) {
        DatosEventoAtencion datos = evento.payload();
        ActorEvento actor = datos.actor() != null ? datos.actor() : new ActorEvento(null, null, null);
        OrigenEvento origen = datos.origen() != null ? datos.origen() : new OrigenEvento(null, null);
        return new EntradaAuditoria(
                EntradaAuditoria.VERSION_ACTUAL,
                evento.eventId(),
                evento.type(),
                evento.timestamp(),
                datos.atencionId(),
                datos.estadoAnterior(),
                datos.estado(),
                actor.id(),
                actor.nombre(),
                actor.rol(),
                origen.servicio(),
                origen.ip(),
                evento.traceId());
    }
}
