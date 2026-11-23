package com.example.learndsandalgorithm.presentation.learn

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GreedySceneGeometryTest {

    @Test
    fun everyGreedySnapshotHasContainedNonOverlappingSceneGeometry() {
        val viewports = representativeViewports
        val scenes = buildList<Pair<GreedyScene, Pair<Float, Float>>> {
            viewports.forEach { (width, height) ->
                GreedyFundamentalsTrace.createTrace().forEach { add(GreedyFundamentalsSceneBuilder.build(it, width, height) to (width to height)) }
                GreedyCoinChangeTrace.createTrace().forEach { add(GreedyCoinChangeSceneBuilder.build(it, width, height) to (width to height)) }
                ActivitySelectionTrace.createTrace().forEach { add(ActivitySelectionSceneBuilder.build(it, width, height) to (width to height)) }
                FractionalKnapsackTrace.createTrace().forEach { add(FractionalKnapsackSceneBuilder.build(it, width, height) to (width to height)) }
                GreedyCounterexamplesTrace.createTrace().forEach { add(GreedyCounterexampleSceneBuilder.build(it, width, height) to (width to height)) }
            }
        }

        scenes.forEach { (scene, viewport) ->
            assertTrue(scene.width > 0f && scene.height > 0f)
            assertTrue(scene.width <= viewport.first, "${scene.width} exceeds viewport ${viewport.first}")
            assertTrue(scene.height <= viewport.second || scene.items.any { it.id == "timeline-axis" }, "${scene.height} exceeds viewport ${viewport.second}")
            assertEquals(scene.items.size, scene.items.map { it.id }.toSet().size, "scene item identities must be unique")
            scene.items.forEach { item ->
                assertTrue(scene.bounds.contains(item.bounds), "${item.id} outside ${scene.bounds}: ${item.bounds}")
                assertTrue(item.bounds.width > 0f && item.bounds.height > 0f)
            }
            scene.items.forEachIndexed { index, item ->
                scene.items.drop(index + 1).forEach { other ->
                    if (!intentionalTimelineOverlay(item.id, other.id)) assertFalse(overlaps(item.bounds, other.bounds), "${item.id} overlaps ${other.id}")
                }
            }
        }
    }

    @Test
    fun eachDefaultTraceSceneFitsRepresentativeViewportConstraintsAtNaturalScale() {
        val builders = listOf<(Float, Float) -> GreedyScene>(
            { w, h -> GreedyFundamentalsSceneBuilder.build(GreedyFundamentalsTrace.createTrace().first(), w, h) },
            { w, h -> GreedyCoinChangeSceneBuilder.build(GreedyCoinChangeTrace.createTrace().first(), w, h) },
            { w, h -> ActivitySelectionSceneBuilder.build(ActivitySelectionTrace.createTrace().first(), w, h) },
            { w, h -> FractionalKnapsackSceneBuilder.build(FractionalKnapsackTrace.createTrace().first(), w, h) },
            { w, h -> GreedyCounterexampleSceneBuilder.build(GreedyCounterexamplesTrace.createTrace().first(), w, h) }
        )
        representativeViewports.forEach { (width, height) ->
            builders.forEach { build ->
                val scene = build(width, height)
                assertTrue(scene.width <= width, "${scene.width} exceeds viewport width $width")
                assertTrue(scene.height <= height || scene.items.any { it.id == "timeline-axis" }, "${scene.height} exceeds viewport height $height")
            }
        }
    }

    @Test
    fun layoutsKeepStableIdentityAndBoundsAcrossTraceSnapshots() {
        representativeViewports.forEach { (width, height) ->
            val traces = listOf(
                GreedyFundamentalsTrace.createTrace().map { GreedyFundamentalsSceneBuilder.build(it, width, height) },
                GreedyCoinChangeTrace.createTrace().map { GreedyCoinChangeSceneBuilder.build(it, width, height) },
                ActivitySelectionTrace.createTrace().map { ActivitySelectionSceneBuilder.build(it, width, height) },
                FractionalKnapsackTrace.createTrace().map { FractionalKnapsackSceneBuilder.build(it, width, height) },
                GreedyCounterexamplesTrace.createTrace().map { GreedyCounterexampleSceneBuilder.build(it, width, height) }
            )
            traces.forEach { traceScenes ->
                val commonIds = traceScenes.map { scene -> scene.items.map { it.id }.toSet() }
                    .reduce { common, ids -> common intersect ids }
                val reference = traceScenes.first().items.filter { it.id in commonIds }.associate { it.id to it.bounds }
                traceScenes.drop(1).forEach { scene ->
                    reference.forEach { (id, bounds) ->
                        val item = scene.items.firstOrNull { it.id == id }
                        assertNotNull(item, "stable scene identity $id")
                        if (id != "timeline-last-finish-label") assertEquals(bounds, item.bounds, "stable bounds for $id at $width x $height")
                    }
                }
            }
        }
    }

    @Test
    fun selectionMetersProgressWithOriginalPlaybackSnapshots() {
        val fundamentals = GreedyFundamentalsTrace.createTrace()
        val budgetFractions = fundamentals.map { step ->
            GreedyFundamentalsSceneBuilder.build(step).items.single { it.id == "budget-track" }.fillFraction
        }
        assertEquals(listOf(0f, 0f, 0f, 0.2f, 0.2f, 0.5f, 0.5f, 0.9f, 0.9f, 0.9f), budgetFractions.filterNotNull())

        val knapsack = FractionalKnapsackTrace.createTrace()
        val usedFractions = knapsack.map { step ->
            FractionalKnapsackSceneBuilder.build(step).items.single { it.id == "capacity-track" }.fillFraction
        }.filterNotNull()
        assertEquals(listOf(0f, 0f, 0f, 0.2f, 0.2f, 0.6f, 0.6f, 1f, 1f), usedFractions)
    }

    @Test
    fun fundamentalsRetainsRejectedCandidateInTerminalVisualState() {
        val trace = GreedyFundamentalsTrace.createTrace()
        val rejectedIndex = trace.lastOrNull { it.status == GreedyFundamentalsStatus.REJECTED }?.currentCandidateIndex
        val finished = trace.last()
        val scene = GreedyFundamentalsSceneBuilder.build(
            finished,
            previouslyRejectedIndex = rejectedIndex
        )
        assertEquals(GreedySceneTone.REJECTED, scene.items.single { it.id == "candidate-3" }.tone)
        assertTrue(scene.items.single { it.id == "candidate-3" }.label.contains("REJECTED"))
    }

    @Test
    fun activityTimelineUsesActualInputRangeAndPreservesEveryInterval() {
        val trace = ActivitySelectionTrace.createTrace()
        val firstScene = ActivitySelectionSceneBuilder.build(trace.first())
        val labels = firstScene.items.filter { it.id.startsWith("activity-label-") }.map { it.label }.toSet()
        assertEquals(10, labels.size)
        assertTrue(labels.any { it.startsWith("A [1, 4)") })
        assertTrue(labels.any { it.startsWith("J [12, 16)") })
        val defaultTicks = firstScene.items.filter { it.id.matches(Regex("timeline-tick-\\d+-\\d+")) }
        assertTrue(defaultTicks.any { it.label == "0" })
        assertTrue(defaultTicks.any { it.label == "16" })
        assertTrue(defaultTicks.first().bounds.left < defaultTicks.last().bounds.left)
        val minTick = defaultTicks.single { it.label == "0" }
        val maxTick = defaultTicks.single { it.label == "16" }
        assertEquals(firstScene.items.single { it.id == "activity-bar-2" }.bounds.left, minTick.bounds.left + minTick.bounds.width / 2f)
        assertEquals(firstScene.items.single { it.id == "activity-bar-9" }.bounds.right, maxTick.bounds.left + maxTick.bounds.width / 2f)
        assertTrue(firstScene.items.single { it.id == "timeline-heading" }.label.startsWith("ACTIVITY TIMELINE"))
        ActivitySelectionTrace.createTrace().forEach { step ->
            val scene = ActivitySelectionSceneBuilder.build(step, 460f, 320f)
            val axis = scene.items.single { it.id == "timeline-axis" }.bounds
            assertEquals(10, scene.items.count { it.id.startsWith("activity-track-") })
            assertEquals(10, scene.items.count { it.id.startsWith("activity-label-") })
            assertTrue(scene.items.filter { it.id.startsWith("activity-track-") }.all {
                it.bounds.left == axis.left && it.bounds.right == axis.right
            })
            assertTrue(scene.items.single { it.id == "timeline-last-finish-label" }.label
                .contains("LAST FINISH THRESHOLD: ${step.lastFinish ?: "NONE"}"))
            assertTrue(scene.items.single { it.id == "activity-selection-summary" }.label
                .contains("MAX ACTIVITIES: ${step.selectedActivities.size}"))
        }

        val custom = ActivitySelectionSceneBuilder.build(
            ActivitySelectionTrace.createTrace(listOf(Activity("X", -3, 2), Activity("Y", 2, 9))).first()
        )
        val customTicks = custom.items.filter { it.id.matches(Regex("timeline-tick-\\d+-\\d+")) }
        assertTrue(customTicks.any { it.label == "-3" })
        assertTrue(customTicks.any { it.label == "9" })
        assertTrue(custom.items.any { it.id == "activity-label-0" && it.label.startsWith("X [-3, 2)") })
        assertEquals(custom.items.single { it.id == "activity-bar-0" }.bounds.left,
            customTicks.single { it.label == "-3" }.bounds.left + customTicks.single { it.label == "-3" }.bounds.width / 2f)
    }

    @Test
    fun compactActivityTimelineFitsAllDefaultSnapshotsAtNaturalScale() {
        ActivitySelectionTrace.createTrace().forEach { step ->
            val scene = ActivitySelectionSceneBuilder.build(step, 360f, 224f)
            assertEquals(360f, scene.width)
            assertEquals(215f, scene.height)
            assertEquals(10, scene.items.count { it.id.startsWith("activity-label-") })
            assertEquals(10, scene.items.count { it.id.startsWith("activity-track-") })
            assertEquals(10, scene.items.count { it.id.startsWith("activity-bar-") || it.id.startsWith("activity-instant-marker-") })
            assertEquals(listOf("0", "8", "16"), scene.items.filter { it.id.startsWith("timeline-tick-0-") && !it.id.startsWith("timeline-tick-mark") }.map { it.label })
            assertTrue(scene.items.single { it.id == "timeline-heading" }.label.contains("EARLIEST FINISH RULE"))
            assertTrue(scene.items.single { it.id == "timeline-last-finish-label" }.label.contains("LAST FINISH THRESHOLD"))
            assertTrue(scene.items.single { it.id == "activity-selection-summary" }.label.contains(step.selectedActivities.joinToString { it.name }))
            assertTrue(scene.items.single { it.id == "activity-selection-count" }.label.contains("MAX ACTIVITIES: ${step.selectedActivities.size}"))
            assertTrue(scene.items.filter { it.label.isNotEmpty() }.all { it.fontSize >= 9 && it.textBounds.height - it.textVerticalPaddingDp >= it.fontSize + 3 })
            assertTrue(scene.items.all { scene.bounds.contains(it.bounds) })
        }
    }

    @Test
    fun activityLastFinishMarkerTracksAcceptedBoundaryAndDecisionStates() {
        val trace = ActivitySelectionTrace.createTrace()
        val firstAccepted = trace.single { it.status == ActivitySelectionStatus.SELECTED && it.currentActivity?.name == "A" }
        val rejected = trace.single { it.status == ActivitySelectionStatus.REJECTED && it.currentActivity?.name == "B" }
        val nextAccepted = trace.single { it.status == ActivitySelectionStatus.SELECTED && it.currentActivity?.name == "D" }
        val firstScene = ActivitySelectionSceneBuilder.build(firstAccepted, 460f, 320f)
        val rejectedScene = ActivitySelectionSceneBuilder.build(rejected, 460f, 320f)
        val nextScene = ActivitySelectionSceneBuilder.build(nextAccepted, 460f, 320f)
        val markerId = "timeline-last-finish-marker-0"
        val firstMarker = firstScene.items.single { it.id == markerId }
        val rejectedMarker = rejectedScene.items.single { it.id == markerId }
        val nextMarker = nextScene.items.single { it.id == markerId }

        assertEquals(4, firstAccepted.lastFinish)
        assertEquals(firstScene.items.single { it.id == "activity-bar-0" }.bounds.right,
            firstMarker.bounds.left + firstMarker.bounds.width / 2f)
        assertEquals(firstMarker.bounds, rejectedMarker.bounds)
        assertEquals(7, nextAccepted.lastFinish)
        assertTrue(nextMarker.bounds.left + nextMarker.bounds.width / 2f > firstMarker.bounds.left + firstMarker.bounds.width / 2f)
        assertEquals(nextScene.items.single { it.id == "activity-bar-3" }.bounds.right,
            nextMarker.bounds.left + nextMarker.bounds.width / 2f)
        assertTrue(firstAccepted.explanation.contains("first activity"))
        assertTrue(rejected.explanation.contains("start 3 < last finish 4"))
        assertTrue(nextAccepted.explanation.contains("start 5 ≥ prev finish 4"))
        assertTrue(rejectedScene.items.single { it.id == "activity-label-1" }.label.contains("REJECTED"))
        assertTrue(nextScene.items.single { it.id == "activity-label-3" }.label.contains("SELECTED"))
    }

    @Test
    fun negativeTimeActivitiesAcceptTheFirstThenUseTheLastSelectedFinish() {
        val trace = ActivitySelectionTrace.createTrace(
            listOf(
                Activity("A", -8, -3),
                Activity("C", -4, 0),
                Activity("B", -2, 1),
                Activity("D", 1, 2)
            )
        )
        val firstCheck = trace.single { it.status == ActivitySelectionStatus.CHECKING && it.currentActivity?.name == "A" }
        val firstAccepted = trace.single { it.status == ActivitySelectionStatus.SELECTED && it.currentActivity?.name == "A" }
        val overlapRejected = trace.single { it.status == ActivitySelectionStatus.REJECTED && it.currentActivity?.name == "C" }
        val compatibleAccepted = trace.single { it.status == ActivitySelectionStatus.SELECTED && it.currentActivity?.name == "B" }
        val nextAccepted = trace.single { it.status == ActivitySelectionStatus.SELECTED && it.currentActivity?.name == "D" }

        assertEquals(11, trace.size)
        assertEquals(null, firstCheck.lastFinish)
        assertTrue(firstCheck.explanation.contains("no previous finish boundary applies"))
        assertTrue(firstCheck.badgeText.contains("NO PREVIOUS FINISH"))
        assertEquals(listOf("A"), firstAccepted.selectedActivities.map { it.name })
        assertEquals(-3, firstAccepted.lastFinish)
        assertTrue(firstAccepted.explanation.contains("first activity"))
        assertEquals(-3, overlapRejected.lastFinish)
        assertTrue(overlapRejected.explanation.contains("start -4 < last finish -3"))
        assertEquals(-3, compatibleAccepted.selectedActivities[0].finish)
        assertEquals(1, compatibleAccepted.lastFinish)
        assertTrue(compatibleAccepted.explanation.contains("start -2 ≥ prev finish -3"))
        assertEquals(2, nextAccepted.lastFinish)
        assertEquals(listOf("A", "B", "D"), trace.last().selectedActivities.map { it.name })
        assertEquals(2, trace.last().lastFinish)

        val firstScene = ActivitySelectionSceneBuilder.build(firstAccepted, 460f, 320f)
        assertTrue(firstScene.items.none { it.id == "timeline-decision" })
        assertTrue(firstScene.items.single { it.id == "timeline-last-finish-label" }.label.contains("LAST FINISH THRESHOLD: -3"))
        assertTrue(firstScene.items.any { it.id == "timeline-last-finish-marker-0" })
    }

    @Test
    fun duplicateActivityNamesKeepStableUniqueSceneIdentitiesAndOccurrenceStates() {
        val activities = listOf(
            Activity("X", 0, 2),
            Activity("X", 0, 2),
            Activity("X", 2, 4)
        )
        val finalStep = ActivitySelectionTrace.createTrace(activities).last()
        val scene = ActivitySelectionSceneBuilder.build(finalStep, 460f, 320f)
        val activityItems = scene.items.filter { it.id.startsWith("activity-label-") }

        assertEquals(3, activityItems.map { it.id }.toSet().size)
        assertEquals(listOf("activity-label-0", "activity-label-1", "activity-label-2"), activityItems.map { it.id })
        assertTrue(activityItems[0].label.contains("SELECTED"))
        assertTrue(activityItems[1].label.contains("REJECTED"))
        assertTrue(activityItems[2].label.contains("SELECTED"))
    }

    @Test
    fun activityTimelinePreservesPendingCurrentSelectedAndRejectedStates() {
        val trace = ActivitySelectionTrace.createTrace()
        val initial = ActivitySelectionSceneBuilder.build(trace.first())
        val checking = ActivitySelectionSceneBuilder.build(trace.single {
            it.status == ActivitySelectionStatus.CHECKING && it.currentActivity?.name == "A"
        })
        val selected = ActivitySelectionSceneBuilder.build(trace.single {
            it.status == ActivitySelectionStatus.SELECTED && it.currentActivity?.name == "A"
        })
        val rejected = ActivitySelectionSceneBuilder.build(trace.single {
            it.status == ActivitySelectionStatus.REJECTED && it.currentActivity?.name == "B"
        })

        assertTrue(initial.items.filter { it.id.startsWith("activity-label-") }.all { it.label.endsWith("PENDING") })
        assertEquals(GreedySceneTone.ACTIVE, checking.items.single { it.id == "activity-label-0" }.tone)
        assertEquals(GreedySceneTone.SELECTED, selected.items.single { it.id == "activity-label-0" }.tone)
        assertEquals(GreedySceneTone.REJECTED, rejected.items.single { it.id == "activity-label-1" }.tone)
        assertTrue(rejected.items.single { it.id == "activity-selection-summary" }.label.contains("A"))
    }

    @Test
    fun activityTimelineKeepsOneScaleAndCumulativeDecisionsAcrossEverySnapshot() {
        val trace = ActivitySelectionTrace.createTrace(listOf(
            Activity("Same", -5, -2), Activity("Same", -3, 1), Activity("Later", 1, 1)
        ))
        val scenes = trace.map { ActivitySelectionSceneBuilder.build(it, 560f, 400f) }
        scenes.forEachIndexed { snapshotIndex, scene ->
            val step = trace[snapshotIndex]
            val axis = scene.items.single { it.id == "timeline-axis" }.bounds
            val bars = (0..1).map { scene.items.single { item -> item.id == "activity-bar-$it" } }
            assertEquals(listOf("activity-bar-0", "activity-bar-1"), bars.map { it.id })
            assertEquals(axis.left, bars[0].bounds.left)
            val instant = scene.items.single { it.id == "activity-instant-marker-2" }
            assertEquals(axis.right, instant.bounds.left + instant.bounds.width / 2f)
            assertEquals(8f, instant.bounds.width)
            assertTrue(scene.items.none { it.id == "activity-bar-2" })
            assertTrue(bars[0].bounds.right < bars[1].bounds.right)
            assertEquals(3, scene.items.count { it.id.startsWith("activity-label-") })
            val marker = scene.items.singleOrNull { it.id == "timeline-last-finish-marker-0" }
            val acceptedFinish = step.selectedActivities.lastOrNull()?.finish
            if (acceptedFinish == null) assertEquals(null, marker) else {
                assertNotNull(marker)
                assertEquals(axis.left + (acceptedFinish + 5) / 6f * axis.width,
                    marker.bounds.left + marker.bounds.width / 2f)
            }
            assertTrue(scene.items.single { it.id == "activity-selection-summary" }.label.contains("MAX ACTIVITIES: ${step.selectedActivities.size}"))
            val tracks = (0..2).map { scene.items.single { item -> item.id == "activity-track-$it" } }
            assertEquals(3, tracks.size)
            assertTrue(tracks.all { it.bounds.left == axis.left && it.bounds.right == axis.right })
            assertEquals(step.currentActivityIndex != null, scene.items.any { it.id == "activity-start-boundary" })
            assertTrue(scene.items.all { scene.bounds.contains(it.bounds) })
        }
        val checking = trace.indexOfFirst { it.status == ActivitySelectionStatus.CHECKING }
        val accepted = checking + 1
        assertTrue(scenes[checking].items.none { it.id == "timeline-last-finish-marker-0" })
        assertEquals(-2, trace[accepted].lastFinish)
        assertEquals(scenes[accepted].items.single { it.id == "timeline-last-finish-marker-0" }.bounds,
            scenes[accepted + 1].items.single { it.id == "timeline-last-finish-marker-0" }.bounds)
        val details = greedyDetailsProjection(null, emptyList(),
            singleProgressIndicator = true, overflowHeading = "Activity timeline detail")
        assertEquals(1, details.count { it.showsStepProgress })
        assertTrue(details.none { it.heading == GreedyRendererText.ADDITIONAL_DETAIL_HEADING })
    }

    @Test
    fun shortIntervalsUseExactEndpointsAndInstantsUsePointMarkers() {
        val trace = ActivitySelectionTrace.createTrace(listOf(
            Activity("Span", -1000, 1000), Activity("Short", 0, 1), Activity("Instant", 1, 1)
        ))
        val checking = trace.single { it.status == ActivitySelectionStatus.CHECKING && it.currentActivity?.name == "Short" }
        val scene = ActivitySelectionSceneBuilder.build(checking, 460f, 320f)
        val axis = scene.items.single { it.id == "timeline-axis" }.bounds
        val short = scene.items.single { it.id == "activity-bar-0" }.bounds
        val start = scene.items.single { it.id == "activity-start-boundary" }.bounds
        val finish = scene.items.single { it.id == "activity-finish-boundary" }.bounds
        val expectedStart = axis.left + axis.width / 2f
        val expectedFinish = expectedStart + axis.width / 2000f
        assertEquals(expectedStart, short.left)
        assertTrue(kotlin.math.abs(expectedFinish - short.right) < 0.001f)
        assertTrue(short.width < 4f)
        assertEquals(expectedStart, start.left + start.width / 2f)
        assertTrue(kotlin.math.abs(expectedFinish - (finish.left + finish.width / 2f)) < 0.001f)
        val instant = scene.items.single { it.id == "activity-instant-marker-1" }.bounds
        assertTrue(kotlin.math.abs(expectedFinish - (instant.left + instant.width / 2f)) < 0.001f)
        assertTrue(scene.items.none { it.id == "activity-bar-1" })
        assertTrue(scene.items.all { scene.bounds.contains(it.bounds) })
        assertEquals(scene.items.size, scene.items.map { it.id }.toSet().size)
    }

    @Test
    fun longCoinSequenceRemainsCompleteAndSceneFitsShortViewport() {
        val finalStep = GreedyCoinChangeTrace.createTrace(target = 100, denominations = listOf(1)).last()
        val scene = GreedyCoinChangeSceneBuilder.build(finalStep, 360f, 224f)
        val sequence = scene.items.single { it.id == "coin-sequence" }

        assertEquals(100, finalStep.selectedCoins.size)
        assertTrue(sequence.label.startsWith("SELECTED COINS (100)"))
        assertEquals(100, sequence.label.substringAfter('\n').split(" + ").size)
        assertTrue(scene.height <= 224f)
        assertTrue(scene.bounds.contains(sequence.bounds))
    }

    @Test
    fun fractionalKnapsackSceneShowsRatiosSelectionsAndFractionalPortion() {
        val steps = FractionalKnapsackTrace.createTrace()
        val sorted = FractionalKnapsackSceneBuilder.build(steps[1])
        assertTrue(sorted.items.any { it.label.contains("RATIO 6") })
        assertTrue(sorted.items.any { it.label.contains("RATIO 5") })
        assertTrue(sorted.items.any { it.label.contains("RATIO 4") })
        val fractional = FractionalKnapsackSceneBuilder.build(steps.last())
        val partial = fractional.items.single { it.id == "item-2" }
        assertTrue(partial.label.contains("FRACTION · 66.67%"))
        assertTrue(partial.label.contains("TAKEN 20 / 30 · LEFT 10"))
        assertTrue(partial.label.contains("+80 VALUE"))
        assertTrue(partial.fractionGauge)
        assertEquals(2f / 3f, partial.fillFraction)
        assertTrue(fractional.items.any { it.id == "capacity-consumption" && it.label.contains("50 / 50") && it.label.contains("240") })
        val initialCards = sorted.items.filter { it.id.startsWith("item-") }
        assertTrue(initialCards.all { it.label.contains("+0 VALUE") })
    }

    @Test
    fun decimalKnapsackInputsRemainPreciseInSceneAndFractionsMatchTrace() {
        val items = listOf(
            KnapsackItem("A", 1.5, 4.5),
            KnapsackItem("B", 2.25, 6.75),
            KnapsackItem("C", 1.5, 4.125)
        )
        val trace = FractionalKnapsackTrace.createTrace(items, capacity = 3.0)
        val result = trace.last()
        assertEquals(3.0, result.capacity)
        assertEquals(0.0, result.remainingCapacity)
        assertEquals(9.0, result.totalValue)
        assertEquals(2.0 / 3.0, result.selections.last().fraction)
        assertEquals(4.5, result.selections.last().takenValue)

        val scene = FractionalKnapsackSceneBuilder.build(result, 460f, 320f)
        assertTrue(scene.items.single { it.id == "capacity-header" }.label.contains("CAPACITY 3 · REMAINING 0 · VALUE 9"))
        val partial = scene.items.single { it.id == "item-1" }
        assertTrue(partial.label.contains("W 2.25 · V 6.75"))
        assertTrue(partial.label.contains("FRACTION · 66.67%"))
        assertTrue(partial.label.contains("TAKEN 1.5 / 2.25 · LEFT 0.75"))
        assertTrue(partial.label.contains("+4.5 VALUE"))
        assertEquals(2f / 3f, partial.fillFraction)
        assertTrue(partial.fractionGauge)
        assertEquals(partial.bounds.height - 10f, partial.textBounds.height)
        assertEquals("0.3333333333333333", formatGreedyDecimal(1.0 / 3.0))
    }

    @Test
    fun duplicateKnapsackNamesUseOccurrenceIdentityAndCorrectSelections() {
        val items = listOf(
            KnapsackItem("X", 2.0, 20.0),
            KnapsackItem("X", 3.0, 15.0),
            KnapsackItem("X", 4.0, 4.0)
        )
        val result = FractionalKnapsackTrace.createTrace(items, capacity = 6.0).last()
        val scene = FractionalKnapsackSceneBuilder.build(result, 460f, 320f)
        val cards = scene.items.filter { it.id.startsWith("item-") }

        assertEquals(listOf("item-0", "item-1", "item-2"), cards.map { it.id })
        assertTrue(cards[0].label.contains("W 2 · V 20") && cards[0].label.contains("FULL · 100%"))
        assertTrue(cards[1].label.contains("W 3 · V 15") && cards[1].label.contains("FULL · 100%"))
        assertTrue(cards[2].label.contains("W 4 · V 4") && cards[2].label.contains("FRACTION · 25%"))
        assertEquals(listOf(1f, 1f, 0.25f), cards.map { it.fillFraction })
        assertEquals(listOf("TAKEN 2 / 2 · LEFT 0", "TAKEN 3 / 3 · LEFT 0", "TAKEN 1 / 4 · LEFT 3"), cards.map { it.label.lines()[4] })
    }

    @Test
    fun identicalKnapsackItemsKeepDistinctFractionGaugeIdentities() {
        val identical = KnapsackItem("Same", 2.0, 8.0)
        val result = FractionalKnapsackTrace.createTrace(listOf(identical, identical), capacity = 3.0).last()
        val cards = FractionalKnapsackSceneBuilder.build(result, 460f, 320f).items
            .filter { it.id.matches(Regex("item-\\d+")) }

        assertEquals(listOf("item-0", "item-1"), cards.map { it.id })
        assertEquals(listOf(1f, 0.5f), cards.map { it.fillFraction })
        assertEquals(listOf("TAKEN 2 / 2 · LEFT 0", "TAKEN 1 / 2 · LEFT 1"), cards.map { it.label.lines()[4] })
        assertTrue(cards.all { it.fractionGauge })
    }

    @Test
    fun decimalFormattingPreservesMeaningfulPrecisionAndHandlesEdges() {
        assertEquals("1.234567890123", formatGreedyDecimal(1.234567890123))
        assertEquals("0.000000123456", formatGreedyDecimal(0.000000123456))
        assertEquals("-0.000000123456", formatGreedyDecimal(-0.000000123456))
        assertEquals("1234500000000", formatGreedyDecimal(1.2345E12))
        assertEquals("-1234500000000", formatGreedyDecimal(-1.2345E12))
        assertEquals("123456789.125", formatGreedyDecimal(123456789.125))
        assertEquals("12.34", formatGreedyDecimal(12.34))
        assertEquals("12", formatGreedyDecimal(12.0))
        assertEquals("0", formatGreedyDecimal(-0.0))
        assertEquals("0.3333333333333333", formatGreedyDecimal(1.0 / 3.0))
        assertEquals("33.33%", formatGreedyPercent(1.0 / 3.0))
        assertEquals("NaN", formatGreedyDecimal(Double.NaN))
        assertEquals("Infinity", formatGreedyDecimal(Double.POSITIVE_INFINITY))
        assertEquals("-Infinity", formatGreedyDecimal(Double.NEGATIVE_INFINITY))
    }

    @Test
    fun counterexampleExplanationIsDisclosedOnlyAtComparison() {
        val steps = GreedyCounterexamplesTrace.createTrace()
        val beforeComparison = steps.filter { it.status < CounterexampleStatus.COMPARE_OPTIMAL }
        beforeComparison.forEach { step ->
            val scene = GreedyCounterexampleSceneBuilder.build(step)
        assertFalse(scene.secondaryDetails.any { it.contains(GreedyCounterexampleSceneBuilder.WHY_GREEDY_FAILS) })
        assertFalse(scene.items.any { it.id == "why-greedy-fails" })
            assertTrue(scene.items.single { it.id == "optimal-result" }.label.contains("REVEALED AT COMPARISON"))
        }

        val comparison = GreedyCounterexampleSceneBuilder.build(steps[4])
        assertEquals(1, comparison.secondaryDetails.size)
        assertTrue(comparison.secondaryDetails.single().contains(GreedyCounterexampleSceneBuilder.WHY_GREEDY_FAILS))
        assertTrue(comparison.items.single { it.id == "why-greedy-fails" }.label.contains("FULL EXPLANATION IN DETAILS"))
        assertTrue(comparison.items.single { it.id == "optimal-result" }.label.contains("3 + 3"))
        assertTrue(comparison.items.single { it.id == "coin-count-comparison" }.label.contains("2 OPTIMAL COINS < 3 GREEDY COINS"))
        assertTrue(steps[4].explanation.contains("2 coins < 3 coins"))
        assertTrue(steps.last().explanation.contains("Greedy choice property fails here"))
    }

    @Test
    fun textMeasurementRoutesOverlongAndMultilineLabelsWithoutClippingThem() {
        val longExplanation = GreedyCounterexampleSceneBuilder.WHY_GREEDY_FAILS
        val scene = GreedyScene(
            width = 300f,
            height = 100f,
            items = listOf(
                GreedySceneItem("long", GreedyRect(8f, 8f, 120f, 22f), longExplanation),
                GreedySceneItem("multiline", GreedyRect(8f, 40f, 180f, 60f), "First line\nSecond line")
            )
        )
        val overflows = overflowingGreedySceneItems(
            scene,
            mapOf(
                "long" to GreedyTextMeasurement(width = 108f, height = 64f),
                "multiline" to GreedyTextMeasurement(width = 168f, height = 42f)
            )
        )
        assertEquals(listOf("long"), overflows.map { it.id })
        assertEquals(longExplanation, overflows.single().label)
        assertTrue(scene.items.single { it.id == "multiline" }.bounds.height - 8f >= 42f)
    }

    @Test
    fun productionTextMeasurementUsesNaturalWidthAndActualWrappedWidth() {
        val longLabel = GreedySceneItem("long", GreedyRect(0f, 0f, 100f, 50f), "A long unbroken label")
        val multiline = GreedySceneItem("multiline", GreedyRect(0f, 56f, 100f, 50f), "Two\nlines")
        val scene = GreedyScene(120f, 112f, listOf(longLabel, multiline))
        val calls = mutableListOf<Triple<String, GreedyTextMeasureMode, Float>>()

        val measurements = measureGreedySceneText(scene) { item, mode, availableWidth ->
            calls += Triple(item.id, mode, availableWidth)
            when (item.id to mode) {
                "long" to GreedyTextMeasureMode.NATURAL_WIDTH -> GreedyTextMeasurement(160f, 12f)
                "long" to GreedyTextMeasureMode.WRAPPED -> GreedyTextMeasurement(88f, 34f)
                "multiline" to GreedyTextMeasureMode.NATURAL_WIDTH -> GreedyTextMeasurement(38f, 12f)
                else -> GreedyTextMeasurement(88f, 34f)
            }
        }

        assertEquals(
            listOf(
                Triple("long", GreedyTextMeasureMode.NATURAL_WIDTH, Float.POSITIVE_INFINITY),
                Triple("long", GreedyTextMeasureMode.WRAPPED, 88f),
                Triple("multiline", GreedyTextMeasureMode.NATURAL_WIDTH, Float.POSITIVE_INFINITY),
                Triple("multiline", GreedyTextMeasureMode.WRAPPED, 88f)
            ),
            calls
        )
        assertEquals(GreedyTextMeasurement(width = 160f, height = 34f), measurements.getValue("long"))
        assertEquals(GreedyTextMeasurement(width = 38f, height = 34f), measurements.getValue("multiline"))
        assertEquals(listOf("long"), overflowingGreedySceneItems(scene, measurements).map { it.id })
    }

    @Test
    fun textOverflowDetectsHorizontalAndVerticalBoundsIndependently() {
        val scene = GreedyScene(
            width = 240f,
            height = 120f,
            items = listOf(
                GreedySceneItem("horizontal", GreedyRect(8f, 8f, 100f, 40f), "wide"),
                GreedySceneItem("vertical", GreedyRect(8f, 56f, 100f, 40f), "tall"),
                GreedySceneItem("fits", GreedyRect(116f, 8f, 116f, 88f), "fits")
            )
        )
        val overflow = overflowingGreedySceneItems(
            scene,
            mapOf(
                "horizontal" to GreedyTextMeasurement(width = 89f, height = 20f),
                "vertical" to GreedyTextMeasurement(width = 70f, height = 33f),
                "fits" to GreedyTextMeasurement(width = 104f, height = 80f)
            )
        )
        assertEquals(listOf("horizontal", "vertical"), overflow.map { it.id })
    }

    @Test
    fun overflowDetailsPreserveStableIdsForIdenticalLabels() {
        val first = GreedySceneItem("first-id", GreedyRect(0f, 0f, 100f, 20f), "SAME LABEL")
        val second = GreedySceneItem("second-id", GreedyRect(0f, 24f, 100f, 20f), "SAME LABEL")
        val repeatedFirst = first.copy(bounds = GreedyRect(0f, 48f, 100f, 20f))

        val details = stableGreedyOverflowItems(listOf(first, second, repeatedFirst))

        assertEquals(listOf("first-id", "second-id"), details.map { it.id })
        assertEquals(listOf("SAME LABEL", "SAME LABEL"), details.map { it.label })
    }

    @Test
    fun targetDetailsProjectionKeepsOneProgressIndicatorAndDistinctSemanticOverflowEntries() {
        val first = GreedySceneItem("activity-label-0", GreedyRect(0f, 0f, 20f, 20f), "SAME LONG LABEL")
        val second = GreedySceneItem("activity-label-1", GreedyRect(0f, 24f, 20f, 20f), "SAME LONG LABEL")
        val sections = greedyDetailsProjection(
            additionalExplanation = GreedyCounterexampleSceneBuilder.WHY_GREEDY_FAILS,
            overflowItems = listOf(first, second),
            singleProgressIndicator = true
        )

        assertEquals(1, sections.count { it.showsStepProgress })
        assertEquals("steps", sections.last().elementId)
        assertEquals(listOf("activity-label-0", "activity-label-1"), sections.map { it.elementId }.drop(1).dropLast(1))
        assertEquals(listOf("SAME LONG LABEL", "SAME LONG LABEL"), sections.map { it.explanation }.drop(1).dropLast(1))
        assertTrue(sections.all { section -> !section.heading.contains("activity-label-") })
        assertTrue(sections.single { it.elementId == "counterexample-explanation" }.explanation.contains("Local best != Global best"))

        val legacyProjection = greedyDetailsProjection(null, listOf(first, second), singleProgressIndicator = false)
        assertEquals(3, legacyProjection.count { it.showsStepProgress })
        assertTrue(legacyProjection[0].heading.contains("activity-label-0"))
    }

    @Test
    fun overflowDetailsAreClearedUntilTheNewSnapshotMeasurementArrives() {
        val oldItem = GreedySceneItem("candidate-0", GreedyRect(0f, 0f, 20f, 20f), "OLD LONG LABEL")
        val newItem = oldItem.copy(label = "NEW LONG LABEL")
        var overflowState = GreedyOverflowState(snapshotIndex = 4, items = listOf(oldItem))

        assertEquals(listOf(oldItem), visibleGreedyOverflowItems(overflowState, snapshotIndex = 4))
        assertTrue(visibleGreedyOverflowItems(overflowState, snapshotIndex = 5).isEmpty())

        overflowState = GreedyOverflowState(snapshotIndex = 5, items = listOf(newItem))
        assertEquals(listOf(newItem), visibleGreedyOverflowItems(overflowState, snapshotIndex = 5))
        assertTrue(visibleGreedyOverflowItems(overflowState, snapshotIndex = 4).isEmpty())
    }

    @Test
    fun largerCollectionsPreserveAllSnapshotContentAndGeometry() {
        val customFundamentals = GreedyFundamentalsTrace.createTrace(
            distances = (1..14).toList(),
            budget = 45
        )
        val customCoins = GreedyCoinChangeTrace.createTrace(
            target = 73,
            denominations = listOf(40, 25, 10, 5, 2, 1, 50, 20, 3, 4)
        )
        val customKnapsack = FractionalKnapsackTrace.createTrace(
            items = (1..9).map { index ->
                KnapsackItem("Item $index", index + 0.125, index * 7.375)
            },
            capacity = 23.75
        )

        representativeViewports.forEach { (width, height) ->
            customFundamentals.forEach { step ->
                val scene = GreedyFundamentalsSceneBuilder.build(step, width, height)
                assertSceneElementsContainedAndNonOverlapping(scene)
                assertEquals(14, scene.items.count { it.id.startsWith("candidate-") })
            }
            customCoins.forEach { step ->
                val scene = GreedyCoinChangeSceneBuilder.build(step, width, height)
                assertSceneElementsContainedAndNonOverlapping(scene)
                assertEquals(10, scene.items.count { it.id.startsWith("denomination-") })
                assertTrue(scene.items.single { it.id == "coin-sequence" }.label.contains(step.selectedCoins.joinToString(" + ")))
            }
            customKnapsack.forEach { step ->
                val scene = FractionalKnapsackSceneBuilder.build(step, width, height)
                assertSceneElementsContainedAndNonOverlapping(scene)
                assertEquals(9, scene.items.count { it.id.startsWith("item-") })
                assertTrue(scene.items.single { it.id == "capacity-consumption" }.label.contains(formatGreedyDecimal(step.totalValue)))
            }
        }
    }

    @Test
    fun extremeActivityTimelineRemainsContainedOnOneSharedAxis() {
        val activities = List(28) { index ->
            Activity("Activity ${index + 1} with long label", -1_000 + index * 75, 4_000 + index * 125)
        }
        val steps = ActivitySelectionTrace.createTrace(activities)
        representativeViewports.forEach { (width, height) ->
            steps.forEach { step ->
                val scene = ActivitySelectionSceneBuilder.build(step, width, height)
                assertTrue(scene.width <= width)
                assertEquals(1, scene.items.count { it.id == "timeline-axis" })
                assertEquals(activities.size, scene.items.count { it.id.startsWith("activity-label-") })
                scene.items.forEach { assertTrue(scene.bounds.contains(it.bounds), "${it.id} outside $width x $height") }
                scene.items.forEachIndexed { index, item ->
                    scene.items.drop(index + 1).forEach { other ->
                        if (!intentionalTimelineOverlay(item.id, other.id)) assertFalse(overlaps(item.bounds, other.bounds), "${item.id} overlaps ${other.id}")
                    }
                }
            }
        }
    }

    @Test
    fun renderedEducationalStringsContainNoKnownMojibake() {
        val fundamentals = GreedyFundamentalsTrace.createTrace()
        val coins = GreedyCoinChangeTrace.createTrace()
        val activities = ActivitySelectionTrace.createTrace()
        val knapsack = FractionalKnapsackTrace.createTrace()
        val counterexamples = GreedyCounterexamplesTrace.createTrace()
        val scenes = buildList {
            fundamentals.forEach { add(GreedyFundamentalsSceneBuilder.build(it)) }
            coins.forEach { add(GreedyCoinChangeSceneBuilder.build(it)) }
            activities.forEach { add(ActivitySelectionSceneBuilder.build(it)) }
            knapsack.forEach { add(FractionalKnapsackSceneBuilder.build(it)) }
            counterexamples.forEach { add(GreedyCounterexampleSceneBuilder.build(it)) }
        }
        val sceneText = scenes.flatMap { scene -> scene.items.map { it.label } + scene.secondaryDetails }
        val statusText = fundamentals.map(::greedyFundamentalsStatusText) +
            coins.map(::greedyCoinChangeStatusText) +
            activities.map(::activitySelectionStatusText) +
            knapsack.map(::fractionalKnapsackStatusText) +
            counterexamples.map(::greedyCounterexampleStatusText)
        val traceText = fundamentals.flatMap { listOf(it.explanation, it.badgeText) } +
            coins.flatMap { listOf(it.explanation, it.badgeText) } +
            activities.flatMap { listOf(it.explanation, it.badgeText) } +
            knapsack.flatMap { listOf(it.explanation, it.badgeText) } +
            counterexamples.flatMap { listOf(it.explanation, it.badgeText) }
        val overflowSample = scenes.first().items.first { it.label.isNotEmpty() }
        val targetDetails = greedyDetailsProjection(
            additionalExplanation = GreedyCounterexampleSceneBuilder.WHY_GREEDY_FAILS,
            overflowItems = listOf(overflowSample),
            singleProgressIndicator = true
        )
        val legacyDetails = greedyDetailsProjection(
            additionalExplanation = GreedyCounterexampleSceneBuilder.WHY_GREEDY_FAILS,
            overflowItems = listOf(overflowSample),
            singleProgressIndicator = false
        )
        val detailsText = (targetDetails + legacyDetails).flatMap { listOf(it.heading, it.explanation) }
        val educationalText = sceneText + traceText + statusText + GreedyRendererText.educationalStrings + detailsText +
            GreedyCounterexampleSceneBuilder.WHY_GREEDY_FAILS

        educationalText.forEach(::assertNoKnownMojibake)
    }

    @Test
    fun playbackSceneExplanationAndBadgeShareTheSameTraceSnapshot() {
        val steps = GreedyCounterexamplesTrace.createTrace()
        val playback = VisualizerPlaybackState(initialStep = 0, stepCount = steps.size)
        repeat(steps.lastIndex) { playback.next() }
        val frame = greedyPresentationSnapshot(
            playback = playback,
            steps = steps,
            sceneBuilder = GreedyCounterexampleSceneBuilder::build,
            explanation = GreedyCounterexamplesStep::explanation,
            badge = GreedyCounterexamplesStep::badgeText
        )
        assertEquals(steps.lastIndex, frame.stepIndex)
        assertEquals(steps.last(), frame.snapshot)
        assertEquals(frame.snapshot.explanation, frame.explanation)
        assertEquals(frame.snapshot.badgeText, frame.badge)
        assertTrue(frame.sceneForViewport(360f, 224f).items.any { it.label.contains("DISPROVES UNIVERSAL") })

        playback.previous()
        val previous = greedyPresentationSnapshot(
            playback,
            steps,
            GreedyCounterexampleSceneBuilder::build,
            GreedyCounterexamplesStep::explanation,
            GreedyCounterexamplesStep::badgeText
        )
        assertEquals(steps.lastIndex - 1, previous.stepIndex)
        assertEquals(steps[steps.lastIndex - 1].explanation, previous.explanation)
        assertEquals(steps[steps.lastIndex - 1].badgeText, previous.badge)

        playback.reset()
        val reset = greedyPresentationSnapshot(
            playback,
            steps,
            GreedyCounterexampleSceneBuilder::build,
            GreedyCounterexamplesStep::explanation,
            GreedyCounterexamplesStep::badgeText
        )
        assertEquals(0, reset.stepIndex)
        assertEquals(steps.first(), reset.snapshot)
    }

    @Test
    fun allOriginalTraceExplanationsRemainConnectedToPresentation() {
        val fundamentals = GreedyFundamentalsTrace.createTrace()
        val coins = GreedyCoinChangeTrace.createTrace()
        val activities = ActivitySelectionTrace.createTrace()
        val knapsack = FractionalKnapsackTrace.createTrace()
        val counterexample = GreedyCounterexamplesTrace.createTrace()

        assertEquals(10, fundamentals.size)
        assertTrue(fundamentals.first().explanation.contains("Stop distances: [2, 3, 4, 6]"))
        assertTrue(fundamentals.last().explanation.contains("globally optimal for independent costs"))
        assertEquals(9, coins.size)
        assertTrue(coins.first().explanation.contains("largest coin first"))
        assertTrue(coins.last().explanation.contains("Time complexity: O(d + k)"))
        assertEquals(23, activities.size)
        assertTrue(activities[1].explanation.contains("Earliest finish leaves maximum space"))
        assertTrue(activities.last().explanation.contains("Complexity: O(N log N)"))
        assertEquals(9, knapsack.size)
        assertTrue(knapsack[1].explanation.contains("Highest ratio gives most value per weight unit"))
        assertTrue(knapsack.last().explanation.contains("when item fractioning is allowed"))
        assertEquals(6, counterexample.size)
        assertTrue(counterexample[4].explanation.contains("3 + 3 = 6 using only 2 coins"))
        assertTrue(counterexample.last().explanation.contains("Greedy choice property fails here"))
    }

    private fun overlaps(a: GreedyRect, b: GreedyRect): Boolean =
        a.left < b.right && b.left < a.right && a.top < b.bottom && b.top < a.bottom

    private fun intentionalTimelineOverlay(a: String, b: String): Boolean =
        listOf(a, b).any { it.startsWith("timeline-last-finish-marker") || it == "activity-start-boundary" || it == "activity-finish-boundary" } ||
            ((a.startsWith("activity-track-") || a.startsWith("activity-interval-text-")) &&
                (b.startsWith("activity-bar-") || b.startsWith("activity-instant-marker-"))) ||
            ((b.startsWith("activity-track-") || b.startsWith("activity-interval-text-")) &&
                (a.startsWith("activity-bar-") || a.startsWith("activity-instant-marker-"))) ||
            (a.startsWith("activity-track-") && b.startsWith("activity-interval-text-")) ||
            (b.startsWith("activity-track-") && a.startsWith("activity-interval-text-"))

    private fun assertSceneElementsContainedAndNonOverlapping(scene: GreedyScene) {
        assertTrue(scene.items.all { scene.bounds.contains(it.bounds) })
        scene.items.forEachIndexed { index, item ->
            scene.items.drop(index + 1).forEach { other ->
                assertFalse(overlaps(item.bounds, other.bounds), "${item.id} overlaps ${other.id}")
            }
        }
    }

    private fun assertNoKnownMojibake(text: String) {
        val brokenSequences = listOf(
            "\u00c3",
            "\u00c2",
            "\u00e2\u20ac\u201c",
            "\u00e2\u20ac\u2122",
            "\u00e2\u20ac\u2013",
            "\u00e2\u20ac",
            "\ufffd"
        )
        brokenSequences.forEach { broken ->
            assertFalse(text.contains(broken), "Mojibake '$broken' in: $text")
        }
    }

    private companion object {
        val representativeViewports = listOf(360f to 224f, 460f to 320f, 560f to 400f, 960f to 640f)
    }
}
