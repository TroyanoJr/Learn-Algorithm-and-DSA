package com.example.learndsandalgorithm.presentation.learn

internal data class QueensRect(val x: Float, val y: Float, val width: Float, val height: Float) {
    val right get() = x + width
    val bottom get() = y + height
    val centerX get() = x + width / 2f
    val centerY get() = y + height / 2f
}
internal data class QueensLayoutMetrics(
    val headingWidth: Float = 140f, val headingHeight: Float = 12f,
    val indexWidth: Float = 10f, val indexHeight: Float = 12f,
    val symbolWidth: Float = 22f, val symbolHeight: Float = 22f,
    val solutionsHeadingWidth: Float = 65f, val countWidth: Float = 45f, val textHeight: Float = 14f,
    val solutionWidth: Float = 105f, val solutionHeight: Float = 14f
)
internal data class NQueensSceneLayoutResult(
    val width: Float, val height: Float, val lateral: Boolean, val boardRegion: QueensRect,
    val boardHeading: QueensRect, val columns: List<QueensRect>, val rows: List<QueensRect>,
    val cells: Map<QueenCell, QueensRect>, val solutionRegion: QueensRect,
    val solutionHeading: QueensRect, val solutionCount: QueensRect, val solutions: List<QueensRect>, val empty: QueensRect
)
internal object NQueensSceneLayout {
    fun calculate(presentation: NQueensPresentation, viewportWidth: Float, viewportHeight: Float,
                  m: QueensLayoutMetrics = QueensLayoutMetrics()): NQueensSceneLayoutResult {
        val n = presentation.size
        val cell = maxOf(42f, m.symbolWidth + 12f, m.symbolHeight + 12f, m.indexWidth + 8f, m.indexHeight + 8f)
        val boardSize = n * cell + (n - 1).coerceAtLeast(0) * 4f
        val leftMargin = maxOf(14f, m.indexWidth + 6f)
        val boardWidth = maxOf(boardSize + leftMargin + 16f, m.headingWidth + 16f)
        val boardHeight = 8f + m.headingHeight + 4f + m.indexHeight + 4f + boardSize + 8f
        val solutionCount = presentation.frames.maxOfOrNull { it.step.solutions.size } ?: 0
        val solutionWidth = maxOf(m.solutionsHeadingWidth + m.countWidth + 24f, m.solutionWidth + 16f)
        val solutionHeight = 8f + m.textHeight + 4f + solutionCount.coerceAtLeast(1) * m.solutionHeight +
            (solutionCount - 1).coerceAtLeast(0) * 3f + 8f
        val verticalWidth = maxOf(viewportWidth, boardWidth + 16f, solutionWidth + 16f)
        val verticalHeight = 8f + boardHeight + 6f + solutionHeight + 8f
        val lateralWidth = maxOf(viewportWidth, boardWidth + solutionWidth + 22f)
        val lateralHeight = 16f + maxOf(boardHeight, solutionHeight)
        val verticalScale = minOf(viewportWidth / verticalWidth, viewportHeight / verticalHeight, 1f)
        val lateralScale = minOf(viewportWidth / lateralWidth, viewportHeight / lateralHeight, 1f)
        val lateral = lateralScale > verticalScale
        val width = if (lateral) lateralWidth else verticalWidth
        val boardRegion = QueensRect(8f, 8f, if (lateral) boardWidth else width - 16f, boardHeight)
        val boardHeading = QueensRect(boardRegion.x + 8f, boardRegion.y + 8f, boardRegion.width - 16f, m.headingHeight)
        val boardLeft = boardRegion.x + leftMargin + (boardRegion.width - leftMargin - 8f - boardSize) / 2f
        val boardTop = boardHeading.bottom + 4f + m.indexHeight + 4f
        val columns = (0 until n).map { col -> QueensRect(boardLeft + col * (cell + 4f), boardHeading.bottom + 4f, cell, m.indexHeight) }
        val rows = (0 until n).map { row -> QueensRect(boardLeft - leftMargin, boardTop + row * (cell + 4f), leftMargin - 4f, cell) }
        val cells = presentation.cells.associateWith { pos -> QueensRect(boardLeft + pos.column * (cell + 4f), boardTop + pos.row * (cell + 4f), cell, cell) }
        val solutionRegion = if (lateral) QueensRect(boardRegion.right + 6f, 8f, width - boardRegion.right - 14f, solutionHeight)
            else QueensRect(8f, boardRegion.bottom + 6f, width - 16f, solutionHeight)
        val solutionHeading = QueensRect(solutionRegion.x + 8f, solutionRegion.y + 8f, m.solutionsHeadingWidth, m.textHeight)
        val count = QueensRect(solutionRegion.right - 8f - m.countWidth, solutionHeading.y, m.countWidth, m.textHeight)
        val top = solutionHeading.bottom + 4f
        val solutions = (0 until solutionCount).map { i -> QueensRect(solutionRegion.x + 8f, top + i * (m.solutionHeight + 3f), solutionRegion.width - 16f, m.solutionHeight) }
        val empty = QueensRect(solutionRegion.x + 8f, top, solutionRegion.width - 16f, m.solutionHeight)
        return NQueensSceneLayoutResult(width, if (lateral) lateralHeight else verticalHeight, lateral, boardRegion, boardHeading,
            columns, rows, cells, solutionRegion, solutionHeading, count, solutions, empty)
    }
}
