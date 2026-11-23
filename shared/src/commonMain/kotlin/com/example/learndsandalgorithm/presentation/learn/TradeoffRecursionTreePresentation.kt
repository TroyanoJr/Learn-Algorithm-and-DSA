package com.example.learndsandalgorithm.presentation.learn

internal enum class TradeoffTreeSelection { BALANCED, UNBALANCED }

internal enum class TradeoffPlaybackPhaseKind { ROOT, DIVIDE, BASE_CASES, COMPLETE }

internal const val TRADEOFF_NODE_TRANSITION_MILLIS = 650
internal const val TRADEOFF_REVEAL_STAGGER_MILLIS = 80

internal data class TradeoffPlaybackPhase(
    val id: String,
    val selection: TradeoffTreeSelection,
    val semanticStepIndex: Int,
    val revealDepth: Int,
    val kind: TradeoffPlaybackPhaseKind,
    val label: String,
    val operation: String,
    val explanation: String
)

internal data class TradeoffTreePresentationEdge(
    val connectionKey: String,
    val parentId: String,
    val childId: String,
    val visible: Boolean,
    val visualState: ConnectionVisualState
)

internal data class TradeoffTreeLevel(
    val depth: Int,
    val nodeCount: Int,
    val visibleCount: Int,
    val activeCount: Int,
    val completedCount: Int,
    val rangeSegments: List<TradeoffTreeRangeSegment>
)

internal data class TradeoffTreeRangeSegment(
    val nodeId: String,
    val startFraction: Float,
    val endFraction: Float
)

internal data class TradeoffTreeAnnotations(
    val depth: String,
    val work: String,
    val cause: String
)

internal data class TradeoffTreePresentationModel(
    val selection: TradeoffTreeSelection,
    val title: String,
    val inputSize: Int,
    val nodes: List<TradeoffRecursionNode>,
    val edges: List<TradeoffTreePresentationEdge>,
    val focusNode: TradeoffRecursionNode?,
    val focusChildren: List<TradeoffRecursionNode>,
    val levels: List<TradeoffTreeLevel>,
    val annotations: TradeoffTreeAnnotations
)

internal data class TradeoffPartitionBarLayout(
    val nodeId: String,
    val bounds: Rect2D
)

internal data class TradeoffPartitionConnectorLayout(
    val connectionKey: String,
    val parentId: String,
    val childId: String,
    val line: LineSegment2D
)

internal data class TradeoffPartitionDiagramLayout(
    val width: Float,
    val height: Float,
    val barBounds: Map<String, TradeoffPartitionBarLayout>,
    val connectors: List<TradeoffPartitionConnectorLayout>
)

