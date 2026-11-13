package com.example.learndsandalgorithm.presentation.learn

import kotlin.math.max
import kotlin.math.sqrt

/**
 * Pure Kotlin Multiplatform layout calculator for arbitrary binary tree scenes.
 * Supports complete, incomplete, left-skewed, right-skewed, and asymmetric binary trees
 * using a tidy contour-based tree layout algorithm with exact boundary-to-boundary contour gap separation.
 */
internal object TreeSceneLayout {

    const val MIN_NODE_SIZE_DP = 40f
    const val MIN_LEVEL_GAP_DP = 56f
    const val MIN_SIBLING_GAP_DP = 24f
    const val MIN_HORIZONTAL_MARGIN_DP = 24f
    const val MIN_VERTICAL_MARGIN_DP = 24f

    data class TreeLayoutResult(
        val nodeBounds: Map<String, Rect2D>,           // slotKey -> Rect2D ("tree_slot:N5" -> Rect)
        val connectionLines: Map<String, LineSegment2D>, // connectionKey -> LineSegment (boundary to boundary)
        val requiredWidth: Float,
        val requiredHeight: Float
    )

    private class SubtreeLayout(
        val nodePositions: MutableMap<String, Point2D> = mutableMapOf(), // nodeId -> relative (x, y)
        val leftContour: MutableMap<Int, Float> = mutableMapOf(),       // depth -> min X
        val rightContour: MutableMap<Int, Float> = mutableMapOf()       // depth -> max X
    )

