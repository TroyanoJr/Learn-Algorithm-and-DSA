package com.example.learndsandalgorithm.presentation.learn

import kotlin.math.log2
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Pure 2D point coordinate in DP space.
 */
internal data class Point2D(val x: Float, val y: Float)

/**
 * Pure 2D line segment connecting two points.
 */
internal data class LineSegment2D(val start: Point2D, val end: Point2D)

/**
 * Pure 2D rectangle bounds for nodes or array cells.
 */
internal data class Rect2D(val left: Float, val top: Float, val width: Float, val height: Float) {
    val right: Float get() = left + width
    val bottom: Float get() = top + height
    val center: Point2D get() = Point2D(left + width / 2f, top + height / 2f)
}

/**
 * Pure layout result containing calculated geometry for a Heap scene in one unified coordinate space.
 */
internal data class HeapSceneLayoutResult(
    val treeRegionBounds: Rect2D,
    val arrayRegionBounds: Rect2D,
    val treeNodeBounds: Map<String, Rect2D>,               // slotKey -> Rect2D (tree_slot:0 -> Rect)
    val treeConnectionLines: Map<String, LineSegment2D>,       // connectionKey -> LineSegment (boundary to boundary)
    val arrayCellBounds: Map<String, Rect2D>,              // slotKey -> Rect2D (array_slot:0 -> Rect)
    val arrayIndexLabelBounds: Map<String, Rect2D>,        // slotKey -> Rect2D (array_slot:0 -> Index Label Rect)
    val requiredSceneWidth: Float,
    val requiredSceneHeight: Float
)

/**
 * Pure Kotlin Multiplatform layout calculator for Heap scenes.
 * Contains no Compose or Android UI dependencies.
 */
internal object HeapSceneLayout {

    const val MIN_NODE_SIZE_DP = 40f
    const val MIN_LEVEL_GAP_DP = 48f
    const val MIN_ARRAY_CELL_WIDTH_DP = 36f
    const val ARRAY_CELL_HEIGHT_DP = 38f
    const val ARRAY_INDEX_LABEL_HEIGHT_DP = 16f
    const val MIN_HORIZONTAL_MARGIN_DP = 16f
    const val MIN_VERTICAL_MARGIN_DP = 20f
    const val BOTTOM_MARGIN_DP = 20f
    const val GAP_BETWEEN_REGIONS_DP = 20f

