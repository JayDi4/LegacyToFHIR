package de.gib.betrieb.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI Konfiguration für die FHIR API Dokumentation
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FHIR Legacy Adapter API")
                        .version("1.0")
                        .description("REST API zum Zugriff auf Legacy-Krankenhaus-Daten " +
                                "im FHIR R4 Format. Dieser Adapter wandelt interne " +
                                "Datenbank-Strukturen in FHIR-konforme JSON-Ressourcen um.")
                        );
    }
}