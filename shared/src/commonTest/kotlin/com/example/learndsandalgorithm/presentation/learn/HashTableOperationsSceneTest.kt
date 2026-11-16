package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HashTableOperationsSceneTest {
    private val steps = HashTableOperationsTrace.stepsForOperations()
    private val scenes = HashTableOperationsSceneBuilder.buildScenes(steps)
    private val measuredLoadCardHeightDp = 90f

    @Test
    fun scenesPreserveStableLogicalEntryIdentitiesAndNoPhantoms() {
        assertEquals(steps.size, scenes.size)
        scenes.forEachIndexed { index, scene ->
            val visible = scene.subScenes.flatMap { it.entities }
            val expectedKeys = buildSet {
                steps[index].oldBuckets.orEmpty().filterNotNull().forEach { add(it.key) }
                steps[index].buckets.filterNotNull().forEach { add(it.key) }
            }
            assertEquals(expectedKeys, visible.mapNotNull { it.entryKey }.toSet())
            visible.forEach { entity -> assertEquals("hash-op:${entity.entryKey}", entity.elementId) }
        }
        assertFalse(scenes[11].subScenes.flatMap { it.entities }.any { it.entryKey == "DOG" })
    }

    @Test
    fun rehashConnectionsAreAccurateAndOnlyAppearAfterTransfer() {
        assertTrue(scenes[5].subScenes.flatMap { it.connections }.isEmpty())
        listOf("CAT", "FOX", "DOG").forEachIndexed { offset, key ->
            val connections = scenes[6 + offset].subScenes.flatMap { it.connections }
            assertEquals(listOf("rehash:$key"), connections.map { it.connectionKey })
        }
    }

    @Test
    fun sequenceGeometryIsStableContainedAndNonOverlapping() {
        val layouts = steps.map { HashTableOperationsSceneLayout.calculate(it, measuredLoadCardHeightDp) }
        assertTrue(layouts.all { it.requiredSceneWidth == 400f && it.requiredSceneHeight == 269f })
        assertEquals(1, layouts.map { it.loadCardBounds }.distinct().size)
        assertEquals(1, layouts.map { it.oldHeadingBounds.top }.distinct().size)
        assertEquals(1, layouts.map { it.currentHeadingBounds.top }.distinct().size)
        assertEquals(1, layouts.map { it.currentSlotBounds.values.first().top }.distinct().size)
        layouts.forEach { layout ->
            val all = listOf(layout.loadCardBounds, layout.oldHeadingBounds, layout.currentHeadingBounds) +
                layout.oldSlotBounds.values + layout.currentSlotBounds.values + layout.currentIndexBounds.values
            assertTrue(all.all { it.left >= 0f && it.top >= 0f && it.right <= layout.requiredSceneWidth && it.bottom <= layout.requiredSceneHeight })
            assertTrue(layout.loadCardBounds.bottom <= layout.oldHeadingBounds.top)
            assertTrue(layout.oldHeadingBounds.bottom <= layout.oldSlotBounds.values.minOf { it.top })
            assertTrue(layout.loadCardBounds.bottom <= layout.currentHeadingBounds.top)
            assertTrue(layout.currentHeadingBounds.bottom <= layout.currentSlotBounds.values.minOf { it.top })
            layout.currentSlotBounds.values.sortedBy { it.left }.zipWithNext().forEach { (a, b) -> assertTrue(a.right <= b.left) }
            layout.oldSlotBounds.values.sortedBy { it.left }.zipWithNext().forEach { (a, b) -> assertTrue(a.right <= b.left) }
            layout.rehashArrow?.let {
                assertTrue(layout.rehashRouteY!! > layout.currentHeadingBounds.bottom)
                assertTrue(layout.rehashRouteY < layout.currentSlotBounds.values.minOf { bounds -> bounds.top })
            }
        }
    }

    @Test
    fun representativeLandscapeFitUsesBothDimensions() {
        val sceneHeight = HashTableOperationsSceneLayout.calculate(steps.first(), measuredLoadCardHeightDp).requiredSceneHeight
        assertEquals(0.85347825f, HashTableOperationsSceneLayout.initialFitScale(341.3913f, 234f, sceneHeight))
        assertEquals(0.9739777f, HashTableOperationsSceneLayout.initialFitScale(395.65216f, 262f, sceneHeight))
        assertEquals(0.4275093f, HashTableOperationsSceneLayout.initialFitScale(400f, 115f, sceneHeight))
    }

    @Test
    fun measuredLoadCardHeightMovesEveryDownstreamRegionWithoutOverlap() {
        val shorter = HashTableOperationsSceneLayout.calculate(steps[4], measuredLoadCardHeightDp = 74f)
        val taller = HashTableOperationsSceneLayout.calculate(steps[4], measuredLoadCardHeightDp = 90f)
        val delta = 16f
        assertEquals(delta, taller.loadCardBounds.height - shorter.loadCardBounds.height)
        assertEquals(delta, taller.oldHeadingBounds.top - shorter.oldHeadingBounds.top)
        assertEquals(delta, taller.oldSlotBounds.getValue(0).top - shorter.oldSlotBounds.getValue(0).top)
        assertEquals(delta, taller.currentHeadingBounds.top - shorter.currentHeadingBounds.top)
        assertEquals(delta, taller.currentSlotBounds.getValue(0).top - shorter.currentSlotBounds.getValue(0).top)
        assertEquals(delta, taller.currentIndexBounds.getValue(0).top - shorter.currentIndexBounds.getValue(0).top)
        assertEquals(delta, taller.requiredSceneHeight - shorter.requiredSceneHeight)
        assertTrue(taller.loadCardBounds.bottom <= taller.oldHeadingBounds.top)
        assertTrue(taller.oldSlotBounds.values.maxOf { it.bottom } < taller.currentHeadingBounds.top)
    }

    @Test
    fun allThirteenCardVariantsContributeToStableMaximumHeight() {
        val measuredVariantHeightsPx = MutableList(steps.size) { index -> 60 + index }
        measuredVariantHeightsPx[4] = 96
        assertEquals(13, measuredVariantHeightsPx.size)

        val selectedMaximum = selectMaximumOperationsLoadCardHeightPx(measuredVariantHeightsPx)
        assertEquals(96, selectedMaximum)
        assertTrue(measuredVariantHeightsPx.all { it <= selectedMaximum })

        val layouts = steps.map {
            HashTableOperationsSceneLayout.calculate(it, measuredLoadCardHeightDp = selectedMaximum.toFloat())
        }
        assertTrue(layouts.all { it.loadCardBounds.height == selectedMaximum.toFloat() })
        assertTrue(layouts.all { it.requiredSceneHeight == selectedMaximum + 179f })
    }

    @Test
    fun resizeTriggerSemanticsRemainHistoricalAfterCapacityAllocation() {
        val trigger = HashTableOperationsSceneLayout.loadFactorPresentation(steps[4])
        assertEquals(3, trigger.currentSize)
        assertEquals(4, trigger.currentCapacity)
        assertEquals(0.75, trigger.currentLoadFactor)
        assertEquals(4, trigger.triggerSize)
        assertEquals(4, trigger.triggerCapacity)
        assertEquals(1.0, trigger.triggerLoadFactor)

        val allocation = HashTableOperationsSceneLayout.loadFactorPresentation(steps[5])
        assertEquals(3, allocation.currentSize)
        assertEquals(8, allocation.currentCapacity)
        assertEquals(0.37, allocation.currentLoadFactor)
        assertEquals(4, allocation.triggerSize)
        assertEquals(4, allocation.triggerCapacity)
        assertEquals(1.0, allocation.triggerLoadFactor)
    }

    @Test
    fun detailsPolicyAllocatesMeasuredContentAndProvidesAccessibleFallbacks() {
        assertEquals(1.3f, HashTableLessonDetailsLayout.OPERATIONS_VISUAL_WEIGHT)
        assertEquals(1.2f, HashTableLessonDetailsLayout.APPLICATIONS_VISUAL_WEIGHT)

        val full = HashTableLessonDetailsLayout.resolveMeasuredAllocation(
            availableHeightPx = 194,
            headerHeightPx = 60,
            fullStepsHeightPx = 120,
            stepsHeaderHeightPx = 27,
            gapPx = 3,
            stepsExplanationSpacingPx = 2
        )
        assertEquals(HashTableDetailsAllocationMode.FULL_CONTENT, full.mode)
        assertEquals(183, full.headerViewportHeightPx + full.gapPx + full.stepsViewportHeightPx)

        val scrollingExplanation = HashTableLessonDetailsLayout.resolveMeasuredAllocation(
            availableHeightPx = 194,
            headerHeightPx = 60,
            fullStepsHeightPx = 150,
            stepsHeaderHeightPx = 27,
            gapPx = 3,
            stepsExplanationSpacingPx = 2
        )
        assertEquals(HashTableDetailsAllocationMode.SCROLLING_EXPLANATION, scrollingExplanation.mode)
        assertEquals(194, scrollingExplanation.headerViewportHeightPx + scrollingExplanation.gapPx + scrollingExplanation.stepsViewportHeightPx)
        assertTrue(scrollingExplanation.stepsViewportHeightPx >= 27)

        val scrollingSections = HashTableLessonDetailsLayout.resolveMeasuredAllocation(
            availableHeightPx = 50,
            headerHeightPx = 40,
            fullStepsHeightPx = 100,
            stepsHeaderHeightPx = 27,
            gapPx = 3,
            stepsExplanationSpacingPx = 2
        )
        assertEquals(HashTableDetailsAllocationMode.SCROLLING_SECTIONS, scrollingSections.mode)
        assertEquals(50, scrollingSections.headerViewportHeightPx + scrollingSections.gapPx + scrollingSections.stepsViewportHeightPx)
        assertTrue(scrollingSections.headerViewportHeightPx > 0)
        assertTrue(scrollingSections.stepsViewportHeightPx > 0)
    }

    @Test
    fun measuredAllocatorHandlesEveryStepsHeaderBoundaryWithoutOverlap() {
        val header = 40
        val fullSteps = 80
        val stepsHeader = 20
        val sectionGap = 3
        val stepsSpacing = 2

        fun allocation(available: Int) = HashTableLessonDetailsLayout.resolveMeasuredAllocation(
            availableHeightPx = available,
            headerHeightPx = header,
            fullStepsHeightPx = fullSteps,
            stepsHeaderHeightPx = stepsHeader,
            gapPx = sectionGap,
            stepsExplanationSpacingPx = stepsSpacing
        )

        val exactFull = allocation(header + sectionGap + fullSteps)
        assertEquals(HashTableDetailsAllocationMode.FULL_CONTENT, exactFull.mode)

        val belowFull = allocation(header + sectionGap + fullSteps - 1)
        assertEquals(HashTableDetailsAllocationMode.SCROLLING_EXPLANATION, belowFull.mode)
        assertTrue(belowFull.stepsViewportHeightPx >= stepsHeader + stepsSpacing + 1)

        val equalHeaderRemaining = allocation(header + sectionGap + stepsHeader)
        val oneAboveHeaderRemaining = allocation(header + sectionGap + stepsHeader + 1)
        val oneBelowHeaderRemaining = allocation(header + sectionGap + stepsHeader - 1)
        listOf(equalHeaderRemaining, oneAboveHeaderRemaining, oneBelowHeaderRemaining).forEach { result ->
            assertEquals(HashTableDetailsAllocationMode.SCROLLING_SECTIONS, result.mode)
            assertEquals(stepsHeader + stepsSpacing + 1, result.stepsViewportHeightPx)
            assertTrue(result.headerViewportHeightPx > 0)
        }

        val smallerThanStepsHeader = allocation(stepsHeader - 1)
        assertEquals(HashTableDetailsAllocationMode.CONSTRAINED_SECTIONS, smallerThanStepsHeader.mode)
        assertTrue(smallerThanStepsHeader.stepsViewportHeightPx < stepsHeader)

        val zero = allocation(0)
        assertEquals(HashTableDetailsAllocationMode.CONSTRAINED_SECTIONS, zero.mode)
        assertEquals(0, zero.headerViewportHeightPx)
        assertEquals(0, zero.stepsViewportHeightPx)
        assertEquals(0, zero.gapPx)

        (0..130).forEach { available ->
            val result = allocation(available)
            assertTrue(result.headerViewportHeightPx >= 0)
            assertTrue(result.stepsViewportHeightPx >= 0)
            assertTrue(result.gapPx >= 0)
            val stepsTop = result.headerViewportHeightPx + result.gapPx
            val stepsBottom = stepsTop + result.stepsViewportHeightPx
            assertTrue(stepsTop >= result.headerViewportHeightPx)
            assertTrue(stepsBottom <= available)
        }
    }

    @Test
    fun longestRealDetailsTextRemainsComplete() {
        val longestBadge = steps.maxBy { it.badgeText.length }.badgeText
        val longestExplanation = steps.maxBy { it.explanation.length }.explanation
        assertEquals("RESIZE TRIGGERED (alpha=1.0)", longestBadge)
        assertTrue(steps.any { it.explanation == longestExplanation })
        val thresholdExplanation = steps[4].explanation
        assertTrue(thresholdExplanation.contains("THRESHOLD EXCEEDED!"))
        assertTrue(thresholdExplanation.endsWith("Resizing triggered."))
    }
}
