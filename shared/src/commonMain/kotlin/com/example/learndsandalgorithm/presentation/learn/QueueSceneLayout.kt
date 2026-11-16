package com.example.learndsandalgorithm.presentation.learn

internal data class QueueSceneLayoutResult(
    val arraySlotBounds: Map<Int, Rect2D>,
    val arrayIndexBounds: Map<Int, Rect2D>,
    val arrayValueBounds: Map<Int, Rect2D>,
    val frontRearLabelBounds: Map<Int, Rect2D>,
    val linkedHeadingBounds: Rect2D,
    val linkedNodeBounds: Map<String, Rect2D>,
    val linkedConnectionLines: Map<String, LineSegment2D>,
    val linkedFrontBounds: Rect2D,
    val linkedNullBounds: Rect2D,
    val requiredSceneWidth: Float,
    val requiredSceneHeight: Float
)

internal data class QueueSceneLayoutArgs(
    val capacity: Int,
    val maxItems: Int,
    val showLinked: Boolean,
    val maxValueWidthDp: Float = 28f,
    val frontLabelWidthDp: Float = 58f,
    val frontTextHeightDp: Float = 18f,
    val terminalLabelWidthDp: Float = 62f,
    val pointerTextHeightDp: Float = 14f,
    val valueTextHeightDp: Float = 24f,
    val indexTextHeightDp: Float = 12f,
    val linkedHeadingHeightDp: Float = 14f,
    val linkedValueHeightDp: Float = 14f,
    val linkedNextHeightDp: Float = 12f
)

internal object QueueSceneLayout {
    private const val MARGIN = 12f
    private const val CELL_WIDTH = 58f
    private const val CELL_HEIGHT = 44f
    private const val CELL_GAP = 4f
    private const val NODE_WIDTH = 48f
    private const val NODE_HEIGHT = 42f
    private const val LINK_GAP = 10f

    fun calculate(args: QueueSceneLayoutArgs): QueueSceneLayoutResult = calculate(
        capacity = args.capacity,
        maxItems = args.maxItems,
        showLinked = args.showLinked,
        maxValueWidthDp = args.maxValueWidthDp,
        frontLabelWidthDp = args.frontLabelWidthDp,
        frontTextHeightDp = args.frontTextHeightDp,
        terminalLabelWidthDp = args.terminalLabelWidthDp,
        pointerTextHeightDp = args.pointerTextHeightDp,
        valueTextHeightDp = args.valueTextHeightDp,
        indexTextHeightDp = args.indexTextHeightDp,
        linkedHeadingHeightDp = args.linkedHeadingHeightDp,
        linkedValueHeightDp = args.linkedValueHeightDp,
        linkedNextHeightDp = args.linkedNextHeightDp
    )

    fun calculate(
        capacity: Int,
        maxItems: Int,
        showLinked: Boolean,
        maxValueWidthDp: Float = 28f,
        frontLabelWidthDp: Float = 58f,
        frontTextHeightDp: Float = 18f,
        terminalLabelWidthDp: Float = 62f,
        pointerTextHeightDp: Float = 14f,
        valueTextHeightDp: Float = 24f,
        indexTextHeightDp: Float = 12f,
        linkedHeadingHeightDp: Float = 14f,
        linkedValueHeightDp: Float = 14f,
        linkedNextHeightDp: Float = 12f
    ): QueueSceneLayoutResult {
        require(capacity > 0)
        val cellWidth = maxOf(CELL_WIDTH, maxValueWidthDp + 20f)
        val nodeWidth = maxOf(NODE_WIDTH, maxValueWidthDp + 12f)
        val pointerHeight = maxOf(18f, pointerTextHeightDp + 4f)
        val cellHeight = maxOf(CELL_HEIGHT, valueTextHeightDp + 10f)
        val indexHeight = maxOf(16f, indexTextHeightDp + 4f)
        val slots = (0 until capacity).associateWith { index ->
            Rect2D(MARGIN + index * (cellWidth + CELL_GAP), MARGIN + pointerHeight + 4f, cellWidth, cellHeight)
        }
        val indices = slots.mapValues { (_, rect) -> Rect2D(rect.left, rect.bottom + 4f, rect.width, indexHeight) }
        val values = slots.mapValues { (_, rect) -> Rect2D(rect.left + 2f, rect.top + 3f, rect.width - 4f, rect.height - 6f) }
        val pointerLabels = slots.mapValues { (_, rect) -> Rect2D(rect.left, MARGIN, rect.width, pointerHeight) }
        val linkedFrontWidth = maxOf(58f, frontLabelWidthDp)
        val linkedLeft = MARGIN + linkedFrontWidth + 6f
        val nodeHeight = maxOf(NODE_HEIGHT, linkedValueHeightDp + linkedNextHeightDp + 8f)
        val linked = (0 until maxItems.coerceAtLeast(0)).associate { index ->
            "queue_linked:$index" to Rect2D(linkedLeft + index * (nodeWidth + LINK_GAP), 0f, nodeWidth, nodeHeight)
        }.takeIf { showLinked }.orEmpty()
        val headingTop = (indices.values.maxOfOrNull { it.bottom } ?: MARGIN) + 12f
        val heading = Rect2D(MARGIN, headingTop, 0f, maxOf(16f, linkedHeadingHeightDp + 2f))
        val linkedNodeTop = heading.bottom + 8f
        val linkedNodes = linked.mapValues { (_, rect) -> rect.copy(top = linkedNodeTop) }
        val frontHeight = maxOf(30f, frontTextHeightDp + 12f)
        val front = if (showLinked) Rect2D(MARGIN, linkedNodeTop + (nodeHeight - frontHeight) / 2f, linkedFrontWidth, frontHeight) else Rect2D(0f, 0f, 0f, 0f)
        val terminalBounds = if (showLinked) {
            Rect2D(linkedLeft + maxItems.coerceAtLeast(0) * (nodeWidth + LINK_GAP), front.top, maxOf(62f, terminalLabelWidthDp), front.height)
        } else Rect2D(0f, 0f, 0f, 0f)
        val arrayRight = slots.values.maxOfOrNull { it.right } ?: MARGIN
        val width = maxOf(arrayRight + MARGIN, if (showLinked) terminalBounds.right + MARGIN else 0f)
        val arrayHeight = (indices.values.maxOfOrNull { it.bottom } ?: MARGIN) + MARGIN
        val height = if (showLinked) linkedNodeTop + nodeHeight + MARGIN else arrayHeight
        val finalHeading = if (showLinked) heading.copy(width = width - MARGIN * 2f) else Rect2D(0f, 0f, 0f, 0f)
        val linkedLines = linkedNodes.entries.zipWithNext().mapIndexed { index, (a, b) ->
            "queue_linked:$index->queue_linked:${index + 1}" to
                LineSegment2D(Point2D(a.value.right, a.value.center.y), Point2D(b.value.left, b.value.center.y))
        }.toMap()
        return QueueSceneLayoutResult(slots, indices, values, pointerLabels, finalHeading, linkedNodes, linkedLines, front, terminalBounds, width, height)
    }

    fun initialFitScale(layout: QueueSceneLayoutResult, viewportWidth: Float, viewportHeight: Float): Float =
        minOf(1f, viewportWidth / layout.requiredSceneWidth, viewportHeight / layout.requiredSceneHeight)
}
