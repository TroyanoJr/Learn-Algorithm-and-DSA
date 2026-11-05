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
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9CA3AF)

@Composable
internal fun GraphFundamentalsVisualizationCard(
    title: String = "Graph Fundamentals and Terminology"
) {
    var selectedTopic by rememberSaveable { mutableStateOf(GraphConceptTopic.VERTICES_AND_EDGES) }
    val steps = GraphFundamentalsTrace.stepsForTopic(selectedTopic)
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val playback = rememberVisualizerPlaybackState(
        stepCount = steps.size,
        initialStep = 0,
        key = selectedTopic
    )
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val step = steps[stepIndex]

    ArrayWindowButton(text = "Open graph terminology visualizer") {
        isExpanded = true
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
                FitToPane(modifier = Modifier.fillMaxSize()) {
                    GraphFundamentalsVisualizationContent(step = step, scale = 1f)
                }
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
private fun GraphFundamentalsVisualizationContent(step: GraphFundamentalsTraceStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Concept Badge Header Card
        Surface(
            color = Color(0xFF13131C),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, CardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp * scale).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp * scale)) {
                    Text(
                        text = "GRAPH TOPOLOGY: {A, B, C, D, E}",
                        color = OrangeAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Edges: {AB, AC, BD, CD, DE} | Vertices = 5, Edges = 5",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }

                Surface(
                    color = Color(0xFF2A1C12),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, OrangeAccent)
                ) {
                    Text(
                        text = step.topic.name,
                        color = OrangeAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp * scale, vertical = 4.dp * scale)
                    )
                }
            }
        }

        // Graph Layout Diagram
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
                // Layer 1: A, B, C
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    GraphVertexCircle("A", isActive = "A" in step.highlightedVertices)
                    Text(if (step.topic == GraphConceptTopic.DIRECTED_VS_UNDIRECTED) "→" else "—", color = BlueAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    GraphVertexCircle("B", isActive = "B" in step.highlightedVertices)
                    Text(if (step.topic == GraphConceptTopic.DIRECTED_VS_UNDIRECTED) "→" else "—", color = BlueAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    GraphVertexCircle("C", isActive = "C" in step.highlightedVertices)
                }

                // Layer 2: D, E
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    GraphVertexCircle("D", isActive = "D" in step.highlightedVertices, badgeText = if (step.degreeVal != null) "deg=3" else null)
                    Text("—", color = BlueAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    GraphVertexCircle("E", isActive = "E" in step.highlightedVertices)
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
