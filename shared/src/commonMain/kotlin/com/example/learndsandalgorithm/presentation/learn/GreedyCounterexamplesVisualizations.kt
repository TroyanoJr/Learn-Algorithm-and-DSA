package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.runtime.Composable

@Composable
internal fun GreedyCounterexamplesVisualizationCard(title: String = "Greedy Counterexamples & Correctness") {
    val steps = GreedyCounterexamplesTrace.createTrace()
    val playback = rememberVisualizerPlaybackState(stepCount = steps.size, initialStep = 0)
    val presentation = greedyPresentationSnapshot(playback, steps, GreedyCounterexampleSceneBuilder::build,
        GreedyCounterexamplesStep::explanation, GreedyCounterexamplesStep::badgeText)
    val step = presentation.snapshot
    val tone = when (step.status) {
        CounterexampleStatus.FINISHED, CounterexampleStatus.GREEDY_STEP_3 -> GreedySceneTone.REJECTED
        CounterexampleStatus.COMPARE_OPTIMAL -> GreedySceneTone.SELECTED
        CounterexampleStatus.GREEDY_STEP_1, CounterexampleStatus.GREEDY_STEP_2 -> GreedySceneTone.ACTIVE
        else -> GreedySceneTone.ACCENT
    }
    val status = greedyCounterexampleStatusText(step)
    GreedyVisualizationModal(
        title = title,
        statusText = status,
        statusTone = tone,
        sceneForViewport = presentation.sceneForViewport,
        playback = playback,
        explanation = presentation.explanation,
        badge = presentation.badge,
        complexity = "Proof by Counterexample",
        viewportKey = "greedy-counterexample",
        openButtonText = "Open counterexamples visualizer",
        additionalExplanation = if (step.status >= CounterexampleStatus.COMPARE_OPTIMAL) {
            GreedyCounterexampleSceneBuilder.WHY_GREEDY_FAILS
        } else null,
        singleProgressIndicatorInDetails = true
    )
}

internal fun greedyCounterexampleStatusText(step: GreedyCounterexamplesStep): String =
    when (step.status) {
        CounterexampleStatus.INIT -> "COUNTEREXAMPLE: TARGET " + step.targetAmount + " WITH " + step.denominations
        CounterexampleStatus.GREEDY_STEP_1 -> "GREEDY CHOICE 1: PICK 4 (LARGEST)"
        CounterexampleStatus.GREEDY_STEP_2 -> "GREEDY CHOICE 2: PICK 1"
        CounterexampleStatus.GREEDY_STEP_3 -> "GREEDY CHOICE 3: PICK 1 (TOTAL 3 COINS)"
        CounterexampleStatus.COMPARE_OPTIMAL -> "OPTIMAL SOLUTION: [3, 3] (2 COINS)"
        CounterexampleStatus.FINISHED -> "COUNTEREXAMPLE PROVEN · GREEDY FAILS HERE"
    }
