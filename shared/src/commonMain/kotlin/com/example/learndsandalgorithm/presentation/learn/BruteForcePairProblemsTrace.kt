package com.example.learndsandalgorithm.presentation.learn

internal enum class PairSearchEvent {
    START,
    EVALUATE_PAIR,
    PAIR_FOUND,
    NO_PAIR_FOUND
}

internal data class EvaluatedPair(
    val i: Int,
    val j: Int,
    val valI: Int,
    val valJ: Int,
    val sum: Int,
    val isMatch: Boolean
)

internal data class BruteForcePairProblemsTraceStep(
    val values: List<Int>,
    val target: Int,
    val currentI: Int? = null,
    val currentJ: Int? = null,
    val currentSum: Int? = null,
    val foundPair: Pair<Int, Int>? = null,
    val evaluatedPairs: List<EvaluatedPair> = emptyList(),
    val event: PairSearchEvent,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object BruteForcePairProblemsTrace {

    fun stepsForFindPair(
        values: List<Int> = listOf(2, 7, 11, 15),
        target: Int = 9
    ): List<BruteForcePairProblemsTraceStep> {
        val steps = mutableListOf<BruteForcePairProblemsTraceStep>()
        val evaluated = mutableListOf<EvaluatedPair>()
        var foundPair: Pair<Int, Int>? = null

        fun snapshot(
            i: Int? = null,
            j: Int? = null,
            sum: Int? = null,
            event: PairSearchEvent,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = BruteForcePairProblemsTraceStep(
            values = values,
            target = target,
            currentI = i,
            currentJ = j,
            currentSum = sum,
            foundPair = foundPair,
            evaluatedPairs = evaluated.toList(),
            event = event,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        // Step 0: Ready
        steps.add(
            snapshot(
                event = PairSearchEvent.START,
                explanation = "Brute Force Pairs: Finding two indices (i, j) where values[i] + values[j] = $target. Outer loop i in [0..N-1], inner loop j in [i+1..N-1].",
                badgeText = "READY · TARGET = $target"
            )
        )

        for (i in values.indices) {
            for (j in (i + 1) until values.size) {
                val valI = values[i]
                val valJ = values[j]
                val sum = valI + valJ
                val isMatch = sum == target

                evaluated.add(EvaluatedPair(i, j, valI, valJ, sum, isMatch))

                if (isMatch) {
                    foundPair = Pair(i, j)
                    steps.add(
                        snapshot(
                            i = i,
                            j = j,
                            sum = sum,
                            event = PairSearchEvent.PAIR_FOUND,
                            isTerminal = true,
                            explanation = "PAIR FOUND! Index $i ($valI) + Index $j ($valJ) = $sum == $target. Returns indices ($i, $j).",
                            badgeText = "MATCH: ($i, $j) → $valI+$valJ=$target"
                        )
                    )
                    return steps
                } else {
                    steps.add(
                        snapshot(
                            i = i,
                            j = j,
                            sum = sum,
                            event = PairSearchEvent.EVALUATE_PAIR,
                            explanation = "Evaluate pair ($i, $j): values[$i] ($valI) + values[$j] ($valJ) = $sum != $target. Continue inner loop.",
                            badgeText = "TEST PAIR ($i, $j): $sum ≠ $target"
                        )
                    )
                }
            }
        }

        // Complete if no pair found
        steps.add(
            snapshot(
                event = PairSearchEvent.NO_PAIR_FOUND,
                isTerminal = true,
                explanation = "Complete: Checked all N(N-1)/2 distinct pairs in O(N²) time. No pair sums to $target (returns NONE).",
                badgeText = "NO PAIR FOUND (-1, -1)"
            )
        )

        return steps
    }
}
