package de.gib.betrieb.datenbank;

import de.gib.betrieb.model.Standort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StandortRepository extends JpaRepository<Standort, Long> {
}
