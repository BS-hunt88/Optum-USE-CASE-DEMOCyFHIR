package com.Optum.CyFHIR.procedures;

import apoc.result.MapResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ConvertUnitTest {

    private Convert convert;

    @BeforeEach
    void setUp() {
        convert = new Convert();
    }

    // ---- toTree tests ----

    @Test
    void testToTreeEmptyPaths() {
        List<Path> paths = new ArrayList<>();
        List<MapResult> results = convert.toTree(paths, true, new HashMap<>())
                .collect(Collectors.toList());

        assertEquals(1, results.size());
        assertTrue(results.get(0).value.isEmpty());
    }

    @Test
    void testToTreeSinglePathOneNode() {
        Node node = mock(Node.class);
        when(node.getId()).thenReturn(1L);
        Map<String, Object> nodeProps = new HashMap<>();
        nodeProps.put("resourceType", "Patient");
        when(node.getAllProperties()).thenReturn(nodeProps);
        when(node.getLabels()).thenReturn(Collections.singletonList(Label.label("resource")));

        Path path = mock(Path.class);
        when(path.startNode()).thenReturn(node);

        Iterator<Entity> entityIterator = Collections.<Entity>singletonList(node).iterator();
        when(path.iterator()).thenReturn(entityIterator);

        List<Path> paths = Collections.singletonList(path);
        List<MapResult> results = convert.toTree(paths, true, new HashMap<>())
                .collect(Collectors.toList());

        assertEquals(1, results.size());
        Map<String, Object> resultMap = results.get(0).value;
        assertNotNull(resultMap);
        assertEquals(1L, resultMap.get("_id"));
        assertEquals("Patient", resultMap.get("resourceType"));
    }

    @Test
    void testToTreePathWithNodeRelationshipNode() {
        Node node1 = mock(Node.class);
        when(node1.getId()).thenReturn(1L);
        Map<String, Object> node1Props = new HashMap<>();
        node1Props.put("resourceType", "Patient");
        when(node1.getAllProperties()).thenReturn(node1Props);
        when(node1.getLabels()).thenReturn(Collections.singletonList(Label.label("entry")));

        Node node2 = mock(Node.class);
        when(node2.getId()).thenReturn(2L);
        Map<String, Object> node2Props = new HashMap<>();
        node2Props.put("resourceType", "Encounter");
        when(node2.getAllProperties()).thenReturn(node2Props);
        when(node2.getLabels()).thenReturn(Collections.singletonList(Label.label("resource")));

        Relationship rel = mock(Relationship.class);
        RelationshipType relType = mock(RelationshipType.class);
        when(relType.name()).thenReturn("HAS_RESOURCE");
        when(rel.getType()).thenReturn(relType);
        when(rel.getOtherNode(node1)).thenReturn(node2);
        when(rel.getAllProperties()).thenReturn(new HashMap<>());

        Path path = mock(Path.class);
        when(path.startNode()).thenReturn(node1);

        List<Entity> entities = new ArrayList<>();
        entities.add(node1);
        entities.add(rel);
        entities.add(node2);
        when(path.iterator()).thenReturn(entities.iterator());

        List<Path> paths = Collections.singletonList(path);
        List<MapResult> results = convert.toTree(paths, true, new HashMap<>())
                .collect(Collectors.toList());

        assertEquals(1, results.size());
        Map<String, Object> resultMap = results.get(0).value;
        assertNotNull(resultMap);
        assertTrue(resultMap.containsKey("has_resource"));
    }

    @Test
    void testToTreeLowerCaseRelsTrue() {
        Node node1 = mock(Node.class);
        when(node1.getId()).thenReturn(1L);
        when(node1.getAllProperties()).thenReturn(new HashMap<>());
        when(node1.getLabels()).thenReturn(Collections.singletonList(Label.label("entry")));

        Node node2 = mock(Node.class);
        when(node2.getId()).thenReturn(2L);
        when(node2.getAllProperties()).thenReturn(new HashMap<>());
        when(node2.getLabels()).thenReturn(Collections.singletonList(Label.label("resource")));

        Relationship rel = mock(Relationship.class);
        RelationshipType relType = mock(RelationshipType.class);
        when(relType.name()).thenReturn("RESOURCE");
        when(rel.getType()).thenReturn(relType);
        when(rel.getOtherNode(node1)).thenReturn(node2);
        when(rel.getAllProperties()).thenReturn(new HashMap<>());

        Path path = mock(Path.class);
        when(path.startNode()).thenReturn(node1);
        List<Entity> entities = new ArrayList<>();
        entities.add(node1);
        entities.add(rel);
        entities.add(node2);
        when(path.iterator()).thenReturn(entities.iterator());

        List<MapResult> results = convert.toTree(Collections.singletonList(path), true, new HashMap<>())
                .collect(Collectors.toList());

        Map<String, Object> resultMap = results.get(0).value;
        assertTrue(resultMap.containsKey("resource"));
        assertFalse(resultMap.containsKey("RESOURCE"));
    }

    @Test
    void testToTreeLowerCaseRelsFalse() {
        Node node1 = mock(Node.class);
        when(node1.getId()).thenReturn(1L);
        when(node1.getAllProperties()).thenReturn(new HashMap<>());
        when(node1.getLabels()).thenReturn(Collections.singletonList(Label.label("entry")));

        Node node2 = mock(Node.class);
        when(node2.getId()).thenReturn(2L);
        when(node2.getAllProperties()).thenReturn(new HashMap<>());
        when(node2.getLabels()).thenReturn(Collections.singletonList(Label.label("resource")));

        Relationship rel = mock(Relationship.class);
        RelationshipType relType = mock(RelationshipType.class);
        when(relType.name()).thenReturn("RESOURCE");
        when(rel.getType()).thenReturn(relType);
        when(rel.getOtherNode(node1)).thenReturn(node2);
        when(rel.getAllProperties()).thenReturn(new HashMap<>());

        Path path = mock(Path.class);
        when(path.startNode()).thenReturn(node1);
        List<Entity> entities = new ArrayList<>();
        entities.add(node1);
        entities.add(rel);
        entities.add(node2);
        when(path.iterator()).thenReturn(entities.iterator());

        List<MapResult> results = convert.toTree(Collections.singletonList(path), false, new HashMap<>())
                .collect(Collectors.toList());

        Map<String, Object> resultMap = results.get(0).value;
        assertTrue(resultMap.containsKey("RESOURCE"));
        assertFalse(resultMap.containsKey("resource"));
    }

    @Test
    void testToTreeWithRelationshipProperties() {
        Node node1 = mock(Node.class);
        when(node1.getId()).thenReturn(1L);
        when(node1.getAllProperties()).thenReturn(new HashMap<>());
        when(node1.getLabels()).thenReturn(Collections.singletonList(Label.label("entry")));

        Node node2 = mock(Node.class);
        when(node2.getId()).thenReturn(2L);
        when(node2.getAllProperties()).thenReturn(new HashMap<>());
        when(node2.getLabels()).thenReturn(Collections.singletonList(Label.label("resource")));

        Relationship rel = mock(Relationship.class);
        RelationshipType relType = mock(RelationshipType.class);
        when(relType.name()).thenReturn("RESOURCE");
        when(rel.getType()).thenReturn(relType);
        when(rel.getOtherNode(node1)).thenReturn(node2);
        Map<String, Object> relProps = new HashMap<>();
        relProps.put("weight", 1.0);
        when(rel.getAllProperties()).thenReturn(relProps);

        Path path = mock(Path.class);
        when(path.startNode()).thenReturn(node1);
        List<Entity> entities = new ArrayList<>();
        entities.add(node1);
        entities.add(rel);
        entities.add(node2);
        when(path.iterator()).thenReturn(entities.iterator());

        List<MapResult> results = convert.toTree(Collections.singletonList(path), true, new HashMap<>())
                .collect(Collectors.toList());

        Map<String, Object> resultMap = results.get(0).value;
        List<Map<String, Object>> children = (List<Map<String, Object>>) resultMap.get("resource");
        assertNotNull(children);
        Map<String, Object> childMap = children.get(0);
        assertEquals(1.0, childMap.get("resource.weight"));
    }

    @Test
    void testToTreeDeduplicatesNodes() {
        Node node1 = mock(Node.class);
        when(node1.getId()).thenReturn(1L);
        when(node1.getAllProperties()).thenReturn(new HashMap<>());
        when(node1.getLabels()).thenReturn(Collections.singletonList(Label.label("entry")));

        Node node2 = mock(Node.class);
        when(node2.getId()).thenReturn(2L);
        when(node2.getAllProperties()).thenReturn(new HashMap<>());
        when(node2.getLabels()).thenReturn(Collections.singletonList(Label.label("resource")));

        Relationship rel = mock(Relationship.class);
        RelationshipType relType = mock(RelationshipType.class);
        when(relType.name()).thenReturn("RESOURCE");
        when(rel.getType()).thenReturn(relType);
        when(rel.getOtherNode(node1)).thenReturn(node2);
        when(rel.getAllProperties()).thenReturn(new HashMap<>());

        // Two paths sharing the same nodes
        Path path1 = mock(Path.class);
        when(path1.startNode()).thenReturn(node1);
        List<Entity> entities1 = new ArrayList<>();
        entities1.add(node1);
        entities1.add(rel);
        entities1.add(node2);
        when(path1.iterator()).thenReturn(entities1.iterator());

        Path path2 = mock(Path.class);
        when(path2.startNode()).thenReturn(node1);
        List<Entity> entities2 = new ArrayList<>();
        entities2.add(node1);
        when(path2.iterator()).thenReturn(entities2.iterator());

        List<Path> paths = Arrays.asList(path1, path2);
        List<MapResult> results = convert.toTree(paths, true, new HashMap<>())
                .collect(Collectors.toList());

        // Should only have one distinct start node
        assertEquals(1, results.size());
    }

    @Test
    void testToTreeWithEmptyConfig() {
        Node node = mock(Node.class);
        when(node.getId()).thenReturn(1L);
        Map<String, Object> nodeProps = new HashMap<>();
        nodeProps.put("resourceType", "Patient");
        nodeProps.put("id", "test-123");
        nodeProps.put("secretField", "hidden");
        when(node.getAllProperties()).thenReturn(nodeProps);
        when(node.getLabels()).thenReturn(Collections.singletonList(Label.label("resource")));

        Path path = mock(Path.class);
        when(path.startNode()).thenReturn(node);
        when(path.iterator()).thenReturn(Collections.<Entity>singletonList(node).iterator());

        // Empty config should not filter anything
        List<MapResult> results = convert.toTree(Collections.singletonList(path), true, new HashMap<>())
                .collect(Collectors.toList());

        Map<String, Object> resultMap = results.get(0).value;
        assertTrue(resultMap.containsKey("resourceType"));
        assertTrue(resultMap.containsKey("id"));
        assertTrue(resultMap.containsKey("secretField"));
        assertEquals("Patient", resultMap.get("resourceType"));
    }
}
