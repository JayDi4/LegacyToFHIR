package de.gib.betrieb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication  // Scannt alle Packages unter de.gib.betrieb automatisch
public class LegacyZuFhirStarter {

    public static void main(String[] args) {
        SpringApplication.run(LegacyZuFhirStarter.class, args);
        System.out.println("FHIR Legacy Adapter gestartet auf Port 8090!");
        System.out.println("Test: http://localhost:8090/api/test/hello");
    }
}