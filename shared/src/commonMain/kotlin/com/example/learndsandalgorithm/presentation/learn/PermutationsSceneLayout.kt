package com.example.learndsandalgorithm.presentation.learn

internal data class PermutationRect(val x: Float, val y: Float, val width: Float, val height: Float) {
    val right get() = x + width
    val bottom get() = y + height
    val centerX get() = x + width / 2f
}

internal data class PermutationsLayoutMetrics(
    val headingWidth: Float = 150f,
    val startWidth: Float = 85f,
    val headingHeight: Float = 12f,
    val labelWidth: Float = 40f,
    val labelHeight: Float = 12f,
    val valueWidth: Float = 25f,
    val valueHeight: Float = 20f,
    val resultHeadingWidth: Float = 240f,
    val resultHeadingHeight: Float = 12f,
    val resultWidth: Float = 56f,
    val resultHeight: Float = 14f,
    val emptyWidth: Float = 150f,
    val emptyHeight: Float = 12f
)

internal data class PermutationsSceneLayoutResult(
    val width: Float,
    val height: Float,
    val workingRegion: PermutationRect,
    val heading: PermutationRect,
    val startIndex: PermutationRect,
    val swapIndicator: PermutationRect,
    val labels: List<PermutationRect>,
    val cells: List<PermutationRect>,
    val resultRegion: PermutationRect,
    val resultHeading: PermutationRect,
    val emptyMessage: PermutationRect,
    val results: List<PermutationRect>,
    val resultColumns: Int
)

internal object PermutationsSceneLayout {
    fun calculate(presentation: PermutationsPresentation, viewportWidth: Float, viewportHeight: Float,
                  metrics: PermutationsLayoutMetrics = PermutationsLayoutMetrics()): PermutationsSceneLayoutResult {
        val count = presentation.maximumArraySize
        val results = presentation.maximumResultCount
        val cellWidth = maxOf(44f, metrics.valueWidth + 12f, metrics.labelWidth + 8f)
        val cellHeight = maxOf(44f, metrics.valueHeight + 8f)
        val resultWidth = maxOf(1f, metrics.resultWidth + 12f)
        val resultHeight = maxOf(metrics.resultHeight + 8f, metrics.emptyHeight + 8f)
        val width = maxOf(viewportWidth, 36f + count * cellWidth + (count - 1).coerceAtLeast(0) * 4f,
            metrics.headingWidth + metrics.startWidth + 42f, metrics.resultHeadingWidth + 36f,
            metrics.emptyWidth + 36f, resultWidth + 36f)
        val innerWidth = width - 16f
        val resultAvailable = innerWidth - 20f
        val maxColumns = minOf(results.coerceAtLeast(1), ((resultAvailable + 6f) / (resultWidth + 6f)).toInt().coerceAtLeast(1))
        val headingHeight = metrics.headingHeight.coerceAtLeast(1f)
        val workHeight = 8f + headingHeight + 4f + 14f + metrics.labelHeight + 2f + cellHeight + 8f
        fun heightFor(columns: Int): Float {
            val rows = if (results == 0) 1 else (results + columns - 1) / columns
            return 8f + workHeight + 6f + 8f + metrics.resultHeadingHeight + 6f + rows * resultHeight + (rows - 1) * 6f + 8f + 8f
        }
        val columns = (maxColumns downTo 1).maxBy { columns -> minOf(viewportWidth / width, viewportHeight / heightFor(columns), 1f) }
        val work = PermutationRect(8f, 8f, innerWidth, workHeight)
        val heading = PermutationRect(18f, 16f, metrics.headingWidth, headingHeight)
        val start = PermutationRect(work.right - 10f - metrics.startWidth, 16f, metrics.startWidth, headingHeight)
        val indicator = PermutationRect(18f, heading.bottom + 4f, innerWidth - 20f, 14f)
        val totalCells = count * cellWidth + (count - 1).coerceAtLeast(0) * 4f
        val firstX = (width - totalCells) / 2f
        val labels = (0 until count).map { index -> PermutationRect(firstX + index * (cellWidth + 4f), indicator.bottom, cellWidth, metrics.labelHeight) }
        val cells = labels.map { label -> PermutationRect(label.x, label.bottom + 2f, cellWidth, cellHeight) }
        val resultY = work.bottom + 6f
        val resultHeading = PermutationRect(18f, resultY + 8f, innerWidth - 20f, metrics.resultHeadingHeight)
        val resultTop = resultHeading.bottom + 6f
        val resultRects = (0 until results).map { index ->
            PermutationRect(18f + (index % columns) * (resultWidth + 6f), resultTop + (index / columns) * (resultHeight + 6f), resultWidth, resultHeight)
        }
        val bottom = if (resultRects.isEmpty()) resultTop + resultHeight else resultRects.maxOf { it.bottom }
        val resultRegion = PermutationRect(8f, resultY, innerWidth, bottom - resultY + 8f)
        return PermutationsSceneLayoutResult(width, resultRegion.bottom + 8f, work, heading, start, indicator, labels, cells,
            resultRegion, resultHeading, PermutationRect(18f, resultTop, resultAvailable, resultHeight), resultRects, columns)
    }
}
