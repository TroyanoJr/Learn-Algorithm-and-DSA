package com.example.learndsandalgorithm.presentation.learn

import kotlin.math.max

internal data class DivideConquerSceneLayoutResult(
    val nodeBounds: Map<String, Rect2D>,
    val connectionLines: Map<String, LineSegment2D>,
    val connectionLabelBounds: Map<String, Rect2D>,
    val phaseHeaderBounds: Rect2D,
    val phaseTitleBounds: Rect2D,
    val treeTitleBounds: Rect2D,
    val phaseBadgeBounds: Map<DivideConquerPhase, Rect2D>,
    val combineBounds: Rect2D,
    val requiredSceneWidth: Float,
    val requiredSceneHeight: Float
)

internal object DivideConquerSceneLayout {
    const val TREE_TITLE_TEXT = "RECURSIVE SUBPROBLEM TREE (max(arr, low, high)):"

    private const val MARGIN = 20f
    private const val HEADER_TOP = 12f
    private const val HEADER_HEIGHT = 76f
    private const val TREE_TITLE_TOP = 96f
    private const val TREE_TITLE_HEIGHT = 18f
    private const val BADGE_WIDTH = 112f
    private const val BADGE_HEIGHT = 30f
    private const val BADGE_GAP = 12f
    private const val TREE_TOP = 120f
    private const val NODE_HEIGHT = 74f
    private const val LEVEL_GAP = 26f
    private const val SIBLING_GAP = 28f
    private const val MIN_NODE_WIDTH = 108f
    private const val CARD_HORIZONTAL_PADDING = 32f
    private const val COMBINE_GAP = 22f
    private const val COMBINE_HEIGHT = 52f
    private const val EDGE_LABEL_WIDTH = 20f
    private const val EDGE_LABEL_HEIGHT = 18f

    private data class SubtreeLayout(
        val nodeBounds: Map<String, Rect2D>,
        val leftContour: Map<Int, Float>,
        val rightContour: Map<Int, Float>
    )

