package com.example.learndsandalgorithm.presentation.learn

/** Geometry used by the Greedy lesson scenes. Coordinates are in dp and remain independent of Canvas density. */
internal data class GreedyRect(val left: Float, val top: Float, val width: Float, val height: Float) {
    val right: Float get() = left + width
    val bottom: Float get() = top + height
    fun contains(other: GreedyRect): Boolean =
        other.left >= left && other.top >= top && other.right <= right && other.bottom <= bottom
}

internal enum class GreedySceneTone { NEUTRAL, ACTIVE, SELECTED, REJECTED, ACCENT, MUTED }

internal data class GreedySceneItem(
    val id: String,
    val bounds: GreedyRect,
    val label: String,
    val tone: GreedySceneTone = GreedySceneTone.NEUTRAL,
    val fontSize: Int = 12,
    val bold: Boolean = true,
    val filled: Boolean = true,
    val fillFraction: Float? = null,
    val fractionGauge: Boolean = false,
    val textHorizontalPaddingDp: Float = 12f,
    val textVerticalPaddingDp: Float = 8f
) {
    /** Reserve the lower edge of a fractional item card for its dedicated fill gauge. */
    val textBounds: GreedyRect
        get() = if (fractionGauge) bounds.copy(height = (bounds.height - 10f).coerceAtLeast(1f)) else bounds
}

internal data class GreedyScene(
    val width: Float,
    val height: Float,
    val items: List<GreedySceneItem>,
    val secondaryDetails: List<String> = emptyList()
) {
    val bounds = GreedyRect(0f, 0f, width, height)
}

internal data class GreedyPresentationSnapshot<S>(
    val stepIndex: Int,
    val snapshot: S,
    val explanation: String,
    val badge: String,
    val sceneForViewport: (Float, Float) -> GreedyScene
)

internal data class GreedyTextMeasurement(
    val width: Float,
    val height: Float
)

internal enum class GreedyTextMeasureMode { NATURAL_WIDTH, WRAPPED }

internal data class GreedyOverflowState(
    val snapshotIndex: Int,
    val items: List<GreedySceneItem>
)

internal fun measureGreedySceneText(
    scene: GreedyScene,
    horizontalPaddingDp: Float = 12f,
    measure: (GreedySceneItem, GreedyTextMeasureMode, Float) -> GreedyTextMeasurement
): Map<String, GreedyTextMeasurement> = scene.items.filter { it.label.isNotEmpty() }.associate { item ->
    val natural = measure(item, GreedyTextMeasureMode.NATURAL_WIDTH, Float.POSITIVE_INFINITY)
    val wrappedWidth = (item.textBounds.width - minOf(horizontalPaddingDp, item.textHorizontalPaddingDp)).coerceAtLeast(1f)
    val wrapped = measure(item, GreedyTextMeasureMode.WRAPPED, wrappedWidth)
    item.id to GreedyTextMeasurement(width = natural.width, height = wrapped.height)
}

internal fun stableGreedyOverflowItems(items: List<GreedySceneItem>): List<GreedySceneItem> =
    items.distinctBy { it.id }

internal fun visibleGreedyOverflowItems(state: GreedyOverflowState, snapshotIndex: Int): List<GreedySceneItem> =
    if (state.snapshotIndex == snapshotIndex) stableGreedyOverflowItems(state.items) else emptyList()

internal fun <S> greedyPresentationSnapshot(
    playback: VisualizerPlaybackState,
    steps: List<S>,
    sceneBuilder: (S, Float, Float) -> GreedyScene,
    explanation: (S) -> String,
    badge: (S) -> String
): GreedyPresentationSnapshot<S> {
    require(steps.isNotEmpty()) { "A Greedy presentation requires at least one trace snapshot." }
    val index = playback.safeStepIndex.coerceIn(steps.indices)
    val snapshot = steps[index]
    return GreedyPresentationSnapshot(
        stepIndex = index,
        snapshot = snapshot,
        explanation = explanation(snapshot),
        badge = badge(snapshot),
        sceneForViewport = { width, height -> sceneBuilder(snapshot, width, height) }
    )
}

