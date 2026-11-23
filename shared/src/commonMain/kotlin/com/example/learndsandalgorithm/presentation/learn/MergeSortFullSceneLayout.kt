package com.example.learndsandalgorithm.presentation.learn

import kotlin.math.max

internal data class MergeSortFullSceneLayoutResult(
    val treeRegionBounds: Rect2D,
    val detailRegionBounds: Rect2D,
    val treeTitleBounds: Rect2D,
    val detailTitleBounds: Rect2D,
    val nodeBounds: Map<String, Rect2D>,
    val connectionLines: Map<String, LineSegment2D>,
    val leftRunLabelBounds: Rect2D,
    val leftRunMarkerBounds: Map<Int, Rect2D>,
    val leftRunCellBounds: Map<Int, Rect2D>,
    val rightRunLabelBounds: Rect2D,
    val rightRunMarkerBounds: Map<Int, Rect2D>,
    val rightRunCellBounds: Map<Int, Rect2D>,
    val comparisonBounds: Rect2D,
    val bufferLabelBounds: Rect2D,
    val bufferMarkerBounds: Map<Int, Rect2D>,
    val bufferCellBounds: Map<Int, Rect2D>,
    val bufferIndexBounds: Map<Int, Rect2D>,
    val workingLabelBounds: Rect2D,
    val workingCellBounds: Map<Int, Rect2D>,
    val workingIndexBounds: Map<Int, Rect2D>,
    val emptyLeftRunBounds: Rect2D?,
    val emptyRightRunBounds: Rect2D?,
    val emptyBufferBounds: Rect2D?,
    val emptyWorkingBounds: Rect2D?,
    val footerBounds: Rect2D,
    val nodeWidths: Map<String, Float>,
    val requiredSceneWidth: Float,
    val requiredSceneHeight: Float
)

/** Sequence-wide compact tree plus active merge geometry. */
internal object MergeSortFullSceneLayout {
    private const val MARGIN = 10f
    private const val REGION_GAP = 14f
    private const val NODE_GAP = 10f
    private const val NODE_HEIGHT = 42f
    private const val LEVEL_GAP = 60f
    private const val TREE_TOP = 42f
    private const val DETAIL_TOP = 8f
    private const val DETAIL_HEADER_HEIGHT = 22f
    private const val RUN_LABEL_HEIGHT = 16f
    private const val MARKER_HEIGHT = 14f
    private const val CELL_HEIGHT = 42f
    private const val CELL_GAP = 6f
    private const val MIN_CELL_WIDTH = 50f
    private const val CONTENT_PAD = 10f

