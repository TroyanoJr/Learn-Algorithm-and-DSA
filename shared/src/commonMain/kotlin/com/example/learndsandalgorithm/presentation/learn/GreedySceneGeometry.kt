package com.example.learndsandalgorithm.presentation.learn

/** Geometry used by the Greedy lesson scenes. Coordinates are in dp and remain independent of Canvas density. */
internal data class GreedyRect(val left: Float, val top: Float, val width: Float, val height: Float) {
    val right: Float get() = left + width
    val bottom: Float get() = top + height
    fun contains(other: GreedyRect): Boolean =
        other.left >= left && other.top >= top && other.right <= right && other.bottom <= bottom
}

internal enum class GreedySceneTone { NEUTRAL, ACTIVE, SELECTED, REJECTED, ACCENT, MUTED }
internal enum class GreedySceneShape { ROUNDED_RECT, CIRCLE }

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
    val textVerticalPaddingDp: Float = 8f,
    val shape: GreedySceneShape = GreedySceneShape.ROUNDED_RECT,
    val progressTrack: Boolean = false,
    val wrapText: Boolean = false
) {
    /** Reserve the lower edge of a fractional item card for its dedicated fill gauge. */
    val textBounds: GreedyRect
        get() = if (fractionGauge) bounds.copy(height = (bounds.height - 10f).coerceAtLeast(1f)) else bounds
}

