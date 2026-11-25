package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.geometry.Offset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Synthetic metrics exercise the production viewport math; device font metrics are measured by renderers. */
class BacktrackingViewportAuditTest {
    @Test
    fun defaultSceneGeometryUsesProductionFitAcrossReferenceViewports() {
        val fundamentals = BacktrackingFundamentalsSceneBuilder.build(BacktrackingFundamentalsTrace.createTrace())
        val permutations = PermutationsSceneBuilder.build(PermutationsTrace.createTrace())
        val combinations = CombinationsPruningSceneBuilder.build(CombinationsPruningTrace.createTrace())
        val queens = NQueensSceneBuilder.build(NQueensTrace.createTrace())
        val grid = GridPathfindingSceneBuilder.build(GridPathfindingTrace.createTrace())
        val complexity = BacktrackingComplexitySceneBuilder.build(BacktrackingComplexityTrace.createTrace())
        println("SNAPSHOTS fundamentals=${fundamentals.frames.size} permutations=${permutations.frames.size} combinations=${combinations.frames.size} queens=${queens.frames.size} grid=${grid.frames.size} complexity=${complexity.frames.size}")
        listOf(IntSize(360, 224), IntSize(400, 260), IntSize(460, 320), IntSize(240, 224)).forEach { viewport ->
            val w = viewport.width.toFloat(); val h = viewport.height.toFloat()
            val f = BacktrackingFundamentalsSceneLayout.calculate(fundamentals, w, h)
            val p = PermutationsSceneLayout.calculate(permutations, w, h)
            val c = CombinationsPruningSceneLayout.calculate(combinations, w, h)
            val q = NQueensSceneLayout.calculate(queens, w, h)
            val g = GridPathfindingSceneLayout.calculate(grid, w, h)
            val x = BacktrackingComplexitySceneLayout.calculate(complexity, w, h)
            listOf("fundamentals" to (f.width to f.height), "permutations" to (p.width to p.height),
                "combinations" to (c.width to c.height), "queens" to (q.width to q.height),
                "grid" to (g.width to g.height), "complexity" to (x.width to x.height)).forEach { (name, size) ->
                val state = VisualizerCanvasState(initialScaleMode = VisualizerCanvasInitialScale.AUTO_FIT)
                state.updateViewportSize(viewport)
                state.updateContentSize(IntSize(size.first.toInt(), size.second.toInt()))
                assertTrue(size.first * state.scale <= w + .01f, "$name $viewport")
                assertTrue(size.second * state.scale <= h + .01f, "$name $viewport")
                assertEquals(0f, state.translationX, "$name $viewport")
                assertEquals(0f, state.translationY, "$name $viewport")
                println("GEOMETRY $name ${viewport.width}x${viewport.height} ${size.first}x${size.second} scale=${state.scale}")
                val fit = state.scale
                val center = Offset(w / 2f, h / 2f)
                state.onTransform(3f, Offset.Zero, center)
                assertTrue(state.scale > fit, "$name can zoom in at $viewport")
                state.onTransform(1f, Offset(-30f, -30f), center)
                assertTrue(state.translationX != 0f || state.translationY != 0f, "$name can pan when zoomed at $viewport")
                state.reset()
                assertEquals(fit, state.scale, "$name reset at $viewport")
                assertEquals(0f, state.translationX)
                assertEquals(0f, state.translationY)
            }
        }
    }
}
