# LegacyToFHIR

Dieses Projekt wurde im Rahmen der Bachelorarbeit **"LegacyToFHIR: Konzeption und Evaluation einer Adapter-Fassade zur Transformation relationaler Labordaten in FHIR R4"** entwickelt. Ziel ist es, Daten aus einem bestehenden Krankenhaus-Legacy-System als FHIR-R4-Ressourcen über eine REST-Schnittstelle bereitzustellen.

## Voraussetzungen

- Java 17 und Maven
- Laufende PostgreSQL-Datenbank (Standardverbindung `jdbc:postgresql://localhost:5432/postgres`, Benutzer `fhiruser`, Passwort `1234`)
- Optional: Zugriff auf die automatisch eingebundene Swagger/OpenAPI-Dokumentation

## Installation & Start

1. Repository klonen und in das Projektverzeichnis wechseln
2. Datenbank anlegen bzw. Zugangsdaten in `src/main/resources/application.properties` anpassen
3. Anwendung starten
   ```bash
   ./mvnw spring-boot:run
   ```
4. Die API ist anschließend unter `http://localhost:8090` erreichbar (konfigurierter Port)

## Architektur

Die Anwendung ist als Spring-Boot-Projekt aufgebaut und gliedert sich in mehrere Pakete:

- **model** – JPA-Entitäten (z. B. `Patient`, `Arzt`) mit Beziehungen zu Behandlungsfällen, Verordnungen und Terminen
- **datenbank** – Spring-Data-Repository-Schicht für den Datenbankzugriff
- **adapter** – Konverter, die Legacy-Objekte in FHIR-R4-Ressourcen übersetzen (z. B. `PatientZuFhirAdapter`, `BefundZuFhirAdapter`)
- **service** – Geschäftslogik; insbesondere `FhirAdapterService` als zentrale Koordinationsschicht der Transformation
- **controller** – REST-Controller für FHIR- und Test-Endpunkte (`FhirController`, `TestController`)
- **config** – Swagger/OpenAPI-Konfiguration für die API-Dokumentation

### Daten und Mapping

Beispieldaten können über die Test-API generiert werden:

- `POST /api/test/generiere/{anzahl}` erzeugt Datensätze in der PostgreSQL-Datenbank.

Das Mapping von Legacy-Entitäten zu FHIR-Ressourcen erfolgt in den Adapter-Klassen des Pakets `adapter`.

### Validierung und Tests

Die Unit-Tests unter `src/test/java` nutzen den **HAPI FHIR Instance Validator**, um die erzeugten FHIR-Ressourcen zu überprüfen. Die Klasse `FhirTestValidator` kapselt den Validator und wird in den Adapter-Tests verwendet.

Tests können mit folgendem Befehl ausgeführt werden:

```bash
./mvnw test
```


## Wichtige Endpunkte

### FHIR-API (`/fhir`)

- `GET /fhir/` – Übersicht aller verfügbaren Endpunkte
- `GET /fhir/Patient` – Alle Patienten als FHIR-Patient-Ressourcen
- `GET /fhir/Patient/{id}` – Einzelner Patient
- `GET /fhir/Observation/{id}` – Befunde
- `GET /fhir/Patient/{id}/Observation` – Befunde eines Patienten
- `GET /fhir/DiagnosticReport/{id}` – Berichte
- `GET /fhir/Patient/{id}/DiagnosticReport` – Berichte eines Patienten
- `GET /fhir/Patient/{id}/Bundle` – Komplettes Bundle zu einem Patienten
- `GET /fhir/stats` – Adapterstatistiken
- `GET /fhir/metadata` – vereinfachtes CapabilityStatement

### Test-API (`/api/test`)

- `GET /api/test/help` – Übersicht aller Test-Endpunkte
- `POST /api/test/generiere/{anzahl}` – erzeugt Beispieldaten
- `GET /api/test/letzte10Patienten` – zeigt die letzten zehn Patienten
- `GET /api/test/fhir-test` – führt einen FHIR-Konvertierungstest aus
- `GET /api/test/vergleiche/{id}` – vergleicht Legacy- und FHIR-Darstellung eines Patienten
- `GET /api/test/performance-test/{anzahl}` – Performance-Messung der Konvertierung

## Dokumentation

- Swagger UI: `http://localhost:8090/swagger-ui.html`
- OpenAPI-Definition: `http://localhost:8090/v3/api-docs`

Mit diesen Informationen lässt sich das Projekt schnell in Betrieb nehmen und zur Bereitstellung FHIR-konformer Daten aus einem Legacy-System nutzen.



### _Diese README.md wurde mithilfe von ChatGPT erstellt um dem Nutzer einen Überblick über das gesamte Projekt zu geben_
