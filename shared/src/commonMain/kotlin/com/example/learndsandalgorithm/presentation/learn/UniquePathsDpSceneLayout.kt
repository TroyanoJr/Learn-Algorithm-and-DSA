package com.example.learndsandalgorithm.presentation.learn

internal data class PathRect(val x: Float, val y: Float, val width: Float, val height: Float) {
    val right get() = x + width
    val bottom get() = y + height
    val centerX get() = x + width / 2f
    val centerY get() = y + height / 2f
}
internal data class UniquePathsLayout(val width: Float, val height: Float, val heading: PathRect,
    val cells: List<List<PathRect>>, val rowHeaders: List<PathRect>, val columnHeaders: List<PathRect>, val formula: PathRect) {
    fun dependencyArrow(source: PathCellId, destination: PathCellId): PathArrowGeometry {
        val from = cells[source.row][source.column]
        val to = cells[destination.row][destination.column]
        val vertical = source.row != destination.row
        return PathArrowGeometry(from.right - 3f, if (vertical) from.bottom - 3f else from.y + 3f,
            if (vertical) to.right - 3f else to.x + 3f, to.y + 3f)
    }
}
internal data class PathArrowGeometry(val startX: Float, val startY: Float, val endX: Float, val endY: Float)
internal object UniquePathsDpSceneLayout {
    fun calculate(scene: UniquePathsPresentation, viewportWidth: Float, textWidth: Float = 200f,
                  textHeight: Float = 14f, cellTextWidth: Float = 18f, headerWidth: Float = 22f): UniquePathsLayout {
        val cw = maxOf(36f, cellTextWidth + 14f); val ch = maxOf(33f, textHeight + 10f)
        val gap = 6f
        val matrixWidth = scene.columns * cw + (scene.columns - 1).coerceAtLeast(0) * gap
        val width = maxOf(viewportWidth, textWidth + 36f, 2f * (headerWidth + 12f) + matrixWidth)
        val heading = PathRect(18f, 12f, width - 36f, textHeight)
        val left = (width - matrixWidth) / 2f
        val top = heading.bottom + textHeight + 8f
        val cells = (0 until scene.rows).map { r -> (0 until scene.columns).map { c -> PathRect(left + c * (cw + gap), top + r * (ch + gap), cw, ch) } }
        val rowHeaders = (0 until scene.rows).map { PathRect(left - headerWidth - 3f, top + it * (ch + gap), headerWidth, ch) }
        val columnHeaders = (0 until scene.columns).map { PathRect(left + it * (cw + gap), top - textHeight - 2f, cw, textHeight) }
        val formula = PathRect(18f, top + scene.rows * ch + (scene.rows - 1).coerceAtLeast(0) * gap + 9f, width - 36f, textHeight)
        return UniquePathsLayout(width, formula.bottom + 10f, heading, cells, rowHeaders, columnHeaders, formula)
    }
}
