package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.ui.unit.IntSize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GreedyCoinChangeSceneTest {
    @Test
    fun defaultTraceProjectsEveryOriginalSnapshotWithoutRevealingFutureCoins() {
        val trace = GreedyCoinChangeTrace.createTrace()
        val presentation = DedicatedGreedyCoinChangeSceneBuilder.build(trace)

        assertEquals(9, trace.size)
        assertEquals(
            listOf(
                CoinChangeStatus.INIT,
                CoinChangeStatus.CONSIDERING,
                CoinChangeStatus.TAKE_COIN,
                CoinChangeStatus.CONSIDERING,
                CoinChangeStatus.TAKE_COIN,
                CoinChangeStatus.SKIP_COIN,
                CoinChangeStatus.CONSIDERING,
                CoinChangeStatus.TAKE_COIN,
                CoinChangeStatus.FINISHED
            ),
            presentation.frames.map { it.step.status }
        )
        assertEquals(trace, presentation.frames.map { it.step })
        assertEquals(listOf(11, 11, 7, 7, 3, 3, 3, 0, 0), presentation.frames.map { it.step.remainingAmount })
        assertEquals(
            listOf(
                emptyList(), emptyList(), listOf(4), listOf(4),
                listOf(4, 4), listOf(4, 4), listOf(4, 4),
                listOf(4, 4, 3), listOf(4, 4, 3)
            ),
            presentation.frames.map { frame -> frame.selectedCoinOccurrences.map { it.value } }
        )
        assertEquals(
            listOf(
                listOf(CoinDenominationVisualState.PENDING, CoinDenominationVisualState.PENDING, CoinDenominationVisualState.PENDING),
                listOf(CoinDenominationVisualState.ACTIVE, CoinDenominationVisualState.PENDING, CoinDenominationVisualState.PENDING),
                listOf(CoinDenominationVisualState.TAKE, CoinDenominationVisualState.PENDING, CoinDenominationVisualState.PENDING),
                listOf(CoinDenominationVisualState.ACTIVE, CoinDenominationVisualState.PENDING, CoinDenominationVisualState.PENDING),
                listOf(CoinDenominationVisualState.TAKE, CoinDenominationVisualState.PENDING, CoinDenominationVisualState.PENDING),
                listOf(CoinDenominationVisualState.SKIP, CoinDenominationVisualState.PENDING, CoinDenominationVisualState.PENDING),
                listOf(CoinDenominationVisualState.PENDING, CoinDenominationVisualState.ACTIVE, CoinDenominationVisualState.PENDING),
                listOf(CoinDenominationVisualState.PENDING, CoinDenominationVisualState.TAKE, CoinDenominationVisualState.PENDING),
                listOf(CoinDenominationVisualState.PENDING, CoinDenominationVisualState.PENDING, CoinDenominationVisualState.PENDING)
            ),
            presentation.frames.map { it.denominationStates }
        )
        presentation.frames.forEach { frame ->
            assertEquals(frame.step.targetAmount - frame.step.remainingAmount, frame.selectedAmount.toInt())
            assertEquals(frame.step.selectedCoins.sum().toLong(), frame.selectedAmount)
        }
        assertEquals(3, presentation.maximumSelectedCoinCount)
        assertEquals(listOf(4, 4, 3), presentation.frames.last().selectedCoinOccurrences.map { it.value })
        assertEquals(3, presentation.frames.last().selectedCoinOccurrences.size)
        assertEquals(11L, presentation.frames.last().selectedAmount)
        assertEquals(11, presentation.frames.last().selectedCoinOccurrences.sumOf { it.value })
        assertEquals(0, presentation.frames.last().step.remainingAmount)
    }

    @Test
    fun duplicateDenominationsAndSelectedCoinsKeepDistinctOccurrenceIdentities() {
        val trace = GreedyCoinChangeTrace.createTrace(target = 8, denominations = listOf(4, 4, 2))
        val presentation = DedicatedGreedyCoinChangeSceneBuilder.build(trace)
        val finalFrame = presentation.frames.last()

        assertEquals(listOf("denomination-0", "denomination-1", "denomination-2"), presentation.denominationOccurrences.map { it.id })
        assertEquals(listOf(4, 4, 2), presentation.denominationOccurrences.map { it.value })
        assertEquals(listOf("selected-coin-0", "selected-coin-1"), finalFrame.selectedCoinOccurrences.map { it.id })
        assertEquals(listOf(4, 4), finalFrame.selectedCoinOccurrences.map { it.value })
        assertEquals(8L, finalFrame.selectedAmount)
    }

    @Test
    fun skippedDenominationDoesNotRevealFutureSelectedCoins() {
        val trace = GreedyCoinChangeTrace.createTrace(target = 3, denominations = listOf(4, 3, 1))
        val presentation = DedicatedGreedyCoinChangeSceneBuilder.build(trace)

        assertEquals(
            listOf(CoinChangeStatus.INIT, CoinChangeStatus.SKIP_COIN, CoinChangeStatus.CONSIDERING, CoinChangeStatus.TAKE_COIN, CoinChangeStatus.FINISHED),
            presentation.frames.map { it.step.status }
        )
        assertEquals(emptyList(), presentation.frames[1].selectedCoinOccurrences)
        assertEquals(listOf(CoinDenominationVisualState.SKIP, CoinDenominationVisualState.PENDING, CoinDenominationVisualState.PENDING), presentation.frames[1].denominationStates)
        assertEquals(emptyList(), presentation.frames[2].selectedCoinOccurrences)
        assertEquals(listOf(3), presentation.frames[3].selectedCoinOccurrences.map { it.value })
    }

    @Test
    fun layoutContainsAllRegionsAndCoinValuesAcrossSupportedPanesAndSnapshots() {
        val presentation = DedicatedGreedyCoinChangeSceneBuilder.build(GreedyCoinChangeTrace.createTrace())
        listOf(360f to 224f, 400f to 260f, 460f to 320f).forEach { (width, height) ->
            val layouts = presentation.frames.map {
                GreedyCoinChangeSceneLayout.calculate(presentation, viewportWidth = width)
            }
            val layout = layouts.first()
            layouts.drop(1).forEach { assertEquals(layout, it, "Static geometry changed at $width x $height") }

            assertTrue(layout.width >= width)
            assertTrue(layout.height > 0f)
            assertTrue(layout.targetRegion.contains(layout.targetTitle))
            assertTrue(layout.targetRegion.contains(layout.denominationList))
            assertTrue(layout.targetRegion.contains(layout.remainingBadge))
            assertTrue(layout.denominationRegion.contains(layout.denominationsHeading))
            assertTrue(layout.selectedRegion.contains(layout.selectedHeading))
            assertTrue(layout.selectedRegion.contains(layout.totalCount))
            assertTrue(layout.selectedRegion.contains(layout.selectedAmount))
            assertTrue(layout.emptyMessage.height > 0f)
            assertTrue(layout.selectedRegion.contains(layout.emptyMessage))
            layout.denominations.forEach { denomination ->
                assertTrue(layout.denominationRegion.contains(denomination.cell))
                assertTrue(denomination.cell.contains(denomination.stateLabel))
                assertTrue(denomination.cell.contains(denomination.circle))
                assertEquals(48f, denomination.circle.width)
                assertEquals(48f, denomination.circle.height)
            }
            layout.selectedCoins.forEach { coin ->
                assertTrue(layout.selectedRegion.contains(coin.circle))
                assertEquals(36f, coin.circle.width)
                assertEquals(36f, coin.circle.height)
            }
            assertTrue(layout.targetRegion.bottom < layout.denominationRegion.y)
            assertTrue(layout.denominationRegion.bottom < layout.selectedRegion.y)
            assertTrue(layout.selectedRegion.bottom <= layout.height)
        }
    }

    @Test
    fun initialEmptyMessageUsesReservedCoinAreaWithoutChangingDefaultSceneHeight() {
        val presentation = DedicatedGreedyCoinChangeSceneBuilder.build(GreedyCoinChangeTrace.createTrace())
        val initialLayout = GreedyCoinChangeSceneLayout.calculate(presentation, viewportWidth = 360f)
        val laterLayout = GreedyCoinChangeSceneLayout.calculate(presentation, viewportWidth = 360f)

        assertTrue(initialLayout.emptyMessage.width > 0f)
        assertTrue(initialLayout.emptyMessage.height > 0f)
        assertTrue(initialLayout.selectedRegion.contains(initialLayout.emptyMessage))
        assertEquals(initialLayout.height, laterLayout.height)
        assertTrue(initialLayout.emptyMessage.bottom <= initialLayout.selectedCoins.first().circle.bottom)
    }

    @Test
    fun measuredLongLabelsAndCoinValuesExpandGeometryWithoutOverlap() {
        val presentation = DedicatedGreedyCoinChangeSceneBuilder.build(
            GreedyCoinChangeTrace.createTrace(
                target = 987654,
                denominations = listOf(987654, 123456, 34567, 2345, 123, 45, 2, 1)
            )
        )
        val base = CoinChangeTextMeasurements.NORMAL
        val measured = base.copy(
            targetTitle = CoinChangeTextExtent(216f, 14f),
            denominationList = CoinChangeTextExtent(238f, 13f),
            denominationValue = CoinChangeTextExtent(71f, 19f),
            selectedValue = CoinChangeTextExtent(58f, 16f),
            selectedHeading = CoinChangeTextExtent(154f, 13f),
            selectedTotal = CoinChangeTextExtent(83f, 13f),
            selectedAmount = CoinChangeTextExtent(164f, 12f)
        )
        val layout = GreedyCoinChangeSceneLayout.calculate(
            presentation,
            viewportWidth = 360f,
            denominationDiameter = 83f,
            selectedCoinDiameter = 70f,
            measurements = measured
        )

        assertTrue(layout.width > 360f)
        assertTrue(layout.denominationRowCount > 1)
        assertTrue(layout.denominations.all { it.circle.width >= measured.denominationValue.width + 12f })
        assertTrue(layout.denominations.all { it.cell.contains(it.stateLabel) && it.cell.contains(it.circle) })
        assertTrue(layout.targetRegion.contains(layout.targetTitle))
        assertTrue(layout.targetRegion.contains(layout.denominationList))
        assertTrue(layout.selectedRegion.contains(layout.selectedHeading))
        assertTrue(layout.selectedRegion.contains(layout.totalCount))
        assertTrue(layout.selectedRegion.contains(layout.emptyMessage))
        assertTrue(layout.selectedRegion.contains(layout.selectedAmount))
        assertTrue(layout.height >= layout.selectedRegion.bottom)
        assertTrue(layout.selectedCoins.all { it.circle.width >= measured.selectedValue.width + 12f })
        layout.denominations.forEachIndexed { index, denomination ->
            layout.denominations.drop(index + 1).forEach { other ->
                assertTrue(!intersects(denomination.cell, other.cell), "${denomination.id} overlaps ${other.id}")
            }
        }
    }

    @Test
    fun longerSelectedSequenceReservesNonOverlappingRows() {
        val trace = GreedyCoinChangeTrace.createTrace(target = 11, denominations = listOf(1))
        val presentation = DedicatedGreedyCoinChangeSceneBuilder.build(trace)
        val layout = GreedyCoinChangeSceneLayout.calculate(presentation, viewportWidth = 360f)
        val finalFrame = presentation.frames.last()

        assertEquals(11, presentation.maximumSelectedCoinCount)
        assertEquals(11, finalFrame.selectedCoinOccurrences.size)
        assertEquals(11, layout.selectedCoins.size)
        assertTrue(layout.selectedRowCount > 1)
        layout.selectedCoins.forEachIndexed { index, coin ->
            assertEquals(index, coin.sequenceIndex)
            assertTrue(layout.selectedRegion.contains(coin.circle))
            layout.selectedCoins.drop(index + 1).forEach { other ->
                assertTrue(!intersects(coin.circle, other.circle), "${coin.id} overlaps ${other.id}")
            }
        }
    }

    @Test
    fun viewportUsesProductionAutoFitStateForCompleteScene() {
        val presentation = DedicatedGreedyCoinChangeSceneBuilder.build(GreedyCoinChangeTrace.createTrace())
        listOf(IntSize(360, 224), IntSize(400, 260), IntSize(460, 320)).forEach { viewport ->
            val layout = GreedyCoinChangeSceneLayout.calculate(presentation, viewport.width.toFloat())
            val state = VisualizerCanvasState(initialScaleMode = VisualizerCanvasInitialScale.AUTO_FIT)
            state.updateViewportSize(viewport)
            state.updateContentSize(IntSize(layout.width.toInt(), layout.height.toInt()))
            assertTrue(state.scale > 0f)
            assertTrue(layout.width * state.scale <= viewport.width + 1f)
            assertTrue(layout.height * state.scale <= viewport.height + 1f)
            assertEquals(0f, state.translationX)
            assertEquals(0f, state.translationY)
        }
    }

    @Test
    fun compactViewportPlacesSelectedAmountBesideReservedCoinsWithoutOverlap() {
        val presentation = DedicatedGreedyCoinChangeSceneBuilder.build(GreedyCoinChangeTrace.createTrace())
        listOf(360f to 224f, 400f to 260f, 240f to 224f).forEach { (width, height) ->
            val layout = GreedyCoinChangeSceneLayout.calculate(presentation, width, viewportHeight = height)
            assertEquals(250f, layout.height)
            assertEquals(56f, layout.targetRegion.height)
            assertEquals(90f, layout.denominationRegion.height)
            assertTrue(layout.selectedRegion.contains(layout.emptyMessage))
            assertTrue(layout.selectedRegion.contains(layout.selectedAmount))
            assertTrue(!intersects(layout.emptyMessage, layout.selectedAmount))
            layout.selectedCoins.forEach { coin ->
                assertTrue(layout.selectedRegion.contains(coin.circle))
                assertTrue(!intersects(coin.circle, layout.selectedAmount))
            }
            assertTrue(minOf(width / layout.width, height / layout.height, 1f) >= .85f)
            val state = VisualizerCanvasState(initialScaleMode = VisualizerCanvasInitialScale.AUTO_FIT)
            state.updateViewportSize(IntSize(width.toInt(), height.toInt()))
            state.updateContentSize(IntSize(layout.width.toInt(), layout.height.toInt()))
            assertEquals(0f, state.translationX)
            assertEquals(0f, state.translationY)
        }
    }

    private fun intersects(first: CoinChangeRect, second: CoinChangeRect): Boolean =
        first.x < second.right && first.right > second.x && first.y < second.bottom && first.bottom > second.y
}
