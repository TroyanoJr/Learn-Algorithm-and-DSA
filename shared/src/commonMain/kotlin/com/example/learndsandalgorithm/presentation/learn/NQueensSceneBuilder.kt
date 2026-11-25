package com.example.learndsandalgorithm.presentation.learn

import kotlin.math.abs

internal data class QueenCell(val row: Int, val column: Int) {
    val id: String get() = "cell-$row-$column"
}
internal enum class QueenConflictKind { COLUMN, DIAGONAL }
internal data class QueenConflict(val from: QueenCell, val to: QueenCell, val kind: QueenConflictKind) {
    val id: String get() = "${from.id}->${to.id}:${kind.name}"
}
internal data class NQueensSceneFrame(
    val step: NQueensStep,
    val placed: List<QueenCell>,
    val testing: QueenCell?,
    val removed: QueenCell?,
    val conflicts: List<QueenConflict>
)
internal data class NQueensPresentation(val size: Int, val cells: List<QueenCell>, val frames: List<NQueensSceneFrame>)

internal object NQueensSceneBuilder {
    fun build(steps: List<NQueensStep>): NQueensPresentation {
        val size = steps.firstOrNull()?.boardSize ?: 0
        val cells = (0 until size).flatMap { row -> (0 until size).map { column -> QueenCell(row, column) } }
        val frames = steps.mapIndexed { index, step ->
            val placed = step.queenColumns.mapIndexedNotNull { row, column -> column?.let { QueenCell(row, it) } }
            val testing = if (step.action == NQueensAction.TRY_POSITION || step.action == NQueensAction.CONFLICT_PRUNE)
                step.testingCol?.let { QueenCell(step.currentRow, it) } else null
            val previous = steps.getOrNull(index - 1)?.queenColumns.orEmpty()
            val removed = if (step.action == NQueensAction.BACKTRACK) previous.mapIndexedNotNull { row, column ->
                if (column != null && step.queenColumns.getOrNull(row) == null) QueenCell(row, column) else null
            }.singleOrNull() else null
            val conflicts = if (testing == null) emptyList() else placed.mapNotNull { queen ->
                val kind = when {
                    queen.column == testing.column -> QueenConflictKind.COLUMN
                    abs(queen.row - testing.row) == abs(queen.column - testing.column) -> QueenConflictKind.DIAGONAL
                    else -> null
                }
                kind?.let { QueenConflict(queen, testing, it) }
            }
            NQueensSceneFrame(step, placed, testing, removed, conflicts)
        }
        return NQueensPresentation(size, cells, frames)
    }
}
