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
    val rollingInitialized: Boolean = false,
    val action: TabulationAction = TabulationAction.INIT,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object DpTabulationTrace {

    fun createTrace(targetN: Int = 5): List<DpTabulationStep> {
        require(targetN >= 0) { "targetN must be nonnegative" }
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
            rollingInitialized = action != TabulationAction.INIT,
            action = action,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        // Step 0: INIT
        steps.add(
            snapshot(
                idx = null,
                p2 = 0,
                p1 = 0,
                curr = null,
                action = TabulationAction.INIT,
                explanation = "Bottom-Up Tabulation: Computing ways to climb $targetN stairs. Solves subproblems iteratively starting from base cases up to target.",
                badgeText = "READY · STAIRS $targetN TABULATION"
            )
        )

        // Step 1: Base cases
        dp[0] = 1L
        if (targetN >= 1) dp[1] = 1L
        if (targetN >= 2) dp[2] = 2L
        var prev2 = 1L
        var prev1 = if (targetN >= 2) 2L else 1L

        steps.add(
            snapshot(
                idx = minOf(2, targetN),
                p2 = prev2,
                p1 = prev1,
                curr = null,
                action = TabulationAction.SET_BASE_CASES,
                explanation = if (targetN >= 2) "Base cases: dp[0] = 1, dp[1] = 1 (1 step), dp[2] = 2 (1+1 or 2 steps). Initialized rolling variables prev2 = 1, prev1 = 2." else "Base cases: dp[0] = 1${if (targetN >= 1) ", dp[1] = 1" else ""}.",
                badgeText = if (targetN >= 2) "BASE CASES: dp[0]=1, dp[1]=1, dp[2]=2" else "BASE CASES INITIALIZED"
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
                explanation = "Tabulation complete! Total ways to climb $targetN stairs = $finalAns. This educational trace retains the O(N) table and snapshots; a rolling-variable-only algorithm uses O(1) auxiliary space.",
                badgeText = "COMPLETE: $finalAns WAYS TO CLIMB $targetN STAIRS",
                isTerminal = true
            )
        )

        return steps
    }
}
