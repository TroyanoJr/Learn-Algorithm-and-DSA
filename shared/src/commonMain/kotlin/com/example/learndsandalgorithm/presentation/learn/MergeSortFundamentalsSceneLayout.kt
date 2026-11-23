package com.example.learndsandalgorithm.presentation.learn

import kotlin.math.max

internal data class MergeSortFundamentalsSceneLayoutResult(
    val headerBounds: Rect2D,
    val comparisonBounds: Rect2D,
    val leftLabelBounds: Rect2D,
    val rightLabelBounds: Rect2D,
    val outputLabelBounds: Rect2D,
    val leftMarkerBounds: Map<Int, Rect2D>,
    val rightMarkerBounds: Map<Int, Rect2D>,
    val outputMarkerBounds: Map<Int, Rect2D>,
    val leftCellBounds: Map<Int, Rect2D>,
    val rightCellBounds: Map<Int, Rect2D>,
    val outputCellBounds: Map<Int, Rect2D>,
    val leftIndexBounds: Map<Int, Rect2D>,
    val rightIndexBounds: Map<Int, Rect2D>,
    val outputIndexBounds: Map<Int, Rect2D>,
    val emptyLeftBounds: Rect2D?,
    val emptyRightBounds: Rect2D?,
    val emptyOutputBounds: Rect2D?,
    val transferRoutes: Map<Int, List<Point2D>>,
    val requiredSceneWidth: Float,
    val requiredSceneHeight: Float
)

/** Pure, sequence-wide geometry for the two-input merge lesson. */
internal object MergeSortFundamentalsSceneLayout {
    private const val HORIZONTAL_MARGIN = 12f
    private const val COLUMN_GAP = 12f
    private const val CELL_GAP = 5f
    private const val HEADER_TOP = 8f
    private const val HEADER_HEIGHT = 22f
    private const val INDICATOR_TOP = 34f
    private const val INDICATOR_HEIGHT = 26f
    private const val INPUT_LABEL_TOP = 68f
    private const val INPUT_LABEL_HEIGHT = 14f
    private const val MARKER_TOP = 83f
    private const val MARKER_HEIGHT = 13f
    private const val INPUT_CELL_TOP = 98f
    private const val CELL_HEIGHT = 40f
    private const val INDEX_HEIGHT = 12f
    private const val OUTPUT_LABEL_TOP = 169f
    private const val OUTPUT_LABEL_HEIGHT = 14f
    private const val OUTPUT_MARKER_TOP = 184f
    private const val OUTPUT_MARKER_HEIGHT = 12f
    private const val OUTPUT_CELL_TOP = 207f
    private const val OUTPUT_INDEX_TOP = 248f
    private const val BOTTOM_MARGIN = 8f
    private const val MIN_CELL_WIDTH = 44f

