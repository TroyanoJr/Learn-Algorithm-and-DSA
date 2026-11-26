package com.example.learndsandalgorithm.presentation.learn

internal data class PathCellId(val row: Int, val column: Int)
internal data class UniquePathsFrame(val step: UniquePathsDpStep, val computed: Set<PathCellId>,
    val bases: Set<PathCellId>, val dependencies: List<PathCellId>)
internal data class UniquePathsPresentation(val frames: List<UniquePathsFrame>, val rows: Int, val columns: Int)
internal object UniquePathsDpSceneBuilder {
    fun build(steps: List<UniquePathsDpStep>): UniquePathsPresentation {
        val first = steps.first()
        val bases = (0 until first.rows).map { PathCellId(it, 0) }.toSet() +
            (0 until first.cols).map { PathCellId(0, it) }
        val computed = mutableSetOf<PathCellId>()
        val frames = steps.map { step ->
            if (step.action == UniquePathsDpAction.FILL_BASE_CASES) computed.addAll(bases)
            val r = step.currentR; val c = step.currentC
            val deps = if (step.action == UniquePathsDpAction.COMPUTE_CELL && r != null && c != null) {
                computed.add(PathCellId(r, c))
                listOf(PathCellId(r - 1, c), PathCellId(r, c - 1))
            } else emptyList()
            UniquePathsFrame(step, computed.toSet(), if (step.action == UniquePathsDpAction.INIT) emptySet() else bases, deps)
        }
        return UniquePathsPresentation(frames, first.rows, first.cols)
    }
}
