package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class Knapsack01DpSceneTest {
    @Test
    fun equalAndWorseCandidatesDoNotProduceUpdates() {
        val items = listOf(Knapsack01Item("first", 1, 3), Knapsack01Item("equal", 1, 3),
            Knapsack01Item("worse", 1, 2))
        val frames = Knapsack01DpSceneBuilder.build(Knapsack01DpTrace.createTrace(items, 1)).frames
        listOf(1, 2).forEach { itemIndex ->
            val frame = frames.single { it.step.action == Knapsack01Action.EVALUATE_ITEM_CAP && it.step.currentItemIndex == itemIndex }
            assertEquals(KnapsackDecision.KEEP_EXISTING, frame.decision)
            assertEquals(3, frame.storedAtDestination)
            assertTrue(frame.step.includeVal!! <= frame.step.excludeVal!!)
            assertTrue(frames.none { it.step.action == Knapsack01Action.UPDATE_DP && it.step.currentItemIndex == itemIndex })
        }
    }
    @Test
    fun zeroWeightUsesPreviousDestinationAndDuplicateOccurrencesRemainDistinct() {
        val scene = Knapsack01DpSceneBuilder.build(
            Knapsack01DpTrace.createTrace(listOf(Knapsack01Item("A", 0, 2), Knapsack01Item("A", 0, 2)), 0))
        val checks = scene.frames.filter { it.step.action == Knapsack01Action.EVALUATE_ITEM_CAP }
        assertEquals(listOf(0, 1), checks.map { it.step.currentItemIndex })
        assertEquals(listOf(0, 2), checks.map { it.sourceValueBeforeUpdate })
        assertTrue(checks.all { it.source == it.destination && it.destination == 0 })
        assertEquals(4, scene.frames.last().step.dpTable[0])
    }
    @Test
    fun evaluationAndUpdateKeepCandidateSeparateFromCommittedValue() {
        val trace = Knapsack01DpTrace.createTrace()
        val scene = Knapsack01DpSceneBuilder.build(trace)
        assertEquals(16, scene.frames.size)
        assertEquals(listOf(0, 0, 3, 4, 4, 7), scene.frames.last().step.dpTable)
        scene.frames.forEach { frame ->
            if (frame.step.action == Knapsack01Action.EVALUATE_ITEM_CAP || frame.step.action == Knapsack01Action.UPDATE_DP) {
                val item = frame.step.items[frame.step.currentItemIndex!!]
                assertEquals(frame.destination!! - item.weight, frame.source)
                assertEquals(frame.step.includeVal!! - item.value, frame.sourceValueBeforeUpdate)
                assertEquals(frame.step.dpTable[frame.destination], frame.storedAtDestination)
            } else assertNull(frame.source)
        }
        val example = scene.frames.first { it.step.action == Knapsack01Action.EVALUATE_ITEM_CAP && it.step.currentItemIndex == 1 && it.destination == 5 }
        assertEquals(7, example.bestCandidate)
        assertEquals(3, example.storedAtDestination)
        assertEquals(KnapsackDecision.INCLUDE_WINS, example.decision)
        assertTrue(scene.frames.last().destination == null)
        val layout = Knapsack01DpSceneLayout.calculate(scene, 360f, 224f)
        scene.frames.filter { it.source != null && it.destination != null }.forEach { frame ->
            val source = frame.source!!
            val destination = frame.destination!!
            val arrow = layout.dependencyArrow(source, destination)
            assertEquals(layout.cells[source].x - 2f, arrow.startX)
            assertEquals(layout.cells[destination].x - 2f, arrow.endX)
            assertEquals(layout.cells[source].y, arrow.sourceY)
            assertEquals(layout.cells[destination].y, arrow.destinationY)
            assertTrue(arrow.laneY in layout.arrowLane.y..layout.arrowLane.bottom)
            assertTrue(arrow.endX - 3f >= layout.tableRegion.x && arrow.endX + 3f <= layout.tableRegion.right)
            assertTrue(arrow.destinationY - 3f >= layout.tableRegion.y && arrow.destinationY <= layout.tableRegion.bottom)
        }
    }
}