internal fun overflowingGreedySceneItems(
    scene: GreedyScene,
    measuredTextDp: Map<String, GreedyTextMeasurement>,
    horizontalPaddingDp: Float = 12f,
    verticalPaddingDp: Float = 8f
): List<GreedySceneItem> = scene.items.filter { item ->
    val measured = measuredTextDp[item.id] ?: GreedyTextMeasurement(0f, 0f)
    measured.width > (item.textBounds.width - minOf(horizontalPaddingDp, item.textHorizontalPaddingDp)).coerceAtLeast(0f) ||
        measured.height > (item.textBounds.height - minOf(verticalPaddingDp, item.textVerticalPaddingDp)).coerceAtLeast(0f)
}

internal fun formatGreedyDecimal(value: Double): String {
    if (!value.isFinite()) return value.toString()
    if (value == 0.0) return "0"
    val raw = value.toString()
    val exponentMarker = raw.indexOfFirst { it == 'E' || it == 'e' }
    val text = if (exponentMarker >= 0) {
        val exponent = raw.substring(exponentMarker + 1).toInt()
        if (exponent in -12..20) expandGreedyScientificNotation(raw, exponentMarker, exponent) else raw
    } else raw
    return if (text.endsWith(".0")) text.dropLast(2) else text
}

private fun expandGreedyScientificNotation(text: String, exponentMarker: Int, exponent: Int): String {
    val mantissa = text.substring(0, exponentMarker)
    val negative = mantissa.startsWith('-')
    val unsigned = mantissa.removePrefix("-")
    val decimalIndex = unsigned.indexOf('.').let { if (it >= 0) it else unsigned.length }
    val digits = unsigned.replace(".", "")
    val shiftedIndex = decimalIndex + exponent
    val expanded = when {
        shiftedIndex <= 0 -> "0." + "0".repeat(-shiftedIndex) + digits
        shiftedIndex >= digits.length -> digits + "0".repeat(shiftedIndex - digits.length)
        else -> digits.substring(0, shiftedIndex) + "." + digits.substring(shiftedIndex)
    }
    return if (negative) "-$expanded" else expanded
}

internal fun formatGreedyPercent(fraction: Double): String {
    if (!fraction.isFinite()) return fraction.toString()
    val percent = fraction * 100.0
    val rounded = kotlin.math.round(percent * 100.0) / 100.0
    return formatGreedyDecimal(rounded) + "%"
}

internal object GreedyFundamentalsSceneBuilder {
    fun build(
        step: GreedyFundamentalsStep,
        viewportWidth: Float = 560f,
        viewportHeight: Float = 400f,
        previouslyRejectedIndex: Int? = null
    ): GreedyScene {
        val width = viewportWidth.coerceAtLeast(1f)
        val innerWidth = (width - 16f).coerceAtLeast(1f)
        val items = mutableListOf<GreedySceneItem>()
        fun add(id: String, x: Float, y: Float, w: Float, h: Float, label: String, tone: GreedySceneTone = GreedySceneTone.NEUTRAL, size: Int = 12, filled: Boolean = true, fillFraction: Float? = null) {
            items += GreedySceneItem(id, GreedyRect(x, y, w, h), label, tone, size, filled = filled, fillFraction = fillFraction?.coerceIn(0f, 1f))
        }
        add("budget-header", 8f, 6f, innerWidth, 32f, "DELIVERY ROBOT · BUDGET ${step.totalBudget} · REMAINING ${step.remainingBudget}", GreedySceneTone.ACCENT, 13)
        add("budget-track", 12f, 43f, innerWidth - 8f, 20f, "USED ${step.totalBudget - step.remainingBudget} / ${step.totalBudget}", GreedySceneTone.MUTED, 11, fillFraction = if (step.totalBudget > 0) (step.totalBudget - step.remainingBudget).toFloat() / step.totalBudget else 0f)
        val n = step.sortedDistances.size.coerceAtLeast(1)
        val gap = 5f
        val columns = minOf(n, ((innerWidth + gap) / 74f).toInt().coerceAtLeast(1))
        val cellWidth = (innerWidth - gap * (columns - 1)) / columns
        val cardHeight = 50f
        val rowGap = 4f
        step.sortedDistances.forEachIndexed { index, distance ->
            val row = index / columns
            val column = index % columns
            val x = 8f + column * (cellWidth + gap)
            val y = 72f + row * (cardHeight + rowGap)
            val current = step.currentCandidateIndex == index
            val rejected = (current && step.status == GreedyFundamentalsStatus.REJECTED) ||
                (step.status == GreedyFundamentalsStatus.FINISHED && previouslyRejectedIndex == index)
            val chosen = step.chosenDistances.count { it == distance } > step.sortedDistances.take(index).count { it == distance }
            val state = when { rejected -> "REJECTED"; chosen -> "SELECTED"; current -> "EVALUATING"; else -> "PENDING" }
            val tone = when { rejected -> GreedySceneTone.REJECTED; chosen -> GreedySceneTone.SELECTED; current -> GreedySceneTone.ACTIVE; else -> GreedySceneTone.NEUTRAL }
            add("candidate-$index", x, y, cellWidth, cardHeight, "STOP ${index + 1}\nDISTANCE $distance\n$state", tone, 11)
        }
        val candidatesBottom = 72f + ((n + columns - 1) / columns) * (cardHeight + rowGap) - rowGap
        val summaryY = candidatesBottom + 5f
        add("selected-summary", 8f, summaryY, innerWidth, 30f, "SELECTED [${step.chosenDistances.joinToString()}] · COUNT ${step.chosenDistances.size}", GreedySceneTone.SELECTED, 11)
        var sceneHeight = summaryY + 36f
        if (viewportHeight >= sceneHeight + 27f) {
            add("complexity", 8f, sceneHeight, innerWidth, 22f, "SORT O(N log N) + SCAN O(N)", GreedySceneTone.ACCENT, 11)
            sceneHeight += 27f
        }
        return GreedyScene(width, sceneHeight, items)
    }
}

