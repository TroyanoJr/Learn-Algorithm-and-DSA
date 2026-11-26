package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class DpMemoizationSceneTest {
    @Test
    fun framesPreserveStackArgumentsAndCacheResults() {
        val trace = DpMemoizationTrace.createTrace()
        val scene = DpMemoizationSceneBuilder.build(trace)
        assertEquals(trace, scene.frames.map { it.step })
        scene.frames.forEach { frame ->
            assertEquals(frame.step.callStack, frame.calls.map { it.argument })
            assertEquals(frame.calls.indices.toList(), frame.calls.map { it.depth })
            if (frame.step.cacheHit) assertTrue(frame.step.memoArray[frame.step.currentN!!] != null)
        }
        assertEquals(5L, scene.frames.last().step.result)
        assertTrue(scene.frames.last().calls.isEmpty())
        val distinctCalls = scene.frames.flatMap { it.calls }.distinctBy { it.id }
        assertTrue(distinctCalls.map { it.argument }.distinct().size < distinctCalls.size)
        val callsToTwo = distinctCalls.filter { it.argument == 2 }
        assertTrue(callsToTwo.size >= 2)
        assertNotEquals(callsToTwo.first().id, callsToTwo.last().id)
    }

    @Test
    fun layoutReservesEveryTableCellAndMaximumStackAcrossViewports() {
        val scene = DpMemoizationSceneBuilder.build(DpMemoizationTrace.createTrace())
        listOf(360f to 224f, 400f to 260f, 460f to 320f, 240f to 224f).forEach { (width, height) ->
            val layout = DpMemoizationSceneLayout.calculate(scene, width, height)
            assertEquals(6, layout.cells.size)
            assertEquals(scene.maxDepth, layout.callSlots.size)
            assertTrue(layout.table.bottom < layout.stackRegion.y)
            assertTrue(layout.stackRegion.bottom <= layout.height)
            layout.cells.forEachIndexed { index, cell ->
                assertTrue(cell.x >= layout.table.x && cell.right <= layout.table.right && cell.bottom <= layout.table.bottom)
                layout.cells.drop(index + 1).forEach { other -> assertFalse(cell.x < other.right && cell.right > other.x && cell.y < other.bottom && cell.bottom > other.y) }
            }
            scene.frames.forEach { frame -> assertTrue(frame.calls.size <= layout.callSlots.size) }
        }
    }

    @Test
    fun detailsFitReportsExactDeficitInsteadOfTreatingPositiveHeightAsFit() {
        val fits = DpDetailsFit(operationHeight = 55f, stepsHeight = 90f, spacing = 6f, available = 151f)
        assertTrue(fits.fits)
        assertEquals(0f, fits.deficit)
        val insufficient = fits.copy(available = 120f)
        assertFalse(insufficient.fits)
        assertEquals(31f, insufficient.deficit)
    }
}
