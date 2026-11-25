package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.ui.unit.IntSize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BacktrackingComplexitySceneTest {
    @Test
    fun originalSnapshotsPreserveEvaluationsHaltAndFinalSavings() {
        val trace = BacktrackingComplexityTrace.createTrace()
        val scene = BacktrackingComplexitySceneBuilder.build(trace)
        assertEquals(9, scene.frames.size)
        assertEquals(trace, scene.frames.map { it.step })
        assertEquals(listOf(1, 2, 5, 8), scene.elements.map { it.value })
        assertEquals(listOf("element-0", "element-1", "element-2", "element-3"), scene.elements.map { it.id })
        assertTrue(scene.frames.first().unpruned.all { it == ComplexityElementState.PENDING })
        assertTrue(scene.frames.first().pruned.all { it == ComplexityElementState.PENDING })
        scene.frames.forEach { frame ->
            assertEquals(frame.step.unprunedCalls, frame.unpruned.count { it == ComplexityElementState.EVAL })
            assertEquals(frame.step.prunedCalls, frame.pruned.count { it == ComplexityElementState.VALID || it == ComplexityElementState.HALT })
            if (frame.haltIndex == null) assertFalse(ComplexityElementState.SKIP in frame.pruned)
            frame.haltIndex?.let { haltIndex ->
                assertEquals(ComplexityElementState.HALT, frame.pruned[haltIndex])
                assertTrue(frame.pruned.drop(haltIndex + 1).all { it == ComplexityElementState.SKIP })
            }
            val expected = when (frame.step.action) {
                ComplexityAction.EVALUATE_UNPRUNED -> ComplexityStrategy.UNPRUNED
                ComplexityAction.EVALUATE_PRUNED, ComplexityAction.PRUNED_HALT -> ComplexityStrategy.PRUNED
                else -> null
            }
            assertEquals(expected, frame.activeStrategy)
            assertEquals(if (expected == null) null else frame.step.currentIndex, frame.activeIndex)
        }
        scene.frames.take(8).forEach { assertNull(it.savedCalls); assertNull(it.savingsPercent) }
        val halt = scene.frames.single { it.step.action == ComplexityAction.PRUNED_HALT }
        assertEquals(listOf(ComplexityElementState.VALID, ComplexityElementState.VALID, ComplexityElementState.HALT, ComplexityElementState.SKIP), halt.pruned)
        assertTrue(halt.unpruned.all { it == ComplexityElementState.EVAL })
        assertEquals(2, halt.haltIndex)
        val last = scene.frames.last()
        assertEquals(4, last.step.unprunedCalls)
        assertEquals(3, last.step.prunedCalls)
        assertEquals(1, last.savedCalls)
        assertEquals(25.0, last.savingsPercent)
        assertTrue(last.sortedInput)
    }

    @Test
    fun alternateInputsDoNotClaimSortedSafetyAndZeroCallsAreSafe() {
        val unsorted = BacktrackingComplexitySceneBuilder.build(BacktrackingComplexityTrace.createTrace(listOf(5, 1, 1), 2))
        assertFalse(unsorted.frames.last().sortedInput)
        assertEquals(listOf(ComplexityElementState.HALT, ComplexityElementState.SKIP, ComplexityElementState.SKIP), unsorted.frames.last().pruned)
        assertEquals(3, unsorted.elements.map { it.id }.toSet().size)
        val noPruning = BacktrackingComplexitySceneBuilder.build(BacktrackingComplexityTrace.createTrace(listOf(1, 2), 2)).frames.last()
        assertEquals(0, noPruning.savedCalls)
        assertTrue(noPruning.pruned.all { it == ComplexityElementState.VALID })
        val empty = BacktrackingComplexitySceneBuilder.build(BacktrackingComplexityTrace.createTrace(emptyList(), 2)).frames.last()
        assertEquals(0, empty.savedCalls)
        assertEquals(0.0, empty.savingsPercent)
    }

    @Test
    fun measuredRegionsAndElementsRemainSeparatedAtAllViewports() {
        val scene = BacktrackingComplexitySceneBuilder.build(BacktrackingComplexityTrace.createTrace())
        val metrics = ComplexityLayoutMetrics(strategyHeadingWidth = 145f, elementWidth = 55f, elementHeight = 32f,
            paragraphHeights = listOf(34f, 24f, 35f, 46f), savingsHeight = 18f)
        listOf(IntSize(360, 224), IntSize(400, 260), IntSize(460, 320), IntSize(240, 224)).forEach { viewport ->
            val layout = BacktrackingComplexitySceneLayout.calculate(scene, viewport.width.toFloat(), viewport.height.toFloat(), metrics)
            assertFalse(overlap(layout.unprunedCard, layout.prunedCard))
            assertTrue(layout.header.bottom < layout.unprunedCard.y)
            assertTrue(layout.unprunedCard.bottom < layout.explanationRegion.y)
            assertTrue(layout.explanationRegion.bottom <= layout.height)
            assertEquals(4, layout.unprunedElements.size)
            assertEquals(4, layout.prunedElements.size)
            listOf(layout.unprunedElements to layout.unprunedCard, layout.prunedElements to layout.prunedCard).forEach { (elements, card) ->
                elements.forEachIndexed { index, rect ->
                    assertTrue(rect.x >= card.x && rect.right <= card.right && rect.bottom <= card.bottom)
                    elements.drop(index + 1).forEach { assertFalse(overlap(rect, it)) }
                }
            }
            layout.paragraphs.zipWithNext().forEach { (a, b) -> assertTrue(a.bottom < b.y) }
            assertTrue(layout.paragraphs.last().bottom < layout.savings.y)
            val state = VisualizerCanvasState(initialScaleMode = VisualizerCanvasInitialScale.AUTO_FIT)
            state.updateViewportSize(viewport)
            state.updateContentSize(IntSize(layout.width.toInt(), layout.height.toInt()))
            assertEquals(0f, state.translationX, "Initial horizontal translation")
            assertEquals(0f, state.translationY, "Initial vertical translation")
            assertTrue(layout.width * state.scale <= viewport.width + .01f)
            assertTrue(layout.height * state.scale <= viewport.height + .01f)
        }
    }

    @Test
    fun selectionComparesBothDimensionsOfMeasuredCandidates() {
        val scene = BacktrackingComplexitySceneBuilder.build(BacktrackingComplexityTrace.createTrace())
        val narrowMetrics = ComplexityLayoutMetrics(paragraphHeights = listOf(40f, 30f, 40f, 50f))
        val wideMetrics = ComplexityLayoutMetrics(paragraphHeights = listOf(25f, 20f, 25f, 30f))
        val narrow = BacktrackingComplexitySceneLayout.calculate(scene, 360f, 224f, narrowMetrics)
        val wide = BacktrackingComplexitySceneLayout.calculate(scene, 420f, 224f, wideMetrics)
        val selected = BacktrackingComplexitySceneLayout.choose(360f, 224f, listOf(narrow, wide))
        fun fit(layout: ComplexitySceneLayoutResult) = minOf(360f / layout.width, 224f / layout.height, 1f)
        assertEquals(maxOf(fit(narrow), fit(wide)), fit(selected))
    }

    @Test
    fun wrappedTextMeasurementWidthsStayStableAfterHeightRecalculation() {
        val scene = BacktrackingComplexitySceneBuilder.build(BacktrackingComplexityTrace.createTrace())
        val preliminary = BacktrackingComplexitySceneLayout.calculate(scene, 360f, 224f)
        val measured = BacktrackingComplexitySceneLayout.calculate(scene, 360f, 224f,
            ComplexityLayoutMetrics(strategyExplanationHeight = 55f, paragraphHeights = listOf(60f, 35f, 75f, 80f), savingsHeight = 30f))
        assertEquals(preliminary.unprunedExplanation.width, measured.unprunedExplanation.width)
        assertEquals(preliminary.prunedExplanation.width, measured.prunedExplanation.width)
        assertEquals(preliminary.explanationHeading.width, measured.explanationHeading.width)
        assertTrue(measured.paragraphs.last().bottom < measured.savings.y)
        assertTrue(measured.savings.bottom <= measured.explanationRegion.bottom)
    }

    private fun overlap(a: ComplexityRect, b: ComplexityRect) = a.x < b.right && a.right > b.x && a.y < b.bottom && a.bottom > b.y
}