internal data class GreedyScene(
    val width: Float,
    val height: Float,
    val items: List<GreedySceneItem>,
    val secondaryDetails: List<String> = emptyList(),
    val constrainedTextMeasurement: Boolean = false
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
    val height: Float,
    val horizontalOverflow: Boolean = false
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
    val availableWidth = (item.textBounds.width - if (scene.constrainedTextMeasurement) item.textHorizontalPaddingDp else minOf(horizontalPaddingDp, item.textHorizontalPaddingDp)).coerceAtLeast(1f)
    if (scene.constrainedTextMeasurement) {
        val mode = if (item.wrapText) GreedyTextMeasureMode.WRAPPED else GreedyTextMeasureMode.NATURAL_WIDTH
        item.id to measure(item, mode, if (item.wrapText) availableWidth else Float.POSITIVE_INFINITY)
    } else {
        val natural = measure(item, GreedyTextMeasureMode.NATURAL_WIDTH, Float.POSITIVE_INFINITY)
        val wrapped = measure(item, GreedyTextMeasureMode.WRAPPED, availableWidth)
        item.id to GreedyTextMeasurement(width = natural.width, height = wrapped.height)
    }
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
    measured.horizontalOverflow ||
        measured.width > (item.textBounds.width - if (scene.constrainedTextMeasurement) item.textHorizontalPaddingDp else minOf(horizontalPaddingDp, item.textHorizontalPaddingDp)).coerceAtLeast(0f) ||
        measured.height > (item.textBounds.height - if (scene.constrainedTextMeasurement) item.textVerticalPaddingDp else minOf(verticalPaddingDp, item.textVerticalPaddingDp)).coerceAtLeast(0f)
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

/** Enlarge only the four compact Greedy scenes when the visual pane has more room. */
private fun GreedyScene.useAvailableGreedyPane(viewportWidth: Float, viewportHeight: Float): GreedyScene {
    val factor = minOf(viewportWidth / 360f, viewportHeight / 224f).coerceAtLeast(1f)
    if (factor <= 1f) return this
    return copy(
        width = width * factor,
        height = height * factor,
        items = items.map { item ->
            item.copy(
                bounds = GreedyRect(item.bounds.left * factor, item.bounds.top * factor, item.bounds.width * factor, item.bounds.height * factor),
                fontSize = kotlin.math.round(item.fontSize * factor).toInt().coerceAtLeast(item.fontSize),
                textHorizontalPaddingDp = item.textHorizontalPaddingDp * factor,
                textVerticalPaddingDp = item.textVerticalPaddingDp * factor
            )
        }
    )
}

internal object GreedyFundamentalsSceneBuilder {
    fun build(
        step: GreedyFundamentalsStep,
        viewportWidth: Float = 560f,
        viewportHeight: Float = 400f,
        previouslyRejectedIndex: Int? = null
    ): GreedyScene {
        val width = viewportWidth.coerceAtMost(360f).coerceAtLeast(1f)
        val innerWidth = (width - 16f).coerceAtLeast(1f)
        val items = mutableListOf<GreedySceneItem>()
        fun add(id: String, x: Float, y: Float, w: Float, h: Float, label: String, tone: GreedySceneTone = GreedySceneTone.NEUTRAL, size: Int = 10, filled: Boolean = true, fillFraction: Float? = null, pad: Float = 4f, progressTrack: Boolean = false) {
            items += GreedySceneItem(id, GreedyRect(x, y, w, h), label, tone, size, filled = filled, fillFraction = fillFraction?.coerceIn(0f, 1f), textHorizontalPaddingDp = pad, textVerticalPaddingDp = pad, progressTrack = progressTrack)
        }
        add("budget-header", 8f, 4f, innerWidth * 0.65f, 24f, "DELIVERY ROBOT DISTANCE BUDGET", GreedySceneTone.ACCENT)
        add("budget-remaining", 10f + innerWidth * 0.65f, 4f, innerWidth * 0.35f - 2f, 24f, "REMAINING ${step.remainingBudget} / ${step.totalBudget}", GreedySceneTone.ACCENT)
        add("budget-track", 8f, 33f, innerWidth, 12f, "", GreedySceneTone.ACCENT, fillFraction = if (step.totalBudget > 0) step.remainingBudget.toFloat() / step.totalBudget else 0f, progressTrack = true)
        add("budget-used", 8f, 49f, innerWidth, 18f, "USED ${step.totalBudget - step.remainingBudget} / ${step.totalBudget}", GreedySceneTone.MUTED)
        add("candidate-heading", 8f, 69f, innerWidth, 20f, "CANDIDATE STOPS · SHORTEST FIRST", GreedySceneTone.ACCENT)
        val n = step.sortedDistances.size.coerceAtLeast(1)
        val gap = 5f
        val columns = minOf(n, ((innerWidth + gap) / 74f).toInt().coerceAtLeast(1))
        val cellWidth = (innerWidth - gap * (columns - 1)) / columns
        val cardHeight = 38f
        val rowPitch = 59f
        step.sortedDistances.forEachIndexed { index, distance ->
            val row = index / columns
            val column = index % columns
            val x = 8f + column * (cellWidth + gap)
            val y = 108f + row * rowPitch
            val current = step.currentCandidateIndex == index
            val rejected = (current && step.status == GreedyFundamentalsStatus.REJECTED) ||
                (step.status == GreedyFundamentalsStatus.FINISHED && previouslyRejectedIndex == index)
            val chosen = step.chosenDistances.count { it == distance } > step.sortedDistances.take(index).count { it == distance }
            val state = when { rejected -> "REJECTED"; chosen -> "SELECTED"; current -> "EVALUATING"; else -> "PENDING" }
            val tone = when { rejected -> GreedySceneTone.REJECTED; chosen -> GreedySceneTone.SELECTED; current -> GreedySceneTone.ACTIVE; else -> GreedySceneTone.NEUTRAL }
            add("candidate-state-$index", x, y - 17f, cellWidth, 16f, state, tone, 9, pad = 2f)
            add("candidate-card-$index", x, y, cellWidth, cardHeight, "", tone)
            add("candidate-id-$index", x, y, cellWidth, 16f, "STOP ${index + 1}", tone, 9, filled = false)
            add("candidate-$index", x, y + 17f, cellWidth, 20f, "DISTANCE $distance", tone, 11, filled = false)
        }
        val candidatesBottom = 108f + (((n + columns - 1) / columns) - 1) * rowPitch + cardHeight
        val summaryY = candidatesBottom + 5f
        add("selected-summary", 8f, summaryY, innerWidth * 0.72f, 29f, "SELECTED [${step.chosenDistances.joinToString()}] · USED ${step.totalBudget - step.remainingBudget}", GreedySceneTone.SELECTED)
        add("selected-count", 10f + innerWidth * 0.72f, summaryY, innerWidth * 0.28f - 2f, 29f, "COUNT ${step.chosenDistances.size}", GreedySceneTone.SELECTED)
        val complexityY = summaryY + 33f
        add("complexity", 8f, complexityY, innerWidth, 20f, "SORT O(N log N) + SCAN O(N)", GreedySceneTone.ACCENT)
        val sceneHeight = complexityY + 24f
        return GreedyScene(width, sceneHeight, items, constrainedTextMeasurement = true).useAvailableGreedyPane(viewportWidth, viewportHeight)
    }
}

internal object GreedyCoinChangeSceneBuilder {
    fun build(step: GreedyCoinChangeStep, viewportWidth: Float = 560f, viewportHeight: Float = 400f): GreedyScene {
        val items = mutableListOf<GreedySceneItem>()
        fun add(id: String, x: Float, y: Float, w: Float, h: Float, label: String, tone: GreedySceneTone = GreedySceneTone.NEUTRAL, size: Int = 10, shape: GreedySceneShape = GreedySceneShape.ROUNDED_RECT, pad: Float = 4f) {
            items += GreedySceneItem(id, GreedyRect(x, y, w, h), label, tone, size, textHorizontalPaddingDp = pad, textVerticalPaddingDp = pad, shape = shape)
        }
        val width = viewportWidth.coerceAtMost(360f).coerceAtLeast(1f)
        val innerWidth = (width - 16f).coerceAtLeast(1f)
        add("amount-header", 8f, 4f, innerWidth * 0.64f, 27f, "TARGET ${step.targetAmount} · LARGEST FIRST", GreedySceneTone.ACCENT)
        add("remaining-amount", 10f + innerWidth * 0.64f, 4f, innerWidth * 0.36f - 2f, 27f, "REMAINING ${step.remainingAmount}", GreedySceneTone.ACTIVE)
        add("denominations-heading", 8f, 36f, innerWidth, 21f, "DENOMINATIONS · LARGEST FIRST: [${step.denominations.joinToString()}]", GreedySceneTone.ACCENT)
        val columns = minOf(step.denominations.size.coerceAtLeast(1), (innerWidth / 54f).toInt().coerceAtLeast(1))
        val cellWidth = innerWidth / columns
        val denominationRows = (step.denominations.size + columns - 1) / columns
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
            val center = 8f + (column + 0.5f) * cellWidth
            val top = 74f + row * 60f
            add("denomination-state-$index", 9f + column * cellWidth, top - 17f, cellWidth - 2f, 16f, state, tone, 9, pad = 2f)
            add("denomination-$index", center - 18f, top, 36f, 36f, coin.toString(), tone, 13, GreedySceneShape.CIRCLE)
        }
        val sequenceY = 74f + denominationRows * 60f - 19f
        val sequence = if (step.selectedCoins.isEmpty()) "No coins selected yet" else step.selectedCoins.joinToString(" + ")
        add("coin-sequence", 8f, sequenceY, innerWidth, 30f, if (step.selectedCoins.isEmpty()) "SELECTED COINS · $sequence" else "SELECTED COINS · CHRONOLOGICAL ORDER", GreedySceneTone.SELECTED)
        val coinSize = 34f
        val selectedColumns = (innerWidth / 38f).toInt().coerceAtLeast(1)
        val maxSelectionCount = if (step.targetAmount > 0 && step.denominations.any { it > 0 }) {
            var amount = step.targetAmount
            var count = 0
            step.denominations.filter { it > 0 }.forEach { coin -> count += amount / coin; amount %= coin }
            maxOf(count, step.selectedCoins.size)
        } else step.selectedCoins.size
        val selectedY = sequenceY + 34f
        step.selectedCoins.forEachIndexed { index, coin ->
            val col = index % selectedColumns
            val row = index / selectedColumns
            add("selected-coin-$index", 8f + col * 38f, selectedY + row * 38f, coinSize, coinSize, coin.toString(), GreedySceneTone.SELECTED, 12, GreedySceneShape.CIRCLE)
        }
        val selectedRows = (maxSelectionCount + selectedColumns - 1) / selectedColumns
        val summaryY = selectedY + maxOf(1, selectedRows) * 38f
        add("selected-coin-summary", 8f, summaryY, innerWidth, 19f, "${step.selectedCoins.size} COINS · AMOUNT ${step.targetAmount - step.remainingAmount} / ${step.targetAmount}", GreedySceneTone.SELECTED)
        return GreedyScene(width, summaryY + 23f, items, constrainedTextMeasurement = true).useAvailableGreedyPane(viewportWidth, viewportHeight)
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
        fun add(id: String, x: Float, y: Float, w: Float, h: Float, label: String, tone: GreedySceneTone = GreedySceneTone.NEUTRAL, size: Int = 10, fillFraction: Float? = null, pad: Float = 3f, progressTrack: Boolean = false, filled: Boolean = true, wrapText: Boolean = false) {
            items += GreedySceneItem(id, GreedyRect(x, y, w, h), label, tone, size, filled = filled, fillFraction = fillFraction?.coerceIn(0f, 1f), textHorizontalPaddingDp = pad, textVerticalPaddingDp = pad, progressTrack = progressTrack, wrapText = wrapText)
        }
        val width = viewportWidth.coerceAtMost(360f).coerceAtLeast(1f)
        val innerWidth = (width - 16f).coerceAtLeast(1f)
        add("capacity-header", 8f, 4f, innerWidth, 24f, "KNAPSACK CAPACITY ${formatGreedyDecimal(step.capacity)} · REMAINING ${formatGreedyDecimal(step.remainingCapacity)}", GreedySceneTone.ACCENT)
        val selectedWeight = step.selections.sumOf { it.takenWeight }
        add("capacity-track", 8f, 31f, innerWidth, 10f, "", GreedySceneTone.ACCENT, fillFraction = if (step.capacity > 0.0) (selectedWeight / step.capacity).toFloat() else 0f, progressTrack = true)
        add("capacity-values", 8f, 44f, innerWidth, 18f, "FILLED ${formatGreedyDecimal(selectedWeight)} / ${formatGreedyDecimal(step.capacity)} · REMAINING ${formatGreedyDecimal(step.remainingCapacity)}", GreedySceneTone.MUTED)
        add("items-heading", 8f, 65f, innerWidth, 18f, "ITEMS SORTED BY VALUE / WEIGHT RATIO", GreedySceneTone.ACCENT)
        val itemGap = 5f
        val columns = minOf(step.sortedItems.size.coerceAtLeast(1), ((innerWidth + itemGap) / 105f).toInt().coerceAtLeast(1))
        val cardWidth = (innerWidth - itemGap * (columns - 1)) / columns
        val rowPitch = 122f
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
            val x = 8f + column * (cardWidth + itemGap)
            val y = 85f + row * rowPitch
            add("item-card-$index", x, y, cardWidth, 117f, "", tone)
            add("item-name-$index", x, y, cardWidth, 18f, "ITEM ${item.name}", tone, 10, filled = false)
            add("item-stats-$index", x, y + 19f, cardWidth, 26f, "W ${formatGreedyDecimal(item.weight)} · V ${formatGreedyDecimal(item.value)}\nRATIO ${formatGreedyDecimal(item.ratio)}", GreedySceneTone.NEUTRAL, 9, pad = 1f, filled = false, wrapText = true)
            add("item-state-$index", x, y + 46f, cardWidth, 17f, portion, tone, 9, filled = false)
            add("item-weight-$index", x, y + 64f, cardWidth, 26f, "TAKEN ${formatGreedyDecimal(takenWeight)} / ${formatGreedyDecimal(item.weight)}\nLEFT ${formatGreedyDecimal(remainingItemWeight)}", GreedySceneTone.NEUTRAL, 9, pad = 1f, filled = false, wrapText = true)
            add("item-value-$index", x, y + 91f, cardWidth, 17f, "+${formatGreedyDecimal(itemValue)} VALUE", tone, 9, filled = false)
            add("item-fraction-$index", x, y + 110f, cardWidth, 6f, "", tone, fillFraction = selection?.fraction?.toFloat() ?: 0f, progressTrack = true)
        }
        val rows = (step.sortedItems.size + columns - 1) / columns
        val summaryY = 85f + (rows - 1) * rowPitch + 118f
        add("capacity-consumption", 8f, summaryY, (innerWidth - 4f) * 0.55f, 18f, "VALUE ${formatGreedyDecimal(step.totalValue)} · WEIGHT ${formatGreedyDecimal(selectedWeight)} / ${formatGreedyDecimal(step.capacity)}", GreedySceneTone.SELECTED, 9)
        add("optimality", 12f + (innerWidth - 4f) * 0.55f, summaryY, (innerWidth - 4f) * 0.45f, 18f, if (step.isTerminal) "OPTIMAL FRACTIONAL RESULT" else "RATIO RULE: OPTIMAL FOR FRACTIONS", GreedySceneTone.SELECTED, 9)
        return GreedyScene(width, summaryY + 21f, items, constrainedTextMeasurement = true).useAvailableGreedyPane(viewportWidth, viewportHeight)
    }
}

