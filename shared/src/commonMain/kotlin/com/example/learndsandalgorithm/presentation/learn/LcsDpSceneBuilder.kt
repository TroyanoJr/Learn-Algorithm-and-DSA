package com.example.learndsandalgorithm.presentation.learn

internal data class LcsCellId(val row: Int, val column: Int)
internal data class LcsSceneFrame(val step: LcsDpStep, val computed: Set<LcsCellId>, val dependencies: List<LcsCellId>)
internal data class LcsPresentation(val frames: List<LcsSceneFrame>, val rows: Int, val columns: Int)
internal object LcsDpSceneBuilder {
    fun build(steps: List<LcsDpStep>): LcsPresentation {
        val first = steps.first()
        val rows = first.dpMatrix.size
        val columns = first.dpMatrix.first().size
        val computed = mutableSetOf<LcsCellId>()
        for (i in 0 until rows) computed.add(LcsCellId(i, 0))
        for (j in 0 until columns) computed.add(LcsCellId(0, j))
        val frames = steps.map { step ->
            val i = step.currentI; val j = step.currentJ
            val deps = if (step.action == LcsAction.COMPUTE_CELL && i != null && j != null) {
                computed.add(LcsCellId(i, j))
                if (step.isMatch == true) listOf(LcsCellId(i - 1, j - 1))
                else listOf(LcsCellId(i - 1, j), LcsCellId(i, j - 1))
            } else emptyList()
            LcsSceneFrame(step, computed.toSet(), deps)
        }
        return LcsPresentation(frames, rows, columns)
    }
}
