package com.example.learndsandalgorithm.presentation.learn

import kotlin.math.max
import kotlin.math.sqrt

/** Deterministic geometry for arbitrary rooted trees, built directly from structural connections. */
internal object GeneralTreeSceneLayout {
    const val NODE_DIAMETER_DP = 40f
    const val LEVEL_SPACING_DP = 56f
    const val MIN_NODE_SEPARATION_DP = 24f
    const val HORIZONTAL_MARGIN_DP = 24f
    const val VERTICAL_MARGIN_DP = 24f

    data class LayoutResult(
        val nodeBounds: Map<String, Rect2D>,
        val connectionLines: Map<String, LineSegment2D>,
        val requiredWidth: Float,
        val requiredHeight: Float
    )

    fun calculateLayout(subScene: StructureSubScene): LayoutResult {
        val entities = subScene.entities
        if (entities.isEmpty()) {
            require(subScene.connections.none { it.type == ConnectionType.STRUCTURAL_BRANCH }) {
                "Structural connections cannot exist without nodes"
            }
            return LayoutResult(emptyMap(), emptyMap(), 0f, 0f)
        }

        val byKey = entities.associateBy { it.visualKey }
        require(byKey.size == entities.size) { "Visual entity keys must be unique" }
        require(entities.map { it.slotKey }.toSet().size == entities.size) { "Visual entity slot keys must be unique" }
        val children = entities.associate { it.visualKey to mutableListOf<String>() }
        val parentByChild = mutableMapOf<String, String>()
        val structural = subScene.connections.filter { it.type == ConnectionType.STRUCTURAL_BRANCH }
        require(structural.map { it.connectionKey }.toSet().size == structural.size) {
            "Structural connection keys must be unique"
        }
        structural.forEach { edge ->
            require(edge.fromVisualKey in byKey) { "Missing structural parent endpoint: ${edge.fromVisualKey}" }
            require(edge.toVisualKey in byKey) { "Missing structural child endpoint: ${edge.toVisualKey}" }
            require(edge.fromVisualKey != edge.toVisualKey) { "A node cannot be its own parent: ${edge.fromVisualKey}" }
            require(parentByChild.put(edge.toVisualKey, edge.fromVisualKey) == null) {
                "Node ${edge.toVisualKey} has multiple parents"
            }
            children.getValue(edge.fromVisualKey).add(edge.toVisualKey)
        }
        children.values.forEach { it.sort() }

        val roots = entities.map { it.visualKey }.filter { it !in parentByChild }
        require(roots.size == 1) { "Expected one root, found ${roots.size}" }
        val root = roots.single()
        val depth = mutableMapOf<String, Int>()
        val visiting = mutableSetOf<String>()
        val visited = mutableSetOf<String>()
        fun measure(id: String, level: Int): Float {
            require(visiting.add(id)) { "Cycle detected at $id" }
            depth[id] = level
            val childIds = children.getValue(id)
            val width = if (childIds.isEmpty()) NODE_DIAMETER_DP else {
                childIds.sumOf { measure(it, level + 1).toDouble() }.toFloat() +
                    MIN_NODE_SEPARATION_DP * (childIds.size - 1)
            }
            visiting.remove(id)
            visited.add(id)
            return max(NODE_DIAMETER_DP, width)
        }
        val subtreeWidths = mutableMapOf<String, Float>()
        fun widthOf(id: String): Float {
            val ids = children.getValue(id)
            val w = if (ids.isEmpty()) NODE_DIAMETER_DP else
                max(NODE_DIAMETER_DP, ids.sumOf { widthOf(it).toDouble() }.toFloat() + MIN_NODE_SEPARATION_DP * (ids.size - 1))
            subtreeWidths[id] = w
            return w
        }
        // A separate traversal keeps validation explicit while computing levels and cycle detection.
        measure(root, 0)
        require(visited.size == entities.size) { "Topology is disconnected or contains a cycle" }
        widthOf(root)

        val positions = mutableMapOf<String, Rect2D>()
        fun place(id: String, left: Float) {
            val x = left + (subtreeWidths.getValue(id) - NODE_DIAMETER_DP) / 2f
            val y = VERTICAL_MARGIN_DP + depth.getValue(id) * LEVEL_SPACING_DP
            positions[id] = Rect2D(x, y, NODE_DIAMETER_DP, NODE_DIAMETER_DP)
            var cursor = left
            children.getValue(id).forEach { child ->
                place(child, cursor)
                cursor += subtreeWidths.getValue(child) + MIN_NODE_SEPARATION_DP
            }
        }
        place(root, HORIZONTAL_MARGIN_DP)

        val lines = structural.associate { edge ->
            edge.connectionKey to boundaryLine(positions.getValue(edge.fromVisualKey), positions.getValue(edge.toVisualKey))
        }
        val maxRight = positions.values.maxOf { it.right }
        val maxBottom = positions.values.maxOf { it.bottom }
        return LayoutResult(
            nodeBounds = entities.associate { it.slotKey to positions.getValue(it.visualKey) },
            connectionLines = lines,
            requiredWidth = maxRight + HORIZONTAL_MARGIN_DP,
            requiredHeight = maxBottom + VERTICAL_MARGIN_DP
        )
    }

    private fun boundaryLine(from: Rect2D, to: Rect2D): LineSegment2D {
        val a = from.center
        val b = to.center
        val dx = b.x - a.x
        val dy = b.y - a.y
        val distance = max(0.001f, sqrt(dx * dx + dy * dy))
        val radius = NODE_DIAMETER_DP / 2f
        return LineSegment2D(
            Point2D(a.x + dx / distance * radius, a.y + dy / distance * radius),
            Point2D(b.x - dx / distance * radius, b.y - dy / distance * radius)
        )
    }
}
