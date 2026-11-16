package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
internal fun GraphBfsVisualizationCard(
    title: String = "Breadth-First Search (BFS)"
) {
    val steps = remember { GraphBfsTrace.stepsForBfs() }
    val scenes = remember(steps) { GraphSceneBuilder.buildBfsScenes(steps) }
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
        ArrayWindowButton(text = "Open BFS visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.event) {
            BfsStepEvent.START -> "BFS START · ENQUEUED A"
            BfsStepEvent.DEQUEUE_VERTEX -> "DEQUEUED VERTEX ${step.currentVertex}"
            BfsStepEvent.ENQUEUE_NEIGHBORS -> "ENQUEUED UNVISITED NEIGHBORS"
            BfsStepEvent.BFS_COMPLETE -> "BFS TRAVERSAL COMPLETE"
        }

        val statusColor = when (step.event) {
            BfsStepEvent.BFS_COMPLETE -> GreenAccent
            BfsStepEvent.DEQUEUE_VERTEX -> OrangeAccent
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
                GraphBfsDetailsAndStepsContent(
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
private fun GraphBfsDetailsAndStepsContent(
    step: GraphBfsTraceStep,
    stepIndex: Int,
    totalSteps: Int,
    allExplanations: List<String>,
    scale: Float
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val compactDashboard = maxHeight < 280.dp
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(3.dp * scale)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("CURRENT BFS STEP", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("Complexity: O(V + E) · O(V) space", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Text(step.badgeText, color = when (step.event) {
                    BfsStepEvent.BFS_COMPLETE -> GreenAccent
                    BfsStepEvent.DEQUEUE_VERTEX -> OrangeAccent
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

            if (compactDashboard) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    Box(Modifier.weight(1f)) { GraphQueueRow(items = step.queue, title = "QUEUE (FRONT → REAR):") }
                    GraphBfsInfoPanel("VISITED SET", step.visited.takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "Empty", Modifier.weight(1f))
                    GraphBfsInfoPanel("DISCOVERY / VISITED ORDER", step.visited.takeIf { it.isNotEmpty() }?.joinToString(" → ") ?: "Empty", Modifier.weight(1f))
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    Box(Modifier.weight(1f)) { GraphQueueRow(items = step.queue, title = "QUEUE (FRONT → REAR):") }
                    GraphBfsInfoPanel("VISITED SET", step.visited.takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "Empty", Modifier.weight(1f))
                }
                GraphBfsInfoPanel("DISCOVERY / VISITED ORDER", step.visited.takeIf { it.isNotEmpty() }?.joinToString(" → ") ?: "Empty")
            }
        }
    }
}

@Composable
private fun GraphBfsInfoPanel(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(color = Color(0xFF13131C), shape = RoundedCornerShape(8.dp), modifier = modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(4.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = BlueAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(value, color = Color.White, fontSize = 11.sp)
        }
    }
}
