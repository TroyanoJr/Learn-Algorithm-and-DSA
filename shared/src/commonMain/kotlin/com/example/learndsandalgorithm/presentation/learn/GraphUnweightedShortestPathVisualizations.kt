package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.SubcomposeLayout

private val OrangeAccent = Color(0xFFFA6E13)
private val BlueAccent = Color(0xFF38BDF8)
private val GreenAccent = Color(0xFF10B981)
private val TextSecondary = Color(0xFF9CA3AF)

@Composable
internal fun GraphUnweightedShortestPathVisualizationCard(
    title: String = "Shortest Paths in Unweighted Graphs"
) {
    val steps = remember { GraphUnweightedShortestPathTrace.stepsForShortestPath() }
    val scenes = remember(steps) { GraphSceneBuilder.buildShortestPathScenes(steps) }
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
        ArrayWindowButton(text = "Open unweighted shortest path visualizer") {
            isExpanded = true
        }
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
                GraphSceneRenderer(scene = scene, modifier = Modifier.fillMaxSize())
            },
            detailsScrollable = false,
            detailsContent = {
                FitToPane(modifier = Modifier.fillMaxSize(), fitMaxScale = 1f) {
                    GraphUnweightedShortestPathDetailsAndStepsContent(
                        step = step,
                        stepIndex = stepIndex,
                        totalSteps = steps.size,
                        allExplanations = steps.map { it.explanation },
                        allBadgeTexts = steps.map { it.badgeText },
                        reconstructedPathForMeasurement = steps.lastOrNull { it.reconstructedPath != null }?.reconstructedPath
                            ?: emptyList(),
                        scale = 1f
                    )
                }
            }
        )
    }
}

@Composable
private fun GraphUnweightedShortestPathDetailsAndStepsContent(
    step: UnweightedShortestPathTraceStep,
    stepIndex: Int,
    totalSteps: Int,
    allExplanations: List<String>,
    allBadgeTexts: List<String>,
    reconstructedPathForMeasurement: List<String>,
    scale: Float
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val density = LocalDensity.current
        val textMeasurer = rememberTextMeasurer()
        val badgeStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold)
        val labelStyle = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold)
        val complexityStyle = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold)
        val textWidthPx = with(density) { maxWidth.roundToPx().coerceAtLeast(1) }
        val badgeHeightPx = remember(allBadgeTexts, textWidthPx, density.density, density.fontScale) {
            allBadgeTexts.maxOf { badge: String ->
                textMeasurer.measure(
                    text = badge,
                    style = badgeStyle,
                    constraints = Constraints(maxWidth = textWidthPx)
                ).size.height
            }
        }
        val headerHeightPx = remember(textWidthPx, badgeHeightPx, density.density, density.fontScale) {
            val labelHeight = textMeasurer.measure(
                text = "CURRENT STEP",
                style = labelStyle,
                constraints = Constraints(maxWidth = textWidthPx)
            ).size.height
            val complexityHeight = textMeasurer.measure(
                text = "Fewest Edges: O(V + E)",
                style = complexityStyle,
                constraints = Constraints(maxWidth = textWidthPx)
            ).size.height
            labelHeight + badgeHeightPx + complexityHeight + with(density) { 4.dp.roundToPx() }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp * scale)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().height(with(density) { headerHeightPx.toDp() }),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
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
                Text("Fewest Edges: O(V + E)", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

        Surface(
            color = Color(0xFF13131C),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFF262634)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 4.dp * scale, vertical = 3.dp * scale),
                verticalArrangement = Arrangement.spacedBy(2.dp * scale)
            ) {
                Text(
                    "DISTANCE & PREDECESSOR TABLE:",
                    color = BlueAccent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    step.distances.forEach { (vertex, distance) ->
                        val predecessor = step.predecessors[vertex] ?: "—"
                        val isCurrent = vertex == step.currentVertex
                        Surface(
                            color = if (isCurrent) Color(0xFF2A1C12) else Color(0xFF181824),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isCurrent) OrangeAccent else Color(0xFF262634)
                            ),
                            modifier = Modifier.weight(1f).padding(horizontal = 1.dp * scale)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 2.dp * scale, vertical = 1.dp * scale),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(vertex, color = OrangeAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("dist: ${distance ?: "∞"}", color = GreenAccent, fontSize = 9.sp)
                                Text("pred: $predecessor", color = BlueAccent, fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }

        ReconstructedShortestPathRegion(
            path = step.reconstructedPath,
            measuredPath = reconstructedPathForMeasurement,
            scale = scale
        )

        GraphStepsCard(
            label = "Steps",
            explanation = step.explanation,
            allExplanations = allExplanations,
            stepIndex = stepIndex,
            totalSteps = totalSteps,
            isCompleted = stepIndex == totalSteps - 1
        )
        }
    }
}

@Composable
private fun ReconstructedShortestPathRegion(
    path: List<String>?,
    measuredPath: List<String>,
    scale: Float
) {
    SubcomposeLayout(modifier = Modifier.fillMaxWidth()) { constraints ->
        val card = subcompose("reconstructed-shortest-path") {
            Surface(
                color = Color(0xFF13131C),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, GreenAccent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 7.dp * scale, vertical = 4.dp * scale),
                    verticalArrangement = Arrangement.spacedBy(2.dp * scale)
                ) {
                    Text(
                        "RECONSTRUCTED SHORTEST PATH:",
                        color = GreenAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        measuredPath.joinToString(" - "),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }.first().measure(constraints.copy(minWidth = 0, minHeight = 0))

        layout(constraints.maxWidth, card.height) {
            if (path != null) card.place(0, 0)
        }
    }
}