internal fun tradeoffTreePresentationModel(
    frame: DivideConquerTradeoffsSceneFrame,
    selection: TradeoffTreeSelection,
    revealDepth: Int? = null,
    forceCompleted: Boolean = false
): TradeoffTreePresentationModel {
    val sourceNodes: List<TradeoffRecursionNode>
    val sceneId: String
    val title: String
    val annotations: TradeoffTreeAnnotations
    when (selection) {
        TradeoffTreeSelection.BALANCED -> {
            val depth = frame.meta.source.goodTreeDepth ?: 4
            sourceNodes = frame.meta.balancedNodes
            sceneId = DivideConquerTradeoffsSceneBuilder.BALANCED_TREE_SCENE_ID
            title = "BALANCED RECURSION · N/2 + N/2"
            annotations = TradeoffTreeAnnotations(
                "DEPTH O(log N) · $depth edges / ${depth + 1} node levels",
                "WORK / LEVEL O(N) · TOTAL O(N log N)",
                "Near-equal splits keep recursion shallow"
            )
        }
        TradeoffTreeSelection.UNBALANCED -> {
            val n = frame.meta.source.inputN
            val calls = frame.meta.source.badTreeDepth ?: n
            val levels = (frame.meta.unbalancedNodes.maxOfOrNull { it.depth } ?: 0) + 1
            sourceNodes = frame.meta.unbalancedNodes
            sceneId = DivideConquerTradeoffsSceneBuilder.UNBALANCED_TREE_SCENE_ID
            title = "UNBALANCED RECURSION · 1 + (N−1)"
            annotations = TradeoffTreeAnnotations(
                "DEPTH O(N) · $calls calls / $levels node levels",
                "N + … + 1 = ${n * (n + 1) / 2} · TOTAL O(N²)",
                "Sorted input + last pivot → 1 + (N−1)"
            )
        }
    }
    val nodes = if (revealDepth == null) sourceNodes else sourceNodes.map { node ->
        val visible = node.depth <= revealDepth
        node.copy(
            visible = visible,
            status = when {
                !visible -> TradeoffNodeStatus.PENDING
                forceCompleted || node.depth < revealDepth -> TradeoffNodeStatus.COMPLETED
                else -> TradeoffNodeStatus.ACTIVE
            }
        )
    }
    val visibleNodes = nodes.filter { it.visible }
    val active = visibleNodes.firstOrNull { it.status == TradeoffNodeStatus.ACTIVE }
    val focus = active ?: visibleNodes.firstOrNull { it.parentId == null }
    val scene = frame.scene.subScenes.single { it.subPaneId == sceneId }
    val currentConnections = scene.connections.associateBy { it.connectionKey }
    val idFromKey: (String) -> String = { it.substringAfterLast(":node:") }
    val edges = scene.referenceConnections.map { edge ->
        val parentId = idFromKey(edge.fromVisualKey)
        val childId = idFromKey(edge.toVisualKey)
        TradeoffTreePresentationEdge(
            connectionKey = edge.connectionKey,
            parentId = parentId,
            childId = childId,
            visible = nodes.single { it.id == parentId }.visible && nodes.single { it.id == childId }.visible,
            visualState = if (revealDepth != null) {
                val child = nodes.single { it.id == childId }
                when {
                    !child.visible -> ConnectionVisualState.DEFAULT
                    forceCompleted || child.depth < revealDepth -> ConnectionVisualState.PATH
                    else -> ConnectionVisualState.HIGHLIGHTED
                }
            } else {
                currentConnections[edge.connectionKey]?.visualState ?: ConnectionVisualState.DEFAULT
            }
        )
    }
    val inputSize = nodes.firstOrNull { it.parentId == null }?.size ?: 0
    val levels = nodes.groupBy { it.depth }.toSortedMap().map { (depth, levelNodes) ->
        TradeoffTreeLevel(
            depth = depth,
            nodeCount = levelNodes.size,
            visibleCount = levelNodes.count { it.visible },
            activeCount = levelNodes.count { it.status == TradeoffNodeStatus.ACTIVE },
            completedCount = levelNodes.count { it.status == TradeoffNodeStatus.COMPLETED },
            rangeSegments = levelNodes.sortedBy { it.low }.map { node ->
                TradeoffTreeRangeSegment(
                    nodeId = node.id,
                    startFraction = node.low.toFloat() / inputSize,
                    endFraction = node.highExclusive.toFloat() / inputSize
                )
            }
        )
    }
    return TradeoffTreePresentationModel(
        selection = selection,
        title = title,
        inputSize = inputSize,
        nodes = nodes,
        edges = edges,
        focusNode = focus,
        focusChildren = focus?.let { parent -> nodes.filter { it.parentId == parent.id && it.visible }.sortedBy { it.branch } }.orEmpty(),
        levels = levels,
        annotations = annotations
    )
}