    fun calculate(sequence: List<MergeSortFundamentalsSceneFrame>): MergeSortFundamentalsSceneLayoutResult {
        if (sequence.isEmpty()) return emptyLayout()
        val first = sequence.first().step
        val left = first.leftArr
        val right = first.rightArr
        require(sequence.all { it.step.leftArr == left && it.step.rightArr == right }) {
            "Merge array geometry must remain stable through the sequence"
        }

        val allValues = left + right
        val widestValue = allValues.maxOfOrNull { it.toString().length } ?: 1
        val cellWidth = max(MIN_CELL_WIDTH, widestValue * 8.4f + 16f)
        val outputCellWidth = max(cellWidth, estimatedWidth("[${(left.size + right.size).coerceAtLeast(1)}]", 8.5f) + 18f)
        val leftRowWidth = rowWidth(left.size, cellWidth, EMPTY_COLUMN_WIDTH)
        val rightRowWidth = rowWidth(right.size, cellWidth, EMPTY_COLUMN_WIDTH)
        val outputCount = left.size + right.size
        val outputRowWidth = rowWidth(outputCount, outputCellWidth, EMPTY_COLUMN_WIDTH)
        val leftLabelWidth = sequence.maxOf { frame ->
            estimatedWidth(inputLabel(MergeSource.LEFT, frame.step), 9f)
        } + 4f
        val rightLabelWidth = sequence.maxOf { frame ->
            estimatedWidth(inputLabel(MergeSource.RIGHT, frame.step), 9f)
        } + 4f
        val leftColumnWidth = max(leftRowWidth, leftLabelWidth)
        val rightColumnWidth = max(rightRowWidth, rightLabelWidth)
        val inputsWidth = leftColumnWidth + COLUMN_GAP + rightColumnWidth
        val widestHeader = sequence.maxOf { estimatedWidth(it.step.badgeText, 10f) }
        val widestIndicator = sequence.maxOf { estimatedWidth(indicatorText(it.step), 9f) }
        val outputTitleWidth = sequence.maxOf { frame ->
            estimatedWidth("MERGED OUTPUT · k=${frame.step.k}/${outputCount} · O(N)", 9f)
        }
        val requiredWidth = maxOf(
            inputsWidth + MARKER_WIDTH * 2f + 1f,
            outputRowWidth + MARKER_WIDTH * 2f + 1f,
            widestHeader + HORIZONTAL_MARGIN * 2f,
            widestIndicator + HORIZONTAL_MARGIN * 2f,
            outputTitleWidth + HORIZONTAL_MARGIN * 2f
        ) + HORIZONTAL_MARGIN * 2f

        val leftColumnLeft = (requiredWidth - inputsWidth) / 2f
        val rightColumnLeft = leftColumnLeft + leftColumnWidth + COLUMN_GAP
        val leftCellsWidth = contentRowWidth(left.size, cellWidth)
        val rightCellsWidth = contentRowWidth(right.size, cellWidth)
        val outputCellsWidth = contentRowWidth(outputCount, outputCellWidth)
        val leftCellsLeft = leftColumnLeft + (leftColumnWidth - leftCellsWidth) / 2f
        val rightCellsLeft = rightColumnLeft + (rightColumnWidth - rightCellsWidth) / 2f
        val outputCellsLeft = (requiredWidth - outputCellsWidth) / 2f

        val leftCells = (left.indices).associateWith { index ->
            Rect2D(leftCellsLeft + index * (cellWidth + CELL_GAP), INPUT_CELL_TOP, cellWidth, CELL_HEIGHT)
        }
        val rightCells = (right.indices).associateWith { index ->
            Rect2D(rightCellsLeft + index * (cellWidth + CELL_GAP), INPUT_CELL_TOP, cellWidth, CELL_HEIGHT)
        }
        val outputCells = (0 until outputCount).associateWith { index ->
            Rect2D(outputCellsLeft + index * (outputCellWidth + CELL_GAP), OUTPUT_CELL_TOP, outputCellWidth, CELL_HEIGHT)
        }
        val leftMarkers = markerBounds(left.size, leftCellsLeft, leftCellsWidth, cellWidth, MARKER_TOP)
        val rightMarkers = markerBounds(right.size, rightCellsLeft, rightCellsWidth, cellWidth, MARKER_TOP)
        val outputMarkers = markerBounds(outputCount, outputCellsLeft, outputCellsWidth, outputCellWidth, OUTPUT_MARKER_TOP, OUTPUT_MARKER_HEIGHT)
        val transferRoutes = sequence.mapNotNull { frame ->
            val step = frame.step
            val source = step.source
            if (step.destinationIndex == null || source == null || step.sourceIndexBeforeTransfer == null) {
                return@mapNotNull null
            }
            val sourceRect = when (source) {
                MergeSource.LEFT -> leftCells[step.sourceIndexBeforeTransfer]
                MergeSource.RIGHT -> rightCells[step.sourceIndexBeforeTransfer]
            }
            val destinationRect = outputCells[step.destinationIndex]
            if (sourceRect != null && destinationRect != null) {
                val routeX = requiredWidth - HORIZONTAL_MARGIN / 2f
                val routeTop = OUTPUT_LABEL_TOP - 7f
                val routeBottom = OUTPUT_CELL_TOP - 2f
                frame.snapshotIndex to listOf(
                    Point2D(sourceRect.center.x, sourceRect.bottom),
                    Point2D(sourceRect.center.x, routeTop),
                    Point2D(routeX, routeTop),
                    Point2D(routeX, routeBottom),
                    Point2D(destinationRect.center.x, routeBottom),
                    Point2D(destinationRect.center.x, destinationRect.top)
                )
            } else null
        }.toMap()

        val labelBoundsWidth = max(leftColumnWidth, requiredWidth / 2f - HORIZONTAL_MARGIN)
        return MergeSortFundamentalsSceneLayoutResult(
            headerBounds = Rect2D(HORIZONTAL_MARGIN, HEADER_TOP, requiredWidth - HORIZONTAL_MARGIN * 2f, HEADER_HEIGHT),
            comparisonBounds = Rect2D(HORIZONTAL_MARGIN, INDICATOR_TOP, requiredWidth - HORIZONTAL_MARGIN * 2f, INDICATOR_HEIGHT),
            leftLabelBounds = Rect2D(leftColumnLeft, INPUT_LABEL_TOP, labelBoundsWidth.coerceAtMost(leftColumnWidth), INPUT_LABEL_HEIGHT),
            rightLabelBounds = Rect2D(rightColumnLeft, INPUT_LABEL_TOP, rightColumnWidth, INPUT_LABEL_HEIGHT),
            outputLabelBounds = Rect2D(HORIZONTAL_MARGIN, OUTPUT_LABEL_TOP, outputTitleWidth, OUTPUT_LABEL_HEIGHT),
            leftMarkerBounds = leftMarkers,
            rightMarkerBounds = rightMarkers,
            outputMarkerBounds = outputMarkers,
            leftCellBounds = leftCells,
            rightCellBounds = rightCells,
            outputCellBounds = outputCells,
            leftIndexBounds = left.indices.associateWith { index ->
                val cell = leftCells.getValue(index)
                Rect2D(cell.left + 2f, cell.top + 26f, cell.width - 4f, INDEX_HEIGHT)
            },
            rightIndexBounds = right.indices.associateWith { index ->
                val cell = rightCells.getValue(index)
                Rect2D(cell.left + 2f, cell.top + 26f, cell.width - 4f, INDEX_HEIGHT)
            },
            outputIndexBounds = outputCells.mapValues { (_, cell) ->
                Rect2D(cell.left, OUTPUT_INDEX_TOP, cell.width, INDEX_HEIGHT)
            },
            emptyLeftBounds = if (left.isEmpty()) Rect2D(leftColumnLeft, INPUT_CELL_TOP, leftColumnWidth, CELL_HEIGHT) else null,
            emptyRightBounds = if (right.isEmpty()) Rect2D(rightColumnLeft, INPUT_CELL_TOP, rightColumnWidth, CELL_HEIGHT) else null,
            emptyOutputBounds = if (outputCount == 0) Rect2D(HORIZONTAL_MARGIN, OUTPUT_CELL_TOP, requiredWidth - HORIZONTAL_MARGIN * 2f, CELL_HEIGHT) else null,
            transferRoutes = transferRoutes,
            requiredSceneWidth = requiredWidth,
            requiredSceneHeight = OUTPUT_INDEX_TOP + INDEX_HEIGHT + BOTTOM_MARGIN
        )
    }

