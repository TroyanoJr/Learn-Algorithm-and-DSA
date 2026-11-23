package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MergeSortFundamentalsSceneBuilderTest {

    @Test
    fun sourceAndOutputSlotIdentitiesRemainStableAcrossEveryFrame() {
        val frames = build(listOf(1, 4, 7), listOf(2, 5, 8))
        val expectedSourceIds = setOf("left:0", "left:1", "left:2", "right:0", "right:1", "right:2")
        val expectedOutputIds = (0 until 6).map { "output_slot:$it" }.toSet()

        frames.forEach { frame ->
            val sourceIds = frame.scene.subScenes
                .filter { it.subPaneId != MergeSortFundamentalsSceneBuilder.OUTPUT_SCENE_ID }
                .flatMap { it.entities }
                .map { it.elementId }
                .toSet()
            val output = outputScene(frame)
            assertEquals(expectedSourceIds, sourceIds)
            assertEquals(expectedOutputIds, output.entities.map { it.elementId }.toSet())
            assertEquals(6, output.entities.size)
        }
    }

    @Test
    fun fixedOutputSlotsShowOnlyWrittenOccurrencesWithTheirSourceProvenance() {
        val frames = build(listOf(1, 4), listOf(2))
        frames.forEach { frame ->
            val output = outputScene(frame)
            val step = frame.step
            output.entities.forEachIndexed { outputIndex, entity ->
                val origin = step.outputOrigins.getOrNull(outputIndex)
                if (origin == null) {
                    assertTrue(entity.dataValue.isEmpty())
                    assertNull(entity.occurrenceId)
                    assertEquals(EntityVisualState.DEFAULT, entity.visualState)
                } else {
                    assertEquals(step.mergedResult[outputIndex].toString(), entity.dataValue)
                    assertEquals("output_occurrence:${origin.identity}", entity.occurrenceId)
                    assertEquals(MergeSortFundamentalsSceneBuilder.originLabel(origin), entity.badgeLabel)
                }
            }
        }
    }

    @Test
    fun comparisonAndTransferStatesAreDistinctAndTransfersKeepExactOrigin() {
        val frames = build(listOf(1, 2), listOf(1, 3))
        frames.forEach { frame ->
            val sourceEntities = sourceEntities(frame)
            val active = sourceEntities.filter { it.visualState == EntityVisualState.ACTIVE }
            if (frame.step.event == MergeStepEvent.COMPARE) {
                assertEquals(2, active.size)
                val comparison = assertNotNull(frame.step.comparison)
                assertTrue(active.any { it.elementId == "left:${comparison.leftIndex}" })
                assertTrue(active.any { it.elementId == "right:${comparison.rightIndex}" })
                assertNull(frame.step.selectedOrigin)
            } else {
                assertTrue(active.isEmpty(), "Only COMPARE frames can show active comparison entities")
            }

            if (frame.step.destinationIndex != null) {
                val origin = assertNotNull(frame.step.selectedOrigin)
                assertEquals(EntityVisualState.TARGET, sourceEntities.single { it.elementId == origin.identity }.visualState)
                val connection = outputScene(frame).referenceConnections.single {
                    it.visualState == ConnectionVisualState.HIGHLIGHTED
                }
                assertEquals(MergeSortFundamentalsSceneBuilder.sourceVisualKey(origin.source, origin.index), connection.fromVisualKey)
                assertEquals(MergeSortFundamentalsSceneBuilder.outputSlotKey(frame.step.destinationIndex), connection.toVisualKey)
                assertEquals(ConnectionType.POINTER_LINK, connection.type)
                assertTrue(connection.directed)
            } else {
                assertTrue(outputScene(frame).referenceConnections.none { it.visualState == ConnectionVisualState.HIGHLIGHTED })
            }
        }
    }

    @Test
    fun duplicateValuesHaveDistinctSourceAndOutputOccurrenceIdentities() {
        val frames = build(listOf(2, 2), listOf(2, 2))
        val terminal = frames.last()
        val sourceEntities = sourceEntities(terminal)
        assertEquals(4, sourceEntities.map { it.elementId }.distinct().size)
        val occurrences = outputScene(terminal).entities.mapNotNull { it.occurrenceId }
        assertEquals(
            listOf("output_occurrence:left:0", "output_occurrence:left:1", "output_occurrence:right:0", "output_occurrence:right:1"),
            occurrences
        )
        assertEquals(4, occurrences.distinct().size)
    }

    @Test
    fun emptyInputsAndSingletonsRetainSafeEmptyPresentation() {
        val empty = build(emptyList(), emptyList())
        empty.forEach { frame ->
            assertTrue(sourceEntities(frame).isEmpty())
            assertTrue(outputScene(frame).entities.isEmpty())
            assertTrue(frame.step.i == 0 && frame.step.j == 0 && frame.step.k == 0)
        }

        val onlyRight = build(emptyList(), listOf(Int.MIN_VALUE))
        onlyRight.forEach { frame ->
            assertEquals(1, outputScene(frame).entities.size)
            if (frame.step.mergedResult.isEmpty()) {
                assertTrue(outputScene(frame).entities.single().dataValue.isEmpty())
            }
        }
        val terminal = onlyRight.last()
        assertEquals(Int.MIN_VALUE.toString(), outputScene(terminal).entities.single().dataValue)
        assertEquals("output_occurrence:right:0", outputScene(terminal).entities.single().occurrenceId)
    }

    private fun build(left: List<Int>, right: List<Int>) =
        MergeSortFundamentalsSceneBuilder.build(MergeSortFundamentalsTrace.stepsForMerge(left, right))

    private fun outputScene(frame: MergeSortFundamentalsSceneFrame) =
        frame.scene.subScenes.single { it.subPaneId == MergeSortFundamentalsSceneBuilder.OUTPUT_SCENE_ID }

    private fun sourceEntities(frame: MergeSortFundamentalsSceneFrame) = frame.scene.subScenes
        .filter { it.subPaneId == MergeSortFundamentalsSceneBuilder.LEFT_SCENE_ID || it.subPaneId == MergeSortFundamentalsSceneBuilder.RIGHT_SCENE_ID }
        .flatMap { it.entities }
}
