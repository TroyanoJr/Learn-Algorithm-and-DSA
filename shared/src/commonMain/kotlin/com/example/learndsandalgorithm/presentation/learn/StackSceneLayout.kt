package com.example.learndsandalgorithm.presentation.learn

internal data class StackSceneLayoutResult(
    val arrayBounds: Map<String, Rect2D>,
    val arrayIndexBounds: Map<String, Rect2D>,
    val linkedNodeBounds: Map<String, Rect2D>,
    val linkedConnectionLines: Map<String, LineSegment2D>,
    val emptyStateBounds: Rect2D,
    val topLabelBounds: Rect2D,
    val bottomLabelBounds: Rect2D,
    val headLabelBounds: Rect2D,
    val nullBounds: Rect2D,
    val requiredSceneWidth: Float,
    val requiredSceneHeight: Float
)

internal object StackSceneLayout {
    private const val MARGIN = 8f
    private const val CELL_WIDTH = 116f
    private const val CELL_HEIGHT = 30f
    private const val VERTICAL_GAP = 2f
    private const val NODE_WIDTH = 42f
    private const val NODE_HEIGHT = 42f
    private const val LINK_GAP = 10f
    private const val HEAD_WIDTH = 42f
    private const val NULL_WIDTH = 48f

    fun calculate(
        maxDepth: Int,
        maxValueWidthDp: Float = 40f,
        maxValueHeightDp: Float = 16f,
        includeLinked: Boolean = false,
        includeEmptyState: Boolean = false,
        headerWidthDp: Float = 180f,
        headerHeightDp: Float = 18f,
        headLabelWidthDp: Float = 42f,
        headLabelHeightDp: Float = 16f,
        indexLabelWidthDp: Float = 24f,
        nullLabelWidthDp: Float = 48f,
        nullLabelHeightDp: Float = 16f
    ): StackSceneLayoutResult {
        val depth = maxDepth.coerceAtLeast(0)
        val cellWidth = maxOf(CELL_WIDTH, maxValueWidthDp + 24f)
        val cellHeight = maxOf(CELL_HEIGHT, maxValueHeightDp + 8f)
        val nodeWidth = maxOf(NODE_WIDTH, maxValueWidthDp + 10f)
        val nodeHeight = maxOf(NODE_HEIGHT, maxValueHeightDp + 10f)
        val indexWidth = maxOf(24f, indexLabelWidthDp)
        val cellLeft = MARGIN + indexWidth + 6f
        val labelHeight = maxOf(18f, headerHeightDp)
        val baseArrayHeight = MARGIN * 2 + labelHeight * 2 + 12f + depth * (cellHeight + VERTICAL_GAP)
        val emptyBounds = Rect2D(cellLeft, MARGIN + labelHeight + 6f, maxOf(130f, cellWidth), maxOf(38f, cellHeight))
        val arrayHeight = if (includeEmptyState && !includeLinked) {
            maxOf(baseArrayHeight, emptyBounds.bottom + 6f + MARGIN + labelHeight)
        } else baseArrayHeight
        val array = (0 until depth).associate { index ->
            val top = MARGIN + labelHeight + 6f + (depth - 1 - index) * (cellHeight + VERTICAL_GAP)
            "stack_array:$index" to Rect2D(cellLeft, top, cellWidth, cellHeight)
        }
        val arrayIndices = array.mapValues { (_, rect) -> Rect2D(MARGIN, rect.top, indexWidth, rect.height) }
        val topLabel = Rect2D(MARGIN, MARGIN, maxOf(180f, headerWidthDp), labelHeight)
        val bottomLabel = Rect2D(MARGIN, arrayHeight - MARGIN - labelHeight, maxOf(180f, headerWidthDp), labelHeight)

        val headWidth = maxOf(HEAD_WIDTH, headLabelWidthDp)
        val headHeight = maxOf(30f, headLabelHeightDp + 12f, nullLabelHeightDp + 12f)
        val linkedLeft = MARGIN + headWidth + 8f
        val linked = (0 until depth).associate { index ->
            "stack_linked:$index" to Rect2D(linkedLeft + index * (nodeWidth + LINK_GAP), MARGIN + labelHeight + 8f, nodeWidth, nodeHeight)
        }.takeIf { includeLinked }.orEmpty()
        val linkedLines = linked.entries.zipWithNext().mapIndexed { index, (a, b) ->
            "stack_linked:$index->stack_linked:${index + 1}" to
                LineSegment2D(Point2D(a.value.right, a.value.center.y), Point2D(b.value.left, b.value.center.y))
        }.toMap()
        val linkedY = MARGIN + labelHeight + 8f + (nodeHeight - headHeight) / 2f
        val head = Rect2D(MARGIN, linkedY, headWidth, headHeight)
        val nullBounds = Rect2D(linkedLeft + depth * (nodeWidth + LINK_GAP), linkedY, maxOf(NULL_WIDTH, nullLabelWidthDp), headHeight)
        val arrayTopLabelWidth = cellLeft + cellWidth + 6f + 42f + MARGIN
        val width = maxOf(arrayTopLabelWidth, topLabel.right + MARGIN, bottomLabel.right + MARGIN, if (includeLinked) nullBounds.right + MARGIN else 0f)
        val height = if (includeLinked) maxOf(arrayHeight, MARGIN + labelHeight + 8f + nodeHeight + MARGIN) else arrayHeight
        return StackSceneLayoutResult(array, arrayIndices, linked, linkedLines, emptyBounds, topLabel, bottomLabel, head, nullBounds, width, height)
    }

    fun initialFitScale(layout: StackSceneLayoutResult, viewportWidth: Float, viewportHeight: Float): Float =
        minOf(1f, viewportWidth / layout.requiredSceneWidth, viewportHeight / layout.requiredSceneHeight)
}