internal object GreedyCounterexampleSceneBuilder {
    const val WHY_GREEDY_FAILS = "Picking coin 4 locally leaves amount 2, which requires two 1s (total 3 coins). Picking coin 3 leaves 3, solved by one 3 (total 2 coins). Local best != Global best!"

    fun build(step: GreedyCounterexamplesStep, viewportWidth: Float = 560f, viewportHeight: Float = 400f): GreedyScene {
        val items = mutableListOf<GreedySceneItem>()
        fun add(id: String, x: Float, y: Float, w: Float, h: Float, label: String, tone: GreedySceneTone = GreedySceneTone.NEUTRAL, size: Int = 10, shape: GreedySceneShape = GreedySceneShape.ROUNDED_RECT) {
            items += GreedySceneItem(id, GreedyRect(x, y, w, h), label, tone, size, textHorizontalPaddingDp = 4f, textVerticalPaddingDp = 4f, shape = shape)
        }
        val width = viewportWidth.coerceAtMost(360f).coerceAtLeast(1f)
        val innerWidth = (width - 16f).coerceAtLeast(1f)
        val comparisonVisible = step.status >= CounterexampleStatus.COMPARE_OPTIMAL
        add("counterexample-header", 8f, 4f, innerWidth, 26f, "TARGET ${step.targetAmount} · DENOMINATIONS [${step.denominations.joinToString()}] · LARGEST FIRST", GreedySceneTone.ACCENT, 9)
        add("counterexample-status", 8f, 33f, innerWidth, 18f, if (comparisonVisible) "FAILED GREEDY · COMPARISON REVEALED" else "GREEDY CHOICES IN PROGRESS · OPTIMAL HIDDEN", if (comparisonVisible) GreedySceneTone.REJECTED else GreedySceneTone.ACTIVE, 9)
        val gap = 6f
        val resultWidth = (innerWidth - gap) / 2f
        val right = 8f + resultWidth + gap
        val greedyTone = if (comparisonVisible) GreedySceneTone.REJECTED else GreedySceneTone.ACTIVE
        add("greedy-result", 8f, 54f, resultWidth, 20f, "GREEDY · LARGEST FIRST", greedyTone)
        add("optimal-result", right, 54f, resultWidth, 20f,
            "OPTIMAL · NON-GREEDY",
            if (comparisonVisible) GreedySceneTone.SELECTED else GreedySceneTone.MUTED, 9)
        if (step.greedyCoins.isEmpty()) add("greedy-empty", 8f, 77f, resultWidth, 30f, "NO COINS YET", GreedySceneTone.MUTED, 9)
        step.greedyCoins.forEachIndexed { index, coin ->
            add("greedy-coin-$index", 8f + (index + 0.5f) * resultWidth / maxOf(3, step.greedyCoins.size) - 15f,
                77f, 30f, 30f, coin.toString(), greedyTone, 11, GreedySceneShape.CIRCLE)
        }
        if (comparisonVisible) step.optimalCoins.forEachIndexed { index, coin ->
            add("optimal-coin-$index", right + (index + 0.5f) * resultWidth / maxOf(3, step.optimalCoins.size) - 15f,
                77f, 30f, 30f, coin.toString(), GreedySceneTone.SELECTED, 11, GreedySceneShape.CIRCLE)
        } else add("optimal-hidden", right, 77f, resultWidth, 30f, "? · HIDDEN UNTIL COMPARISON", GreedySceneTone.MUTED, 9)
        add("greedy-count", 8f, 111f, resultWidth, 19f, "${step.greedyCoins.size} GREEDY COINS · REM ${step.greedyRemaining}", greedyTone, 9)
        add("optimal-count", right, 111f, resultWidth, 19f,
            if (comparisonVisible) "${step.optimalCoins.size} OPTIMAL COINS" else "COUNT HIDDEN",
            if (comparisonVisible) GreedySceneTone.SELECTED else GreedySceneTone.MUTED, 9)
        add("coin-count-comparison", 8f, 134f, innerWidth, 21f,
            if (comparisonVisible) "${step.greedyCoins.size} GREEDY COINS > ${step.optimalCoins.size} OPTIMAL COINS" else "GREEDY PICKS SHOWN · OPTIMAL RESULT NOT YET REVEALED",
            if (comparisonVisible) GreedySceneTone.REJECTED else GreedySceneTone.MUTED, 9)
        items += GreedySceneItem("comparison-principle", GreedyRect(8f, 157f, innerWidth, 43f),
            if (comparisonVisible) "4 LEAVES 2 → TWO 1s (3 COINS)\n3 LEAVES 3 → ONE MORE 3 (2 COINS)\nLOCAL BEST ≠ GLOBAL BEST" else "OPTIMAL RESULT AND EXPLANATION REVEALED AT COMPARISON",
            if (comparisonVisible) GreedySceneTone.ACTIVE else GreedySceneTone.MUTED, 9,
            textHorizontalPaddingDp = 4f, textVerticalPaddingDp = 4f, wrapText = true)
        add("counterexample-proof", 8f, 203f, innerWidth, 18f,
            if (comparisonVisible) "THIS INPUT DISPROVES UNIVERSAL MINIMUM-COIN OPTIMALITY" else "A COUNTEREXAMPLE REFUTES A UNIVERSAL CLAIM",
            if (comparisonVisible) GreedySceneTone.REJECTED else GreedySceneTone.ACCENT, 9)
        return GreedyScene(
            width,
            224f,
            items,
            secondaryDetails = if (comparisonVisible) listOf("WHY GREEDY FAILS HERE: $WHY_GREEDY_FAILS") else emptyList(),
            constrainedTextMeasurement = true
        ).useAvailableGreedyPane(viewportWidth, viewportHeight)
    }
}
