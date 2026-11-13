package com.example.learndsandalgorithm.presentation.learn

import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GraphSceneLayoutTest {

    @Test
    fun testEmptyGraphLayout() {
        val result = GraphSceneLayout.calculateLayout(emptyList(), emptyList())
        assertTrue(result.vertexBounds.isEmpty())
        assertTrue(result.edgeLines.isEmpty())
        assertEquals(0f, result.requiredWidth)
        assertEquals(0f, result.requiredHeight)
    }

    @Test
    fun testOneIsolatedVertex() {
        val vertices = listOf(GraphVertexData("A", "A"))
        val result = GraphSceneLayout.calculateLayout(vertices, emptyList())

        assertEquals(1, result.vertexBounds.size)
        assertTrue(result.edgeLines.isEmpty())

        val boundsA = result.vertexBounds["A"]
        assertTrue(boundsA != null)
        assertTrue(boundsA.left >= 0f)
        assertTrue(boundsA.top >= 0f)
        assertFalse(boundsA.left.isNaN())
        assertFalse(boundsA.top.isNaN())
        verifySceneBounds(result)
    }

    @Test
    fun testTwoConnectedVertices() {
        val vertices = listOf(
            GraphVertexData("A", "A"),
            GraphVertexData("B", "B")
        )
        val edges = listOf(
            GraphEdgeData("e1", "A", "B")
        )
        val result = GraphSceneLayout.calculateLayout(vertices, edges)

        assertEquals(2, result.vertexBounds.size)
        assertEquals(1, result.edgeLines.size)
        assertTrue(result.edgeLines.containsKey("e1"))

        verifyEdgeEndpointsTouchCircleBoundaries(edges, result)
        verifyNoNodeOverlap(result)
        verifyMinimumClearance(result)
        verifySceneBounds(result)
    }

    @Test
    fun testTriangleWithClosingCycleEdge() {
        val vertices = listOf(
            GraphVertexData("A", "A"),
            GraphVertexData("B", "B"),
            GraphVertexData("C", "C")
        )
        val edges = listOf(
            GraphEdgeData("e1", "A", "B"),
            GraphEdgeData("e2", "B", "C"),
            GraphEdgeData("e3", "C", "A") // Closing cycle edge
        )
        val result = GraphSceneLayout.calculateLayout(vertices, edges)

        assertEquals(3, result.vertexBounds.size)
        assertEquals(3, result.edgeLines.size)
        verifyNoNodeOverlap(result)
        verifyMinimumClearance(result)
        verifyEdgeEndpointsTouchCircleBoundaries(edges, result)
        verifySceneBounds(result)
    }

    @Test
    fun testLargerCyclicGraph() {
        val vertices = (1..6).map { GraphVertexData("V$it", "V$it") }
        val edges = listOf(
            GraphEdgeData("e12", "V1", "V2"),
            GraphEdgeData("e23", "V2", "V3"),
            GraphEdgeData("e34", "V3", "V4"),
            GraphEdgeData("e45", "V4", "V5"),
            GraphEdgeData("e56", "V5", "V6"),
            GraphEdgeData("e61", "V6", "V1")
        )
        val result = GraphSceneLayout.calculateLayout(vertices, edges)

        assertEquals(6, result.vertexBounds.size)
        assertEquals(6, result.edgeLines.size)
        verifyNoNodeOverlap(result)
        verifyMinimumClearance(result)
        verifySceneBounds(result)
    }

    @Test
    fun testConnectedChainSizing6_7_8_10() {
        listOf(2, 3, 6, 7, 8, 10).forEach { k ->
            val vertices = (1..k).map { GraphVertexData("V$it", "V$it") }
            val edges = (1 until k).map { GraphEdgeData("e$it", "V$it", "V${it + 1}") }
            val result = GraphSceneLayout.calculateLayout(vertices, edges)

            assertEquals(k, result.vertexBounds.size)
            verifyNoNodeOverlap(result)
            verifyMinimumClearance(result)
            verifySceneBounds(result)
        }
    }

    @Test
    fun testMultipleDisconnectedComponentsWithIsolatedVertex() {
        val vertices = listOf(
            GraphVertexData("A1", "A1"), GraphVertexData("A2", "A2"), // Comp 1
            GraphVertexData("B1", "B1"), GraphVertexData("B2", "B2"), GraphVertexData("B3", "B3"), // Comp 2
            GraphVertexData("C_iso", "C_iso") // Isolated
        )
        val edges = listOf(
            GraphEdgeData("eA", "A1", "A2"),
            GraphEdgeData("eB1", "B1", "B2"),
            GraphEdgeData("eB2", "B2", "B3")
        )
        val result = GraphSceneLayout.calculateLayout(vertices, edges)

        assertEquals(6, result.vertexBounds.size)
        assertEquals(3, result.edgeLines.size)
        verifyNoNodeOverlap(result)
        verifyMinimumClearance(result)
        verifyNonNegativeCoordinates(result)
        verifySceneBounds(result)
    }

    @Test
    fun testDirectedAndUndirectedEdges() {
        val vertices = listOf(
            GraphVertexData("A", "A"),
            GraphVertexData("B", "B")
        )
        val edges = listOf(
            GraphEdgeData("e_dir", "A", "B", directed = true),
            GraphEdgeData("e_undir", "B", "A", directed = false)
        )
        val result = GraphSceneLayout.calculateLayout(vertices, edges)

        assertEquals(2, result.vertexBounds.size)
        assertEquals(2, result.edgeLines.size)
        verifyNoNodeOverlap(result)
        verifyMinimumClearance(result)
    }

    @Test
    fun testMissingEdgeEndpointsIgnoredSafely() {
        val vertices = listOf(
            GraphVertexData("A", "A"),
            GraphVertexData("B", "B")
        )
        val edges = listOf(
            GraphEdgeData("e_valid", "A", "B"),
            GraphEdgeData("e_missing_from", "NON_EXISTENT", "B"),
            GraphEdgeData("e_missing_to", "A", "NON_EXISTENT")
        )
        val result = GraphSceneLayout.calculateLayout(vertices, edges)

        assertEquals(2, result.vertexBounds.size)
        assertEquals(1, result.edgeLines.size) // Only e_valid
        assertTrue(result.edgeLines.containsKey("e_valid"))
        assertFalse(result.edgeLines.containsKey("e_missing_from"))
        assertFalse(result.edgeLines.containsKey("e_missing_to"))
    }

    @Test
    fun testSelfLoopsIgnoredSafely() {
        val vertices = listOf(
            GraphVertexData("A", "A"),
            GraphVertexData("B", "B")
        )
        val edges = listOf(
            GraphEdgeData("e_valid", "A", "B"),
            GraphEdgeData("e_self", "A", "A") // Self-loop
        )
        val result = GraphSceneLayout.calculateLayout(vertices, edges)

        assertEquals(2, result.vertexBounds.size)
        assertEquals(1, result.edgeLines.size)
        assertTrue(result.edgeLines.containsKey("e_valid"))
        assertFalse(result.edgeLines.containsKey("e_self"))
    }

    @Test
    fun testDuplicateVertexKeysThrowException() {
        val vertices = listOf(
            GraphVertexData("A", "Vertex A"),
            GraphVertexData("A", "Duplicate A")
        )
        assertFailsWith<IllegalArgumentException> {
            GraphSceneLayout.calculateLayout(vertices, emptyList())
        }
    }

    @Test
    fun testDuplicateEdgeKeysThrowException() {
        val vertices = listOf(
            GraphVertexData("A", "A"),
            GraphVertexData("B", "B")
        )
        val edges = listOf(
            GraphEdgeData("e1", "A", "B"),
            GraphEdgeData("e1", "B", "A") // Duplicate key
        )
        assertFailsWith<IllegalArgumentException> {
            GraphSceneLayout.calculateLayout(vertices, edges)
        }
    }

    @Test
    fun testEmptyGraphWithDuplicateEdgeKeysThrowsException() {
        val edges = listOf(
            GraphEdgeData("e1", "A", "B"),
            GraphEdgeData("e1", "C", "D") // Duplicate edge key with empty vertices
        )
        assertFailsWith<IllegalArgumentException> {
            GraphSceneLayout.calculateLayout(emptyList(), edges)
        }
    }

    @Test
    fun testDeterministicOutput() {
        val vertices = listOf(
            GraphVertexData("A", "A"),
            GraphVertexData("B", "B"),
            GraphVertexData("C", "C")
        )
        val edges = listOf(
            GraphEdgeData("e1", "A", "B"),
            GraphEdgeData("e2", "B", "C")
        )

        val resA = GraphSceneLayout.calculateLayout(vertices, edges)
        val resB = GraphSceneLayout.calculateLayout(vertices, edges)

        assertEquals(resA.vertexBounds, resB.vertexBounds)
        assertEquals(resA.edgeLines, resB.edgeLines)
        assertEquals(resA.requiredWidth, resB.requiredWidth)
        assertEquals(resA.requiredHeight, resB.requiredHeight)
    }

    @Test
    fun testNoNaNOrInfiniteCoordinates() {
        val vertices = listOf(
            GraphVertexData("A", "A"),
            GraphVertexData("B", "B")
        )
        val edges = listOf(
            GraphEdgeData("e1", "A", "B")
        )
        val result = GraphSceneLayout.calculateLayout(vertices, edges)

        result.vertexBounds.values.forEach { r ->
            assertFalse(r.left.isNaN() || r.left.isInfinite())
            assertFalse(r.top.isNaN() || r.top.isInfinite())
            assertFalse(r.width.isNaN() || r.width.isInfinite())
            assertFalse(r.height.isNaN() || r.height.isInfinite())
        }

        result.edgeLines.values.forEach { line ->
            assertFalse(line.start.x.isNaN() || line.start.x.isInfinite())
            assertFalse(line.start.y.isNaN() || line.start.y.isInfinite())
            assertFalse(line.end.x.isNaN() || line.end.x.isInfinite())
            assertFalse(line.end.y.isNaN() || line.end.y.isInfinite())
        }
    }

    // ==========================================
    // REUSABLE GEOMETRY TEST HELPERS
    // ==========================================

    private fun verifyNoNodeOverlap(result: GraphLayoutResult) {
        val rects = result.vertexBounds.values.toList()
        for (i in rects.indices) {
            for (j in i + 1 until rects.size) {
                val r1 = rects[i]
                val r2 = rects[j]
                val overlaps = r1.left < r2.right && r1.right > r2.left && r1.top < r2.bottom && r1.bottom > r2.top
                assertFalse(overlaps, "Vertices at $r1 and $r2 should not overlap")
            }
        }
    }

    private fun verifyMinimumClearance(result: GraphLayoutResult, minClearanceDp: Float = 24f) {
        val rects = result.vertexBounds.values.toList()
        val minCenterDist = GraphSceneLayout.MIN_NODE_SIZE_DP + minClearanceDp
        for (i in rects.indices) {
            for (j in i + 1 until rects.size) {
                val r1 = rects[i]
                val r2 = rects[j]

                val dX = r1.center.x - r2.center.x
                val dY = r1.center.y - r2.center.y
                val centerDist = sqrt(dX * dX + dY * dY)

                // 1. Verify Euclidean center distance (node circle boundary-to-boundary clearance)
                assertTrue(
                    centerDist >= minCenterDist - 0.05f,
                    "Euclidean center distance ($centerDist) between $r1 and $r2 must be >= $minCenterDist"
                )

                // 2. Verify 2D axis-aligned bounding box non-overlap
                val horizSep = maxOf(r1.left - r2.right, r2.left - r1.right)
                val vertSep = maxOf(r1.top - r2.bottom, r2.top - r1.bottom)
                assertTrue(
                    horizSep >= 0f || vertSep >= 0f,
                    "Rectangles $r1 and $r2 must be separated (no 2D bounding box intersection)"
                )
            }
        }
    }

    private fun verifyNonNegativeCoordinates(result: GraphLayoutResult) {
        result.vertexBounds.values.forEach { r ->
            assertTrue(r.left >= 0f, "Vertex left (${r.left}) must be >= 0")
            assertTrue(r.top >= 0f, "Vertex top (${r.top}) must be >= 0")
        }
    }

    private fun verifyEdgeEndpointsTouchCircleBoundaries(
        edges: List<GraphEdgeData>,
        result: GraphLayoutResult
    ) {
        edges.forEach { edge ->
            val fromRect = result.vertexBounds[edge.fromVertexKey] ?: return@forEach
            val toRect = result.vertexBounds[edge.toVertexKey] ?: return@forEach
            val line = result.edgeLines[edge.key] ?: return@forEach

            val radius = fromRect.width / 2f
            val fromCenter = fromRect.center
            val toCenter = toRect.center

            val startDist = sqrt((line.start.x - fromCenter.x) * (line.start.x - fromCenter.x) + (line.start.y - fromCenter.y) * (line.start.y - fromCenter.y))
            val endDist = sqrt((line.end.x - toCenter.x) * (line.end.x - toCenter.x) + (line.end.y - toCenter.y) * (line.end.y - toCenter.y))

            assertEquals(radius, startDist, 0.05f, "Edge start must touch fromVertex circle boundary")
            assertEquals(radius, endDist, 0.05f, "Edge end must touch toVertex circle boundary")
        }
    }

    private fun verifySceneBounds(result: GraphLayoutResult) {
        val maxRight = result.vertexBounds.values.maxOfOrNull { it.right } ?: 0f
        val maxBottom = result.vertexBounds.values.maxOfOrNull { it.bottom } ?: 0f

        assertTrue(result.requiredWidth >= maxRight + GraphSceneLayout.MIN_HORIZONTAL_MARGIN_DP)
        assertTrue(result.requiredHeight >= maxBottom + GraphSceneLayout.MIN_VERTICAL_MARGIN_DP)
    }
}