    fun calculateLayout(
        treeElementCount: Int,
        arrayElementCount: Int,
        availableWidthDp: Float,
        availableHeightDp: Float
    ): HeapSceneLayoutResult {
        if (treeElementCount <= 0 && arrayElementCount <= 0) {
            return HeapSceneLayoutResult(
                treeRegionBounds = Rect2D(0f, 0f, 0f, 0f),
                arrayRegionBounds = Rect2D(0f, 0f, 0f, 0f),
                treeNodeBounds = emptyMap(),
                treeConnectionLines = emptyMap(),
                arrayCellBounds = emptyMap(),
                arrayIndexLabelBounds = emptyMap(),
                requiredSceneWidth = 0f,
                requiredSceneHeight = 0f
            )
        }

        val treeMaxLevel = if (treeElementCount > 0) log2(treeElementCount.toDouble()).toInt() else 0
        val treeLevelsCount = treeMaxLevel + 1

        val requiredTreeWidth = max(availableWidthDp, MIN_HORIZONTAL_MARGIN_DP * 2 + max(1, treeElementCount) * MIN_NODE_SIZE_DP)
        val requiredTreeHeight = if (treeElementCount > 0) MIN_VERTICAL_MARGIN_DP * 2 + treeLevelsCount * MIN_LEVEL_GAP_DP else 0f

        val arrayElCount = max(1, arrayElementCount)
        val cellWidth = max(MIN_ARRAY_CELL_WIDTH_DP, (availableWidthDp - MIN_HORIZONTAL_MARGIN_DP * 2) / arrayElCount)
        val requiredArrayWidth = cellWidth * arrayElCount + MIN_HORIZONTAL_MARGIN_DP * 2
        val requiredArrayHeight = ARRAY_CELL_HEIGHT_DP + ARRAY_INDEX_LABEL_HEIGHT_DP + BOTTOM_MARGIN_DP

        val requiredSceneWidth = max(requiredTreeWidth, requiredArrayWidth)
        val requiredSceneHeight = (if (treeElementCount > 0) requiredTreeHeight + GAP_BETWEEN_REGIONS_DP else 0f) + requiredArrayHeight

        val treeRegionBounds = Rect2D(0f, 0f, requiredSceneWidth, requiredTreeHeight)
        val arrayRegionBounds = Rect2D(0f, if (treeElementCount > 0) requiredTreeHeight + GAP_BETWEEN_REGIONS_DP else 0f, requiredSceneWidth, requiredArrayHeight)

        // 1. Calculate Tree Node Bounds
        val nodeBounds = mutableMapOf<String, Rect2D>()
        for (i in 0 until treeElementCount) {
            val slotKey = "tree_slot:$i"
            val level = log2((i + 1).toDouble()).toInt()
            val levelStartIdx = (2.0.pow(level) - 1).toInt()
            val posInLevel = i - levelStartIdx
            val itemsInLevel = 2.0.pow(level).toInt()

            val levelY = treeRegionBounds.top + MIN_VERTICAL_MARGIN_DP + level * MIN_LEVEL_GAP_DP
            val colSpacing = requiredSceneWidth / (itemsInLevel + 1)
            val levelX = colSpacing * (posInLevel + 1) - MIN_NODE_SIZE_DP / 2f

            nodeBounds[slotKey] = Rect2D(
                left = max(0f, levelX),
                top = max(0f, levelY),
                width = MIN_NODE_SIZE_DP,
                height = MIN_NODE_SIZE_DP
            )
        }

        // 2. Calculate Tree Connection Lines (Parent Boundary -> Child Boundary)
        val connectionLines = mutableMapOf<String, LineSegment2D>()
        for (i in 0 until treeElementCount) {
            val parentSlotKey = "tree_slot:$i"
            val parentRect = nodeBounds[parentSlotKey] ?: continue
            val parentCenter = parentRect.center
            val radius = parentRect.width / 2f

            val leftChildIdx = 2 * i + 1
            if (leftChildIdx < treeElementCount) {
                val leftSlotKey = "tree_slot:$leftChildIdx"
                val leftRect = nodeBounds[leftSlotKey]
                if (leftRect != null) {
                    val childCenter = leftRect.center
                    val dX = childCenter.x - parentCenter.x
                    val dY = childCenter.y - parentCenter.y
                    val dist = max(0.001f, sqrt(dX * dX + dY * dY))

                    val startPoint = Point2D(
                        x = parentCenter.x + (dX / dist) * radius,
                        y = parentCenter.y + (dY / dist) * radius
                    )
                    val endPoint = Point2D(
                        x = childCenter.x - (dX / dist) * radius,
                        y = childCenter.y - (dY / dist) * radius
                    )

                    connectionLines["branch:$parentSlotKey->$leftSlotKey"] = LineSegment2D(
                        start = startPoint,
                        end = endPoint
                    )
                }
            }

            val rightChildIdx = 2 * i + 2
            if (rightChildIdx < treeElementCount) {
                val rightSlotKey = "tree_slot:$rightChildIdx"
                val rightRect = nodeBounds[rightSlotKey]
                if (rightRect != null) {
                    val childCenter = rightRect.center
                    val dX = childCenter.x - parentCenter.x
                    val dY = childCenter.y - parentCenter.y
                    val dist = max(0.001f, sqrt(dX * dX + dY * dY))

                    val startPoint = Point2D(
                        x = parentCenter.x + (dX / dist) * radius,
                        y = parentCenter.y + (dY / dist) * radius
                    )
                    val endPoint = Point2D(
                        x = childCenter.x - (dX / dist) * radius,
                        y = childCenter.y - (dY / dist) * radius
                    )

                    connectionLines["branch:$parentSlotKey->$rightSlotKey"] = LineSegment2D(
                        start = startPoint,
                        end = endPoint
                    )
                }
            }
        }

        // 3. Calculate Array Cell Bounds and Index Label Bounds
        val arrayBounds = mutableMapOf<String, Rect2D>()
        val arrayIndexLabelBounds = mutableMapOf<String, Rect2D>()

        for (i in 0 until arrayElementCount) {
            val slotKey = "array_slot:$i"
            val cellX = MIN_HORIZONTAL_MARGIN_DP + i * cellWidth
            val cellY = arrayRegionBounds.top + 4f

            val cellRect = Rect2D(
                left = max(0f, cellX),
                top = max(0f, cellY),
                width = cellWidth - 4f, // Leave 4dp gap between array cells
                height = ARRAY_CELL_HEIGHT_DP
            )
            arrayBounds[slotKey] = cellRect

            arrayIndexLabelBounds[slotKey] = Rect2D(
                left = cellRect.left,
                top = cellRect.bottom + 2f,
                width = cellRect.width,
                height = ARRAY_INDEX_LABEL_HEIGHT_DP
            )
        }

        return HeapSceneLayoutResult(
            treeRegionBounds = treeRegionBounds,
            arrayRegionBounds = arrayRegionBounds,
            treeNodeBounds = nodeBounds,
            treeConnectionLines = connectionLines,
            arrayCellBounds = arrayBounds,
            arrayIndexLabelBounds = arrayIndexLabelBounds,
            requiredSceneWidth = requiredSceneWidth,
            requiredSceneHeight = requiredSceneHeight
        )
    }

    fun calculateLayout(
        totalElements: Int,
        availableWidthDp: Float,
        availableHeightDp: Float
    ): HeapSceneLayoutResult {
        return calculateLayout(
            treeElementCount = totalElements,
            arrayElementCount = totalElements,
            availableWidthDp = availableWidthDp,
            availableHeightDp = availableHeightDp
        )
    }
}
