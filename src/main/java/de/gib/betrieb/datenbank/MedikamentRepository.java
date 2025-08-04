package de.gib.betrieb.datenbank;

import de.gib.betrieb.model.Medikament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedikamentRepository extends JpaRepository<Medikament, Long> {
}