    internal fun indicatorText(step: MergeFundamentalsTraceStep): String = when (step.event) {
        MergeStepEvent.START -> "READY · COMPARE THE FRONT VALUES"
        MergeStepEvent.COMPARE -> {
            step.comparison?.let { comparison ->
                if (comparison.valuesAreEqual) {
                    "LEFT[${comparison.leftIndex}]=${comparison.leftValue} = RIGHT[${comparison.rightIndex}]=${comparison.rightValue} · LEFT WINS TIES"
                } else {
                    "COMPARE LEFT[${comparison.leftIndex}]=${comparison.leftValue} WITH RIGHT[${comparison.rightIndex}]=${comparison.rightValue}"
                }
            } ?: "COMPARE SNAPSHOT"
        }
        MergeStepEvent.TAKE_LEFT, MergeStepEvent.TAKE_RIGHT, MergeStepEvent.FLUSH_REMAINING -> {
            val origin = step.selectedOrigin
            val destination = step.destinationIndex
            val selected = if (origin == null) "TRANSFER" else "${MergeSortFundamentalsSceneBuilder.originLabel(origin)} → OUTPUT[$destination]"
            val tieNote = if (step.tieBreakDecision != null) " · STABLE TIE: LEFT FIRST" else ""
            val exhaustion = when {
                step.leftExhausted && step.rightExhausted -> " · BOTH INPUTS EXHAUSTED"
                step.leftExhausted -> " · LEFT EXHAUSTED"
                step.rightExhausted -> " · RIGHT EXHAUSTED"
                else -> ""
            }
            "$selected · k=${step.k}$tieNote$exhaustion"
        }
        MergeStepEvent.COMPLETE -> "COMPLETE · ${step.leftArr.size + step.rightArr.size} VALUES MERGED · O(N)"
    }

