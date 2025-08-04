package de.gib.betrieb.datenbank;

import de.gib.betrieb.model.Arzt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ArztRepository extends JpaRepository<Arzt, Long> {

    List<Arzt> findByFachrichtung(String fachrichtung);
    List<Arzt> findByNachname(String nachname);
}
