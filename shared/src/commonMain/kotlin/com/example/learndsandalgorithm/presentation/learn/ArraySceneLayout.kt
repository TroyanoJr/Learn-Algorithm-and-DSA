package com.example.learndsandalgorithm.presentation.learn

internal data class ArrayRegionLayout(
    val key: String,
    val headerBounds: Rect2D,
    val cellBounds: Map<Int, Rect2D>,
    val indexBounds: Map<Int, Rect2D>,
    val continuationBounds: Map<Int, Rect2D>,
    val columns: Int
)

internal data class ArraySceneLayoutResult(
    val regionLayouts: Map<String, ArrayRegionLayout>,
    val requiredSceneWidth: Float,
    val requiredSceneHeight: Float
)

internal object ArraySceneLayout {
    private const val HORIZONTAL_MARGIN = 12f
    private const val VERTICAL_MARGIN = 10f
    private const val ROW_GAP = 4f
    private const val REGION_GAP = 10f

    fun calculate(
        frames: List<ArraySceneFrame>,
        valueTextWidthDp: Float = 24f,
        valueTextHeightDp: Float = 18f,
        indexTextWidthDp: Float = 12f,
        indexTextHeightDp: Float = 13f,
        headerTextWidthDp: Float = 150f,
        headerTextHeightDp: Float = 16f,
        continuationTextWidthDp: Float = 105f,
        continuationTextHeightDp: Float = 14f
    ): ArraySceneLayoutResult {
        if (frames.isEmpty()) return ArraySceneLayoutResult(emptyMap(), 0f, 0f)
        val regionKeys = frames.first().regions.map { it.key }
        require(frames.all { it.regions.map(ArraySceneRegion::key) == regionKeys }) {
            "Array regions must remain sequence-stable"
        }

        val regions = regionKeys.map { key ->
            val variants = frames.mapNotNull { frame -> frame.regions.firstOrNull { it.key == key } }
            val region = variants.first().copy(
                layoutSlotCount = variants.maxOfOrNull { it.layoutSlotCount } ?: 0,
                columns = variants.maxOfOrNull { it.columns }?.coerceAtLeast(1) ?: 1,
                cellHeightDp = variants.maxOfOrNull { it.cellHeightDp } ?: 36f,
                columnGapDp = variants.maxOfOrNull { it.columnGapDp } ?: 4f
            )
            val headerHeight = if (variants.any { it.headerText != null }) maxOf(18f, headerTextHeightDp + 4f) else 0f
            region to headerHeight
        }

        val cellWidth = maxOf(34f, valueTextWidthDp + 12f)
        val indexWidth = maxOf(12f, indexTextWidthDp + 4f)
        val indexHeight = maxOf(14f, indexTextHeightDp + 2f)
        val continuationHeight = maxOf(14f, continuationTextHeightDp + 2f)
        val gridWidth = regions.maxOfOrNull { (region, _) ->
            val columns = minOf(region.layoutSlotCount, region.columns).coerceAtLeast(1)
            columns * cellWidth + (columns - 1).coerceAtLeast(0) * region.columnGapDp
        } ?: 0f
        val naturalHeaderWidth = frames.flatMap { it.regions }.mapNotNull { it.headerText }.maxOfOrNull { headerTextWidthDp } ?: 0f
        val continuationWidth = if (regions.any { (region, _) -> region.layoutSlotCount > region.columns }) continuationTextWidthDp else 0f
        val sceneWidth = maxOf(gridWidth, naturalHeaderWidth, continuationWidth) + HORIZONTAL_MARGIN * 2

        var top = VERTICAL_MARGIN
        val result = linkedMapOf<String, ArrayRegionLayout>()
        regions.forEachIndexed { regionIndex, (region, headerHeight) ->
            val key = region.key
            val reservedSlots = region.layoutSlotCount
            val columns = minOf(reservedSlots, region.columns).coerceAtLeast(1)
            val cellHeight = maxOf(region.cellHeightDp, valueTextHeightDp + 10f)
            val hasHeader = frames.any { frame -> frame.regions.first { it.key == key }.headerText != null }
            val headerBounds = if (hasHeader) Rect2D(HORIZONTAL_MARGIN, top, sceneWidth - HORIZONTAL_MARGIN * 2, headerHeight) else Rect2D(0f, top, 0f, 0f)
            if (hasHeader) top += headerHeight + 4f

            val cells = linkedMapOf<Int, Rect2D>()
            val indices = linkedMapOf<Int, Rect2D>()
            val continuation = linkedMapOf<Int, Rect2D>()
            val rowCount = if (reservedSlots == 0) 0 else (reservedSlots + columns - 1) / columns
            val rowItemHeight = cellHeight + 4f + indexHeight
            val regionGridWidth = columns * cellWidth + (columns - 1).coerceAtLeast(0) * region.columnGapDp
            var rowTop = top
            for (index in 0 until reservedSlots) {
                val row = index / columns
                val column = index % columns
                val rowStart = row * columns
                if (column == 0 && row > 0) {
                    continuation[rowStart] = Rect2D(HORIZONTAL_MARGIN, rowTop, maxOf(continuationWidth, regionGridWidth), continuationHeight)
                    rowTop += continuationHeight + ROW_GAP
                }
                val left = HORIZONTAL_MARGIN + column * (cellWidth + region.columnGapDp)
                cells[index] = Rect2D(left, rowTop, cellWidth, cellHeight)
                indices[index] = Rect2D(left, rowTop + cellHeight + 4f, cellWidth, indexHeight)
                if (column == columns - 1 || index == reservedSlots - 1) {
                    rowTop += rowItemHeight
                    if (row < rowCount - 1) rowTop += ROW_GAP
                }
            }
            if (rowCount > 0) top = rowTop
            if (regionIndex < regions.lastIndex) top += REGION_GAP
            result[key] = ArrayRegionLayout(key, headerBounds, cells, indices, continuation, columns)
        }
        return ArraySceneLayoutResult(result, sceneWidth, top + VERTICAL_MARGIN)
    }

    fun initialFitScale(layout: ArraySceneLayoutResult, viewportWidth: Float, viewportHeight: Float): Float =
        if (layout.requiredSceneWidth <= 0f || layout.requiredSceneHeight <= 0f || viewportWidth <= 0f || viewportHeight <= 0f) 1f
        else minOf(1f, viewportWidth / layout.requiredSceneWidth, viewportHeight / layout.requiredSceneHeight)
}
