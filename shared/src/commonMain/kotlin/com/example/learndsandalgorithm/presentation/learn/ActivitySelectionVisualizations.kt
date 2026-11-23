package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.runtime.Composable

@Composable
internal fun ActivitySelectionVisualizationCard(title: String = "Activity Selection") {
    val steps = ActivitySelectionTrace.createTrace()
    val playback = rememberVisualizerPlaybackState(stepCount = steps.size, initialStep = 0)
    val presentation = greedyPresentationSnapshot(playback, steps, ActivitySelectionSceneBuilder::build,
        ActivitySelectionStep::explanation, ActivitySelectionStep::badgeText)
    val step = presentation.snapshot
    val tone = when (step.status) {
        ActivitySelectionStatus.SELECTED, ActivitySelectionStatus.FINISHED -> GreedySceneTone.SELECTED
        ActivitySelectionStatus.REJECTED -> GreedySceneTone.REJECTED
        ActivitySelectionStatus.CHECKING, ActivitySelectionStatus.SORTED -> GreedySceneTone.ACTIVE
        else -> GreedySceneTone.ACCENT
    }
    val status = activitySelectionStatusText(step)
    GreedyVisualizationModal(
        title = title,
        statusText = status,
        statusTone = tone,
        sceneForViewport = presentation.sceneForViewport,
        playback = playback,
        explanation = presentation.explanation,
        badge = presentation.badge,
        complexity = "Complexity: O(N log N)",
        viewportKey = "greedy-activity-selection",
        openButtonText = "Open activity selection visualizer",
        singleProgressIndicatorInDetails = true,
        overflowHeading = "Activity timeline detail",
        initialScaleMode = VisualizerCanvasInitialScale.NATURAL,
        resetViewportWithPlayback = true
    )
}

internal fun activitySelectionStatusText(step: ActivitySelectionStep): String =
    when (step.status) {
        ActivitySelectionStatus.INIT -> "EARLIEST-FINISH-TIME STRATEGY"
        ActivitySelectionStatus.SORTED -> "SORTED ACTIVITIES BY FINISH TIME"
        ActivitySelectionStatus.CHECKING -> "CHECKING ACTIVITY " + step.currentActivity?.name
        ActivitySelectionStatus.SELECTED -> "SELECTED ACTIVITY " + step.currentActivity?.name
        ActivitySelectionStatus.REJECTED -> "REJECTED OVERLAPPING ACTIVITY " + step.currentActivity?.name
        ActivitySelectionStatus.FINISHED -> "SELECTION COMPLETE · " + step.selectedActivities.size + " ACTIVITIES"
    }
