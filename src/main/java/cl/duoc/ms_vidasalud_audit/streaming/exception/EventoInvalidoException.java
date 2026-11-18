package cl.duoc.ms_vidasalud_audit.streaming.exception;

/** El mensaje nunca se podrá procesar (JSON roto, campos faltantes, versión desconocida): va directo al DLT, sin reintentos. */
public class EventoInvalidoException extends RuntimeException {

    public EventoInvalidoException(String mensaje) {
        super(mensaje);
    }

    public EventoInvalidoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
