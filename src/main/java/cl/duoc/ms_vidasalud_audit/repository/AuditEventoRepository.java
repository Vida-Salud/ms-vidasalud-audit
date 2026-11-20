package cl.duoc.ms_vidasalud_audit.repository;

import cl.duoc.ms_vidasalud_audit.model.AuditEvento;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface AuditEventoRepository extends JpaRepository<AuditEvento, String> {

    /**
     * usuario coincide con el oid; nombre (en minúsculas) con parte del nombre del actor. Se usa locate y no
     * LIKE: Hibernate agrega "escape ''" al LIKE con parámetro, y H2 en modo Oracle (tests) no lo interpreta igual.
     * desde y hasta nunca son null (el servicio usa límites amplios): un Instant null no siempre se compara bien.
     */
    @Query("""
            select e from AuditEvento e
            where (:usuario is null or e.actorId = :usuario or locate(:nombre, lower(e.actorNombre)) > 0)
              and (:tipo is null or e.tipo = :tipo)
              and (:atencionId is null or e.atencionId = :atencionId)
              and e.ocurridoEn >= :desde
              and e.ocurridoEn < :hasta
            order by e.ocurridoEn desc
            """)
    List<AuditEvento> buscar(@Param("usuario") String usuario,
                             @Param("nombre") String nombre,
                             @Param("tipo") String tipo,
                             @Param("atencionId") Long atencionId,
                             @Param("desde") Instant desde,
                             @Param("hasta") Instant hasta,
                             Pageable limite);

    List<AuditEvento> findByAtencionIdOrderByOcurridoEnAsc(Long atencionId);
}
