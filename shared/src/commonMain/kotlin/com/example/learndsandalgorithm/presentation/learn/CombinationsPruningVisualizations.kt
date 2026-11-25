package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

private val OrangeAccent = Color(0xFFFA6E13)
private val BlueAccent = Color(0xFF38BDF8)
private val GreenAccent = Color(0xFF10B981)
private val RedAccent = Color(0xFFEF4444)

@Composable
internal fun CombinationsPruningVisualizationCard(
    title: String = "Combinations & Subsets with Pruning"
) {
    val steps = remember { CombinationsPruningTrace.createTrace() }
    val autoExpand = LocalAutoExpandVisualizer.current
    var isExpanded by rememberSaveable { mutableStateOf(autoExpand) }

    val playback = rememberVisualizerPlaybackState(
        stepCount = steps.size,
        initialStep = 0
    )
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val step = steps[stepIndex]

    if (!autoExpand) {
        ArrayWindowButton(text = "Open combination sum visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.action) {
            CombinationAction.INIT -> "COMBINATION SUM: TARGET ${step.target}"
            CombinationAction.TRY_CANDIDATE -> "TRY CANDIDATE -> CURRENT SUM ${step.currentSum}"
            CombinationAction.PRUNE -> "PRUNE BRANCH: SUM ${step.currentSum} > TARGET ${step.target}"
            CombinationAction.SOLUTION_FOUND -> "SOLUTION FOUND: ${step.currentCombination}"
            CombinationAction.BACKTRACK -> "BACKTRACK -> RESTORE SUM ${step.currentSum}"
            CombinationAction.FINISHED -> "SEARCH COMPLETE · ${step.validCombinations.size} SOLUTIONS"
        }

        val statusColor = when (step.action) {
            CombinationAction.SOLUTION_FOUND, CombinationAction.FINISHED -> GreenAccent
            CombinationAction.PRUNE -> RedAccent
            CombinationAction.TRY_CANDIDATE -> OrangeAccent
            else -> BlueAccent
        }

        VisualizerModal(
            title = title,
            statusText = statusText,
            isCompleted = isCompleted,
            stepIndex = stepIndex,
            stepCount = steps.size,
            isPlaying = isPlaying,
            layoutMode = VisualizerLayoutMode.FULL_WIDTH,
            detailsScrollable = false,
            statusColor = statusColor,
            onReset = { playback.reset() },
            onPrevious = { playback.previous() },
            onTogglePlayback = { playback.togglePlayback() },
            onNext = { playback.next() },
            onClose = {
                playback.reset()
                isExpanded = false
            },
            visualContent = {
                val presentation = remember(steps) { CombinationsPruningSceneBuilder.build(steps) }
                CombinationsPruningSceneRenderer(presentation, presentation.frames[stepIndex])
            },
            detailsContent = {
                BacktrackingDetailsPanel("PRUNING DECISION", step.badgeText, statusColor,
                    "Pruning Optimization", step.explanation, stepIndex, steps.size, isCompleted)
            }
        )
    }
}
