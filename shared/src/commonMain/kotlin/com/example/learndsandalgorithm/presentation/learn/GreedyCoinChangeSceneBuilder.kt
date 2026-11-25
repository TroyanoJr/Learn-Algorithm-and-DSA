package com.example.learndsandalgorithm.presentation.learn

internal enum class CoinDenominationVisualState { PENDING, ACTIVE, TAKE, SKIP }

internal data class CoinDenominationOccurrence(
    val id: String,
    val index: Int,
    val value: Int
)

internal data class SelectedCoinOccurrence(
    val id: String,
    val sequenceIndex: Int,
    val value: Int
)

internal data class GreedyCoinChangeSceneFrame(
    val step: GreedyCoinChangeStep,
    val denominationStates: List<CoinDenominationVisualState>,
    val selectedCoinOccurrences: List<SelectedCoinOccurrence>,
    val selectedAmount: Long
)

internal data class GreedyCoinChangeScenePresentation(
    val denominationOccurrences: List<CoinDenominationOccurrence>,
    val frames: List<GreedyCoinChangeSceneFrame>,
    val maximumSelectedCoinCount: Int
)

internal object DedicatedGreedyCoinChangeSceneBuilder {
    fun build(steps: List<GreedyCoinChangeStep>): GreedyCoinChangeScenePresentation {
        require(steps.isNotEmpty())
        val denominationOccurrences = steps.first().denominations.mapIndexed { index, value ->
            CoinDenominationOccurrence(id = "denomination-$index", index = index, value = value)
        }
        val frames = steps.map { step ->
            GreedyCoinChangeSceneFrame(
                step = step,
                denominationStates = denominationOccurrences.map { denomination ->
                    if (step.currentCoinIndex != denomination.index) {
                        CoinDenominationVisualState.PENDING
                    } else {
                        when (step.status) {
                            CoinChangeStatus.CONSIDERING -> CoinDenominationVisualState.ACTIVE
                            CoinChangeStatus.TAKE_COIN -> CoinDenominationVisualState.TAKE
                            CoinChangeStatus.SKIP_COIN -> CoinDenominationVisualState.SKIP
                            else -> CoinDenominationVisualState.PENDING
                        }
                    }
                },
                selectedCoinOccurrences = step.selectedCoins.mapIndexed { index, value ->
                    SelectedCoinOccurrence(id = "selected-coin-$index", sequenceIndex = index, value = value)
                },
                selectedAmount = step.selectedCoins.sumOf { it.toLong() }
            )
        }
        return GreedyCoinChangeScenePresentation(
            denominationOccurrences = denominationOccurrences,
            frames = frames,
            maximumSelectedCoinCount = frames.maxOf { it.selectedCoinOccurrences.size }
        )
    }
}
