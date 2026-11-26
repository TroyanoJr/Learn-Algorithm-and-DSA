package com.example.learndsandalgorithm.presentation.learn

internal data class CoinDpRect(val x: Float, val y: Float, val width: Float, val height: Float) {
    val right get() = x + width
    val bottom get() = y + height
    val centerX get() = x + width / 2
}
internal data class CoinDpMetrics(val textWidth: Float = 250f, val textHeight: Float = 14f,
    val cellTextWidth: Float = 24f, val coinWidth: Float = 24f)
internal data class CoinDpLayout(val width: Float, val height: Float, val table: CoinDpRect,
    val heading: CoinDpRect, val coins: List<CoinDpRect>, val arrowLane: CoinDpRect,
    val indices: List<CoinDpRect>, val cells: List<CoinDpRect>, val summary: List<CoinDpRect>) {
    fun dependencyArrow(source: Int, destination: Int): CoinArrowGeometry =
        CoinArrowGeometry(cells[source].x - 2f, cells[destination].x - 2f, arrowLane.y + 6f,
            cells[source].y, cells[destination].y)
}
internal data class CoinArrowGeometry(val startX: Float, val endX: Float, val laneY: Float,
    val sourceY: Float, val destinationY: Float)
internal object CoinChangeDpSceneLayout {
    fun calculate(scene: CoinChangePresentation, viewportWidth: Float, metrics: CoinDpMetrics = CoinDpMetrics()): CoinDpLayout {
        val cellWidth = maxOf(36f, metrics.cellTextWidth + 12f)
        val coinWidth = maxOf(32f, metrics.coinWidth + 14f)
        val coinCount = scene.frames.firstOrNull()?.step?.coins?.size ?: 0
        val width = maxOf(viewportWidth, metrics.textWidth + 36f, 36f + scene.tableSize * cellWidth +
            (scene.tableSize - 1).coerceAtLeast(0) * 4f, 36f + coinCount * (coinWidth + 4f))
        val heading = CoinDpRect(18f, 16f, width - 36f, metrics.textHeight)
        val coinY = heading.bottom + 5f
        val coins = (0 until coinCount).map { CoinDpRect(18f + it * (coinWidth + 4f), coinY, coinWidth, metrics.textHeight + 12f) }
        val arrow = CoinDpRect(18f, coinY + metrics.textHeight + 16f, width - 36f, 12f)
        val rowWidth = scene.tableSize * cellWidth + (scene.tableSize - 1).coerceAtLeast(0) * 4f
        val rowX = (width - rowWidth) / 2f
        val indices = (0 until scene.tableSize).map { CoinDpRect(rowX + it * (cellWidth + 4f), arrow.bottom + 2f, cellWidth, metrics.textHeight) }
        val cells = indices.map { CoinDpRect(it.x, it.bottom + 2f, cellWidth, metrics.textHeight + 14f) }
        val tableBottom = (cells.maxOfOrNull { it.bottom } ?: arrow.bottom) + 8f
        val table = CoinDpRect(8f, 8f, width - 16f, tableBottom - 8f)
        val summary = (0..2).map { CoinDpRect(18f, tableBottom + 12f + it * (metrics.textHeight + 3f), width - 36f, metrics.textHeight) }
        return CoinDpLayout(width, summary.last().bottom + 12f, table, heading, coins, arrow, indices, cells, summary)
    }
}
