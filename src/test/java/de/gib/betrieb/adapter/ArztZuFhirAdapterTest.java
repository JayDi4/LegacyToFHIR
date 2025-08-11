package de.gib.betrieb.adapter;

import de.gib.betrieb.model.Arzt;
import de.gib.betrieb.testsupport.FhirTestValidator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ArztZuFhirAdapterTest {

    @Test
    void konvertiereZuFhir_basisfelder_korrekt() {
        var arzt = Mockito.mock(Arzt.class);
        Mockito.when(arzt.getArztId()).thenReturn(123L);
        Mockito.when(arzt.getVorname()).thenReturn("Emma");
        Mockito.when(arzt.getNachname()).thenReturn("Meyer");
        Mockito.when(arzt.getFachrichtung()).thenReturn("Kardiologie");

        var adapter = new ArztZuFhirAdapter();
        var out = adapter.konvertiereZuFhir(arzt);

        assertEquals("Practitioner", out.get("resourceType"));
        assertEquals("123", out.get("id"));
        assertTrue(out.containsKey("name"), "Name fehlt");
        assertTrue(out.containsKey("qualification"), "Qualification fehlt");
        assertEquals(true, out.get("active"));

        var res = FhirTestValidator.validateMap(out);
        FhirTestValidator.assertValid(res);
    }

    @Test
    void generiereUrl_und_Reference() {
        var arzt = Mockito.mock(Arzt.class);
        Mockito.when(arzt.getArztId()).thenReturn(7L);
        Mockito.when(arzt.getVorname()).thenReturn("Max");
        Mockito.when(arzt.getNachname()).thenReturn("Mustermann");
        Mockito.when(arzt.getFachrichtung()).thenReturn("Innere Medizin");

        var adapter = new ArztZuFhirAdapter();

        assertEquals("Practitioner/7", adapter.generiereFhirUrl(7L));

        Map<String, Object> ref = adapter.erstellePractitionerReference(arzt);
        assertEquals("Practitioner/7", ref.get("reference"));
        assertTrue(((String) ref.get("display")).contains("Max"));
    }

    @Test
    void validierung_r4_practitioner_ist_gueltig() {
        var arzt = Mockito.mock(Arzt.class);
        Mockito.when(arzt.getArztId()).thenReturn(321L);
        Mockito.when(arzt.getVorname()).thenReturn("Lena");
        Mockito.when(arzt.getNachname()).thenReturn("Kern");
        Mockito.when(arzt.getFachrichtung()).thenReturn("Radiologie");

        var adapter = new ArztZuFhirAdapter();
        var out = adapter.konvertiereZuFhir(arzt);

        var res = FhirTestValidator.validateMap(out);
        FhirTestValidator.assertValid(res);
    }
}