    fun calculateLayout(
        nodes: List<TraversalNode>,
        availableWidthDp: Float = 400f,
        availableHeightDp: Float = 400f
    ): TreeLayoutResult {
        if (nodes.isEmpty()) {
            return TreeLayoutResult(emptyMap(), emptyMap(), 0f, 0f)
        }

        val nodeMap = nodes.associateBy { it.id }
        val rootData = findRoot(nodes) ?: return TreeLayoutResult(emptyMap(), emptyMap(), 0f, 0f)

        // Recursive contour builder
        fun buildSubtree(nodeId: String?): SubtreeLayout {
            val layout = SubtreeLayout()
            if (nodeId == null) return layout

            val node = nodeMap[nodeId] ?: return layout
            val leftLayout = buildSubtree(node.leftId)
            val rightLayout = buildSubtree(node.rightId)

            val hasLeft = node.leftId != null && leftLayout.nodePositions.isNotEmpty()
            val hasRight = node.rightId != null && rightLayout.nodePositions.isNotEmpty()

            val nodeWidth = MIN_NODE_SIZE_DP
            val siblingGap = MIN_SIBLING_GAP_DP

            when {
                !hasLeft && !hasRight -> {
                    // Leaf
                    layout.nodePositions[node.id] = Point2D(0f, 0f)
                    layout.leftContour[0] = 0f
                    layout.rightContour[0] = nodeWidth
                }
                hasLeft && !hasRight -> {
                    // Left child only
                    val leftChildPos = leftLayout.nodePositions[node.leftId!!]!!
                    val childX = leftChildPos.x
                    val parentX = childX
                    layout.nodePositions[node.id] = Point2D(parentX, 0f)

                    layout.leftContour[0] = parentX
                    layout.rightContour[0] = parentX + nodeWidth
                    leftLayout.nodePositions.forEach { (id, pt) ->
                        layout.nodePositions[id] = Point2D(pt.x, pt.y + MIN_LEVEL_GAP_DP)
                    }
                    leftLayout.leftContour.forEach { (d, x) -> layout.leftContour[d + 1] = x }
                    leftLayout.rightContour.forEach { (d, x) -> layout.rightContour[d + 1] = x }
                }
                !hasLeft && hasRight -> {
                    // Right child only
                    val rightChildPos = rightLayout.nodePositions[node.rightId!!]!!
                    val childX = rightChildPos.x
                    val parentX = childX
                    layout.nodePositions[node.id] = Point2D(parentX, 0f)

                    layout.leftContour[0] = parentX
                    layout.rightContour[0] = parentX + nodeWidth
                    rightLayout.nodePositions.forEach { (id, pt) ->
                        layout.nodePositions[id] = Point2D(pt.x, pt.y + MIN_LEVEL_GAP_DP)
                    }
                    rightLayout.leftContour.forEach { (d, x) -> layout.leftContour[d + 1] = x }
                    rightLayout.rightContour.forEach { (d, x) -> layout.rightContour[d + 1] = x }
                }
                hasLeft && hasRight -> {
                    // Both children exist: calculate separation based on boundary-to-boundary contours
                    val overlapDepths = leftLayout.rightContour.keys.intersect(rightLayout.leftContour.keys)
                    var maxShift = 0f
                    overlapDepths.forEach { d ->
                        val leftExt = leftLayout.rightContour[d] ?: 0f
                        val rightExt = rightLayout.leftContour[d] ?: 0f
                        val currentGap = rightExt - leftExt
                        val requiredGap = siblingGap // 24dp boundary-to-boundary gap
                        if (currentGap < requiredGap) {
                            maxShift = max(maxShift, requiredGap - currentGap)
                        }
                    }

                    val halfShift = maxShift / 2f
                    val leftShift = -halfShift
                    val rightShift = halfShift

                    // Apply shifts to subtrees
                    leftLayout.nodePositions.forEach { (id, pt) ->
                        leftLayout.nodePositions[id] = Point2D(pt.x + leftShift, pt.y)
                    }
                    val shiftedLeftRightContour = mutableMapOf<Int, Float>()
                    leftLayout.rightContour.forEach { (d, x) -> shiftedLeftRightContour[d] = x + leftShift }
                    val shiftedLeftLeftContour = mutableMapOf<Int, Float>()
                    leftLayout.leftContour.forEach { (d, x) -> shiftedLeftLeftContour[d] = x + leftShift }

                    rightLayout.nodePositions.forEach { (id, pt) ->
                        rightLayout.nodePositions[id] = Point2D(pt.x + rightShift, pt.y)
                    }
                    val shiftedRightLeftContour = mutableMapOf<Int, Float>()
                    rightLayout.leftContour.forEach { (d, x) -> shiftedRightLeftContour[d] = x + rightShift }
                    val shiftedRightRightContour = mutableMapOf<Int, Float>()
                    rightLayout.rightContour.forEach { (d, x) -> shiftedRightRightContour[d] = x + rightShift }

                    // Parent exactly centered between child root centers
                    val leftRootX = leftLayout.nodePositions[node.leftId!!]!!.x
                    val rightRootX = rightLayout.nodePositions[node.rightId!!]!!.x
                    val parentX = (leftRootX + rightRootX) / 2f

                    layout.nodePositions[node.id] = Point2D(parentX, 0f)
                    layout.leftContour[0] = parentX
                    layout.rightContour[0] = parentX + nodeWidth

                    // Combine subtrees into layout
                    leftLayout.nodePositions.forEach { (id, pt) ->
                        layout.nodePositions[id] = Point2D(pt.x, pt.y + MIN_LEVEL_GAP_DP)
                    }
                    shiftedLeftLeftContour.forEach { (d, x) -> layout.leftContour[d + 1] = x }
                    shiftedLeftRightContour.forEach { (d, x) -> layout.rightContour[d + 1] = x }

                    rightLayout.nodePositions.forEach { (id, pt) ->
                        layout.nodePositions[id] = Point2D(pt.x, pt.y + MIN_LEVEL_GAP_DP)
                    }
                    shiftedRightLeftContour.forEach { (d, x) ->
                        if (!layout.leftContour.containsKey(d + 1)) layout.leftContour[d + 1] = x
                        else layout.leftContour[d + 1] = minOf(layout.leftContour[d + 1]!!, x)
                    }
                    shiftedRightRightContour.forEach { (d, x) -> layout.rightContour[d + 1] = x }
                }
            }

            return layout
        }

        val rootLayout = buildSubtree(rootData.id)

        // Absolute positioning normalization (min X >= MIN_HORIZONTAL_MARGIN_DP, min Y >= MIN_VERTICAL_MARGIN_DP)
        val minX = rootLayout.nodePositions.values.minOfOrNull { it.x } ?: 0f
        val xShift = if (minX < MIN_HORIZONTAL_MARGIN_DP) MIN_HORIZONTAL_MARGIN_DP - minX else -minX + MIN_HORIZONTAL_MARGIN_DP

        val absolutePositions = mutableMapOf<String, Point2D>()
        rootLayout.nodePositions.forEach { (id, pt) ->
            absolutePositions[id] = Point2D(pt.x + xShift, pt.y + MIN_VERTICAL_MARGIN_DP)
        }

        // 2. Build nodeBounds
        val nodeBounds = mutableMapOf<String, Rect2D>()
        nodes.forEach { node ->
            val slotKey = "tree_slot:${node.id}"
            val pt = absolutePositions[node.id] ?: Point2D(MIN_HORIZONTAL_MARGIN_DP, MIN_VERTICAL_MARGIN_DP)
            nodeBounds[slotKey] = Rect2D(
                left = pt.x,
                top = pt.y,
                width = MIN_NODE_SIZE_DP,
                height = MIN_NODE_SIZE_DP
            )
        }

        // 3. Build connectionLines (boundary-to-boundary)
        val connectionLines = mutableMapOf<String, LineSegment2D>()
        nodes.forEach { node ->
            val parentSlotKey = "tree_slot:${node.id}"
            val parentRect = nodeBounds[parentSlotKey] ?: return@forEach

            node.leftId?.let { leftChildId ->
                val childSlotKey = "tree_slot:$leftChildId"
                val childRect = nodeBounds[childSlotKey]
                if (childRect != null) {
                    connectionLines["branch:$parentSlotKey->$childSlotKey"] =
                        calculateBoundaryConnectionLine(parentRect, childRect)
                }
            }

            node.rightId?.let { rightChildId ->
                val childSlotKey = "tree_slot:$rightChildId"
                val childRect = nodeBounds[childSlotKey]
                if (childRect != null) {
                    connectionLines["branch:$parentSlotKey->$childSlotKey"] =
                        calculateBoundaryConnectionLine(parentRect, childRect)
                }
            }
        }

        val maxX = nodeBounds.values.maxOfOrNull(Rect2D::right) ?: availableWidthDp
        val maxY = nodeBounds.values.maxOfOrNull(Rect2D::bottom) ?: availableHeightDp

        return TreeLayoutResult(
            nodeBounds = nodeBounds,
            connectionLines = connectionLines,
            requiredWidth = max(availableWidthDp, maxX + MIN_HORIZONTAL_MARGIN_DP),
            requiredHeight = max(availableHeightDp, maxY + MIN_VERTICAL_MARGIN_DP)
        )
    }