internal fun tradeoffPlaybackPhases(
    frames: List<DivideConquerTradeoffsSceneFrame>,
    selection: TradeoffTreeSelection
): List<TradeoffPlaybackPhase> {
    require(frames.size == 4) { "Partition Split Balance requires the four original semantic snapshots" }
    val maxDepth = frames.maxOf { frame ->
        val nodes = if (selection == TradeoffTreeSelection.BALANCED) frame.meta.balancedNodes else frame.meta.unbalancedNodes
        nodes.maxOfOrNull { it.depth } ?: 0
    }
    val semanticDivisionStep = if (selection == TradeoffTreeSelection.BALANCED) 1 else 2
    val prefix = selection.name.lowercase()
    return buildList {
        val sourceNodes = if (selection == TradeoffTreeSelection.BALANCED) {
            frames.last().meta.balancedNodes
        } else {
            frames.last().meta.unbalancedNodes
        }
        val inputSize = frames.first().meta.source.inputN
        add(TradeoffPlaybackPhase(
            id = "$prefix:root",
            selection = selection,
            semanticStepIndex = 0,
            revealDepth = 0,
            kind = TradeoffPlaybackPhaseKind.ROOT,
            label = "Create root problem N=$inputSize",
            operation = "Create root call N=$inputSize [0,$inputSize)",
            explanation = frames.first().meta.source.explanation
        ))
        for (depth in 1..maxDepth) {
            val parents = sourceNodes.filter { it.depth == depth - 1 && it.size > 1 }.sortedBy { it.low }
            val splits = parents.joinToString("; ") { parent ->
                val children = sourceNodes.filter { it.parentId == parent.id }.sortedBy { it.low }
                "${parent.size} ${parent.rangeLabel} → " + children.joinToString(" + ") { child ->
                    "${child.size} ${child.rangeLabel}"
                }
            }
            val kind = if (depth == maxDepth) TradeoffPlaybackPhaseKind.BASE_CASES else TradeoffPlaybackPhaseKind.DIVIDE
            val explanation = if (kind == TradeoffPlaybackPhaseKind.BASE_CASES) {
                "Final partitions: $splits. All ${sourceNodes.count { it.size == 1 }} size-one calls are now visible. " +
                    "${if (selection == TradeoffTreeSelection.BALANCED) "The nearly equal divisions have reached their leaves." else "Each remaining continuing call has reduced to a single element."}"
            } else {
                "Depth $depth partitions: $splits. " +
                    if (selection == TradeoffTreeSelection.UNBALANCED) {
                        "Follow the N−1 child as the continuing recursive call; the size-one child is a base case."
                    } else {
                        "Each child is a smaller recursive subproblem; work at this level spans the input ranges."
                    }
            }
            add(TradeoffPlaybackPhase(
                id = "$prefix:$depth",
                selection = selection,
                semanticStepIndex = semanticDivisionStep,
                revealDepth = depth,
                kind = kind,
                label = when {
                    depth == maxDepth -> "${selection.displayName()} base cases revealed"
                    selection == TradeoffTreeSelection.BALANCED -> "Balanced division reaches depth $depth"
                    else -> "Follow the N−1 branch to depth $depth"
                },
                operation = if (kind == TradeoffPlaybackPhaseKind.BASE_CASES) {
                    "Final partition: $splits; expose ${sourceNodes.count { it.size == 1 }} size-one base cases"
                } else "Partition at depth $depth: $splits",
                explanation = explanation
            ))
        }
        add(TradeoffPlaybackPhase(
            id = "$prefix:complete",
            selection = selection,
            semanticStepIndex = 3,
            revealDepth = maxDepth,
            kind = TradeoffPlaybackPhaseKind.COMPLETE,
            label = "${selection.displayName()} tree complete",
            operation = frames.last().meta.source.badgeText,
            explanation = frames.last().meta.source.explanation
        ))
    }
}

private fun TradeoffTreeSelection.displayName(): String =
    if (this == TradeoffTreeSelection.BALANCED) "Balanced" else "Unbalanced"

internal fun selectTradeoffTree(
    current: TradeoffTreeSelection,
    next: TradeoffTreeSelection,
    balancedPlayback: VisualizerPlaybackState,
    unbalancedPlayback: VisualizerPlaybackState
): TradeoffTreeSelection {
    if (current != next) {
        if (current == TradeoffTreeSelection.BALANCED) balancedPlayback.isPlaying = false
        else unbalancedPlayback.isPlaying = false
    }
    return next
}

internal fun tradeoffTreeRevealOrder(nodes: List<TradeoffRecursionNode>): List<TradeoffRecursionNode> =
    tradeoffTreeNodeOrder(nodes).filter { it.visible }

internal fun tradeoffTreeNodeOrder(nodes: List<TradeoffRecursionNode>): List<TradeoffRecursionNode> =
    nodes.sortedWith(compareBy(TradeoffRecursionNode::depth, TradeoffRecursionNode::low))

internal fun tradeoffNodeRevealDelayMillis(model: TradeoffTreePresentationModel, node: TradeoffRecursionNode): Int =
    model.nodes.asSequence().filter { it.depth == node.depth }.sortedBy { it.low }
        .indexOfFirst { it.id == node.id }.coerceAtLeast(0) * TRADEOFF_REVEAL_STAGGER_MILLIS

internal fun tradeoffConnectionRevealDelayMillis(
    model: TradeoffTreePresentationModel,
    edge: TradeoffTreePresentationEdge
): Int {
    val child = model.nodes.single { it.id == edge.childId }
    return model.edges.asSequence()
        .filter { model.nodes.single { node -> node.id == it.childId }.depth == child.depth }
        .sortedBy { model.nodes.single { node -> node.id == it.childId }.low }
        .indexOfFirst { it.connectionKey == edge.connectionKey }.coerceAtLeast(0) * TRADEOFF_REVEAL_STAGGER_MILLIS
}

/** Fits the entire topology into the actual measured viewport for both tree presentations. */
internal object TradeoffRecursionTreeLayout {
    const val MAX_BAR_HEIGHT = 22f
    const val UNBALANCED_MAX_BAR_HEIGHT = 26f
    const val UNBALANCED_NODE_WIDTH_FRACTION = 0.84f

