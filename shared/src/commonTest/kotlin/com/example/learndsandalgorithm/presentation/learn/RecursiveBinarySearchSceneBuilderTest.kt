package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RecursiveBinarySearchSceneBuilderTest {

    @Test
    fun allArrayCellsAndTheirOriginalIdentitiesStayStableAcrossEveryFrame() {
        val trace = RecursiveBinarySearchTrace.stepsForSearch()
        val frames = RecursiveBinarySearchSceneBuilder.build(trace)

        assertEquals(trace.size, frames.size)
        frames.forEachIndexed { index, frame ->
            val arrayScene = frame.scene.subScenes.single()
            assertEquals(StructureType.ARRAY, arrayScene.type)
            assertEquals(index, frame.scene.stepIndex)
            assertEquals(trace[index].explanation, frame.scene.globalMessage)
            assertEquals(index, frame.snapshotIndex)
            assertEquals(trace.size, frame.snapshotCount)
            assertEquals(trace[index].isTerminal, frame.isCompleted)
            assertEquals(trace[index].array.size, arrayScene.entities.size)
            assertEquals(trace[index].array, arrayScene.entities.sortedBy { it.slotKey.substringAfterLast(':').toInt() }.map { it.dataValue.toInt() })
            assertEquals((0 until trace[index].array.size).map { "array:item:$it" }, arrayScene.entities.map { it.elementId })
            assertEquals((0 until trace[index].array.size).map { "recursive_binary_search:index:$it" }, arrayScene.entities.map { it.visualKey })
            arrayScene.entities.forEachIndexed { itemIndex, entity ->
                assertEquals("binary_search:array_slot:$itemIndex", entity.slotKey)
                assertEquals(itemIndex.toString(), entity.badgeLabel)
            }
        }

        frames.flatMap { it.scene.subScenes.single().entities }
            .groupBy { it.elementId }
            .forEach { (id, appearances) ->
                assertTrue(appearances.all { it.visualKey == appearances.first().visualKey })
                assertTrue(appearances.all { it.slotKey == appearances.first().slotKey })
                assertEquals(1, appearances.map { it.dataValue }.distinct().size)
            }
    }

    @Test
    fun activeExcludedMidpointAndMatchStatesFollowTraceMetadata() {
        val frames = RecursiveBinarySearchSceneBuilder.build(RecursiveBinarySearchTrace.stepsForSearch())
        val start = frames.first()
        assertEquals(0..4, start.meta.activeInterval)
        assertEquals(emptySet(), start.meta.excludedIndices)
        assertEquals(listOf("L"), start.meta.markerLabels.getValue(0))
        assertEquals(listOf("R"), start.meta.markerLabels.getValue(4))
        assertTrue(start.scene.subScenes.single().entities.all { it.visualState == EntityVisualState.DEFAULT })

        val compare = frames.first { it.meta.event == BinarySearchEvent.COMPARE_TARGET }
        assertEquals(2, compare.meta.midIndex)
        assertEquals(8, compare.meta.midValue)
        assertEquals(BinarySearchComparison.LESS, compare.meta.comparison)
        assertEquals(EntityVisualState.ACTIVE, entityAt(compare, 2).visualState)

        val recurse = frames.single { it.meta.event == BinarySearchEvent.RECURSE_RIGHT }
        assertEquals(0..4, recurse.meta.activeInterval)
        assertEquals(2, recurse.meta.midIndex)
        assertEquals(3 to 4, recurse.meta.calleeCall?.left to recurse.meta.calleeCall?.right)

        val childCall = frames.single { it.meta.event == BinarySearchEvent.CALL_ENTER && it.meta.callDepth == 1 }
        assertEquals(3..4, childCall.meta.activeInterval)
        assertEquals(setOf(0, 1, 2), childCall.meta.excludedIndices)
        assertTrue((0..2).all { entityAt(childCall, it).visualState == EntityVisualState.VIOLATION })
        assertTrue((3..4).all { entityAt(childCall, it).visualState == EntityVisualState.DEFAULT })
        assertEquals(listOf("L"), childCall.meta.markerLabels.getValue(3))
        assertEquals(listOf("R"), childCall.meta.markerLabels.getValue(4))

        val found = frames.single { it.meta.event == BinarySearchEvent.TARGET_FOUND }
        assertEquals(3, found.meta.foundIndex)
        assertEquals(EntityVisualState.TARGET, entityAt(found, 3).visualState)
        val returnedToCaller = frames.first { it.meta.event == BinarySearchEvent.RETURN }
        assertEquals(3, returnedToCaller.meta.returnValue)
        assertEquals(0, returnedToCaller.meta.activeCallId)
        assertEquals(listOf("MID"), returnedToCaller.meta.markerLabels.getValue(2))

        val final = frames.last()
        assertTrue(final.meta.isTerminal)
        assertEquals(0, final.meta.stackDepth)
        assertNull(final.meta.activeInterval)
        assertEquals(EntityVisualState.TARGET, entityAt(final, 3).visualState)
        assertTrue(final.scene.subScenes.single().entities.filter { it.elementId != "array:item:3" }
            .all { it.visualState == EntityVisualState.VIOLATION })
    }

    @Test
    fun returnResultsAndStackMetadataAppearOnlyWhenTheCallReturns() {
        val steps = RecursiveBinarySearchTrace.stepsForSearch(target = 12)
        val frames = RecursiveBinarySearchSceneBuilder.build(steps)

        assertTrue(frames.filter { it.meta.event != BinarySearchEvent.RETURN }.all { it.meta.returnValue == null })
        assertEquals(2, frames.first().meta.maximumStackDepth)
        val enterFrames = frames.filter { it.meta.event == BinarySearchEvent.CALL_ENTER }
        assertEquals(listOf(0, 1), enterFrames.map { it.meta.callStack.last().depth })
        val returns = frames.filter { it.meta.event == BinarySearchEvent.RETURN }
        assertEquals(listOf(1, 0), returns.map { it.meta.stackDepth })
        assertEquals(listOf(3, 3), returns.map { it.meta.returnValue })
        assertEquals(1, returns.first().meta.callStack.size)
        assertEquals(0, returns.first().meta.callStack.single().callId)
        assertTrue(returns.last().meta.callStack.isEmpty())

        val missing = RecursiveBinarySearchSceneBuilder.build(RecursiveBinarySearchTrace.stepsForSearch(target = 10))
        val baseCase = missing.single { it.meta.event == BinarySearchEvent.TARGET_NOT_FOUND }
        assertTrue(baseCase.meta.isEmptyRangeBaseCase)
        assertNull(baseCase.meta.returnValue)
        assertTrue(missing.filter { it.meta.event != BinarySearchEvent.RETURN }.all { it.meta.returnValue == null })
        assertEquals(listOf(-1, -1, -1), missing.filter { it.meta.event == BinarySearchEvent.RETURN }.map { it.meta.returnValue })
        assertEquals("FINAL RESULT · -1", RecursiveBinarySearchSceneLayout.resultText(missing.last().meta))
    }

    @Test
    fun coincidentMarkersAndEmptyArraysKeepTheirSemanticStates() {
        val singleton = RecursiveBinarySearchSceneBuilder.build(
            RecursiveBinarySearchTrace.stepsForSearch(listOf(-5), target = -5)
        )
        val singletonMid = singleton.single { it.meta.event == BinarySearchEvent.CALCULATE_MID }
        assertEquals(listOf("L", "MID", "R"), singletonMid.meta.markerLabels.getValue(0))
        assertEquals(1, singletonMid.scene.subScenes.single().entities.size)

        val empty = RecursiveBinarySearchSceneBuilder.build(RecursiveBinarySearchTrace.stepsForSearch(emptyList()))
        assertTrue(empty.isNotEmpty())
        assertTrue(empty.all { it.scene.subScenes.single().entities.isEmpty() })
        assertTrue(empty.all { it.meta.markerLabels.isEmpty() })
        assertTrue(empty.last().meta.isTerminal)
        assertEquals(-1, empty.last().meta.returnValue)
    }

    private fun entityAt(frame: RecursiveBinarySearchSceneFrame, index: Int) =
        frame.scene.subScenes.single().entities.single { it.elementId == "array:item:$index" }
}
