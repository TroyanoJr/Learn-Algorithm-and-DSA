package com.example.learndsandalgorithm.presentation.learn

import kotlin.math.min

internal data class HashTableLoadFactorPresentation(
    val currentSize: Int,
    val currentCapacity: Int,
    val currentLoadFactor: Double,
    val triggerSize: Int? = null,
    val triggerCapacity: Int? = null,
    val triggerLoadFactor: Double? = null
)

internal object HashTableLessonDetailsLayout {
    const val OPERATIONS_VISUAL_WEIGHT = 1.3f
    const val APPLICATIONS_VISUAL_WEIGHT = 1.2f
    const val SECTION_GAP_DP = 3f

    fun resolveMeasuredAllocation(
        availableHeightPx: Int,
        headerHeightPx: Int,
        fullStepsHeightPx: Int,
        stepsHeaderHeightPx: Int,
        gapPx: Int,
        stepsExplanationSpacingPx: Int,
        minimumExplanationViewportPx: Int = 1
    ): HashTableDetailsAllocation {
        val safeAvailable = availableHeightPx.coerceAtLeast(0)
        val safeHeader = headerHeightPx.coerceAtLeast(0)
        val safeFullSteps = fullStepsHeightPx.coerceAtLeast(0)
        val safeStepsHeader = stepsHeaderHeightPx.coerceAtLeast(0)
        val safeGap = gapPx.coerceAtLeast(0)
        val safeStepsSpacing = stepsExplanationSpacingPx.coerceAtLeast(0)
        val safeMinimumExplanation = minimumExplanationViewportPx.coerceAtLeast(1)
        if (safeHeader + safeGap + safeFullSteps <= safeAvailable) {
            return HashTableDetailsAllocation(
                mode = HashTableDetailsAllocationMode.FULL_CONTENT,
                headerViewportHeightPx = safeHeader,
                stepsViewportHeightPx = safeFullSteps,
                gapPx = safeGap
            )
        }

        val minimumScrollableStepsHeight = safeStepsHeader + safeStepsSpacing + safeMinimumExplanation
        val remainingForSteps = (safeAvailable - safeHeader - safeGap).coerceAtLeast(0)
        if (remainingForSteps >= minimumScrollableStepsHeight) {
            return HashTableDetailsAllocation(
                mode = HashTableDetailsAllocationMode.SCROLLING_EXPLANATION,
                headerViewportHeightPx = safeHeader,
                stepsViewportHeightPx = remainingForSteps,
                gapPx = safeGap
            )
        }

        val usableGap = if (safeAvailable >= 3) minOf(safeGap, safeAvailable - 2) else 0
        val contentHeight = (safeAvailable - usableGap).coerceAtLeast(0)
        if (contentHeight >= minimumScrollableStepsHeight + 1) {
            return HashTableDetailsAllocation(
                mode = HashTableDetailsAllocationMode.SCROLLING_SECTIONS,
                headerViewportHeightPx = contentHeight - minimumScrollableStepsHeight,
                stepsViewportHeightPx = minimumScrollableStepsHeight,
                gapPx = usableGap
            )
        }

        val headerViewport = when {
            contentHeight >= 2 -> contentHeight / 2
            contentHeight == 1 -> 1
            else -> 0
        }
        return HashTableDetailsAllocation(
            mode = HashTableDetailsAllocationMode.CONSTRAINED_SECTIONS,
            headerViewportHeightPx = headerViewport,
            stepsViewportHeightPx = contentHeight - headerViewport,
            gapPx = usableGap
        )
    }
}

internal enum class HashTableDetailsAllocationMode {
    FULL_CONTENT,
    SCROLLING_EXPLANATION,
    SCROLLING_SECTIONS,
    CONSTRAINED_SECTIONS
}

internal data class HashTableDetailsAllocation(
    val mode: HashTableDetailsAllocationMode,
    val headerViewportHeightPx: Int,
    val stepsViewportHeightPx: Int,
    val gapPx: Int
)

internal data class HashTableOperationsLayoutResult(
    val loadCardBounds: Rect2D,
    val oldHeadingBounds: Rect2D,
    val oldSlotBounds: Map<Int, Rect2D>,
    val currentHeadingBounds: Rect2D,
    val currentSlotBounds: Map<Int, Rect2D>,
    val currentIndexBounds: Map<Int, Rect2D>,
    val rehashArrow: LineSegment2D?,
    val rehashRouteY: Float?,
    val requiredSceneWidth: Float,
    val requiredSceneHeight: Float
)