internal object GreedyCoinChangeSceneBuilder {
    fun build(step: GreedyCoinChangeStep, viewportWidth: Float = 560f, viewportHeight: Float = 400f): GreedyScene {
        val items = mutableListOf<GreedySceneItem>()
        fun add(id: String, x: Float, y: Float, w: Float, h: Float, label: String, tone: GreedySceneTone = GreedySceneTone.NEUTRAL, size: Int = 12) {
            items += GreedySceneItem(id, GreedyRect(x, y, w, h), label, tone, size)
        }
        val width = viewportWidth.coerceAtLeast(1f)
        val innerWidth = (width - 16f).coerceAtLeast(1f)
        add("amount-header", 8f, 6f, innerWidth, 30f, "TARGET ${step.targetAmount} · REMAINING ${step.remainingAmount} · LARGEST FIRST", GreedySceneTone.ACCENT, 12)
        val gap = 5f
        val maxColumns = if (viewportHeight < 250f) 4 else 3
        val columns = minOf(step.denominations.size.coerceAtLeast(1), maxColumns, ((innerWidth + gap) / 80f).toInt().coerceAtLeast(1))
        val cardWidth = (innerWidth - gap * (columns - 1)) / columns
        val cardHeight = 52f
        step.denominations.forEachIndexed { index, coin ->
            val isCurrent = step.currentCoinIndex == index
            val tone = when {
                isCurrent && step.status == CoinChangeStatus.SKIP_COIN -> GreedySceneTone.REJECTED
                isCurrent && step.status == CoinChangeStatus.TAKE_COIN -> GreedySceneTone.SELECTED
                isCurrent -> GreedySceneTone.ACTIVE
                index < (step.currentCoinIndex ?: -1) -> GreedySceneTone.MUTED
                else -> GreedySceneTone.NEUTRAL
            }
            val state = when (tone) {
                GreedySceneTone.REJECTED -> "TOO LARGE · SKIP"
                GreedySceneTone.SELECTED -> "TAKEN"
                GreedySceneTone.ACTIVE -> "CONSIDER"
                GreedySceneTone.MUTED -> "PASSED"
                else -> "AVAILABLE"
            }
            val row = index / columns
            val column = index % columns
            add("denomination-$index", 8f + column * (cardWidth + gap), 44f + row * 56f, cardWidth, cardHeight, "COIN $coin\n$state", tone, 11)
        }
        val rows = (step.denominations.size + columns - 1) / columns
        val sequence = if (step.selectedCoins.isEmpty()) "No coins selected yet" else step.selectedCoins.joinToString(" + ")
        val charsPerLine = ((innerWidth - 20f) / 7f).toInt().coerceAtLeast(12)
        val sequenceLines = (sequence.length + charsPerLine - 1) / charsPerLine
        val sequenceY = 44f + rows * 56f + 5f
        val naturalSequenceHeight = 34f + sequenceLines * 15f
        val availableSequenceHeight = (viewportHeight - sequenceY - 6f).coerceAtLeast(34f)
        val sequenceHeight = minOf(naturalSequenceHeight, availableSequenceHeight)
        add("coin-sequence", 8f, sequenceY, innerWidth, sequenceHeight, "SELECTED COINS (${step.selectedCoins.size})\n$sequence", GreedySceneTone.SELECTED, 11)
        return GreedyScene(width, sequenceY + sequenceHeight + 6f, items)
    }
}

