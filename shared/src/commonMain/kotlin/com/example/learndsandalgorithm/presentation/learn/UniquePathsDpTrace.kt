package com.example.learndsandalgorithm.presentation.learn

internal enum class UniquePathsDpAction {
    INIT,
    FILL_BASE_CASES,
    COMPUTE_CELL,
    FINISHED
}

internal data class UniquePathsDpStep(
    val rows: Int = 3,
    val cols: Int = 3,
    val dpGrid: List<List<Long>> = List(3) { List(3) { 0L } },
    val currentR: Int? = null,
    val currentC: Int? = null,
    val topVal: Long? = null,
    val leftVal: Long? = null,
    val action: UniquePathsDpAction = UniquePathsDpAction.INIT,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object UniquePathsDpTrace {

    fun createTrace(rows: Int = 3, cols: Int = 3): List<UniquePathsDpStep> {
        val steps = mutableListOf<UniquePathsDpStep>()
        val grid = List(rows) { MutableList(cols) { 0L } }

        fun snapshot(
            r: Int?,
            c: Int?,
            top: Long?,
            left: Long?,
            action: UniquePathsDpAction,
            explanation: String,
            badgeText: String,
            isTerminal: Boolean = false
        ) = UniquePathsDpStep(
            rows = rows,
            cols = cols,
            dpGrid = grid.map { it.toList() },
            currentR = r,
            currentC = c,
            topVal = top,
            leftVal = left,
            action = action,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        // Step 0: INIT
        steps.add(
            snapshot(
                r = null,
                c = null,
                top = null,
                left = null,
                action = UniquePathsDpAction.INIT,
                explanation = "2D Grid DP: Finding unique paths from (0,0) to (${rows - 1},${cols - 1}) in a $rows x $cols grid. Transition: dp[r][c] = top + left.",
                badgeText = "READY · $rows x $cols GRID DP"
            )
        )

        // Step 1: Base cases
        for (r in 0 until rows) grid[r][0] = 1L
        for (c in 0 until cols) grid[0][c] = 1L

        steps.add(
            snapshot(
                r = 0,
                c = 0,
                top = null,
                left = null,
                action = UniquePathsDpAction.FILL_BASE_CASES,
                explanation = "Base cases: Set first row dp[0][c] = 1 and first column dp[r][0] = 1 (strictly right or strictly down movement).",
                badgeText = "BASE CASES: FIRST ROW & COL = 1"
            )
        )

        for (r in 1 until rows) {
            for (c in 1 until cols) {
                val top = grid[r - 1][c]
                val left = grid[r][c - 1]
                val ans = top + left
                grid[r][c] = ans

                steps.add(
                    snapshot(
                        r = r,
                        c = c,
                        top = top,
                        left = left,
                        action = UniquePathsDpAction.COMPUTE_CELL,
                        explanation = "Cell ($r, $c): dp[$r][$c] = top ($top) + left ($left) = $ans unique paths.",
                        badgeText = "dp[$r][$c] = $top + $left = $ans"
                    )
                )
            }
        }

        val finalAns = grid[rows - 1][cols - 1]

        // FINISHED
        steps.add(
            snapshot(
                r = rows - 1,
                c = cols - 1,
                top = null,
                left = null,
                action = UniquePathsDpAction.FINISHED,
                explanation = "Grid DP complete! Total unique paths from (0,0) to (${rows - 1},${cols - 1}) = $finalAns. Time: O(R * C), Space optimized to O(C).",
                badgeText = "COMPLETE: $finalAns UNIQUE PATHS",
                isTerminal = true
            )
        )

        return steps
    }
}