    fun calculate(frames: List<DivideConquerSceneFrame>): DivideConquerSceneLayoutResult {
        if (frames.isEmpty()) {
            return DivideConquerSceneLayoutResult(
                nodeBounds = emptyMap(),
                connectionLines = emptyMap(),
                connectionLabelBounds = emptyMap(),
                phaseHeaderBounds = Rect2D(0f, 0f, 0f, 0f),
                phaseTitleBounds = Rect2D(0f, 0f, 0f, 0f),
                treeTitleBounds = Rect2D(0f, 0f, 0f, 0f),
                phaseBadgeBounds = emptyMap(),
                combineBounds = Rect2D(0f, 0f, 0f, 0f),
                requiredSceneWidth = 0f,
                requiredSceneHeight = 0f
            )
        }

        val nodePresentations = linkedMapOf<String, DivideConquerNodePresentation>()
        val measuredWidths = mutableMapOf<String, Float>()
        frames.forEach { frame ->
            frame.meta.nodePresentations.values.forEach { node ->
                nodePresentations[node.id] = node
                measuredWidths[node.id] = max(measuredWidths[node.id] ?: 0f, preferredNodeWidth(node))
            }
        }
        require(nodePresentations.containsKey("root")) { "Divide & conquer scene sequence must include its root" }

        val entitiesByVisualKey = frames
            .flatMap { frame -> frame.scene.subScenes.flatMap { it.entities } }
            .associateBy { it.visualKey }
        val referenceConnections = frames
            .flatMap { frame -> frame.scene.subScenes.flatMap { it.referenceConnections } }
            .distinctBy { it.connectionKey }
        val leftChildren = mutableMapOf<String, String>()
        val rightChildren = mutableMapOf<String, String>()
        referenceConnections.forEach { connection ->
            val parent = entitiesByVisualKey[connection.fromVisualKey]
                ?: error("Reference connection has no parent entity: ${connection.connectionKey}")
            val child = entitiesByVisualKey[connection.toVisualKey]
                ?: error("Reference connection has no child entity: ${connection.connectionKey}")
            val parentId = parent.elementId.removePrefix("node:")
            val childId = child.elementId.removePrefix("node:")
            when (connection.label) {
                "L" -> leftChildren[parentId] = childId
                "R" -> rightChildren[parentId] = childId
                else -> error("Divide & conquer branch must be labeled L or R")
            }
        }

        val childIds = (leftChildren.values + rightChildren.values).toSet()
        val roots = nodePresentations.keys.filter { it !in childIds }
        require(roots == listOf("root")) { "Divide & conquer scene sequence must have one root" }

        val subtree = buildSubtree(
            nodeId = "root",
            nodePresentations = nodePresentations,
            measuredWidths = measuredWidths,
            leftChildren = leftChildren,
            rightChildren = rightChildren
        )
        val minTreeLeft = subtree.nodeBounds.values.minOf { it.left }
        val maxTreeRight = subtree.nodeBounds.values.maxOf { it.right }
        val treeWidth = maxTreeRight - minTreeLeft
        val badgeRowWidth = BADGE_WIDTH * DivideConquerPhase.entries.size + BADGE_GAP * (DivideConquerPhase.entries.size - 1)
        val requiredWidth = max(treeWidth + MARGIN * 2, badgeRowWidth + MARGIN * 2)
        val xShift = MARGIN - minTreeLeft
        val nodeBounds = subtree.nodeBounds.mapValues { (_, bounds) ->
            Rect2D(bounds.left + xShift, bounds.top + TREE_TOP, bounds.width, bounds.height)
        }

        val connectionLines = linkedMapOf<String, LineSegment2D>()
        val connectionLabelBounds = linkedMapOf<String, Rect2D>()
        referenceConnections.forEach { connection ->
            val parent = entitiesByVisualKey.getValue(connection.fromVisualKey).elementId.removePrefix("node:")
            val child = entitiesByVisualKey.getValue(connection.toVisualKey).elementId.removePrefix("node:")
            val parentBounds = nodeBounds.getValue(parent)
            val childBounds = nodeBounds.getValue(child)
            val line = LineSegment2D(
                start = Point2D(parentBounds.center.x, parentBounds.bottom),
                end = Point2D(childBounds.center.x, childBounds.top)
            )
            connectionLines[connection.connectionKey] = line
            connectionLabelBounds[connection.connectionKey] = Rect2D(
                left = (line.start.x + line.end.x) / 2f - EDGE_LABEL_WIDTH / 2f,
                top = (line.start.y + line.end.y) / 2f - EDGE_LABEL_HEIGHT / 2f,
                width = EDGE_LABEL_WIDTH,
                height = EDGE_LABEL_HEIGHT
            )
        }

        val phaseHeader = Rect2D(0f, HEADER_TOP, requiredWidth, HEADER_HEIGHT)
        val badgeRowLeft = (requiredWidth - badgeRowWidth) / 2f
        val phaseBadges = DivideConquerPhase.entries.mapIndexed { index, phase ->
            phase to Rect2D(
                left = badgeRowLeft + index * (BADGE_WIDTH + BADGE_GAP),
                top = HEADER_TOP + 38f,
                width = BADGE_WIDTH,
                height = BADGE_HEIGHT
            )
        }.toMap()
        val combineTop = nodeBounds.values.maxOf { it.bottom } + COMBINE_GAP
        val combineBounds = Rect2D(MARGIN, combineTop, requiredWidth - MARGIN * 2, COMBINE_HEIGHT)
        val requiredHeight = combineBounds.bottom + MARGIN

        return DivideConquerSceneLayoutResult(
            nodeBounds = nodeBounds,
            connectionLines = connectionLines,
            connectionLabelBounds = connectionLabelBounds,
            phaseHeaderBounds = phaseHeader,
            phaseTitleBounds = Rect2D(MARGIN, HEADER_TOP + 8f, requiredWidth - MARGIN * 2, 18f),
            treeTitleBounds = Rect2D(MARGIN, TREE_TITLE_TOP, requiredWidth - MARGIN * 2, TREE_TITLE_HEIGHT),
            phaseBadgeBounds = phaseBadges,
            combineBounds = combineBounds,
            requiredSceneWidth = requiredWidth,
            requiredSceneHeight = requiredHeight
        )
    }

