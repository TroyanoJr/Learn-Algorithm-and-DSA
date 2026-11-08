package com.example.learndsandalgorithm.presentation.learn

internal enum class GreedyFundamentalsStatus {
    INIT,
    SORTING,
    EVALUATING,
    SELECTED,
    REJECTED,
    FINISHED
}

internal data class GreedyFundamentalsStep(
    val distances: List<Int> = listOf(2, 3, 4, 6),
    val sortedDistances: List<Int> = listOf(2, 3, 4, 6),
    val totalBudget: Int = 10,
    val remainingBudget: Int = 10,
    val chosenDistances: List<Int> = emptyList(),
    val currentCandidateIndex: Int? = null,
    val currentCandidateDistance: Int? = null,
    val status: GreedyFundamentalsStatus = GreedyFundamentalsStatus.INIT,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object GreedyFundamentalsTrace {

    fun createTrace(
        distances: List<Int> = listOf(2, 3, 4, 6),
        budget: Int = 10
    ): List<GreedyFundamentalsStep> {
        val steps = mutableListOf<GreedyFundamentalsStep>()
        val sorted = distances.sorted()
        val chosen = mutableListOf<Int>()
        var remaining = budget

        // Step 0: INIT
        steps.add(
            GreedyFundamentalsStep(
                distances = distances,
                sortedDistances = sorted,
                totalBudget = budget,
                remainingBudget = remaining,
                chosenDistances = chosen.toList(),
                status = GreedyFundamentalsStatus.INIT,
                explanation = "Greedy Fundamentals: Delivery robot wants to visit maximum stops within budget $budget. Stop distances: $distances.",
                badgeText = "READY · BUDGET $budget"
            )
        )

        // Step 1: SORTING
        steps.add(
            GreedyFundamentalsStep(
                distances = distances,
                sortedDistances = sorted,
                totalBudget = budget,
                remainingBudget = remaining,
                chosenDistances = chosen.toList(),
                status = GreedyFundamentalsStatus.SORTING,
                explanation = "Sort stops in ascending order: $sorted. Shortest distance first maximizes available budget for remaining stops.",
                badgeText = "SORTED: ${sorted.joinToString()}"
            )
        )

        for ((idx, dist) in sorted.withIndex()) {
            if (dist <= remaining) {
                steps.add(
                    GreedyFundamentalsStep(
                        distances = distances,
                        sortedDistances = sorted,
                        totalBudget = budget,
                        remainingBudget = remaining,
                        chosenDistances = chosen.toList(),
                        currentCandidateIndex = idx,
                        currentCandidateDistance = dist,
                        status = GreedyFundamentalsStatus.EVALUATING,
                        explanation = "Evaluate stop distance $dist. Distance $dist ≤ remaining budget $remaining (fits).",
                        badgeText = "EVALUATE: DISTANCE $dist ≤ $remaining"
                    )
                )

                chosen.add(dist)
                remaining -= dist

                steps.add(
                    GreedyFundamentalsStep(
                        distances = distances,
                        sortedDistances = sorted,
                        totalBudget = budget,
                        remainingBudget = remaining,
                        chosenDistances = chosen.toList(),
                        currentCandidateIndex = idx,
                        currentCandidateDistance = dist,
                        status = GreedyFundamentalsStatus.SELECTED,
                        explanation = "Select stop distance $dist. Remaining budget updated: ${remaining + dist} - $dist = $remaining.",
                        badgeText = "SELECTED: $dist (REM BUDGET: $remaining)"
                    )
                )
            } else {
                steps.add(
                    GreedyFundamentalsStep(
                        distances = distances,
                        sortedDistances = sorted,
                        totalBudget = budget,
                        remainingBudget = remaining,
                        chosenDistances = chosen.toList(),
                        currentCandidateIndex = idx,
                        currentCandidateDistance = dist,
                        status = GreedyFundamentalsStatus.REJECTED,
                        explanation = "Evaluate stop distance $dist. Distance $dist > remaining budget $remaining (exceeds budget), so stop search.",
                        badgeText = "REJECTED: $dist > $remaining"
                    )
                )
                break
            }
        }

        steps.add(
            GreedyFundamentalsStep(
                distances = distances,
                sortedDistances = sorted,
                totalBudget = budget,
                remainingBudget = remaining,
                chosenDistances = chosen.toList(),
                status = GreedyFundamentalsStatus.FINISHED,
                explanation = "Greedy selection complete! Total stops visited: ${chosen.size} (distances: $chosen). Shortest-first rule is globally optimal for independent costs.",
                badgeText = "COMPLETE: ${chosen.size} STOPS VISITED",
                isTerminal = true
            )
        )

        return steps
    }
}
