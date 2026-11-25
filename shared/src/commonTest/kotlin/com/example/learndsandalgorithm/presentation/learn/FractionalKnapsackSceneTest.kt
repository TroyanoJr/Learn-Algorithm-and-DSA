package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.ui.unit.IntSize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FractionalKnapsackSceneTest {
    @Test
    fun defaultTracePreservesAllSnapshotsAndFinalFractionalResult() {
        val trace = FractionalKnapsackTrace.createTrace()
        val presentation = DedicatedFractionalKnapsackSceneBuilder.build(trace)

        assertEquals(9, trace.size)
        assertEquals(trace, presentation.frames.map { it.step })
        assertEquals(
            listOf(
                FractionalKnapsackStatus.INIT,
                FractionalKnapsackStatus.SORTED,
                FractionalKnapsackStatus.EVALUATING,
                FractionalKnapsackStatus.TAKE_FULL,
                FractionalKnapsackStatus.EVALUATING,
                FractionalKnapsackStatus.TAKE_FULL,
                FractionalKnapsackStatus.EVALUATING,
                FractionalKnapsackStatus.TAKE_FRACTION,
                FractionalKnapsackStatus.FINISHED
            ),
            presentation.frames.map { it.step.status }
        )
        assertEquals(listOf("A", "B", "C"), presentation.sortedOccurrences.map { it.item.name })
        assertEquals(listOf(6.0, 5.0, 4.0), presentation.sortedOccurrences.map { it.item.ratio })
        assertEquals(listOf("item-0", "item-1", "item-2"), presentation.sortedOccurrences.map { it.id })

        val partialFrame = presentation.frames[7]
        assertEquals(listOf(FractionalItemVisualState.FULL, FractionalItemVisualState.FULL, FractionalItemVisualState.PARTIAL), partialFrame.items.map { it.state })
        assertEquals(listOf(10.0, 20.0, 20.0), partialFrame.items.map { it.takenWeight })
        assertEquals(listOf(0.0, 0.0, 10.0), partialFrame.items.map { it.remainingWeight })
        assertEquals(listOf(60.0, 100.0, 80.0), partialFrame.items.map { it.valueContribution })
        assertEquals(20.0 / 30.0, partialFrame.items[2].fraction)
        assertEquals(50.0, partialFrame.selectedWeight)
        assertEquals(0.0, partialFrame.remainingCapacity)
        assertEquals(240.0, partialFrame.totalValue)
        assertEquals(false, partialFrame.isFinalResult)

        val finalFrame = presentation.frames.last()
        assertEquals(true, finalFrame.isFinalResult)
        assertEquals(240.0, finalFrame.totalValue)
        assertEquals(0.0, finalFrame.remainingCapacity)
        assertEquals(3, finalFrame.items.size)
        assertTrue(finalFrame.step.explanation.contains("globally optimal when item fractioning is allowed"))
        assertTrue(finalFrame.step.explanation.contains("O(N log N)"))
    }

    @Test
    fun duplicateNamesRetainStableOccurrenceIdentityThroughSelections() {
        val items = listOf(
            KnapsackItem("Same", 5.0, 10.0),
            KnapsackItem("Same", 5.0, 10.0),
            KnapsackItem("Other", 5.0, 5.0)
        )
        val presentation = DedicatedFractionalKnapsackSceneBuilder.build(
            FractionalKnapsackTrace.createTrace(items, capacity = 10.0)
        )
        val finalFrame = presentation.frames.last()

        assertEquals(listOf("item-0", "item-1", "item-2"), presentation.sortedOccurrences.map { it.id })
        assertEquals(listOf("Same", "Same", "Other"), presentation.sortedOccurrences.map { it.item.name })
        assertEquals(listOf(FractionalItemVisualState.FULL, FractionalItemVisualState.FULL, FractionalItemVisualState.PENDING), finalFrame.items.map { it.state })
        assertEquals(listOf(5.0, 5.0, 0.0), finalFrame.items.map { it.takenWeight })
    }

    @Test
    fun fractionalPrecisionAndCompletePerItemValuesArePreserved() {
        val items = listOf(
            KnapsackItem("A", 1.25, 3.75),
            KnapsackItem("B", 2.0, 4.25)
        )
        val presentation = DedicatedFractionalKnapsackSceneBuilder.build(
            FractionalKnapsackTrace.createTrace(items = items, capacity = 2.75)
        )
        val partialFrame = presentation.frames.single { it.step.status == FractionalKnapsackStatus.TAKE_FRACTION }

        assertEquals(0.75, partialFrame.items[1].fraction)
        assertEquals(1.5, partialFrame.items[1].takenWeight)
        assertEquals(0.5, partialFrame.items[1].remainingWeight)
        assertEquals(3.1875, partialFrame.items[1].valueContribution)
        assertEquals(6.9375, partialFrame.totalValue)
        assertTrue(partialFrame.step.explanation.contains("75% (1.5 / 2)"))
        assertEquals("EVALUATE B (RATIO 2.125)", presentation.frames[4].step.badgeText)
    }

    @Test
    fun partialPercentageAndContributionRemainDistinctAndFullyRepresented() {
        val presentation = DedicatedFractionalKnapsackSceneBuilder.build(FractionalKnapsackTrace.createTrace())
        val partial = presentation.frames.single { it.step.status == FractionalKnapsackStatus.TAKE_FRACTION }
        val itemC = partial.items.single { it.occurrence.id == "item-2" }

        assertEquals("66.67%", formatGreedyPercent(itemC.fraction))
        assertEquals(20.0, itemC.takenWeight)
        assertEquals(10.0, itemC.remainingWeight)
        assertEquals(80.0, itemC.valueContribution)
        assertEquals("FRACTION 66.67%", "FRACTION ${formatGreedyPercent(itemC.fraction)}")
        assertTrue(partial.step.explanation.contains("66.67% (20 / 30)"))
    }

    @Test
    fun layoutIsStableContainedAndNonOverlappingAcrossSnapshotsAndViewports() {
        val presentation = DedicatedFractionalKnapsackSceneBuilder.build(FractionalKnapsackTrace.createTrace())
        listOf(360f to 224f, 400f to 260f, 460f to 320f).forEach { (width, _) ->
            val layouts = presentation.frames.map {
                FractionalKnapsackSceneLayout.calculate(presentation, width)
            }
            val layout = layouts.first()
            layouts.drop(1).forEach { assertEquals(layout, it) }
            assertEquals(3, layout.items.size)
            assertTrue(layout.width >= width)
            assertTrue(layout.height > 0f)
            assertTrue(layout.capacityRegion.contains(layout.capacityTitle))
            assertTrue(layout.capacityRegion.contains(layout.capacityFilled))
            assertTrue(layout.capacityRegion.contains(layout.capacityRemaining))
            assertTrue(layout.capacityRegion.contains(layout.capacityTrack))
            assertTrue(layout.itemsRegion.contains(layout.itemsHeading))
            assertTrue(layout.resultRegion.contains(layout.resultHeading))
            assertTrue(layout.resultRegion.contains(layout.resultValue))
            assertTrue(layout.resultRegion.contains(layout.resultStatus))
            assertTrue(layout.resultRegion.contains(layout.usedCapacity))
            assertTrue(layout.resultRegion.contains(layout.optimalityExplanation))
            assertTrue(layout.resultRegion.contains(layout.optimalityBadge))
            layout.items.forEach { item ->
                assertTrue(layout.itemsRegion.contains(item.cell))
                assertTrue(item.cell.contains(item.stateLabel))
                assertTrue(item.card.contains(item.name))
                assertTrue(item.card.contains(item.weightValue))
                assertTrue(item.card.contains(item.ratio))
                assertTrue(item.card.contains(item.taken))
                assertTrue(item.card.contains(item.remaining))
                assertTrue(item.card.contains(item.contribution))
                assertTrue(item.card.contains(item.fraction))
                assertTrue(item.card.contains(item.gaugeTrack))
            }
            layout.items.forEachIndexed { index, item ->
                layout.items.drop(index + 1).forEach { other ->
                    assertTrue(!intersects(item.cell, other.cell), "${item.id} overlaps ${other.id}")
                }
                val textFields = listOf(
                    item.name, item.weightValue, item.ratio, item.taken,
                    item.remaining, item.contribution, item.fraction
                )
                textFields.forEachIndexed { fieldIndex, field ->
                    textFields.drop(fieldIndex + 1).forEach { other ->
                        assertTrue(!intersects(field, other), "${item.id} text fields overlap")
                    }
                    assertTrue(item.card.contains(field))
                }
                textFields.forEach { field -> assertTrue(!intersects(field, item.gaugeTrack)) }
            }
            assertTrue(!intersects(layout.optimalityExplanation, layout.optimalityBadge))
            assertTrue(layout.capacityRegion.bottom < layout.itemsRegion.y)
            assertTrue(layout.itemsRegion.bottom < layout.resultRegion.y)
            assertTrue(layout.resultRegion.bottom <= layout.height)
        }
    }

    @Test
    fun largerCollectionsUseAdditionalRowsWithoutOverlappingItemCards() {
        val items = (0 until 5).map { index -> KnapsackItem("I$index", 10.0, 50.0 - index) }
        val presentation = DedicatedFractionalKnapsackSceneBuilder.build(
            FractionalKnapsackTrace.createTrace(items = items, capacity = 100.0)
        )
        val layout = FractionalKnapsackSceneLayout.calculate(presentation, viewportWidth = 360f)

        assertEquals(5, presentation.sortedOccurrences.size)
        assertEquals(3, layout.itemColumnCount)
        assertEquals(2, layout.itemRowCount)
        assertEquals(5, layout.items.size)
        layout.items.forEachIndexed { index, item ->
            assertEquals(index / 3, item.row)
            assertEquals(index % 3, item.column)
            assertTrue(layout.itemsRegion.contains(item.cell))
            layout.items.drop(index + 1).forEach { other ->
                assertTrue(!intersects(item.cell, other.cell), "${item.id} overlaps ${other.id}")
            }
        }
    }

    @Test
    fun measuredMultilineContentExpandsRegionsAndKeepsResponsiveColumns() {
        val items = listOf(
            KnapsackItem("An exceptionally descriptive item name", 123456.789, 987654.321),
            KnapsackItem("Duplicate long name", 0.125, 0.333333),
            KnapsackItem("Duplicate long name", 0.125, 0.333333)
        )
        val presentation = DedicatedFractionalKnapsackSceneBuilder.build(
            FractionalKnapsackTrace.createTrace(items, capacity = 123456.875)
        )
        val metrics = FractionalKnapsackTextExtents.GEOMETRY_DEFAULT.copy(
            itemsHeading = FractionalTextExtent(naturalWidth = 260f, wrappedHeight = 24f),
            items = presentation.sortedOccurrences.mapIndexed { index, _ ->
                FractionalKnapsackTextExtents.DEFAULT_ITEM.copy(
                    name = FractionalTextExtent(310f + index, 24f + index),
                    weightValue = FractionalTextExtent(220f, 22f),
                    taken = FractionalTextExtent(170f, 20f),
                    remaining = FractionalTextExtent(160f, 20f),
                    contribution = FractionalTextExtent(190f, 22f),
                    fraction = FractionalTextExtent(180f, 22f)
                )
            },
            usedCapacity = FractionalTextExtent(360f, 33f),
            optimalityExplanation = FractionalTextExtent(270f, 33f)
        )

        val wide = FractionalKnapsackSceneLayout.calculate(presentation, 360f, textExtents = metrics)
        val narrow = FractionalKnapsackSceneLayout.calculate(presentation, 240f, textExtents = metrics)
        assertEquals(3, wide.itemColumnCount)
        assertEquals(2, narrow.itemColumnCount)
        assertEquals(listOf("item-0", "item-1", "item-2"), wide.items.map { it.id })
        assertTrue(wide.itemsRegion.height > FractionalKnapsackSceneLayout.calculate(presentation, 360f).itemsRegion.height)
        assertTrue(wide.resultRegion.height > FractionalKnapsackSceneLayout.calculate(presentation, 360f).resultRegion.height)
        listOf(wide, narrow).forEach { layout ->
            assertTrue(layout.itemsRegion.contains(layout.itemsHeading))
            assertTrue(layout.resultRegion.contains(layout.usedCapacity))
            assertTrue(layout.resultRegion.contains(layout.optimalityExplanation))
            assertTrue(layout.resultRegion.contains(layout.optimalityBadge))
            layout.items.forEach { item ->
                assertTrue(item.card.contains(item.name))
                assertTrue(item.card.contains(item.contribution))
                assertTrue(item.card.contains(item.fraction))
                assertTrue(item.card.contains(item.gaugeTrack))
                assertTrue(!intersects(item.fraction, item.gaugeTrack))
            }
        }
    }

    @Test
    fun viewportAutoFitShowsTheCompleteNaturalScene() {
        val presentation = DedicatedFractionalKnapsackSceneBuilder.build(FractionalKnapsackTrace.createTrace())
        listOf(IntSize(360, 224), IntSize(400, 260), IntSize(460, 320)).forEach { viewport ->
            val layout = FractionalKnapsackSceneLayout.calculate(presentation, viewport.width.toFloat())
            val state = VisualizerCanvasState(initialScaleMode = VisualizerCanvasInitialScale.AUTO_FIT)
            state.updateViewportSize(viewport)
            state.updateContentSize(IntSize(layout.width.toInt(), layout.height.toInt()))
            assertTrue(layout.width * state.scale <= viewport.width + 1f)
            assertTrue(layout.height * state.scale <= viewport.height + 1f)
            assertEquals(0f, state.translationX)
            assertEquals(0f, state.translationY, "viewport=$viewport, scene=${layout.width}x${layout.height}, scale=${state.scale}")
        }
    }

    @Test
    fun compactViewportKeepsAllMeasuredFieldsAndImprovesInitialFit() {
        val presentation = DedicatedFractionalKnapsackSceneBuilder.build(FractionalKnapsackTrace.createTrace())
        val metrics = FractionalKnapsackTextExtents.GEOMETRY_DEFAULT.copy(
            capacityTitle = FractionalTextExtent(130f, 12f),
            capacityFilled = FractionalTextExtent(75f, 12f),
            capacityRemaining = FractionalTextExtent(75f, 11f),
            resultHeading = FractionalTextExtent(115f, 11f),
            resultStatus = FractionalTextExtent(125f, 11f),
            items = presentation.sortedOccurrences.map {
                FractionalKnapsackTextExtents.DEFAULT_ITEM.copy(
                    name = FractionalTextExtent(36f, 12f),
                    state = FractionalTextExtent(36f, 11f),
                    taken = FractionalTextExtent(39f, 11f),
                    remaining = FractionalTextExtent(35f, 11f)
                )
            }
        )
        listOf(IntSize(360, 224), IntSize(400, 260), IntSize(460, 320), IntSize(240, 224)).forEach { viewport ->
            val layout = FractionalKnapsackSceneLayout.calculate(
                presentation, viewport.width.toFloat(), textExtents = metrics,
                viewportHeight = viewport.height.toFloat()
            )
            val baseline = FractionalKnapsackSceneLayout.calculate(presentation, viewport.width.toFloat(), textExtents = metrics)
            assertEquals(3, layout.items.size)
            assertTrue(layout.capacityRegion.contains(layout.capacityRemaining))
            assertTrue(layout.resultRegion.contains(layout.optimalityExplanation))
            assertTrue(layout.resultRegion.contains(layout.optimalityBadge))
            layout.items.forEach { item ->
                assertTrue(item.cell.contains(item.card))
                assertTrue(item.cell.contains(item.stateLabel))
                assertTrue(item.card.contains(item.gaugeTrack))
                assertTrue(!intersects(item.name, item.stateLabel))
                assertTrue(!intersects(item.taken, item.remaining))
                assertTrue(!intersects(item.fraction, item.gaugeTrack))
            }
            assertTrue(!intersects(layout.resultHeading, layout.resultStatus))
            if (viewport.height < 300) assertTrue(layout.height < baseline.height)
            val state = VisualizerCanvasState(initialScaleMode = VisualizerCanvasInitialScale.AUTO_FIT)
            state.updateViewportSize(viewport)
            state.updateContentSize(IntSize(layout.width.toInt(), layout.height.toInt()))
            assertEquals(0f, state.translationX)
            assertEquals(0f, state.translationY)
            if (viewport == IntSize(360, 224)) assertTrue(state.scale >= .85f, "scene=${layout.width}x${layout.height}, scale=${state.scale}")
        }
    }

    private fun intersects(first: FractionalRect, second: FractionalRect): Boolean =
        first.x < second.right && first.right > second.x && first.y < second.bottom && first.bottom > second.y
}
