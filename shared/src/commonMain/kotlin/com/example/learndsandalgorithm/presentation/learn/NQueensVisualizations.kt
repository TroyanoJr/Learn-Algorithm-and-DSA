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
internal fun NQueensVisualizationCard(
    title: String = "The N-Queens Problem"
) {
    val steps = remember { NQueensTrace.createTrace() }
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
        ArrayWindowButton(text = "Open N-Queens visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.action) {
            NQueensAction.INIT -> "N-QUEENS: ${step.boardSize}x${step.boardSize} BOARD"
            NQueensAction.TRY_POSITION -> "TESTING ROW ${step.currentRow}, COL ${step.testingCol}"
            NQueensAction.PLACE_QUEEN -> "SAFE! PLACED QUEEN AT (${step.currentRow}, ${step.testingCol})"
            NQueensAction.CONFLICT_PRUNE -> "CONFLICT AT (${step.currentRow}, ${step.testingCol}) · PRUNE"
            NQueensAction.BACKTRACK -> "BACKTRACK FROM ROW ${step.currentRow}"
            NQueensAction.SOLUTION_FOUND -> "VALID N-QUEENS SOLUTION FOUND!"
            NQueensAction.FINISHED -> "SEARCH COMPLETE · ${step.solutions.size} SOLUTIONS"
        }

        val statusColor = when (step.action) {
            NQueensAction.SOLUTION_FOUND, NQueensAction.FINISHED -> GreenAccent
            NQueensAction.CONFLICT_PRUNE, NQueensAction.BACKTRACK -> RedAccent
            NQueensAction.PLACE_QUEEN, NQueensAction.TRY_POSITION -> OrangeAccent
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
                val presentation = remember(steps) { NQueensSceneBuilder.build(steps) }
                NQueensSceneRenderer(presentation, presentation.frames[stepIndex])
            },
            detailsContent = {
                BacktrackingDetailsPanel("N-QUEENS DECISION", step.badgeText, statusColor,
                    "Complexity: O(N!)", step.explanation, stepIndex, steps.size, isCompleted)
            }
        )
    }
}