internal object ActivitySelectionSceneBuilder {
    fun build(step: ActivitySelectionStep, viewportWidth: Float = 620f, viewportHeight: Float = 420f): GreedyScene {
        val activities = step.sortedActivities.ifEmpty { step.allActivities }
        val minTime = activities.minOfOrNull { it.start } ?: 0
        val maxTime = activities.maxOfOrNull { it.finish } ?: 1
        val timeSpan = (maxTime.toLong() - minTime.toLong()).coerceAtLeast(1L).toDouble()
        val width = viewportWidth.coerceAtLeast(1f)
        val innerWidth = (width - 16f).coerceAtLeast(1f)
        val compact = viewportHeight <= 224f
        val rowTop = if (compact) 43f else 72f
        val rowHeight = if (compact) 15f else 21f
        val textPadding = if (compact) 1f else 8f
        val labelWidth = minOf(154f, innerWidth * 0.36f)
        val items = mutableListOf<GreedySceneItem>()
        fun add(id: String, x: Float, y: Float, w: Float, h: Float, label: String, tone: GreedySceneTone = GreedySceneTone.NEUTRAL, size: Int = 11, filled: Boolean = true, fillFraction: Float? = null, textHorizontalPaddingDp: Float = 12f) {
            items += GreedySceneItem(id, GreedyRect(x, y, w, h), label, tone, size, filled = filled, fillFraction = fillFraction?.coerceIn(0f, 1f), textHorizontalPaddingDp = textHorizontalPaddingDp, textVerticalPaddingDp = textPadding)
        }
        val lastAcceptedFinish = step.selectedActivities.lastOrNull()?.finish
        val hasAcceptedBoundary = lastAcceptedFinish != null
        add(
            "timeline-heading", 8f, if (compact) 0f else 2f, innerWidth, if (compact) 13f else 21f,
            "ACTIVITY TIMELINE · EARLIEST FINISH RULE",
            GreedySceneTone.ACCENT, if (compact) 9 else 10
        )

        val middleTime = (minTime.toLong() + (maxTime.toLong() - minTime.toLong()) / 2L).toInt()
        val axisTimes = listOf(minTime, middleTime, maxTime).distinct()
        val maxTickWidth = axisTimes.maxOf { (it.toString().length * 6f + 4f).coerceAtLeast(18f) }
        val trackLeft = 8f + labelWidth + 8f + maxTickWidth / 2f
        val trackWidth = (width - 8f - maxTickWidth / 2f - trackLeft).coerceAtLeast(1f)
        val plotTrack = trackLeft to trackWidth
        run {
            val columnLeft = 8f
            val columnWidth = innerWidth
            val track = plotTrack
            val positions = axisTimes.map { time ->
                track.first + ((time.toLong() - minTime.toLong()).toDouble() / timeSpan).toFloat() * track.second
            }
            axisTimes.forEachIndexed { index, time ->
                val adjacentDistances = buildList {
                    if (index > 0) add(positions[index] - positions[index - 1])
                    if (index < positions.lastIndex) add(positions[index + 1] - positions[index])
                }
                val availableLabelWidth = (adjacentDistances.minOrNull()?.minus(2f) ?: columnWidth)
                    .coerceAtLeast(1f)
                val desiredLabelWidth = (time.toString().length * 6f + 4f).coerceAtLeast(18f)
                val tickWidth = minOf(desiredLabelWidth, availableLabelWidth, columnWidth).coerceAtLeast(1f)
                val tickLeft = (positions[index] - tickWidth / 2f)
                    .coerceIn(columnLeft, columnLeft + columnWidth - tickWidth)
                add("timeline-tick-0-$index", tickLeft, if (compact) 13f else 24f, tickWidth, if (compact) 13f else 20f, time.toString(), GreedySceneTone.MUTED, 9, false, textHorizontalPaddingDp = 2f)
                add("timeline-tick-mark-0-$index", positions[index] - 0.75f, if (compact) 26f else 44f, 1.5f, if (compact) 1f else 2f, "", GreedySceneTone.MUTED, 9)
            }
            add("timeline-axis", track.first, if (compact) 28f else 47f, track.second, 1.5f, "", GreedySceneTone.MUTED)
            val boundaryX = lastAcceptedFinish?.let { finish ->
                track.first + ((finish.toLong() - minTime.toLong()).toDouble() / timeSpan).toFloat() * track.second
            }
            val markerLabelWidth = minOf(170f, innerWidth)
            val markerLabelLeft = ((boundaryX ?: track.first) - markerLabelWidth / 2f)
                .coerceIn(8f, width - 8f - markerLabelWidth)
            add("timeline-last-finish-label", markerLabelLeft, if (compact) 30f else 49f, markerLabelWidth, if (compact) 13f else 21f,
                "LAST FINISH THRESHOLD: ${lastAcceptedFinish ?: "NONE"}", GreedySceneTone.SELECTED, if (compact) 9 else 10, false, textHorizontalPaddingDp = 2f)
            if (hasAcceptedBoundary) {
                val markerTop = if (compact) 43f else 70f
                add("timeline-last-finish-marker-0", requireNotNull(boundaryX) - 1.25f, markerTop, 2.5f, (rowTop + activities.size * rowHeight - markerTop).coerceAtLeast(7f), "", GreedySceneTone.SELECTED, 9)
            }
        }
        activities.forEachIndexed { index, activity ->
            val y = rowTop + index * rowHeight
            val occurrence = activities.take(index).count { it == activity }
            val selectedOccurrences = step.selectedActivities.count { it == activity }
            val rejectedOccurrences = step.rejectedActivities.count { it == activity }
            val selected = occurrence < selectedOccurrences
            val rejected = occurrence >= selectedOccurrences && occurrence < selectedOccurrences + rejectedOccurrences
            val current = step.currentActivityIndex == index
            val tone = when { selected -> GreedySceneTone.SELECTED; rejected -> GreedySceneTone.REJECTED; current -> GreedySceneTone.ACTIVE; else -> GreedySceneTone.NEUTRAL }
            val barTone = if (tone == GreedySceneTone.NEUTRAL) GreedySceneTone.ACCENT else tone
            val status = when { selected -> "SELECTED"; rejected -> "REJECTED"; current -> "CURRENT"; else -> "PENDING" }
            val track = plotTrack
            val barLeft = track.first + ((activity.start.toLong() - minTime.toLong()).toDouble() / timeSpan).toFloat() * track.second
            val barRight = track.first + ((activity.finish.toLong() - minTime.toLong()).toDouble() / timeSpan).toFloat() * track.second
            add("activity-label-$index", 8f, y, labelWidth, if (compact) 14f else 21f, "${activity.name} [${activity.start}, ${activity.finish}) · $status", tone, if (compact) 9 else 10, false)
            add("activity-track-$index", track.first, y + (if (compact) 3f else 2f), track.second, if (compact) 9f else 16f, "", GreedySceneTone.MUTED, 10)
            if (activity.start == activity.finish) {
                val instantSize = if (compact) 6f else 8f
                add("activity-instant-marker-$index", barLeft - instantSize / 2f, y + (if (compact) 4f else 6f), instantSize, instantSize, "", barTone, 10)
            } else {
                val intervalText = "${activity.start}–${activity.finish}"
                val barWidth = barRight - barLeft
                add("activity-bar-$index", barLeft, y + (if (compact) 3f else 2f), barWidth, if (compact) 9f else 16f, "", barTone, 9)
                if (barWidth >= intervalText.length * 7f + 12f) {
                    add("activity-interval-text-$index", barLeft, y, barWidth, if (compact) 14f else 20f, intervalText, tone, 9, false, textHorizontalPaddingDp = 2f)
                }
            }
            if (current) {
                add("activity-start-boundary", barLeft - 1f, y, 2f, if (compact) 14f else 19f, "", GreedySceneTone.ACTIVE)
                add("activity-finish-boundary", barRight - 1f, y, 2f, if (compact) 14f else 19f, "", GreedySceneTone.ACTIVE)
            }
        }
        val summaryY = rowTop + activities.size * rowHeight + if (compact) 2f else 4f
        val height = summaryY + if (compact) 20f else 28f
        if (compact) {
            val countWidth = minOf(128f, innerWidth * 0.4f)
            add("activity-selection-summary", 8f, summaryY, innerWidth - countWidth - 4f, 18f,
                "SELECTED: [${step.selectedActivities.joinToString { it.name }}]", GreedySceneTone.SELECTED, 9)
            add("activity-selection-count", width - 8f - countWidth, summaryY, countWidth, 18f,
                "MAX ACTIVITIES: ${step.selectedActivities.size}", GreedySceneTone.SELECTED, 9)
        } else {
            add("activity-selection-summary", 8f, summaryY, innerWidth, 22f,
                "SELECTED SET: [${step.selectedActivities.joinToString { it.name }}] · MAX ACTIVITIES: ${step.selectedActivities.size} · O(N log N)", GreedySceneTone.SELECTED, 10)
        }
        return GreedyScene(width, height, items)
    }

}

