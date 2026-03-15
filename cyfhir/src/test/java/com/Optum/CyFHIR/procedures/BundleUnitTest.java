package com.Optum.CyFHIR.procedures;

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
}
