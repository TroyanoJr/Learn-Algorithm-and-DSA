package com.example.learndsandalgorithm.presentation.learn

internal data class GridCell(val r: Int, val c: Int)

internal enum class GridPathAction {
    INIT,
    MOVE,
    HIT_WALL,
    REVISITED,
    TARGET_REACHED,
    BACKTRACK,
    FINISHED
}

internal data class GridPathStep(
    val rows: Int = 3,
    val cols: Int = 3,
    val grid: List<List<Int>> = listOf(
        listOf(1, 1, 0),
        listOf(0, 1, 1),
        listOf(0, 0, 1)
    ),
    val currentCell: GridCell? = GridCell(0, 0),
    val targetCell: GridCell = GridCell(2, 2),
    val currentPath: List<GridCell> = emptyList(),
    val visitedCells: List<GridCell> = emptyList(),
    val action: GridPathAction = GridPathAction.INIT,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object GridPathfindingTrace {

    val defaultGrid = listOf(
        listOf(1, 1, 0),
        listOf(0, 1, 1),
        listOf(0, 0, 1)
    )

    fun createTrace(
        grid: List<List<Int>> = defaultGrid,
        target: GridCell = GridCell(2, 2)
    ): List<GridPathStep> {
        val steps = mutableListOf<GridPathStep>()
        val rows = grid.size
        val cols = grid[0].size
        val visited = mutableSetOf<GridCell>()
        val path = mutableListOf<GridCell>()
        var foundTarget = false

        fun snapshot(
            cell: GridCell?,
            action: GridPathAction,
            explanation: String,
            badgeText: String,
            isTerminal: Boolean = false
        ) = GridPathStep(
            rows = rows,
            cols = cols,
            grid = grid,
            currentCell = cell,
            targetCell = target,
            currentPath = path.toList(),
            visitedCells = visited.toList(),
            action = action,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        // Step 0: INIT
        steps.add(
            snapshot(
                cell = GridCell(0, 0),
                action = GridPathAction.INIT,
                explanation = "Grid Pathfinding: Finding path from (0,0) to target (${target.r},${target.c}) in 2D matrix using 4-directional search. O(4^(R×C)) is a broad upper bound assuming 4 choices per step; actual search depends on grid dimensions, walls, boundaries, and visited restrictions (cells visited at most once per path).",
                badgeText = "READY · TARGET (${target.r},${target.c})"
            )
        )

        fun dfs(r: Int, c: Int): Boolean {
            val cell = GridCell(r, c)

            if (r !in 0 until rows || c !in 0 until cols || grid[r][c] == 0) {
                steps.add(
                    snapshot(
                        cell = cell,
                        action = GridPathAction.HIT_WALL,
                        explanation = "Cannot move to ($r, $c): Out of bounds or obstacle wall. BACKTRACK.",
                        badgeText = "HIT WALL AT ($r, $c)"
                    )
                )
                return false
            }

            if (visited.contains(cell)) {
                steps.add(
                    snapshot(
                        cell = cell,
                        action = GridPathAction.REVISITED,
                        explanation = "Cannot move to ($r, $c): Already visited in current path. PRUNE to avoid cycles.",
                        badgeText = "REVISITED ($r, $c) · PRUNE"
                    )
                )
                return false
            }

            path.add(cell)
            visited.add(cell)

            steps.add(
                snapshot(
                    cell = cell,
                    action = GridPathAction.MOVE,
                    explanation = "Move to cell ($r, $c). Marked as visited. Current path length: ${path.size}.",
                    badgeText = "MOVE TO ($r, $c)"
                )
            )

            if (r == target.r && c == target.c) {
                foundTarget = true
                steps.add(
                    snapshot(
                        cell = cell,
                        action = GridPathAction.TARGET_REACHED,
                        explanation = "TARGET REACHED! Found valid path: ${path.map { "(${it.r},${it.c})" }}.",
                        badgeText = "TARGET REACHED AT (${target.r},${target.c})"
                    )
                )
                return true
            }

            // 4 directions: Down, Right, Up, Left
            val directions = listOf(Pair(1, 0), Pair(0, 1), Pair(-1, 0), Pair(0, -1))
            for ((dr, dc) in directions) {
                if (dfs(r + dr, c + dc)) {
                    return true
                }
            }

            // Backtrack
            path.removeAt(path.lastIndex)
            visited.remove(cell)

            steps.add(
                snapshot(
                    cell = cell,
                    action = GridPathAction.BACKTRACK,
                    explanation = "UNDO (Backtrack): No path to target from ($r, $c). Unmark cell ($r, $c) and backtrack.",
                    badgeText = "BACKTRACK FROM ($r, $c)"
                )
            )

            return false
        }

        dfs(0, 0)

        // FINISHED
        steps.add(
            snapshot(
                cell = if (foundTarget) target else null,
                action = GridPathAction.FINISHED,
                explanation = if (foundTarget) "Pathfinding complete! Target (${target.r},${target.c}) reached successfully with path ${path.map { "(${it.r},${it.c})" }}. Note: O(4^(R×C)) represents a broad upper bound for simple path exploration, not an exact runtime for every grid." else "Pathfinding complete! No valid path exists.",
                badgeText = if (foundTarget) "COMPLETE: PATH FOUND" else "COMPLETE: NO PATH",
                isTerminal = true
            )
        )

        return steps
    }
}
