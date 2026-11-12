package com.example.learndsandalgorithm.presentation.learn

import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TreeSceneLayoutTest {

    @Test
    fun testEmptyTreeLayout() {
        val result = TreeSceneLayout.calculateLayout(emptyList())
        assertTrue(result.nodeBounds.isEmpty())
        assertTrue(result.connectionLines.isEmpty())
        assertEquals(0f, result.requiredWidth)
        assertEquals(0f, result.requiredHeight)
    }

    @Test
    fun testSingleNodeTree() {
        val nodes = listOf(TraversalNode("N1", 10))
        val result = TreeSceneLayout.calculateLayout(nodes)

        assertEquals(1, result.nodeBounds.size)
        assertEquals(0, result.connectionLines.size)
        val bounds = result.nodeBounds["tree_slot:N1"]
        assertTrue(bounds != null)
        assertTrue(bounds.left >= 0f)
        assertTrue(bounds.top >= 0f)
    }

    @Test
    fun testTwoChildrenTree() {
        val nodes = listOf(
            TraversalNode("N1", 10, leftId = "N2", rightId = "N3"),
            TraversalNode("N2", 5),
            TraversalNode("N3", 15)
        )
        val result = TreeSceneLayout.calculateLayout(nodes)

        assertEquals(3, result.nodeBounds.size)
        assertEquals(2, result.connectionLines.size)
        assertTrue(result.connectionLines.containsKey("branch:tree_slot:N1->tree_slot:N2"))
        assertTrue(result.connectionLines.containsKey("branch:tree_slot:N1->tree_slot:N3"))

        verifyParentCentering(nodes, result)
    }

    @Test
    fun testCompleteTree() {
        val nodes = TreeTraversalsTrace.treeNodes
        val result = TreeSceneLayout.calculateLayout(nodes)

        assertEquals(7, result.nodeBounds.size)
        assertEquals(6, result.connectionLines.size)
        verifyAllPairsNoOverlap(result)
        verifySameLevelSeparation(result)
        verifyConnectionEndpoints(nodes, result)
        verifySceneBounds(result)
        verifyParentCentering(nodes, result)
    }

    @Test
    fun testIncompleteTree() {
        val nodes = listOf(
            TraversalNode("N1", 10, leftId = "N2", rightId = "N3"),
            TraversalNode("N2", 5, leftId = "N4"),
            TraversalNode("N3", 15),
            TraversalNode("N4", 2)
        )
        val result = TreeSceneLayout.calculateLayout(nodes)

        assertEquals(4, result.nodeBounds.size)
        assertEquals(3, result.connectionLines.size)
        verifyAllPairsNoOverlap(result)
        verifyParentCentering(nodes, result)
    }

    @Test
    fun testLeftSkewedTree() {
        val nodes = listOf(
            TraversalNode("N1", 10, leftId = "N2"),
            TraversalNode("N2", 5, leftId = "N3"),
            TraversalNode("N3", 2)
        )
        val result = TreeSceneLayout.calculateLayout(nodes)

        assertEquals(3, result.nodeBounds.size)
        assertEquals(2, result.connectionLines.size)
        verifyAllPairsNoOverlap(result)
    }

    @Test
    fun testRightSkewedTree() {
        val nodes = listOf(
            TraversalNode("N1", 10, rightId = "N2"),
            TraversalNode("N2", 15, rightId = "N3"),
            TraversalNode("N3", 20)
        )
        val result = TreeSceneLayout.calculateLayout(nodes)

        assertEquals(3, result.nodeBounds.size)
        assertEquals(2, result.connectionLines.size)
        verifyAllPairsNoOverlap(result)
    }

    @Test
    fun testDeepLeftSubtreeShallowRightSubtree() {
        val nodes = listOf(
            TraversalNode("Root", 10, leftId = "L1", rightId = "R1"),
            TraversalNode("L1", 5, leftId = "L2"),
            TraversalNode("L2", 3, leftId = "L3"),
            TraversalNode("L3", 1),
            TraversalNode("R1", 20)
        )
        val result = TreeSceneLayout.calculateLayout(nodes)
        assertEquals(5, result.nodeBounds.size)
        verifyAllPairsNoOverlap(result)
        verifyParentCentering(nodes, result)
    }

    @Test
    fun testShallowLeftSubtreeDeepRightSubtree() {
        val nodes = listOf(
            TraversalNode("Root", 10, leftId = "L1", rightId = "R1"),
            TraversalNode("L1", 5),
            TraversalNode("R1", 20, rightId = "R2"),
            TraversalNode("R2", 25, rightId = "R3"),
            TraversalNode("R3", 30)
        )
        val result = TreeSceneLayout.calculateLayout(nodes)
        assertEquals(5, result.nodeBounds.size)
        verifyAllPairsNoOverlap(result)
        verifyParentCentering(nodes, result)
    }

    @Test
    fun testAsymmetricBranchingBothSides() {
        val nodes = listOf(
            TraversalNode("A", 1, leftId = "B", rightId = "C"),
            TraversalNode("B", 2, leftId = "D", rightId = "E"),
            TraversalNode("C", 3, leftId = "F"),
            TraversalNode("D", 4, leftId = "G"),
            TraversalNode("E", 5),
            TraversalNode("F", 6, rightId = "H"),
            TraversalNode("G", 7),
            TraversalNode("H", 8)
        )
        val result = TreeSceneLayout.calculateLayout(nodes)
        assertEquals(8, result.nodeBounds.size)
        verifyAllPairsNoOverlap(result)
    }

    @Test
    fun testNeighboringSubtreesDifferentWidths() {
        val nodes = listOf(
            TraversalNode("Root", 10, leftId = "L1", rightId = "R1"),
            TraversalNode("L1", 5, leftId = "L2", rightId = "L3"),
            TraversalNode("L2", 2),
            TraversalNode("L3", 4),
            TraversalNode("R1", 20)
        )
        val result = TreeSceneLayout.calculateLayout(nodes)
        assertEquals(5, result.nodeBounds.size)
        verifyAllPairsNoOverlap(result)
        verifyParentCentering(nodes, result)
    }

    @Test
    fun testMissingChildrenMultipleLevels() {
        val nodes = listOf(
            TraversalNode("N1", 1, rightId = "N2"),
            TraversalNode("N2", 2, leftId = "N3"),
            TraversalNode("N3", 3, rightId = "N4"),
            TraversalNode("N4", 4)
        )
        val result = TreeSceneLayout.calculateLayout(nodes)
        assertEquals(4, result.nodeBounds.size)
        verifyAllPairsNoOverlap(result)
    }

    @Test
    fun testLargeIrregularTree20Nodes() {
        val nodes = mutableListOf<TraversalNode>()
        for (i in 1..20) {
            val left = if (2 * i <= 20) "N${2 * i}" else null
            val right = if (2 * i + 1 <= 20) "N${2 * i + 1}" else null
            nodes.add(TraversalNode("N$i", i * 10, leftId = left, rightId = right))
        }
        val result = TreeSceneLayout.calculateLayout(nodes)
        assertEquals(20, result.nodeBounds.size)
        verifyAllPairsNoOverlap(result)
        verifySameLevelSeparation(result)
        verifySceneBounds(result)
    }

    @Test
    fun testDeterministicOutput() {
        val nodes = TreeTraversalsTrace.treeNodes
        val resA = TreeSceneLayout.calculateLayout(nodes)
        val resB = TreeSceneLayout.calculateLayout(nodes)

        assertEquals(resA.nodeBounds, resB.nodeBounds)
        assertEquals(resA.connectionLines, resB.connectionLines)
    }

    @Test
    fun testIdentityIndependentOfValues() {
        val nodesA = listOf(
            TraversalNode("N1", 10, leftId = "N2"),
            TraversalNode("N2", 20)
        )
        val nodesB = listOf(
            TraversalNode("N1", 999, leftId = "N2"),
            TraversalNode("N2", 888)
        )

        val resA = TreeSceneLayout.calculateLayout(nodesA)
        val resB = TreeSceneLayout.calculateLayout(nodesB)

        assertEquals(resA.nodeBounds.keys, resB.nodeBounds.keys)
    }

    // ==========================================
    // ADVERSARIAL TESTS (Batch 2.1.4 / 2.1.6)
    // ==========================================

    @Test
    fun testAdversarialDeepLeafBesideWideSubtree() {
        val nodes = mutableListOf(
            TraversalNode("Root", 100, leftId = "D1", rightId = "W1")
        )
        var curr = "D1"
        for (d in 1..10) {
            val next = if (d < 10) "D${d + 1}" else null
            nodes.add(TraversalNode(curr, d * 10, leftId = next))
            if (next != null) curr = next
        }
        nodes.addAll(
            listOf(
                TraversalNode("W1", 200, leftId = "W2", rightId = "W3"),
                TraversalNode("W2", 150, leftId = "W4", rightId = "W5"),
                TraversalNode("W3", 250, leftId = "W6", rightId = "W7"),
                TraversalNode("W4", 120),
                TraversalNode("W5", 170),
                TraversalNode("W6", 220),
                TraversalNode("W7", 270)
            )
        )
        val result = TreeSceneLayout.calculateLayout(nodes)
        verifyAllPairsNoOverlap(result)
        verifySameLevelSeparation(result)
    }

    @Test
    fun testAdversarialPreviousGlobalSweepFailureTopology() {
        val nodes = listOf(
            TraversalNode("A", 10, leftId = "B", rightId = "C"),
            TraversalNode("B", 5, leftId = "D"),
            TraversalNode("C", 15, rightId = "E"),
            TraversalNode("D", 2, leftId = "F", rightId = "G"),
            TraversalNode("E", 20, leftId = "H", rightId = "I"),
            TraversalNode("F", 1),
            TraversalNode("G", 3),
            TraversalNode("H", 18),
            TraversalNode("I", 22)
        )
        val result = TreeSceneLayout.calculateLayout(nodes)
        verifyAllPairsNoOverlap(result)
        verifyParentCentering(nodes, result)
        verifySameLevelSeparation(result)
    }

    // ==========================================
    // REUSABLE TEST ASSERTION HELPERS
    // ==========================================

    private fun verifyAllPairsNoOverlap(result: TreeSceneLayout.TreeLayoutResult) {
        val rects = result.nodeBounds.values.toList()
        for (i in rects.indices) {
            for (j in i + 1 until rects.size) {
                val r1 = rects[i]
                val r2 = rects[j]
                val overlaps = r1.left < r2.right && r1.right > r2.left && r1.top < r2.bottom && r1.bottom > r2.top
                assertTrue(!overlaps, "Node rects at ($r1) and ($r2) must not intersect across any depth")
            }
        }
    }

    private fun verifySameLevelSeparation(result: TreeSceneLayout.TreeLayoutResult) {
        val rectsByLevel = result.nodeBounds.values.groupBy { it.top }
        rectsByLevel.forEach { (_, rects) ->
            val sorted = rects.sortedBy { it.left }
            for (i in 0 until sorted.size - 1) {
                val r1 = sorted[i]
                val r2 = sorted[i + 1]
                val horizontalGap = r2.left - r1.right
                assertTrue(
                    horizontalGap >= TreeSceneLayout.MIN_SIBLING_GAP_DP - 0.05f,
                    "Same level nodes must maintain at least MIN_SIBLING_GAP_DP separation (actual gap: $horizontalGap)"
                )
            }
        }
    }

    private fun verifyParentCentering(nodes: List<TraversalNode>, result: TreeSceneLayout.TreeLayoutResult) {
        nodes.forEach { node ->
            val leftId = node.leftId
            val rightId = node.rightId
            if (leftId != null && rightId != null) {
                val parentRect = result.nodeBounds["tree_slot:${node.id}"]
                val leftRect = result.nodeBounds["tree_slot:$leftId"]
                val rightRect = result.nodeBounds["tree_slot:$rightId"]
                if (parentRect != null && leftRect != null && rightRect != null) {
                    val expectedParentX = (leftRect.center.x + rightRect.center.x) / 2f
                    assertEquals(expectedParentX, parentRect.center.x, 0.01f, "Parent ${node.id} must be centered between children")
                }
            }
        }
    }

    private fun verifyConnectionEndpoints(nodes: List<TraversalNode>, result: TreeSceneLayout.TreeLayoutResult) {
        nodes.forEach { node ->
            val parentRect = result.nodeBounds["tree_slot:${node.id}"] ?: return@forEach
            val radius = parentRect.width / 2f

            listOfNotNull(node.leftId, node.rightId).forEach { childId ->
                val childRect = result.nodeBounds["tree_slot:$childId"] ?: return@forEach
                val line = result.connectionLines["branch:tree_slot:${node.id}->tree_slot:$childId"]
                if (line != null) {
                    val startDist = sqrt((line.start.x - parentRect.center.x) * (line.start.x - parentRect.center.x) + (line.start.y - parentRect.center.y) * (line.start.y - parentRect.center.y))
                    val endDist = sqrt((line.end.x - childRect.center.x) * (line.end.x - childRect.center.x) + (line.end.y - childRect.center.y) * (line.end.y - childRect.center.y))
                    assertEquals(radius, startDist, 0.05f, "Connection start must touch parent circle boundary")
                    assertEquals(radius, endDist, 0.05f, "Connection end must touch child circle boundary")
                }
            }
        }
    }

    private fun verifySceneBounds(result: TreeSceneLayout.TreeLayoutResult) {
        val maxNodeRight = result.nodeBounds.values.maxOfOrNull { it.right } ?: 0f
        val maxNodeBottom = result.nodeBounds.values.maxOfOrNull { it.bottom } ?: 0f

        assertTrue(result.requiredWidth >= maxNodeRight + TreeSceneLayout.MIN_HORIZONTAL_MARGIN_DP)
        assertTrue(result.requiredHeight >= maxNodeBottom + TreeSceneLayout.MIN_VERTICAL_MARGIN_DP)
    }
}
