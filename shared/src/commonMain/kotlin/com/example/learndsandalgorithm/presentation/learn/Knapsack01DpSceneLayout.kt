package com.example.learndsandalgorithm.presentation.learn

internal data class KnapsackDpRect(val x: Float, val y: Float, val width: Float, val height: Float) {
    val right get() = x + width
    val bottom get() = y + height
    val centerX get() = x + width / 2f
}
internal data class KnapsackDpMetrics(val headerWidth: Float = 160f, val headerHeight: Float = 12f,
    val itemWidth: Float = 70f, val itemHeight: Float = 16f, val tableHeadingWidth: Float = 180f,
    val tableHeadingHeight: Float = 12f, val cellWidth: Float = 40f, val valueHeight: Float = 16f,
    val indexHeight: Float = 12f, val summaryWidth: Float = 270f, val summaryHeight: Float = 14f)
internal data class KnapsackDpLayout(val width: Float, val height: Float, val itemRegion: KnapsackDpRect,
    val itemHeading: KnapsackDpRect, val items: List<KnapsackDpRect>, val tableRegion: KnapsackDpRect,
    val tableHeading: KnapsackDpRect, val arrowLane: KnapsackDpRect, val indices: List<KnapsackDpRect>,
    val cells: List<KnapsackDpRect>, val decisionRegion: KnapsackDpRect, val summary: List<KnapsackDpRect>)
internal object Knapsack01DpSceneLayout {
    fun calculate(scene: KnapsackDpPresentation, viewportWidth: Float, viewportHeight: Float,
                  m: KnapsackDpMetrics = KnapsackDpMetrics()): KnapsackDpLayout {
        val itemWidth = maxOf(1f, m.itemWidth + 12f)
        val cellWidth = maxOf(34f, m.cellWidth + 8f)
        val width = maxOf(viewportWidth, m.headerWidth + 36f, m.tableHeadingWidth + 36f, m.summaryWidth + 36f,
            36f + scene.tableSize * cellWidth + (scene.tableSize - 1).coerceAtLeast(0) * 4f, itemWidth + 36f)
        val inner = width - 16f
        val columns = ((inner - 20f + 4f) / (itemWidth + 4f)).toInt().coerceAtLeast(1)
        val rows = ((scene.itemCount.coerceAtLeast(1) + columns - 1) / columns)
        val itemHeight = m.itemHeight + 8f
        val itemHeading = KnapsackDpRect(18f, 16f, inner - 20f, m.headerHeight)
        val itemTop = itemHeading.bottom + 4f
        val items = (0 until scene.itemCount).map { i -> KnapsackDpRect(18f + i % columns * (itemWidth + 4f), itemTop + i / columns * (itemHeight + 4f), itemWidth, itemHeight) }
        val itemRegion = KnapsackDpRect(8f, 8f, inner, itemTop + rows * itemHeight + (rows - 1) * 4f)
        val tableTop = itemRegion.bottom + 6f
        val tableHeading = KnapsackDpRect(18f, tableTop + 8f, inner - 20f, m.tableHeadingHeight)
        val arrowLane = KnapsackDpRect(18f, tableHeading.bottom + 2f, inner - 20f, 12f)
        val rowWidth = scene.tableSize * cellWidth + (scene.tableSize - 1).coerceAtLeast(0) * 4f
        val rowX = (width - rowWidth) / 2f
        val indices = (0 until scene.tableSize).map { i -> KnapsackDpRect(rowX + i * (cellWidth + 4f), arrowLane.bottom + 2f, cellWidth, m.indexHeight) }
        val cells = indices.map { KnapsackDpRect(it.x, it.bottom + 2f, cellWidth, maxOf(30f, m.valueHeight + 8f)) }
        val tableBottom = (cells.maxOfOrNull { it.bottom } ?: arrowLane.bottom) + 8f
        val table = KnapsackDpRect(8f, tableTop, inner, tableBottom - tableTop)
        val decisionY = table.bottom + 6f
        val summary = (0..2).map { i -> KnapsackDpRect(18f, decisionY + 8f + i * (m.summaryHeight + 2f), inner - 20f, m.summaryHeight) }
        val decision = KnapsackDpRect(8f, decisionY, inner, summary.last().bottom + 8f - decisionY)
        return KnapsackDpLayout(width, decision.bottom + 8f, itemRegion, itemHeading, items, table, tableHeading, arrowLane, indices, cells, decision, summary)
    }
}
