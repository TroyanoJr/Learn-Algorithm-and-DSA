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
internal fun GraphUnweightedShortestPathVisualizationCard(
    title: String = "Shortest Paths in Unweighted Graphs"
) {
    val steps = GraphUnweightedShortestPathTrace.stepsForShortestPath()
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val playback = rememberVisualizerPlaybackState(
        stepCount = steps.size,
        initialStep = 0
    )
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val step = steps[stepIndex]

    ArrayWindowButton(text = "Open unweighted shortest path visualizer") {
        isExpanded = true
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
                FitToPane(modifier = Modifier.fillMaxSize()) {
                    GraphUnweightedShortestPathVisualizationContent(step = step, scale = 1f)
                }
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
private fun GraphUnweightedShortestPathVisualizationContent(step: UnweightedShortestPathTraceStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Distance Table Header Card
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
                Text("DISTANCE & PREDECESSOR TABLE:", color = BlueAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
                                Text("$v", color = OrangeAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("dist: ${dVal ?: "∞"}", color = GreenAccent, fontSize = 9.sp)
                                Text("pred: $pVal", color = BlueAccent, fontSize = 8.sp)
                            }
                        }
                    }
                }
            }
        }

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
                Text("GRAPH SHORTEST PATH TRAVERSAL:", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val pathList = step.reconstructedPath ?: emptyList()
                    GraphVertexCircle("A", isActive = step.currentVertex == "A", isVisited = "A" in pathList, badgeText = "dist=0")
                    Text("—", color = BlueAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    GraphVertexCircle("B", isActive = step.currentVertex == "B", isVisited = "B" in pathList, badgeText = "dist=1")
                    Text("—", color = BlueAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    GraphVertexCircle("C", isActive = step.currentVertex == "C", isVisited = "C" in pathList, badgeText = "dist=1")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val pathList = step.reconstructedPath ?: emptyList()
                    GraphVertexCircle("D", isActive = step.currentVertex == "D", isVisited = "D" in pathList, badgeText = "dist=2")
                    Text("—", color = BlueAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    GraphVertexCircle("E", isActive = step.currentVertex == "E", isVisited = "E" in pathList, badgeText = "dist=3")
                }
            }
        }

        // Reconstructed Path Output Card
        if (step.reconstructedPath != null) {
            Surface(
                color = Color(0xFF13131C),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, GreenAccent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp * scale).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("RECONSTRUCTED SHORTEST PATH:", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(step.reconstructedPath.joinToString(" - "), color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
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
