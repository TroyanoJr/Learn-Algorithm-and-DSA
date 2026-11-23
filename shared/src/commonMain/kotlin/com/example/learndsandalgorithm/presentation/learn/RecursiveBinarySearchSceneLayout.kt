package com.example.learndsandalgorithm.presentation.learn

import kotlin.math.max

internal data class RecursiveBinarySearchSceneLayoutResult(
    val headerBounds: Rect2D,
    val targetBounds: Rect2D,
    val rangeBounds: Rect2D,
    val markerBounds: Map<Int, Rect2D>,
    val cellBounds: Map<Int, Rect2D>,
    val indexBounds: Map<Int, Rect2D>,
    val emptyArrayBounds: Rect2D?,
    val midpointBounds: Rect2D,
    val stackHeaderBounds: Rect2D,
    val stackRowBounds: Map<Int, Rect2D>,
    val returnBounds: Rect2D,
    val complexityBounds: Rect2D,
    val maximumStackDepth: Int,
    val requiredSceneWidth: Float,
    val requiredSceneHeight: Float
)

internal object RecursiveBinarySearchSceneLayout {
    private const val HORIZONTAL_MARGIN = 12f
    private const val TOP_MARGIN = 8f
    private const val BOTTOM_MARGIN = 8f
    private const val HEADER_HEIGHT = 42f
    private const val HEADER_GAP = 8f
    private const val MARKER_HEIGHT = 16f
    private const val MARKER_CELL_GAP = 2f
    private const val CELL_HEIGHT = 44f
    private const val CELL_GAP = 8f
    private const val INDEX_GAP = 2f
    private const val INDEX_HEIGHT = 14f
    private const val COMPARISON_GAP = 6f
    private const val COMPARISON_HEIGHT = 24f
    private const val STACK_HEADER_GAP = 8f
    private const val STACK_HEADER_HEIGHT = 16f
    private const val STACK_ROW_GAP = 4f
    private const val STACK_ROW_HEIGHT = 30f
    private const val RETURN_GAP = 6f
    private const val RETURN_HEIGHT = 30f
    private const val COMPLEXITY_GAP = 6f
    private const val COMPLEXITY_HEIGHT = 18f
    private const val MIN_CELL_WIDTH = 64f
    private const val VALUE_HORIZONTAL_PADDING = 20f
    private const val HEADER_COLUMN_GAP = 12f

