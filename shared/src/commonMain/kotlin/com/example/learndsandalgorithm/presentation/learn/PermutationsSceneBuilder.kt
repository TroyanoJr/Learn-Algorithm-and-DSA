package com.example.learndsandalgorithm.presentation.learn

internal data class PermutationOccurrence(val id: String, val value: Int)
internal data class PermutationResult(val id: String, val values: List<Int>)
internal data class PermutationsSceneFrame(
    val step: PermutationStep,
    val working: List<PermutationOccurrence>,
    val results: List<PermutationResult>,
    val swap: Pair<Int, Int>?,
    val undo: Boolean
)
internal data class PermutationsPresentation(
    val frames: List<PermutationsSceneFrame>,
    val maximumResultCount: Int,
    val maximumArraySize: Int
)

internal object PermutationsSceneBuilder {
    fun build(steps: List<PermutationStep>): PermutationsPresentation {
        val initial = steps.firstOrNull()?.currentPermutation.orEmpty()
        val occurrences = initial.mapIndexed { index, value -> PermutationOccurrence("item-$index", value) }.toMutableList()
        val frames = steps.map { step ->
            val isSwap = step.action == PermutationAction.SWAP_CHOICE || step.action == PermutationAction.BACKTRACK_SWAP
            val i = step.swapI
            val j = step.swapJ
            val swap = if (isSwap && i != null && j != null && i in occurrences.indices && j in occurrences.indices) i to j else null
            if (swap != null) {
                val temp = occurrences[swap.first]
                occurrences[swap.first] = occurrences[swap.second]
                occurrences[swap.second] = temp
            }
            PermutationsSceneFrame(
                step = step,
                working = occurrences.toList(),
                results = step.completedPermutations.mapIndexed { index, values -> PermutationResult("result-$index", values) },
                swap = swap,
                undo = step.action == PermutationAction.BACKTRACK_SWAP
            )
        }
        return PermutationsPresentation(frames, frames.maxOfOrNull { it.results.size } ?: 0, initial.size)
    }
}
