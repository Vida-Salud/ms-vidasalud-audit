package cl.duoc.ms_vidasalud_audit.streaming.consumer;

import cl.duoc.ms_vidasalud_audit.service.AuditService;
import cl.duoc.ms_vidasalud_audit.service.NuevaEntrada;
import cl.duoc.ms_vidasalud_audit.streaming.LectorJson;
import cl.duoc.ms_vidasalud_audit.streaming.dto.EntradaAuditoria;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Etapa 2, grupo {@code vidasalud-audit-timeline}: lee {@code audit.timeline} y guarda cada entrada en
 * Oracle (tabla AUD_EVENTO), de donde la lee la API {@code /api/v1/audit}. Grupo propio: se puede
 * reprocesar el timeline completo (reset de offsets) sin volver a leer appointments.events.
 */
@Component
public class TimelineConsumer {

    private static final Logger log = LoggerFactory.getLogger(TimelineConsumer.class);

    private final LectorJson lector;
    private final AuditService auditService;

    public TimelineConsumer(LectorJson lector, AuditService auditService) {
        this.lector = lector;
        this.auditService = auditService;
    }

    @KafkaListener(id = "audit-timeline",
            topics = "${vidasalud.kafka.topico-timeline}",
            groupId = "${vidasalud.kafka.grupo-timeline}")
    public void recibir(ConsumerRecord<String, byte[]> registro) {
        try {
            EntradaAuditoria e = lector.leer(registro, EntradaAuditoria.class, EntradaAuditoria::version);
            if (e.traceId() != null) {
                MDC.put("traceId", e.traceId());
            }
            boolean nueva = auditService.registrar(new NuevaEntrada(e.eventId(), e.type(), e.ocurridoEn(),
                    e.atencionId(), e.estadoAnterior(), e.estado(), e.actorId(), e.actorNombre(), e.actorRol(),
                    e.origenServicio(), e.origenIp(), e.traceId()));
            log.info("{} [topico={}, particion={}, offset={}, eventId={}, atencionId={}]",
                    nueva ? "Entrada de auditoría guardada" : "Duplicado ignorado",
                    registro.topic(), registro.partition(), registro.offset(), e.eventId(), e.atencionId());
        } finally {
            MDC.remove("traceId");
        }
    }
}