internal object FractionalKnapsackSceneBuilder {
    fun build(step: FractionalKnapsackStep, viewportWidth: Float = 560f, viewportHeight: Float = 400f): GreedyScene {
        val items = mutableListOf<GreedySceneItem>()
        fun add(id: String, x: Float, y: Float, w: Float, h: Float, label: String, tone: GreedySceneTone = GreedySceneTone.NEUTRAL, size: Int = 12, fillFraction: Float? = null, fractionGauge: Boolean = false) {
            items += GreedySceneItem(id, GreedyRect(x, y, w, h), label, tone, size, fillFraction = fillFraction?.coerceIn(0f, 1f), fractionGauge = fractionGauge)
        }
        val width = viewportWidth.coerceAtLeast(1f)
        val innerWidth = (width - 16f).coerceAtLeast(1f)
        add("capacity-header", 8f, 6f, innerWidth, 32f, "CAPACITY ${formatGreedyDecimal(step.capacity)} · REMAINING ${formatGreedyDecimal(step.remainingCapacity)} · VALUE ${formatGreedyDecimal(step.totalValue)}", GreedySceneTone.ACCENT, 12)
        val selectedWeight = step.selections.sumOf { it.takenWeight }
        add("capacity-track", 12f, 43f, innerWidth - 8f, 20f, "USED ${formatGreedyDecimal(selectedWeight)} / ${formatGreedyDecimal(step.capacity)}", GreedySceneTone.MUTED, 10, fillFraction = if (step.capacity > 0.0) (selectedWeight / step.capacity).toFloat() else 0f)
        val itemGap = 5f
        val maxColumns = if (viewportHeight < 260f) 4 else 3
        val widthColumns = ((innerWidth + itemGap) / 68f).toInt().coerceAtLeast(1)
        val columns = minOf(step.sortedItems.size.coerceAtLeast(1), maxColumns, widthColumns)
        val cardWidth = (innerWidth - itemGap * (columns - 1)) / columns
        val cardHeight = 108f
        step.sortedItems.forEachIndexed { index, item ->
            val occurrence = step.sortedItems.take(index).count { it == item }
            val selection = step.selections.filter { it.item == item }.getOrNull(occurrence)
            val isCurrent = step.currentItemIndex == index
            val tone = when {
                selection?.fraction?.let { it >= 1.0 } == true -> GreedySceneTone.SELECTED
                selection != null -> GreedySceneTone.ACCENT
                isCurrent -> GreedySceneTone.ACTIVE
                else -> GreedySceneTone.NEUTRAL
            }
            val portion = when {
                selection == null && isCurrent -> "EVALUATING"
                selection == null -> "PENDING"
                selection.fraction >= 1.0 -> "FULL · 100%"
                else -> "FRACTION · ${formatGreedyPercent(selection.fraction)}"
            }
            val takenWeight = selection?.takenWeight ?: 0.0
            val remainingItemWeight = (item.weight - takenWeight).coerceAtLeast(0.0)
            val itemValue = selection?.takenValue ?: 0.0
            val row = index / columns
            val column = index % columns
            add("item-$index", 8f + column * (cardWidth + itemGap), 72f + row * 112f, cardWidth, cardHeight,
                "ITEM ${item.name}\nW ${formatGreedyDecimal(item.weight)} · V ${formatGreedyDecimal(item.value)}\nRATIO ${formatGreedyDecimal(item.ratio)}\n$portion\nTAKEN ${formatGreedyDecimal(takenWeight)} / ${formatGreedyDecimal(item.weight)} · LEFT ${formatGreedyDecimal(remainingItemWeight)}\n+${formatGreedyDecimal(itemValue)} VALUE",
                tone, 11, fillFraction = selection?.fraction?.toFloat() ?: 0f, fractionGauge = true)
        }
        val rows = (step.sortedItems.size + columns - 1) / columns
        val summaryY = 72f + rows * 112f + 4f
        add("capacity-consumption", 8f, summaryY, innerWidth, 24f, "WEIGHT ${formatGreedyDecimal(selectedWeight)} / ${formatGreedyDecimal(step.capacity)} · VALUE ${formatGreedyDecimal(step.totalValue)}", GreedySceneTone.SELECTED, 10)
        return GreedyScene(width, summaryY + 30f, items)
    }
}

