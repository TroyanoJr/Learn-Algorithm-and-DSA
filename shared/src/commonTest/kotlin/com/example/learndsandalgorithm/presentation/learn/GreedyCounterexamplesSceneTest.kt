package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.ui.unit.IntSize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GreedyCounterexamplesSceneTest {
    @Test
    fun defaultTracePreservesAllSixSnapshotsAndRevealsOptimalOnlyAtComparison() {
        val trace = GreedyCounterexamplesTrace.createTrace()
        val presentation = DedicatedGreedyCounterexamplesSceneBuilder.build(trace)

        assertEquals(6, trace.size)
        assertEquals(trace, presentation.frames.map { it.step })
        assertEquals(
            listOf(
                CounterexampleStatus.INIT,
                CounterexampleStatus.GREEDY_STEP_1,
                CounterexampleStatus.GREEDY_STEP_2,
                CounterexampleStatus.GREEDY_STEP_3,
                CounterexampleStatus.COMPARE_OPTIMAL,
                CounterexampleStatus.FINISHED
            ),
            presentation.frames.map { it.step.status }
        )
        assertEquals(
            listOf(emptyList(), listOf(4), listOf(4, 1), listOf(4, 1, 1), listOf(4, 1, 1), listOf(4, 1, 1)),
            presentation.frames.map { frame -> frame.greedyCoins.map { it.value } }
        )
        assertTrue(presentation.frames.take(4).all { it.optimalCoins.isEmpty() && !it.optimalRevealed })
        assertEquals(listOf(3, 3), presentation.frames[4].optimalCoins.map { it.value })
        assertEquals(listOf(3, 3), presentation.frames[5].optimalCoins.map { it.value })
        assertTrue(presentation.frames[4].failureDemonstrated)
        assertFalse(presentation.frames[4].finalVerdict)
        assertTrue(presentation.frames[5].finalVerdict)
        assertEquals(
            listOf("COMPARING", "COMPARING", "COMPARING", "COMPARING", "COMPARING", "FAILED GREEDY"),
            presentation.frames.map(::counterexampleHeaderBadgeText)
        )
        assertEquals(listOf("greedy-coin-0", "greedy-coin-1", "greedy-coin-2"), presentation.frames[3].greedyCoins.map { it.id })
        assertEquals(listOf("optimal-coin-0", "optimal-coin-1"), presentation.frames[4].optimalCoins.map { it.id })
        assertEquals(3, presentation.maximumGreedyCoinCount)
        assertEquals(2, presentation.maximumOptimalCoinCount)
    }

    @Test
    fun completeCausalExplanationAndConclusionRemainAvailableAfterComparison() {
        val presentation = DedicatedGreedyCounterexamplesSceneBuilder.build(GreedyCounterexamplesTrace.createTrace())
        val finalFrame = presentation.frames.last()
        val explanation = GreedyCounterexampleSceneBuilder.WHY_GREEDY_FAILS

        assertTrue(explanation.contains("coin 4 locally leaves amount 2"))
        assertTrue(explanation.contains("two 1s (total 3 coins)"))
        assertTrue(explanation.contains("coin 3 leaves 3"))
        assertTrue(explanation.contains("Local best != Global best"))
        assertTrue(finalFrame.step.explanation.contains("locally optimal"))
        assertTrue(finalFrame.step.explanation.contains("global optimum"))
        assertTrue(finalFrame.step.explanation.contains("Greedy choice property fails here"))
        assertTrue(finalFrame.step.badgeText.contains("COUNTEREXAMPLE PROVEN"))
    }

    @Test
    fun failureStylingRequiresBothValidAmountsAndFewerOptimalCoins() {
        val trace = GreedyCounterexamplesTrace.createTrace()
        val normal = DedicatedGreedyCounterexamplesSceneBuilder.build(trace)
        assertTrue(normal.frames.take(4).none { it.failureDemonstrated })
        assertTrue(normal.frames[4].failureDemonstrated)
        assertEquals(6, normal.frames[4].greedyCoins.sumOf { it.value })
        assertEquals(6, normal.frames[4].optimalCoins.sumOf { it.value })
        assertTrue(normal.frames[4].optimalCoins.size < normal.frames[4].greedyCoins.size)

        val equalCountComparison = trace.map { step ->
            if (step.status == CounterexampleStatus.COMPARE_OPTIMAL || step.status == CounterexampleStatus.FINISHED) {
                step.copy(optimalCoins = listOf(4, 1, 1))
            } else step
        }
        val noFailure = DedicatedGreedyCounterexamplesSceneBuilder.build(equalCountComparison)
        assertTrue(noFailure.frames[4].optimalRevealed)
        assertFalse(noFailure.frames[4].failureDemonstrated)
        assertFalse(noFailure.frames[5].finalVerdict)
        assertEquals("COMPARING", counterexampleHeaderBadgeText(noFailure.frames[4]))
    }

    @Test
    fun comparisonGeometryStaysStableWhileCoinsAndDisclosureChange() {
        val presentation = DedicatedGreedyCounterexamplesSceneBuilder.build(GreedyCounterexamplesTrace.createTrace())
        listOf(360f to 224f, 400f to 260f, 460f to 320f).forEach { (width, _) ->
            val layouts = presentation.frames.map { frame ->
                GreedyCounterexamplesSceneLayout.calculate(presentation, frame, width)
            }
            val initial = layouts.first()
            layouts.drop(1).forEach { layout ->
                assertEquals(initial.width, layout.width)
                assertEquals(initial.height, layout.height)
                assertEquals(initial.headerRegion, layout.headerRegion)
                assertEquals(initial.targetLabel, layout.targetLabel)
                assertEquals(initial.denominationLabel, layout.denominationLabel)
                assertEquals(initial.headerStatusBadge, layout.headerStatusBadge)
                assertEquals(initial.greedyCard, layout.greedyCard)
                assertEquals(initial.optimalCard, layout.optimalCard)
                assertEquals(initial.comparisonSummary, layout.comparisonSummary)
                assertEquals(initial.insightRegion, layout.insightRegion)
                assertEquals(initial.proofConclusion, layout.proofConclusion)
            }
            layouts.forEachIndexed { index, layout ->
                assertTrue(layout.width >= width)
                assertTrue(layout.headerRegion.contains(layout.targetLabel))
                assertTrue(layout.headerRegion.contains(layout.denominationLabel))
                assertTrue(layout.headerRegion.contains(layout.headerStatusBadge))
                assertTrue(layout.greedyCard.contains(layout.greedyHeading))
                assertTrue(layout.greedyCard.contains(layout.greedyCount))
                assertTrue(layout.greedyCard.contains(layout.greedyRemaining))
                assertTrue(layout.optimalCard.contains(layout.optimalHeading))
                assertTrue(layout.optimalCard.contains(layout.optimalCount))
                assertTrue(layout.greedyCard.contains(layout.greedyCoinArea))
                assertTrue(layout.greedyCard.contains(layout.greedyCount))
                assertTrue(layout.greedyCoinArea.bottom < layout.greedyCount.y)
                assertTrue(layout.greedyCount.bottom < layout.greedyRemaining.y)
                assertTrue(layout.optimalCard.contains(layout.optimalCoinArea))
                assertTrue(layout.optimalCoinArea.bottom < layout.optimalCount.y)
                assertTrue(layout.comparisonSummary.bottom <= layout.insightRegion.y)
                assertTrue(layout.insightRegion.contains(layout.insightHeading))
                assertTrue(layout.insightRegion.contains(layout.insightBody))
                assertTrue(layout.proofConclusion.bottom <= layout.height)
                layout.greedyCoins.forEach { coin ->
                    assertTrue(layout.greedyCard.contains(coin.circle))
                    assertTrue(layout.greedyCoinArea.contains(coin.circle))
                    assertTrue(coin.circle.contains(coin.valueLabel))
                    assertTrue(coin.valueLabel.width < coin.circle.width)
                    assertEquals(32f, coin.circle.width)
                    assertEquals(32f, coin.circle.height)
                }
                layout.optimalCoins.forEach { coin ->
                    assertTrue(layout.optimalCard.contains(coin.circle))
                    assertTrue(layout.optimalCoinArea.contains(coin.circle))
                    assertTrue(coin.circle.contains(coin.valueLabel))
                }
                assertEquals(index >= 4, layout.optimalCoins.isNotEmpty())
                layout.greedyCoins.forEachIndexed { coinIndex, coin ->
                    layout.greedyCoins.drop(coinIndex + 1).forEach { other -> assertFalse(intersects(coin.circle, other.circle)) }
                }
                if (layout.greedyCoins.isNotEmpty()) {
                    val coinsLeft = layout.greedyCoins.minOf { it.circle.x }
                    val coinsRight = layout.greedyCoins.maxOf { it.circle.right }
                    assertEquals(layout.greedyCard.x + layout.greedyCard.width / 2f, (coinsLeft + coinsRight) / 2f)
                }
                layout.optimalCoins.forEachIndexed { coinIndex, coin ->
                    layout.optimalCoins.drop(coinIndex + 1).forEach { other -> assertFalse(intersects(coin.circle, other.circle)) }
                }
            }
        }
    }

    @Test
    fun viewportAutoFitContainsCompleteComparisonScene() {
        val presentation = DedicatedGreedyCounterexamplesSceneBuilder.build(GreedyCounterexamplesTrace.createTrace())
        val finalFrame = presentation.frames.last()
        listOf(IntSize(360, 224), IntSize(400, 260), IntSize(460, 320)).forEach { viewport ->
            val layout = GreedyCounterexamplesSceneLayout.calculate(presentation, finalFrame, viewport.width.toFloat())
            val state = VisualizerCanvasState(initialScaleMode = VisualizerCanvasInitialScale.AUTO_FIT)
            state.updateViewportSize(viewport)
            state.updateContentSize(IntSize(layout.width.toInt(), layout.height.toInt()))
            assertTrue(layout.width * state.scale <= viewport.width + 1f)
            assertTrue(layout.height * state.scale <= viewport.height + 1f)
            assertEquals(0f, state.translationX)
            assertEquals(0f, state.translationY)
        }
    }

    @Test
    fun largerMeasuredCoinsAndWrappedExplanationsExpandWithoutOverlapping() {
        val presentation = DedicatedGreedyCounterexamplesSceneBuilder.build(GreedyCounterexamplesTrace.createTrace())
        val metrics = CounterexampleSceneTextExtents.GEOMETRY_DEFAULT.copy(
            target = CounterexampleTextExtent(250f, 30f),
            denominations = CounterexampleTextExtent(300f, 36f),
            headerBadge = CounterexampleTextExtent(145f, 22f),
            greedyCount = CounterexampleTextExtent(170f, 24f),
            greedyRemaining = CounterexampleTextExtent(180f, 24f),
            optimalCount = CounterexampleTextExtent(210f, 32f),
            comparisonSummary = CounterexampleTextExtent(360f, 36f),
            insightBody = CounterexampleTextExtent(460f, 78f),
            proofConclusion = CounterexampleTextExtent(440f, 32f)
        )
        val largeCoinLayout = GreedyCounterexamplesSceneLayout.calculate(
            presentation,
            presentation.frames.last(),
            viewportWidth = 360f,
            coinDiameter = counterexampleCoinDiameter(maxNaturalWidth = 48f, maxTextHeight = 22f),
            textExtents = metrics
        )
        val standardLayout = GreedyCounterexamplesSceneLayout.calculate(presentation, presentation.frames.last(), 360f)

        assertEquals(60f, largeCoinLayout.greedyCoins.first().circle.width)
        assertTrue(largeCoinLayout.width > 360f)
        assertTrue(largeCoinLayout.height > standardLayout.height)
        assertTrue(largeCoinLayout.headerRegion.contains(largeCoinLayout.targetLabel))
        assertTrue(largeCoinLayout.headerRegion.contains(largeCoinLayout.denominationLabel))
        assertTrue(largeCoinLayout.headerRegion.contains(largeCoinLayout.headerStatusBadge))
        assertTrue(largeCoinLayout.greedyCard.contains(largeCoinLayout.greedyCoinArea))
        assertTrue(largeCoinLayout.greedyCoinArea.bottom < largeCoinLayout.greedyCount.y)
        assertTrue(largeCoinLayout.greedyCount.bottom < largeCoinLayout.greedyRemaining.y)
        largeCoinLayout.greedyCoins.forEach { coin ->
            assertTrue(largeCoinLayout.greedyCoinArea.contains(coin.circle))
            assertTrue(coin.circle.contains(coin.valueLabel))
        }
        assertTrue(largeCoinLayout.comparisonSummary.contains(
            largeCoinLayout.comparisonSummary.copy(x = largeCoinLayout.comparisonSummary.x + 4f, y = largeCoinLayout.comparisonSummary.y + 4f, width = largeCoinLayout.comparisonSummary.width - 8f, height = largeCoinLayout.comparisonSummary.height - 8f)
        ))
        assertTrue(largeCoinLayout.insightRegion.contains(largeCoinLayout.insightBody))
        assertTrue(largeCoinLayout.proofConclusion.bottom <= largeCoinLayout.height)
        assertTrue(largeCoinLayout.greedyCoins.map { it.circle.width }.distinct().size == 1)
    }

    @Test
    fun compactViewportPreservesComparisonAndCausalProofAcrossAllSnapshots() {
        val presentation = DedicatedGreedyCounterexamplesSceneBuilder.build(GreedyCounterexamplesTrace.createTrace())
        val metrics = CounterexampleSceneTextExtents.GEOMETRY_DEFAULT.copy(
            greedyCount = CounterexampleTextExtent(68f, 13f),
            greedyRemaining = CounterexampleTextExtent(70f, 12f)
        )
        listOf(IntSize(360, 224), IntSize(400, 260), IntSize(460, 320), IntSize(240, 224)).forEach { viewport ->
            val layouts = presentation.frames.map { frame ->
                GreedyCounterexamplesSceneLayout.calculate(
                    presentation, frame, viewport.width.toFloat(), textExtents = metrics,
                    viewportHeight = viewport.height.toFloat()
                )
            }
            val first = layouts.first()
            val baseline = GreedyCounterexamplesSceneLayout.calculate(
                presentation, presentation.frames.first(), viewport.width.toFloat(), textExtents = metrics
            )
            layouts.forEach { layout ->
                assertEquals(first.width, layout.width)
                assertEquals(first.height, layout.height)
                assertEquals(first.greedyCard, layout.greedyCard)
                assertEquals(first.optimalCard, layout.optimalCard)
                assertTrue(layout.headerRegion.contains(layout.targetLabel))
                assertTrue(layout.headerRegion.contains(layout.denominationLabel))
                assertTrue(layout.headerRegion.contains(layout.headerStatusBadge))
                assertTrue(layout.greedyCard.contains(layout.greedyCoinArea))
                assertTrue(layout.optimalCard.contains(layout.optimalCoinArea))
                assertTrue(layout.greedyCard.contains(layout.greedyRemaining))
                assertTrue(layout.comparisonSummary.bottom <= layout.insightRegion.y)
                assertTrue(layout.insightRegion.contains(layout.insightBody))
                assertTrue(layout.proofConclusion.bottom <= layout.height)
                assertFalse(intersects(layout.greedyCount, layout.greedyRemaining))
                layout.greedyCoins.forEach { coin -> assertTrue(layout.greedyCoinArea.contains(coin.circle)) }
                layout.optimalCoins.forEach { coin -> assertTrue(layout.optimalCoinArea.contains(coin.circle)) }
            }
            if (viewport.height < 300) assertTrue(first.height < baseline.height)
            val state = VisualizerCanvasState(initialScaleMode = VisualizerCanvasInitialScale.AUTO_FIT)
            state.updateViewportSize(viewport)
            state.updateContentSize(IntSize(first.width.toInt(), first.height.toInt()))
            assertEquals(0f, state.translationX)
            assertEquals(0f, state.translationY)
            if (viewport == IntSize(360, 224)) assertTrue(state.scale >= .85f, "scene=${first.width}x${first.height}, scale=${state.scale}")
        }
    }

    private fun intersects(first: CounterexampleRect, second: CounterexampleRect): Boolean =
        first.x < second.right && first.right > second.x && first.y < second.bottom && first.bottom > second.y
}