    fun calculate(frames: List<RecursiveBinarySearchSceneFrame>): RecursiveBinarySearchSceneLayoutResult {
        if (frames.isEmpty()) {
            val empty = Rect2D(0f, 0f, 0f, 0f)
            return RecursiveBinarySearchSceneLayoutResult(
                headerBounds = empty,
                targetBounds = empty,
                rangeBounds = empty,
                markerBounds = emptyMap(),
                cellBounds = emptyMap(),
                indexBounds = emptyMap(),
                emptyArrayBounds = null,
                midpointBounds = empty,
                stackHeaderBounds = empty,
                stackRowBounds = emptyMap(),
                returnBounds = empty,
                complexityBounds = empty,
                maximumStackDepth = 0,
                requiredSceneWidth = 0f,
                requiredSceneHeight = 0f
            )
        }

        val array = frames.first().meta.array
        require(frames.all { it.meta.array == array }) { "Binary search array must remain stable across the sequence" }
        val maximumStackDepth = frames.maxOf { it.meta.maximumStackDepth }.coerceAtLeast(1)
        val longestValue = array.maxOfOrNull { it.toString().length } ?: 0
        val cellWidth = max(MIN_CELL_WIDTH, longestValue * 9f + VALUE_HORIZONTAL_PADDING)
        val arrayRowWidth = if (array.isEmpty()) {
            128f
        } else {
            array.size * cellWidth + (array.size - 1).coerceAtLeast(0) * CELL_GAP
        }

        val targetTextWidth = frames.maxOf { estimatedWidth("TARGET ${it.meta.target}", 11f) } + 8f
        val rangeTextWidth = frames.maxOf { estimatedWidth(rangeText(it.meta), 10f) } + 8f
        val headerContentWidth = targetTextWidth + HEADER_COLUMN_GAP + rangeTextWidth + 20f
        val comparisonContentWidth = frames.maxOf { estimatedWidth(midpointText(it.meta), 10f) } + 20f
        val stackContentWidth = frames.flatMap { frame ->
            frame.meta.callStack.map(::callText) + listOfNotNull(frame.meta.calleeCall?.let(::pendingCallText))
        }.maxOfOrNull { estimatedWidth(it, 10f) + 20f } ?: estimatedWidth("RECURSIVE CALL STACK", 10f) + 20f
        val returnContentWidth = frames.maxOf { estimatedWidth(resultText(it.meta), 10f) } + 20f
        val complexityContentWidth = estimatedWidth(COMPLEXITY_TEXT, 10f) + 20f
        val requiredWidth = maxOf(
            arrayRowWidth,
            headerContentWidth,
            comparisonContentWidth,
            stackContentWidth,
            returnContentWidth,
            complexityContentWidth
        ) + HORIZONTAL_MARGIN * 2f

        val headerBounds = Rect2D(0f, TOP_MARGIN, requiredWidth, HEADER_HEIGHT)
        val targetBounds = Rect2D(
            HORIZONTAL_MARGIN + 10f,
            headerBounds.top + 12f,
            targetTextWidth,
            18f
        )
        val rangeLeft = targetBounds.right + HEADER_COLUMN_GAP
        val rangeBounds = Rect2D(
            rangeLeft,
            targetBounds.top,
            (requiredWidth - HORIZONTAL_MARGIN - 10f - rangeLeft).coerceAtLeast(1f),
            targetBounds.height
        )

        val markerTop = headerBounds.bottom + HEADER_GAP
        val cellTop = markerTop + MARKER_HEIGHT + MARKER_CELL_GAP
        val indexTop = cellTop + CELL_HEIGHT + INDEX_GAP
        val comparisonTop = indexTop + INDEX_HEIGHT + COMPARISON_GAP
        val stackHeaderTop = comparisonTop + COMPARISON_HEIGHT + STACK_HEADER_GAP
        val stackRowsTop = stackHeaderTop + STACK_HEADER_HEIGHT + STACK_ROW_GAP
        val stackRowsBottom = stackRowsTop + maximumStackDepth * STACK_ROW_HEIGHT +
            (maximumStackDepth - 1).coerceAtLeast(0) * STACK_ROW_GAP
        val returnTop = stackRowsBottom + RETURN_GAP
        val complexityTop = returnTop + RETURN_HEIGHT + COMPLEXITY_GAP
        val requiredHeight = complexityTop + COMPLEXITY_HEIGHT + BOTTOM_MARGIN
        val rowLeft = (requiredWidth - arrayRowWidth) / 2f
        val markerBounds = array.indices.associateWith { index ->
            Rect2D(rowLeft + index * (cellWidth + CELL_GAP), markerTop, cellWidth, MARKER_HEIGHT)
        }
        val cellBounds = array.indices.associateWith { index ->
            Rect2D(rowLeft + index * (cellWidth + CELL_GAP), cellTop, cellWidth, CELL_HEIGHT)
        }
        val indexBounds = array.indices.associateWith { index ->
            Rect2D(rowLeft + index * (cellWidth + CELL_GAP), indexTop, cellWidth, INDEX_HEIGHT)
        }
        val stackRows = (0 until maximumStackDepth).associateWith { index ->
            Rect2D(
                HORIZONTAL_MARGIN,
                stackRowsTop + index * (STACK_ROW_HEIGHT + STACK_ROW_GAP),
                requiredWidth - HORIZONTAL_MARGIN * 2f,
                STACK_ROW_HEIGHT
            )
        }

        return RecursiveBinarySearchSceneLayoutResult(
            headerBounds = headerBounds,
            targetBounds = targetBounds,
            rangeBounds = rangeBounds,
            markerBounds = markerBounds,
            cellBounds = cellBounds,
            indexBounds = indexBounds,
            emptyArrayBounds = if (array.isEmpty()) Rect2D(HORIZONTAL_MARGIN, cellTop, requiredWidth - HORIZONTAL_MARGIN * 2f, CELL_HEIGHT) else null,
            midpointBounds = Rect2D(HORIZONTAL_MARGIN, comparisonTop, requiredWidth - HORIZONTAL_MARGIN * 2f, COMPARISON_HEIGHT),
            stackHeaderBounds = Rect2D(HORIZONTAL_MARGIN, stackHeaderTop, requiredWidth - HORIZONTAL_MARGIN * 2f, STACK_HEADER_HEIGHT),
            stackRowBounds = stackRows,
            returnBounds = Rect2D(HORIZONTAL_MARGIN, returnTop, requiredWidth - HORIZONTAL_MARGIN * 2f, RETURN_HEIGHT),
            complexityBounds = Rect2D(HORIZONTAL_MARGIN, complexityTop, requiredWidth - HORIZONTAL_MARGIN * 2f, COMPLEXITY_HEIGHT),
            maximumStackDepth = maximumStackDepth,
            requiredSceneWidth = requiredWidth,
            requiredSceneHeight = requiredHeight
        )
    }

