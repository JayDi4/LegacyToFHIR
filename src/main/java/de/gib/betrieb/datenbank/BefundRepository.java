package de.gib.betrieb.datenbank;

import de.gib.betrieb.model.krankenhaus.Befund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BefundRepository extends JpaRepository<Befund, Long> {

    List<Befund> findByBehandlungsfallFallId(Long fallId);
    List<Befund> findByCode(String code);
}
