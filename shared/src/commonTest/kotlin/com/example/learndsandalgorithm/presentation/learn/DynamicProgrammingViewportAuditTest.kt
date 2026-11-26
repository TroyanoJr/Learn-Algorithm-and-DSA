package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DynamicProgrammingViewportAuditTest {
    @Test
    fun syntheticEffectiveCellWidthsImproveAgainstPreBatchGeometry() {
        val memo = DpMemoizationSceneLayout.calculate(DpMemoizationSceneBuilder.build(DpMemoizationTrace.createTrace()), 360f, 224f)
        val tab = DpTabulationSceneLayout.calculate(DpTabulationSceneBuilder.build(DpTabulationTrace.createTrace()), 360f, 224f)
        val kn = Knapsack01DpSceneLayout.calculate(Knapsack01DpSceneBuilder.build(Knapsack01DpTrace.createTrace()), 360f, 224f)
        val coin = CoinChangeDpSceneLayout.calculate(CoinChangeDpSceneBuilder.build(CoinChangeDpTrace.createTrace()), 360f)
        val lcs = LcsDpSceneLayout.calculate(LcsDpSceneBuilder.build(LcsDpTrace.createTrace()), 360f)
        val paths = UniquePathsDpSceneLayout.calculate(UniquePathsDpSceneBuilder.build(UniquePathsDpTrace.createTrace()), 360f)
        // Baseline dimensions are from the pre-batch layout formulas with their default synthetic metrics.
        val previous = listOf(Triple("memo", 40f, 199f), Triple("tab", 46f, 181f),
            Triple("knapsack", 48f, 234f), Triple("coin", 34f, 195f),
            Triple("lcs", 28f, 234f), Triple("paths", 32f, 168f))
        val current = listOf(memo.cells.first().width to memo.height, tab.cells.first().width to tab.height,
            kn.cells.first().width to kn.height, coin.cells.first().width to coin.height,
            lcs.cells.first().first().width to lcs.height, paths.cells.first().first().width to paths.height)
        val beforeFont = listOf(11f, 12f, 10f, 10f, 10f, 10f)
        val afterFont = listOf(12f, 13f, 11f, 11f, 11f, 12f)
        val beforeCellHeight = listOf(32f, 32f, 30f, 24f, 23f, 29f)
        val afterCellHeight = listOf(memo.cells.first().height, tab.cells.first().height,
            kn.cells.first().height, coin.cells.first().height, lcs.cells.first().first().height,
            paths.cells.first().first().height)
        previous.zip(current).forEachIndexed { i, (before, after) ->
            val oldScale = minOf(1f, 224f / before.third)
            val newScale = minOf(1f, 224f / after.second)
            val oldEffective = before.second * oldScale
            val newEffective = after.first * newScale
            println("synthetic ${before.first}: cell=$oldEffective→$newEffective scale=$oldScale→$newScale " +
                "cellHeight=${beforeCellHeight[i]}→${afterCellHeight[i]} effectiveHeight=${beforeCellHeight[i] * oldScale}→${afterCellHeight[i] * newScale} " +
                "font=${beforeFont[i]}→${afterFont[i]} effectiveFont=${beforeFont[i] * oldScale}→${afterFont[i] * newScale}")
            assertTrue(newEffective > oldEffective, "${before.first}: before=$oldEffective after=$newEffective")
        }
    }
    @Test
    fun reportSyntheticSceneDimensionsAndFitScales() {
        val memo = DpMemoizationSceneBuilder.build(DpMemoizationTrace.createTrace())
        val tab = DpTabulationSceneBuilder.build(DpTabulationTrace.createTrace())
        val knapsack = Knapsack01DpSceneBuilder.build(Knapsack01DpTrace.createTrace())
        val coin = CoinChangeDpSceneBuilder.build(CoinChangeDpTrace.createTrace())
        val lcs = LcsDpSceneBuilder.build(LcsDpTrace.createTrace())
        val paths = UniquePathsDpSceneBuilder.build(UniquePathsDpTrace.createTrace())
        listOf(360 to 224, 400 to 260, 460 to 320, 240 to 224).forEach { (w, h) ->
            val m = DpMemoizationSceneLayout.calculate(memo, w.toFloat(), h.toFloat())
            val t = DpTabulationSceneLayout.calculate(tab, w.toFloat(), h.toFloat())
            val k = Knapsack01DpSceneLayout.calculate(knapsack, w.toFloat(), h.toFloat())
            val c = CoinChangeDpSceneLayout.calculate(coin, w.toFloat())
            val l = LcsDpSceneLayout.calculate(lcs, w.toFloat())
            val p = UniquePathsDpSceneLayout.calculate(paths, w.toFloat())
            val scenes = listOf(Triple("memo", m.width to m.height, m.cells.first().width to m.cells.first().height),
                Triple("tabulation", t.width to t.height, t.cells.first().width to t.cells.first().height),
                Triple("knapsack", k.width to k.height, k.cells.first().width to k.cells.first().height),
                Triple("coin", c.width to c.height, c.cells.first().width to c.cells.first().height),
                Triple("lcs", l.width to l.height, l.cells.first().first().width to l.cells.first().first().height),
                Triple("paths", p.width to p.height, p.cells.first().first().width to p.cells.first().first().height))
            scenes.forEach { (name, scene, cell) ->
                val fit = minOf(w / scene.first.roundToInt().toFloat(), h / scene.second.roundToInt().toFloat(), 1f)
                println("synthetic viewport=${w}x$h $name scene=${scene.first}x${scene.second} fit=$fit cell=${cell.first}x${cell.second} effective=${cell.first * fit}x${cell.second * fit}")
                assertTrue(fit > 0f && fit <= 1f)
            }
        }
    }
    @Test
    fun defaultTracesPreserveAllSnapshotsAndResults() {
        val memo = DpMemoizationTrace.createTrace()
        val tab = DpTabulationTrace.createTrace()
        val knapsack = Knapsack01DpTrace.createTrace()
        val coin = CoinChangeDpTrace.createTrace()
        val lcs = LcsDpTrace.createTrace()
        val paths = UniquePathsDpTrace.createTrace()
        val counts = listOf(memo.size, tab.size, knapsack.size, coin.size, lcs.size, paths.size)
        assertEquals(listOf(15, 6, 16, 24, 22, 7), counts)
        assertEquals(90, counts.sum())
        assertEquals(5L, memo.last().result)
        assertEquals(listOf(1L, 1L, 2L, 3L, 5L, 8L), tab.last().dpTable)
        assertEquals(listOf(0, 0, 3, 4, 4, 7), knapsack.last().dpTable)
        assertEquals(listOf(0, 1, 1, 2, 2, 1, 2), coin.last().dpTable)
        assertEquals("ace", lcs.last().lcsString)
        assertEquals(3, lcs.last().dpMatrix.last().last())
        assertEquals(listOf(listOf(1L, 1L, 1L), listOf(1L, 2L, 3L), listOf(1L, 3L, 6L)), paths.last().dpGrid)
    }
    @Test
    fun defaultScenesFitAtSyntheticPaneSizesAndReset() {
        val memo = DpMemoizationSceneBuilder.build(DpMemoizationTrace.createTrace())
        val tab = DpTabulationSceneBuilder.build(DpTabulationTrace.createTrace())
        val knapsack = Knapsack01DpSceneBuilder.build(Knapsack01DpTrace.createTrace())
        val coin = CoinChangeDpSceneBuilder.build(CoinChangeDpTrace.createTrace())
        val lcs = LcsDpSceneBuilder.build(LcsDpTrace.createTrace())
        val paths = UniquePathsDpSceneBuilder.build(UniquePathsDpTrace.createTrace())
        assertEquals(listOf(15, 6, 16, 24, 22, 7),
            listOf(memo.frames.size, tab.frames.size, knapsack.frames.size, coin.frames.size, lcs.frames.size, paths.frames.size))
        listOf(360 to 224, 400 to 260, 460 to 320, 240 to 224).forEach { (w, h) ->
            val m = DpMemoizationSceneLayout.calculate(memo, w.toFloat(), h.toFloat())
            val t = DpTabulationSceneLayout.calculate(tab, w.toFloat(), h.toFloat())
            val k = Knapsack01DpSceneLayout.calculate(knapsack, w.toFloat(), h.toFloat())
            val c = CoinChangeDpSceneLayout.calculate(coin, w.toFloat())
            val l = LcsDpSceneLayout.calculate(lcs, w.toFloat())
            val p = UniquePathsDpSceneLayout.calculate(paths, w.toFloat())
            assertTrue(l.rowHeaders.all { it.x >= 0f && it.right <= l.width && it.bottom <= l.height })
            assertTrue(p.rowHeaders.all { it.x >= 0f && it.right <= p.width && it.bottom <= p.height })
            assertTrue(l.cells.all { row -> row.zipWithNext().all { (a, b) -> a.right < b.x } })
            assertTrue(p.cells.all { row -> row.zipWithNext().all { (a, b) -> a.right < b.x } })
            assertTrue(l.columnHeaders.all { it.x >= 0f && it.right <= l.width })
            assertTrue(p.columnHeaders.all { it.x >= 0f && it.right <= p.width })
            listOf(m.width to m.height, t.width to t.height, k.width to k.height,
                c.width to c.height, l.width to l.height, p.width to p.height).forEach { (width, height) ->
                val state = VisualizerCanvasState()
                state.updateViewportSize(IntSize(w, h))
                state.updateContentSize(IntSize(width.roundToInt(), height.roundToInt()))
                val fit = minOf(w / width.roundToInt().toFloat(), h / height.roundToInt().toFloat(), 1f)
                assertEquals(fit, state.scale)
                assertTrue(state.contentSize.width * state.scale <= w + 0.001f)
                assertTrue(state.contentSize.height * state.scale <= h + 0.001f)
                assertEquals(0f, state.translationX)
                val initialTop = state.initialY + state.translationY -
                    (state.contentSize.height * state.scale - state.contentSize.height) / 2f
                assertTrue(initialTop >= -0.001f && initialTop + state.contentSize.height * state.scale <= h + 0.001f)
                state.onTransform(1.5f, Offset(5f, 5f), Offset(w / 2f, h / 2f))
                assertTrue(state.scale > fit)
                state.reset()
                assertEquals(fit, state.scale)
                assertEquals(0f, state.translationX)
                assertEquals(0f, state.translationY)
                val resetTop = state.initialY + state.translationY -
                    (state.contentSize.height * state.scale - state.contentSize.height) / 2f
                assertTrue(resetTop >= -0.001f && resetTop + state.contentSize.height * state.scale <= h + 0.001f,
                    "viewport=${w}x$h scene=${state.contentSize} scale=${state.scale} resetTop=$resetTop")
            }
        }
    }
}
