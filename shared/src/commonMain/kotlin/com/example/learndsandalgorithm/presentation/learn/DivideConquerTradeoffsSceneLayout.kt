package com.example.learndsandalgorithm.presentation.learn

import kotlin.math.max

internal data class TradeoffTreeEdgeLayout(
    val connectionKey: String,
    val line: LineSegment2D,
    val labelBounds: Rect2D,
    val label: String
)

internal data class TradeoffTreePanelLayout(
    val panelBounds: Rect2D,
    val titleBounds: Rect2D,
    val depthBounds: Rect2D,
    val workBounds: Rect2D,
    val causeBounds: Rect2D,
    val nodeBounds: Map<String, Rect2D>,
    val edges: List<TradeoffTreeEdgeLayout>
)

internal data class DivideConquerTradeoffsSceneLayoutResult(
    val sceneBounds: Rect2D,
    val titleBounds: Rect2D,
    val legendBounds: Map<TradeoffNodeStatus, Rect2D>,
    val balancedTree: TradeoffTreePanelLayout?,
    val unbalancedTree: TradeoffTreePanelLayout?,
    val comparisonHeaderBounds: Rect2D?,
    val comparisonColumnHeaderBounds: List<Rect2D>,
    val comparisonRowBounds: Map<String, Rect2D>,
    val comparisonCells: Map<String, List<Rect2D>>,
    val mergeExampleBounds: Rect2D?,
    val quickExampleBounds: Rect2D?,
    val mergeInputLabelBounds: Rect2D?,
    val mergeInputRecordBounds: List<Rect2D>,
    val mergeOutputLabelBounds: Rect2D?,
    val mergeOutputRecordBounds: List<Rect2D>,
    val mergeBufferLabelBounds: Rect2D?,
    val mergeBufferRecordBounds: List<Rect2D>,
    val quickInputLabelBounds: Rect2D?,
    val quickInputRecordBounds: List<Rect2D>,
    val quickOutputLabelBounds: Rect2D?,
    val quickOutputRecordBounds: List<Rect2D>,
    val quickDisclaimerBounds: Rect2D?,
    val requiredSceneWidth: Float,
    val requiredSceneHeight: Float
)

internal object DivideConquerTradeoffsSceneLayout {
    const val COMPARISON_MATRIX_HEADING = "COMPLEXITY & TRADE-OFF MATRIX:"
    private const val OUTER_MARGIN = 14f
    private const val TREE_PANEL_GAP = 12f
    private const val TREE_SIDE_PADDING = 14f
    private const val TREE_NODE_WIDTH = 38f
    private const val TREE_NODE_HEIGHT = 24f
    private const val TREE_LEVEL_PITCH = 32f
    private const val MIN_LEAF_CENTER_GAP = 20f
    private const val MIN_NODE_CENTER_GAP = TREE_NODE_WIDTH
    private const val MIN_SIBLING_CENTER_GAP = 40f
    private const val TREE_HEADER_HEIGHT = 18f
    private const val TREE_HEADER_TOP = 8f
    private const val TREE_CONTENT_TOP = 30f
    private const val TREE_FOOTER_GAP = 9f
    private const val TREE_FOOTER_HEIGHT = 62f

    fun calculate(sequence: List<DivideConquerTradeoffsSceneFrame>): DivideConquerTradeoffsSceneLayoutResult {
        if (sequence.isEmpty()) return emptyLayout()
        val first = sequence.first().meta
        return when (first.source.scenario) {
            TradeoffScenario.BALANCED_VS_UNBALANCED_SPLIT -> calculateSplit(sequence)
            TradeoffScenario.ALGORITHM_COMPARISON -> calculateComparison(sequence)
        }
    }

