package de.gib.betrieb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication  // Scannt alle Packages unter de.gib.betrieb automatisch
public class LegacyZuFhirStarter {

    public static void main(String[] args) {
        SpringApplication.run(LegacyZuFhirStarter.class, args);
        System.out.println("Swagger UI: http://localhost:8090/swagger-ui.html");
        System.out.println("FHIR Legacy Adapter - Vollständige API-Übersicht unter http://localhost:8090/swagger-ui/index.html#/test-controller/getHilfe");
    }
}