    private fun buildSubtree(
        nodeId: String,
        nodePresentations: Map<String, DivideConquerNodePresentation>,
        measuredWidths: Map<String, Float>,
        leftChildren: Map<String, String>,
        rightChildren: Map<String, String>
    ): SubtreeLayout {
        val node = nodePresentations[nodeId] ?: error("Missing divide & conquer node: $nodeId")
        val nodeWidth = measuredWidths[nodeId] ?: preferredNodeWidth(node)
        val leftId = leftChildren[nodeId]
        val rightId = rightChildren[nodeId]
        val leftSubtree = leftId?.let { buildSubtree(it, nodePresentations, measuredWidths, leftChildren, rightChildren) }
        val rightSubtree = rightId?.let { buildSubtree(it, nodePresentations, measuredWidths, leftChildren, rightChildren) }

        var leftShift = 0f
        var rightShift = 0f
        if (leftSubtree != null && rightSubtree != null) {
            var requiredShift = 0f
            leftSubtree.rightContour.keys.intersect(rightSubtree.leftContour.keys).forEach { depth ->
                val currentGap = rightSubtree.leftContour.getValue(depth) - leftSubtree.rightContour.getValue(depth)
                requiredShift = max(requiredShift, SIBLING_GAP - currentGap)
            }
            leftShift = -requiredShift / 2f
            rightShift = requiredShift / 2f
        }

        val nodeBounds = linkedMapOf<String, Rect2D>()
        nodeBounds[nodeId] = Rect2D(-nodeWidth / 2f, 0f, nodeWidth, NODE_HEIGHT)
        val leftContour = mutableMapOf(0 to -nodeWidth / 2f)
        val rightContour = mutableMapOf(0 to nodeWidth / 2f)

        fun includeSubtree(subtree: SubtreeLayout, shift: Float) {
            subtree.nodeBounds.forEach { (id, bounds) ->
                nodeBounds[id] = Rect2D(bounds.left + shift, bounds.top + NODE_HEIGHT + LEVEL_GAP, bounds.width, bounds.height)
            }
            subtree.leftContour.forEach { (depth, left) ->
                val adjustedDepth = depth + 1
                val adjustedLeft = left + shift
                leftContour[adjustedDepth] = minOf(leftContour[adjustedDepth] ?: adjustedLeft, adjustedLeft)
            }
            subtree.rightContour.forEach { (depth, right) ->
                val adjustedDepth = depth + 1
                val adjustedRight = right + shift
                rightContour[adjustedDepth] = max(rightContour[adjustedDepth] ?: adjustedRight, adjustedRight)
            }
        }

        leftSubtree?.let { includeSubtree(it, leftShift) }
        rightSubtree?.let { includeSubtree(it, rightShift) }

        return SubtreeLayout(nodeBounds, leftContour, rightContour)
    }

    private fun preferredNodeWidth(node: DivideConquerNodePresentation): Float {
        val lines = listOfNotNull(node.valuesLabel, node.rangeLabel, node.roleLabel, node.maxLabel)
        val measuredContent = lines.maxOfOrNull { line ->
            val fontSize = when (line) {
                node.valuesLabel -> 15f
                node.rangeLabel -> 11f
                node.maxLabel -> 11f
                else -> 10f
            }
            line.length * fontSize * 0.68f
        } ?: 0f
        return max(MIN_NODE_WIDTH, measuredContent + CARD_HORIZONTAL_PADDING)
    }
}
