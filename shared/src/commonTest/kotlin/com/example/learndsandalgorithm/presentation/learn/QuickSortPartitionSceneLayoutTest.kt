package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class QuickSortPartitionSceneLayoutTest {

    @Test
    fun everyDefaultStepHasCellsPointersLegendsAndAnnotationsInsideSceneBounds() {
        val frames = QuickSortPartitionSceneBuilder.build(QuickSortPartitionTrace.stepsForPartition())
        val layout = QuickSortPartitionSceneLayout.calculate(frames)
        val scene = layout.sceneBounds

        assertEquals(frames.first().meta.array.size, layout.arrayCellBounds.size)
        assertEquals(512f, layout.requiredSceneWidth)
        assertEquals(258f, layout.requiredSceneHeight)
        assertEquals(QuickPartitionRegion.entries.toSet(), layout.legendBounds.keys)
        assertEquals(frames.map { it.scene.stepIndex }.toSet(), layout.stepLayouts.keys)
        listOf(
            layout.titleBounds,
            layout.pivotInfoBounds,
            layout.arrayRegionBounds,
            layout.zoneRibbonBounds,
            layout.comparisonResultBounds
        ).forEach { assertContained(scene, it) }
        layout.legendBounds.values.forEach { assertContained(scene, it) }
        layout.arrayCellBounds.values.forEach { assertContained(scene, it) }
        layout.arrayValueBounds.values.forEach { assertContained(scene, it) }
        layout.arrayIndexBounds.values.forEach { assertContained(scene, it) }

        layout.stepLayouts.forEach { (stepIndex, stepLayout) ->
            assertContained(scene, stepLayout.iPointerBounds)
            assertContained(scene, stepLayout.jPointerBounds)
            stepLayout.regionBounds.values.forEach { assertContained(scene, it) }
            stepLayout.swapCellBounds.values.forEach { assertContained(scene, it) }
            stepLayout.partitionBoundaryLabelBounds?.let { assertContained(scene, it) }
            listOf(stepLayout.iPointerLine, stepLayout.jPointerLine, stepLayout.partitionBoundary)
                .filterNotNull()
                .forEach { line ->
                    assertPointContained(scene, line.start)
                    assertPointContained(scene, line.end)
                }
            assertEquals(stepIndex, frames[stepIndex].scene.stepIndex)
        }
    }

    @Test
    fun sentinelPointerUsesExternalGutterAndNeverCreatesAnArrayCellAtMinusOne() {
        val frames = QuickSortPartitionSceneBuilder.build(QuickSortPartitionTrace.stepsForPartition())
        val layout = QuickSortPartitionSceneLayout.calculate(frames)
        val start = layout.stepLayouts.getValue(0)
        val firstCell = layout.arrayCellBounds.getValue(0)

        assertTrue(start.iPointerBounds.right < firstCell.left)
        assertTrue(layout.arrayCellBounds.keys.all { it >= 0 })
        assertFalse(start.iPointerBounds.intersects(firstCell))
        assertFalse(start.iPointerBounds.intersects(start.jPointerBounds))
    }

    @Test
    fun coincidentIAndJPointersRemainInSeparateLabeledLanes() {
        val frames = QuickSortPartitionSceneBuilder.build(QuickSortPartitionTrace.stepsForPartition(listOf(1, 2)))
        val sameIndexFrame = frames.first { it.meta.i == it.meta.j }
        val stepLayout = QuickSortPartitionSceneLayout.calculate(frames).stepLayouts.getValue(sameIndexFrame.scene.stepIndex)

        assertEquals(stepLayout.iPointerBounds.center.x, stepLayout.jPointerBounds.center.x)
        assertFalse(stepLayout.iPointerBounds.intersects(stepLayout.jPointerBounds))
        assertTrue(stepLayout.iPointerBounds.bottom <= stepLayout.jPointerBounds.top)
    }

    @Test
    fun arrayCellsDoNotOverlapAndSequenceGeometryIsOrderIndependent() {
        val frames = QuickSortPartitionSceneBuilder.build(QuickSortPartitionTrace.stepsForPartition())
        val normal = QuickSortPartitionSceneLayout.calculate(frames)
        val reversed = QuickSortPartitionSceneLayout.calculate(frames.reversed())

        assertEquals(normal.requiredSceneWidth, reversed.requiredSceneWidth)
        assertEquals(normal.requiredSceneHeight, reversed.requiredSceneHeight)
        assertEquals(normal.arrayCellBounds, reversed.arrayCellBounds)
        assertEquals(normal.stepLayouts, reversed.stepLayouts)
        normal.arrayCellBounds.values.forEachIndexed { index, bounds ->
            normal.arrayCellBounds.values.drop(index + 1).forEach { other ->
                assertFalse(bounds.intersects(other))
            }
        }
    }

    @Test
    fun pivotBoundaryTracksItsFinalCellWithoutClippingAtEitherEnd() {
        listOf(
            listOf(1, 2, 3, 4),
            listOf(4, 3, 2, 1)
        ).forEach { input ->
            val frames = QuickSortPartitionSceneBuilder.build(QuickSortPartitionTrace.stepsForPartition(input))
            val layout = QuickSortPartitionSceneLayout.calculate(frames)
            val terminal = frames.last()
            val terminalLayout = layout.stepLayouts.getValue(terminal.scene.stepIndex)
            val line = terminalLayout.partitionBoundary

            assertEquals(terminal.meta.finalPivotPosition, terminal.meta.pivotPosition)
            assertEquals(
                layout.arrayCellBounds.getValue(terminal.meta.finalPivotPosition!!).right + 4f,
                line?.start?.x
            )
            assertNotNullInside(layout.sceneBounds, terminalLayout.partitionBoundaryLabelBounds)
        }
    }

    @Test
    fun extremeValuesAndLongerArraysExpandCellsAndRemainContained() {
        val values = listOf(Int.MIN_VALUE, 0, Int.MAX_VALUE, -1, 1, 2, 3, 4, 5, 6)
        val frames = QuickSortPartitionSceneBuilder.build(QuickSortPartitionTrace.stepsForPartition(values))
        val layout = QuickSortPartitionSceneLayout.calculate(frames)

        assertEquals(values.size, layout.arrayCellBounds.size)
        assertTrue(layout.arrayCellBounds.getValue(0).width >= "${Int.MIN_VALUE}".length * 9f + 18f)
        layout.arrayCellBounds.values.forEach { assertContained(layout.sceneBounds, it) }
        layout.arrayValueBounds.values.forEach { assertContained(layout.sceneBounds, it) }
        layout.stepLayouts.values.forEach { step ->
            step.regionBounds.values.forEach { assertContained(layout.sceneBounds, it) }
            assertContained(layout.sceneBounds, step.iPointerBounds)
            assertContained(layout.sceneBounds, step.jPointerBounds)
        }
    }

    @Test
    fun detailsAllocationUsesContentHeightAndReservesOperationOnShortAndNormalViewports() {
        val shortExplanation = quickPartitionDetailsAllocation(
            availableHeight = 180f,
            preferredSteps = 88f,
            minimumSteps = 58f,
            operation = 40f,
            minimumMetadata = 24f
        )
        val longExplanation = quickPartitionDetailsAllocation(
            availableHeight = 180f,
            preferredSteps = 220f,
            minimumSteps = 58f,
            operation = 40f,
            minimumMetadata = 24f
        )
        val normalViewport = quickPartitionDetailsAllocation(
            availableHeight = 420f,
            preferredSteps = 88f,
            minimumSteps = 58f,
            operation = 40f,
            minimumMetadata = 24f
        )

        assertEquals(88f, shortExplanation.stepsHeight)
        assertTrue(shortExplanation.stepsHeight < longExplanation.stepsHeight)
        assertTrue(longExplanation.stepsHeight < 180f)
        assertEquals(40f, shortExplanation.operationHeight)
        assertEquals(24f, shortExplanation.minimumMetadataHeight)
        assertFalse(shortExplanation.localScrollFallback)
        assertFalse(longExplanation.localScrollFallback)
        assertFalse(normalViewport.localScrollFallback)
        assertEquals(88f, normalViewport.stepsHeight)
    }

    @Test
    fun compactQuickSceneKeepsEveryEducationalBandInsideNonOverlappingNaturalBounds() {
        val frames = QuickSortPartitionSceneBuilder.build(QuickSortPartitionTrace.stepsForPartition())
        val layout = QuickSortPartitionSceneLayout.calculate(frames)
        val verticalBands = listOf(
            layout.titleBounds,
            layout.pivotInfoBounds,
            layout.legendBounds.getValue(QuickPartitionRegion.LESS_EQUAL),
            layout.zoneRibbonBounds,
            Rect2D(0f, 82f, layout.requiredSceneWidth, 38f),
            layout.arrayRegionBounds,
            Rect2D(0f, 176f, layout.requiredSceneWidth, 14f),
            Rect2D(0f, 192f, layout.requiredSceneWidth, 14f),
            layout.comparisonResultBounds
        )
        verticalBands.forEach { assertContained(layout.sceneBounds, it) }
        verticalBands.zipWithNext().forEach { (upper, lower) -> assertTrue(upper.bottom <= lower.top) }
        layout.stepLayouts.values.forEach { step ->
            assertTrue(step.iPointerBounds.bottom <= layout.arrayCellBounds.values.first().top)
            assertTrue(step.jPointerBounds.bottom <= layout.arrayCellBounds.values.first().top)
        }
        assertTrue(layout.requiredSceneHeight < 0.8f * 340f)
    }

    @Test
    fun detailsAllocationUsesLocalFallbackWhenWholeContentCannotFit() {
        val allocation = quickPartitionDetailsAllocation(
            availableHeight = 25f,
            preferredSteps = 130f,
            minimumSteps = 58f,
            operation = 40f,
            minimumMetadata = 24f
        )

        assertTrue(allocation.localScrollFallback)
        assertEquals(58f, allocation.stepsHeight)
        assertEquals(40f, allocation.operationHeight)
        assertEquals(24f, allocation.minimumMetadataHeight)
    }

    @Test
    fun quickDetailsAllocationReservesPrimaryMinimumBeforeMetadataAtBoundaryHeights() {
        val inputs = listOf(420f, 180f, 110f, 104f, 103f, 80f, 25f)
        val allocations = inputs.associateWith { height ->
            quickPartitionDetailsAllocation(height, 130f, 58f, 40f, 24f, 6f)
        }

        listOf(420f, 180f).forEach { height ->
            val allocation = allocations.getValue(height)
            assertFalse(allocation.localScrollFallback)
            assertFalse(quickPartitionMetadataNeedsExplicitAccess(allocation))
            assertTrue(allocation.stepsHeight >= 58f)
            assertEquals(40f, allocation.operationHeight)
            assertTrue(allocation.metadataHeight >= 24f)
            assertTrue(allocation.stepsHeight + allocation.operationHeight + allocation.metadataHeight + 12f <= height)
        }
        listOf(110f, 104f).forEach { height ->
            val allocation = allocations.getValue(height)
            assertFalse(allocation.localScrollFallback)
            assertTrue(quickPartitionMetadataNeedsExplicitAccess(allocation))
            assertEquals(58f, allocation.stepsHeight)
            assertEquals(40f, allocation.operationHeight)
            assertEquals(height - 104f, allocation.metadataHeight)
            assertEquals(0f, allocation.metadataGap)
        }
        listOf(103f, 80f, 25f).forEach { height ->
            val allocation = allocations.getValue(height)
            assertTrue(allocation.localScrollFallback)
            assertTrue(quickPartitionMetadataNeedsExplicitAccess(allocation))
            assertEquals(58f, allocation.stepsHeight)
            assertEquals(40f, allocation.operationHeight)
            assertEquals(0f, allocation.metadataHeight)
        }
    }

    @Test
    fun quickDetailsAllocationHandlesLongTextMultilineBadgesAndNonFiniteMeasurements() {
        val longExplanation = quickPartitionDetailsAllocation(180f, 280f, 58f, 64f, 24f, 6f)
        assertFalse(longExplanation.localScrollFallback)
        assertEquals(80f, longExplanation.stepsHeight)
        assertEquals(64f, longExplanation.operationHeight)
        assertEquals(24f, longExplanation.metadataHeight)

        val nonFinite = quickPartitionDetailsAllocation(Float.NaN, Float.POSITIVE_INFINITY, 58f, 40f, 24f)
        assertTrue(nonFinite.localScrollFallback)
        assertTrue(nonFinite.stepsHeight.isFinite() && nonFinite.operationHeight.isFinite() && nonFinite.metadataHeight.isFinite())
    }

    @Test
    fun quickMetadataAccessRetainsBothEducationalLabels() {
        assertEquals(listOf("In-Place Partitioning", "Aux Space: O(1) in-place"), quickPartitionMetadataLines())
    }

    @Test
    fun quickOperationReservationIncludesDetailsTouchTargetAndWrappedBadge() {
        val detailsButtonHeight = 48f
        val wrappedBadgeHeight = 42f
        val operationHeight = quickPartitionOperationHeight(header = 12f, button = detailsButtonHeight, badge = wrappedBadgeHeight, spacing = 2f)
        val exactPrimaryMinimum = 58f + operationHeight + 6f
        val atMinimum = quickPartitionDetailsAllocation(exactPrimaryMinimum, 130f, 58f, operationHeight, 24f, 6f)
        val belowMinimum = quickPartitionDetailsAllocation(exactPrimaryMinimum - 1f, 130f, 58f, operationHeight, 24f, 6f)

        assertTrue(operationHeight >= 12f + wrappedBadgeHeight + 2f)
        assertTrue(operationHeight >= detailsButtonHeight + wrappedBadgeHeight + 2f)
        assertFalse(atMinimum.localScrollFallback)
        assertEquals(58f, atMinimum.stepsHeight)
        assertEquals(operationHeight, atMinimum.operationHeight)
        assertTrue(belowMinimum.localScrollFallback)
        assertEquals(58f, belowMinimum.stepsHeight)
    }

    private fun assertNotNullInside(scene: Rect2D, bounds: Rect2D?) {
        assertTrue(bounds != null)
        assertContained(scene, bounds!!)
    }

    private fun assertContained(outer: Rect2D, inner: Rect2D) {
        assertTrue(inner.width >= 0f && inner.height >= 0f)
        assertTrue(inner.left >= outer.left - 0.001f)
        assertTrue(inner.top >= outer.top - 0.001f)
        assertTrue(inner.right <= outer.right + 0.001f)
        assertTrue(inner.bottom <= outer.bottom + 0.001f)
    }

    private fun assertPointContained(bounds: Rect2D, point: Point2D) {
        assertTrue(point.x >= bounds.left - 0.001f && point.x <= bounds.right + 0.001f)
        assertTrue(point.y >= bounds.top - 0.001f && point.y <= bounds.bottom + 0.001f)
    }

    private fun Rect2D.intersects(other: Rect2D): Boolean =
        left < other.right && right > other.left && top < other.bottom && bottom > other.top
}
