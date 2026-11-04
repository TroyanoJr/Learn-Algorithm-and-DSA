package com.example.learndsandalgorithm.presentation.learn

internal enum class TradeoffScenario {
    BALANCED_VS_UNBALANCED_SPLIT,
    ALGORITHM_COMPARISON
}

internal data class TradeoffsTraceStep(
    val scenario: TradeoffScenario,
    val inputN: Int = 10,
    val goodLeft: Int? = null,
    val goodRight: Int? = null,
    val goodTreeDepth: Int? = null,
    val badLeft: Int? = null,
    val badRight: Int? = null,
    val badTreeDepth: Int? = null,
    val mergeSortTime: String = "O(N log N) Guaranteed",
    val mergeSortSpace: String = "O(N) Auxiliary",
    val mergeSortStable: Boolean = true,
    val quickSortTime: String = "O(N log N) Avg / O(N²) Worst",
    val quickSortSpace: String = "O(log N) Stack (In-place)",
    val quickSortStable: Boolean = false,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object DivideConquerTradeoffsTrace {

    fun stepsForScenario(scenario: TradeoffScenario): List<TradeoffsTraceStep> {
        return when (scenario) {
            TradeoffScenario.BALANCED_VS_UNBALANCED_SPLIT -> generatePartitionSplitSteps()
            TradeoffScenario.ALGORITHM_COMPARISON -> generateComparisonSteps()
        }
    }

    private fun generatePartitionSplitSteps(): List<TradeoffsTraceStep> {
        val steps = mutableListOf<TradeoffsTraceStep>()
        val n = 10

        fun snapshot(
            goodL: Int? = null,
            goodR: Int? = null,
            goodDepth: Int? = null,
            badL: Int? = null,
            badR: Int? = null,
            badDepth: Int? = null,
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = TradeoffsTraceStep(
            scenario = TradeoffScenario.BALANCED_VS_UNBALANCED_SPLIT,
            inputN = n,
            goodLeft = goodL,
            goodRight = goodR,
            goodTreeDepth = goodDepth,
            badLeft = badL,
            badRight = badR,
            badTreeDepth = badDepth,
            isTerminal = isTerminal,
            explanation = explanation,
            badgeText = badgeText
        )

        steps.add(
            snapshot(
                explanation = "Partition Split Comparison: Analyzing how partition balance affects recursion tree depth and overall algorithm performance for N = $n.",
                badgeText = "READY · INPUT N = $n"
            )
        )

        // Step 1: Balanced Split (N/2, N/2)
        val goodL = n / 2
        val goodR = n - goodL
        steps.add(
            snapshot(
                goodL = goodL,
                goodR = goodR,
                goodDepth = 4, // log2(10) ≈ 3.32 -> 4 levels
                explanation = "BALANCED SPLIT: Splitting N=$n into Left=$goodL and Right=$goodR. Binary tree depth is O(log N) = log2(10) ≈ 4 levels. Total Work: O(N log N).",
                badgeText = "BALANCED: $goodL & $goodR (DEPTH log N)"
            )
        )

        // Step 2: Unbalanced Split (1, N-1)
        val badL = 1
        val badR = n - badL
        steps.add(
            snapshot(
                goodL = goodL,
                goodR = goodR,
                goodDepth = 4,
                badL = badL,
                badR = badR,
                badDepth = n, // N levels
                explanation = "UNBALANCED SPLIT: Splitting N=$n into Left=$badL and Right=$badR. Recursion tree degenerates into a linear chain of N=$n levels! Total Work: O(N²).",
                badgeText = "UNBALANCED: $badL & $badR (DEPTH N)"
            )
        )

        // Step 3: Summary
        steps.add(
            snapshot(
                goodL = goodL,
                goodR = goodR,
                goodDepth = 4,
                badL = badL,
                badR = badR,
                badDepth = n,
                isTerminal = true,
                explanation = "Partition Balance Conclusion: Balanced halves yield O(N log N) time; worst-case unbalanced pivots (e.g. sorted input with last-element pivot) degrade Quick Sort to O(N²).",
                badgeText = "CONCLUSION: BALANCE KEEPS O(N log N)"
            )
        )

        return steps
    }

    private fun generateComparisonSteps(): List<TradeoffsTraceStep> {
        val steps = mutableListOf<TradeoffsTraceStep>()

        fun snapshot(
            isTerminal: Boolean = false,
            explanation: String,
            badgeText: String
        ) = TradeoffsTraceStep(
            scenario = TradeoffScenario.ALGORITHM_COMPARISON,
            isTerminal = isTerminal,
            explanation = explanation,
            badgeText = badgeText
        )

        steps.add(
            snapshot(
                explanation = "Algorithm Trade-offs: Comparing Merge Sort vs Quick Sort across Time Complexity, Memory, and Stability.",
                badgeText = "READY · MERGE SORT vs QUICK SORT"
            )
        )

        steps.add(
            snapshot(
                explanation = "1. MERGE SORT: Guarantees O(N log N) time in ALL cases (best, average, worst). Requires O(N) auxiliary space for merging. STABLE sorting.",
                badgeText = "MERGE SORT: GUARANTEED O(N log N), O(N) SPACE"
            )
        )

        steps.add(
            snapshot(
                explanation = "2. QUICK SORT: Runs in O(N log N) average time, but degrades to O(N²) worst-case with poor pivots. Sorts IN-PLACE (O(log N) stack space). UNSTABLE sorting.",
                badgeText = "QUICK SORT: FAST IN-PLACE, O(N²) WORST-CASE"
            )
        )

        steps.add(
            snapshot(
                isTerminal = true,
                explanation = "PRACTICAL DECISION RULE: Use Merge Sort when guaranteed O(N log N) time or stability is mandatory. Use Quick Sort when in-place memory efficiency and practical cache performance are paramount.",
                badgeText = "DECISION RULE: MERGE (GUARANTEE/STABLE) vs QUICK (IN-PLACE)"
            )
        )

        return steps
    }
}
