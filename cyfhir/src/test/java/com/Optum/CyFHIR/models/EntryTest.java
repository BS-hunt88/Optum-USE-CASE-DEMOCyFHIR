package com.Optum.CyFHIR.models;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class EntryTest {

    @Test
    void testSetResourceSetsFullUrl() {
        Entry entry = new Entry();
        Map<String, Object> resource = new HashMap<>();
        resource.put("id", "test-id-123");
        resource.put("resourceType", "Patient");

        entry.setResource(resource);

        assertEquals("urn:uuid:test-id-123", entry.get("fullUrl"));
    }

    @Test
    void testSetResourceSetsResource() {
        Entry entry = new Entry();
        Map<String, Object> resource = new HashMap<>();
        resource.put("id", "test-id-123");
        resource.put("resourceType", "Patient");

        entry.setResource(resource);

        assertNotNull(entry.get("resource"));
        assertEquals(resource, entry.get("resource"));
    }

    @Test
    void testSetResourceSetsRequestWithPostMethod() {
        Entry entry = new Entry();
        Map<String, Object> resource = new HashMap<>();
        resource.put("id", "test-id-123");
        resource.put("resourceType", "Patient");

        entry.setResource(resource);

        Object request = entry.get("request");
        assertNotNull(request);
        assertTrue(request instanceof Map);
        Map requestMap = (Map) request;
        assertEquals("POST", requestMap.get("method"));
        assertEquals("Patient", requestMap.get("url"));
    }

    @Test
    void testSetResourceWithEncounterType() {
        Entry entry = new Entry();
        Map<String, Object> resource = new HashMap<>();
        resource.put("id", "enc-456");
        resource.put("resourceType", "Encounter");

        entry.setResource(resource);

        assertEquals("urn:uuid:enc-456", entry.get("fullUrl"));
        Map requestMap = (Map) entry.get("request");
        assertEquals("Encounter", requestMap.get("url"));
    }

    @Test
    void testSetResourceWithConditionType() {
        Entry entry = new Entry();
        Map<String, Object> resource = new HashMap<>();
        resource.put("id", "cond-789");
        resource.put("resourceType", "Condition");

        entry.setResource(resource);

        assertEquals("urn:uuid:cond-789", entry.get("fullUrl"));
        Map requestMap = (Map) entry.get("request");
        assertEquals("Condition", requestMap.get("url"));
    }

    @Test
    void testSetResourceWithBundleType() {
        Entry entry = new Entry();
        Map<String, Object> resource = new HashMap<>();
        resource.put("id", "bundle-001");
        resource.put("resourceType", "Bundle");

        entry.setResource(resource);

        assertEquals("urn:uuid:bundle-001", entry.get("fullUrl"));
        Map requestMap = (Map) entry.get("request");
        assertEquals("Bundle", requestMap.get("url"));
    }

    @Test
    void testSetResourceWithNullId() {
        Entry entry = new Entry();
        Map<String, Object> resource = new HashMap<>();
        resource.put("id", null);
        resource.put("resourceType", "Patient");

        entry.setResource(resource);

        assertEquals("urn:uuid:null", entry.get("fullUrl"));
    }

    @Test
    void testSetResourceWithMissingId() {
        Entry entry = new Entry();
        Map<String, Object> resource = new HashMap<>();
        resource.put("resourceType", "Patient");

        entry.setResource(resource);

        assertEquals("urn:uuid:null", entry.get("fullUrl"));
    }

    @Test
    void testSetResourceWithNullResourceType() {
        Entry entry = new Entry();
        Map<String, Object> resource = new HashMap<>();
        resource.put("id", "test-id");
        resource.put("resourceType", null);

        entry.setResource(resource);

        Map requestMap = (Map) entry.get("request");
        assertNull(requestMap.get("url"));
    }

    @Test
    void testSetResourceWithMissingResourceType() {
        Entry entry = new Entry();
        Map<String, Object> resource = new HashMap<>();
        resource.put("id", "test-id");

        entry.setResource(resource);

        Map requestMap = (Map) entry.get("request");
        assertNull(requestMap.get("url"));
    }
}
