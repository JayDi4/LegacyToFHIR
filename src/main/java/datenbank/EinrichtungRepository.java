package datenbank;

import model.krankenhaus.Einrichtung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EinrichtungRepository extends JpaRepository<Einrichtung, Long> {
}