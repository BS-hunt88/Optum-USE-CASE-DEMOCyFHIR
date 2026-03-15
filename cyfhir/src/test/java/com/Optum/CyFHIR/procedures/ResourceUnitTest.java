package com.Optum.CyFHIR.procedures;

import com.Optum.CyFHIR.models.Entry;
import com.Optum.CyFHIR.models.FhirRecursiveObj;
import com.Optum.CyFHIR.models.FhirRelationship;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.neo4j.graphdb.*;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ResourceUnitTest {

    private Resource resource;

    @BeforeEach
    void setUp() throws Exception {
        resource = new Resource();
    }

    // ---- stringToMap tests ----

    @Test
    void testStringToMapValidJson() throws IOException {
        String json = "{\"key\":\"value\",\"num\":42}";
        Map<String, Object> result = resource.stringToMap(json);
        assertEquals("value", result.get("key"));
        assertEquals(42, result.get("num"));
    }

    @Test
    void testStringToMapEmptyObject() throws IOException {
        String json = "{}";
        Map<String, Object> result = resource.stringToMap(json);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testStringToMapNestedJson() throws IOException {
        String json = "{\"outer\":{\"inner\":\"value\"}}";
        Map<String, Object> result = resource.stringToMap(json);
        assertNotNull(result.get("outer"));
        assertTrue(result.get("outer") instanceof Map);
        Map innerMap = (Map) result.get("outer");
        assertEquals("value", innerMap.get("inner"));
    }

    @Test
    void testStringToMapInvalidJsonThrowsIOException() {
        assertThrows(IOException.class, () -> {
            resource.stringToMap("not valid json");
        });
    }

    @Test
    void testStringToMapWithArray() throws IOException {
        String json = "{\"items\":[1,2,3]}";
        Map<String, Object> result = resource.stringToMap(json);
        assertNotNull(result.get("items"));
        assertTrue(result.get("items") instanceof List);
    }

    // ---- validateFHIR tests ----

    @Test
    void testValidateFHIREmptyConfigReturnsNull() throws Exception {
        Map<String, Object> configMap = new HashMap<>();
        IAnyResource result = resource.validateFHIR("{}", "Patient", configMap);
        assertNull(result);
    }

    @Test
    void testValidateFHIRWithoutValidationKeyReturnsNull() throws Exception {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("someOtherKey", "value");
        IAnyResource result = resource.validateFHIR("{}", "Patient", configMap);
        assertNull(result);
    }

    @Test
    void testValidateFHIRWithValidationFalseReturnsNull() throws Exception {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("validation", false);
        IAnyResource result = resource.validateFHIR("{}", "Patient", configMap);
        assertNull(result);
    }

    @Test
    void testValidateFHIRWithValidationTrueNoVersion() throws Exception {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("validation", true);
        String patientJson = "{\"resourceType\":\"Patient\",\"id\":\"test-123\"}";
        IAnyResource result = resource.validateFHIR(patientJson, "Patient", configMap);
        assertNotNull(result);
    }

    @Test
    void testValidateFHIRWithValidationTrueAndDSTU3() throws Exception {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("validation", true);
        configMap.put("version", "DSTU3");
        String patientJson = "{\"resourceType\":\"Patient\",\"id\":\"test-123\"}";
        IAnyResource result = resource.validateFHIR(patientJson, "Patient", configMap);
        assertNotNull(result);
    }

    @Test
    void testValidateFHIRWithValidationTrueAndR5() throws Exception {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("validation", true);
        configMap.put("version", "R5");
        String patientJson = "{\"resourceType\":\"Patient\",\"id\":\"test-123\"}";
        IAnyResource result = resource.validateFHIR(patientJson, "Patient", configMap);
        assertNotNull(result);
    }

    // ---- attachLooseReferences tests ----

    @Test
    void testAttachLooseReferencesSingleFullUrl() {
        Transaction tx = mock(Transaction.class);
        when(tx.execute(anyString())).thenReturn(mock(Result.class));

        ArrayList<String> fullUrls = new ArrayList<>();
        fullUrls.add("urn:uuid:test-123");

        resource.attachLooseReferences(fullUrls, tx);

        verify(tx).execute(contains("urn:uuid:test-123"));
    }

    @Test
    void testAttachLooseReferencesMultipleFullUrls() {
        Transaction tx = mock(Transaction.class);
        when(tx.execute(anyString())).thenReturn(mock(Result.class));

        ArrayList<String> fullUrls = new ArrayList<>();
        fullUrls.add("urn:uuid:test-1");
        fullUrls.add("urn:uuid:test-2");

        resource.attachLooseReferences(fullUrls, tx);

        verify(tx).execute(contains("urn:uuid:test-1"));
    }

    // ---- addToDatabase tests ----

    @Test
    void testAddToDatabaseDuplicateReturnsEmptyList() {
        Transaction tx = mock(Transaction.class);
        Node existingNode = mock(Node.class);
        when(existingNode.getProperty("_resourceId")).thenReturn("existing-id");

        ResourceIterator<Node> iterator = mock(ResourceIterator.class);
        when(iterator.next()).thenReturn(existingNode);
        when(tx.findNodes(any(Label.class), eq("fullUrl"), anyString())).thenReturn(iterator);

        Entry entry = new Entry();
        Map<String, Object> resourceMap = new HashMap<>();
        resourceMap.put("id", "test-id");
        resourceMap.put("resourceType", "Patient");
        entry.setResource(resourceMap);

        ArrayList<FhirRelationship> result = resource.addToDatabase(entry, tx);
        assertTrue(result.isEmpty());
    }

    @Test
    void testAddToDatabaseNewResourceCallsNodeRecursion() {
        Transaction tx = mock(Transaction.class);
        Node newNode = mock(Node.class);

        ResourceIterator<Node> iterator = mock(ResourceIterator.class);
        when(iterator.next()).thenThrow(new NoSuchElementException());
        when(tx.findNodes(any(Label.class), eq("fullUrl"), anyString())).thenReturn(iterator);
        when(tx.createNode(any(Label.class))).thenReturn(newNode);

        Entry entry = new Entry();
        Map<String, Object> resourceMap = new HashMap<>();
        resourceMap.put("id", "new-id");
        resourceMap.put("resourceType", "Patient");
        entry.setResource(resourceMap);

        ArrayList<FhirRelationship> result = resource.addToDatabase(entry, tx);
        assertNotNull(result);
        verify(tx, atLeast(1)).createNode(any(Label.class));
    }

    // ---- createRelationships tests ----

    @Test
    void testCreateRelationshipsEmptyList() {
        Transaction tx = mock(Transaction.class);
        ArrayList<FhirRelationship> relationships = new ArrayList<>();

        resource.createRelationships(relationships, tx);
        // No exception expected
    }

    @Test
    void testCreateRelationshipsChildNodeNull() {
        Transaction tx = mock(Transaction.class);
        when(tx.findNode(any(Label.class), eq("fullUrl"), anyString())).thenReturn(null);

        Node parentNode = mock(Node.class);
        FhirRelationship rel = new FhirRelationship();
        rel.setParentNode(parentNode);
        rel.setChildRelationship("urn:uuid:missing", "reference");

        ArrayList<FhirRelationship> relationships = new ArrayList<>();
        relationships.add(rel);

        resource.createRelationships(relationships, tx);
        // Should not throw - relationship skipped when childNode is null
    }

    // ---- startToNodes tests ----

    @Test
    void testStartToNodesNull() throws Exception {
        // Use reflection to call the private method
        java.lang.reflect.Method method = Resource.class.getDeclaredMethod("startToNodes", Object.class, Transaction.class);
        method.setAccessible(true);

        Transaction tx = mock(Transaction.class);
        List<Node> result = (List<Node>) method.invoke(resource, null, tx);
        assertTrue(result.isEmpty());
    }

    @Test
    void testStartToNodesWithNode() throws Exception {
        java.lang.reflect.Method method = Resource.class.getDeclaredMethod("startToNodes", Object.class, Transaction.class);
        method.setAccessible(true);

        Transaction tx = mock(Transaction.class);
        Node node = mock(Node.class);
        List<Node> result = (List<Node>) method.invoke(resource, node, tx);
        assertEquals(1, result.size());
        assertEquals(node, result.get(0));
    }

    @Test
    void testStartToNodesWithNumber() throws Exception {
        java.lang.reflect.Method method = Resource.class.getDeclaredMethod("startToNodes", Object.class, Transaction.class);
        method.setAccessible(true);

        Transaction tx = mock(Transaction.class);
        Node node = mock(Node.class);
        when(tx.getNodeById(42L)).thenReturn(node);

        List<Node> result = (List<Node>) method.invoke(resource, 42L, tx);
        assertEquals(1, result.size());
        assertEquals(node, result.get(0));
    }

    @Test
    void testStartToNodesWithEmptyList() throws Exception {
        java.lang.reflect.Method method = Resource.class.getDeclaredMethod("startToNodes", Object.class, Transaction.class);
        method.setAccessible(true);

        Transaction tx = mock(Transaction.class);
        List<Node> result = (List<Node>) method.invoke(resource, new ArrayList<>(), tx);
        assertTrue(result.isEmpty());
    }

    @Test
    void testStartToNodesWithNodeList() throws Exception {
        java.lang.reflect.Method method = Resource.class.getDeclaredMethod("startToNodes", Object.class, Transaction.class);
        method.setAccessible(true);

        Transaction tx = mock(Transaction.class);
        Node node1 = mock(Node.class);
        Node node2 = mock(Node.class);
        List<Node> nodeList = new ArrayList<>();
        nodeList.add(node1);
        nodeList.add(node2);

        List<Node> result = (List<Node>) method.invoke(resource, nodeList, tx);
        assertEquals(2, result.size());
    }

    @Test
    void testStartToNodesWithNumberList() throws Exception {
        java.lang.reflect.Method method = Resource.class.getDeclaredMethod("startToNodes", Object.class, Transaction.class);
        method.setAccessible(true);

        Transaction tx = mock(Transaction.class);
        Node node1 = mock(Node.class);
        Node node2 = mock(Node.class);
        when(tx.getNodeById(1L)).thenReturn(node1);
        when(tx.getNodeById(2L)).thenReturn(node2);

        List<Number> numberList = new ArrayList<>();
        numberList.add(1L);
        numberList.add(2L);

        List<Node> result = (List<Node>) method.invoke(resource, numberList, tx);
        assertEquals(2, result.size());
    }

    @Test
    void testStartToNodesWithUnsupportedType() throws Exception {
        java.lang.reflect.Method method = Resource.class.getDeclaredMethod("startToNodes", Object.class, Transaction.class);
        method.setAccessible(true);

        Transaction tx = mock(Transaction.class);

        try {
            method.invoke(resource, "unsupported string", tx);
            fail("Expected exception");
        } catch (java.lang.reflect.InvocationTargetException e) {
            assertTrue(e.getCause() instanceof Exception);
        }
    }

    // ---- nodeRecursion tests ----

    @Test
    void testNodeRecursionSimpleFlatJson() {
        Transaction tx = mock(Transaction.class);
        Node mockNode = mock(Node.class);
        when(tx.createNode(any(Label.class))).thenReturn(mockNode);

        Map<String, Object> json = new LinkedHashMap<>();
        json.put("resourceType", "Patient");
        json.put("id", "test-123");

        FhirRecursiveObj result = resource.nodeRecursion(tx, json, "resource", true, "test-123");

        assertNotNull(result);
        assertNotNull(result.getNode());
        verify(mockNode).setProperty("_isArray", true);
        verify(mockNode).setProperty("_resourceId", "test-123");
        verify(mockNode).setProperty("resourceType", "Patient");
        verify(mockNode).setProperty("id", "test-123");
    }

    @Test
    void testNodeRecursionWithReference() {
        Transaction tx = mock(Transaction.class);
        Node mockNode = mock(Node.class);
        when(tx.createNode(any(Label.class))).thenReturn(mockNode);

        Map<String, Object> json = new LinkedHashMap<>();
        json.put("reference", "urn:uuid:patient-ref");

        FhirRecursiveObj result = resource.nodeRecursion(tx, json, "subject", false, "res-id");

        assertNotNull(result);
        assertNotNull(result.getRelationships());
        boolean hasReference = result.getRelationships().stream()
                .anyMatch(r -> "reference".equals(r.getRelationType()));
        assertTrue(hasReference);
    }

    @Test
    void testNodeRecursionWithPrimitiveArray() {
        Transaction tx = mock(Transaction.class);
        Node mockNode = mock(Node.class);
        when(tx.createNode(any(Label.class))).thenReturn(mockNode);

        Map<String, Object> json = new LinkedHashMap<>();
        ArrayList<String> stringList = new ArrayList<>();
        stringList.add("tag1");
        stringList.add("tag2");
        json.put("tags", stringList);

        FhirRecursiveObj result = resource.nodeRecursion(tx, json, "resource", false, "res-id");

        assertNotNull(result);
        verify(mockNode).setProperty(eq("tags"), eq("[tag1, tag2]"));
    }

    @Test
    void testNodeRecursionWithLinkedHashMap() {
        Transaction tx = mock(Transaction.class);
        Node parentNode = mock(Node.class);
        Node childNode = mock(Node.class);
        when(tx.createNode(any(Label.class))).thenReturn(parentNode, childNode);

        Map<String, Object> json = new LinkedHashMap<>();
        LinkedHashMap<String, Object> nestedMap = new LinkedHashMap<>();
        nestedMap.put("value", "test");
        json.put("name", nestedMap);

        FhirRecursiveObj result = resource.nodeRecursion(tx, json, "resource", false, "res-id");

        assertNotNull(result);
        // Should create two nodes: parent + child via getRelationshipsFromMap
        verify(tx, atLeast(2)).createNode(any(Label.class));
    }

    @Test
    void testNodeRecursionWithArrayOfLinkedHashMaps() {
        Transaction tx = mock(Transaction.class);
        Node parentNode = mock(Node.class);
        Node childNode = mock(Node.class);
        when(tx.createNode(any(Label.class))).thenReturn(parentNode, childNode);

        Map<String, Object> json = new LinkedHashMap<>();
        ArrayList<Object> list = new ArrayList<>();
        LinkedHashMap<String, Object> item = new LinkedHashMap<>();
        item.put("system", "http://example.com");
        list.add(item);
        json.put("identifier", list);

        FhirRecursiveObj result = resource.nodeRecursion(tx, json, "resource", false, "res-id");

        assertNotNull(result);
        verify(tx, atLeast(2)).createNode(any(Label.class));
    }
}