    fun calculate(sequence: List<MergeSortFullSceneFrame>): MergeSortFullSceneLayoutResult {
        if (sequence.isEmpty()) return emptyLayout()
        val first = sequence.first().meta.traceStep
        val input = first.array
        require(sequence.all { it.meta.traceStep.array == input }) { "Full Merge Sort input must remain stable across frames" }

        val finalNodes = sequence.flatMap { it.meta.traceStep.nodes }
            .groupBy { it.nodeId }
            .mapValues { (_, versions) ->
                versions.maxBy { node ->
                    (if (node.leftChildId != null) 100 else 0) +
                        (if (node.rightChildId != null) 100 else 0) +
                        (if (node.isCompleted) 10 else 0) +
                        (if (node.hasEntered) 5 else 0)
                }
            }
        require(finalNodes.isNotEmpty()) { "A full Merge Sort trace must contain its root node" }
        val nodeWidths = finalNodes.mapValues { (_, node) ->
            max(
                MIN_NODE_WIDTH,
                max(
                    estimatedWidth(MergeSortFullSceneBuilder.rangeLabel(node.rangeStart, node.rangeEndExclusive), 10f) + 14f,
                    estimatedWidth(if (node.isBaseCase) "BASE CASE" else "SORTED", 8.5f) + 14f
                )
            )
        }
        val subtreeWidths = mutableMapOf<String, Float>()
        fun measureSubtree(nodeId: String): Float {
            val node = finalNodes.getValue(nodeId)
            val children = listOfNotNull(node.leftChildId, node.rightChildId)
            val childrenWidth = children.sumOf { measureSubtree(it).toDouble() }.toFloat() +
                NODE_GAP * (children.size - 1).coerceAtLeast(0)
            return max(nodeWidths.getValue(nodeId), childrenWidth).also { subtreeWidths[nodeId] = it }
        }
        val root = finalNodes.values.singleOrNull { it.parentNodeId == null }
            ?: error("Expected exactly one recursion root")
        val treeContentWidth = measureSubtree(root.nodeId)
        val maximumDepth = finalNodes.values.maxOf { it.callDepth }
        val treeWidth = treeContentWidth + MARGIN * 2f
        val treeHeight = TREE_TOP + maximumDepth * LEVEL_GAP + NODE_HEIGHT + MARGIN
        val treeRegion = Rect2D(MARGIN, DETAIL_TOP, treeWidth, treeHeight)
        val nodeBounds = mutableMapOf<String, Rect2D>()
        fun place(nodeId: String, left: Float) {
            val node = finalNodes.getValue(nodeId)
            val subtreeWidth = subtreeWidths.getValue(nodeId)
            val nodeWidth = nodeWidths.getValue(nodeId)
            val x = left + (subtreeWidth - nodeWidth) / 2f
            nodeBounds[nodeId] = Rect2D(x, TREE_TOP + node.callDepth * LEVEL_GAP, nodeWidth, NODE_HEIGHT)
            val children = listOfNotNull(node.leftChildId, node.rightChildId)
            val childrenWidth = children.sumOf { subtreeWidths.getValue(it).toDouble() }.toFloat() +
                NODE_GAP * (children.size - 1).coerceAtLeast(0)
            var childLeft = left + (subtreeWidth - childrenWidth) / 2f
            children.forEach { childId ->
                place(childId, childLeft)
                childLeft += subtreeWidths.getValue(childId) + NODE_GAP
            }
        }
        place(root.nodeId, MARGIN)
        val allConnections = finalNodes.values.mapNotNull { node ->
            val parentId = node.parentNodeId ?: return@mapNotNull null
            val parent = nodeBounds.getValue(parentId)
            val child = nodeBounds.getValue(node.nodeId)
            MergeSortFullSceneBuilder.branchKey(parentId, node.nodeId) to LineSegment2D(
                Point2D(parent.center.x, parent.bottom),
                Point2D(child.center.x, child.top)
            )
        }.toMap()

        val numberWidth = max(input.maxOfOrNull { it.toString().length } ?: 1, input.size.toString().length)
        val cellWidth = max(MIN_CELL_WIDTH, numberWidth * 9.5f + 18f)
        val maximumLeftRun = sequence.maxOf { it.meta.traceStep.leftRun.size }
        val maximumRightRun = sequence.maxOf { it.meta.traceStep.rightRun.size }
        val arrayWidth = rowWidth(input.size, cellWidth)
        val leftRunWidth = rowWidth(maximumLeftRun, cellWidth)
        val rightRunWidth = rowWidth(maximumRightRun, cellWidth)
        val detailStrings = sequence.map { indicatorText(it.meta.traceStep) }
        val detailTextWidth = detailStrings.maxOf { estimatedWidth(it, 10.5f) }
        val detailTitleWidth = estimatedWidth("MERGE DETAIL · AUXILIARY BUFFER", 10.5f)
        val combinedRunsWidth = leftRunWidth + RUN_GROUP_GAP + rightRunWidth
        val detailContentWidth = maxOf(
            arrayWidth + MARKER_WIDTH,
            combinedRunsWidth + MARKER_WIDTH / 2f,
            detailTextWidth,
            detailTitleWidth
        )
        val detailWidth = max(MIN_DETAIL_WIDTH, detailContentWidth + CONTENT_PAD * 2f)
        val detailLeft = treeRegion.right + REGION_GAP
        val detailRegion = Rect2D(detailLeft, DETAIL_TOP, detailWidth, DETAIL_HEIGHT)
        val detailX = detailLeft + CONTENT_PAD
        val leftRunX = detailX
        val rightRunX = detailX + leftRunWidth + RUN_GROUP_GAP
        val bufferX = detailX + max(0f, (detailContentWidth - arrayWidth) / 2f)
        val workingX = bufferX

        val leftRunCells = (0 until maximumLeftRun).associateWith { index -> cellRect(leftRunX, index, cellWidth, LEFT_CELLS_TOP) }
        val rightRunCells = (0 until maximumRightRun).associateWith { index -> cellRect(rightRunX, index, cellWidth, RIGHT_CELLS_TOP) }
        val bufferCells = input.indices.associateWith { index -> cellRect(bufferX, index, cellWidth, BUFFER_CELLS_TOP) }
        val workingCells = input.indices.associateWith { index -> cellRect(workingX, index, cellWidth, WORKING_CELLS_TOP) }
        val leftMarkers = markerBounds(maximumLeftRun, leftRunX, cellWidth, LEFT_MARKERS_TOP)
        val rightMarkers = markerBounds(maximumRightRun, rightRunX, cellWidth, RIGHT_MARKERS_TOP)
        val bufferMarkers = markerBounds(input.size, bufferX, cellWidth, BUFFER_MARKERS_TOP)
        val leftRunLabel = Rect2D(leftRunX, RUN_LABEL_TOP, leftRunWidth, RUN_LABEL_HEIGHT)
        val rightRunLabel = Rect2D(rightRunX, RUN_LABEL_TOP, rightRunWidth, RUN_LABEL_HEIGHT)
        val bufferLabel = Rect2D(detailX, BUFFER_LABEL_TOP, detailContentWidth, RUN_LABEL_HEIGHT)
        val workingLabel = Rect2D(detailX, WORKING_LABEL_TOP, detailContentWidth, RUN_LABEL_HEIGHT)
        val comparisonBounds = Rect2D(detailLeft + MARGIN, COMPARISON_TOP, detailWidth - MARGIN * 2f, COMPARISON_HEIGHT)
        val footerBounds = Rect2D(detailLeft + MARGIN, FOOTER_TOP, detailWidth - MARGIN * 2f, FOOTER_HEIGHT)

        return MergeSortFullSceneLayoutResult(
            treeRegionBounds = treeRegion,
            detailRegionBounds = detailRegion,
            treeTitleBounds = Rect2D(treeRegion.left, DETAIL_TOP, treeRegion.width, TREE_TITLE_HEIGHT),
            detailTitleBounds = Rect2D(detailLeft + MARGIN, DETAIL_TOP, detailWidth - MARGIN * 2f, DETAIL_HEADER_HEIGHT),
            nodeBounds = nodeBounds,
            connectionLines = allConnections,
            leftRunLabelBounds = leftRunLabel,
            leftRunMarkerBounds = leftMarkers,
            leftRunCellBounds = leftRunCells,
            rightRunLabelBounds = rightRunLabel,
            rightRunMarkerBounds = rightMarkers,
            rightRunCellBounds = rightRunCells,
            comparisonBounds = comparisonBounds,
            bufferLabelBounds = bufferLabel,
            bufferMarkerBounds = bufferMarkers,
            bufferCellBounds = bufferCells,
            bufferIndexBounds = bufferCells.mapValues { (_, bounds) -> Rect2D(bounds.left + 1f, bounds.top + INDEX_TOP_OFFSET, bounds.width - 2f, INDEX_HEIGHT) },
            workingLabelBounds = workingLabel,
            workingCellBounds = workingCells,
            workingIndexBounds = workingCells.mapValues { (_, bounds) -> Rect2D(bounds.left + 1f, bounds.top + INDEX_TOP_OFFSET, bounds.width - 2f, INDEX_HEIGHT) },
            emptyLeftRunBounds = if (maximumLeftRun == 0) Rect2D(leftRunX, LEFT_CELLS_TOP, EMPTY_RUN_WIDTH, CELL_HEIGHT) else null,
            emptyRightRunBounds = if (maximumRightRun == 0) Rect2D(rightRunX, RIGHT_CELLS_TOP, EMPTY_RUN_WIDTH, CELL_HEIGHT) else null,
            emptyBufferBounds = if (input.isEmpty()) Rect2D(bufferX, BUFFER_CELLS_TOP, EMPTY_RUN_WIDTH, CELL_HEIGHT) else null,
            emptyWorkingBounds = if (input.isEmpty()) Rect2D(workingX, WORKING_CELLS_TOP, EMPTY_RUN_WIDTH, CELL_HEIGHT) else null,
            footerBounds = footerBounds,
            nodeWidths = nodeWidths,
            requiredSceneWidth = detailRegion.right + MARGIN,
            requiredSceneHeight = max(treeHeight, DETAIL_HEIGHT) + DETAIL_TOP + MARGIN
        )
    }

