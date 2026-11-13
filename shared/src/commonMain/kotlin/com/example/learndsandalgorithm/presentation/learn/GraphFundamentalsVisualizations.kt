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
internal fun GraphFundamentalsVisualizationCard(
    title: String = "Graph Fundamentals and Terminology"
) {
    var selectedTopic by rememberSaveable { mutableStateOf(GraphConceptTopic.VERTICES_AND_EDGES) }
    val steps = remember(selectedTopic) { GraphFundamentalsTrace.stepsForTopic(selectedTopic) }
    val scenes = remember(steps) { GraphSceneBuilder.buildFundamentalsScenes(steps) }
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
    val scene = scenes[stepIndex]

    if (!autoExpand) {
        ArrayWindowButton(text = "Open graph terminology visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.topic) {
            GraphConceptTopic.VERTICES_AND_EDGES -> "VERTICES (V) & EDGES (E)"
            GraphConceptTopic.DIRECTED_VS_UNDIRECTED -> "DIRECTED VS UNDIRECTED"
            GraphConceptTopic.VERTEX_DEGREE -> "VERTEX DEGREE"
            GraphConceptTopic.PATHS_AND_CYCLES -> "PATHS & CYCLES"
            GraphConceptTopic.CONNECTEDNESS -> "GRAPH CONNECTEDNESS"
        }

        val statusColor = when (step.topic) {
            GraphConceptTopic.VERTICES_AND_EDGES, GraphConceptTopic.CONNECTEDNESS -> GreenAccent
            GraphConceptTopic.PATHS_AND_CYCLES -> OrangeAccent
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
                GraphTopicSelector(
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
                GraphSceneRenderer(scene = scene, modifier = Modifier.fillMaxSize())
            },
            detailsContent = {
                GraphFundamentalsDetailsAndStepsContent(
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
private fun GraphTopicSelector(
    selectedTopic: GraphConceptTopic,
    onTopicSelected: (GraphConceptTopic) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 6.dp)
    ) {
        listOf(
            GraphConceptTopic.VERTICES_AND_EDGES to "1. Vertices & Edges",
            GraphConceptTopic.DIRECTED_VS_UNDIRECTED to "2. Directed vs Undirected",
            GraphConceptTopic.VERTEX_DEGREE to "3. Degree",
            GraphConceptTopic.PATHS_AND_CYCLES to "4. Paths & Cycles",
            GraphConceptTopic.CONNECTEDNESS to "5. Connectedness"
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
private fun GraphFundamentalsDetailsAndStepsContent(
    step: GraphFundamentalsTraceStep,
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
                Text("CURRENT CONCEPT", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.topic) {
                        GraphConceptTopic.VERTICES_AND_EDGES, GraphConceptTopic.CONNECTEDNESS -> GreenAccent
                        GraphConceptTopic.PATHS_AND_CYCLES -> OrangeAccent
                        else -> BlueAccent
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Graph Topology G=(V,E)", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
