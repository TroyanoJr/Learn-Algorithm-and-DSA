package com.example.learndsandalgorithm.presentation.learn

internal enum class ComplexityElementState { PENDING, EVAL, VALID, HALT, SKIP }
internal data class ComplexityElement(val id: String, val index: Int, val value: Int)
internal data class ComplexitySceneFrame(
    val step: BacktrackingComplexityStep,
    val unpruned: List<ComplexityElementState>,
    val pruned: List<ComplexityElementState>,
    val haltIndex: Int?,
    val sortedInput: Boolean,
    val savedCalls: Int?,
    val savingsPercent: Double?
)
internal data class ComplexityPresentation(val elements: List<ComplexityElement>, val frames: List<ComplexitySceneFrame>)

internal object BacktrackingComplexitySceneBuilder {
    fun build(steps: List<BacktrackingComplexityStep>): ComplexityPresentation {
        val elements = steps.firstOrNull()?.array.orEmpty().mapIndexed { index, value -> ComplexityElement("element-$index", index, value) }
        val unpruned = MutableList(elements.size) { ComplexityElementState.PENDING }
        val pruned = MutableList(elements.size) { ComplexityElementState.PENDING }
        var halt: Int? = null
        val frames = steps.map { step ->
            val index = step.currentIndex
            if (index != null && index in elements.indices) {
                when (step.action) {
                    ComplexityAction.EVALUATE_UNPRUNED -> unpruned[index] = ComplexityElementState.EVAL
                    ComplexityAction.EVALUATE_PRUNED -> pruned[index] = ComplexityElementState.VALID
                    ComplexityAction.PRUNED_HALT -> {
                        pruned[index] = ComplexityElementState.HALT
                        halt = index
                        for (i in index + 1 until elements.size) pruned[i] = ComplexityElementState.SKIP
                    }
                    else -> Unit
                }
            }
            val saved = if (step.action == ComplexityAction.FINISHED) step.unprunedCalls - step.prunedCalls else null
            val percentage = saved?.let { if (step.unprunedCalls == 0) 0.0 else it * 100.0 / step.unprunedCalls }
            ComplexitySceneFrame(step, unpruned.toList(), pruned.toList(), halt,
                step.array.zipWithNext().all { (a, b) -> a <= b }, saved, percentage)
        }
        return ComplexityPresentation(elements, frames)
    }
}
