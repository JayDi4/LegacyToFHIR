package de.gib.betrieb.adapter;

import de.gib.betrieb.datenbank.BefundRepository;
import de.gib.betrieb.model.Arzt;
import de.gib.betrieb.model.Befund;
import de.gib.betrieb.model.Behandlungsfall;
import de.gib.betrieb.model.Bericht;
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
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BerichtZuFhirAdapterTest {

    @Mock PatientZuFhirAdapter patientAdapter;
    @Mock ArztZuFhirAdapter arztAdapter;
    @Mock BefundRepository befundRepository;

    @InjectMocks BerichtZuFhirAdapter adapter;

    Bericht bericht;
    Behandlungsfall fall;
    Patient patient;
    Arzt arzt;

    private static final DateTimeFormatter FHIR_DATETIME_TZ =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    @BeforeEach
    void init() {
        bericht = mock(Bericht.class);
        fall = mock(Behandlungsfall.class);
        patient = mock(Patient.class);
        arzt = mock(Arzt.class);

        when(bericht.getBerichtId()).thenReturn(200L);
        when(bericht.getCode()).thenReturn("LABORBERICHT");
        when(bericht.getErstelltAm()).thenReturn(LocalDateTime.of(2024, 4, 2, 8, 30));
        when(bericht.getBehandlungsfall()).thenReturn(fall);
        when(fall.getPatient()).thenReturn(patient);
        when(fall.getArzt()).thenReturn(arzt);
        when(fall.getFallId()).thenReturn(77L);

        when(patientAdapter.erstellePatientReference(patient))
                .thenReturn(Map.of("reference", "Patient/1"));
        when(arztAdapter.erstellePractitionerReference(arzt))
                .thenReturn(Map.of("reference", "Practitioner/9"));

        var b1 = mock(Befund.class);
        var b2 = mock(Befund.class);
        when(b1.getBefundId()).thenReturn(10L);
        when(b1.getCode()).thenReturn("LAB-001");
        when(b2.getBefundId()).thenReturn(11L);
        when(b2.getCode()).thenReturn("LAB-002");

        when(befundRepository.findByBehandlungsfallFallId(77L))
                .thenReturn(List.of(b1, b2));
    }

    @Test
    void konvertiereZuFhir_diagnosticReport_mit_verweisen() {
        var out = adapter.konvertiereZuFhir(bericht);

        assertEquals("DiagnosticReport", out.get("resourceType"));
        assertEquals("200", out.get("id"));
        assertEquals("final", out.get("status"));
        assertTrue(out.containsKey("category"));
        assertTrue(out.containsKey("code"));
        assertEquals("Patient/1", ((Map<?, ?>) out.get("subject")).get("reference"));
        assertEquals("Encounter/77", ((Map<?, ?>) out.get("encounter")).get("reference"));

        var erwartetesEffective = bericht.getErstelltAm()
                .atZone(ZoneId.systemDefault())
                .format(FHIR_DATETIME_TZ);
        assertEquals(erwartetesEffective, out.get("effectiveDateTime"));

        var erwartetesIssued = bericht.getErstelltAm()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toString(); // immer UTC mit 'Z'
        assertEquals(erwartetesIssued, out.get("issued"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> result = (List<Map<String, Object>>) out.get("result");
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Observation/10", result.get(0).get("reference"));

        var res = FhirTestValidator.validateMap(out);
        FhirTestValidator.assertValid(res);
    }

    @Test
    void generiereUrl() {
        assertEquals("DiagnosticReport/200", adapter.generiereFhirUrl(200L));
    }

    @Test
    void validierung_r4_prueft_diagnosticReport_ist_gueltig() {
        var out = adapter.konvertiereZuFhir(bericht);
        var res = FhirTestValidator.validateMap(out);
        FhirTestValidator.assertValid(res);
    }
}
