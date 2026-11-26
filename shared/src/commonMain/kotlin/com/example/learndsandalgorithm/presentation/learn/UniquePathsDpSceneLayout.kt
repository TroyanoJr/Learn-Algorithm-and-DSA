package com.example.learndsandalgorithm.presentation.learn

internal data class PathRect(val x: Float, val y: Float, val width: Float, val height: Float) {
    val right get() = x + width
    val bottom get() = y + height
    val centerX get() = x + width / 2f
    val centerY get() = y + height / 2f
}
internal data class UniquePathsLayout(val width: Float, val height: Float, val heading: PathRect,
    val cells: List<List<PathRect>>, val rowHeaders: List<PathRect>, val columnHeaders: List<PathRect>, val formula: PathRect)
internal object UniquePathsDpSceneLayout {
    fun calculate(scene: UniquePathsPresentation, viewportWidth: Float, textWidth: Float = 200f,
                  textHeight: Float = 14f, cellTextWidth: Float = 18f): UniquePathsLayout {
        val cw = maxOf(32f, cellTextWidth + 12f); val ch = maxOf(29f, textHeight + 8f)
        val width = maxOf(viewportWidth, textWidth + 36f, 48f + scene.columns * cw)
        val heading = PathRect(18f, 12f, width - 36f, textHeight)
        val left = (width - scene.columns * cw) / 2f
        val top = heading.bottom + textHeight + 8f
        val cells = (0 until scene.rows).map { r -> (0 until scene.columns).map { c -> PathRect(left + c * cw, top + r * ch, cw, ch) } }
        val rowHeaders = (0 until scene.rows).map { PathRect(left - 25f, top + it * ch, 22f, ch) }
        val columnHeaders = (0 until scene.columns).map { PathRect(left + it * cw, top - textHeight - 2f, cw, textHeight) }
        val formula = PathRect(18f, top + scene.rows * ch + 9f, width - 36f, textHeight)
        return UniquePathsLayout(width, formula.bottom + 10f, heading, cells, rowHeaders, columnHeaders, formula)
    }
}
