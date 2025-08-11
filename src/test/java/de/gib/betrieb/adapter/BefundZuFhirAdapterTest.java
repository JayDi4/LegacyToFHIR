package de.gib.betrieb.adapter;

import de.gib.betrieb.model.Arzt;
import de.gib.betrieb.model.Befund;
import de.gib.betrieb.model.Behandlungsfall;
import de.gib.betrieb.model.Patient;
import de.gib.betrieb.testsupport.FhirTestValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BefundZuFhirAdapterTest {

    @Mock PatientZuFhirAdapter patientAdapter;
    @Mock ArztZuFhirAdapter arztAdapter;

    @InjectMocks BefundZuFhirAdapter adapter;

    Befund befund;
    Behandlungsfall fall;
    Patient patient;
    Arzt arzt;

    private static final DateTimeFormatter FHIR_DATETIME_TZ =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    @BeforeEach
    void setUp() {
        befund = mock(Befund.class);
        fall = mock(Behandlungsfall.class);
        patient = mock(Patient.class);
        arzt = mock(Arzt.class);

        when(befund.getBefundId()).thenReturn(100L);
        when(befund.getCode()).thenReturn("LAB-001");
        when(befund.getZeitpunkt()).thenReturn(LocalDateTime.of(2024, 4, 1, 12, 0));
        when(befund.getWert()).thenReturn("120.5");
        when(befund.getEinheit()).thenReturn("mmHg");

        when(befund.getBehandlungsfall()).thenReturn(fall);
        when(fall.getPatient()).thenReturn(patient);
        when(fall.getArzt()).thenReturn(arzt);
        when(fall.getFallId()).thenReturn(77L);

        when(patientAdapter.erstellePatientReference(patient))
                .thenReturn(Map.of("reference", "Patient/1"));
        when(arztAdapter.erstellePractitionerReference(arzt))
                .thenReturn(Map.of("reference", "Practitioner/9"));
    }

    @Test
    void konvertiereZuFhir_observation_mit_werten() {
        var out = adapter.konvertiereZuFhir(befund);

        assertEquals("Observation", out.get("resourceType"));
        assertEquals("100", out.get("id"));
        assertEquals("final", out.get("status"));
        assertTrue(out.containsKey("category"));
        assertTrue(out.containsKey("code"));
        assertEquals("Patient/1", ((Map<?, ?>) out.get("subject")).get("reference"));
        assertEquals("Encounter/77", ((Map<?, ?>) out.get("encounter")).get("reference"));

        var erwartetesEffective = befund.getZeitpunkt()
                .atZone(ZoneId.systemDefault())
                .format(FHIR_DATETIME_TZ);
        assertEquals(erwartetesEffective, out.get("effectiveDateTime"));

        Map<?, ?> valueQuantity = (Map<?, ?>) out.get("valueQuantity");
        assertNotNull(valueQuantity);
        assertEquals(120.5, (Double) valueQuantity.get("value"), 0.0001);
        assertEquals("mmHg", valueQuantity.get("unit"));

        var res = FhirTestValidator.validateMap(out);
        FhirTestValidator.assertValid(res);
    }

    @Test
    void generiereUrl() {
        assertEquals("Observation/55", adapter.generiereFhirUrl(55L));
    }

    @Test
    void validierung_r4_prueft_observation_ist_gueltig() {
        var out = adapter.konvertiereZuFhir(befund);
        var res = FhirTestValidator.validateMap(out);
        FhirTestValidator.assertValid(res);
    }
}