internal object HashTableOperationsSceneLayout {
    const val SCENE_WIDTH_DP = 400f
    const val MARGIN_DP = 12f
    const val SLOT_WIDTH_DP = 43f
    const val SLOT_GAP_DP = 4f
    const val LOAD_CARD_TOP_DP = 8f
    const val CARD_TO_OLD_HEADING_GAP_DP = 6f
    const val HEADING_TO_ROW_GAP_DP = 4f
    const val ROW_TO_CURRENT_HEADING_GAP_DP = 6f
    const val CURRENT_MARKER_HEIGHT_DP = 16f
    const val INDEX_GAP_DP = 2f
    const val SCENE_BOTTOM_PADDING_DP = 7f

    fun loadFactorPresentation(step: OperationsTraceStep): HashTableLoadFactorPresentation {
        val triggerCapacity = if (step.isThresholdExceeded) step.oldCapacity ?: step.capacity else null
        val triggerSize = if (step.isThresholdExceeded) step.size + 1 else null
        return HashTableLoadFactorPresentation(
            currentSize = step.size,
            currentCapacity = step.capacity,
            currentLoadFactor = step.loadFactor,
            triggerSize = triggerSize,
            triggerCapacity = triggerCapacity,
            triggerLoadFactor = if (triggerSize != null && triggerCapacity != null) {
                triggerSize.toDouble() / triggerCapacity
            } else null
        )
    }

    fun calculate(step: OperationsTraceStep, measuredLoadCardHeightDp: Float): HashTableOperationsLayoutResult {
        val cardHeight = measuredLoadCardHeightDp.coerceAtLeast(0f)
        val cardBottom = LOAD_CARD_TOP_DP + cardHeight
        val oldHeadingTop = cardBottom + CARD_TO_OLD_HEADING_GAP_DP
        val oldHeadingHeight = 15f
        val oldSlotTop = oldHeadingTop + oldHeadingHeight + HEADING_TO_ROW_GAP_DP
        val oldSlotHeight = 34f
        val currentHeadingTop = oldSlotTop + oldSlotHeight + ROW_TO_CURRENT_HEADING_GAP_DP
        val currentHeadingHeight = 17f
        val currentSlotTop = currentHeadingTop + currentHeadingHeight + CURRENT_MARKER_HEIGHT_DP
        val currentSlotHeight = 50f
        val indexTop = currentSlotTop + currentSlotHeight + INDEX_GAP_DP
        val indexHeight = 14f
        val requiredSceneHeight = indexTop + indexHeight + SCENE_BOTTOM_PADDING_DP

        fun slots(capacity: Int, top: Float, height: Float): Map<Int, Rect2D> =
            (0 until capacity).associateWith { index ->
                Rect2D(MARGIN_DP + index * (SLOT_WIDTH_DP + SLOT_GAP_DP), top, SLOT_WIDTH_DP, height)
            }
        val old = slots(step.oldCapacity ?: 4, oldSlotTop, oldSlotHeight)
        val current = slots(step.capacity, currentSlotTop, currentSlotHeight)
        val indices = current.mapValues { (_, bounds) -> Rect2D(bounds.left, indexTop, bounds.width, indexHeight) }
        val oldIndex = step.oldBuckets?.indexOfFirst { it?.key == step.activeKey } ?: -1
        val target = step.targetIndex
        val arrow = if (oldIndex >= 0 && target != null && step.buckets.getOrNull(target)?.key == step.activeKey) {
            LineSegment2D(old.getValue(oldIndex).center.copy(y = old.getValue(oldIndex).bottom), current.getValue(target).center.copy(y = current.getValue(target).top))
        } else null
        return HashTableOperationsLayoutResult(
            loadCardBounds = Rect2D(MARGIN_DP, LOAD_CARD_TOP_DP, SCENE_WIDTH_DP - MARGIN_DP * 2f, cardHeight),
            oldHeadingBounds = Rect2D(MARGIN_DP, oldHeadingTop, 184f, oldHeadingHeight),
            oldSlotBounds = old,
            currentHeadingBounds = Rect2D(200f, currentHeadingTop, 188f, currentHeadingHeight),
            currentSlotBounds = current,
            currentIndexBounds = indices,
            rehashArrow = arrow,
            rehashRouteY = arrow?.let { currentSlotTop - 3f },
            requiredSceneWidth = SCENE_WIDTH_DP,
            requiredSceneHeight = requiredSceneHeight
        )
    }

    fun initialFitScale(viewportWidth: Float, viewportHeight: Float, sceneHeightDp: Float): Float =
        min(1f, min(viewportWidth / SCENE_WIDTH_DP, viewportHeight / sceneHeightDp))
}
