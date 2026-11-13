package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class GraphSceneAdapterTest {

    @Test
    fun testValidUndirectedGraphAdaptation() {
        val entityA = VisualEntity(
            visualKey = "graph:vertex:A",
            elementId = "vertex:A",
            dataValue = "A",
            slotKey = "graph_slot:A"
        )
        val entityB = VisualEntity(
            visualKey = "graph:vertex:B",
            elementId = "vertex:B",
            dataValue = "B",
            slotKey = "graph_slot:B"
        )
        val connAB = VisualConnection(
            connectionKey = "edge:A<->B",
            fromVisualKey = "graph:vertex:A",
            toVisualKey = "graph:vertex:B",
            label = "weight:5",
            type = ConnectionType.GRAPH_EDGE,
            directed = false
        )

        val subScene = StructureSubScene(
            subPaneId = "graph_main_pane",
            type = StructureType.GRAPH,
            entities = listOf(entityA, entityB),
            connections = listOf(connAB)
        )

        val result = GraphSceneAdapter.adapt(subScene)

        assertEquals(2, result.vertices.size)
        assertEquals(1, result.edges.size)
        assertEquals("A", result.vertices[0].key)
        assertEquals("B", result.vertices[1].key)
        assertEquals("edge:A<->B", result.edges[0].key)
        assertEquals(false, result.edges[0].directed)
        assertEquals("weight:5", result.edgeConnectionMap["edge:A<->B"]?.label)
        assertEquals(entityA, result.vertexEntityMap["A"])
        assertEquals(connAB, result.edgeConnectionMap["edge:A<->B"])
    }

    @Test
    fun testValidDirectedGraphAdaptation() {
        val entityA = VisualEntity(
            visualKey = "graph:vertex:A",
            elementId = "vertex:A",
            dataValue = "A",
            slotKey = "graph_slot:A"
        )
        val entityB = VisualEntity(
            visualKey = "graph:vertex:B",
            elementId = "vertex:B",
            dataValue = "B",
            slotKey = "graph_slot:B"
        )
        val connAB = VisualConnection(
            connectionKey = "edge:A->B",
            fromVisualKey = "graph:vertex:A",
            toVisualKey = "graph:vertex:B",
            type = ConnectionType.GRAPH_EDGE,
            directed = true
        )

        val subScene = StructureSubScene(
            subPaneId = "graph_main_pane",
            type = StructureType.GRAPH,
            entities = listOf(entityA, entityB),
            connections = listOf(connAB)
        )

        val result = GraphSceneAdapter.adapt(subScene)

        assertEquals(1, result.edges.size)
        assertEquals(true, result.edges[0].directed)
        assertEquals("edge:A->B", result.edges[0].key)
    }

    @Test
    fun testInvalidStructureTypeThrows() {
        val subScene = StructureSubScene(
            subPaneId = "tree_pane",
            type = StructureType.TREE
        )

        assertFailsWith<IllegalArgumentException> {
            GraphSceneAdapter.adapt(subScene)
        }
    }

    @Test
    fun testInvalidElementIdPrefixThrows() {
        val entity = VisualEntity(
            visualKey = "graph:vertex:A",
            elementId = "node:A",
            dataValue = "A",
            slotKey = "graph_slot:A"
        )
        val subScene = StructureSubScene(
            subPaneId = "graph_main_pane",
            type = StructureType.GRAPH,
            entities = listOf(entity)
        )

        assertFailsWith<IllegalArgumentException> {
            GraphSceneAdapter.adapt(subScene)
        }
    }

    @Test
    fun testVisualKeyInconsistentWithElementIdThrows() {
        val entity = VisualEntity(
            visualKey = "graph:vertex:B", // Inconsistent with vertex:A
            elementId = "vertex:A",
            dataValue = "A",
            slotKey = "graph_slot:A"
        )
        val subScene = StructureSubScene(
            subPaneId = "graph_main_pane",
            type = StructureType.GRAPH,
            entities = listOf(entity)
        )

        assertFailsWith<IllegalArgumentException> {
            GraphSceneAdapter.adapt(subScene)
        }
    }

    @Test
    fun testMalformedFromVisualKeyPrefixThrows() {
        val entityA = VisualEntity(
            visualKey = "graph:vertex:A",
            elementId = "vertex:A",
            dataValue = "A",
            slotKey = "graph_slot:A"
        )
        val entityB = VisualEntity(
            visualKey = "graph:vertex:B",
            elementId = "vertex:B",
            dataValue = "B",
            slotKey = "graph_slot:B"
        )
        val conn = VisualConnection(
            connectionKey = "edge:A->B",
            fromVisualKey = "malformed:A", // Malformed prefix
            toVisualKey = "graph:vertex:B",
            type = ConnectionType.GRAPH_EDGE
        )
        val subScene = StructureSubScene(
            subPaneId = "graph_main_pane",
            type = StructureType.GRAPH,
            entities = listOf(entityA, entityB),
            connections = listOf(conn)
        )

        assertFailsWith<IllegalArgumentException> {
            GraphSceneAdapter.adapt(subScene)
        }
    }

    @Test
    fun testMalformedToVisualKeyPrefixThrows() {
        val entityA = VisualEntity(
            visualKey = "graph:vertex:A",
            elementId = "vertex:A",
            dataValue = "A",
            slotKey = "graph_slot:A"
        )
        val entityB = VisualEntity(
            visualKey = "graph:vertex:B",
            elementId = "vertex:B",
            dataValue = "B",
            slotKey = "graph_slot:B"
        )
        val conn = VisualConnection(
            connectionKey = "edge:A->B",
            fromVisualKey = "graph:vertex:A",
            toVisualKey = "malformed:B", // Malformed prefix
            type = ConnectionType.GRAPH_EDGE
        )
        val subScene = StructureSubScene(
            subPaneId = "graph_main_pane",
            type = StructureType.GRAPH,
            entities = listOf(entityA, entityB),
            connections = listOf(conn)
        )

        assertFailsWith<IllegalArgumentException> {
            GraphSceneAdapter.adapt(subScene)
        }
    }

    @Test
    fun testDuplicateConnectionKeyThrows() {
        val entityA = VisualEntity(
            visualKey = "graph:vertex:A",
            elementId = "vertex:A",
            dataValue = "A",
            slotKey = "graph_slot:A"
        )
        val entityB = VisualEntity(
            visualKey = "graph:vertex:B",
            elementId = "vertex:B",
            dataValue = "B",
            slotKey = "graph_slot:B"
        )
        val conn1 = VisualConnection(
            connectionKey = "edge:A<->B",
            fromVisualKey = "graph:vertex:A",
            toVisualKey = "graph:vertex:B",
            type = ConnectionType.GRAPH_EDGE
        )
        val conn2 = VisualConnection(
            connectionKey = "edge:A<->B", // Duplicate key
            fromVisualKey = "graph:vertex:A",
            toVisualKey = "graph:vertex:B",
            type = ConnectionType.GRAPH_EDGE
        )
        val subScene = StructureSubScene(
            subPaneId = "graph_main_pane",
            type = StructureType.GRAPH,
            entities = listOf(entityA, entityB),
            connections = listOf(conn1, conn2)
        )

        assertFailsWith<IllegalArgumentException> {
            GraphSceneAdapter.adapt(subScene)
        }
    }

    @Test
    fun testInconsistentSlotKeyThrows() {
        val entity = VisualEntity(
            visualKey = "graph:vertex:A",
            elementId = "vertex:A",
            dataValue = "A",
            slotKey = "graph_slot:B" // Mismatch with A
        )
        val subScene = StructureSubScene(
            subPaneId = "graph_main_pane",
            type = StructureType.GRAPH,
            entities = listOf(entity)
        )

        assertFailsWith<IllegalArgumentException> {
            GraphSceneAdapter.adapt(subScene)
        }
    }

    @Test
    fun testDuplicateVertexKeyThrows() {
        val entity1 = VisualEntity(
            visualKey = "graph:vertex:A",
            elementId = "vertex:A",
            dataValue = "A",
            slotKey = "graph_slot:A"
        )
        val entity2 = VisualEntity(
            visualKey = "graph:vertex:A",
            elementId = "vertex:A",
            dataValue = "A",
            slotKey = "graph_slot:A"
        )
        val subScene = StructureSubScene(
            subPaneId = "graph_main_pane",
            type = StructureType.GRAPH,
            entities = listOf(entity1, entity2)
        )

        assertFailsWith<IllegalArgumentException> {
            GraphSceneAdapter.adapt(subScene)
        }
    }

    @Test
    fun testMissingEndpointIgnored() {
        val entityA = VisualEntity(
            visualKey = "graph:vertex:A",
            elementId = "vertex:A",
            dataValue = "A",
            slotKey = "graph_slot:A"
        )
        val connMissingTo = VisualConnection(
            connectionKey = "edge:A->X",
            fromVisualKey = "graph:vertex:A",
            toVisualKey = "graph:vertex:X", // Valid prefix but unknown endpoint
            type = ConnectionType.GRAPH_EDGE
        )
        val subScene = StructureSubScene(
            subPaneId = "graph_main_pane",
            type = StructureType.GRAPH,
            entities = listOf(entityA),
            connections = listOf(connMissingTo)
        )

        val result = GraphSceneAdapter.adapt(subScene)
        assertEquals(1, result.vertices.size)
        assertTrue(result.edges.isEmpty())
    }
}
