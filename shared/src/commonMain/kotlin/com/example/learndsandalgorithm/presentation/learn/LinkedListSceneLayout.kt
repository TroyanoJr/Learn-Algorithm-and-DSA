package com.example.learndsandalgorithm.presentation.learn

import kotlin.math.max

/**
 * Pure layout result containing calculated geometry for a Linked List scene in one unified coordinate space.
 */
internal data class LinkedListSceneLayoutResult(
    val nodeBounds: Map<String, Rect2D>,               // slotKey -> Rect2D
    val dataFieldBounds: Map<String, Rect2D>,          // slotKey -> Rect2D
    val nextFieldBounds: Map<String, Rect2D>,          // slotKey -> Rect2D
    val connectionLines: Map<String, LineSegment2D>,   // connectionKey -> LineSegment2D
    val badgeBounds: Map<String, Rect2D>,              // slotKey -> Rect2D
    val requiredSceneWidth: Float,
    val requiredSceneHeight: Float
)

/**
 * Pure Kotlin Multiplatform layout calculator for Linked List scenes based on explicit connections.
 */
internal object LinkedListSceneLayout {

    const val NODE_WIDTH_DP = 76f
    const val NODE_HEIGHT_DP = 44f
    const val HEAD_POINTER_WIDTH_DP = 48f
    const val HEAD_POINTER_HEIGHT_DP = 36f
    const val NULL_NODE_WIDTH_DP = 44f
    const val NULL_NODE_HEIGHT_DP = 36f
    const val GAP_BETWEEN_NODES_DP = 40f
    const val DETACHED_Y_OFFSET_DP = 64f
    const val HORIZONTAL_MARGIN_DP = 16f
    const val VERTICAL_MARGIN_DP = 24f
    const val BADGE_HEIGHT_DP = 16f

