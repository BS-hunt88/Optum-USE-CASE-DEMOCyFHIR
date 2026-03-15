package com.Optum.CyFHIR.models;

import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.Node;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class FhirRelationshipTest {

    @Test
    void testDefaultParentNodeIsNull() {
        FhirRelationship rel = new FhirRelationship();
        assertNull(rel.getParentNode());
    }

    @Test
    void testDefaultChildUUIDIsNull() {
        FhirRelationship rel = new FhirRelationship();
        assertNull(rel.getChildUUID());
    }

    @Test
    void testDefaultRelationTypeIsNull() {
        FhirRelationship rel = new FhirRelationship();
        assertNull(rel.getRelationType());
    }

    @Test
    void testSetAndGetParentNode() {
        FhirRelationship rel = new FhirRelationship();
        Node mockNode = mock(Node.class);

        rel.setParentNode(mockNode);

        assertEquals(mockNode, rel.getParentNode());
    }

    @Test
    void testSetChildRelationship() {
        FhirRelationship rel = new FhirRelationship();

        rel.setChildRelationship("urn:uuid:test-123", "reference");

        assertEquals("urn:uuid:test-123", rel.getChildUUID());
        assertEquals("reference", rel.getRelationType());
    }

    @Test
    void testSetParentNodeToNull() {
        FhirRelationship rel = new FhirRelationship();
        Node mockNode = mock(Node.class);
        rel.setParentNode(mockNode);

        rel.setParentNode(null);

        assertNull(rel.getParentNode());
    }

    @Test
    void testSetChildRelationshipWithNullValues() {
        FhirRelationship rel = new FhirRelationship();

        rel.setChildRelationship(null, null);

        assertNull(rel.getChildUUID());
        assertNull(rel.getRelationType());
    }

    @Test
    void testSetChildRelationshipOverwrite() {
        FhirRelationship rel = new FhirRelationship();

        rel.setChildRelationship("uuid-1", "type-1");
        rel.setChildRelationship("uuid-2", "type-2");

        assertEquals("uuid-2", rel.getChildUUID());
        assertEquals("type-2", rel.getRelationType());
    }

    @Test
    void testFullRelationshipSetup() {
        FhirRelationship rel = new FhirRelationship();
        Node mockNode = mock(Node.class);

        rel.setParentNode(mockNode);
        rel.setChildRelationship("urn:uuid:child-456", "encounter");

        assertEquals(mockNode, rel.getParentNode());
        assertEquals("urn:uuid:child-456", rel.getChildUUID());
        assertEquals("encounter", rel.getRelationType());
    }
}
