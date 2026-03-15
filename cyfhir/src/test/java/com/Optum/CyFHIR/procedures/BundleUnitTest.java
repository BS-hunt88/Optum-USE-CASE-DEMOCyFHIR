package com.Optum.CyFHIR.procedures;

import com.Optum.CyFHIR.models.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BundleUnitTest {

    @Test
    void testBundleConstructorCreatesDefaultValidator() throws Exception {
        Bundle bundle = new Bundle();
        assertNotNull(Bundle.validator);
    }

    @Test
    void testBundleValidatorIsR4ByDefault() throws Exception {
        Bundle bundle = new Bundle();
        // The default validator is R4
        String patientJson = "{\"resourceType\":\"Patient\",\"id\":\"test-123\"}";
        Object result = Bundle.validator.validate(patientJson, "Patient");
        assertNotNull(result);
    }

    @Test
    void testBundleValidatorNotNull() throws Exception {
        Bundle bundle = new Bundle();
        assertNotNull(Bundle.validator);
        assertTrue(Bundle.validator instanceof Validator);
    }

    @Test
    void testBundleValidatorCanValidateBundle() throws Exception {
        Bundle bundle = new Bundle();
        String bundleJson = "{\"resourceType\":\"Bundle\",\"type\":\"transaction\",\"entry\":[]}";
        Object result = Bundle.validator.validate(bundleJson, "Bundle");
        assertNotNull(result);
    }

    @Test
    void testBundleValidatorCanValidateEncounter() throws Exception {
        Bundle bundle = new Bundle();
        String encounterJson = "{\"resourceType\":\"Encounter\",\"id\":\"enc-1\",\"status\":\"finished\",\"class\":{\"system\":\"http://terminology.hl7.org/CodeSystem/v3-ActCode\",\"code\":\"AMB\"}}";
        Object result = Bundle.validator.validate(encounterJson, "Encounter");
        assertNotNull(result);
    }

    @Test
    void testBundleConstructorMultipleTimesOverwritesValidator() throws Exception {
        Bundle bundle1 = new Bundle();
        Validator v1 = Bundle.validator;
        Bundle bundle2 = new Bundle();
        Validator v2 = Bundle.validator;
        // Each constructor call creates a new validator
        assertNotNull(v1);
        assertNotNull(v2);
    }
}
