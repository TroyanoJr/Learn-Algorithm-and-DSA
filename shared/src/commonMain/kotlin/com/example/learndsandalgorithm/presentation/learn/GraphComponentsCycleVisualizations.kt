package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
private val RedAccent = Color(0xFFEF4444)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9CA3AF)

@Composable
internal fun GraphComponentsCycleVisualizationCard(
    title: String = "Connected Components and Cycle Detection"
) {
    var selectedTopic by rememberSaveable { mutableStateOf(ComponentCycleTopic.CONNECTED_COMPONENTS) }
    val steps = GraphComponentsCycleTrace.stepsForTopic(selectedTopic)
    val autoExpand = LocalAutoExpandVisualizer.current
    var isExpanded by rememberSaveable { mutableStateOf(autoExpand) }

    val playback = rememberVisualizerPlaybackState(
        stepCount = steps.size,
        initialStep = 0,
        key = selectedTopic
    )
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val step = steps[stepIndex]

    if (!autoExpand) {
        ArrayWindowButton(text = "Open components & cycle visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.topic) {
            ComponentCycleTopic.CONNECTED_COMPONENTS -> "CONNECTED COMPONENTS DISCOVERY"
            ComponentCycleTopic.UNDIRECTED_CYCLE -> if (step.cycleDetected) "UNDIRECTED CYCLE DETECTED!" else "UNDIRECTED CYCLE CHECK"
            ComponentCycleTopic.DIRECTED_CYCLE -> if (step.cycleDetected) "DIRECTED BACK-EDGE CYCLE DETECTED!" else "DIRECTED CYCLE CHECK"
        }

        val statusColor = when {
            step.cycleDetected -> RedAccent
            step.topic == ComponentCycleTopic.CONNECTED_COMPONENTS -> GreenAccent
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
            topContent = {
                ComponentTopicSelector(
                    selectedTopic = selectedTopic,
                    onTopicSelected = { newTopic ->
                        if (newTopic != selectedTopic) {
                            playback.reset()
                            selectedTopic = newTopic
                        }
                    }
                )
            },
            visualContent = {
                FitToPane(modifier = Modifier.fillMaxSize()) {
                    GraphComponentsCycleVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                GraphComponentsCycleDetailsAndStepsContent(
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
private fun ComponentTopicSelector(
    selectedTopic: ComponentCycleTopic,
    onTopicSelected: (ComponentCycleTopic) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 6.dp)
    ) {
        listOf(
            ComponentCycleTopic.CONNECTED_COMPONENTS to "1. Connected Components",
            ComponentCycleTopic.UNDIRECTED_CYCLE to "2. Undirected Cycle",
            ComponentCycleTopic.DIRECTED_CYCLE to "3. Directed Cycle"
        ).forEach { (topic, label) ->
            val isSelected = selectedTopic == topic
            Surface(
                color = if (isSelected) Color(0xFF2A1C12) else Color(0xFF22222E),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (isSelected) OrangeAccent else CardBorderColor),
                modifier = Modifier
                    .heightIn(min = 36.dp)
                    .clickable { onTopicSelected(topic) }
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) OrangeAccent else TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun GraphComponentsCycleVisualizationContent(step: ComponentCycleTraceStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Status Header Card
        Surface(
            color = Color(0xFF13131C),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, if (step.cycleDetected) RedAccent else CardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp * scale).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp * scale)) {
                    Text(
                        text = step.topic.name,
                        color = OrangeAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (step.topic == ComponentCycleTopic.CONNECTED_COMPONENTS) "Components Found: ${step.components.size}" else if (step.cycleDetected) "CYCLE DETECTED AT EDGE ${step.cycleEdges?.first}-${step.cycleEdges?.second}" else "Checking cycle invariant...",
                        color = if (step.cycleDetected) RedAccent else TextSecondary,
                        fontSize = 10.sp
                    )
                }

                if (step.cycleDetected) {
                    Surface(
                        color = Color(0xFF381212),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, RedAccent)
                    ) {
                        Text(
                            "CYCLE DETECTED",
                            color = RedAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp * scale, vertical = 4.dp * scale)
                        )
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
                if (step.topic == ComponentCycleTopic.CONNECTED_COMPONENTS) {
                    Text("UNDIRECTED COMPONENTS ({AB, BC}, {DE}, {F}):", color = BlueAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        GraphVertexCircle("A", isVisited = "A" in step.visited, badgeText = "Comp 1")
                        Text("—", color = BlueAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        GraphVertexCircle("B", isVisited = "B" in step.visited, badgeText = "Comp 1")
                        Text("—", color = BlueAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        GraphVertexCircle("C", isVisited = "C" in step.visited, badgeText = "Comp 1")
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        GraphVertexCircle("D", isVisited = "D" in step.visited, badgeText = "Comp 2")
                        Text("—", color = BlueAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        GraphVertexCircle("E", isVisited = "E" in step.visited, badgeText = "Comp 2")
                        GraphVertexCircle("F", isVisited = "F" in step.visited, badgeText = "Comp 3 (Isolated)")
                    }
                } else {
                    Text("TRIANGLE GRAPH {A, B, C}:", color = BlueAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val isA = step.currentVertex == "A"
                        val isB = step.currentVertex == "B"
                        val isC = step.currentVertex == "C"

                        GraphVertexCircle("A", isActive = isA, isVisited = "A" in step.visited, isCycle = step.cycleDetected && isA)
                        Text(if (step.topic == ComponentCycleTopic.DIRECTED_CYCLE) "→" else "—", color = BlueAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        GraphVertexCircle("B", isActive = isB, isVisited = "B" in step.visited, isCycle = step.cycleDetected && isB)
                        Text(if (step.topic == ComponentCycleTopic.DIRECTED_CYCLE) "→" else "—", color = BlueAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        GraphVertexCircle("C", isActive = isC, isVisited = "C" in step.visited, isCycle = step.cycleDetected && isC)
                    }
                }
            }
        }
    }
}

@Composable
private fun GraphComponentsCycleDetailsAndStepsContent(
    step: ComponentCycleTraceStep,
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
                Text("CURRENT CHECK", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = if (step.cycleDetected) RedAccent else GreenAccent,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Outer Loop: O(V + E)", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
