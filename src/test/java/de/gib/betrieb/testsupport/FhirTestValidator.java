package de.gib.betrieb.testsupport;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hl7.fhir.common.hapi.validation.support.CommonCodeSystemsTerminologyService;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.NpmPackageValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.SnapshotGeneratingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;

import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

public final class FhirTestValidator {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final FhirContext CTX = FhirContext.forR4Cached();
    private static final FhirValidator VALIDATOR = build();

    private FhirTestValidator() {}

    private static FhirValidator build() {
        // Basis-Supports
        var defaultSupport   = new DefaultProfileValidationSupport(CTX);
        var codeSystems      = new CommonCodeSystemsTerminologyService(CTX);
        var inMemoryTerminology = new InMemoryTerminologyServerValidationSupport(CTX);
        var snapshotSupport  = new SnapshotGeneratingValidationSupport(CTX);

        // NPM-Pakete optional für später falls man noch z.B. isik Profile o.ä. laden würde
        NpmPackageValidationSupport npm = null;
        try {
            npm = new NpmPackageValidationSupport(CTX);

            npm.loadPackageFromClasspath("npm/hl7.fhir.r4.core.tgz");
            // Terminologien
            try {
                npm.loadPackageFromClasspath("npm/hl7.terminology.r4.tgz");
            } catch (Exception ignore) { /* optional */ }
        } catch (Exception ignore) {
            npm = null;
        }

        // Validierungs-Chain zusammenstecken
        ValidationSupportChain chain = (npm != null)
                ? new ValidationSupportChain(npm, defaultSupport, codeSystems, inMemoryTerminology, snapshotSupport)
                : new ValidationSupportChain(defaultSupport, codeSystems, inMemoryTerminology, snapshotSupport);

        var instance = new FhirInstanceValidator(chain);
        instance.setAnyExtensionsAllowed(false);
        instance.setErrorForUnknownProfiles(true);

        var v = CTX.newValidator();
        v.registerValidatorModule(instance);
        return v;
    }

    /** Map -> JSON -> validieren */
    public static ValidationResult validateMap(Map<String, Object> resourceAsMap) {
        try {
            String json = MAPPER.writeValueAsString(resourceAsMap);
            return VALIDATOR.validateWithResult(json);
        } catch (Exception e) {
            throw new IllegalStateException("Konnte Resource nicht serialisieren: " + e.getMessage(), e);
        }
    }

    /** Bequeme Assert-Hilfe mit sauberer Fehlzusammenfassung */
    public static void assertValid(ValidationResult res) {
        assertTrue(res != null && res.isSuccessful(), () ->
                (res == null)
                        ? "Kein ValidationResult erhalten."
                        : res.getMessages().stream()
                        .map(m -> m.getSeverity() + ": " + m.getLocationString() + " - " + m.getMessage())
                        .collect(Collectors.joining("\n"))
        );
    }
}
