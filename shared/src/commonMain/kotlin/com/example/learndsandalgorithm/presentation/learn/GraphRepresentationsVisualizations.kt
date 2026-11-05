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
internal fun GraphRepresentationsVisualizationCard(
    title: String = "Graph Representations"
) {
    var selectedView by rememberSaveable { mutableStateOf(RepresentationView.ADJACENCY_LIST) }
    val steps = GraphRepresentationsTrace.stepsForView(selectedView)
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val playback = rememberVisualizerPlaybackState(
        stepCount = steps.size,
        initialStep = 0,
        key = selectedView
    )
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val step = steps[stepIndex]

    ArrayWindowButton(text = "Open graph representations visualizer") {
        isExpanded = true
    }

    if (isExpanded) {
        val statusText = when (step.view) {
            RepresentationView.ADJACENCY_LIST -> "ADJACENCY LIST (Space: O(V + E))"
            RepresentationView.ADJACENCY_MATRIX -> "ADJACENCY MATRIX (Space: O(V²))"
            RepresentationView.COMPARISON -> "DATA STRUCTURE TRADE-OFF COMPARISON"
        }

        val statusColor = when (step.view) {
            RepresentationView.ADJACENCY_LIST -> GreenAccent
            RepresentationView.ADJACENCY_MATRIX -> BlueAccent
            RepresentationView.COMPARISON -> OrangeAccent
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
                RepresentationViewSelector(
                    selectedView = selectedView,
                    onViewSelected = { newView ->
                        if (newView != selectedView) {
                            playback.reset()
                            selectedView = newView
                        }
                    }
                )
            },
            visualContent = {
                FitToPane(modifier = Modifier.fillMaxSize()) {
                    GraphRepresentationsVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                GraphRepresentationsDetailsAndStepsContent(
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
private fun RepresentationViewSelector(
    selectedView: RepresentationView,
    onViewSelected: (RepresentationView) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 6.dp)
    ) {
        listOf(
            RepresentationView.ADJACENCY_LIST to "1. Adjacency List",
            RepresentationView.ADJACENCY_MATRIX to "2. Adjacency Matrix",
            RepresentationView.COMPARISON to "3. Comparison"
        ).forEach { (view, label) ->
            val isSelected = selectedView == view
            Surface(
                color = if (isSelected) Color(0xFF2A1C12) else Color(0xFF22222E),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (isSelected) OrangeAccent else CardBorderColor),
                modifier = Modifier
                    .heightIn(min = 38.dp)
                    .clickable { onViewSelected(view) }
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) OrangeAccent else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun GraphRepresentationsVisualizationContent(step: GraphRepresentationsTraceStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        when (step.view) {
            RepresentationView.ADJACENCY_LIST -> {
                Surface(
                    color = Color(0xFF13131C),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CardBorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp * scale),
                        verticalArrangement = Arrangement.spacedBy(6.dp * scale)
                    ) {
                        Text("ADJACENCY LIST (NEIGHBORS):", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        step.adjList.forEach { (vertex, neighbors) ->
                            val isInspected = vertex == step.inspectedVertex
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp * scale)
                            ) {
                                Surface(
                                    color = if (isInspected) Color(0xFF2A1C12) else Color(0xFF1F1F2C),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, if (isInspected) OrangeAccent else CardBorderColor)
                                ) {
                                    Text(
                                        text = "$vertex:",
                                        color = if (isInspected) OrangeAccent else TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp * scale, vertical = 4.dp * scale)
                                    )
                                }
                                Text("→ [${neighbors.joinToString(", ")}]", color = BlueAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            RepresentationView.ADJACENCY_MATRIX -> {
                Surface(
                    color = Color(0xFF13131C),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CardBorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp * scale),
                        verticalArrangement = Arrangement.spacedBy(4.dp * scale)
                    ) {
                        Text("ADJACENCY MATRIX (5x5 GRID):", color = BlueAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                        // Column headers
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Text(" ", fontSize = 10.sp, modifier = Modifier.weight(1f))
                            step.vertices.forEach { v ->
                                Text(v, color = OrangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            }
                        }

                        // Matrix rows
                        step.adjMatrix.forEachIndexed { rowIdx, row ->
                            val vLabel = step.vertices[rowIdx]
                            val isInspected = vLabel == step.inspectedVertex
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                                Text(vLabel, color = OrangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                row.forEach { valCell ->
                                    Surface(
                                        color = if (isInspected && valCell == 1) Color(0xFF2A1C12) else if (valCell == 1) Color(0xFF122838) else Color(0xFF181824),
                                        shape = RoundedCornerShape(4.dp),
                                        border = BorderStroke(1.dp, if (isInspected && valCell == 1) OrangeAccent else CardBorderColor),
                                        modifier = Modifier.weight(1f).padding(1.dp * scale)
                                    ) {
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 4.dp * scale)) {
                                            Text("$valCell", color = if (valCell == 1) BlueAccent else TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            RepresentationView.COMPARISON -> {
                Surface(
                    color = Color(0xFF13131C),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CardBorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp * scale),
                        verticalArrangement = Arrangement.spacedBy(6.dp * scale)
                    ) {
                        Text("DATA STRUCTURE COMPARISON MATRIX:", color = OrangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Property", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("Adjacency List", color = GreenAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                            Text("Adjacency Matrix", color = BlueAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                        }
                        ComparisonRow("Space", "O(V + E) Optimal", "O(V²) Dense")
                        ComparisonRow("Edge Lookup (u,v)", "O(degree(u))", "O(1) Constant")
                        ComparisonRow("List Neighbors", "O(degree(u))", "O(V) Row Scan")
                    }
                }
            }
        }
    }
}

@Composable
private fun ComparisonRow(property: String, listVal: String, matrixVal: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(property, color = TextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text(listVal, color = GreenAccent, fontSize = 9.sp, modifier = Modifier.weight(1.2f))
        Text(matrixVal, color = BlueAccent, fontSize = 9.sp, modifier = Modifier.weight(1.2f))
    }
}

@Composable
private fun GraphRepresentationsDetailsAndStepsContent(
    step: GraphRepresentationsTraceStep,
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
                Text("CURRENT REPRESENTATION", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.view) {
                        RepresentationView.ADJACENCY_LIST -> GreenAccent
                        RepresentationView.ADJACENCY_MATRIX -> BlueAccent
                        RepresentationView.COMPARISON -> OrangeAccent
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Graph G = (V, E)", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
