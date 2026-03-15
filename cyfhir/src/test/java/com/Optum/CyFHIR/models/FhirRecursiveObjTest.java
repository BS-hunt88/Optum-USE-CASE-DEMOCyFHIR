package com.Optum.CyFHIR.models;

import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.Node;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class FhirRecursiveObjTest {

    @Test
    void testDefaultNodeIsNull() {
        FhirRecursiveObj obj = new FhirRecursiveObj();
        assertNull(obj.getNode());
    }

    @Test
    void testDefaultRelationshipsIsNull() {
        FhirRecursiveObj obj = new FhirRecursiveObj();
        assertNull(obj.getRelationships());
    }

    @Test
    void testSetAndGetNode() {
        FhirRecursiveObj obj = new FhirRecursiveObj();
        Node mockNode = mock(Node.class);

        obj.setNode(mockNode);

        assertEquals(mockNode, obj.getNode());
    }

    @Test
    void testSetAndGetRelationships() {
        FhirRecursiveObj obj = new FhirRecursiveObj();
        ArrayList<FhirRelationship> rels = new ArrayList<>();
        FhirRelationship rel = new FhirRelationship();
        rels.add(rel);

        obj.setRelationships(rels);

        assertEquals(1, obj.getRelationships().size());
        assertEquals(rel, obj.getRelationships().get(0));
    }

    @Test
    void testSetNodeToNull() {
        FhirRecursiveObj obj = new FhirRecursiveObj();
        Node mockNode = mock(Node.class);
        obj.setNode(mockNode);

        obj.setNode(null);

        assertNull(obj.getNode());
    }

    @Test
    void testSetRelationshipsToEmptyList() {
        FhirRecursiveObj obj = new FhirRecursiveObj();
        ArrayList<FhirRelationship> rels = new ArrayList<>();

        obj.setRelationships(rels);

        assertNotNull(obj.getRelationships());
        assertTrue(obj.getRelationships().isEmpty());
    }

    @Test
    void testSetRelationshipsWithMultipleItems() {
        FhirRecursiveObj obj = new FhirRecursiveObj();
        ArrayList<FhirRelationship> rels = new ArrayList<>();
        rels.add(new FhirRelationship());
        rels.add(new FhirRelationship());
        rels.add(new FhirRelationship());

        obj.setRelationships(rels);

        assertEquals(3, obj.getRelationships().size());
    }
}
