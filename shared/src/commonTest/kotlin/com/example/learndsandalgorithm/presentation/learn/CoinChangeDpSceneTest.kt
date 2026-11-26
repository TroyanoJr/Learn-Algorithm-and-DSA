package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CoinChangeDpSceneTest {
    @Test
    fun unreachableTiedAndWorseCandidatesKeepTheirRealValues() {
        val unreachable = CoinChangeDpSceneBuilder.build(CoinChangeDpTrace.createTrace(3, listOf(2)))
        val impossible = unreachable.frames.first { it.step.action == CoinChangeDpAction.CHECK_COIN && it.destination == 3 }
        assertEquals(CoinChangeDpTrace.INF, impossible.candidate)
        assertEquals(false, impossible.improves)
        assertEquals(-1, unreachable.frames.last().step.candidateVal)
        assertNull(unreachable.frames.last().source)

        val tie = CoinChangeDpSceneBuilder.build(CoinChangeDpTrace.createTrace(2, listOf(1, 1)))
            .frames.first { it.step.action == CoinChangeDpAction.CHECK_COIN && it.step.currentAmount == 1 &&
                it.step.currentCoinIndex == 1 }
        assertEquals(tie.existingBefore, tie.candidate)
        assertEquals(false, tie.improves)

        val worse = CoinChangeDpSceneBuilder.build(CoinChangeDpTrace.createTrace(6, listOf(4, 3, 1)))
            .frames.first { it.step.action == CoinChangeDpAction.CHECK_COIN && it.step.currentAmount == 6 &&
                it.step.currentCoin == 1 }
        assertTrue(worse.candidate!! > worse.existingBefore!!)
        assertEquals(false, worse.improves)
    }
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
        scene.frames.filter { it.source != null && it.destination != null }.forEach { frame ->
            val source = frame.source!!
            val destination = frame.destination!!
            val arrow = layout.dependencyArrow(source, destination)
            assertEquals(layout.cells[source].x - 2f, arrow.startX)
            assertEquals(layout.cells[destination].x - 2f, arrow.endX)
            assertEquals(layout.cells[source].y, arrow.sourceY)
            assertEquals(layout.cells[destination].y, arrow.destinationY)
            assertTrue(arrow.laneY in layout.arrowLane.y..layout.arrowLane.bottom)
            assertTrue(arrow.endX - 3f >= layout.table.x && arrow.endX + 3f <= layout.table.right)
            assertTrue(arrow.destinationY - 3f >= layout.table.y && arrow.destinationY <= layout.table.bottom)
        }
        val duplicateChecks = CoinChangeDpTrace.createTrace(2, listOf(1, 1))
            .filter { it.action == CoinChangeDpAction.CHECK_COIN && it.currentAmount == 1 }
        assertEquals(listOf(0, 1), duplicateChecks.map { it.currentCoinIndex })
    }
}
