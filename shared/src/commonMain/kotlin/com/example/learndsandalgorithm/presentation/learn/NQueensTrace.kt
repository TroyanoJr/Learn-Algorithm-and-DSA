package com.example.learndsandalgorithm.presentation.learn

internal enum class NQueensAction {
    INIT,
    TRY_POSITION,
    PLACE_QUEEN,
    CONFLICT_PRUNE,
    BACKTRACK,
    SOLUTION_FOUND,
    FINISHED
}

internal data class NQueensStep(
    val boardSize: Int = 4,
    val currentRow: Int = 0,
    val testingCol: Int? = null,
    val queenColumns: List<Int?> = listOf(null, null, null, null),
    val colsOccupied: List<Boolean> = List(4) { false },
    val diag1Occupied: List<Boolean> = List(8) { false },
    val diag2Occupied: List<Boolean> = List(8) { false },
    val solutions: List<List<Int>> = emptyList(),
    val action: NQueensAction = NQueensAction.INIT,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object NQueensTrace {

    fun createTrace(n: Int = 4): List<NQueensStep> {
        val steps = mutableListOf<NQueensStep>()
        val queens = MutableList<Int?>(n) { null }
        val cols = BooleanArray(n)
        val diag1 = BooleanArray(2 * n)
        val diag2 = BooleanArray(2 * n)
        val solutions = mutableListOf<List<Int>>()

        fun snapshot(
            row: Int,
            col: Int?,
            action: NQueensAction,
            explanation: String,
            badgeText: String,
            isTerminal: Boolean = false
        ) = NQueensStep(
            boardSize = n,
            currentRow = row,
            testingCol = col,
            queenColumns = queens.toList(),
            colsOccupied = cols.toList(),
            diag1Occupied = diag1.toList(),
            diag2Occupied = diag2.toList(),
            solutions = solutions.toList(),
            action = action,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        // Step 0: INIT
        steps.add(
            snapshot(
                row = 0,
                col = null,
                action = NQueensAction.INIT,
                explanation = "N-Queens Constraint Satisfaction: Place $n queens on a $n x $n board such that no two queens share the same row, column, or diagonal.",
                badgeText = "READY · $n-QUEENS BOARD"
            )
        )

        fun solve(row: Int) {
            if (row == n) {
                val sol = queens.filterNotNull()
                solutions.add(sol)
                steps.add(
                    snapshot(
                        row = row,
                        col = null,
                        action = NQueensAction.SOLUTION_FOUND,
                        explanation = "VALID SOLUTION FOUND! Placed all $n queens safely at columns $sol.",
                        badgeText = "SOLUTION FOUND: $sol"
                    )
                )
                return
            }

            for (col in 0 until n) {
                val d1Idx = row - col + n
                val d2Idx = row + col

                steps.add(
                    snapshot(
                        row = row,
                        col = col,
                        action = NQueensAction.TRY_POSITION,
                        explanation = "Row $row: Test column $col for queen placement. Checking col $col, main diag $d1Idx, anti-diag $d2Idx.",
                        badgeText = "TRY (Row $row, Col $col)"
                    )
                )

                val isSafe = !cols[col] && !diag1[d1Idx] && !diag2[d2Idx]

                if (isSafe) {
                    queens[row] = col
                    cols[col] = true
                    diag1[d1Idx] = true
                    diag2[d2Idx] = true

                    steps.add(
                        snapshot(
                            row = row,
                            col = col,
                            action = NQueensAction.PLACE_QUEEN,
                            explanation = "SAFE! Placed queen at (Row $row, Col $col). Occupied col $col, main diag $d1Idx, anti-diag $d2Idx. Recurse to row ${row + 1}.",
                            badgeText = "PLACE QUEEN AT ($row, $col)"
                        )
                    )

                    solve(row + 1)

                    // Backtrack
                    queens[row] = null
                    cols[col] = false
                    diag1[d1Idx] = false
                    diag2[d2Idx] = false

                    steps.add(
                        snapshot(
                            row = row,
                            col = col,
                            action = NQueensAction.BACKTRACK,
                            explanation = "BACKTRACK: Remove queen from (Row $row, Col $col). Unmark col $col and diagonals to try next column.",
                            badgeText = "BACKTRACK FROM ($row, $col)"
                        )
                    )
                } else {
                    steps.add(
                        snapshot(
                            row = row,
                            col = col,
                            action = NQueensAction.CONFLICT_PRUNE,
                            explanation = "CONFLICT AT (Row $row, Col $col)! Position is under attack by another queen on column or diagonal. PRUNE.",
                            badgeText = "CONFLICT AT ($row, $col)"
                        )
                    )
                }
            }
        }

        solve(0)

        // FINISHED
        steps.add(
            snapshot(
                row = n,
                col = null,
                action = NQueensAction.FINISHED,
                explanation = "N-Queens complete! Found total of ${solutions.size} valid solutions for $n-Queens board: $solutions.",
                badgeText = "COMPLETE: ${solutions.size} SOLUTIONS FOUND",
                isTerminal = true
            )
        )

        return steps
    }
}