internal object GreedyCounterexampleSceneBuilder {
    const val WHY_GREEDY_FAILS = "Picking coin 4 locally leaves amount 2, which requires two 1s (total 3 coins). Picking coin 3 leaves 3, solved by one 3 (total 2 coins). Local best != Global best!"

    fun build(step: GreedyCounterexamplesStep, viewportWidth: Float = 560f, viewportHeight: Float = 400f): GreedyScene {
        val items = mutableListOf<GreedySceneItem>()
        fun add(id: String, x: Float, y: Float, w: Float, h: Float, label: String, tone: GreedySceneTone = GreedySceneTone.NEUTRAL, size: Int = 12) {
            items += GreedySceneItem(id, GreedyRect(x, y, w, h), label, tone, size)
        }
        val width = viewportWidth.coerceAtLeast(1f)
        val innerWidth = (width - 16f).coerceAtLeast(1f)
        val comparisonVisible = step.status >= CounterexampleStatus.COMPARE_OPTIMAL
        add("counterexample-header", 8f, 6f, innerWidth, 30f, "TARGET ${step.targetAmount} · COINS [${step.denominations.joinToString()}] · LARGEST FIRST", GreedySceneTone.ACCENT, 12)
        val cardGap = 6f
        val resultWidth = (innerWidth - cardGap) / 2f
        add("greedy-result", 8f, 42f, resultWidth, 68f, "GREEDY\n${if (step.greedyCoins.isEmpty()) "No coins yet" else step.greedyCoins.joinToString(" + ")}\n${step.greedyCoins.size} COINS", GreedySceneTone.REJECTED, 12)
        add("optimal-result", 8f + resultWidth + cardGap, 42f, resultWidth, 68f,
            if (comparisonVisible) "OPTIMAL\n${step.optimalCoins.joinToString(" + ")}\n${step.optimalCoins.size} COINS" else "OPTIMAL\n?\nREVEALED AT COMPARISON",
            if (comparisonVisible) GreedySceneTone.SELECTED else GreedySceneTone.MUTED, 12)
        add("coin-count-comparison", 8f, 116f, innerWidth, 30f,
            if (comparisonVisible) "${step.optimalCoins.size} OPTIMAL COINS < ${step.greedyCoins.size} GREEDY COINS" else "GREEDY PICKS SHOWN · OPTIMAL RESULT NOT YET REVEALED",
            if (comparisonVisible) GreedySceneTone.SELECTED else GreedySceneTone.MUTED, 11)
        if (comparisonVisible) add("why-greedy-fails", 8f, 152f, innerWidth, 24f, "WHY GREEDY FAILS HERE · FULL EXPLANATION IN DETAILS", GreedySceneTone.ACTIVE, 10)
        else add("why-greedy-fails-pending", 8f, 152f, innerWidth, 24f, "OPTIMAL RESULT AND EXPLANATION REVEALED AT COMPARISON", GreedySceneTone.MUTED, 10)
        add("counterexample-proof", 8f, 182f, innerWidth, 26f,
            if (step.isTerminal) "THIS INPUT DISPROVES UNIVERSAL MINIMUM-COIN OPTIMALITY" else "A COUNTEREXAMPLE REFUTES A UNIVERSAL CLAIM",
            if (step.isTerminal) GreedySceneTone.REJECTED else GreedySceneTone.ACCENT, 10)
        return GreedyScene(
            width,
            214f,
            items,
            secondaryDetails = if (comparisonVisible) listOf("WHY GREEDY FAILS HERE: $WHY_GREEDY_FAILS") else emptyList()
        )
    }
}
