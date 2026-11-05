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
internal fun GraphBfsVisualizationCard(
    title: String = "Breadth-First Search (BFS)"
) {
    val steps = GraphBfsTrace.stepsForBfs()
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val playback = rememberVisualizerPlaybackState(
        stepCount = steps.size,
        initialStep = 0
    )
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val step = steps[stepIndex]

    ArrayWindowButton(text = "Open BFS visualizer") {
        isExpanded = true
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
                    GraphBfsVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                GraphBfsDetailsAndStepsContent(
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
private fun GraphBfsVisualizationContent(step: GraphBfsTraceStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Queue Row Component
        GraphQueueRow(
            title = "BFS QUEUE (FIRST-IN, FIRST-OUT):",
            items = step.queue,
            accentColor = BlueAccent
        )

        // Graph Topology Diagram
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
                Text("UNDIRECTED GRAPH (VISITED SET: {${step.visited.joinToString(", ")}}):", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                // Row 1: A, B, C
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    GraphVertexCircle("A", isActive = step.currentVertex == "A", isVisited = "A" in step.visited, isQueued = "A" in step.queue)
                    Text("—", color = BlueAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    GraphVertexCircle("B", isActive = step.currentVertex == "B", isVisited = "B" in step.visited, isQueued = "B" in step.queue)
                    Text("—", color = BlueAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    GraphVertexCircle("C", isActive = step.currentVertex == "C", isVisited = "C" in step.visited, isQueued = "C" in step.queue)
                }

                // Row 2: D, E
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    GraphVertexCircle("D", isActive = step.currentVertex == "D", isVisited = "D" in step.visited, isQueued = "D" in step.queue)
                    Text("—", color = BlueAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    GraphVertexCircle("E", isActive = step.currentVertex == "E", isVisited = "E" in step.visited, isQueued = "E" in step.queue)
                }
            }
        }

        // Visited Output Sequence
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
                Text("DISCOVERY / VISITED ORDER:", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                if (step.visited.isEmpty()) {
                    Text("Visited set empty", color = TextSecondary, fontSize = 10.sp)
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        step.visited.forEach { v ->
                            Surface(
                                color = Color(0xFF123820),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, GreenAccent),
                                modifier = Modifier.padding(end = 4.dp * scale)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp * scale, vertical = 4.dp * scale)) {
                                    Text(v, color = GreenAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GraphBfsDetailsAndStepsContent(
    step: GraphBfsTraceStep,
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
                Text("CURRENT BFS STEP", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.event) {
                        BfsStepEvent.BFS_COMPLETE -> GreenAccent
                        BfsStepEvent.DEQUEUE_VERTEX -> OrangeAccent
                        else -> BlueAccent
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Complexity: O(V + E) Time, O(V) Space", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
