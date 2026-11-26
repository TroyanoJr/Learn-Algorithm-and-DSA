package com.example.learndsandalgorithm.presentation.learn

internal enum class CoinChangeDpAction {
    INIT,
    CHECK_COIN,
    UPDATE_DP,
    FINISHED
}

internal data class CoinChangeDpStep(
    val targetAmount: Int = 6,
    val coins: List<Int> = listOf(1, 2, 5),
    val currentAmount: Int? = null,
    val currentCoin: Int? = null,
    val currentCoinIndex: Int? = null,
    val dpTable: List<Int> = List(7) { Int.MAX_VALUE },
    val candidateVal: Int? = null,
    val action: CoinChangeDpAction = CoinChangeDpAction.INIT,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object CoinChangeDpTrace {

    const val INF = Int.MAX_VALUE

    fun createTrace(
        targetAmount: Int = 6,
        coins: List<Int> = listOf(1, 2, 5)
    ): List<CoinChangeDpStep> {
        require(targetAmount >= 0) { "target amount must be nonnegative" }
        require(coins.all { it > 0 }) { "coin denominations must be positive" }
        val steps = mutableListOf<CoinChangeDpStep>()
        val dp = MutableList(targetAmount + 1) { INF }
        dp[0] = 0

        fun snapshot(
            w: Int?,
            c: Int?,
            cand: Int?,
            action: CoinChangeDpAction,
            explanation: String,
            badgeText: String,
            isTerminal: Boolean = false,
            coinIndex: Int? = null
        ) = CoinChangeDpStep(
            targetAmount = targetAmount,
            coins = coins,
            currentAmount = w,
            currentCoin = c,
            currentCoinIndex = coinIndex,
            dpTable = dp.toList(),
            candidateVal = cand,
            action = action,
            explanation = explanation,
            badgeText = badgeText,
            isTerminal = isTerminal
        )

        // Step 0: INIT
        steps.add(
            snapshot(
                w = 0,
                c = null,
                cand = null,
                action = CoinChangeDpAction.INIT,
                explanation = "Unbounded Choice Coin Change: Minimum coins to make amount $targetAmount using coins $coins. Base case dp[0] = 0, others initialized to INF.",
                badgeText = "READY · TARGET $targetAmount WITH $coins"
            )
        )

        for (w in 1..targetAmount) {
            for ((coinIndex, coin) in coins.withIndex()) {
                if (w >= coin) {
                    val prevDp = dp[w - coin]
                    val cand = if (prevDp != INF) 1 + prevDp else INF

                    steps.add(
                        snapshot(
                            w = w,
                            c = coin,
                            cand = cand,
                            action = CoinChangeDpAction.CHECK_COIN,
                            explanation = "Amount $w: Try coin $coin. Candidate formula: 1 + dp[$w - $coin] = 1 + dp[${w - coin}] = ${if (cand == INF) "INF" else cand.toString()}.",
                            badgeText = "TRY COIN $coin FOR AMOUNT $w",
                            coinIndex = coinIndex
                        )
                    )

                    if (cand != INF && cand < dp[w]) {
                        dp[w] = cand
                        steps.add(
                            snapshot(
                                w = w,
                                c = coin,
                                cand = cand,
                                action = CoinChangeDpAction.UPDATE_DP,
                                explanation = "UPDATE dp[$w] = $cand (improved using coin $coin).",
                                badgeText = "UPDATE dp[$w] = $cand",
                                coinIndex = coinIndex
                            )
                        )
                    }
                }
            }
        }

        val finalAns = if (dp[targetAmount] == INF) -1 else dp[targetAmount]

        // FINISHED
        steps.add(
            snapshot(
                w = targetAmount,
                c = null,
                cand = finalAns,
                action = CoinChangeDpAction.FINISHED,
                explanation = if (finalAns < 0) "Coin change DP complete! No solution exists for amount $targetAmount with coins $coins. Unbounded choice allows reuse of each coin. Time: O(W * C), Space: O(W)." else "Coin change DP complete! Minimum coins for amount $targetAmount = $finalAns. Unbounded choice allows infinite reuse of each coin. Time: O(W * C), Space: O(W).",
                badgeText = if (finalAns < 0) "COMPLETE: NO SOLUTION" else "COMPLETE: MIN COINS = $finalAns",
                isTerminal = true
            )
        )

        return steps
    }
}