    fun calculateLayout(
        subScene: StructureSubScene?,
        availableWidthDp: Float = 360f,
        availableHeightDp: Float = 400f
    ): LinkedListSceneLayoutResult {
        val nodeBounds = mutableMapOf<String, Rect2D>()
        val dataFieldBounds = mutableMapOf<String, Rect2D>()
        val nextFieldBounds = mutableMapOf<String, Rect2D>()
        val badgeBounds = mutableMapOf<String, Rect2D>()

        val entities = subScene?.entities ?: emptyList()
        val connections = subScene?.connections ?: emptyList()

        val headEntity = entities.firstOrNull { it.elementId == "head_ptr" }
        val nullEntity = entities.firstOrNull { it.elementId == "null_ptr" }
        val detachedEntities = entities.filter { it.slotKey.startsWith("slot:detached") }
        val mainChainEntities = entities.filter { it.elementId != "head_ptr" && it.elementId != "null_ptr" && !it.slotKey.startsWith("slot:detached") }

        var currentX = HORIZONTAL_MARGIN_DP

        // 1. HEAD Pointer Layout
        if (headEntity != null) {
            val headRect = Rect2D(
                left = currentX,
                top = VERTICAL_MARGIN_DP + BADGE_HEIGHT_DP + (NODE_HEIGHT_DP - HEAD_POINTER_HEIGHT_DP) / 2f,
                width = HEAD_POINTER_WIDTH_DP,
                height = HEAD_POINTER_HEIGHT_DP
            )
            nodeBounds[headEntity.slotKey] = headRect
            currentX += HEAD_POINTER_WIDTH_DP + GAP_BETWEEN_NODES_DP
        }

        // 2. Main Chain Nodes Layout
        mainChainEntities.forEachIndexed { index, entity ->
            val nodeY = VERTICAL_MARGIN_DP + BADGE_HEIGHT_DP + 4f
            val nodeRect = Rect2D(
                left = currentX,
                top = nodeY,
                width = NODE_WIDTH_DP,
                height = NODE_HEIGHT_DP
            )
            nodeBounds[entity.slotKey] = nodeRect

            val dataRect = Rect2D(
                left = nodeRect.left,
                top = nodeRect.top,
                width = 46f,
                height = NODE_HEIGHT_DP
            )
            val nextRect = Rect2D(
                left = nodeRect.left + 46f,
                top = nodeRect.top,
                width = 30f,
                height = NODE_HEIGHT_DP
            )
            dataFieldBounds[entity.slotKey] = dataRect
            nextFieldBounds[entity.slotKey] = nextRect

            badgeBounds[entity.slotKey] = Rect2D(
                left = nodeRect.left,
                top = nodeRect.top - BADGE_HEIGHT_DP - 2f,
                width = NODE_WIDTH_DP,
                height = BADGE_HEIGHT_DP
            )

            currentX += NODE_WIDTH_DP + GAP_BETWEEN_NODES_DP
        }

        // 3. NULL Pointer Layout
        if (nullEntity != null) {
            val nullY = VERTICAL_MARGIN_DP + BADGE_HEIGHT_DP + 4f + (NODE_HEIGHT_DP - NULL_NODE_HEIGHT_DP) / 2f
            val nullRect = Rect2D(
                left = currentX,
                top = nullY,
                width = NULL_NODE_WIDTH_DP,
                height = NULL_NODE_HEIGHT_DP
            )
            nodeBounds[nullEntity.slotKey] = nullRect
            currentX += NULL_NODE_WIDTH_DP + HORIZONTAL_MARGIN_DP
        }

        // 4. Detached Nodes Layout
        if (detachedEntities.isNotEmpty()) {
            val detachedY = VERTICAL_MARGIN_DP + BADGE_HEIGHT_DP + NODE_HEIGHT_DP + DETACHED_Y_OFFSET_DP
            var detachedX = HORIZONTAL_MARGIN_DP + (if (headEntity != null) HEAD_POINTER_WIDTH_DP + GAP_BETWEEN_NODES_DP else 0f)

            detachedEntities.forEach { entity ->
                val nodeRect = Rect2D(
                    left = detachedX,
                    top = detachedY,
                    width = NODE_WIDTH_DP,
                    height = NODE_HEIGHT_DP
                )
                nodeBounds[entity.slotKey] = nodeRect

                val dataRect = Rect2D(
                    left = nodeRect.left,
                    top = nodeRect.top,
                    width = 46f,
                    height = NODE_HEIGHT_DP
                )
                val nextRect = Rect2D(
                    left = nodeRect.left + 46f,
                    top = nodeRect.top,
                    width = 30f,
                    height = NODE_HEIGHT_DP
                )
                dataFieldBounds[entity.slotKey] = dataRect
                nextFieldBounds[entity.slotKey] = nextRect

                badgeBounds[entity.slotKey] = Rect2D(
                    left = nodeRect.left,
                    top = nodeRect.top - BADGE_HEIGHT_DP - 2f,
                    width = NODE_WIDTH_DP,
                    height = BADGE_HEIGHT_DP
                )

                detachedX += NODE_WIDTH_DP + GAP_BETWEEN_NODES_DP
            }
        }

        // 5. Calculate Connection Lines based explicitly on subScene.connections
        val connectionLines = mutableMapOf<String, LineSegment2D>()
        connections.forEach { conn ->
            val fromEntity = entities.firstOrNull { it.visualKey == conn.fromVisualKey || it.elementId == conn.fromVisualKey.substringAfter("linked_list:") }
            val toEntity = entities.firstOrNull { it.visualKey == conn.toVisualKey || it.elementId == conn.toVisualKey.substringAfter("linked_list:") }

            if (fromEntity != null && toEntity != null) {
                val fromSlot = fromEntity.slotKey
                val toSlot = toEntity.slotKey

                val startPoint = when {
                    fromEntity.elementId == "head_ptr" -> {
                        val r = nodeBounds[fromSlot]
                        if (r != null) Point2D(r.right, r.center.y) else null
                    }
                    else -> {
                        val nextR = nextFieldBounds[fromSlot]
                        if (nextR != null) Point2D(nextR.center.x, nextR.center.y) else null
                    }
                }

                val endPoint = when {
                    toEntity.elementId == "null_ptr" -> {
                        val r = nodeBounds[toSlot]
                        if (r != null) Point2D(r.left, r.center.y) else null
                    }
                    else -> {
                        val dataR = dataFieldBounds[toSlot] ?: nodeBounds[toSlot]
                        if (dataR != null) Point2D(dataR.left, dataR.center.y) else null
                    }
                }

                if (startPoint != null && endPoint != null) {
                    connectionLines[conn.connectionKey] = LineSegment2D(startPoint, endPoint)
                }
            }
        }

        val requiredSceneWidth = max(availableWidthDp, currentX + HORIZONTAL_MARGIN_DP)
        val requiredSceneHeight = if (detachedEntities.isNotEmpty()) {
            VERTICAL_MARGIN_DP + BADGE_HEIGHT_DP + NODE_HEIGHT_DP + DETACHED_Y_OFFSET_DP + NODE_HEIGHT_DP + VERTICAL_MARGIN_DP
        } else {
            VERTICAL_MARGIN_DP + BADGE_HEIGHT_DP + NODE_HEIGHT_DP + VERTICAL_MARGIN_DP + 16f
        }

        return LinkedListSceneLayoutResult(
            nodeBounds = nodeBounds,
            dataFieldBounds = dataFieldBounds,
            nextFieldBounds = nextFieldBounds,
            connectionLines = connectionLines,
            badgeBounds = badgeBounds,
            requiredSceneWidth = requiredSceneWidth,
            requiredSceneHeight = requiredSceneHeight
        )
    }

    // Backward compatibility helper if needed
    fun calculateLayout(
        mainChainSlotKeys: List<String>,
        detachedSlotKeys: List<String> = emptyList(),
        hasHeadPointer: Boolean = true,
        hasNullPointer: Boolean = true,
        availableWidthDp: Float = 360f,
        availableHeightDp: Float = 400f
    ): LinkedListSceneLayoutResult {
        return calculateLayout(null, availableWidthDp, availableHeightDp)
    }
}
