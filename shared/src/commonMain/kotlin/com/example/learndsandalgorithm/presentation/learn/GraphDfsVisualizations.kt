package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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

private val OrangeAccent = Color(0xFFFA6E13)
private val BlueAccent = Color(0xFF38BDF8)
private val GreenAccent = Color(0xFF10B981)
private val TextSecondary = Color(0xFF9CA3AF)

@Composable
internal fun GraphDfsVisualizationCard(
    title: String = "Depth-First Search (DFS)"
) {
    val steps = remember { GraphDfsTrace.stepsForDfs() }
    val scenes = remember(steps) { GraphSceneBuilder.buildDfsScenes(steps) }
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
        ArrayWindowButton(text = "Open DFS visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.event) {
            DfsStepEvent.START -> "DFS START · RECURSIVE DEEP EXPLORATION"
            DfsStepEvent.EXPLORE_VERTEX -> "EXPLORING VERTEX ${step.currentVertex}"
            DfsStepEvent.BACKTRACK -> "BACKTRACKING TO ${step.currentVertex}"
            DfsStepEvent.DFS_COMPLETE -> "DFS TRAVERSAL COMPLETE"
        }

        val statusColor = when (step.event) {
            DfsStepEvent.DFS_COMPLETE -> GreenAccent
            DfsStepEvent.BACKTRACK -> OrangeAccent
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
            fullWidthVisualWeight = 0.65f,
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
                GraphSceneRenderer(scene = scene, modifier = Modifier.fillMaxSize())
            },
            detailsContent = {
                GraphDfsDetailsAndStepsContent(
                    step = step,
                    stepIndex = stepIndex,
                    totalSteps = steps.size,
                    allExplanations = steps.map { it.explanation },
                    scale = 1f
                )
            }
        )
    }
}

@Composable
private fun GraphDfsDetailsAndStepsContent(
    step: GraphDfsTraceStep,
    stepIndex: Int,
    totalSteps: Int,
    allExplanations: List<String>,
    scale: Float
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(3.dp * scale)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("CURRENT DFS STEP", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text("Stack Depth: ${step.callStack.size} · O(V+E)", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Text(step.badgeText, color = when (step.event) {
                DfsStepEvent.DFS_COMPLETE -> GreenAccent
                DfsStepEvent.BACKTRACK -> OrangeAccent
                else -> BlueAccent
            }, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        GraphStepsCard(
            label = "Steps",
            explanation = step.explanation,
            allExplanations = allExplanations,
            stepIndex = stepIndex,
            totalSteps = totalSteps,
            isCompleted = stepIndex == totalSteps - 1
        )

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            GraphDfsInfoPanel("CALL STACK (BOTTOM → TOP)", step.callStack.takeIf { it.isNotEmpty() }?.joinToString(" → ") ?: "Empty", Modifier.weight(1f))
            GraphDfsInfoPanel("VISITED SEQUENCE", step.visited.takeIf { it.isNotEmpty() }?.joinToString(" → ") ?: "Empty", Modifier.weight(1f))
        }
    }
}

@Composable
private fun GraphDfsInfoPanel(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(color = Color(0xFF13131C), shape = RoundedCornerShape(8.dp), modifier = modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(4.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = BlueAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(value, color = Color.White, fontSize = 11.sp)
        }
    }
}
