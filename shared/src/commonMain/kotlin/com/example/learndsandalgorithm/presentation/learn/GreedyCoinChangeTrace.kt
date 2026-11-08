package com.example.learndsandalgorithm.presentation.learn

internal enum class CoinChangeStatus {
    INIT,
    CONSIDERING,
    TAKE_COIN,
    SKIP_COIN,
    FINISHED
}

internal data class GreedyCoinChangeStep(
    val targetAmount: Int = 11,
    val remainingAmount: Int = 11,
    val denominations: List<Int> = listOf(4, 3, 1),
    val currentCoinIndex: Int? = null,
    val currentCoinValue: Int? = null,
    val selectedCoins: List<Int> = emptyList(),
    val status: CoinChangeStatus = CoinChangeStatus.INIT,
    val explanation: String,
    val badgeText: String,
    val isTerminal: Boolean = false
)

internal object GreedyCoinChangeTrace {

    fun createTrace(
        target: Int = 11,
        denominations: List<Int> = listOf(4, 3, 1)
    ): List<GreedyCoinChangeStep> {
        val steps = mutableListOf<GreedyCoinChangeStep>()
        val denoms = denominations.sortedDescending()
        val selected = mutableListOf<Int>()
        var remaining = target

        // Step 0: INIT
        steps.add(
            GreedyCoinChangeStep(
                targetAmount = target,
                remainingAmount = remaining,
                denominations = denoms,
                selectedCoins = selected.toList(),
                status = CoinChangeStatus.INIT,
                explanation = "Greedy Coin Change: Make target amount $target using denominations $denoms. Strategy: largest coin first.",
                badgeText = "READY · TARGET $target"
            )
        )

        for ((idx, coin) in denoms.withIndex()) {
            while (coin <= remaining) {
                // CONSIDERING
                steps.add(
                    GreedyCoinChangeStep(
                        targetAmount = target,
                        remainingAmount = remaining,
                        denominations = denoms,
                        currentCoinIndex = idx,
                        currentCoinValue = coin,
                        selectedCoins = selected.toList(),
                        status = CoinChangeStatus.CONSIDERING,
                        explanation = "Consider coin $coin. $coin ≤ remaining amount $remaining (fits).",
                        badgeText = "CONSIDER COIN $coin ≤ $remaining"
                    )
                )

                selected.add(coin)
                remaining -= coin

                // TAKE_COIN
                steps.add(
                    GreedyCoinChangeStep(
                        targetAmount = target,
                        remainingAmount = remaining,
                        denominations = denoms,
                        currentCoinIndex = idx,
                        currentCoinValue = coin,
                        selectedCoins = selected.toList(),
                        status = CoinChangeStatus.TAKE_COIN,
                        explanation = "Take coin $coin. Remaining amount updated: ${remaining + coin} - $coin = $remaining.",
                        badgeText = "TAKE COIN $coin (REM: $remaining)"
                    )
                )
            }

            if (remaining > 0 && coin > remaining) {
                steps.add(
                    GreedyCoinChangeStep(
                        targetAmount = target,
                        remainingAmount = remaining,
                        denominations = denoms,
                        currentCoinIndex = idx,
                        currentCoinValue = coin,
                        selectedCoins = selected.toList(),
                        status = CoinChangeStatus.SKIP_COIN,
                        explanation = "Coin $coin > remaining amount $remaining. Move to next smaller denomination.",
                        badgeText = "SKIP COIN $coin > $remaining"
                    )
                )
            }
        }

        // FINISHED
        steps.add(
            GreedyCoinChangeStep(
                targetAmount = target,
                remainingAmount = remaining,
                denominations = denoms,
                selectedCoins = selected.toList(),
                status = CoinChangeStatus.FINISHED,
                explanation = "Greedy coin change complete! Total coins used: ${selected.size} ($selected). Time complexity: O(d + k).",
                badgeText = "COMPLETE: ${selected.size} COINS USED",
                isTerminal = true
            )
        )

        return steps
    }
}
