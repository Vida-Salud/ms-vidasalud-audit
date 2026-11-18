package cl.duoc.ms_vidasalud_audit.streaming.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Propiedades {@code vidasalud.kafka} de application.yaml.
 *
 * @param topicoEventos  tópico de eventos de la atención (entrada)
 * @param topicoTimeline tópico del timeline de auditoría (salida y entrada)
 * @param grupoTimeline  grupo que persiste el timeline en Oracle
 * @param sufijoDlt      se agrega al tópico de origen para formar el DLT de este servicio
 * @param esperaEnvio    cuánto se espera la confirmación de Kafka al publicar en el timeline
 * @param reintentos     política de reintentos antes de enviar al DLT
 */
@ConfigurationProperties("vidasalud.kafka")
public record KafkaConsumidorProperties(String topicoEventos, String topicoTimeline, String grupoTimeline,
                                        String sufijoDlt, Duration esperaEnvio, Reintentos reintentos) {

    public record Reintentos(int maximo, Duration intervaloInicial, double multiplicador, Duration intervaloMaximo) {
    }
}
