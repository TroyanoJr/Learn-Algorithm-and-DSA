package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9CA3AF)

@Composable
internal fun DijkstraVisualizationCard(
    title: String = "Dijkstra's Algorithm"
) {
    val steps = DijkstraTrace.stepsForDijkstra()
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val playback = rememberVisualizerPlaybackState(
        stepCount = steps.size,
        initialStep = 0
    )
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val step = steps[stepIndex]

    ArrayWindowButton(text = "Open Dijkstra visualizer") {
        isExpanded = true
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
                FitToPane(modifier = Modifier.fillMaxSize()) {
                    DijkstraVisualizationContent(step = step, scale = 1f)
                }
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
private fun DijkstraVisualizationContent(step: DijkstraTraceStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Distance & Predecessor Table
        Surface(
            color = Color(0xFF13131C),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, CardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(8.dp * scale),
                verticalArrangement = Arrangement.spacedBy(4.dp * scale)
            ) {
                Text("TENTATIVE DISTANCE & PREDECESSOR TABLE:", color = BlueAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    step.distances.forEach { (v, dVal) ->
                        val pVal = step.predecessors[v] ?: "—"
                        Surface(
                            color = if (v == step.currentVertex) Color(0xFF2A1C12) else Color(0xFF181824),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, if (v == step.currentVertex) OrangeAccent else CardBorderColor),
                            modifier = Modifier.weight(1f).padding(horizontal = 2.dp * scale)
                        ) {
                            Column(
                                modifier = Modifier.padding(4.dp * scale),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(v, color = OrangeAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("dist: ${dVal ?: "∞"}", color = GreenAccent, fontSize = 9.sp)
                                Text("pred: $pVal", color = BlueAccent, fontSize = 8.sp)
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

        // Graph Visual Render
        Surface(
            color = Color(0xFF13131C),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, CardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(10.dp * scale).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp * scale)
            ) {
                Text("DIRECTED WEIGHTED GRAPH: A→B(4), A→C(1), C→B(2), B→D(1), C→D(5)", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val pathList = step.reconstructedPath ?: emptyList()
                    GraphVertexCircle("A", isActive = step.currentVertex == "A", isVisited = "A" in pathList, badgeText = "dist=0")
                    Text("→ (1)", color = BlueAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    GraphVertexCircle("C", isActive = step.currentVertex == "C", isVisited = "C" in pathList, badgeText = "dist=1")
                    Text("→ (2)", color = BlueAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    GraphVertexCircle("B", isActive = step.currentVertex == "B", isVisited = "B" in pathList, badgeText = "dist=3")
                    Text("→ (1)", color = BlueAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    GraphVertexCircle("D", isActive = step.currentVertex == "D", isVisited = "D" in pathList, badgeText = "dist=4")
                }
            }
        }
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
                        DijkstraStepEvent.DIJKSTRA_COMPLETE -> GreenAccent
                        DijkstraStepEvent.RELAX_EDGE -> OrangeAccent
                        else -> BlueAccent
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Non-negative Weights: O((V+E) log V)", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
