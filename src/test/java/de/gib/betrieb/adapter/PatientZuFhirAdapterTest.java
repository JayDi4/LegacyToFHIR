package de.gib.betrieb.adapter;

import de.gib.betrieb.model.Patient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PatientZuFhirAdapterTest {

    @Test
    void konvertiereZuFhir() {
        Patient p = Mockito.mock(Patient.class);
        Mockito.when(p.getPatientenId()).thenReturn(42L);
        Mockito.when(p.getVorname()).thenReturn("Lara");
        Mockito.when(p.getNachname()).thenReturn("Schmidt");
        Mockito.when(p.getGeschlecht()).thenReturn("weiblich");
        Mockito.when(p.getGeburtsdatum()).thenReturn(LocalDate.of(1999, 5, 1));

        PatientZuFhirAdapter adapter = new PatientZuFhirAdapter();
        Map<String, Object> out = adapter.konvertiereZuFhir(p);

        assertEquals("Patient", out.get("resourceType"));
        assertEquals("42", out.get("id"));
        assertEquals("female", out.get("gender"));
        assertEquals("1999-05-01", out.get("birthDate"));
        assertTrue(out.containsKey("name"));
        assertEquals(true, out.get("active"));
    }

    @Test
    void url_und_reference() {
        Patient p = Mockito.mock(Patient.class);
        Mockito.when(p.getPatientenId()).thenReturn(5L);
        Mockito.when(p.getVorname()).thenReturn("Tim");
        Mockito.when(p.getNachname()).thenReturn("Bauer");

        PatientZuFhirAdapter adapter = new PatientZuFhirAdapter();
        assertEquals("Patient/5", adapter.generiereFhirUrl(5L));

        Map<String, Object> ref = adapter.erstellePatientReference(p);
        assertEquals("Patient/5", ref.get("reference"));
        assertEquals("Tim Bauer", ref.get("display"));
    }
}
