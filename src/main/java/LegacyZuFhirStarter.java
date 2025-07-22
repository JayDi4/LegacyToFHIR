package main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LegacyZuFhirStarter {
    public static void main(String[] args) {
        SpringApplication.run(main.LegacyZuFhirStarter.class, args);
        System.out.println("FHIR Legacy Adapter gestartet!");
        System.out.println("Swagger UI: http://localhost:8080/swagger-ui.html");
    }
}