    private fun calculateSplit(sequence: List<DivideConquerTradeoffsSceneFrame>): DivideConquerTradeoffsSceneLayoutResult {
        val maxN = sequence.maxOf { it.meta.source.inputN }
        val balancedNodes = sequence.flatMap { it.meta.balancedNodes }.distinctBy { it.id }
        val unbalancedNodes = sequence.flatMap { it.meta.unbalancedNodes }.distinctBy { it.id }
        val topologies = listOf(balancedNodes, unbalancedNodes)
        val leafCenters = compactLeafCenters(maxN, topologies)
        val maxDepth = sequence.maxOf { frame ->
            maxOf(
                frame.meta.balancedNodes.maxOfOrNull { it.depth } ?: 0,
                frame.meta.unbalancedNodes.maxOfOrNull { it.depth } ?: 0
            )
        }
        val leafSpan = leafCenters.lastOrNull()?.minus(leafCenters.firstOrNull() ?: 0f) ?: 0f
        val treeWidth = max(260f, TREE_SIDE_PADDING * 2f + TREE_NODE_WIDTH + leafSpan)
        val panelHeight = TREE_CONTENT_TOP + maxDepth * TREE_LEVEL_PITCH + TREE_NODE_HEIGHT + TREE_FOOTER_GAP + TREE_FOOTER_HEIGHT
        val titleBounds = Rect2D(OUTER_MARGIN, 8f, treeWidth * 2f + TREE_PANEL_GAP, 20f)
        val legendTop = titleBounds.bottom + 5f
        val legendBounds = TradeoffNodeStatus.entries.mapIndexed { index, status ->
            status to Rect2D(OUTER_MARGIN + index * 120f, legendTop, 112f, 18f)
        }.toMap()
        val panelTop = legendTop + 27f
        val left = Rect2D(OUTER_MARGIN, panelTop, treeWidth, panelHeight)
        val right = Rect2D(left.right + TREE_PANEL_GAP, panelTop, treeWidth, panelHeight)
        val balancedPanel = treePanel(left, balancedNodes, leafCenters)
        val unbalancedPanel = treePanel(right, unbalancedNodes, leafCenters)
        val bounds = Rect2D(0f, 0f, right.right + OUTER_MARGIN, panelTop + panelHeight + OUTER_MARGIN)
        return emptyLayout().copy(
            sceneBounds = bounds,
            titleBounds = titleBounds,
            legendBounds = legendBounds,
            balancedTree = balancedPanel,
            unbalancedTree = unbalancedPanel,
            requiredSceneWidth = bounds.width,
            requiredSceneHeight = bounds.height
        )
    }

    private fun compactLeafCenters(
        leafCount: Int,
        topologies: List<List<TradeoffRecursionNode>>
    ): List<Float> {
        if (leafCount <= 0) return emptyList()

        val centers = MutableList(leafCount) { index -> index * MIN_LEAF_CENTER_GAP }
        for (index in 0 until leafCount - 1) {
            val sharedDepthLeafPair = topologies.any { nodes ->
                val left = nodes.firstOrNull { it.low == index && it.highExclusive == index + 1 }
                val right = nodes.firstOrNull { it.low == index + 1 && it.highExclusive == index + 2 }
                left != null && right != null && left.depth == right.depth
            }
            if (sharedDepthLeafPair) {
                shiftLeafSuffix(centers, index + 1, MIN_SIBLING_CENTER_GAP - MIN_LEAF_CENTER_GAP)
            }
        }

        val rangeConstraints = topologies.flatMap { nodes ->
            val adjacentNodesAtDepth = nodes
                .filter { it.low >= 0 && it.highExclusive > it.low && it.highExclusive <= leafCount }
                .groupBy { it.depth }
                .values
                .flatMap { depthNodes ->
                    val ordered = depthNodes.sortedBy { it.low }
                    ordered.zipWithNext().mapNotNull { (left, right) ->
                        if (left.highExclusive == right.low) {
                            LeafCenterConstraint(left, right, MIN_NODE_CENTER_GAP)
                        } else null
                    }
                }
            val siblingNodes = nodes
                .filter { it.parentId != null && it.low >= 0 && it.highExclusive > it.low && it.highExclusive <= leafCount }
                .groupBy { it.parentId }
                .values
                .mapNotNull { children ->
                    val ordered = children.sortedBy { it.low }
                    if (ordered.size == 2 && ordered[0].highExclusive == ordered[1].low) {
                        LeafCenterConstraint(ordered[0], ordered[1], MIN_SIBLING_CENTER_GAP)
                    } else null
                }
            adjacentNodesAtDepth + siblingNodes
        }

        // Moving the right range and every later leaf only increases separations, so this
        // sequence-wide relaxation preserves order while remaining safe for other input sizes.
        var changed: Boolean
        var iteration = 0
        val maximumIterations = (leafCount * (rangeConstraints.size + 1)).coerceAtLeast(1)
        do {
            changed = false
            rangeConstraints.forEach { constraint ->
                val leftCenter = centerForRange(centers, constraint.left)
                val rightCenter = centerForRange(centers, constraint.right)
                if (leftCenter != null && rightCenter != null) {
                    val adjustment = constraint.minimumGap - (rightCenter - leftCenter)
                    if (adjustment > 0f) {
                        shiftLeafSuffix(centers, constraint.right.low, adjustment)
                        changed = true
                    }
                }
            }
            iteration++
        } while (changed && iteration < maximumIterations)

        return centers
    }

