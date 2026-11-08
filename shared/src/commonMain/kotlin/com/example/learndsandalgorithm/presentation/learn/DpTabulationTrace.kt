package com.example.learndsandalgorithm.presentation.learn

internal enum class TabulationAction {
    INIT,
    SET_BASE_CASES,
    FILL_TABLE,
    FINISHED
}

internal data class DpTabulationStep(
    val targetN: Int = 5,
    val currentIndex: Int? = null,
    val dpTable: List<Long> = List(6) { 0L },
    val prev2: Long = 1,
    val prev1: Long = 2,
    val currentWays: Long? = null,
    val action: TabulationAction = TabulationAction.INIT,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object DpTabulationTrace {

    fun createTrace(targetN: Int = 5): List<DpTabulationStep> {
        val steps = mutableListOf<DpTabulationStep>()
        val dp = MutableList(targetN + 1) { 0L }

        fun snapshot(
            idx: Int?,
            p2: Long,
            p1: Long,
            curr: Long?,
            action: TabulationAction,
            explanation: String,
            badgeText: String,
            isTerminal: Boolean = false
        ) = DpTabulationStep(
            targetN = targetN,
            currentIndex = idx,
            dpTable = dp.toList(),
            prev2 = p2,
            prev1 = p1,
            currentWays = curr,
            action = action,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        // Step 0: INIT
        steps.add(
            snapshot(
                idx = null,
                p2 = 1,
                p1 = 2,
                curr = null,
                action = TabulationAction.INIT,
                explanation = "Bottom-Up Tabulation: Computing ways to climb $targetN stairs. Solves subproblems iteratively starting from base cases up to target.",
                badgeText = "READY · STAIRS $targetN TABULATION"
            )
        )

        // Step 1: Base cases
        dp[1] = 1L
        dp[2] = 2L
        var prev2 = 1L
        var prev1 = 2L

        steps.add(
            snapshot(
                idx = 2,
                p2 = prev2,
                p1 = prev1,
                curr = null,
                action = TabulationAction.SET_BASE_CASES,
                explanation = "Base cases: dp[1] = 1 (1 step), dp[2] = 2 (1+1 or 2 steps). Initialized rolling variables prev2 = 1, prev1 = 2.",
                badgeText = "BASE CASING: dp[1]=1, dp[2]=2"
            )
        )

        for (i in 3..targetN) {
            val curr = prev1 + prev2
            dp[i] = curr

            steps.add(
                snapshot(
                    idx = i,
                    p2 = prev2,
                    p1 = prev1,
                    curr = curr,
                    action = TabulationAction.FILL_TABLE,
                    explanation = "Stair $i: dp[$i] = dp[${i - 1}] + dp[${i - 2}] = $prev1 + $prev2 = $curr. Rolling update: prev2 = $prev1, prev1 = $curr.",
                    badgeText = "FILL dp[$i] = $curr (PREV1=$prev1, PREV2=$prev2)"
                )
            )

            prev2 = prev1
            prev1 = curr
        }

        val finalAns = if (targetN <= 2) dp[targetN] else prev1

        // FINISHED
        steps.add(
            snapshot(
                idx = targetN,
                p2 = prev2,
                p1 = prev1,
                curr = finalAns,
                action = TabulationAction.FINISHED,
                explanation = "Tabulation complete! Total ways to climb $targetN stairs = $finalAns. Space-optimized using rolling variables from O(N) down to O(1) auxiliary space.",
                badgeText = "COMPLETE: $finalAns WAYS TO CLIMB $targetN STAIRS",
                isTerminal = true
            )
        )

        return steps
    }
}
