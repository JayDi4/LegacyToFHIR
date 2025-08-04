package de.gib.betrieb.datenbank;

import de.gib.betrieb.model.Bericht;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BerichtRepository extends JpaRepository<Bericht, Long> {
}
