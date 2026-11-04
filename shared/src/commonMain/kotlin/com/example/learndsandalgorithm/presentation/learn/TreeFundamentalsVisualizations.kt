package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
internal fun TreeFundamentalsVisualizationCard(
    title: String = "Tree Fundamentals and Terminology"
) {
    val steps = TreeFundamentalsTrace.stepsForFundamentals()
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val playback = rememberVisualizerPlaybackState(
        stepCount = steps.size,
        initialStep = 0
    )
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val step = steps[stepIndex]

    ArrayWindowButton(text = "Open tree terminology visualizer") {
        isExpanded = true
    }

    if (isExpanded) {
        val statusText = when (step.concept) {
            TreeConcept.ROOT -> "ROOT NODE"
            TreeConcept.PARENT_CHILD -> "PARENT & CHILD RELATIONSHIPS"
            TreeConcept.SIBLINGS -> "SIBLING NODES"
            TreeConcept.LEAVES -> "LEAF NODES (TERMINAL)"
            TreeConcept.SUBTREE -> "SUBTREE STRUCTURE"
            TreeConcept.DEPTH_AND_HEIGHT -> "DEPTH & HEIGHT MEASUREMENTS"
        }

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
                FitToPane(modifier = Modifier.fillMaxSize()) {
                    TreeFundamentalsVisualizationContent(step = step, scale = 1f)
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

        // Tree Diagram Layout
        Surface(
            color = Color(0xFF13131C),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, CardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp * scale).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp * scale)
            ) {
                // Level 0: Root "A"
                TreeNodeCircle(
                    value = "A",
                    isHighlighted = "A" in step.highlightedNodeIds,
                    badgeLabel = "Root (d=0, h=2)"
                )

                TreeBranchConnector(label = "↙     ↓     ↘")

                // Level 1: "B", "C", "D"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TreeNodeCircle(
                        value = "B",
                        isHighlighted = "B" in step.highlightedNodeIds,
                        badgeLabel = "B (d=1, h=1)"
                    )
                    TreeNodeCircle(
                        value = "C",
                        isHighlighted = "C" in step.highlightedNodeIds,
                        badgeLabel = "C (d=1, h=1)"
                    )
                    TreeNodeCircle(
                        value = "D",
                        isHighlighted = "D" in step.highlightedNodeIds,
                        badgeLabel = "D (Leaf)"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TreeBranchConnector(label = "↙   ↘")
                    TreeBranchConnector(label = "↓")
                    Text(" ", fontSize = 10.sp)
                }

                // Level 2: "E", "F", "G"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TreeNodeCircle(
                        value = "E",
                        isHighlighted = "E" in step.highlightedNodeIds,
                        badgeLabel = "E (Leaf)"
                    )
                    TreeNodeCircle(
                        value = "F",
                        isHighlighted = "F" in step.highlightedNodeIds,
                        badgeLabel = "F (Leaf)"
                    )
                    TreeNodeCircle(
                        value = "G",
                        isHighlighted = "G" in step.highlightedNodeIds,
                        badgeLabel = "G (Leaf, d=2)"
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
                    color = when (step.concept) {
                        TreeConcept.ROOT, TreeConcept.SUBTREE -> OrangeAccent
                        TreeConcept.LEAVES, TreeConcept.DEPTH_AND_HEIGHT -> GreenAccent
                        else -> BlueAccent
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Tree Measurements: Depth & Height", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
