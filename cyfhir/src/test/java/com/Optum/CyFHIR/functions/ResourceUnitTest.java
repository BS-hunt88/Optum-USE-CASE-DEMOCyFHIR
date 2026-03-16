package com.Optum.CyFHIR.functions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ResourceUnitTest {

    private Resource resource;

    @BeforeEach
    void setUp() {
        resource = new Resource();
    }

    // ---- parseResources tests ----

    @Test
    void testParseResourcesFlatKeyValues() {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("_resourceId", "res-1");
        input.put("_id", 1L);
        input.put("_type", "resource");
        input.put("_isArray", false);
        input.put("resourceType", "Patient");
        input.put("id", "patient-123");
        input.put("gender", "male");

        Map result = resource.parseResources(input);

        assertEquals("Patient", result.get("resourceType"));
        assertEquals("patient-123", result.get("id"));
        assertEquals("male", result.get("gender"));
        assertFalse(result.containsKey("_resourceId"));
        assertFalse(result.containsKey("_id"));
        assertFalse(result.containsKey("_type"));
        assertFalse(result.containsKey("_isArray"));
    }

    @Test
    void testParseResourcesWithArrayListIsArrayFalse() {
        Map<String, Object> nestedData = new LinkedHashMap<>();
        nestedData.put("_resourceId", "res-1");
        nestedData.put("_id", 2L);
        nestedData.put("_type", "name");
        nestedData.put("_isArray", false);
        nestedData.put("family", "Smith");

        ArrayList<Map> list = new ArrayList<>();
        list.add(nestedData);

        Map<String, Object> input = new LinkedHashMap<>();
        input.put("_resourceId", "res-1");
        input.put("_id", 1L);
        input.put("_type", "resource");
        input.put("_isArray", false);
        input.put("resourceType", "Patient");
        input.put("someProp", list);

        Map result = resource.parseResources(input);

        assertTrue(result.containsKey("name"));
        Map nameMap = (Map) result.get("name");
        assertEquals("Smith", nameMap.get("family"));
    }

    @Test
    void testParseResourcesWithArrayListIsArrayTrue() {
        Map<String, Object> subResource1 = new LinkedHashMap<>();
        subResource1.put("_resourceId", "res-1");
        subResource1.put("_id", 2L);
        subResource1.put("_type", "identifier");
        subResource1.put("_isArray", true);
        subResource1.put("system", "http://example.com");
        subResource1.put("value", "123");

        Map<String, Object> subResource2 = new LinkedHashMap<>();
        subResource2.put("_resourceId", "res-1");
        subResource2.put("_id", 3L);
        subResource2.put("_type", "identifier");
        subResource2.put("_isArray", true);
        subResource2.put("system", "http://example2.com");
        subResource2.put("value", "456");

        ArrayList<Map> list = new ArrayList<>();
        list.add(subResource1);
        list.add(subResource2);

        Map<String, Object> input = new LinkedHashMap<>();
        input.put("_resourceId", "res-1");
        input.put("_id", 1L);
        input.put("_type", "resource");
        input.put("_isArray", false);
        input.put("resourceType", "Patient");
        input.put("identifiers", list);

        Map result = resource.parseResources(input);

        assertTrue(result.containsKey("identifier"));
        List identifiers = (List) result.get("identifier");
        assertEquals(2, identifiers.size());
    }

    @Test
    void testParseResourcesStringMatchingArrayPattern() {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("_resourceId", "res-1");
        input.put("_id", 1L);
        input.put("_type", "resource");
        input.put("_isArray", false);
        input.put("tags", "[tag1, tag2, tag3]");

        Map result = resource.parseResources(input);

        assertTrue(result.containsKey("tags"));
        List tags = (List) result.get("tags");
        assertEquals(3, tags.size());
        assertEquals("tag1", tags.get(0));
        assertEquals("tag2", tags.get(1));
        assertEquals("tag3", tags.get(2));
    }

    @Test
    void testParseResourcesNumericArrayPattern() {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("_resourceId", "res-1");
        input.put("_id", 1L);
        input.put("_type", "resource");
        input.put("_isArray", false);
        input.put("values", "[1, 2, 3]");

        Map result = resource.parseResources(input);

        assertTrue(result.containsKey("values"));
        List values = (List) result.get("values");
        assertEquals(3, values.size());
        assertTrue(values.get(0) instanceof Number);
    }

    // ---- removeMetadata tests ----

    @Test
    void testRemoveMetadataRemovesAllMetadataKeys() {
        Map<String, Object> input = new HashMap<>();
        input.put("_resourceId", "res-1");
        input.put("_id", 1L);
        input.put("_type", "resource");
        input.put("_isArray", false);
        input.put("resourceType", "Patient");

        Map result = resource.removeMetadata(input);

        assertFalse(result.containsKey("_resourceId"));
        assertFalse(result.containsKey("_id"));
        assertFalse(result.containsKey("_type"));
        assertFalse(result.containsKey("_isArray"));
        assertTrue(result.containsKey("resourceType"));
    }

    @Test
    void testRemoveMetadataWithoutMetadataKeys() {
        Map<String, Object> input = new HashMap<>();
        input.put("resourceType", "Patient");
        input.put("id", "123");

        Map result = resource.removeMetadata(input);

        assertEquals(2, result.size());
        assertTrue(result.containsKey("resourceType"));
        assertTrue(result.containsKey("id"));
    }

    @Test
    void testRemoveMetadataEmptyMap() {
        Map<String, Object> input = new HashMap<>();

        Map result = resource.removeMetadata(input);

        assertTrue(result.isEmpty());
    }

    // ---- parseArrayString tests ----

    @Test
    void testParseArrayStringNumericInteger() {
        Serializable result = resource.parseArrayString("42");
        assertTrue(result instanceof Number);
        assertEquals(42, ((Number) result).intValue());
    }

    @Test
    void testParseArrayStringNumericDecimal() {
        Serializable result = resource.parseArrayString("3.14");
        assertTrue(result instanceof Number);
    }

    @Test
    void testParseArrayStringText() {
        Serializable result = resource.parseArrayString("hello");
        assertEquals("hello", result);
    }

    @Test
    void testParseArrayStringWithLeadingTrailingSpaces() {
        Serializable result = resource.parseArrayString("  hello  ");
        assertEquals("hello", result);
    }

    @Test
    void testParseArrayStringNegativeNumber() {
        Serializable result = resource.parseArrayString("-5");
        assertTrue(result instanceof Number);
    }

    @Test
    void testParseArrayStringZero() {
        Serializable result = resource.parseArrayString("0");
        assertTrue(result instanceof Number);
    }

    // ---- format method tests ----

    @Test
    void testFormatWithEmptyPaths() {
        Map result = resource.format(new ArrayList<>());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ---- Additional parseResources edge cases ----

    @Test
    void testParseResourcesWithOnlyMetadata() {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("_resourceId", "res-1");
        input.put("_id", 1L);
        input.put("_type", "resource");
        input.put("_isArray", false);

        Map result = resource.parseResources(input);
        assertTrue(result.isEmpty());
    }

    @Test
    void testParseResourcesWithStringNotMatchingArrayPattern() {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("_resourceId", "res-1");
        input.put("_id", 1L);
        input.put("_type", "resource");
        input.put("_isArray", false);
        input.put("status", "active");
        input.put("name", "John Doe");

        Map result = resource.parseResources(input);
        assertEquals("active", result.get("status"));
        assertEquals("John Doe", result.get("name"));
    }

    @Test
    void testParseResourcesSingleElementArray() {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("_resourceId", "res-1");
        input.put("_id", 1L);
        input.put("_type", "resource");
        input.put("_isArray", false);
        input.put("codes", "[ABC]");

        Map result = resource.parseResources(input);
        assertTrue(result.containsKey("codes"));
    }

    @Test
    void testParseResourcesWithMixedContent() {
        Map<String, Object> nestedData = new LinkedHashMap<>();
        nestedData.put("_resourceId", "res-1");
        nestedData.put("_id", 2L);
        nestedData.put("_type", "name");
        nestedData.put("_isArray", false);
        nestedData.put("family", "Smith");
        nestedData.put("given", "[John, James]");

        ArrayList<Map> list = new ArrayList<>();
        list.add(nestedData);

        Map<String, Object> input = new LinkedHashMap<>();
        input.put("_resourceId", "res-1");
        input.put("_id", 1L);
        input.put("_type", "resource");
        input.put("_isArray", false);
        input.put("resourceType", "Patient");
        input.put("active", true);
        input.put("birthDate", "1990-01-01");
        input.put("nameField", list);

        Map result = resource.parseResources(input);
        assertEquals("Patient", result.get("resourceType"));
        assertEquals(true, result.get("active"));
        assertEquals("1990-01-01", result.get("birthDate"));
        assertTrue(result.containsKey("name"));
    }

    // ---- parseArrayString edge cases ----

    @Test
    void testParseArrayStringWithLargeNumber() {
        Serializable result = resource.parseArrayString("999999999");
        assertTrue(result instanceof Number);
    }

    @Test
    void testParseArrayStringWithFloat() {
        Serializable result = resource.parseArrayString("1.5");
        assertTrue(result instanceof Number);
    }

    @Test
    void testParseArrayStringEmptyString() {
        Serializable result = resource.parseArrayString("");
        assertEquals("", result);
    }

    @Test
    void testParseArrayStringWithSpecialChars() {
        Serializable result = resource.parseArrayString("http://example.com");
        assertEquals("http://example.com", result);
    }

    // ---- removeMetadata edge cases ----

    @Test
    void testRemoveMetadataPreservesNonMetadata() {
        Map<String, Object> input = new HashMap<>();
        input.put("_resourceId", "res-1");
        input.put("_id", 1L);
        input.put("_type", "resource");
        input.put("_isArray", false);
        input.put("resourceType", "Patient");
        input.put("id", "123");
        input.put("active", true);
        input.put("gender", "male");

        Map result = resource.removeMetadata(input);
        assertEquals(4, result.size());
        assertTrue(result.containsKey("resourceType"));
        assertTrue(result.containsKey("id"));
        assertTrue(result.containsKey("active"));
        assertTrue(result.containsKey("gender"));
    }
}
