package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
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

internal fun treeFundamentalsConceptTitle(concept: TreeConcept): String = when (concept) {
    TreeConcept.ROOT -> "Root Node"
    TreeConcept.PARENT_CHILD -> "Parent & Child Relationships"
    TreeConcept.SIBLINGS -> "Sibling Nodes"
    TreeConcept.LEAVES -> "Leaf Nodes"
    TreeConcept.SUBTREE -> "Subtrees"
    TreeConcept.DEPTH_AND_HEIGHT -> "Tree Measurements: Depth & Height"
}

@Composable
internal fun TreeFundamentalsVisualizationCard(
    title: String = "Tree Fundamentals and Terminology"
) {
    val steps = TreeFundamentalsTrace.stepsForFundamentals()
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

    val fundamentalsScenes = remember {
        val s = TreeFundamentalsTrace.stepsForFundamentals()
        if (s.isNotEmpty()) {
            TreeFundamentalsSceneBuilder.buildFundamentalsScenes(s)
        } else {
            emptyList()
        }
    }

    if (!autoExpand) {
        ArrayWindowButton(text = "Open tree terminology visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = treeFundamentalsConceptTitle(step.concept)

        val statusColor = when (step.concept) {
            TreeConcept.ROOT, TreeConcept.SUBTREE -> OrangeAccent
            TreeConcept.LEAVES, TreeConcept.DEPTH_AND_HEIGHT -> GreenAccent
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
                val currentScene = if (stepIndex in fundamentalsScenes.indices) fundamentalsScenes[stepIndex] else null
                if (currentScene != null) {
                    TreeSceneRenderer(
                        scene = currentScene,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    FitToPane(modifier = Modifier.fillMaxSize()) {
                        TreeFundamentalsVisualizationContent(step = step, scale = 1f)
                    }
                }
            },
            detailsContent = {
                TreeFundamentalsDetailsAndStepsContent(
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
private fun TreeFundamentalsVisualizationContent(step: TreeFundamentalsTraceStep, scale: Float) {
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
                        text = "TREE TERMINOLOGY & MEASUREMENTS",
                        color = OrangeAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Root Height = 2 | Tree Height = 2 | Total Nodes = 7",
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
                        text = step.concept.name,
                        color = OrangeAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp * scale, vertical = 4.dp * scale)
                    )
                }
            }
        }
    }
}

@Composable
private fun TreeFundamentalsDetailsAndStepsContent(
    step: TreeFundamentalsTraceStep,
    stepIndex: Int,
    totalSteps: Int,
    scale: Float
) {
    val root = step.nodes.first { it.parentId == null }
    val rootHeight = root.height
    val treeHeight = step.nodes.maxOf { it.height }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Surface(
            color = Color(0xFF161622),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("CURRENT CONCEPT", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Surface(
                        color = Color(0xFF22222E),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Text(
                            text = treeFundamentalsConceptTitle(step.concept),
                            color = GreenAccent,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = step.badgeText,
                    color = when (step.concept) {
                        TreeConcept.ROOT, TreeConcept.SUBTREE -> OrangeAccent
                        TreeConcept.LEAVES, TreeConcept.DEPTH_AND_HEIGHT -> GreenAccent
                        else -> BlueAccent
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 13.sp
                )
            }
        }

        Surface(
            color = Color(0xFF13131C),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, CardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "TREE TERMINOLOGY & MEASUREMENTS",
                    color = OrangeAccent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Root Height = $rootHeight | Tree Height = $treeHeight | Total Nodes = ${step.nodes.size}",
                    color = TextSecondary,
                    fontSize = 8.sp
                )
                step.nodes.chunked(2).forEach { pair ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        pair.forEach { node ->
                            Box(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = buildString {
                                        append("${node.id}: d${node.depth} · h${node.height}")
                                        if (node.parentId == null) append(" · Root")
                                        if (node.isLeaf) append(" · Leaf")
                                    },
                                    color = TextPrimary,
                                    fontSize = 8.sp,
                                    lineHeight = 9.sp
                                )
                            }
                        }
                        if (pair.size == 1) Box(Modifier.weight(1f))
                    }
                }
            }
        }

        Surface(color = Color(0xFF161622), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Steps", color = OrangeAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("STEP ${stepIndex + 1} / $totalSteps", color = if (stepIndex == totalSteps - 1) GreenAccent else TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Text(step.explanation, color = TextPrimary, fontSize = 12.sp, lineHeight = 15.sp)
            }
        }
    }
}
