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
internal fun TopologicalSortVisualizationCard(
    title: String = "Topological Sorting"
) {
    val steps = TopologicalSortTrace.stepsForTopoSort()
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
        ArrayWindowButton(text = "Open topological sort visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.event) {
            TopoSortEvent.START -> "KAHN'S ALGORITHM · ZERO IN-DEGREE QUEUE"
            TopoSortEvent.DEQUEUE_ZERO_INDEGREE, TopoSortEvent.DECREASE_INDEGREE -> "DEQUEUED ZERO-INDEGREE VERTEX ${step.currentVertex}"
            TopoSortEvent.TOPO_COMPLETE -> "TOPOLOGICAL SORT COMPLETE"
        }

        val statusColor = when (step.event) {
            TopoSortEvent.TOPO_COMPLETE -> GreenAccent
            TopoSortEvent.DEQUEUE_ZERO_INDEGREE -> OrangeAccent
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
                    TopologicalSortVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                TopologicalSortDetailsAndStepsContent(
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
private fun TopologicalSortVisualizationContent(step: TopologicalSortTraceStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Zero In-Degree Queue Row Component
        GraphQueueRow(
            title = "ZERO IN-DEGREE READY QUEUE:",
            items = step.queue,
            accentColor = BlueAccent
        )

        // In-Degrees Table
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
                Text("CURRENT IN-DEGREES:", color = OrangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    step.inDegrees.forEach { (v, degVal) ->
                        val isZero = degVal == 0
                        Surface(
                            color = if (isZero) Color(0xFF123820) else Color(0xFF181824),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, if (isZero) GreenAccent else CardBorderColor),
                            modifier = Modifier.weight(1f).padding(horizontal = 2.dp * scale)
                        ) {
                            Column(
                                modifier = Modifier.padding(4.dp * scale),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(v, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("in: $degVal", color = if (isZero) GreenAccent else TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Topo Order Output Card
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
                Text("TOPOLOGICAL SCHEDULE ORDER (DAG):", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                if (step.topoOrder.isEmpty()) {
                    Text("No vertices scheduled yet", color = TextSecondary, fontSize = 10.sp)
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        step.topoOrder.forEach { v ->
                            Surface(
                                color = Color(0xFF123820),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, GreenAccent),
                                modifier = Modifier.padding(end = 4.dp * scale)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 10.dp * scale, vertical = 4.dp * scale)) {
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
private fun TopologicalSortDetailsAndStepsContent(
    step: TopologicalSortTraceStep,
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
                        TopoSortEvent.TOPO_COMPLETE -> GreenAccent
                        TopoSortEvent.DEQUEUE_ZERO_INDEGREE -> OrangeAccent
                        else -> BlueAccent
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Kahn's Cost: O(V + E) Time", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
