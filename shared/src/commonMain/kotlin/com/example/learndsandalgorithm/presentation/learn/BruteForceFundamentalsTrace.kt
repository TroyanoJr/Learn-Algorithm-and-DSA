package com.example.learndsandalgorithm.presentation.learn

internal enum class FundamentalsSearchEvent {
    START,
    EVALUATE_CANDIDATE,
    SOLUTION_FOUND,
    SEARCH_COMPLETE
}

internal data class CandidateEvaluation(
    val candidate: Int,
    val squareValue: Int,
    val isMatch: Boolean
)

internal data class BruteForceFundamentalsTraceStep(
    val rangeMin: Int = 1,
    val rangeMax: Int = 5,
    val targetSquare: Int = 16,
    val currentCandidate: Int? = null,
    val currentSquare: Int? = null,
    val foundSolution: Int? = null,
    val evaluatedCandidates: List<CandidateEvaluation> = emptyList(),
    val event: FundamentalsSearchEvent,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object BruteForceFundamentalsTrace {

    fun stepsForSquareSearch(
        range: IntRange = 1..5,
        target: Int = 16
    ): List<BruteForceFundamentalsTraceStep> {
        val steps = mutableListOf<BruteForceFundamentalsTraceStep>()
        val evaluations = mutableListOf<CandidateEvaluation>()
        var foundSol: Int? = null

        fun snapshot(
            candidate: Int? = null,
            square: Int? = null,
            event: FundamentalsSearchEvent,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = BruteForceFundamentalsTraceStep(
            rangeMin = range.first,
            rangeMax = range.last,
            targetSquare = target,
            currentCandidate = candidate,
            currentSquare = square,
            foundSolution = foundSol,
            evaluatedCandidates = evaluations.toList(),
            event = event,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        // Step 0: Ready
        steps.add(
            snapshot(
                event = FundamentalsSearchEvent.START,
                explanation = "Brute Force Fundamentals: Searching candidate set [${range.first}..${range.last}] for integer x where x² = $target.",
                badgeText = "READY · SEARCH RANGE [${range.first}..${range.last}]"
            )
        )

        for (candidate in range) {
            val sq = candidate * candidate
            val isMatch = sq == target

            evaluations.add(CandidateEvaluation(candidate, sq, isMatch))

            if (isMatch) {
                foundSol = candidate
                steps.add(
                    snapshot(
                        candidate = candidate,
                        square = sq,
                        event = FundamentalsSearchEvent.SOLUTION_FOUND,
                        isTerminal = true,
                        explanation = "SOLUTION FOUND! Candidate $candidate: $candidate * $candidate = $sq == $target. Stopping early as one solution is enough.",
                        badgeText = "MATCH FOUND: x = $candidate"
                    )
                )
                return steps
            } else {
                steps.add(
                    snapshot(
                        candidate = candidate,
                        square = sq,
                        event = FundamentalsSearchEvent.EVALUATE_CANDIDATE,
                        explanation = "Candidate $candidate: $candidate * $candidate = $sq != $target. Continue exhaustive search.",
                        badgeText = "CANDIDATE $candidate: $sq ≠ $target"
                    )
                )
            }
        }

        // Step Complete (if no solution found)
        steps.add(
            snapshot(
                event = FundamentalsSearchEvent.SEARCH_COMPLETE,
                isTerminal = true,
                explanation = "Search complete. Evaluated all candidates in [${range.first}..${range.last}]. No solution exists (solution = -1).",
                badgeText = "COMPLETE: NO MATCH (-1)"
            )
        )

        return steps
    }
}