    internal fun inputLabel(source: MergeSource, step: MergeFundamentalsTraceStep): String {
        val pointer = if (source == MergeSource.LEFT) step.i else step.j
        val exhausted = if (source == MergeSource.LEFT) step.leftExhausted else step.rightExhausted
        val name = if (source == MergeSource.LEFT) "LEFT" else "RIGHT"
        val marker = if (source == MergeSource.LEFT) "i" else "j"
        return "$name SORTED · $marker=$pointer${if (exhausted) " · END" else ""}"
    }

    private fun markerBounds(
        count: Int,
        rowLeft: Float,
        rowWidth: Float,
        cellWidth: Float,
        top: Float,
        markerHeight: Float = MARKER_HEIGHT
    ): Map<Int, Rect2D> =
        (0..count).associateWith { index ->
            val centerX = if (index < count) {
                rowLeft + index * (cellWidth + CELL_GAP) + cellWidth / 2f
            } else if (count > 0) {
                rowLeft + (count - 1) * (cellWidth + CELL_GAP) + cellWidth + CELL_GAP / 2f
            } else {
                rowLeft + rowWidth / 2f
            }
            Rect2D(centerX - MARKER_WIDTH / 2f, top, MARKER_WIDTH, markerHeight)
        }

    private fun rowWidth(count: Int, cellWidth: Float, emptyWidth: Float): Float =
        if (count == 0) emptyWidth else contentRowWidth(count, cellWidth)

    private fun contentRowWidth(count: Int, cellWidth: Float): Float =
        if (count == 0) 0f else count * cellWidth + (count - 1) * CELL_GAP

    private fun estimatedWidth(text: String, fontSize: Float): Float = text.length * fontSize * 0.65f

    private fun emptyLayout() = MergeSortFundamentalsSceneLayoutResult(
        headerBounds = EMPTY_RECT,
        comparisonBounds = EMPTY_RECT,
        leftLabelBounds = EMPTY_RECT,
        rightLabelBounds = EMPTY_RECT,
        outputLabelBounds = EMPTY_RECT,
        leftMarkerBounds = emptyMap(),
        rightMarkerBounds = emptyMap(),
        outputMarkerBounds = emptyMap(),
        leftCellBounds = emptyMap(),
        rightCellBounds = emptyMap(),
        outputCellBounds = emptyMap(),
        leftIndexBounds = emptyMap(),
        rightIndexBounds = emptyMap(),
        outputIndexBounds = emptyMap(),
        emptyLeftBounds = null,
        emptyRightBounds = null,
        emptyOutputBounds = null,
        transferRoutes = emptyMap(),
        requiredSceneWidth = 0f,
        requiredSceneHeight = 0f
    )

    private const val EMPTY_COLUMN_WIDTH = 112f
    private const val MARKER_WIDTH = 24f
    private val EMPTY_RECT = Rect2D(0f, 0f, 0f, 0f)
}
