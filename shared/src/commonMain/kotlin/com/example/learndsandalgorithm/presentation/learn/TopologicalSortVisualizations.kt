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
import androidx.compose.ui.unit.Constraints
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
                TopologicalSortDetailsAndStepsContent(
                    step = step,
                    stepIndex = stepIndex,
                    totalSteps = steps.size,
                    allExplanations = steps.map { it.explanation },
                    allBadgeTexts = steps.map { it.badgeText },
                    allQueues = steps.map { it.queue },
                    allTopoOrders = steps.map { it.topoOrder },
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
    allExplanations: List<String>,
    allBadgeTexts: List<String>,
    allQueues: List<List<String>>,
    allTopoOrders: List<List<String>>,
    scale: Float
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(3.dp * scale)
    ) {
        TopologicalStableHeader(step = step, allBadgeTexts = allBadgeTexts)

        GraphStepsCard(
            label = "Steps",
            explanation = step.explanation,
            allExplanations = allExplanations,
            stepIndex = stepIndex,
            totalSteps = totalSteps,
            isCompleted = stepIndex == totalSteps - 1
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp * scale),
            verticalAlignment = Alignment.Top
        ) {
            TopologicalStableQueue(
                items = step.queue,
                allItems = allQueues,
                modifier = Modifier.weight(1f)
            )
            TopologicalStableOrder(
                order = step.topoOrder,
                allOrders = allTopoOrders,
                modifier = Modifier.weight(1f)
            )
        }

        // In-Degrees Table
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
                Text("CURRENT IN-DEGREES:", color = OrangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
                                modifier = Modifier.padding(horizontal = 2.dp * scale, vertical = 1.dp * scale),
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

    }
}

@Composable
private fun TopologicalStableHeader(
    step: TopologicalSortTraceStep,
    allBadgeTexts: List<String>
) {
    SubcomposeLayout(modifier = Modifier.fillMaxWidth()) { constraints ->
        val childConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val reservedHeight = allBadgeTexts.mapIndexed { index, badgeText ->
            subcompose("header-measure-$index") {
                TopologicalHeaderContent(badgeText = badgeText, badgeColor = BlueAccent)
            }.first().measure(childConstraints).height
        }.maxOrNull() ?: 0
        val current = subcompose("header-current") {
            TopologicalHeaderContent(
                badgeText = step.badgeText,
                badgeColor = when (step.event) {
                    TopoSortEvent.TOPO_COMPLETE -> GreenAccent
                    TopoSortEvent.DEQUEUE_ZERO_INDEGREE -> OrangeAccent
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
private fun TopologicalHeaderContent(
    badgeText: String,
    badgeColor: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("CURRENT STEP", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text("Kahn's Cost: O(V + E) Time", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Text(badgeText, color = badgeColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TopologicalStableQueue(
    items: List<String>,
    allItems: List<List<String>>,
    modifier: Modifier = Modifier
) {
    SubcomposeLayout(modifier = modifier.fillMaxWidth()) { constraints ->
        val childConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val reservedHeight = allItems.mapIndexed { index, queueItems ->
            subcompose("queue-measure-$index") {
                TopologicalQueueCard(items = queueItems)
            }.first().measure(childConstraints).height
        }.maxOrNull() ?: 0
        val current = subcompose("queue-current") {
            TopologicalQueueCard(items = items)
        }.first().measure(childConstraints)

        layout(constraints.maxWidth, maxOf(reservedHeight, current.height)) {
            current.place(0, 0)
        }
    }
}

@Composable
private fun TopologicalStableOrder(
    order: List<String>,
    allOrders: List<List<String>>,
    modifier: Modifier = Modifier
) {
    SubcomposeLayout(modifier = modifier.fillMaxWidth()) { constraints ->
        val childConstraints = constraints.copy(
            minWidth = 0,
            minHeight = 0,
            maxHeight = Constraints.Infinity
        )
        val reservedHeight = allOrders.mapIndexed { index, topoOrder ->
            subcompose("order-measure-$index") {
                TopologicalOrderCard(order = topoOrder)
            }.first().measure(childConstraints).height
        }.maxOrNull() ?: 0
        val current = subcompose("order-current") {
            TopologicalOrderCard(order = order)
        }.first().measure(childConstraints)

        layout(constraints.maxWidth, maxOf(reservedHeight, current.height)) {
            current.place(0, 0)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TopologicalQueueCard(items: List<String>) {
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
            Text("ZERO IN-DEGREE READY QUEUE:", color = BlueAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
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
                            border = BorderStroke(1.dp, BlueAccent)
                        ) {
                            Text(
                                item,
                                color = BlueAccent,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TopologicalOrderCard(order: List<String>) {
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
            Text("TOPOLOGICAL SCHEDULE ORDER (DAG):", color = GreenAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            if (order.isEmpty()) {
                Text("No vertices scheduled yet", color = TextSecondary, fontSize = 9.sp)
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    order.forEach { vertex ->
                        Surface(
                            color = Color(0xFF123820),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, GreenAccent)
                        ) {
                            Text(
                                vertex,
                                color = GreenAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
