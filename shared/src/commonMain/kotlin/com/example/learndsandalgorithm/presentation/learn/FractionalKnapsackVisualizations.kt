package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.runtime.Composable

@Composable
internal fun FractionalKnapsackVisualizationCard(title: String = "Fractional Knapsack") {
    val steps = FractionalKnapsackTrace.createTrace()
    val playback = rememberVisualizerPlaybackState(stepCount = steps.size, initialStep = 0)
    val presentation = greedyPresentationSnapshot(playback, steps, FractionalKnapsackSceneBuilder::build,
        FractionalKnapsackStep::explanation, FractionalKnapsackStep::badgeText)
    val step = presentation.snapshot
    val tone = when (step.status) {
        FractionalKnapsackStatus.TAKE_FULL, FractionalKnapsackStatus.TAKE_FRACTION, FractionalKnapsackStatus.FINISHED -> GreedySceneTone.SELECTED
        FractionalKnapsackStatus.EVALUATING, FractionalKnapsackStatus.SORTED -> GreedySceneTone.ACTIVE
        else -> GreedySceneTone.ACCENT
    }
    val status = fractionalKnapsackStatusText(step)
    GreedyVisualizationModal(
        title = title,
        statusText = status,
        statusTone = tone,
        sceneForViewport = presentation.sceneForViewport,
        playback = playback,
        explanation = presentation.explanation,
        badge = presentation.badge,
        complexity = "Complexity: O(N log N)",
        viewportKey = "greedy-fractional-knapsack",
        openButtonText = "Open fractional knapsack visualizer",
        singleProgressIndicatorInDetails = true
    )
}

internal fun fractionalKnapsackStatusText(step: FractionalKnapsackStep): String =
    when (step.status) {
        FractionalKnapsackStatus.INIT -> "FRACTIONAL KNAPSACK: CAPACITY " + formatGreedyDecimal(step.capacity)
        FractionalKnapsackStatus.SORTED -> "SORTED ITEMS BY VALUE/WEIGHT RATIO"
        FractionalKnapsackStatus.EVALUATING -> "EVALUATING ITEM " + step.currentItem?.name
        FractionalKnapsackStatus.TAKE_FULL -> "TAKEN 100% OF " + step.currentItem?.name
        FractionalKnapsackStatus.TAKE_FRACTION -> "TAKEN FRACTION OF " + step.currentItem?.name
        FractionalKnapsackStatus.FINISHED -> "KNAPSACK FILLED · TOTAL VALUE " + formatGreedyDecimal(step.totalValue)
    }
