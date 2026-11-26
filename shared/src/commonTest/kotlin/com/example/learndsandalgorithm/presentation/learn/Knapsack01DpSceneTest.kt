package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class Knapsack01DpSceneTest {
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
    }
}
