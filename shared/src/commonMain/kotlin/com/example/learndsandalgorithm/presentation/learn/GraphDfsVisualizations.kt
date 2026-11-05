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
internal fun GraphDfsVisualizationCard(
    title: String = "Depth-First Search (DFS)"
) {
    val steps = GraphDfsTrace.stepsForDfs()
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val playback = rememberVisualizerPlaybackState(
        stepCount = steps.size,
        initialStep = 0
    )
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val step = steps[stepIndex]

    ArrayWindowButton(text = "Open DFS visualizer") {
        isExpanded = true
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
                    GraphDfsVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                GraphDfsDetailsAndStepsContent(
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
private fun GraphDfsVisualizationContent(step: GraphDfsTraceStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Call Stack Component
        GraphQueueRow(
            title = "CALL STACK (BOTTOM → TOP):",
            items = step.callStack,
            accentColor = OrangeAccent
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
                Text("UNDIRECTED GRAPH (DFS PATH):", color = BlueAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                // Row 1: A, B, C
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    GraphVertexCircle("A", isActive = step.currentVertex == "A", isVisited = "A" in step.visited)
                    Text("—", color = BlueAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    GraphVertexCircle("B", isActive = step.currentVertex == "B", isVisited = "B" in step.visited)
                    Text("—", color = BlueAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    GraphVertexCircle("C", isActive = step.currentVertex == "C", isVisited = "C" in step.visited)
                }

                // Row 2: D, E
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    GraphVertexCircle("D", isActive = step.currentVertex == "D", isVisited = "D" in step.visited)
                    Text("—", color = BlueAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    GraphVertexCircle("E", isActive = step.currentVertex == "E", isVisited = "E" in step.visited)
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
                Text("DFS VISITED SEQUENCE OUTPUT:", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
private fun GraphDfsDetailsAndStepsContent(
    step: GraphDfsTraceStep,
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
                Text("CURRENT DFS STEP", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.event) {
                        DfsStepEvent.DFS_COMPLETE -> GreenAccent
                        DfsStepEvent.BACKTRACK -> OrangeAccent
                        else -> BlueAccent
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Stack Depth: ${step.callStack.size} | O(V+E)", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
