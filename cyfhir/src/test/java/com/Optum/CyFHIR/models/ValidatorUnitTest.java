package com.Optum.CyFHIR.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ValidatorUnitTest {

    @Test
    void testDefaultConstructorDefaultsToR4() throws Exception {
        Validator validator = new Validator();
        assertNotNull(validator);
        assertNotNull(validator.ctx);
        assertNotNull(validator.parser);
        assertNotNull(validator.hapiValidator);
    }

    @Test
    void testConstructorWithR4Uppercase() throws Exception {
        Validator validator = new Validator("R4");
        assertNotNull(validator);
    }

    @Test
    void testConstructorWithR4Lowercase() throws Exception {
        Validator validator = new Validator("r4");
        assertNotNull(validator);
    }

    @Test
    void testConstructorWithDSTU3Lowercase() throws Exception {
        Validator validator = new Validator("dstu3");
        assertNotNull(validator);
    }

    @Test
    void testConstructorWithDSTU3Uppercase() throws Exception {
        Validator validator = new Validator("DSTU3");
        assertNotNull(validator);
    }

    @Test
    void testConstructorWithR5() throws Exception {
        Validator validator = new Validator("R5");
        assertNotNull(validator);
    }

    @Test
    void testConstructorWithR5Lowercase() throws Exception {
        Validator validator = new Validator("r5");
        assertNotNull(validator);
    }

    @Test
    void testConstructorWithInvalidVersionThrowsException() {
        Exception exception = assertThrows(Exception.class, () -> {
            new Validator("invalid_version");
        });
        assertEquals("Unsupported FHIR Version", exception.getMessage());
    }

    @Test
    void testConstructorWithEmptyStringThrowsException() {
        Exception exception = assertThrows(Exception.class, () -> {
            new Validator("");
        });
        assertEquals("Unsupported FHIR Version", exception.getMessage());
    }

    @Test
    void testValidateWithPatientR4() throws Exception {
        Validator validator = new Validator("R4");
        String patientJson = "{\"resourceType\":\"Patient\",\"id\":\"test-123\"}";
        Object result = validator.validate(patientJson, "Patient");
        assertNotNull(result);
    }

    @Test
    void testValidateWithBundleR4() throws Exception {
        Validator validator = new Validator("R4");
        String bundleJson = "{\"resourceType\":\"Bundle\",\"type\":\"transaction\",\"entry\":[]}";
        Object result = validator.validate(bundleJson, "Bundle");
        assertNotNull(result);
    }

    @Test
    void testValidateWithEncounterR4() throws Exception {
        Validator validator = new Validator("R4");
        String encounterJson = "{\"resourceType\":\"Encounter\",\"id\":\"enc-1\",\"status\":\"finished\",\"class\":{\"system\":\"http://terminology.hl7.org/CodeSystem/v3-ActCode\",\"code\":\"AMB\"}}";
        Object result = validator.validate(encounterJson, "Encounter");
        assertNotNull(result);
    }

    @Test
    void testValidateWithConditionR4() throws Exception {
        Validator validator = new Validator("R4");
        String conditionJson = "{\"resourceType\":\"Condition\",\"id\":\"cond-1\",\"subject\":{\"reference\":\"Patient/test-123\"}}";
        Object result = validator.validate(conditionJson, "Condition");
        assertNotNull(result);
    }

    @Test
    void testValidateWithPatientDSTU3() throws Exception {
        Validator validator = new Validator("DSTU3");
        String patientJson = "{\"resourceType\":\"Patient\",\"id\":\"test-123\"}";
        Object result = validator.validate(patientJson, "Patient");
        assertNotNull(result);
    }

    @Test
    void testValidateWithPatientR5() throws Exception {
        Validator validator = new Validator("R5");
        String patientJson = "{\"resourceType\":\"Patient\",\"id\":\"test-123\"}";
        Object result = validator.validate(patientJson, "Patient");
        assertNotNull(result);
    }

    @Test
    void testValidateWithUnknownResourceTypeThrowsException() throws Exception {
        Validator validator = new Validator("R4");
        String json = "{\"resourceType\":\"UnknownType\",\"id\":\"test\"}";
        assertThrows(IllegalStateException.class, () -> validator.validate(json, "UnknownType"));
    }
}
