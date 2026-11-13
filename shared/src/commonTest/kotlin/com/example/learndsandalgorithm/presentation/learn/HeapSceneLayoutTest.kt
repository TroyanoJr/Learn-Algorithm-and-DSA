package com.example.learndsandalgorithm.presentation.learn

import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HeapSceneLayoutTest {

    @Test
    fun testEmptyHeapLayoutReturnsEmptyMaps() {
        val layout = HeapSceneLayout.calculateLayout(
            totalElements = 0,
            availableWidthDp = 360f,
            availableHeightDp = 400f
        )

        assertTrue(layout.treeNodeBounds.isEmpty())
        assertTrue(layout.treeConnectionLines.isEmpty())
        assertTrue(layout.arrayCellBounds.isEmpty())
        assertTrue(layout.arrayIndexLabelBounds.isEmpty())
        assertEquals(0f, layout.requiredSceneWidth)
    }

    @Test
    fun testSingleNodeHeapLayout() {
        val layout = HeapSceneLayout.calculateLayout(
            totalElements = 1,
            availableWidthDp = 360f,
            availableHeightDp = 400f
        )

        assertEquals(1, layout.treeNodeBounds.size)
        assertEquals(0, layout.treeConnectionLines.size)
        assertEquals(1, layout.arrayCellBounds.size)
        assertEquals(1, layout.arrayIndexLabelBounds.size)

        val rootNode = layout.treeNodeBounds["tree_slot:0"]
        assertTrue(rootNode != null)
        assertTrue(rootNode.left >= 0f)
        assertTrue(rootNode.top >= 0f)
    }

    @Test
    fun testTwoNodesHeapLayoutHasOneConnection() {
        val layout = HeapSceneLayout.calculateLayout(
            totalElements = 2,
            availableWidthDp = 360f,
            availableHeightDp = 400f
        )

        assertEquals(2, layout.treeNodeBounds.size)
        assertEquals(1, layout.treeConnectionLines.size)
        assertEquals(2, layout.arrayCellBounds.size)
        assertEquals(2, layout.arrayIndexLabelBounds.size)

        val connectionKey = "branch:tree_slot:0->tree_slot:1"
        assertTrue(layout.treeConnectionLines.containsKey(connectionKey))
    }

    @Test
    fun testCompleteSixElementsHeapLayout() {
        val layout = HeapSceneLayout.calculateLayout(
            totalElements = 6,
            availableWidthDp = 360f,
            availableHeightDp = 400f
        )

        assertEquals(6, layout.treeNodeBounds.size)
        assertEquals(5, layout.treeConnectionLines.size)
        assertEquals(6, layout.arrayCellBounds.size)
        assertEquals(6, layout.arrayIndexLabelBounds.size)

        val expectedBranchKeys = setOf(
            "branch:tree_slot:0->tree_slot:1",
            "branch:tree_slot:0->tree_slot:2",
            "branch:tree_slot:1->tree_slot:3",
            "branch:tree_slot:1->tree_slot:4",
            "branch:tree_slot:2->tree_slot:5"
        )
        assertEquals(expectedBranchKeys, layout.treeConnectionLines.keys)
    }

    @Test
    fun testDeeperHeapLayout() {
        val layout = HeapSceneLayout.calculateLayout(
            totalElements = 15,
            availableWidthDp = 600f,
            availableHeightDp = 600f
        )

        assertEquals(15, layout.treeNodeBounds.size)
        assertEquals(14, layout.treeConnectionLines.size)
        assertEquals(15, layout.arrayCellBounds.size)
        assertEquals(15, layout.arrayIndexLabelBounds.size)
    }

    @Test
    fun testAbsenceOfNegativeCoordinates() {
        val layout = HeapSceneLayout.calculateLayout(
            totalElements = 6,
            availableWidthDp = 360f,
            availableHeightDp = 400f
        )

        layout.treeNodeBounds.values.forEach { rect ->
            assertTrue(rect.left >= 0f, "Tree node left should be >= 0")
            assertTrue(rect.top >= 0f, "Tree node top should be >= 0")
        }

        layout.arrayCellBounds.values.forEach { rect ->
            assertTrue(rect.left >= 0f, "Array cell left should be >= 0")
            assertTrue(rect.top >= 0f, "Array cell top should be >= 0")
        }

        layout.arrayIndexLabelBounds.values.forEach { rect ->
            assertTrue(rect.left >= 0f, "Index label left should be >= 0")
            assertTrue(rect.top >= 0f, "Index label top should be >= 0")
        }
    }

    @Test
    fun testNodeSeparationInSameLevel() {
        val layout = HeapSceneLayout.calculateLayout(
            totalElements = 6,
            availableWidthDp = 360f,
            availableHeightDp = 400f
        )

        val node1 = layout.treeNodeBounds["tree_slot:1"]!!
        val node2 = layout.treeNodeBounds["tree_slot:2"]!!

        assertTrue(node2.left >= node1.right, "Node 2 should be to the right of Node 1")
    }

    @Test
    fun testConnectionsTouchNodeBoundaries() {
        val layout = HeapSceneLayout.calculateLayout(
            totalElements = 6,
            availableWidthDp = 360f,
            availableHeightDp = 400f
        )

        val parentRect = layout.treeNodeBounds["tree_slot:0"]!!
        val leftChildRect = layout.treeNodeBounds["tree_slot:1"]!!
        val line = layout.treeConnectionLines["branch:tree_slot:0->tree_slot:1"]!!

        val radius = parentRect.width / 2f
        val parentCenter = parentRect.center
        val childCenter = leftChildRect.center

        val parentStartDist = sqrt(
            (line.start.x - parentCenter.x) * (line.start.x - parentCenter.x) +
                (line.start.y - parentCenter.y) * (line.start.y - parentCenter.y)
        )
        assertEquals(radius, parentStartDist, 0.05f, "Line start must lie on parent node circle boundary")

        val childEndDist = sqrt(
            (line.end.x - childCenter.x) * (line.end.x - childCenter.x) +
                (line.end.y - childCenter.y) * (line.end.y - childCenter.y)
        )
        assertEquals(radius, childEndDist, 0.05f, "Line end must lie on child node circle boundary")
    }

    @Test
    fun testArrayIndexLabelBoundsCalculation() {
        val layout = HeapSceneLayout.calculateLayout(
            totalElements = 6,
            availableWidthDp = 360f,
            availableHeightDp = 400f
        )

        for (i in 0 until 6) {
            val slotKey = "array_slot:$i"
            val cellRect = layout.arrayCellBounds[slotKey]!!
            val labelRect = layout.arrayIndexLabelBounds[slotKey]!!

            assertTrue(labelRect.top >= cellRect.bottom, "Index label $i top must be >= cell bottom")
            assertEquals(cellRect.left, labelRect.left, 0.01f, "Index label $i left must match cell left")
        }
    }

    @Test
    fun testRequiredSceneHeightIncludesBottomMarginAndLabels() {
        val layout = HeapSceneLayout.calculateLayout(
            totalElements = 6,
            availableWidthDp = 360f,
            availableHeightDp = 400f
        )

        val maxLabelBottom = layout.arrayIndexLabelBounds.values.maxOf { it.bottom }
        assertTrue(
            layout.requiredSceneHeight >= maxLabelBottom + 14f,
            "requiredSceneHeight (${layout.requiredSceneHeight}) must accommodate max index label bottom ($maxLabelBottom) plus bottom clearance"
        )
    }

    @Test
    fun testArrayCellCoordinatesOrderedBySlot() {
        val layout = HeapSceneLayout.calculateLayout(
            totalElements = 6,
            availableWidthDp = 360f,
            availableHeightDp = 400f
        )

        for (i in 0 until 5) {
            val currentCell = layout.arrayCellBounds["array_slot:$i"]!!
            val nextCell = layout.arrayCellBounds["array_slot:${i + 1}"]!!
            assertTrue(nextCell.left >= currentCell.left + currentCell.width - 0.01f, "Cell ${i + 1} should be to the right of Cell $i")
        }
    }

    @Test
    fun testSlotGeometryInvarianceToOccupants() {
        val layoutA = HeapSceneLayout.calculateLayout(6, 360f, 400f)
        val layoutB = HeapSceneLayout.calculateLayout(6, 360f, 400f)

        assertEquals(layoutA.treeNodeBounds, layoutB.treeNodeBounds)
        assertEquals(layoutA.arrayCellBounds, layoutB.arrayCellBounds)
        assertEquals(layoutA.arrayIndexLabelBounds, layoutB.arrayIndexLabelBounds)
    }

    @Test
    fun testAbsenceOfNodeOverlapsInTree() {
        val layout = HeapSceneLayout.calculateLayout(
            totalElements = 7,
            availableWidthDp = 400f,
            availableHeightDp = 400f
        )

        val nodes = layout.treeNodeBounds.values.toList()
        for (i in nodes.indices) {
            for (j in i + 1 until nodes.size) {
                val r1 = nodes[i]
                val r2 = nodes[j]
                val overlaps = r1.left < r2.right && r1.right > r2.left && r1.top < r2.bottom && r1.bottom > r2.top
                assertTrue(!overlaps, "Nodes $i and $j should not overlap")
            }
        }
    }

    @Test
    fun testTreeAndArrayRegionsDoNotOverlap() {
        val layout = HeapSceneLayout.calculateLayout(
            totalElements = 6,
            availableWidthDp = 360f,
            availableHeightDp = 400f
        )

        assertTrue(
            layout.treeRegionBounds.bottom <= layout.arrayRegionBounds.top,
            "Tree region bottom (${layout.treeRegionBounds.bottom}) must be <= Array region top (${layout.arrayRegionBounds.top})"
        )
    }

    @Test
    fun testLandscapeLayoutConstraints() {
        val landscapeLayout = HeapSceneLayout.calculateLayout(
            totalElements = 6,
            availableWidthDp = 600f,
            availableHeightDp = 240f
        )

        assertTrue(landscapeLayout.requiredSceneWidth >= 600f)
        assertTrue(landscapeLayout.requiredSceneHeight >= 200f)
        assertEquals(6, landscapeLayout.treeNodeBounds.size)
        assertEquals(6, landscapeLayout.arrayCellBounds.size)
        assertEquals(6, landscapeLayout.arrayIndexLabelBounds.size)
    }

    @Test
    fun testCompactVsWideLayoutDimensions() {
        val compactLayout = HeapSceneLayout.calculateLayout(6, 300f, 400f)
        val wideLayout = HeapSceneLayout.calculateLayout(6, 800f, 400f)

        assertTrue(wideLayout.requiredSceneWidth > compactLayout.requiredSceneWidth)
    }

    // Comprehensive tests for independent tree vs array overload
    @Test
    fun testIndependentTreeAndArrayElementCountLayout() {
        val layout = HeapSceneLayout.calculateLayout(
            treeElementCount = 3,
            arrayElementCount = 6,
            availableWidthDp = 360f,
            availableHeightDp = 400f
        )

        assertEquals(3, layout.treeNodeBounds.size)
        assertEquals(2, layout.treeConnectionLines.size)
        assertEquals(6, layout.arrayCellBounds.size)
        assertEquals(6, layout.arrayIndexLabelBounds.size)
        assertTrue(layout.treeRegionBounds.bottom <= layout.arrayRegionBounds.top)

        // Check no negative coordinates
        layout.treeNodeBounds.values.forEach { assertTrue(it.left >= 0f && it.top >= 0f) }
        layout.arrayCellBounds.values.forEach { assertTrue(it.left >= 0f && it.top >= 0f) }

        // Check no overlaps in tree
        val nodes = layout.treeNodeBounds.values.toList()
        for (i in nodes.indices) {
            for (j in i + 1 until nodes.size) {
                val r1 = nodes[i]
                val r2 = nodes[j]
                val overlaps = r1.left < r2.right && r1.right > r2.left && r1.top < r2.bottom && r1.bottom > r2.top
                assertTrue(!overlaps)
            }
        }
    }

    @Test
    fun testEmptyTreeWithNonEmptyArrayLayout() {
        val layout = HeapSceneLayout.calculateLayout(
            treeElementCount = 0,
            arrayElementCount = 6,
            availableWidthDp = 360f,
            availableHeightDp = 400f
        )

        assertTrue(layout.treeNodeBounds.isEmpty())
        assertTrue(layout.treeConnectionLines.isEmpty())
        assertEquals(6, layout.arrayCellBounds.size)
        assertEquals(6, layout.arrayIndexLabelBounds.size)
        layout.arrayCellBounds.values.forEach { assertTrue(it.left >= 0f && it.top >= 0f) }
    }

    @Test
    fun testNonEmptyTreeWithEmptyArrayLayout() {
        val layout = HeapSceneLayout.calculateLayout(
            treeElementCount = 6,
            arrayElementCount = 0,
            availableWidthDp = 360f,
            availableHeightDp = 400f
        )

        assertEquals(6, layout.treeNodeBounds.size)
        assertEquals(5, layout.treeConnectionLines.size)
        assertTrue(layout.arrayCellBounds.isEmpty())
        assertTrue(layout.arrayIndexLabelBounds.isEmpty())
        layout.treeNodeBounds.values.forEach { assertTrue(it.left >= 0f && it.top >= 0f) }
    }
}
