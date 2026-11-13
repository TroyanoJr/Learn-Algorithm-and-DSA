package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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

private val OrangeAccent = Color(0xFFFA6E13)
private val BlueAccent = Color(0xFF38BDF8)
private val GreenAccent = Color(0xFF10B981)
private val TextSecondary = Color(0xFF9CA3AF)

@Composable
internal fun GraphUnweightedShortestPathVisualizationCard(
    title: String = "Shortest Paths in Unweighted Graphs"
) {
    val steps = remember { GraphUnweightedShortestPathTrace.stepsForShortestPath() }
    val scenes = remember(steps) { GraphSceneBuilder.buildShortestPathScenes(steps) }
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
    val scene = scenes[stepIndex]

    if (!autoExpand) {
        ArrayWindowButton(text = "Open unweighted shortest path visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.event) {
            ShortestPathEvent.START -> "BFS SHORTEST PATH · SOURCE ${step.source} TO ${step.target}"
            ShortestPathEvent.DEQUEUE_AND_UPDATE_DIST -> "PROCESSED VERTEX ${step.currentVertex}"
            ShortestPathEvent.RECONSTRUCT_PATH -> "RECONSTRUCTING PATH VIA PREDECESSORS"
            ShortestPathEvent.PATH_COMPLETE -> "SHORTEST PATH COMPLETE"
        }

        val statusColor = when (step.event) {
            ShortestPathEvent.PATH_COMPLETE -> GreenAccent
            ShortestPathEvent.RECONSTRUCT_PATH -> OrangeAccent
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
                GraphSceneRenderer(scene = scene, modifier = Modifier.fillMaxSize())
            },
            detailsContent = {
                GraphUnweightedShortestPathDetailsAndStepsContent(
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
private fun GraphUnweightedShortestPathDetailsAndStepsContent(
    step: UnweightedShortestPathTraceStep,
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
                Text("CURRENT STEP", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.event) {
                        ShortestPathEvent.PATH_COMPLETE -> GreenAccent
                        ShortestPathEvent.RECONSTRUCT_PATH -> OrangeAccent
                        else -> BlueAccent
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Fewest Edges: O(V + E)", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
