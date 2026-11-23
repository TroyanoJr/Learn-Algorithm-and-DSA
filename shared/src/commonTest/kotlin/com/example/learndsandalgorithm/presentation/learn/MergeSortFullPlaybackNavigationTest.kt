package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MergeSortFullPlaybackNavigationTest {

    private val trace = MergeSortFullTrace.stepsForFullSort()
    private val standard = MergeSortFullPlayback.stepsFor(trace, FullMergePlaybackMode.STANDARD)
    private val detailed = MergeSortFullPlayback.stepsFor(trace, FullMergePlaybackMode.DETAILED)

    @Test
    fun standardToDetailedMapsIntermediateStepAndCanNavigateBackToStart() {
        val standardController = controller(standard).apply { stepIndex = 19 }
        val sourceStep = standard[standardController.safeStepIndex]
        standardController.isPlaying = true
        val capturedAnchor = pauseMergeSortFullPlaybackAndCaptureAnchor(
            standardController,
            sourceStep.originalTraceIndex
        )
        val mappedIndex = MergeSortFullPlayback.nearestVisibleIndex(capturedAnchor, detailed)
        val detailedController = controller(detailed, mappedIndex)

        assertEquals(0, detailedController.initialStep)
        assertEquals(0, detailedController.minStepIndex)
        assertEquals(MergeSortFullPlayback.nearestVisibleIndex(sourceStep.originalTraceIndex, detailed), mappedIndex)
        assertEquals(sourceStep.originalTraceIndex, detailed[mappedIndex].originalTraceIndex)
        assertFalse(detailedController.isPlaying)
        assertFalse(standardController.isPlaying)
        assertTrue(detailedController.canGoPrevious)

        while (detailedController.canGoPrevious) detailedController.previous()
        assertEquals(0, detailedController.safeStepIndex)
        assertEquals(trace.first(), detailed[detailedController.safeStepIndex].traceStep)
        assertFalse(detailedController.isCompleted)
    }

    @Test
    fun detailedAdvanceMapsToFreshStandardControllerInsteadOfOldStandardPosition() {
        val detailedController = controller(detailed).apply { stepIndex = 18 }
        repeat(4) { detailedController.next() }
        val currentOriginalTraceIndex = detailed[detailedController.safeStepIndex].originalTraceIndex

        val previouslyVisitedStandardController = controller(standard).apply { stepIndex = 31 }
        val mappedIndex = MergeSortFullPlayback.nearestVisibleIndex(currentOriginalTraceIndex, standard)
        val freshStandardController = controller(standard, mappedIndex)

        assertTrue(freshStandardController !== previouslyVisitedStandardController)
        assertEquals(0, freshStandardController.initialStep)
        assertEquals(0, freshStandardController.minStepIndex)
        assertEquals(MergeSortFullPlayback.nearestVisibleIndex(currentOriginalTraceIndex, standard), mappedIndex)
        assertEquals(trace[standard[mappedIndex].originalTraceIndex], standard[mappedIndex].traceStep)
        assertEquals(31, previouslyVisitedStandardController.safeStepIndex)
        assertEquals(mappedIndex, freshStandardController.safeStepIndex)
    }

    @Test
    fun standardDetailedStandardUsesLatestDisplayedAnchorNotEarlierStandardCursor() {
        val oldStandardController = controller(standard).apply { stepIndex = 37 }
        val standardAnchor = standard[14].originalTraceIndex
        val detailedIndex = MergeSortFullPlayback.nearestVisibleIndex(standardAnchor, detailed)
        val detailedController = controller(detailed, detailedIndex)
        repeat(5) { detailedController.next() }
        val latestAnchor = detailed[detailedController.safeStepIndex].originalTraceIndex

        val returnedIndex = MergeSortFullPlayback.nearestVisibleIndex(latestAnchor, standard)
        val returnedStandardController = controller(standard, returnedIndex)

        assertEquals(MergeSortFullPlayback.nearestVisibleIndex(latestAnchor, standard), returnedIndex)
        assertEquals(trace[standard[returnedIndex].originalTraceIndex], standard[returnedIndex].traceStep)
        assertEquals(37, oldStandardController.safeStepIndex)
        assertEquals(returnedIndex, returnedStandardController.safeStepIndex)
        assertTrue(returnedStandardController.canGoPrevious)
    }

    @Test
    fun resetAfterModeSwitchReturnsToVisibleZeroAndNavigationCanReachBothEnds() {
        val standardController = controller(standard).apply { stepIndex = 23 }
        val anchor = standard[standardController.safeStepIndex].originalTraceIndex
        val mappedIndex = MergeSortFullPlayback.nearestVisibleIndex(anchor, detailed)
        val detailedController = controller(detailed, mappedIndex)

        detailedController.reset()
        assertEquals(0, detailedController.safeStepIndex)
        assertEquals(trace.first(), detailed[detailedController.safeStepIndex].traceStep)

        while (!detailedController.isCompleted) detailedController.next()
        assertEquals(detailed.lastIndex, detailedController.safeStepIndex)
        assertEquals(FullMergeEvent.COMPLETE, detailed[detailedController.safeStepIndex].traceStep.event)
        assertTrue(detailedController.isCompleted)

        val terminalStandardIndex = MergeSortFullPlayback.nearestVisibleIndex(detailed.last().originalTraceIndex, standard)
        val terminalStandardController = controller(standard, terminalStandardIndex)
        assertEquals(standard.lastIndex, terminalStandardController.safeStepIndex)
        assertTrue(terminalStandardController.isCompleted)
        terminalStandardController.next()
        assertEquals(standard.lastIndex, terminalStandardController.safeStepIndex)
    }

    @Test
    fun terminalPlaybackCannotStartAndSwitchingWhilePlayingPauses() {
        val middleController = controller(standard).apply {
            stepIndex = 12
            isPlaying = true
        }
        val anchor = pauseMergeSortFullPlaybackAndCaptureAnchor(
            middleController,
            standard[middleController.safeStepIndex].originalTraceIndex
        )
        val mappedIndex = MergeSortFullPlayback.nearestVisibleIndex(anchor, detailed)
        val detailedController = controller(detailed, mappedIndex)
        assertFalse(middleController.isPlaying)
        assertFalse(detailedController.isPlaying)

        detailedController.stepIndex = detailed.lastIndex - 1
        detailedController.next()
        assertEquals(detailed.lastIndex, detailedController.safeStepIndex)
        assertTrue(detailedController.isCompleted)
        assertFalse(detailedController.isPlaying)
        detailedController.togglePlayback()
        assertFalse(detailedController.isPlaying)
        detailedController.next()
        assertEquals(detailed.lastIndex, detailedController.safeStepIndex)
    }

    @Test
    fun modeCountsRemainFortyEightAndSixtyFive() {
        assertEquals(48, standard.size)
        assertEquals(65, detailed.size)
    }

    @Test
    fun shortExplanationUsesMeasuredHeightWhenAllMinimumRegionsFit() {
        val allocation = mergeSortFullDetailsAllocation(
            availableHeightDp = 180f,
            preferredStepsHeightDp = 74f,
            minimumStepsHeightDp = 48f,
            currentOperationHeightDp = 34f,
            minimumMetadataHeightDp = 18f
        )

        assertEquals(74f, allocation.stepsHeightDp)
        assertEquals(34f, allocation.currentOperationHeightDp)
        assertEquals(18f, allocation.minimumMetadataHeightDp)
        assertFalse(allocation.useLocalScrollFallback)
        assertTrue(allocation.stepsHeightDp + allocation.currentOperationHeightDp + allocation.minimumMetadataHeightDp + 12f <= 180f)
    }

    @Test
    fun longMultilineExplanationGetsAnInternalViewportAndRetainsItsBeginning() {
        val allocation = mergeSortFullDetailsAllocation(
            availableHeightDp = 140f,
            preferredStepsHeightDp = 260f,
            minimumStepsHeightDp = 54f,
            currentOperationHeightDp = 38f,
            minimumMetadataHeightDp = 18f
        )

        assertEquals(72f, allocation.stepsHeightDp)
        assertEquals(38f, allocation.currentOperationHeightDp)
        assertTrue(allocation.stepsHeightDp < 260f)
        assertTrue(allocation.stepsHeightDp >= 54f)
        assertFalse(allocation.useLocalScrollFallback)
        assertTrue(allocation.stepsHeightDp + allocation.currentOperationHeightDp + allocation.minimumMetadataHeightDp + 12f <= 140f)
    }

    @Test
    fun multilineCurrentOperationReservesItsCompleteMeasuredHeight() {
        val allocation = mergeSortFullDetailsAllocation(
            availableHeightDp = 180f,
            preferredStepsHeightDp = 150f,
            minimumStepsHeightDp = 52f,
            currentOperationHeightDp = 78f,
            minimumMetadataHeightDp = 18f
        )

        assertEquals(72f, allocation.stepsHeightDp)
        assertEquals(78f, allocation.currentOperationHeightDp)
        assertTrue(allocation.stepsHeightDp + allocation.currentOperationHeightDp + allocation.minimumMetadataHeightDp + 12f <= 180f)
        assertTrue(allocation.stepsHeightDp >= 52f)
        assertFalse(allocation.useLocalScrollFallback)
    }

    @Test
    fun minimumStepsHeightIncludesHeaderAndTheBeginningOfItsExplanation() {
        val allocation = mergeSortFullDetailsAllocation(
            availableHeightDp = 48f,
            preferredStepsHeightDp = 220f,
            minimumStepsHeightDp = 58f,
            currentOperationHeightDp = 34f,
            minimumMetadataHeightDp = 18f
        )

        assertEquals(58f, allocation.stepsHeightDp)
        assertTrue(allocation.stepsHeightDp > 0f)
        assertTrue(allocation.useLocalScrollFallback)
    }

    @Test
    fun currentOperationMinimumIsReservedBeforeLongStepsConsumeViewport() {
        val operationHeight = 44f
        val allocation = mergeSortFullDetailsAllocation(
            availableHeightDp = 140f,
            preferredStepsHeightDp = 320f,
            minimumStepsHeightDp = 50f,
            currentOperationHeightDp = operationHeight,
            minimumMetadataHeightDp = 18f
        )

        assertEquals(operationHeight, allocation.currentOperationHeightDp)
        assertEquals(66f, allocation.stepsHeightDp)
        assertFalse(allocation.useLocalScrollFallback)
        assertTrue(allocation.stepsHeightDp + allocation.currentOperationHeightDp + allocation.minimumMetadataHeightDp + 12f <= 140f)
    }

    @Test
    fun insufficientPanelHeightUsesLocalScrollAndKeepsPrimaryRegionsTogetherWhenFeasible() {
        val allocation = mergeSortFullDetailsAllocation(
            availableHeightDp = 98f,
            preferredStepsHeightDp = 260f,
            minimumStepsHeightDp = 44f,
            currentOperationHeightDp = 30f,
            minimumMetadataHeightDp = 18f
        )

        assertTrue(allocation.useLocalScrollFallback)
        assertEquals(62f, allocation.stepsHeightDp)
        assertTrue(allocation.stepsHeightDp + allocation.currentOperationHeightDp + 6f <= 98f)
        assertTrue(allocation.minimumMetadataHeightDp > 0f)
    }

    @Test
    fun physicallyInsufficientPanelKeepsBothPrimaryRegionsNonzeroAndMarksScrollFallback() {
        val allocation = mergeSortFullDetailsAllocation(
            availableHeightDp = 40f,
            preferredStepsHeightDp = 180f,
            minimumStepsHeightDp = 52f,
            currentOperationHeightDp = 36f,
            minimumMetadataHeightDp = 18f
        )

        assertEquals(52f, allocation.stepsHeightDp)
        assertEquals(36f, allocation.currentOperationHeightDp)
        assertTrue(allocation.stepsHeightDp > 0f)
        assertTrue(allocation.currentOperationHeightDp > 0f)
        assertTrue(allocation.useLocalScrollFallback)
    }

    @Test
    fun increasedFontScaleMeasurementsKeepPrimarySectionsReadableAndBounded() {
        val scale = 1.4f
        val allocation = mergeSortFullDetailsAllocation(
            availableHeightDp = 160f,
            preferredStepsHeightDp = 150f * scale,
            minimumStepsHeightDp = 60f * scale,
            currentOperationHeightDp = 36f * scale,
            minimumMetadataHeightDp = 16f * scale
        )

        assertTrue(allocation.stepsHeightDp >= 60f * scale)
        assertTrue(allocation.currentOperationHeightDp >= 36f * scale)
        assertTrue(allocation.useLocalScrollFallback)
        assertTrue(allocation.stepsHeightDp + allocation.currentOperationHeightDp + 6f <= 160f)
    }

    @Test
    fun allocationAccountsForBothSectionGapsAndUsesTheActualPanelHeight() {
        val allocation = mergeSortFullDetailsAllocation(
            availableHeightDp = 132f,
            preferredStepsHeightDp = 200f,
            minimumStepsHeightDp = 36f,
            currentOperationHeightDp = 34f,
            minimumMetadataHeightDp = 20f,
            sectionSpacingDp = 12f
        )

        assertEquals(54f, allocation.stepsHeightDp)
        assertFalse(allocation.useLocalScrollFallback)
        assertEquals(
            132f,
            allocation.stepsHeightDp + allocation.currentOperationHeightDp + allocation.minimumMetadataHeightDp + 24f,
            0.0001f
        )
    }

    @Test
    fun tinyOrInvalidAvailableHeightNeverCreatesZeroSizedMandatoryRegions() {
        listOf(0f, -20f, Float.NaN).forEach { availableHeight ->
            val allocation = mergeSortFullDetailsAllocation(
                availableHeightDp = availableHeight,
                preferredStepsHeightDp = 120f,
                minimumStepsHeightDp = 48f,
                currentOperationHeightDp = 32f,
                minimumMetadataHeightDp = 16f
            )
            assertTrue(allocation.stepsHeightDp >= 48f)
            assertTrue(allocation.currentOperationHeightDp >= 32f)
            assertTrue(allocation.useLocalScrollFallback)
        }
    }

    private fun controller(steps: List<FullMergePlaybackStep>, initialVisibleIndex: Int = 0) =
        createMergeSortFullPlaybackController(steps.size, initialVisibleIndex)
}
