package com.example.learndsandalgorithm.presentation.learn

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

internal data class GraphVertexData(
    val key: String,
    val label: String
)

internal data class GraphEdgeData(
    val key: String,
    val fromVertexKey: String,
    val toVertexKey: String,
    val directed: Boolean = false
)

internal data class GraphLayoutResult(
    val vertexBounds: Map<String, Rect2D>,
    val edgeLines: Map<String, LineSegment2D>,
    val requiredWidth: Float,
    val requiredHeight: Float
)

internal object GraphSceneLayout {

    const val MIN_NODE_SIZE_DP = 40f
    const val MIN_CLEARANCE_DP = 24f
    const val MIN_HORIZONTAL_MARGIN_DP = 24f
    const val MIN_VERTICAL_MARGIN_DP = 24f

    fun calculateLayout(
        vertices: List<GraphVertexData>,
        edges: List<GraphEdgeData>,
        availableWidthDp: Float = 400f,
        availableHeightDp: Float = 400f
    ): GraphLayoutResult {
        val seenEdges = mutableSetOf<String>()
        edges.forEach { e ->
            require(seenEdges.add(e.key)) {
                "Duplicate edge key '${e.key}' found in graph layout inputs"
            }
        }

        if (vertices.isEmpty()) {
            return GraphLayoutResult(emptyMap(), emptyMap(), 0f, 0f)
        }

        val seenVertices = mutableSetOf<String>()
        vertices.forEach { v ->
            require(seenVertices.add(v.key)) {
                "Duplicate vertex key '${v.key}' found in graph layout inputs"
            }
        }
        val vertexKeys = vertices.map { it.key }.toSet()

        // Ignore edges whose endpoints are absent from vertex list or self-loops (fromVertexKey == toVertexKey)
        val validEdges = edges.filter {
            it.fromVertexKey in vertexKeys &&
            it.toVertexKey in vertexKeys &&
            it.fromVertexKey != it.toVertexKey
        }

        // 1. Find connected components (undirected reachability for spatial grouping)
        val adj = mutableMapOf<String, MutableSet<String>>()
        vertices.forEach { adj[it.key] = mutableSetOf() }
        validEdges.forEach { edge ->
            adj[edge.fromVertexKey]?.add(edge.toVertexKey)
            adj[edge.toVertexKey]?.add(edge.fromVertexKey)
        }

        val visited = mutableSetOf<String>()
        val components = mutableListOf<List<String>>()

        vertices.forEach { v ->
            if (v.key !in visited) {
                val component = mutableListOf<String>()
                val queue = ArrayDeque<String>()
                queue.add(v.key)
                visited.add(v.key)

                while (queue.isNotEmpty()) {
                    val curr = queue.removeFirst()
                    component.add(curr)
                    adj[curr]?.forEach { neighbor ->
                        if (neighbor !in visited) {
                            visited.add(neighbor)
                            queue.add(neighbor)
                        }
                    }
                }
                components.add(component)
            }
        }

        // 2. Position vertices per component in a stable, deterministic circular layout
        val vertexCenters = mutableMapOf<String, Point2D>()
        var currentOriginX = MIN_HORIZONTAL_MARGIN_DP

        components.forEach { comp ->
            val k = comp.size
            if (k == 1) {
                val key = comp.first()
                val centerX = currentOriginX + MIN_NODE_SIZE_DP / 2f
                val centerY = MIN_VERTICAL_MARGIN_DP + MIN_NODE_SIZE_DP / 2f
                vertexCenters[key] = Point2D(centerX, centerY)
                currentOriginX += MIN_NODE_SIZE_DP + MIN_CLEARANCE_DP
            } else {
                val minCenterDistance = MIN_NODE_SIZE_DP + MIN_CLEARANCE_DP // 64dp
                val sinHalfAngle = sin(PI.toFloat() / k)
                val requiredRadius = if (sinHalfAngle > 0f) minCenterDistance / (2f * sinHalfAngle) else 32f
                val radius = max(32f, requiredRadius)

                val componentCenterX = currentOriginX + radius + MIN_NODE_SIZE_DP / 2f
                val componentCenterY = MIN_VERTICAL_MARGIN_DP + radius + MIN_NODE_SIZE_DP / 2f

                val angleStep = (2f * PI.toFloat()) / k
                comp.forEachIndexed { idx, key ->
                    val angle = -PI.toFloat() / 2f + idx * angleStep
                    val x = componentCenterX + radius * cos(angle)
                    val y = componentCenterY + radius * sin(angle)
                    vertexCenters[key] = Point2D(x, y)
                }

                currentOriginX += 2f * radius + MIN_NODE_SIZE_DP + MIN_CLEARANCE_DP
            }
        }

        // 3. Normalize all coordinates so minX >= MIN_HORIZONTAL_MARGIN_DP, minY >= MIN_VERTICAL_MARGIN_DP
        val minX = vertexCenters.values.minOfOrNull { it.x - MIN_NODE_SIZE_DP / 2f } ?: MIN_HORIZONTAL_MARGIN_DP
        val minY = vertexCenters.values.minOfOrNull { it.y - MIN_NODE_SIZE_DP / 2f } ?: MIN_VERTICAL_MARGIN_DP

        val shiftX = if (minX < MIN_HORIZONTAL_MARGIN_DP) MIN_HORIZONTAL_MARGIN_DP - minX else 0f
        val shiftY = if (minY < MIN_VERTICAL_MARGIN_DP) MIN_VERTICAL_MARGIN_DP - minY else 0f

        val vertexBounds = mutableMapOf<String, Rect2D>()
        vertices.forEach { v ->
            val center = vertexCenters[v.key] ?: Point2D(MIN_HORIZONTAL_MARGIN_DP + MIN_NODE_SIZE_DP / 2f, MIN_VERTICAL_MARGIN_DP + MIN_NODE_SIZE_DP / 2f)
            val left = center.x + shiftX - MIN_NODE_SIZE_DP / 2f
            val top = center.y + shiftY - MIN_NODE_SIZE_DP / 2f
            vertexBounds[v.key] = Rect2D(left = left, top = top, width = MIN_NODE_SIZE_DP, height = MIN_NODE_SIZE_DP)
        }

        // 4. Calculate boundary-to-boundary edge lines safely
        val edgeLines = mutableMapOf<String, LineSegment2D>()
        validEdges.forEach { edge ->
            val fromRect = vertexBounds[edge.fromVertexKey]
            val toRect = vertexBounds[edge.toVertexKey]

            if (fromRect != null && toRect != null) {
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

                edgeLines[edge.key] = LineSegment2D(start = startPoint, end = endPoint)
            }
        }

        val maxX = vertexBounds.values.maxOfOrNull { it.right } ?: availableWidthDp
        val maxY = vertexBounds.values.maxOfOrNull { it.bottom } ?: availableHeightDp

        return GraphLayoutResult(
            vertexBounds = vertexBounds,
            edgeLines = edgeLines,
            requiredWidth = max(availableWidthDp, maxX + MIN_HORIZONTAL_MARGIN_DP),
            requiredHeight = max(availableHeightDp, maxY + MIN_VERTICAL_MARGIN_DP)
        )
    }
}
