package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class QueueSceneBuilderTest {
    @Test
    fun mapsAllThirtyFiveQueueSnapshotsAndPreservesAllExplanations() {
        val all = QueueDemonstration.entries.flatMap { QueueTrace.stepsFor(it) }
        assertEquals(35, all.size)
        assertEquals(69, all.size + StackDemonstration.entries.sumOf { StackTrace.stepsFor(it).size })
        QueueDemonstration.entries.forEach { demo ->
            val steps = QueueTrace.stepsFor(demo)
            val scenes = QueueSceneBuilder.buildScenes(steps)
            assertEquals(steps.size, scenes.size)
            scenes.zip(steps).forEachIndexed { index, (scene, step) ->
                assertEquals(index, scene.stepIndex)
                assertEquals(step.explanation, scene.globalMessage)
                val array = scene.subScenes.first { it.subPaneId == "queue_array" }
                assertEquals(step.size, array.entities.size)
                assertEquals(step.size, array.entities.map { it.elementId }.distinct().size)
            }
        }
    }

    @Test
    fun duplicateValuesHaveDistinctStableIdsAcrossQueueAndPeek() {
        val steps = QueueTrace.generate(listOf(QueueOperation.Enqueue("X"), QueueOperation.Enqueue("X"), QueueOperation.Peek, QueueOperation.Dequeue), capacity = 3)
        val scenes = QueueSceneBuilder.buildScenes(steps)
        val beforePeek = scenes[2].subScenes.first().entities
        assertEquals(2, beforePeek.map { it.elementId }.distinct().size)
        assertNotEquals(beforePeek[0].elementId, beforePeek[1].elementId)
        assertEquals(beforePeek.map { it.elementId }, scenes[3].subScenes.first().entities.map { it.elementId })
    }

    @Test
    fun arrayAndLinkedRepresentationStaySynchronizedAndExposeNextLinks() {
        val steps = QueueTrace.stepsFor(QueueDemonstration.IMPLEMENTATIONS)
        val scenes = QueueSceneBuilder.buildScenes(steps)
        scenes.zip(steps).forEach { (scene, step) ->
            val array = scene.subScenes.first { it.subPaneId == "queue_array" }
            val linked = scene.subScenes.first { it.subPaneId == "queue_linked" }
            assertEquals(step.values.size, array.entities.size)
            assertEquals(step.values.size, linked.entities.size)
            assertEquals(array.entities.map { it.elementId }.toSet(), linked.entities.map { it.elementId }.toSet())
            assertEquals((step.values.size - 1).coerceAtLeast(0), linked.connections.size)
            assertTrue(linked.connections.all { it.type == ConnectionType.POINTER_LINK && it.directed })
        }
    }

    @Test
    fun enqueuePeekAndDequeueHighlightTheCorrectTraceSlots() {
        val steps = QueueTrace.stepsFor(QueueDemonstration.OPERATIONS)
        val scenes = QueueSceneBuilder.buildScenes(steps)
        assertEquals(EntityVisualState.ACTIVE, scenes[1].subScenes.first().entities.single().visualState)
        assertEquals(EntityVisualState.TARGET, scenes[3].subScenes.first().entities.first().visualState)
        assertEquals("7", steps[3].returnedValue)
        assertEquals(listOf("4"), scenes[4].subScenes.first().entities.map { it.dataValue })
        assertEquals("7", steps[4].returnedValue)
    }

    @Test
    fun circularBufferScenesRetainPhysicalWrapAndPointerStates() {
        val steps = QueueTrace.stepsFor(QueueDemonstration.CIRCULAR_BUFFER)
        val scenes = QueueSceneBuilder.buildScenes(steps)
        val final = steps.last()
        val entities = scenes.last().subScenes.first().entities
        assertEquals(listOf("C", "D", "E", "F"), final.values)
        assertEquals(listOf("E", "F", "C", "D"), final.slots)
        assertEquals(2, final.frontIndex)
        assertEquals(2, final.nextRearIndex)
        assertEquals(final.size, entities.size)
        assertTrue(entities.all { it.slotKey.startsWith("queue_array:") })
        assertEquals(mapOf("queue_array:2" to "C", "queue_array:3" to "D", "queue_array:0" to "E", "queue_array:1" to "F"), entities.associate { it.slotKey to it.dataValue })
    }
}
