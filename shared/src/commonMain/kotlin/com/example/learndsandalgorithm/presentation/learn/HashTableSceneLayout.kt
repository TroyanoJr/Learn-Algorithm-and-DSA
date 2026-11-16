package com.example.learndsandalgorithm.presentation.learn

import kotlin.math.max

/**
 * Pure layout result containing calculated geometry for Hash Table scenes in DP space,
 * separating actual chain nodes from sequence-level reserved vertical space,
 * and providing explicit non-overlapping bounds for title, formula, key badge, collision badge,
 * breakdown, sum, modulo, target arrows, probe labels, buckets, entries, chain nodes,
 * chain null terminators, chain connection lines, and index labels.
 */
internal data class HashTableSceneLayoutResult(
    val hashCardBounds: Rect2D,
    val titleBounds: Rect2D,
    val formulaBounds: Rect2D,
    val keyBadgeBounds: Rect2D? = null,
    val collisionBadgeBounds: Rect2D? = null,
    val breakdownBounds: Rect2D? = null,
    val sumBounds: Rect2D? = null,
    val moduloBounds: Rect2D? = null,
    val targetArrowBounds: Map<String, Rect2D> = emptyMap(),
    val probeLabelBounds: Map<String, Rect2D> = emptyMap(),
    val bucketBounds: Map<String, Rect2D> = emptyMap(),      // bucket_slot:i or linear_slot:i -> Rect2D
    val entryBounds: Map<String, Rect2D> = emptyMap(),       // entry_slot:i -> Rect2D
    val chainNodeBounds: Map<String, Rect2D> = emptyMap(),
    val chainNullBounds: Map<String, Rect2D> = emptyMap(),   // chain_null:i -> Rect2D
    val chainConnectionLines: Map<String, LineSegment2D> = emptyMap(),
    val indexLabelBounds: Map<String, Rect2D> = emptyMap(),  // bucket_slot:i or linear_slot:i -> Index Label Rect
    val requiredSceneWidth: Float,
    val requiredSceneHeight: Float
)

/**
 * Pure Kotlin Multiplatform layout calculator for Hash Table scenes.
 */
internal enum class HashTableSceneMode { COMPLETE, COLLISION_STRUCTURE }

internal data class HashTableStructuralMetrics(
    val bucketWidth: Float = 64f,
    val nodeHeight: Float = 38f,
    val cellHeight: Float = 52f,
    val labelHeight: Float = 18f
)

internal object HashTableSceneLayout {

    const val MIN_BUCKET_WIDTH_DP = 64f
    const val BUCKET_HEIGHT_DP = 52f
    const val BUCKET_HEADER_HEIGHT_DP = 24f
    const val CHAIN_NODE_HEIGHT_DP = 38f
    const val CHAIN_GAP_DP = 10f
    const val PROBE_LABEL_HEIGHT_DP = 16f
    const val INDEX_LABEL_HEIGHT_DP = 16f
    const val HORIZONTAL_MARGIN_DP = 12f
    const val VERTICAL_MARGIN_DP = 12f
    const val GAP_BETWEEN_REGIONS_DP = 10f
    const val TARGET_ARROW_HEIGHT_DP = 14f
    const val GAP_DP = 8f

