package com.example.learndsandalgorithm.presentation.learn

internal enum class ComplexityAction {
    INIT,
    EVALUATE_UNPRUNED,
    EVALUATE_PRUNED,
    PRUNED_HALT,
    FINISHED
}

internal data class BacktrackingComplexityStep(
    val array: List<Int> = listOf(1, 2, 5, 8),
    val limit: Int = 2,
    val currentIndex: Int? = null,
    val currentValue: Int? = null,
    val unprunedCalls: Int = 0,
    val prunedCalls: Int = 0,
    val action: ComplexityAction = ComplexityAction.INIT,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object BacktrackingComplexityTrace {

    fun createTrace(
        array: List<Int> = listOf(1, 2, 5, 8),
        limit: Int = 2
    ): List<BacktrackingComplexityStep> {
        val steps = mutableListOf<BacktrackingComplexityStep>()

        // Step 0: INIT
        steps.add(
            BacktrackingComplexityStep(
                array = array,
                limit = limit,
                unprunedCalls = 0,
                prunedCalls = 0,
                action = ComplexityAction.INIT,
                explanation = "Backtracking Complexity & Trade-offs: Comparing unpruned Brute Force vs pruned Backtracking on array $array for values ≤ $limit. This comparison illustrates pruning on this particular input instance, not a universal performance guarantee.",
                badgeText = "READY · ARRAY $array (LIMIT $limit)"
            )
        )

        // Phase 1: Unpruned Brute Force Search (Evaluates all elements)
        var unprunedCount = 0
        for ((idx, valNum) in array.withIndex()) {
            unprunedCount++
            steps.add(
                BacktrackingComplexityStep(
                    array = array,
                    limit = limit,
                    currentIndex = idx,
                    currentValue = valNum,
                    unprunedCalls = unprunedCount,
                    prunedCalls = 0,
                    action = ComplexityAction.EVALUATE_UNPRUNED,
                    explanation = "Unpruned (Brute Force) Step ${idx + 1}: Evaluates element $valNum at index $idx unconditionally. Total unpruned calls: $unprunedCount.",
                    badgeText = "UNPRUNED STEP ${idx + 1}: EVAL $valNum"
                )
            )
        }

        // Phase 2: Pruned Backtracking Search (Halts when val > limit)
        var prunedCount = 0
        for ((idx, valNum) in array.withIndex()) {
            prunedCount++
            if (valNum > limit) {
                steps.add(
                    BacktrackingComplexityStep(
                        array = array,
                        limit = limit,
                        currentIndex = idx,
                        currentValue = valNum,
                        unprunedCalls = unprunedCount,
                        prunedCalls = prunedCount,
                        action = ComplexityAction.PRUNED_HALT,
                        explanation = "Pruned (Backtracking) Step ${idx + 1}: Element $valNum at index $idx exceeds limit $limit ($valNum > $limit). PRUNE & HALT RECURSION IMMEDIATELY!",
                        badgeText = "PRUNED HALT AT index $idx ($valNum > $limit)"
                    )
                )
                break
            } else {
                steps.add(
                    BacktrackingComplexityStep(
                        array = array,
                        limit = limit,
                        currentIndex = idx,
                        currentValue = valNum,
                        unprunedCalls = unprunedCount,
                        prunedCalls = prunedCount,
                        action = ComplexityAction.EVALUATE_PRUNED,
                        explanation = "Pruned (Backtracking) Step ${idx + 1}: Element $valNum ≤ limit $limit (valid). Total pruned calls so far: $prunedCount.",
                        badgeText = "PRUNED STEP ${idx + 1}: VALID $valNum ≤ $limit"
                    )
                )
            }
        }

        // FINISHED
        steps.add(
            BacktrackingComplexityStep(
                array = array,
                limit = limit,
                unprunedCalls = unprunedCount,
                prunedCalls = prunedCount,
                action = ComplexityAction.FINISHED,
                explanation = "Comparison complete! Unpruned search required $unprunedCount calls vs Pruned search required $prunedCount calls (saved ${unprunedCount - prunedCount} call(s)). Note: Pruning reduces work when a valid condition safely eliminates branches on specific inputs. In the worst case where no branches are pruned, worst-case complexity remains exponential.",
                badgeText = "COMPLETE: PRUNED ($prunedCount CALLS) VS UNPRUNED ($unprunedCount CALLS)",
                isTerminal = true
            )
        )

        return steps
    }
}
