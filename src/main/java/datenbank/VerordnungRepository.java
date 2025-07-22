package datenbank;


import model.krankenhaus.Verordnung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerordnungRepository extends JpaRepository<Verordnung, Long> {
}