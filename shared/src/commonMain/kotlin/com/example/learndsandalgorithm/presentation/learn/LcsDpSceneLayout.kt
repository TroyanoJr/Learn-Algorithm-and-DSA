package com.example.learndsandalgorithm.presentation.learn

internal data class LcsRect(val x: Float, val y: Float, val width: Float, val height: Float) {
    val right get() = x + width
    val bottom get() = y + height
    val centerX get() = x + width / 2f
    val centerY get() = y + height / 2f
}
internal data class LcsLayout(val width: Float, val height: Float, val heading: LcsRect,
    val cells: List<List<LcsRect>>, val rowHeaders: List<LcsRect>, val columnHeaders: List<LcsRect>,
    val summary: List<LcsRect>) {
    fun arrow(from: LcsCellId, to: LcsCellId): LcsArrowGeometry {
        val a = cells[from.row][from.column]
        val b = cells[to.row][to.column]
        val down = b.y > a.y; val up = b.y < a.y
        val right = b.x > a.x; val left = b.x < a.x
        val sx = if (right) a.right - 3f else if (left) a.x + 3f else a.right - 3f
        val ex = if (right) b.x + 3f else if (left) b.right - 3f else b.right - 3f
        val sy = if (down) a.bottom - 3f else if (up) a.y + 3f else a.y + 3f
        val ey = if (down) b.y + 3f else if (up) b.bottom - 3f else b.y + 3f
        return LcsArrowGeometry(sx, sy, ex, ey)
    }
}
internal data class LcsArrowGeometry(val startX: Float, val startY: Float, val endX: Float, val endY: Float)
internal object LcsDpSceneLayout {
    fun calculate(scene: LcsPresentation, viewportWidth: Float, textWidth: Float = 200f,
                  textHeight: Float = 14f, cellTextWidth: Float = 18f, headerWidth: Float = 22f): LcsLayout {
        val cellWidth = maxOf(34f, cellTextWidth + 12f)
        val cellHeight = maxOf(25f, textHeight + 8f)
        val gapX = 5f
        val gapY = 3f
        val matrixWidth = scene.columns * cellWidth + (scene.columns - 1).coerceAtLeast(0) * gapX
        val width = maxOf(viewportWidth, textWidth + 36f, 2f * (headerWidth + 12f) + matrixWidth)
        val heading = LcsRect(18f, 8f, width - 36f, textHeight)
        val left = (width - matrixWidth) / 2f
        val top = heading.bottom + textHeight + 3f
        val cells = (0 until scene.rows).map { i ->
            (0 until scene.columns).map { j -> LcsRect(left + j * (cellWidth + gapX), top + i * (cellHeight + gapY), cellWidth, cellHeight) }
        }
        val rowHeaders = (0 until scene.rows).map { LcsRect(left - headerWidth - 3f, top + it * (cellHeight + gapY), headerWidth, cellHeight) }
        val columnHeaders = (0 until scene.columns).map { LcsRect(left + it * (cellWidth + gapX), top - textHeight - 2f, cellWidth, textHeight) }
        val bottom = top + scene.rows * cellHeight + (scene.rows - 1).coerceAtLeast(0) * gapY
        val summary = (0..1).map { LcsRect(18f, bottom + 6f + it * (textHeight + 2f), width - 36f, textHeight) }
        return LcsLayout(width, summary.last().bottom + 6f, heading, cells, rowHeaders, columnHeaders, summary)
    }
}
