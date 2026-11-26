package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class DpMemoizationSceneTest {
    @Test
    fun cellStatesDistinguishBaseCasesStoresAndCacheHits() {
        for (n in 0..1) {
            val trace = DpMemoizationTrace.createTrace(n)
            assertEquals(MemoCellState.EMPTY, memoCellState(trace.last(), n))
            assertEquals(MemoCellState.ACTIVE, memoCellState(trace.first { it.action == MemoAction.CALL_SUBPROBLEM }, n))
        }
        val trace = DpMemoizationTrace.createTrace()
        val store = trace.first { it.action == MemoAction.COMPUTE_AND_STORE }
        val hit = trace.first { it.action == MemoAction.CACHE_HIT }
        assertEquals(MemoCellState.SUCCESS, memoCellState(store, store.currentN!!))
        assertEquals(MemoCellState.SUCCESS, memoCellState(hit, hit.currentN!!))
        assertEquals(MemoCellState.SUCCESS, memoCellState(trace.last(), 5))
        assertEquals(MemoCellState.CACHED, memoCellState(trace.last(), 2))
    }
    @Test
    fun cacheStoresAndPersistentStackPrefixesReflectTrace() {
        val trace = DpMemoizationTrace.createTrace()
        val frames = DpMemoizationSceneBuilder.build(trace).frames
        assertEquals(listOf(2, 3), trace.filter { it.action == MemoAction.CACHE_HIT }.map { it.currentN })
        frames.zipWithNext().forEach { (before, after) ->
            val common = before.calls.map { it.argument }.zip(after.calls.map { it.argument })
                .takeWhile { (a, b) -> a == b }.size
            assertEquals(before.calls.take(common).map { it.id }, after.calls.take(common).map { it.id })
        }
        trace.zipWithNext().forEach { (before, after) ->
            val changed = before.memoArray.indices.filter { before.memoArray[it] != after.memoArray[it] }
            if (after.action == MemoAction.COMPUTE_AND_STORE) {
                val n = after.currentN!!
                assertEquals(listOf(n), changed)
                fun fibValue(i: Int): Long = if (i <= 1) i.toLong() else after.memoArray[i]!!
                assertEquals(fibValue(n - 1) + fibValue(n - 2), after.memoArray[n])
            } else assertTrue(changed.isEmpty())
        }
    }
    @Test
    fun enlargedMeasuredLabelsRemainInsideTheirRegions() {
        val scene = DpMemoizationSceneBuilder.build(DpMemoizationTrace.createTrace())
        val metrics = MemoLayoutMetrics(headingWidth = 240f, badgeWidth = 95f, valueWidth = 48f,
            valueHeight = 20f, indexHeight = 18f, stackHeadingWidth = 230f, callWidth = 105f,
            callHeight = 24f, emptyWidth = 120f)
        val layout = DpMemoizationSceneLayout.calculate(scene, 240f, 224f, metrics)
        assertTrue(layout.tableHeading.right <= layout.cacheBadge.x)
        assertTrue(layout.cells.all { it.width >= metrics.valueWidth && it.height >= metrics.valueHeight })
        assertTrue(layout.callSlots.all { it.width >= metrics.callWidth && it.height >= metrics.callHeight &&
            it.x >= layout.stackRegion.x && it.right <= layout.stackRegion.right && it.bottom <= layout.stackRegion.bottom })
        assertTrue(layout.empty.right <= layout.stackRegion.right)
    }
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
