package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.runtime.Composable

@Composable
internal fun GreedyFundamentalsVisualizationCard(title: String = "Greedy Algorithm Fundamentals") {
    val steps = GreedyFundamentalsTrace.createTrace()
    val playback = rememberVisualizerPlaybackState(stepCount = steps.size, initialStep = 0)
    val rejectedCandidateIndex = steps.lastOrNull { it.status == GreedyFundamentalsStatus.REJECTED }?.currentCandidateIndex
    val presentation = greedyPresentationSnapshot(playback, steps, { snapshot, width, height ->
        GreedyFundamentalsSceneBuilder.build(
            snapshot,
            width,
            height,
            previouslyRejectedIndex = rejectedCandidateIndex.takeIf { snapshot.status == GreedyFundamentalsStatus.FINISHED }
        )
    },
        GreedyFundamentalsStep::explanation, GreedyFundamentalsStep::badgeText)
    val step = presentation.snapshot
    val tone = when (step.status) {
        GreedyFundamentalsStatus.SELECTED, GreedyFundamentalsStatus.FINISHED -> GreedySceneTone.SELECTED
        GreedyFundamentalsStatus.REJECTED -> GreedySceneTone.REJECTED
        GreedyFundamentalsStatus.EVALUATING, GreedyFundamentalsStatus.SORTING -> GreedySceneTone.ACTIVE
        else -> GreedySceneTone.ACCENT
    }
    val status = greedyFundamentalsStatusText(step)
    GreedyVisualizationModal(
        title = title,
        statusText = status,
        statusTone = tone,
        sceneForViewport = presentation.sceneForViewport,
        playback = playback,
        explanation = presentation.explanation,
        badge = presentation.badge,
        complexity = "Complexity: O(N log N)",
        viewportKey = "greedy-fundamentals",
        openButtonText = "Open greedy fundamentals visualizer"
    )
}

internal fun greedyFundamentalsStatusText(step: GreedyFundamentalsStep): String =
    when (step.status) {
        GreedyFundamentalsStatus.INIT -> "GREEDY FUNDAMENTALS: BUDGET " + step.totalBudget
        GreedyFundamentalsStatus.SORTING -> "SORTING CANDIDATES (ASCENDING)"
        GreedyFundamentalsStatus.EVALUATING -> "EVALUATING DISTANCE " + step.currentCandidateDistance
        GreedyFundamentalsStatus.SELECTED -> "SELECTED DISTANCE " + step.currentCandidateDistance
        GreedyFundamentalsStatus.REJECTED -> "EXCEEDS REMAINING BUDGET"
        GreedyFundamentalsStatus.FINISHED -> "SEARCH COMPLETE · OPTIMAL RESULT"
    }
