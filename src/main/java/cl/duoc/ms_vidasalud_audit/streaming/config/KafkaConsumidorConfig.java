package cl.duoc.ms_vidasalud_audit.streaming.config;

import cl.duoc.ms_vidasalud_audit.streaming.exception.EventoInvalidoException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.RetryListener;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

/**
 * Manejo de errores del consumidor. Spring Boot aplica este {@link DefaultErrorHandler} a todos los
 * {@code @KafkaListener} del servicio:
 * <ol>
 *   <li><b>{@link EventoInvalidoException}</b> (JSON roto, campos faltantes, versión desconocida): no se
 *       reintenta, va directo al DLT.</li>
 *   <li><b>Cualquier otro error</b> (por ejemplo, Oracle no disponible): se reintenta con backoff exponencial
 *       ({@code vidasalud.kafka.reintentos}); agotados los reintentos, al DLT.</li>
 *   <li>Después de publicar en el DLT se confirma el offset: el mensaje defectuoso no bloquea la partición
 *       y el consumidor sigue con el siguiente.</li>
 * </ol>
 * El DLT es {@code <tópico de origen><sufijo>} en la misma partición. El recoverer agrega las cabeceras
 * {@code kafka_dlt-exception-message}, {@code kafka_dlt-original-topic}, {@code kafka_dlt-original-offset}, etc.
 */
@Configuration
@EnableConfigurationProperties(KafkaConsumidorProperties.class)
public class KafkaConsumidorConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumidorConfig.class);

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<Object, Object> kafkaTemplate,
                                                 KafkaConsumidorProperties properties) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (registro, error) -> new TopicPartition(registro.topic() + properties.sufijoDlt(), registro.partition()));

        KafkaConsumidorProperties.Reintentos reintentos = properties.reintentos();
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(reintentos.maximo());
        backOff.setInitialInterval(reintentos.intervaloInicial().toMillis());
        backOff.setMultiplier(reintentos.multiplicador());
        backOff.setMaxInterval(reintentos.intervaloMaximo().toMillis());

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);
        handler.addNotRetryableExceptions(EventoInvalidoException.class);
        handler.setRetryListeners(registroDeReintentos(properties));
        return handler;
    }

    private static RetryListener registroDeReintentos(KafkaConsumidorProperties properties) {
        return new RetryListener() {
            @Override
            public void failedDelivery(ConsumerRecord<?, ?> registro, Exception error, int intento) {
                log.warn("Fallo al procesar [topico={}, particion={}, offset={}, clave={}, intento={}, motivo={}]",
                        registro.topic(), registro.partition(), registro.offset(), registro.key(), intento,
                        error == null ? "?" : causaRaiz(error));
            }

            @Override
            public void recovered(ConsumerRecord<?, ?> registro, Exception error) {
                log.error("[-> DLT] Mensaje enviado a {}{} [particion={}, offset={}, clave={}, motivo={}]",
                        registro.topic(), properties.sufijoDlt(), registro.partition(), registro.offset(),
                        registro.key(), error == null ? "?" : causaRaiz(error));
            }

            @Override
            public void recoveryFailed(ConsumerRecord<?, ?> registro, Exception original, Exception falla) {
                log.error("No se pudo publicar en el DLT; el registro se volverá a leer [topico={}, particion={}, offset={}]",
                        registro.topic(), registro.partition(), registro.offset(), falla);
            }
        };
    }

    private static String causaRaiz(Throwable error) {
        Throwable causa = error;
        while (causa.getCause() != null && causa.getCause() != causa) {
            causa = causa.getCause();
        }
        return causa.getClass().getSimpleName() + ": " + causa.getMessage();
    }
}
