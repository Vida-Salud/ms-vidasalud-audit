package cl.duoc.ms_vidasalud_audit.controller;

import cl.duoc.ms_vidasalud_audit.exception.DatoInvalidoException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Mismo formato de error (ProblemDetail, RFC 9457) que el resto de la API v1.
 * No hay un {@code @ExceptionHandler(Exception.class)}: atraparía el AccessDeniedException de
 * {@code @PreAuthorize} y un 403 saldría como 500.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(DatoInvalidoException.class)
    public ProblemDetail datoInvalido(DatoInvalidoException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