    internal fun indicatorText(step: MergeSortFullTraceStep): String = when (step.event) {
        FullMergeEvent.START -> "Input ready · recurse, divide, and merge."
        FullMergeEvent.CALL_ENTER -> "Enter ${step.activeRange?.let { MergeSortFullSceneBuilder.rangeLabel(it.start, it.endExclusive) }.orEmpty()} · depth ${step.callStack.size - 1}."
        FullMergeEvent.DIVIDE -> "Split ${step.activeRange?.let { MergeSortFullSceneBuilder.rangeLabel(it.start, it.endExclusive) }} at ${step.splitIndex}."
        FullMergeEvent.BASE_CASE -> "Base case · ${step.activeRange?.size ?: 0} value(s) already sorted."
        FullMergeEvent.MERGE_START -> "Merge ${step.leftRun.size} left + ${step.rightRun.size} right → buffer."
        FullMergeEvent.COMPARE -> {
            val pair = step.currentComparison
            if (pair == null) "Compare the first available values."
            else "Compare ${pair.leftElement.value} (L) with ${pair.rightElement.value} (R)."
        }
        FullMergeEvent.WRITE_BUFFER -> "${step.selectedSource} ${step.selectedElement?.value} → buffer[${step.newlyWrittenBufferSlot}]."
        FullMergeEvent.FLUSH_REMAINING -> "Flush ${step.selectedElement?.value} → buffer[${step.newlyWrittenBufferSlot}]."
        FullMergeEvent.COPY_BACK -> "Copy ${step.selectedElement?.value} to working[${step.copyBackIndex}] · ${step.copyBackProgress}/${step.activeRange?.size}."
        FullMergeEvent.RETURN -> "Return sorted range ${step.activeRange?.let { MergeSortFullSceneBuilder.rangeLabel(it.start, it.endExclusive) }} to caller."
        FullMergeEvent.COMPLETE -> "Sorted result · stable order preserved."
    }

