package com.Optum.CyFHIR;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CyFhirTest {

    @Test
    void testMainDoesNotThrow() {
        assertDoesNotThrow(() -> CyFhir.main(new String[]{}));
    }

    @Test
    void testMainWithNullArgs() {
        assertDoesNotThrow(() -> CyFhir.main(null));
    }

    @Test
    void testMainWithArgs() {
        assertDoesNotThrow(() -> CyFhir.main(new String[]{"arg1", "arg2"}));
    }
}
