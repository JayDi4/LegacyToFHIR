# LegacyToFHIR

Der LegacyToFHIR‑Adapter stellt Krankenhausdaten aus einem bestehenden Legacy-System als FHIR‑R4‑Ressourcen über eine REST‑Schnittstelle bereit. Dabei werden Daten aus einer PostgreSQL‑Datenbank ausgelesen, in FHIR‑konforme JSON-Strukturen umgewandelt und über standardisierte Endpunkte ausgegeben.

## Voraussetzungen

* Java 17 und Maven
* Laufende PostgreSQL‑Datenbank (Standardverbindung `jdbc:postgresql://localhost:5432/postgres`, Benutzer `fhiruser`, Passwort `1234`)
* Optional: Zugriff auf die automatisch eingebundene Swagger/OpenAPI‑Doku

## Installation & Start

1. Repository klonen und in das Projektverzeichnis wechseln
2. Datenbank anlegen bzw. Zugangsdaten in `src/main/resources/application.properties` anpassen
3. Anwendung starten
   ```bash
   ./mvnw spring-boot:run
   ```
4. Die API ist anschließend unter `http://localhost:8090` erreichbar (konfigurierter Port)

## Wichtige Endpunkte

### FHIR‑API (`/fhir`)
* `GET /fhir/` – Übersicht aller verfügbaren Endpunkte
* `GET /fhir/Patient` – Alle Patienten als FHIR‑Patient‑Ressourcen
* `GET /fhir/Patient/{id}` – Einzelner Patient
* `GET /fhir/Practitioner` / `GET /fhir/Practitioner/{id}` – Ärzte
* `GET /fhir/Observation/{id}` – Befunde
* `GET /fhir/Patient/{id}/Observation` – Befunde eines Patienten
* `GET /fhir/DiagnosticReport/{id}` – Berichte
* `GET /fhir/Patient/{id}/DiagnosticReport` – Berichte eines Patienten
* `GET /fhir/Patient/{id}/Bundle` – Komplettes Bundle zu einem Patienten
* `GET /fhir/stats` – Adapterstatistiken
* `GET /fhir/metadata` – vereinfachtes CapabilityStatement

### Test‑API (`/api/test`)
* `POST /api/test/generiere/{anzahl}` – erzeugt Beispiel­daten in der Datenbank
* `GET /api/test/letzte10Patienten` – zeigt die letzten zehn Patienten
* `GET /api/test/fhir-test` – führt einen FHIR‑Konvertierungstest aus
* `GET /api/test/vergleiche/{id}` – vergleicht Legacy‑ und FHIR‑Darstellung eines Patienten
* `GET /api/test/performance-test/{anzahl}` – Performance‑Messung der Konvertierung

Eine vollständige Übersicht liefert `GET /api/test/help`.

## Klassenaufbau

* **model** – JPA‑Entitäten wie `Patient` mit Beziehungen zu Behandlungsfällen, Verordnungen und Terminen
* **datenbank** – Spring‑Data‑Repositories zum Zugriff auf die Entitäten
* **adapter** – Konverter, die Legacy‑Objekte in FHIR‑Ressourcen übersetzen (z. B. `PatientZuFhirAdapter`, `BefundZuFhirAdapter`)
* **service** – Geschäftslogik, insbesondere `FhirAdapterService` als zentrale Koordinationsschicht der Konvertierung
* **controller** – REST‑Controller für FHIR‑ und Test‑Endpunkte (`FhirController`, `TestController`)
* **config** – Swagger/OpenAPI‑Konfiguration für die API‑Dokumentation

## Dokumentation

* Swagger UI: `http://localhost:8090/swagger-ui.html`
* OpenAPI‑Definition: `http://localhost:8090/v3/api-docs`

Mit diesen Informationen lässt sich das Projekt schnell in Betrieb nehmen und zur Bereitstellung FHIR‑konformer Daten aus einem Legacy‑System nutzen.