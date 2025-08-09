package de.gib.betrieb.adapter;

import de.gib.betrieb.model.Arzt;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ArztZuFhirAdapterTest {

    @Test
    void konvertiereZuFhir() {
        Arzt arzt = Mockito.mock(Arzt.class);
        Mockito.when(arzt.getArztId()).thenReturn(123L);
        Mockito.when(arzt.getVorname()).thenReturn("Emma");
        Mockito.when(arzt.getNachname()).thenReturn("Meyer");
        Mockito.when(arzt.getFachrichtung()).thenReturn("Kardiologie");

        ArztZuFhirAdapter adapter = new ArztZuFhirAdapter();
        Map<String, Object> out = adapter.konvertiereZuFhir(arzt);

        assertEquals("Practitioner", out.get("resourceType"));
        assertEquals("123", out.get("id"));
        assertTrue(out.containsKey("name"));
        assertTrue(out.containsKey("qualification"));
        assertEquals(true, out.get("active"));
    }

    @Test
    void generiereUrl_und_Reference() {
        Arzt arzt = Mockito.mock(Arzt.class);
        Mockito.when(arzt.getArztId()).thenReturn(7L);
        Mockito.when(arzt.getVorname()).thenReturn("Max");
        Mockito.when(arzt.getNachname()).thenReturn("Mustermann");
        Mockito.when(arzt.getFachrichtung()).thenReturn("Innere Medizin");

        ArztZuFhirAdapter adapter = new ArztZuFhirAdapter();
        assertEquals("Practitioner/7", adapter.generiereFhirUrl(7L));

        Map<String, Object> ref = adapter.erstellePractitionerReference(arzt);
        assertEquals("Practitioner/7", ref.get("reference"));
        assertTrue(((String) ref.get("display")).contains("Max"));
    }
}
