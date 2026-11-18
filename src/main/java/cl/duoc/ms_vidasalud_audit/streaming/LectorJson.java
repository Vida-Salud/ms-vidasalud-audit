package cl.duoc.ms_vidasalud_audit.streaming;

import cl.duoc.ms_vidasalud_audit.streaming.exception.EventoInvalidoException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.Comparator;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Convierte el valor crudo de un registro en el DTO del tópico y lo valida (Bean Validation y versión).
 * Todo problema es permanente y se informa con {@link EventoInvalidoException}: va directo al DLT.
 */
@Component
public class LectorJson {

    private static final int VERSION_SOPORTADA = 1;

    private final JsonMapper jsonMapper;
    private final Validator validator;

    public LectorJson(JsonMapper jsonMapper, Validator validator) {
        this.jsonMapper = jsonMapper;
        this.validator = validator;
    }

    public <T> T leer(ConsumerRecord<String, byte[]> registro, Class<T> tipo, Function<T, Integer> version) {
        if (registro.value() == null || registro.value().length == 0) {
            throw new EventoInvalidoException("Mensaje vacío");
        }
        T valor;
        try {
            valor = jsonMapper.readValue(registro.value(), tipo);
        } catch (JacksonException e) {
            throw new EventoInvalidoException("JSON inválido: " + e.getOriginalMessage(), e);
        }
        Integer v = version.apply(valor);
        if (v != null && v != VERSION_SOPORTADA) {
            throw new EventoInvalidoException("Versión no soportada: " + v);
        }
        Set<ConstraintViolation<T>> errores = validator.validate(valor);
        if (!errores.isEmpty()) {
            throw new EventoInvalidoException("Mensaje incompleto: " + errores.stream()
                    .sorted(Comparator.comparing(e -> e.getPropertyPath().toString()))
                    .map(e -> e.getPropertyPath() + " " + e.getMessage())
                    .collect(Collectors.joining(", ")));
        }
        return valor;
    }
}
