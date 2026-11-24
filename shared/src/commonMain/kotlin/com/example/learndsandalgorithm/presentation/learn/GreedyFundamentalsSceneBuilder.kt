package com.example.learndsandalgorithm.presentation.learn

internal data class GreedyFundamentalsSceneFrame(
    val step: GreedyFundamentalsStep,
    val candidateStates: List<FundamentalsCandidateState>,
    val selectedLabel: String,
    val selectedCount: Int,
    val usedBudget: Int,
    val remainingFraction: Float
)

internal enum class FundamentalsCandidateState { PENDING, TESTING, CHOSEN, REJECTED }

internal object GreedyFundamentalsSceneSequenceBuilder {
    fun build(steps: List<GreedyFundamentalsStep>): List<GreedyFundamentalsSceneFrame> {
        require(steps.isNotEmpty())
        val rejectedCandidateIndices = mutableSetOf<Int>()
        return steps.map { step ->
            if (step.status == GreedyFundamentalsStatus.REJECTED) {
                step.currentCandidateIndex?.let(rejectedCandidateIndices::add)
            }
            GreedyFundamentalsSceneFrame(
                step = step,
                candidateStates = step.sortedDistances.indices.map { index ->
                    val chosen = step.chosenDistances.count { it == step.sortedDistances[index] } >
                        step.sortedDistances.take(index).count { it == step.sortedDistances[index] }
                    when {
                        chosen -> FundamentalsCandidateState.CHOSEN
                        index in rejectedCandidateIndices -> FundamentalsCandidateState.REJECTED
                        step.currentCandidateIndex == index -> FundamentalsCandidateState.TESTING
                        else -> FundamentalsCandidateState.PENDING
                    }
                },
                selectedLabel = "Selected Stops: [${step.chosenDistances.joinToString()}]",
                selectedCount = step.chosenDistances.size,
                usedBudget = step.totalBudget - step.remainingBudget,
                remainingFraction = if (step.totalBudget > 0) (step.remainingBudget.toFloat() / step.totalBudget).coerceIn(0f, 1f) else 0f
            )
        }
    }
}