    internal fun rangeText(meta: RecursiveBinarySearchSceneMeta): String =
        if (meta.rangeLeft == null || meta.rangeRight == null) "NO ACTIVE CALL"
        else if (meta.rangeLeft > meta.rangeRight) "EMPTY RANGE [${meta.rangeLeft}..${meta.rangeRight}]"
        else "INCLUSIVE RANGE [${meta.rangeLeft}..${meta.rangeRight}]"

    internal fun midpointText(meta: RecursiveBinarySearchSceneMeta): String {
        val mid = meta.midIndex ?: return when {
            meta.isEmptyRangeBaseCase -> "Empty interval: no midpoint or array access"
            meta.event == BinarySearchEvent.START -> "Recursive search is ready to enter the initial call"
            meta.event == BinarySearchEvent.RETURN && meta.isTerminal -> "All recursive calls returned"
            meta.calleeCall != null -> "Next call: ${pendingCallText(meta.calleeCall)}"
            else -> "Enter the current call, then calculate its midpoint"
        }
        val value = meta.midValue ?: return "MID = $mid"
        return when (meta.comparison) {
            BinarySearchComparison.LESS -> "arr[$mid]=$value < target ${meta.target}"
            BinarySearchComparison.EQUAL -> "arr[$mid]=$value == target ${meta.target}"
            BinarySearchComparison.GREATER -> "arr[$mid]=$value > target ${meta.target}"
            null -> "mid = left + (right-left)/2 = $mid · arr[$mid]=$value"
        }
    }

    internal fun callText(call: BinarySearchCallFrame): String =
        "CALL #${call.callId} · search(${call.left},${call.right},depth=${call.depth}) · ${call.callerCallId?.let { "caller #$it" } ?: "initial caller"}"

    internal fun pendingCallText(call: BinarySearchCallFrame): String =
        "NEXT #${call.callId} · search(${call.left},${call.right},depth=${call.depth}) · from #${call.callerCallId}"

    internal fun resultText(meta: RecursiveBinarySearchSceneMeta): String = when (meta.event) {
        BinarySearchEvent.TARGET_FOUND -> "FOUND · INDEX ${meta.foundIndex}"
        BinarySearchEvent.TARGET_NOT_FOUND -> "EMPTY RANGE · BASE CASE RETURNS -1"
        BinarySearchEvent.RETURN -> {
            val value = meta.returnValue
            val result = if (value == -1) "-1" else "INDEX $value"
            val destination = meta.returnedCall?.callerCallId?.let { "CALL #$it" } ?: "INITIAL CALLER"
            if (meta.isTerminal) "FINAL RESULT · $result" else "RETURN $result → $destination"
        }
        else -> "RESULT PENDING"
    }

    private fun estimatedWidth(text: String, fontSize: Float): Float = text.length * fontSize * 0.62f

    internal const val COMPLEXITY_TEXT = "TIME O(log N) · RECURSIVE SPACE O(log N)"
}