    fun widthFor(availableWidth: Float): Float = availableWidth.coerceAtLeast(0f)

    fun calculateDiagram(
        model: TradeoffTreePresentationModel,
        availableWidth: Float,
        availableHeight: Float
    ): TradeoffPartitionDiagramLayout {
        val width = widthFor(availableWidth)
        if (model.nodes.isEmpty() || width <= 0f || availableHeight <= 0f) {
            return TradeoffPartitionDiagramLayout(width, 0f, emptyMap(), emptyList())
        }
        if (model.selection == TradeoffTreeSelection.UNBALANCED) {
            return calculateUnbalancedChain(model, width, availableHeight)
        }
        val levelCount = (model.nodes.maxOfOrNull { it.depth } ?: 0) + 1
        val levelPitch = availableHeight / levelCount
        val barHeight = minOf(MAX_BAR_HEIGHT, levelPitch * 0.78f)
        val centers = model.nodes.associate { node ->
            node.id to width * (node.low + node.highExclusive) / (2f * model.inputSize)
        }
        val widths = model.nodes.groupBy { it.depth }.values.flatMap { level ->
            val ordered = level.sortedBy { centers.getValue(it.id) }
            ordered.mapIndexed { index, node ->
                val center = centers.getValue(node.id)
                val leftGap = if (index == 0) center * 2f else center - centers.getValue(ordered[index - 1].id)
                val rightGap = if (index == ordered.lastIndex) (width - center) * 2f else centers.getValue(ordered[index + 1].id) - center
                node.id to minOf(leftGap - 4f, rightGap - 4f).coerceAtLeast(1f)
            }
        }.toMap()
        val bars = model.nodes.associate { node ->
            val nodeWidth = widths.getValue(node.id)
            val center = centers.getValue(node.id)
            node.id to TradeoffPartitionBarLayout(
                nodeId = node.id,
                bounds = Rect2D(
                    center - nodeWidth / 2f,
                    node.depth * levelPitch + (levelPitch - barHeight) / 2f,
                    nodeWidth,
                    barHeight
                )
            )
        }
        val connectors = model.edges.map { edge ->
            val parent = bars.getValue(edge.parentId).bounds
            val child = bars.getValue(edge.childId).bounds
            TradeoffPartitionConnectorLayout(
                connectionKey = edge.connectionKey,
                parentId = edge.parentId,
                childId = edge.childId,
                line = LineSegment2D(
                    Point2D(parent.center.x, parent.bottom),
                    Point2D(child.center.x, child.top)
                )
            )
        }
        return TradeoffPartitionDiagramLayout(width, availableHeight, bars, connectors)
    }

    private fun calculateUnbalancedChain(
        model: TradeoffTreePresentationModel,
        width: Float,
        availableHeight: Float
    ): TradeoffPartitionDiagramLayout {
        val chain = model.nodes.filter { it.highExclusive == model.inputSize }.sortedBy { it.depth }
        val chainIds = chain.map { it.id }.toSet()
        val cellWidth = width / model.inputSize
        val nodeWidth = (cellWidth * UNBALANCED_NODE_WIDTH_FRACTION).coerceAtLeast(1f)
        val rowHeight = availableHeight / 2f
        val barHeight = minOf(UNBALANCED_MAX_BAR_HEIGHT, rowHeight * 0.74f)
        val mainTop = (rowHeight - barHeight) / 2f
        val baseTop = rowHeight + mainTop
        val bars = model.nodes.associate { node ->
            val chainNode = node.id in chainIds
            val column = node.low
            val centerX = (column + 0.5f) * cellWidth
            val y = if (chainNode) mainTop else baseTop
            node.id to TradeoffPartitionBarLayout(
                nodeId = node.id,
                bounds = Rect2D(centerX - nodeWidth / 2f, y, nodeWidth, barHeight)
            )
        }
        val nextChainId = chain.zipWithNext().associate { (parent, child) -> parent.id to child.id }
        val edges = model.edges.map { edge ->
            val parent = bars.getValue(edge.parentId).bounds
            val child = bars.getValue(edge.childId).bounds
            val line = if (nextChainId[edge.parentId] == edge.childId) {
                LineSegment2D(
                    Point2D(parent.right, parent.center.y),
                    Point2D(child.left, child.center.y)
                )
            } else {
                LineSegment2D(
                    Point2D(parent.center.x, parent.bottom),
                    Point2D(child.center.x, child.top)
                )
            }
            TradeoffPartitionConnectorLayout(edge.connectionKey, edge.parentId, edge.childId, line)
        }
        return TradeoffPartitionDiagramLayout(width, availableHeight, bars, edges)
    }
}

