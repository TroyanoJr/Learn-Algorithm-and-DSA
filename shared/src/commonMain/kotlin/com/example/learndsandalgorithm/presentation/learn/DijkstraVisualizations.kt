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
import androidx.compose.ui.layout.SubcomposeLayout
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
                    allExplanations = steps.map { it.explanation },
                    allBadgeTexts = steps.map { it.badgeText },
                    allPriorityQueues = steps.map { traceStep ->
                        traceStep.priorityQueue.map { "(${it.first}, ${it.second})" }
                    },
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
    allExplanations: List<String>,
    allBadgeTexts: List<String>,
    allPriorityQueues: List<List<String>>,
    scale: Float
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp * scale)
    ) {
        DijkstraStableHeader(step = step, allBadgeTexts = allBadgeTexts)

        GraphStepsCard(
            label = "Steps",
            explanation = step.explanation,
            allExplanations = allExplanations,
            stepIndex = stepIndex,
            totalSteps = totalSteps,
            isCompleted = stepIndex == totalSteps - 1
        )

        // Distance & Predecessor Table
        Surface(
            color = Color(0xFF13131C),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, CardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 4.dp * scale, vertical = 3.dp * scale),
                verticalArrangement = Arrangement.spacedBy(2.dp * scale)
            ) {
                Text("TENTATIVE DISTANCE & PREDECESSOR TABLE:", color = BlueAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
                                modifier = Modifier.padding(horizontal = 2.dp * scale, vertical = 1.dp * scale),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(v, color = OrangeAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("dist: ${dVal ?: "∞"}", color = GreenAccent, fontSize = 9.sp)
                                Text("pred: $pVal", color = BlueAccent, fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }

        DijkstraStablePriorityQueue(
            items = step.priorityQueue.map { "(${it.first}, ${it.second})" },
            allItems = allPriorityQueues
        )
    }
}

@Composable
private fun DijkstraStableHeader(
    step: DijkstraTraceStep,
    allBadgeTexts: List<String>
) {
    SubcomposeLayout(modifier = Modifier.fillMaxWidth()) { constraints ->
        val childConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val reservedHeight = allBadgeTexts.mapIndexed { index, badgeText ->
            subcompose("header-measure-$index") {
                DijkstraHeaderContent(badgeText = badgeText, badgeColor = BlueAccent)
            }.first().measure(childConstraints).height
        }.maxOrNull() ?: 0
        val current = subcompose("header-current") {
            DijkstraHeaderContent(
                badgeText = step.badgeText,
                badgeColor = when (step.event) {
                    DijkstraStepEvent.DIJKSTRA_COMPLETE -> GreenAccent
                    DijkstraStepEvent.RELAX_EDGE -> OrangeAccent
                    else -> BlueAccent
                }
            )
        }.first().measure(childConstraints)

        layout(constraints.maxWidth, maxOf(reservedHeight, current.height)) {
            current.place(0, 0)
        }
    }
}

@Composable
private fun DijkstraHeaderContent(
    badgeText: String,
    badgeColor: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text("CURRENT STEP", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(badgeText, color = badgeColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text(
            "Non-negative Weights: O((V+E) log V)",
            color = GreenAccent,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DijkstraStablePriorityQueue(
    items: List<String>,
    allItems: List<List<String>>
) {
    SubcomposeLayout(modifier = Modifier.fillMaxWidth()) { constraints ->
        val childConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val reservedHeight = allItems.mapIndexed { index, queueItems ->
            subcompose("queue-measure-$index") {
                DijkstraPriorityQueueCard(items = queueItems)
            }.first().measure(childConstraints).height
        }.maxOrNull() ?: 0
        val current = subcompose("queue-current") {
            DijkstraPriorityQueueCard(items = items)
        }.first().measure(childConstraints)

        layout(constraints.maxWidth, maxOf(reservedHeight, current.height)) {
            current.place(0, 0)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DijkstraPriorityQueueCard(items: List<String>) {
    Surface(
        color = Color(0xFF13131C),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text("PRIORITY QUEUE (MIN DISTANCE ENTRY):", color = OrangeAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            if (items.isEmpty()) {
                Text("Empty", color = TextSecondary, fontSize = 9.sp)
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    items.forEach { item ->
                        Surface(
                            color = Color(0xFF122838),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, OrangeAccent)
                        ) {
                            Text(
                                item,
                                color = OrangeAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
