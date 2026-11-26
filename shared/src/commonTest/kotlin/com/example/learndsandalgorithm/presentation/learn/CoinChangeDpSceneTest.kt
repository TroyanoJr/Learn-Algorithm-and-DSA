package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CoinChangeDpSceneTest {
    @Test
    fun preservesActualPreUpdateValuesAndDependencies() {
        val steps = CoinChangeDpTrace.createTrace()
        val scene = CoinChangeDpSceneBuilder.build(steps)
        assertEquals(steps.size, scene.frames.size)
        assertEquals(listOf(0, 1, 1, 2, 2, 1, 2), scene.frames.last().step.dpTable)
        scene.frames.forEachIndexed { i, frame ->
            if (frame.destination != null) {
                assertEquals(frame.destination - frame.step.currentCoin!!, frame.source)
                val previous = if (frame.step.action == CoinChangeDpAction.UPDATE_DP) steps[i - 1] else steps[i]
                assertEquals(previous.dpTable[frame.destination], frame.existingBefore)
                assertEquals(frame.candidate != CoinChangeDpTrace.INF && frame.candidate!! < frame.existingBefore!!, frame.improves)
            } else assertNull(frame.source)
        }
        val layout = CoinChangeDpSceneLayout.calculate(scene, 360f)
        assertEquals(7, layout.cells.size)
        assertEquals(3, layout.coins.size)
        assertTrue(layout.cells.all { it.x >= 0 && it.right <= layout.width && it.bottom <= layout.height })
        assertTrue(layout.summary.all { it.bottom <= layout.height })
        assertTrue(layout.cells.zipWithNext().all { (a, b) -> a.right <= b.x })
        val duplicateChecks = CoinChangeDpTrace.createTrace(2, listOf(1, 1))
            .filter { it.action == CoinChangeDpAction.CHECK_COIN && it.currentAmount == 1 }
        assertEquals(listOf(0, 1), duplicateChecks.map { it.currentCoinIndex })
    }
}