    /**
     * Pure geometry helper calculating boundary-to-boundary line segment between two node rectangles.
     */
    fun calculateBoundaryConnectionLine(
        fromRect: Rect2D,
        toRect: Rect2D
    ): LineSegment2D {
        val fromCenter = fromRect.center
        val toCenter = toRect.center
        val radius = fromRect.width / 2f

        val dX = toCenter.x - fromCenter.x
        val dY = toCenter.y - fromCenter.y
        val dist = max(0.001f, sqrt(dX * dX + dY * dY))

        val startPoint = Point2D(
            x = fromCenter.x + (dX / dist) * radius,
            y = fromCenter.y + (dY / dist) * radius
        )
        val endPoint = Point2D(
            x = toCenter.x - (dX / dist) * radius,
            y = toCenter.y - (dY / dist) * radius
        )

        return LineSegment2D(start = startPoint, end = endPoint)
    }

    private fun findRoot(nodes: List<TraversalNode>): TraversalNode? {
        val childIds = mutableSetOf<String>()
        nodes.forEach { node ->
            node.leftId?.let { childIds.add(it) }
            node.rightId?.let { childIds.add(it) }
        }
        val root = nodes.firstOrNull { it.id !in childIds }
        return root ?: nodes.firstOrNull()
    }
}
