package de.gib.betrieb.adapter;

import java.util.HashMap;
import java.util.Map;

/**
 * Baut einfache menschenlesbare Narrative für FHIR DomainResources.
 */
public final class erstelleNarratixtext {

    private erstelleNarratixtext() {}

    // Baut das FHIR-Textfeld mit XHTML-Wrapper
    public static Map<String, Object> baueText(String htmlOhneNamespace) {
        Map<String, Object> text = new HashMap<>();
        text.put("status", "generated");
        String div = "<div xmlns=\"http://www.w3.org/1999/xhtml\">" + htmlOhneNamespace + "</div>";
        text.put("div", div);
        return text;
    }

    // Patient
    public static String patientSatz(String vorname, String nachname, String geburt, String geschlecht, String id) {
        String name = ((vorname != null ? vorname + " " : "") + (nachname != null ? nachname : "")).trim();
        String geb = (geburt != null ? geburt : "unbekannt");
        String sex = (geschlecht != null ? geschlecht : "unknown");
        String pid = (id != null ? id : "?");
        return "Patientin oder Patient " + name + ", geboren am " + geb + ", Geschlecht: " + sex + ", ID: " + pid + ".";
    }

    // Practitioner
    public static String practitionerSatz(String titel, String vorname, String nachname, String fach) {
        String name = ((titel != null ? titel + " " : "")
                + (vorname != null ? vorname + " " : "")
                + (nachname != null ? nachname : "")).trim();
        if (fach != null && !fach.isEmpty()) {
            return "Behandelnde Person " + name + ", Fachrichtung: " + fach + ".";
        }
        return "Behandelnde Person " + name + ".";
    }

    // Observation
    public static String observationSatz(String codeText, String valueText, String zeit) {
        String c = codeText != null ? codeText : "Messung";
        String v = valueText != null ? valueText : "kein Wert";
        String t = zeit != null ? zeit : "ohne Zeitangabe";
        return "Befund " + c + " mit Wert " + v + " am " + t + ".";
    }

    // DiagnosticReport
    public static String diagnosticReportSatz(String codeText, String status, String issued, int anzahlErgebnisse) {
        String c = codeText != null ? codeText : "Bericht";
        String s = status != null ? status : "unbekannt";
        String i = issued != null ? issued : "ohne Ausgabedatum";
        return "Bericht " + c + " mit Status " + s + ", ausgegeben am " + i + ". Ergebnisse: " + anzahlErgebnisse + ".";
    }
}
