package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CardBorderColor = Color(0xFF262634)
private val OrangeAccent = Color(0xFFFA6E13)
private val BlueAccent = Color(0xFF38BDF8)
private val GreenAccent = Color(0xFF10B981)
private val RedAccent = Color(0xFFEF4444)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9CA3AF)

@Composable
internal fun GridPathfindingVisualizationCard(
    title: String = "Grid Pathfinding & Matrix Backtracking"
) {
    val steps = GridPathfindingTrace.createTrace()
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
                GridPathfindingDetailsAndStepsContent(
                    step = step,
                    stepIndex = stepIndex,
                    totalSteps = steps.size,
                    scale = 1f
                )
            }
        )
    }
}

@Composable
private fun GridPathfindingDetailsAndStepsContent(
    step: GridPathStep,
    stepIndex: Int,
    totalSteps: Int,
    scale: Float
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp * scale)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("GRID PATH DECISION", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.action) {
                        GridPathAction.TARGET_REACHED, GridPathAction.FINISHED -> GreenAccent
                        GridPathAction.HIT_WALL, GridPathAction.REVISITED, GridPathAction.BACKTRACK -> RedAccent
                        else -> OrangeAccent
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Upper Bound: O(4^(R×C))", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }

        StepsCard(
            label = "Steps",
            explanation = step.explanation,
            stepIndex = stepIndex,
            totalSteps = totalSteps,
            isCompleted = stepIndex == totalSteps - 1
        )
    }
}
