package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class StackSceneBuilderTest {
    @Test
    fun mapsAllThirtyFourSnapshotsWithoutDroppingExplanations() {
        val all = StackDemonstration.entries.flatMap { StackTrace.stepsFor(it) }
        assertEquals(34, all.size)
        StackDemonstration.entries.forEach { demo ->
            val steps = StackTrace.stepsFor(demo)
            val scenes = StackSceneBuilder.buildScenes(steps)
            assertEquals(steps.size, scenes.size)
            scenes.zip(steps).forEachIndexed { index, (scene, step) ->
                assertEquals(index, scene.stepIndex)
                assertEquals(step.explanation, scene.globalMessage)
                assertEquals(step.values.size, scene.subScenes.first().entities.size)
                assertEquals(step.values.size, scene.subScenes.first().entities.map { it.elementId }.distinct().size)
            }
        }
    }

    @Test
    fun duplicateValuesKeepDistinctStableOccurrenceIdentities() {
        val steps = StackTrace.generate(listOf(StackOperation.Push("X"), StackOperation.Push("X"), StackOperation.Peek, StackOperation.Pop))
        val scenes = StackSceneBuilder.buildScenes(steps)
        val afterPush = scenes[2].subScenes.first().entities
        assertEquals(2, afterPush.map { it.elementId }.distinct().size)
        assertNotEquals(afterPush[0].elementId, afterPush[1].elementId)
        val peekIds = scenes[3].subScenes.first().entities.map { it.elementId }
        assertEquals(afterPush.map { it.elementId }, peekIds)
        assertEquals(afterPush.first().elementId, scenes[4].subScenes.first().entities.single().elementId)
    }

    @Test
    fun pushPeekPopAndDelimiterStatesRemainTraceAligned() {
        val steps = StackTrace.stepsFor(StackDemonstration.OPERATIONS)
        val scenes = StackSceneBuilder.buildScenes(steps)
        val pushed = scenes[2].subScenes.first().entities.last()
        assertEquals(EntityVisualState.ACTIVE, pushed.visualState)
        assertEquals(EntityVisualState.TARGET, scenes[3].subScenes.first().entities.last().visualState)
        assertEquals(listOf("7"), scenes[4].subScenes.first().entities.map { it.dataValue })
        val underflow = StackSceneBuilder.buildScenes(StackTrace.generate(listOf(StackOperation.Pop))).drop(1).first()
        assertTrue(underflow.subScenes.first().entities.isEmpty())
    }

    @Test
    fun linkedRepresentationHasHeadOrderedNextLinksAndExplicitNullEndpoint() {
        val steps = StackTrace.stepsFor(StackDemonstration.IMPLEMENTATIONS)
        val scenes = StackSceneBuilder.buildScenes(steps)
        scenes.zip(steps).forEach { (scene, step) ->
            val linked = scene.subScenes.first { it.subPaneId == "stack_linked" }
            assertEquals(step.values.size, linked.entities.size)
            assertEquals((step.values.size - 1).coerceAtLeast(0), linked.connections.size)
            assertTrue(linked.connections.all { it.type == ConnectionType.POINTER_LINK && it.directed })
            val entityKeys = linked.entities.map { it.visualKey }.toSet()
            assertTrue(linked.connections.all { it.fromVisualKey in entityKeys && it.toVisualKey in entityKeys })
            if (step.values.isNotEmpty()) assertEquals(step.values.last(), linked.entities.first().dataValue)
        }
    }

    @Test
    fun delimiterTransitionsRetainMatchAndInvalidStates() {
        val balanced = StackSceneBuilder.buildScenes(StackTrace.stepsFor(StackDemonstration.DELIMITER_MATCHING))
        assertEquals(8, balanced.size)
        assertEquals("Delimiter validation: } matches the most recent opener {; pop that opener.", balanced[4].globalMessage)
        val invalidSteps = StackTrace.generate(listOf(StackOperation.OpenDelimiter("["), StackOperation.CloseDelimiter(")")))
        val invalidScenes = StackSceneBuilder.buildScenes(invalidSteps)
        assertTrue(invalidScenes.any { it.globalMessage?.contains("does not match") == true })
    }
}
