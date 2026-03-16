package com.Optum.CyFHIR.functions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class BundleUnitTest {

    private Bundle bundle;

    @BeforeEach
    void setUp() {
        bundle = new Bundle();
    }

    @Test
    void testFormatWithEmptyPathsProducesBundleStructure() {
        // format() with empty paths list will cause an IndexOutOfBoundsException
        // because paths list can't be empty for Convert.toTree (returns empty map)
        // But toTree handles empty paths, so let's test with empty list producing
        // an empty map response
        // Actually format() calls toTree which handles empty paths by returning empty map
        // But then mapResponse.get(0).isEmpty() would be checked
        // Let's verify the bundle structure
        Map result = bundle.format(new ArrayList<>());
        assertNotNull(result);
        assertEquals("Bundle", result.get("resourceType"));
        assertEquals("transaction", result.get("type"));
        assertTrue(result.containsKey("entry"));
        List entries = (List) result.get("entry");
        assertTrue(entries.isEmpty());
    }
}
