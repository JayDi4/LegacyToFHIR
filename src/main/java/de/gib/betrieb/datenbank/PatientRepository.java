package de.gib.betrieb.datenbank;

import de.gib.betrieb.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    List<Patient> findByNachname(String nachname);
    List<Patient> findByGeschlecht(String geschlecht);

    List<Patient> findByVornameAndNachname(String vorname, String nachname);

    // Query für Performance-Tests
    @Query("SELECT p FROM Patient p WHERE p.nachname LIKE %?1%")
    List<Patient> findePatientenMitNachnameEnthaelt(String namensteil);

    //Letzte 10 Patienten nach ID sortiert
    @Query("SELECT p FROM Patient p ORDER BY p.patientenId DESC LIMIT 10")
    List<Patient> findLetzteZehnPatienten();
}