package datenbank;

import model.krankenhaus.Behandlungsfall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BehandlungsfallRepository extends JpaRepository<Behandlungsfall, Long> {

    List<Behandlungsfall> findByPatientPatientenId(Long patientenId);
    List<Behandlungsfall> findByArt(String art);

    // Custom Query für komplexe Abfragen
    @Query("SELECT b FROM Behandlungsfall b WHERE b.patient.patientenId = ?1 AND b.art = ?2")
    List<Behandlungsfall> findeBehandlungsfaelleNachPatientUndArt(Long patientenId, String art);
}