    private data class LeafCenterConstraint(
        val left: TradeoffRecursionNode,
        val right: TradeoffRecursionNode,
        val minimumGap: Float
    )

    private fun centerForRange(leafCenters: List<Float>, node: TradeoffRecursionNode): Float? {
        if (node.low < 0 || node.highExclusive <= node.low || node.highExclusive > leafCenters.size) return null
        return (leafCenters[node.low] + leafCenters[node.highExclusive - 1]) / 2f
    }

    private fun shiftLeafSuffix(leafCenters: MutableList<Float>, firstIndex: Int, amount: Float) {
        if (firstIndex !in leafCenters.indices || amount <= 0f) return
        for (index in firstIndex until leafCenters.size) leafCenters[index] += amount
    }

    private fun treePanel(
        panel: Rect2D,
        nodes: List<TradeoffRecursionNode>,
        leafCenters: List<Float>
    ): TradeoffTreePanelLayout {
        val titleBounds = Rect2D(panel.left + 7f, panel.top + TREE_HEADER_TOP, panel.width - 14f, TREE_HEADER_HEIGHT)
        val nodeBounds = nodes.associate { node ->
            val rangeCenter = centerForRange(leafCenters, node)
            val centerX = if (rangeCenter != null) {
                panel.left + TREE_SIDE_PADDING + TREE_NODE_WIDTH / 2f + rangeCenter
            } else {
                panel.center.x
            }
            val bounds = Rect2D(
                centerX - TREE_NODE_WIDTH / 2f,
                panel.top + TREE_CONTENT_TOP + node.depth * TREE_LEVEL_PITCH,
                TREE_NODE_WIDTH,
                TREE_NODE_HEIGHT
            )
            node.id to bounds
        }
        val edges = nodes.mapNotNull { node ->
            val parentId = node.parentId ?: return@mapNotNull null
            val start = nodeBounds[parentId] ?: return@mapNotNull null
            val end = nodeBounds[node.id] ?: return@mapNotNull null
            val midX = (start.center.x + end.center.x) / 2f
            val midY = (start.bottom + end.top) / 2f
            TradeoffTreeEdgeLayout(
                connectionKey = "${node.id}:branch",
                line = LineSegment2D(Point2D(start.center.x, start.bottom), Point2D(end.center.x, end.top)),
                labelBounds = Rect2D(midX - 10f, midY - 4f, 20f, 8f),
                label = "${node.branch}:${node.size}"
            )
        }
        val footerTop = panel.bottom - TREE_FOOTER_HEIGHT + 4f
        return TradeoffTreePanelLayout(
            panelBounds = panel,
            titleBounds = titleBounds,
            depthBounds = Rect2D(panel.left + 8f, footerTop, panel.width - 16f, 18f),
            workBounds = Rect2D(panel.left + 8f, footerTop + 20f, panel.width - 16f, 18f),
            causeBounds = Rect2D(panel.left + 8f, footerTop + 40f, panel.width - 16f, 18f),
            nodeBounds = nodeBounds,
            edges = edges
        )
    }