    fun calculateLayout(
        capacity: Int,
        availableWidthDp: Float = 360f,
        availableHeightDp: Float = 400f,
        hasActiveKey: Boolean = false,
        hasBreakdown: Boolean = false,
        subScene: StructureSubScene? = null,
        strategy: CollisionStrategy? = null,
        maxChainDepth: Int = 0,
        mode: HashTableSceneMode = HashTableSceneMode.COMPLETE,
        metrics: HashTableStructuralMetrics = HashTableStructuralMetrics()
    ): HashTableSceneLayoutResult {
        if (capacity <= 0) {
            return HashTableSceneLayoutResult(
                hashCardBounds = Rect2D(0f, 0f, 0f, 0f),
                titleBounds = Rect2D(0f, 0f, 0f, 0f),
                formulaBounds = Rect2D(0f, 0f, 0f, 0f),
                bucketBounds = emptyMap(),
                entryBounds = emptyMap(),
                indexLabelBounds = emptyMap(),
                requiredSceneWidth = 0f,
                requiredSceneHeight = 0f
            )
        }

        val isSeparateChaining = strategy == CollisionStrategy.SEPARATE_CHAINING || subScene?.entities?.any { it.slotKey.startsWith("chain_slot:") } == true
        val isLinearProbing = strategy == CollisionStrategy.LINEAR_PROBING || subScene?.entities?.any { it.slotKey.startsWith("linear_slot:") } == true

        val structureOnly = mode == HashTableSceneMode.COLLISION_STRUCTURE &&
            (isSeparateChaining || isLinearProbing)
        val nodeHeight = if (structureOnly) metrics.nodeHeight else CHAIN_NODE_HEIGHT_DP
        val cellHeight = if (structureOnly) metrics.cellHeight else BUCKET_HEIGHT_DP
        val labelHeight = if (structureOnly) metrics.labelHeight else INDEX_LABEL_HEIGHT_DP
        val headerHeight = if (structureOnly) max(BUCKET_HEADER_HEIGHT_DP, labelHeight + 4f) else BUCKET_HEADER_HEIGHT_DP
        val minimumWidth = if (structureOnly) metrics.bucketWidth else MIN_BUCKET_WIDTH_DP

        // Stable hash card height across all snapshots for collision strategies
        val hashCardHeight = if (isSeparateChaining || isLinearProbing) 120f else 105f

        val totalPaddingWidth = HORIZONTAL_MARGIN_DP * 2 + (capacity - 1) * GAP_DP
        val targetWidth = max(availableWidthDp, totalPaddingWidth + capacity * minimumWidth)
        val bucketWidth = max(minimumWidth, (targetWidth - totalPaddingWidth) / capacity)

        val requiredSceneWidth = bucketWidth * capacity + GAP_DP * (capacity - 1) + HORIZONTAL_MARGIN_DP * 2

        val cardLeft = HORIZONTAL_MARGIN_DP
        val cardTop = VERTICAL_MARGIN_DP
        val cardWidth = requiredSceneWidth - HORIZONTAL_MARGIN_DP * 2

        val hashCardBounds = Rect2D(
            left = cardLeft,
            top = cardTop,
            width = cardWidth,
            height = hashCardHeight
        )

        val titleBounds = Rect2D(
            left = cardLeft + 12f,
            top = cardTop + 8f,
            width = cardWidth * 0.45f,
            height = 18f
        )

        val formulaBounds = Rect2D(
            left = cardLeft + cardWidth * 0.48f,
            top = cardTop + 8f,
            width = cardWidth * 0.48f,
            height = 18f
        )

        val keyBadgeBounds = if (hasActiveKey) {
            Rect2D(
                left = cardLeft + 12f,
                top = cardTop + 30f,
                width = minOf(cardWidth - 24f, 260f),
                height = 24f
            )
        } else null

        val collisionBadgeBounds = if (hasActiveKey) {
            Rect2D(
                left = cardLeft + cardWidth * 0.68f,
                top = cardTop + 30f,
                width = cardWidth * 0.30f,
                height = 24f
            )
        } else null

        val breakdownBounds = if (hasBreakdown) {
            Rect2D(
                left = cardLeft + 12f,
                top = cardTop + 60f,
                width = cardWidth - 24f,
                height = 22f
            )
        } else null

        val sumBounds = if (hasBreakdown) {
            Rect2D(
                left = cardLeft + 12f,
                top = cardTop + 88f,
                width = (cardWidth - 24f) * 0.45f,
                height = 22f
            )
        } else null

        val moduloBounds = if (hasBreakdown) {
            Rect2D(
                left = cardLeft + cardWidth * 0.48f,
                top = cardTop + 88f,
                width = (cardWidth - 24f) * 0.48f,
                height = 22f
            )
        } else null

        val entities = subScene?.entities ?: emptyList()

        val targetArrowY = if (structureOnly) VERTICAL_MARGIN_DP else hashCardBounds.bottom + GAP_BETWEEN_REGIONS_DP
        val markerHeight = if (structureOnly) labelHeight else TARGET_ARROW_HEIGHT_DP
        val bucketY = targetArrowY + markerHeight

        val targetArrowBounds = mutableMapOf<String, Rect2D>()
        val probeLabelBounds = mutableMapOf<String, Rect2D>()
        val bucketBounds = mutableMapOf<String, Rect2D>()
        val entryBounds = mutableMapOf<String, Rect2D>()
        val chainNodeBounds = mutableMapOf<String, Rect2D>()
        val chainNullBounds = mutableMapOf<String, Rect2D>()
        val chainConnectionLines = mutableMapOf<String, LineSegment2D>()
        val indexLabelBounds = mutableMapOf<String, Rect2D>()

        val inferredMaxDepth = entities.filter { it.slotKey.startsWith("chain_slot:") }.maxOfOrNull {
            it.slotKey.substringAfterLast(":").toIntOrNull()?.plus(1) ?: 0
        } ?: 0
        val chainDepthToUse = max(max(1, maxChainDepth), inferredMaxDepth)
        var maxObservedBottomY = bucketY + cellHeight

        for (i in 0 until capacity) {
            val bucketSlotKey = "bucket_slot:$i"
            val linearSlotKey = "linear_slot:$i"
            val entrySlotKey = "entry_slot:$i"

            val cellX = HORIZONTAL_MARGIN_DP + i * (bucketWidth + GAP_DP)

            targetArrowBounds[bucketSlotKey] = Rect2D(
                left = cellX,
                top = targetArrowY,
                width = bucketWidth,
                height = markerHeight
            )

            probeLabelBounds[linearSlotKey] = Rect2D(
                left = cellX,
                top = targetArrowY,
                width = bucketWidth,
                height = if (structureOnly) labelHeight else PROBE_LABEL_HEIGHT_DP
            )

            if (isSeparateChaining) {
                // Base Header Bucket (Index box [i])
                val headerRect = Rect2D(
                    left = cellX,
                    top = bucketY,
                    width = bucketWidth,
                    height = headerHeight
                )
                bucketBounds[bucketSlotKey] = headerRect
                indexLabelBounds[bucketSlotKey] = headerRect

                // Separate actual chain nodes from reserved sequence-level geometry
                val chainEntities = entities.filter { it.slotKey.startsWith("chain_slot:$i:") }
                val actualChainSize = chainEntities.size

                var nodeY = bucketY + headerHeight + 6f
                // Only actual nodes present in the current snapshot appear in chainNodeBounds
                chainEntities.forEachIndexed { j, chainEntity ->
                    val chainSlotKey = chainEntity.slotKey
                    val nodeRect = Rect2D(
                        left = cellX,
                        top = nodeY,
                        width = bucketWidth,
                        height = nodeHeight
                    )
                    chainNodeBounds[chainSlotKey] = nodeRect
                    if (structureOnly && j == 0) {
                        chainConnectionLines["link:bucket_slot:$i->$chainSlotKey"] = LineSegment2D(
                            Point2D(headerRect.center.x, headerRect.bottom),
                            Point2D(nodeRect.center.x, nodeRect.top)
                        )
                    }

                    if (j < actualChainSize - 1) {
                        val nextNodeY = nodeY + nodeHeight + CHAIN_GAP_DP
                        val start = Point2D(nodeRect.center.x, nodeRect.bottom)
                        val end = Point2D(nodeRect.center.x, nextNodeY)
                        chainConnectionLines["link:$chainSlotKey->chain_slot:$i:${j + 1}"] = LineSegment2D(start, end)
                    }

                    nodeY += nodeHeight + CHAIN_GAP_DP
                }

                // Explicit null terminal bounds right after the last actual node (or header if empty)
                val nullY = if (actualChainSize > 0) {
                    bucketY + headerHeight + 6f + actualChainSize * (nodeHeight + CHAIN_GAP_DP) - 2f
                } else {
                    bucketY + headerHeight + 4f
                }

                val nullRect = Rect2D(
                    left = cellX,
                    top = nullY,
                    width = bucketWidth,
                    height = if (structureOnly) labelHeight else 18f
                )
                chainNullBounds["chain_null:$i"] = nullRect

                // Explicit connection from last real node (or header if empty) to null terminator
                if (actualChainSize > 0) {
                    val lastNodeSlotKey = "chain_slot:$i:${actualChainSize - 1}"
                    val lastNodeRect = chainNodeBounds[lastNodeSlotKey]
                    if (lastNodeRect != null) {
                        val start = Point2D(lastNodeRect.center.x, lastNodeRect.bottom)
                        val end = Point2D(nullRect.center.x, nullRect.top)
                        chainConnectionLines["link:$lastNodeSlotKey->chain_null:$i"] = LineSegment2D(start, end)
                    }
                } else {
                    val start = Point2D(headerRect.center.x, headerRect.bottom)
                    val end = Point2D(nullRect.center.x, nullRect.top)
                    chainConnectionLines["link:bucket_slot:$i->chain_null:$i"] = LineSegment2D(start, end)
                }

                // Reserved vertical height based on sequence-level maxChainDepth for stable layout dimensions across snapshots
                val reservedNodeY = bucketY + headerHeight + 6f + chainDepthToUse * (nodeHeight + CHAIN_GAP_DP)
                maxObservedBottomY = max(maxObservedBottomY, reservedNodeY + (if (structureOnly) labelHeight else 18f))
            } else {
                // Linear Probing or Standard Buckets
                val bucketRect = Rect2D(
                    left = cellX,
                    top = bucketY,
                    width = bucketWidth,
                    height = cellHeight
                )
                if (isLinearProbing) {
                    bucketBounds[linearSlotKey] = bucketRect
                    indexLabelBounds[linearSlotKey] = Rect2D(
                        left = bucketRect.left,
                        top = bucketRect.bottom + 4f,
                        width = bucketRect.width,
                        height = labelHeight
                    )
                } else {
                    bucketBounds[bucketSlotKey] = bucketRect
                    indexLabelBounds[bucketSlotKey] = Rect2D(
                        left = bucketRect.left,
                        top = bucketRect.bottom + 4f,
                        width = bucketRect.width,
                        height = labelHeight
                    )
                }
                entryBounds[entrySlotKey] = bucketRect

                maxObservedBottomY = max(maxObservedBottomY, bucketY + cellHeight + (if (structureOnly) 4f + labelHeight else INDEX_LABEL_HEIGHT_DP))
            }
        }

        val requiredSceneHeight = maxObservedBottomY + VERTICAL_MARGIN_DP // Content-derived height without availableHeightDp inflation

        return HashTableSceneLayoutResult(
            hashCardBounds = if (structureOnly) Rect2D(0f, 0f, 0f, 0f) else hashCardBounds,
            titleBounds = if (structureOnly) Rect2D(0f, 0f, 0f, 0f) else titleBounds,
            formulaBounds = if (structureOnly) Rect2D(0f, 0f, 0f, 0f) else formulaBounds,
            keyBadgeBounds = if (structureOnly) null else keyBadgeBounds,
            collisionBadgeBounds = if (structureOnly) null else collisionBadgeBounds,
            breakdownBounds = if (structureOnly) null else breakdownBounds,
            sumBounds = if (structureOnly) null else sumBounds,
            moduloBounds = if (structureOnly) null else moduloBounds,
            targetArrowBounds = targetArrowBounds,
            probeLabelBounds = probeLabelBounds,
            bucketBounds = bucketBounds,
            entryBounds = entryBounds,
            chainNodeBounds = chainNodeBounds,
            chainNullBounds = chainNullBounds,
            chainConnectionLines = chainConnectionLines,
            indexLabelBounds = indexLabelBounds,
            requiredSceneWidth = requiredSceneWidth,
            requiredSceneHeight = requiredSceneHeight
        )
    }
}
