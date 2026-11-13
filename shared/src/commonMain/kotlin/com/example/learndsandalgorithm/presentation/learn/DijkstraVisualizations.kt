package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
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

private val CardBorderColor = Color(0xFF262634)
private val OrangeAccent = Color(0xFFFA6E13)
private val BlueAccent = Color(0xFF38BDF8)
private val GreenAccent = Color(0xFF10B981)
private val TextSecondary = Color(0xFF9CA3AF)

@Composable
internal fun DijkstraVisualizationCard(
    title: String = "Dijkstra's Algorithm"
) {
    val steps = remember { DijkstraTrace.stepsForDijkstra() }
    val scenes = remember(steps) { GraphSceneBuilder.buildDijkstraScenes(steps) }
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
        ArrayWindowButton(text = "Open Dijkstra visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.event) {
            DijkstraStepEvent.START -> "DIJKSTRA START · SOURCE ${step.source} TO ${step.target}"
            DijkstraStepEvent.DEQUEUE_MIN -> "DEQUEUED MIN DISTANCE VERTEX ${step.currentVertex}"
            DijkstraStepEvent.RELAX_EDGE -> "RELAXING EDGE ${step.relaxedEdge?.from} → ${step.relaxedEdge?.to} (w=${step.relaxedEdge?.weight})"
            DijkstraStepEvent.DIJKSTRA_COMPLETE -> "DIJKSTRA COMPLETE"
        }

        val statusColor = when (step.event) {
            DijkstraStepEvent.DIJKSTRA_COMPLETE -> GreenAccent
            DijkstraStepEvent.RELAX_EDGE -> OrangeAccent
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
            fullWidthVisualWeight = 1.22f,
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
                GraphSceneRenderer(
                    scene = scene,
                    nodeClearanceDp = 36f,
                    modifier = Modifier.fillMaxSize()
                )
            },
            detailsContent = {
                DijkstraDetailsAndStepsContent(
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
private fun DijkstraDetailsAndStepsContent(
    step: DijkstraTraceStep,
    stepIndex: Int,
    totalSteps: Int,
    scale: Float
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(5.dp * scale)
    ) {
        // Current Step Header + Explanation Card
        Surface(
            color = Color(0xFF161622),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, CardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 8.dp * scale, vertical = 6.dp * scale),
                verticalArrangement = Arrangement.spacedBy(3.dp * scale)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("CURRENT STEP", color = TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "STEP ${stepIndex + 1} / $totalSteps",
                        color = if (stepIndex == totalSteps - 1) GreenAccent else OrangeAccent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp * scale)
                ) {
                    Text(
                        text = step.badgeText,
                        color = when (step.event) {
                            DijkstraStepEvent.DIJKSTRA_COMPLETE -> GreenAccent
                            DijkstraStepEvent.RELAX_EDGE -> OrangeAccent
                            else -> BlueAccent
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Non-negative Weights: O((V+E) log V)",
                        color = GreenAccent,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = step.explanation,
                    color = Color(0xFFFFFFFF),
                    fontSize = 9.5.sp,
                    lineHeight = 12.sp
                )
            }
        }

        // Distance & Predecessor Table
        Surface(
            color = Color(0xFF13131C),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, CardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(5.dp * scale),
                verticalArrangement = Arrangement.spacedBy(3.dp * scale)
            ) {
                Text("TENTATIVE DISTANCE & PREDECESSOR TABLE:", color = BlueAccent, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    step.distances.forEach { (v, dVal) ->
                        val pVal = step.predecessors[v] ?: "—"
                        val isCurrent = v == step.currentVertex
                        Surface(
                            color = if (v == step.currentVertex) Color(0xFF2A1C12) else Color(0xFF181824),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, if (v == step.currentVertex) OrangeAccent else CardBorderColor),
                            modifier = Modifier.weight(1f).padding(horizontal = 1.dp * scale)
                        ) {
                            Column(
                                modifier = Modifier.padding(2.5.dp * scale),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(v, color = OrangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("dist: ${dVal ?: "∞"}", color = GreenAccent, fontSize = 8.sp)
                                Text("pred: $pVal", color = BlueAccent, fontSize = 8.5.sp)
                            }
                        }
                    }
                }
            }
        }

        // Priority Queue Component
        GraphQueueRow(
            title = "PRIORITY QUEUE (MIN DISTANCE ENTRY):",
            items = step.priorityQueue.map { "(${it.first}, ${it.second})" },
            accentColor = OrangeAccent
        )
    }
}
