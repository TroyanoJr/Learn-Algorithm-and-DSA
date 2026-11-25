package com.example.learndsandalgorithm.presentation.learn

internal data class GridSceneRect(val x: Float, val y: Float, val width: Float, val height: Float) {
    val right get() = x + width
    val bottom get() = y + height
    val centerX get() = x + width / 2f
    val centerY get() = y + height / 2f
}
internal data class GridLayoutMetrics(
    val headingWidth: Float = 140f, val headingHeight: Float = 12f,
    val indexWidth: Float = 10f, val indexHeight: Float = 12f,
    val symbolWidth: Float = 22f, val symbolHeight: Float = 22f,
    val explanationHeadingHeight: Float = 12f, val explanationHeight: Float = 80f,
    val pathHeight: Float = 42f, val counterHeight: Float = 14f, val operationHeight: Float = 14f,
    val panelWidth: Float = 250f
)
internal data class GridPathSceneLayoutResult(
    val width: Float, val height: Float, val lateral: Boolean,
    val boardRegion: GridSceneRect, val heading: GridSceneRect, val rows: List<GridSceneRect>, val columns: List<GridSceneRect>,
    val cells: Map<GridCell, GridSceneRect>, val outside: Map<GridCell, GridSceneRect>,
    val explanationRegion: GridSceneRect, val explanationHeading: GridSceneRect, val explanation: GridSceneRect,
    val pathRegion: GridSceneRect, val path: GridSceneRect, val counter: GridSceneRect, val operation: GridSceneRect
)
internal object GridPathfindingSceneLayout {
    fun choose(presentation: GridPathPresentation, viewportWidth: Float, viewportHeight: Float,
               choices: List<GridLayoutMetrics>): GridPathSceneLayoutResult =
        choices.map { calculate(presentation, viewportWidth, viewportHeight, it) }
            .maxBy { minOf(viewportWidth / it.width, viewportHeight / it.height, 1f) }

    fun calculate(presentation: GridPathPresentation, viewportWidth: Float, viewportHeight: Float,
                  m: GridLayoutMetrics = GridLayoutMetrics()): GridPathSceneLayoutResult {
        val cell = maxOf(46f, m.symbolWidth + 12f, m.symbolHeight + 12f, m.indexWidth + 8f, m.indexHeight + 8f)
        val boardWidth = presentation.cols * cell + (presentation.cols - 1).coerceAtLeast(0) * 4f
        val boardHeight = presentation.rows * cell + (presentation.rows - 1).coerceAtLeast(0) * 4f
        val marker = maxOf(24f, m.symbolWidth + 4f, m.symbolHeight + 4f)
        val edge = marker + 4f
        val rowLabels = m.indexWidth + 6f
        val regionWidth = maxOf(boardWidth + 2f * edge + rowLabels + 16f, m.headingWidth + 16f)
        val regionHeight = 8f + m.headingHeight + 4f + m.indexHeight + 4f + boardHeight + 2f * edge + 8f
        val panelWidth = m.panelWidth.coerceAtLeast(1f) + 16f
        val explanationHeight = 8f + m.explanationHeadingHeight + 3f + m.explanationHeight + 8f
        val pathHeight = 8f + m.pathHeight + 3f + m.counterHeight + 3f + m.operationHeight + 8f
        val verticalWidth = maxOf(viewportWidth, regionWidth + 16f, panelWidth + 16f)
        val verticalHeight = 8f + regionHeight + 6f + explanationHeight + 6f + pathHeight + 8f
        val lateralWidth = maxOf(viewportWidth, regionWidth + panelWidth + 22f)
        val lateralHeight = 16f + maxOf(regionHeight, explanationHeight + 6f + pathHeight)
        val lateral = minOf(viewportWidth / lateralWidth, viewportHeight / lateralHeight, 1f) >
            minOf(viewportWidth / verticalWidth, viewportHeight / verticalHeight, 1f)
        var width = if (lateral) lateralWidth else verticalWidth
        // Avoid a Float rounding overshoot being interpreted as pannable overflow.
        repeat(4) {
            if (viewportWidth > 0f && width > viewportWidth && width * (viewportWidth / width) > viewportWidth) width += 1f
        }
        val board = GridSceneRect(8f, 8f, if (lateral) regionWidth else width - 16f, regionHeight)
        val heading = GridSceneRect(board.x + 8f, board.y + 8f, board.width - 16f, m.headingHeight)
        val boardLeft = board.x + rowLabels + (board.width - rowLabels - boardWidth) / 2f
        val boardTop = heading.bottom + 4f + m.indexHeight + 4f + edge
        val rows = (0 until presentation.rows).map { r -> GridSceneRect(boardLeft - edge - rowLabels, boardTop + r * (cell + 4f), rowLabels - 4f, cell) }
        val columns = (0 until presentation.cols).map { c -> GridSceneRect(boardLeft + c * (cell + 4f), heading.bottom + 4f, cell, m.indexHeight) }
        val cells = presentation.cells.associate { item -> item.position to GridSceneRect(boardLeft + item.position.c * (cell + 4f),
            boardTop + item.position.r * (cell + 4f), cell, cell) }
        val externalCells = presentation.frames.mapNotNull { frame ->
            frame.rejectedCell?.takeIf { frame.rejection == GridRejection.OUT_OF_BOUNDS }
        }.distinct()
        val outside = externalCells.associateWith { pos ->
            val x = when { pos.c < 0 -> boardLeft - edge; pos.c >= presentation.cols -> boardLeft + boardWidth + 4f; else -> boardLeft + pos.c * (cell + 4f) + (cell - marker) / 2f }
            val y = when { pos.r < 0 -> boardTop - edge; pos.r >= presentation.rows -> boardTop + boardHeight + 4f; else -> boardTop + pos.r * (cell + 4f) + (cell - marker) / 2f }
            GridSceneRect(x, y, marker, marker)
        }
        val panelX = if (lateral) board.right + 6f else 8f
        val panelY = if (lateral) 8f else board.bottom + 6f
        val panelW = if (lateral) width - panelX - 8f else width - 16f
        val explanationRegion = GridSceneRect(panelX, panelY, panelW, explanationHeight)
        val explanationHeading = GridSceneRect(panelX + 8f, panelY + 8f, panelW - 16f, m.explanationHeadingHeight)
        val explanation = GridSceneRect(panelX + 8f, explanationHeading.bottom + 3f, panelW - 16f, m.explanationHeight)
        val pathRegion = GridSceneRect(panelX, explanationRegion.bottom + 6f, panelW, pathHeight)
        val path = GridSceneRect(panelX + 8f, pathRegion.y + 8f, panelW - 16f, m.pathHeight)
        val counter = GridSceneRect(path.x, path.bottom + 3f, path.width, m.counterHeight)
        val operation = GridSceneRect(path.x, counter.bottom + 3f, path.width, m.operationHeight)
        return GridPathSceneLayoutResult(width, if (lateral) lateralHeight else verticalHeight, lateral, board, heading, rows, columns,
            cells, outside, explanationRegion, explanationHeading, explanation, pathRegion, path, counter, operation)
    }
}
