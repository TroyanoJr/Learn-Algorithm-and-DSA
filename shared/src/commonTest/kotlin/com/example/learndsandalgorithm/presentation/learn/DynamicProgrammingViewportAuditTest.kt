package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DynamicProgrammingViewportAuditTest {
    @Test
    fun defaultScenesFitAtSyntheticPaneSizesAndReset() {
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
                assertEquals(0f, state.translationY)
                state.onTransform(1.5f, Offset(5f, 5f), Offset(w / 2f, h / 2f))
                state.reset()
                assertEquals(fit, state.scale)
                assertEquals(0f, state.translationX)
                assertEquals(0f, state.translationY)
            }
        }
    }
}
