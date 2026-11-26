package com.example.learndsandalgorithm.presentation.learn

internal data class TabulationDependency(val source: Int, val destination: Int) {
    val id: String get() = "dp-$source-to-$destination"
}
internal data class TabulationSceneFrame(val step: DpTabulationStep, val dependencies: List<TabulationDependency>)
internal data class TabulationPresentation(val frames: List<TabulationSceneFrame>, val tableSize: Int)
internal object DpTabulationSceneBuilder {
    fun build(steps: List<DpTabulationStep>): TabulationPresentation {
        val frames = steps.map { step ->
            val i = step.currentIndex
            val edges = if (step.action == TabulationAction.FILL_TABLE && i != null && i >= 2)
                listOf(TabulationDependency(i - 2, i), TabulationDependency(i - 1, i)) else emptyList()
            TabulationSceneFrame(step, edges)
        }
        return TabulationPresentation(frames, steps.maxOfOrNull { it.dpTable.size } ?: 0)
    }
}