    private fun calculateComparison(sequence: List<DivideConquerTradeoffsSceneFrame>): DivideConquerTradeoffsSceneLayoutResult {
        val width = 868f
        val title = Rect2D(16f, 8f, width - 32f, 20f)
        val header = Rect2D(16f, 34f, width - 32f, 20f)
        val propertyWidth = 108f
        val mergeWidth = 327f
        val quickWidth = width - 32f - propertyWidth - mergeWidth
        val columnHeaders = listOf(
            Rect2D(16f, 58f, propertyWidth, 20f),
            Rect2D(16f + propertyWidth, 58f, mergeWidth, 20f),
            Rect2D(16f + propertyWidth + mergeWidth, 58f, quickWidth, 20f)
        )
        val rows = sequence.first().meta.comparisonRows
        val rowHeights = mapOf(
            "typical_time" to 40f,
            "worst_time" to 48f,
            "auxiliary_memory" to 58f,
            "stability" to 48f,
            "practical_tradeoff" to 58f
        )
        var top = 82f
        val rowBounds = linkedMapOf<String, Rect2D>()
        val cellBounds = linkedMapOf<String, List<Rect2D>>()
        rows.forEach { row ->
            val height = rowHeights.getValue(row.id)
            val rowRect = Rect2D(16f, top, width - 32f, height)
            rowBounds[row.id] = rowRect
            cellBounds[row.id] = listOf(
                Rect2D(16f, top, propertyWidth, height),
                Rect2D(16f + propertyWidth, top, mergeWidth, height),
                Rect2D(16f + propertyWidth + mergeWidth, top, quickWidth, height)
            )
            top += height
        }
        val panelsTop = top + 10f
        val panelGap = 12f
        val panelWidth = (width - 32f - panelGap) / 2f
        val panelHeight = 138f
        val mergePanel = Rect2D(16f, panelsTop, panelWidth, panelHeight)
        val quickPanel = Rect2D(mergePanel.right + panelGap, panelsTop, panelWidth, panelHeight)
        val recordWidth = 34f
        val recordGap = 4f
        fun recordBounds(panel: Rect2D, rowTop: Float, count: Int = 5): List<Rect2D> {
            val totalWidth = count * recordWidth + (count - 1) * recordGap
            val start = panel.center.x - totalWidth / 2f
            return (0 until count).map { index ->
                Rect2D(start + index * (recordWidth + recordGap), rowTop, recordWidth, 20f)
            }
        }
        val rowBoundsForRecords = panelsTop + 38f
        val outputTop = panelsTop + 73f
        val bufferTop = panelsTop + 108f
        val bottom = panelsTop + panelHeight + 10f
        val sceneBounds = Rect2D(0f, 0f, width, bottom)
        return emptyLayout().copy(
            sceneBounds = sceneBounds,
            titleBounds = title,
            comparisonHeaderBounds = header,
            comparisonColumnHeaderBounds = columnHeaders,
            comparisonRowBounds = rowBounds,
            comparisonCells = cellBounds,
            mergeExampleBounds = mergePanel,
            quickExampleBounds = quickPanel,
            mergeInputLabelBounds = Rect2D(mergePanel.left + 8f, panelsTop + 25f, mergePanel.width - 16f, 12f),
            mergeInputRecordBounds = recordBounds(mergePanel, rowBoundsForRecords),
            mergeOutputLabelBounds = Rect2D(mergePanel.left + 8f, panelsTop + 60f, mergePanel.width - 16f, 12f),
            mergeOutputRecordBounds = recordBounds(mergePanel, outputTop),
            mergeBufferLabelBounds = Rect2D(mergePanel.left + 8f, panelsTop + 95f, mergePanel.width - 16f, 12f),
            mergeBufferRecordBounds = recordBounds(mergePanel, bufferTop),
            quickInputLabelBounds = Rect2D(quickPanel.left + 8f, panelsTop + 25f, quickPanel.width - 16f, 12f),
            quickInputRecordBounds = recordBounds(quickPanel, rowBoundsForRecords),
            quickOutputLabelBounds = Rect2D(quickPanel.left + 8f, panelsTop + 60f, quickPanel.width - 16f, 12f),
            quickOutputRecordBounds = recordBounds(quickPanel, outputTop),
            quickDisclaimerBounds = Rect2D(quickPanel.left + 8f, panelsTop + 108f, quickPanel.width - 16f, 25f),
            requiredSceneWidth = sceneBounds.width,
            requiredSceneHeight = sceneBounds.height
        )
    }

    private fun emptyLayout() = DivideConquerTradeoffsSceneLayoutResult(
        sceneBounds = Rect2D(0f, 0f, 0f, 0f),
        titleBounds = Rect2D(0f, 0f, 0f, 0f),
        legendBounds = emptyMap(),
        balancedTree = null,
        unbalancedTree = null,
        comparisonHeaderBounds = null,
        comparisonColumnHeaderBounds = emptyList(),
        comparisonRowBounds = emptyMap(),
        comparisonCells = emptyMap(),
        mergeExampleBounds = null,
        quickExampleBounds = null,
        mergeInputLabelBounds = null,
        mergeInputRecordBounds = emptyList(),
        mergeOutputLabelBounds = null,
        mergeOutputRecordBounds = emptyList(),
        mergeBufferLabelBounds = null,
        mergeBufferRecordBounds = emptyList(),
        quickInputLabelBounds = null,
        quickInputRecordBounds = emptyList(),
        quickOutputLabelBounds = null,
        quickOutputRecordBounds = emptyList(),
        quickDisclaimerBounds = null,
        requiredSceneWidth = 0f,
        requiredSceneHeight = 0f
    )
}
