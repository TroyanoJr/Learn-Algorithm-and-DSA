package com.example.learndsandalgorithm.presentation.learn

internal data class LcsRect(val x: Float, val y: Float, val width: Float, val height: Float) {
    val right get() = x + width
    val bottom get() = y + height
    val centerX get() = x + width / 2f
    val centerY get() = y + height / 2f
}
internal data class LcsLayout(val width: Float, val height: Float, val heading: LcsRect,
    val cells: List<List<LcsRect>>, val rowHeaders: List<LcsRect>, val columnHeaders: List<LcsRect>,
    val summary: List<LcsRect>)
internal object LcsDpSceneLayout {
    fun calculate(scene: LcsPresentation, viewportWidth: Float, textWidth: Float = 200f,
                  textHeight: Float = 14f, cellTextWidth: Float = 18f): LcsLayout {
        val cellWidth = maxOf(28f, cellTextWidth + 10f)
        val cellHeight = maxOf(23f, textHeight + 6f)
        val width = maxOf(viewportWidth, textWidth + 36f, 48f + scene.columns * cellWidth)
        val heading = LcsRect(18f, 12f, width - 36f, textHeight)
        val left = (width - scene.columns * cellWidth) / 2f
        val top = heading.bottom + textHeight + 7f
        val cells = (0 until scene.rows).map { i ->
            (0 until scene.columns).map { j -> LcsRect(left + j * cellWidth, top + i * cellHeight, cellWidth, cellHeight) }
        }
        val rowHeaders = (0 until scene.rows).map { LcsRect(left - 25f, top + it * cellHeight, 22f, cellHeight) }
        val columnHeaders = (0 until scene.columns).map { LcsRect(left + it * cellWidth, top - textHeight - 2f, cellWidth, textHeight) }
        val bottom = top + scene.rows * cellHeight
        val summary = (0..1).map { LcsRect(18f, bottom + 8f + it * (textHeight + 3f), width - 36f, textHeight) }
        return LcsLayout(width, summary.last().bottom + 10f, heading, cells, rowHeaders, columnHeaders, summary)
    }
}
