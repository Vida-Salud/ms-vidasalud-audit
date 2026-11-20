package cl.duoc.ms_vidasalud_audit.streaming;

import cl.duoc.ms_vidasalud_audit.streaming.config.KafkaConsumidorProperties;
import cl.duoc.ms_vidasalud_audit.streaming.dto.EntradaAuditoria;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Publica cada {@link EntradaAuditoria} en {@code audit.timeline} con clave = atencionId, por dos razones:
 * <ul>
 *   <li>todas las entradas de una atención caen en la misma partición y se leen en orden;</li>
 *   <li>la compactación del tópico ({@code compact,delete}) conserva el último estado conocido de cada atención.</li>
 * </ul>
 * El historial completo e inmutable queda en Oracle (tabla {@code AUD_EVENTO}, una fila por eventId), no en Kafka.
 * <p>
 * A diferencia de appointments, aquí el envío es <b>síncrono</b>: se ejecuta dentro de un consumidor y,
 * si Kafka no confirma, la excepción sube al error handler, que reintenta y no avanza el offset.
 * Así una entrada de auditoría nunca se pierde en silencio.
 */
@Component
public class PublicadorTimeline {

    private static final Logger log = LoggerFactory.getLogger(PublicadorTimeline.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaConsumidorProperties properties;

    public PublicadorTimeline(KafkaTemplate<String, Object> kafkaTemplate, KafkaConsumidorProperties properties) {
        this.kafkaTemplate = kafkaTemplate;
        this.properties = properties;
    }

    public void publicar(EntradaAuditoria entrada) {
        String clave = String.valueOf(entrada.atencionId());
        try {
            RecordMetadata destino = kafkaTemplate.send(properties.topicoTimeline(), clave, entrada)
                    .get(properties.esperaEnvio().toMillis(), TimeUnit.MILLISECONDS)
                    .getRecordMetadata();
            log.info("Entrada de auditoría publicada [topico={}, particion={}, offset={}, eventId={}, atencionId={}]",
                    destino.topic(), destino.partition(), destino.offset(), entrada.eventId(), entrada.atencionId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Publicación interrumpida en " + properties.topicoTimeline(), e);
        } catch (ExecutionException | TimeoutException e) {
            throw new IllegalStateException("Kafka no confirmó la entrada " + entrada.eventId()
                    + " en " + properties.topicoTimeline(), e);
        }
    }
}
