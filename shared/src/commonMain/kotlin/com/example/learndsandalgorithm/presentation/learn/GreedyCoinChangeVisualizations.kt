package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.runtime.Composable

@Composable
internal fun GreedyCoinChangeVisualizationCard(title: String = "Greedy Coin Change") {
    val steps = GreedyCoinChangeTrace.createTrace()
    val playback = rememberVisualizerPlaybackState(stepCount = steps.size, initialStep = 0)
    val presentation = greedyPresentationSnapshot(playback, steps, GreedyCoinChangeSceneBuilder::build,
        GreedyCoinChangeStep::explanation, GreedyCoinChangeStep::badgeText)
    val step = presentation.snapshot
    val tone = when (step.status) {
        CoinChangeStatus.TAKE_COIN, CoinChangeStatus.FINISHED -> GreedySceneTone.SELECTED
        CoinChangeStatus.SKIP_COIN -> GreedySceneTone.REJECTED
        CoinChangeStatus.CONSIDERING -> GreedySceneTone.ACTIVE
        else -> GreedySceneTone.ACCENT
    }
    val status = greedyCoinChangeStatusText(step)
    GreedyVisualizationModal(
        title = title,
        statusText = status,
        statusTone = tone,
        sceneForViewport = presentation.sceneForViewport,
        playback = playback,
        explanation = presentation.explanation,
        badge = presentation.badge,
        complexity = "Time: O(D + K)",
        viewportKey = "greedy-coin-change",
        openButtonText = "Open coin change visualizer"
    )
}

internal fun greedyCoinChangeStatusText(step: GreedyCoinChangeStep): String =
    when (step.status) {
        CoinChangeStatus.INIT -> "GREEDY COIN CHANGE: TARGET " + step.targetAmount
        CoinChangeStatus.CONSIDERING -> "CONSIDERING COIN " + step.currentCoinValue
        CoinChangeStatus.TAKE_COIN -> "TAKING COIN " + step.currentCoinValue
        CoinChangeStatus.SKIP_COIN -> "SKIPPING COIN " + step.currentCoinValue + " (TOO LARGE)"
        CoinChangeStatus.FINISHED -> "TARGET REACHED · " + step.selectedCoins.size + " COINS"
    }
