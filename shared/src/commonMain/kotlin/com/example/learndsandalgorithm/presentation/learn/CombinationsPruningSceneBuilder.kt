package com.example.learndsandalgorithm.presentation.learn

internal data class CombinationCandidate(val id: String, val index: Int, val value: Int)
internal data class CombinationStackOccurrence(val id: String, val candidateIndex: Int, val value: Int)
internal data class CombinationSceneFrame(
    val step: CombinationStep,
    val stack: List<CombinationStackOccurrence>,
    val pushed: CombinationStackOccurrence?,
    val popped: CombinationStackOccurrence?,
    val difference: Int,
    val excessPruningSafe: Boolean
)
internal data class CombinationsPresentation(
    val candidates: List<CombinationCandidate>,
    val frames: List<CombinationSceneFrame>,
    val maximumStackSize: Int,
    val maximumSolutions: Int
)

internal object CombinationsPruningSceneBuilder {
    fun build(steps: List<CombinationStep>): CombinationsPresentation {
        val candidates = steps.firstOrNull()?.candidates.orEmpty().mapIndexed { index, value ->
            CombinationCandidate("candidate-$index", index, value)
        }
        val stack = mutableListOf<CombinationStackOccurrence>()
        var nextId = 0
        val frames = steps.map { step ->
            var pushed: CombinationStackOccurrence? = null
            var popped: CombinationStackOccurrence? = null
            if (step.action == CombinationAction.TRY_CANDIDATE && step.currentCombination.size == stack.size + 1 &&
                step.currentCombination.dropLast(1) == stack.map { it.value } && step.startIndex in candidates.indices) {
                pushed = CombinationStackOccurrence("stack-${nextId++}", step.startIndex, step.currentCombination.last())
                stack.add(pushed)
            } else if (step.action == CombinationAction.BACKTRACK && stack.size == step.currentCombination.size + 1 &&
                stack.dropLast(1).map { it.value } == step.currentCombination) {
                popped = stack.removeAt(stack.lastIndex)
            }
            CombinationSceneFrame(step, stack.toList(), pushed, popped, step.target - step.currentSum,
                step.candidates.all { it >= 0 })
        }
        return CombinationsPresentation(candidates, frames, frames.maxOfOrNull { it.stack.size + if (it.popped != null) 1 else 0 } ?: 0,
            frames.maxOfOrNull { it.step.validCombinations.size } ?: 0)
    }
}
