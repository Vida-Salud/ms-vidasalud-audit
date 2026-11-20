package cl.duoc.ms_vidasalud_audit.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

/** Una fila por entrada de {@code audit.timeline}. Inmutable: la auditoría no se edita. */
@Entity
@Table(name = "AUD_EVENTO", indexes = {
        @Index(name = "IX_AUD_ATENCION", columnList = "ATENCION_ID"),
        @Index(name = "IX_AUD_OCURRIDO", columnList = "OCURRIDO_EN")})
public class AuditEvento {

    @Id
    @Column(name = "EVENT_ID", length = 64)
    private String eventId;

    @Column(name = "TIPO", nullable = false, length = 60)
    private String tipo;

    @Column(name = "OCURRIDO_EN", nullable = false)
    private Instant ocurridoEn;

    @Column(name = "ATENCION_ID", nullable = false)
    private Long atencionId;

    @Column(name = "ESTADO_ANTERIOR", length = 20)
    private String estadoAnterior;

    @Column(name = "ESTADO", nullable = false, length = 20)
    private String estado;

    @Column(name = "ACTOR_ID", length = 64)
    private String actorId;

    @Column(name = "ACTOR_NOMBRE", length = 150)
    private String actorNombre;

    @Column(name = "ACTOR_ROL", length = 30)
    private String actorRol;

    @Column(name = "ORIGEN_SERVICIO", length = 60)
    private String origenServicio;

    @Column(name = "ORIGEN_IP", length = 45)
    private String origenIp;

    @Column(name = "TRACE_ID", length = 64)
    private String traceId;

    @Column(name = "REGISTRADO_EN", nullable = false)
    private Instant registradoEn;

    protected AuditEvento() {
    }

    public AuditEvento(String eventId, String tipo, Instant ocurridoEn, Long atencionId, String estadoAnterior,
                       String estado, String actorId, String actorNombre, String actorRol, String origenServicio,
                       String origenIp, String traceId, Instant registradoEn) {
        this.eventId = eventId;
        this.tipo = tipo;
        this.ocurridoEn = ocurridoEn;
        this.atencionId = atencionId;
        this.estadoAnterior = estadoAnterior;
        this.estado = estado;
        this.actorId = actorId;
        this.actorNombre = actorNombre;
        this.actorRol = actorRol;
        this.origenServicio = origenServicio;
        this.origenIp = origenIp;
        this.traceId = traceId;
        this.registradoEn = registradoEn;
    }

    public String getEventId() { return eventId; }
    public String getTipo() { return tipo; }
    public Instant getOcurridoEn() { return ocurridoEn; }
    public Long getAtencionId() { return atencionId; }
    public String getEstadoAnterior() { return estadoAnterior; }
    public String getEstado() { return estado; }
    public String getActorId() { return actorId; }
    public String getActorNombre() { return actorNombre; }
    public String getActorRol() { return actorRol; }
    public String getOrigenServicio() { return origenServicio; }
    public String getOrigenIp() { return origenIp; }
    public String getTraceId() { return traceId; }
    public Instant getRegistradoEn() { return registradoEn; }
}
