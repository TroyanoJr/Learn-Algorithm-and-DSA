package com.example.learndsandalgorithm.presentation.learn

internal enum class CombinationAction {
    INIT,
    TRY_CANDIDATE,
    PRUNE,
    SOLUTION_FOUND,
    BACKTRACK,
    FINISHED
}

internal data class CombinationStep(
    val candidates: List<Int> = listOf(2, 3),
    val target: Int = 5,
    val startIndex: Int = 0,
    val currentSum: Int = 0,
    val currentCombination: List<Int> = emptyList(),
    val validCombinations: List<List<Int>> = emptyList(),
    val action: CombinationAction = CombinationAction.INIT,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object CombinationsPruningTrace {

    fun createTrace(
        candidates: List<Int> = listOf(2, 3),
        target: Int = 5
    ): List<CombinationStep> {
        val steps = mutableListOf<CombinationStep>()
        val current = mutableListOf<Int>()
        val solutions = mutableListOf<List<Int>>()

        fun snapshot(
            startIdx: Int,
            sum: Int,
            action: CombinationAction,
            explanation: String,
            badgeText: String,
            isTerminal: Boolean = false
        ) = CombinationStep(
            candidates = candidates,
            target = target,
            startIndex = startIdx,
            currentSum = sum,
            currentCombination = current.toList(),
            validCombinations = solutions.toList(),
            action = action,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        // Step 0: INIT
        steps.add(
            snapshot(
                startIdx = 0,
                sum = 0,
                action = CombinationAction.INIT,
                explanation = "Combination Sum: Finding combinations of $candidates that sum to target $target. Rule: prune immediately when sum > $target.",
                badgeText = "READY · TARGET $target"
            )
        )

        fun findCombinations(startIdx: Int, currentSum: Int) {
            if (currentSum == target) {
                solutions.add(current.toList())
                steps.add(
                    snapshot(
                        startIdx = startIdx,
                        sum = currentSum,
                        action = CombinationAction.SOLUTION_FOUND,
                        explanation = "SOLUTION FOUND! Current sum $currentSum == target $target. Recorded combination ${current.toList()}.",
                        badgeText = "SOLUTION: ${current.toList()} (SUM $target)"
                    )
                )
                return
            }

            if (currentSum > target) {
                steps.add(
                    snapshot(
                        startIdx = startIdx,
                        sum = currentSum,
                        action = CombinationAction.PRUNE,
                        explanation = "PRUNE BRANCH! Current sum $currentSum > target $target. Abandoning this invalid search branch early.",
                        badgeText = "PRUNE: SUM $currentSum > $target"
                    )
                )
                return
            }

            for (i in startIdx until candidates.size) {
                val candidate = candidates[i]
                current.add(candidate)
                val newSum = currentSum + candidate

                steps.add(
                    snapshot(
                        startIdx = i,
                        sum = newSum,
                        action = CombinationAction.TRY_CANDIDATE,
                        explanation = "Add candidate $candidate at index $i. Current combination: ${current.toList()}, sum: $newSum.",
                        badgeText = "TRY +$candidate -> SUM $newSum"
                    )
                )

                findCombinations(i, newSum)

                current.removeAt(current.lastIndex)

                steps.add(
                    snapshot(
                        startIdx = i,
                        sum = currentSum,
                        action = CombinationAction.BACKTRACK,
                        explanation = "UNDO (Backtrack): Pop candidate $candidate. Restore combination to ${current.toList()} and sum to $currentSum.",
                        badgeText = "BACKTRACK -> SUM $currentSum"
                    )
                )
            }
        }

        findCombinations(0, 0)

        // FINISHED
        steps.add(
            snapshot(
                startIdx = 0,
                sum = 0,
                action = CombinationAction.FINISHED,
                explanation = "Combination search complete! Found ${solutions.size} valid combinations: $solutions. Invalid branches were pruned early.",
                badgeText = "COMPLETE: ${solutions.size} COMBINATIONS FOUND",
                isTerminal = true
            )
        )

        return steps
    }
}
