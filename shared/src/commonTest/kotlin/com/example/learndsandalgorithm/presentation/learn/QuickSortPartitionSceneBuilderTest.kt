package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class QuickSortPartitionSceneBuilderTest {

    @Test
    fun everyOriginalLomutoTraceStepHasACompleteSceneAndPreservedExplanation() {
        val steps = QuickSortPartitionTrace.stepsForPartition()
        val frames = QuickSortPartitionSceneBuilder.build(steps)

        assertEquals(10, steps.size)
        assertEquals(steps.size, frames.size)
        assertEquals(
            listOf(
                QuickPartitionEvent.START,
                QuickPartitionEvent.SELECT_PIVOT,
                QuickPartitionEvent.SCAN_COMPARE,
                QuickPartitionEvent.SCAN_COMPARE,
                QuickPartitionEvent.SCAN_COMPARE,
                QuickPartitionEvent.SCAN_COMPARE,
                QuickPartitionEvent.SCAN_COMPARE,
                QuickPartitionEvent.SWAP_ELEMENTS,
                QuickPartitionEvent.PLACE_PIVOT,
                QuickPartitionEvent.PARTITION_COMPLETE
            ),
            frames.map { it.meta.event }
        )
        frames.forEachIndexed { index, frame ->
            assertEquals(index, frame.scene.stepIndex)
            assertEquals("quick_sort_partition_step_$index", frame.scene.sceneId)
            assertEquals(steps[index].explanation, frame.meta.explanation)
            assertEquals(steps[index].badgeText, frame.meta.badgeText)
            assertEquals(steps[index].array, frame.meta.array)
            assertEquals(steps[index].explanation, frame.scene.globalMessage)
            val arrayScene = frame.scene.subScenes.single { it.subPaneId == QuickSortPartitionSceneBuilder.ARRAY_SCENE_ID }
            assertEquals(steps[index].array.size, arrayScene.entities.size)
            assertEquals(steps[index].array, arrayScene.entities.sortedBy { it.slotKey.substringAfterLast(':').toInt() }.map { it.dataValue.toInt() })
        }
    }

    @Test
    fun scanComparisonsAndSwapFramesRetainTheirCorrectComparisonOperands() {
        val frames = QuickSortPartitionSceneBuilder.build(QuickSortPartitionTrace.stepsForPartition())
        val compareFrames = frames.filter { it.meta.event == QuickPartitionEvent.SCAN_COMPARE }

        assertEquals(5, compareFrames.size)
        compareFrames.forEach { frame ->
            val meta = frame.meta
            assertEquals(meta.array[meta.j], meta.comparisonValue)
            assertEquals(meta.j, meta.comparisonValueIndex)
            assertEquals(meta.array[meta.j] <= meta.pivotValue, meta.comparisonPassed)
            if (meta.badgeText.startsWith("COMPARE")) assertEquals(meta.j, meta.activeComparisonIndex)
            else {
                assertEquals(null, meta.activeComparisonIndex)
                assertTrue(meta.comparisonPassed == true)
            }
        }

        val swapFrame = frames.single { it.meta.event == QuickPartitionEvent.SWAP_ELEMENTS }
        val (destination, source) = swapFrame.meta.swappedIndices!!
        assertEquals(swapFrame.meta.j, source)
        assertEquals(swapFrame.meta.i, destination)
        assertEquals(swapFrame.meta.array[destination], swapFrame.meta.comparisonValue)
        assertTrue(swapFrame.meta.comparisonPassed == true)
        assertEquals(null, swapFrame.meta.activeComparisonIndex)
    }

    @Test
    fun elementIdentitiesStayStableWhileOccurrencesMoveBetweenSlots() {
        val frames = QuickSortPartitionSceneBuilder.build(QuickSortPartitionTrace.stepsForPartition())
        val identitySets = frames.map { frame ->
            frame.scene.subScenes.single { it.subPaneId == QuickSortPartitionSceneBuilder.ARRAY_SCENE_ID }
                .entities.map { it.elementId }.toSet()
        }
        assertTrue(identitySets.all { it == identitySets.first() })
        assertEquals(6, identitySets.first().size)

        val swapIndex = frames.indexOfFirst { it.meta.event == QuickPartitionEvent.SWAP_ELEMENTS }
        listOf(frames[swapIndex - 1], frames[swapIndex]).zipWithNext().forEach { (before, after) ->
            val beforeIds = before.scene.subScenes.single { it.subPaneId == QuickSortPartitionSceneBuilder.ARRAY_SCENE_ID }
                .entities.sortedBy { it.slotKey.substringAfterLast(':').toInt() }.map { it.elementId }
            val afterIds = after.scene.subScenes.single { it.subPaneId == QuickSortPartitionSceneBuilder.ARRAY_SCENE_ID }
                .entities.sortedBy { it.slotKey.substringAfterLast(':').toInt() }.map { it.elementId }
            val (a, b) = after.meta.swappedIndices!!
            val expected = beforeIds.toMutableList().apply { this[a] = beforeIds[b]; this[b] = beforeIds[a] }
            assertEquals(expected, afterIds)
        }
    }

    @Test
    fun pointerConnectionsTargetTheElementCurrentlyAtEachPointerSlot() {
        val frames = QuickSortPartitionSceneBuilder.build(QuickSortPartitionTrace.stepsForPartition())
        frames.forEach { frame ->
            val pointerScene = frame.scene.subScenes.single { it.subPaneId == QuickSortPartitionSceneBuilder.POINTER_SCENE_ID }
            val elementByVisualKey = frame.scene.subScenes
                .single { it.subPaneId == QuickSortPartitionSceneBuilder.ARRAY_SCENE_ID }
                .entities.associateBy { it.visualKey }
            pointerScene.connections.forEach { connection ->
                val element = elementByVisualKey[connection.toVisualKey]
                assertNotNull(element)
                val expectedIdentity = if (connection.label == "i") {
                    frame.meta.elementIdentityByIndex[frame.meta.i]
                } else {
                    frame.meta.elementIdentityByIndex[frame.meta.j]
                }
                assertEquals(expectedIdentity, element.elementId)
            }
            assertTrue(pointerScene.referenceConnections.containsAll(pointerScene.connections))
            assertEquals(setOf("pointer:i", "pointer:j"), pointerScene.entities.map { it.elementId }.toSet())
        }
    }

    @Test
    fun terminalScenePreservesTheLomutoPartitionAndFinalPivotIdentity() {
        val terminal = QuickSortPartitionSceneBuilder.build(QuickSortPartitionTrace.stepsForPartition()).last()
        assertTrue(terminal.meta.isTerminal)
        assertEquals(QuickPartitionEvent.PARTITION_COMPLETE, terminal.meta.event)
        assertEquals(listOf(1, 5, 8, 9, 10, 7), terminal.meta.array)
        assertEquals(1, terminal.meta.finalPivotPosition)
        assertEquals(1, terminal.meta.pivotPosition)
        assertEquals("item:5", terminal.meta.pivotIdentity)
        assertEquals("item:5", terminal.meta.elementIdentityByIndex[1])
        assertTrue(terminal.meta.array.take(1).all { it <= terminal.meta.pivotValue })
        assertTrue(terminal.meta.array.drop(2).all { it > terminal.meta.pivotValue })
        assertFalse(terminal.meta.event == QuickPartitionEvent.SORT_COMPLETE)
    }

    @Test
    fun duplicateOccurrencesKeepDistinctIdentitiesAndExposeUnstablePartitionMovement() {
        val frames = QuickSortPartitionSceneBuilder.build(
            QuickSortPartitionTrace.stepsForPartition(listOf(5, 5, 1, 0, 3))
        )
        val terminal = frames.last()
        assertEquals(listOf(1, 0, 3, 5, 5), terminal.meta.array)
        val equalFiveIdentities = terminal.meta.array.indices
            .filter { terminal.meta.array[it] == 5 }
            .map { terminal.meta.elementIdentityByIndex[it] }
        assertEquals(listOf("item:1", "item:0"), equalFiveIdentities)
        assertEquals(5, frames.first().scene.subScenes.single { it.subPaneId == QuickSortPartitionSceneBuilder.ARRAY_SCENE_ID }.entities.size)
    }

    @Test
    fun singleElementPartitionHasNoInventedPointerCell() {
        val frames = QuickSortPartitionSceneBuilder.build(QuickSortPartitionTrace.stepsForPartition(listOf(-4)))
        assertEquals(4, frames.size)
        frames.forEach { frame ->
            assertEquals(listOf(-4), frame.meta.array)
            assertEquals(listOf("array_slot:0"), frame.scene.subScenes.single { it.subPaneId == QuickSortPartitionSceneBuilder.ARRAY_SCENE_ID }.entities.map { it.slotKey })
            assertTrue(frame.scene.subScenes.single { it.subPaneId == QuickSortPartitionSceneBuilder.POINTER_SCENE_ID }.entities.any { it.dataValue == "i = -1" })
        }
        assertEquals(0, frames.last().meta.finalPivotPosition)
        assertTrue(frames.last().meta.isTerminal)
    }
}
