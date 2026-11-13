package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9CA3AF)

@Composable
internal fun TopologicalSortVisualizationCard(
    title: String = "Topological Sorting"
) {
    val steps = remember { TopologicalSortTrace.stepsForTopoSort() }
    val scenes = remember(steps) { GraphSceneBuilder.buildTopologicalSortScenes(steps) }
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TopologicalSortDetailsAndStepsContent(
    step: TopologicalSortTraceStep,
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
                            TopoSortEvent.TOPO_COMPLETE -> GreenAccent
                            TopoSortEvent.DEQUEUE_ZERO_INDEGREE -> OrangeAccent
                            else -> BlueAccent
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Kahn's Cost: O(V + E) Time",
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

        // Zero In-Degree Queue Row Component
        GraphQueueRow(
            title = "ZERO IN-DEGREE READY QUEUE:",
            items = step.queue,
            accentColor = BlueAccent
        )

        // In-Degrees Table
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
                Text("CURRENT IN-DEGREES:", color = OrangeAccent, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    step.inDegrees.forEach { (v, degVal) ->
                        val isZero = degVal == 0
                        Surface(
                            color = if (isZero) Color(0xFF123820) else Color(0xFF181824),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, if (isZero) GreenAccent else CardBorderColor),
                            modifier = Modifier.weight(1f).padding(horizontal = 1.dp * scale)
                        ) {
                            Column(
                                modifier = Modifier.padding(2.5.dp * scale),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(v, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("in: $degVal", color = if (isZero) GreenAccent else TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Topo Order Output Card (with FlowRow and descriptive label & empty-state text)
        Surface(
            color = Color(0xFF13131C),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, CardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 8.dp * scale, vertical = 5.dp * scale),
                verticalArrangement = Arrangement.spacedBy(3.dp * scale)
            ) {
                Text("TOPOLOGICAL SCHEDULE ORDER (DAG):", color = GreenAccent, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                if (step.topoOrder.isEmpty()) {
                    Text("No vertices scheduled yet", color = TextSecondary, fontSize = 8.sp)
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(3.dp * scale),
                        verticalArrangement = Arrangement.spacedBy(3.dp * scale),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        step.topoOrder.forEach { v ->
                            Surface(
                                color = Color(0xFF123820),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, GreenAccent)
                            ) {
                                Text(
                                    v,
                                    color = GreenAccent,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp * scale, vertical = 2.dp * scale)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
