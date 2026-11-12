package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class VisualizationSceneTest {

    @Test
    fun testEntitiesWithDuplicateDataValuesHaveDistinctElementIds() {
        val entityA = VisualEntity(
            visualKey = "array:elem:0",
            elementId = "elem:0",
            dataValue = "10",
            slotKey = "array_slot:0"
        )
        val entityB = VisualEntity(
            visualKey = "array:elem:2",
            elementId = "elem:2",
            dataValue = "10",
            slotKey = "array_slot:2"
        )

        // Same dataValue ("10"), but distinct elementIds
        assertEquals(entityA.dataValue, entityB.dataValue)
        assertNotEquals(entityA.elementId, entityB.elementId)
        assertNotEquals(entityA.visualKey, entityB.visualKey)
    }

    @Test
    fun testSameElementIdHasMultipleVisualKeysAcrossSubScenes() {
        val elementId = "node:N5"

        val treeRepresentation = VisualEntity(
            visualKey = "tree:node:N5",
            elementId = elementId,
            dataValue = "5",
            slotKey = "tree_slot:N5"
        )

        val queueRepresentation = VisualEntity(
            visualKey = "queue:node:N5",
            elementId = elementId,
            dataValue = "5",
            slotKey = "queue_slot:0"
        )

        // Shared elementId, distinct visualKeys per sub-scene
        assertEquals(treeRepresentation.elementId, queueRepresentation.elementId)
        assertNotEquals(treeRepresentation.visualKey, queueRepresentation.visualKey)
        assertNotEquals(treeRepresentation.slotKey, queueRepresentation.slotKey)
    }

    @Test
    fun testEphemeralOccurrencesOfSameElementCanBeDistinguished() {
        val occurrence1 = VisualEntity(
            visualKey = "pq:entry:C_dist4",
            elementId = "vertex:C",
            dataValue = "dist=4",
            slotKey = "pq_slot:2",
            occurrenceId = "pq_occurrence:C_step1"
        )

        val occurrence2 = VisualEntity(
            visualKey = "pq:entry:C_dist1",
            elementId = "vertex:C",
            dataValue = "dist=1",
            slotKey = "pq_slot:0",
            occurrenceId = "pq_occurrence:C_step3"
        )

        assertEquals(occurrence1.elementId, occurrence2.elementId)
        assertNotEquals(occurrence1.occurrenceId, occurrence2.occurrenceId)
        assertNotEquals(occurrence1.visualKey, occurrence2.visualKey)
    }

    @Test
    fun testSlotsDistinctFromElements() {
        // Element elem:0 sitting in slot 0, then swapping to slot 2
        val elementBeforeSwap = VisualEntity(
            visualKey = "array:elem:0",
            elementId = "elem:0",
            dataValue = "10",
            slotKey = "array_slot:0"
        )

        val elementAfterSwap = elementBeforeSwap.copy(
            slotKey = "array_slot:2"
        )

        assertEquals(elementBeforeSwap.elementId, elementAfterSwap.elementId)
        assertNotEquals(elementBeforeSwap.slotKey, elementAfterSwap.slotKey)
    }

    @Test
    fun testConnectionEndpointsIdentification() {
        val connection = VisualConnection(
            connectionKey = "branch:tree_slot:0->tree_slot:1",
            fromVisualKey = "tree:elem:0",
            toVisualKey = "tree:elem:1",
            label = "left_child",
            type = ConnectionType.STRUCTURAL_BRANCH
        )

        assertEquals("tree:elem:0", connection.fromVisualKey)
        assertEquals("tree:elem:1", connection.toVisualKey)
        assertEquals(ConnectionType.STRUCTURAL_BRANCH, connection.type)
    }
}