    internal fun rangeText(range: MergeIndexRange): String = MergeSortFullSceneBuilder.rangeLabel(range.start, range.endExclusive)

    private fun cellRect(left: Float, index: Int, cellWidth: Float, top: Float) =
        Rect2D(left + index * (cellWidth + CELL_GAP), top, cellWidth, CELL_HEIGHT)

    private fun markerBounds(count: Int, rowLeft: Float, cellWidth: Float, top: Float): Map<Int, Rect2D> =
        (0..count).associateWith { index ->
            val centerX = if (index < count) rowLeft + index * (cellWidth + CELL_GAP) + cellWidth / 2f
            else if (count > 0) rowLeft + count * cellWidth + (count - 1) * CELL_GAP + CELL_GAP / 2f
            else rowLeft + EMPTY_RUN_WIDTH / 2f
            Rect2D(centerX - MARKER_WIDTH / 2f, top, MARKER_WIDTH, MARKER_HEIGHT)
        }

    private fun rowWidth(count: Int, cellWidth: Float) = if (count == 0) EMPTY_RUN_WIDTH else count * cellWidth + (count - 1) * CELL_GAP
    private fun estimatedWidth(text: String, fontSize: Float): Float = text.length * fontSize * 0.62f

    private fun emptyLayout() = MergeSortFullSceneLayoutResult(
        treeRegionBounds = EMPTY_RECT,
        detailRegionBounds = EMPTY_RECT,
        treeTitleBounds = EMPTY_RECT,
        detailTitleBounds = EMPTY_RECT,
        nodeBounds = emptyMap(),
        connectionLines = emptyMap(),
        leftRunLabelBounds = EMPTY_RECT,
        leftRunMarkerBounds = emptyMap(),
        leftRunCellBounds = emptyMap(),
        rightRunLabelBounds = EMPTY_RECT,
        rightRunMarkerBounds = emptyMap(),
        rightRunCellBounds = emptyMap(),
        comparisonBounds = EMPTY_RECT,
        bufferLabelBounds = EMPTY_RECT,
        bufferMarkerBounds = emptyMap(),
        bufferCellBounds = emptyMap(),
        bufferIndexBounds = emptyMap(),
        workingLabelBounds = EMPTY_RECT,
        workingCellBounds = emptyMap(),
        workingIndexBounds = emptyMap(),
        emptyLeftRunBounds = null,
        emptyRightRunBounds = null,
        emptyBufferBounds = null,
        emptyWorkingBounds = null,
        footerBounds = EMPTY_RECT,
        nodeWidths = emptyMap(),
        requiredSceneWidth = 0f,
        requiredSceneHeight = 0f
    )

    private const val MIN_NODE_WIDTH = 66f
    private const val MIN_DETAIL_WIDTH = 304f
    private const val EMPTY_RUN_WIDTH = 86f
    private const val MARKER_WIDTH = 20f
    private const val TREE_TITLE_HEIGHT = 22f
    private const val RUN_GROUP_GAP = 14f
    private const val RUN_LABEL_TOP = 34f
    private const val LEFT_MARKERS_TOP = 50f
    private const val LEFT_CELLS_TOP = 64f
    private const val RIGHT_MARKERS_TOP = 50f
    private const val RIGHT_CELLS_TOP = 64f
    private const val COMPARISON_TOP = 110f
    private const val COMPARISON_HEIGHT = 34f
    private const val BUFFER_LABEL_TOP = 147f
    private const val BUFFER_MARKERS_TOP = 163f
    private const val BUFFER_CELLS_TOP = 177f
    private const val WORKING_LABEL_TOP = 222f
    private const val WORKING_CELLS_TOP = 238f
    private const val INDEX_TOP_OFFSET = 26f
    private const val INDEX_HEIGHT = 14f
    private const val FOOTER_TOP = 284f
    private const val FOOTER_HEIGHT = 16f
    private const val DETAIL_HEIGHT = 296f
    private val EMPTY_RECT = Rect2D(0f, 0f, 0f, 0f)
}
