package de.gib.betrieb.datenbank;


import de.gib.betrieb.model.Verordnung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerordnungRepository extends JpaRepository<Verordnung, Long> {
}