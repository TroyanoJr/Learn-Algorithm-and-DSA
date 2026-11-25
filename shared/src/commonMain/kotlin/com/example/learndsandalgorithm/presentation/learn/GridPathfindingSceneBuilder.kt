package com.example.learndsandalgorithm.presentation.learn

internal enum class GridRejection { OUT_OF_BOUNDS, OBSTACLE, REVISITED }
internal data class GridSceneCell(val position: GridCell, val wall: Boolean) {
    val id: String get() = "cell-${position.r}-${position.c}"
}
internal data class GridPathConnection(val from: GridCell, val to: GridCell) {
    val id: String get() = "path-${from.r}-${from.c}-${to.r}-${to.c}"
}
internal data class GridSceneFrame(
    val step: GridPathStep,
    val path: List<GridCell>,
    val connections: List<GridPathConnection>,
    val rejection: GridRejection?,
    val rejectedCell: GridCell?,
    val rejectionOrigin: GridCell?,
    val removedCell: GridCell?,
    val restoredCell: GridCell?,
    val pathCells: Int,
    val moves: Int
)
internal data class GridPathPresentation(val rows: Int, val cols: Int, val cells: List<GridSceneCell>, val frames: List<GridSceneFrame>)

internal object GridPathfindingSceneBuilder {
    fun build(steps: List<GridPathStep>): GridPathPresentation {
        val first = steps.firstOrNull()
        val rows = first?.rows ?: 0
        val cols = first?.cols ?: 0
        val cells = (0 until rows).flatMap { r -> (0 until cols).map { c -> GridSceneCell(GridCell(r, c), first?.grid?.getOrNull(r)?.getOrNull(c) == 0) } }
        val frames = steps.mapIndexed { index, step ->
            val rejected = if (step.action == GridPathAction.HIT_WALL || step.action == GridPathAction.REVISITED) step.currentCell else null
            val reason = rejected?.let { cell ->
                when {
                    cell.r !in 0 until step.rows || cell.c !in 0 until step.cols -> GridRejection.OUT_OF_BOUNDS
                    step.action == GridPathAction.REVISITED -> GridRejection.REVISITED
                    step.grid.getOrNull(cell.r)?.getOrNull(cell.c) == 0 -> GridRejection.OBSTACLE
                    else -> null
                }
            }
            val previous = steps.getOrNull(index - 1)?.currentPath.orEmpty()
            val removed = if (step.action == GridPathAction.BACKTRACK && previous.size == step.currentPath.size + 1 &&
                previous.dropLast(1) == step.currentPath) previous.last() else null
            GridSceneFrame(step, step.currentPath, step.currentPath.zipWithNext(::GridPathConnection), reason, rejected,
                if (reason != null) step.currentPath.lastOrNull() else null,
                removed, if (removed != null) step.currentPath.lastOrNull() else null,
                step.currentPath.size, (step.currentPath.size - 1).coerceAtLeast(0))
        }
        return GridPathPresentation(rows, cols, cells, frames)
    }
}
