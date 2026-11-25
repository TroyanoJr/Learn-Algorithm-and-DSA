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
internal fun GridPathfindingVisualizationCard(
    title: String = "Grid Pathfinding & Matrix Backtracking"
) {
    val steps = remember { GridPathfindingTrace.createTrace() }
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
        ArrayWindowButton(text = "Open grid pathfinding visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.action) {
            GridPathAction.INIT -> "GRID PATHFINDING: START (0,0) -> TARGET (${step.targetCell.r},${step.targetCell.c})"
            GridPathAction.MOVE -> "MOVE TO (${step.currentCell?.r}, ${step.currentCell?.c})"
            GridPathAction.HIT_WALL -> "HIT OBSTACLE WALL AT (${step.currentCell?.r}, ${step.currentCell?.c})"
            GridPathAction.REVISITED -> "REVISITED CELL AT (${step.currentCell?.r}, ${step.currentCell?.c}) · PRUNE"
            GridPathAction.TARGET_REACHED -> "TARGET REACHED AT (${step.targetCell.r}, ${step.targetCell.c})!"
            GridPathAction.BACKTRACK -> "BACKTRACK FROM (${step.currentCell?.r}, ${step.currentCell?.c})"
            GridPathAction.FINISHED -> "PATHFINDING COMPLETE · ${step.currentPath.size} STEPS IN PATH"
        }

        val statusColor = when (step.action) {
            GridPathAction.TARGET_REACHED, GridPathAction.FINISHED -> GreenAccent
            GridPathAction.HIT_WALL, GridPathAction.REVISITED, GridPathAction.BACKTRACK -> RedAccent
            GridPathAction.MOVE -> OrangeAccent
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
                val presentation = remember(steps) { GridPathfindingSceneBuilder.build(steps) }
                GridPathfindingSceneRenderer(presentation, presentation.frames[stepIndex])
            },
            detailsContent = {
                BacktrackingDetailsPanel("GRID PATH DECISION", step.badgeText, statusColor,
                    "Upper Bound: O(4^(R×C))", step.explanation, stepIndex, steps.size, isCompleted)
            }
        )
    }
}
