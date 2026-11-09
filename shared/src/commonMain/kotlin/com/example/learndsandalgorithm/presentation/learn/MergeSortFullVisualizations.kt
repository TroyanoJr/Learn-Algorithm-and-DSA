package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
internal fun MergeSortFullVisualizationCard(
    title: String = "Merge Sort Implementation and Complexity"
) {
    val steps = MergeSortFullTrace.stepsForFullSort()
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

    if (!autoExpand) {
        ArrayWindowButton(text = "Open full merge sort visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.event) {
            FullMergeEvent.START -> "READY · FULL MERGE SORT"
            FullMergeEvent.DIVIDE -> "DIVIDING TO LEVEL ${step.activeLevel}"
            FullMergeEvent.BASE_CASES -> "BASE CASES REACHED (LENGTH 1)"
            FullMergeEvent.MERGE_LEVEL -> "MERGING LEVEL ${step.activeLevel}"
            FullMergeEvent.COMPLETE -> "COMPLETE"
        }

        val statusColor = when (step.event) {
            FullMergeEvent.COMPLETE -> GreenAccent
            FullMergeEvent.MERGE_LEVEL -> OrangeAccent
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
                    MergeSortFullVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                MergeSortFullDetailsAndStepsContent(
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
private fun MergeSortFullVisualizationContent(step: MergeSortFullTraceStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Multi-level Recursion Tree
        Surface(
            color = Color(0xFF13131C),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, CardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(8.dp * scale),
                verticalArrangement = Arrangement.spacedBy(6.dp * scale)
            ) {
                Text("RECURSION TREE (LOG N DEPTH):", color = OrangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                // Level 0
                TreeLevelRow("L0", step.level0, step.activeLevel == 0, scale)
                // Level 1
                if (step.level1.isNotEmpty()) TreeLevelRow("L1", step.level1, step.activeLevel == 1, scale)
                // Level 2
                if (step.level2.isNotEmpty()) TreeLevelRow("L2", step.level2, step.activeLevel == 2, scale)
                // Level 3 (Base Cases)
                if (step.level3.isNotEmpty()) TreeLevelRow("L3", step.level3, step.activeLevel == 3, scale)
            }
        }

        // Auxiliary Buffer Card (showing O(N) memory allocation)
        if (step.auxiliaryBuffer.isNotEmpty()) {
            Surface(
                color = Color(0xFF13131C),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, CardBorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(8.dp * scale),
                    verticalArrangement = Arrangement.spacedBy(4.dp * scale)
                ) {
                    Text("AUXILIARY MERGE BUFFER (O(N) Extra Memory):", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        step.auxiliaryBuffer.forEach { valItem ->
                            Surface(
                                color = Color(0xFF123820),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, GreenAccent),
                                modifier = Modifier.padding(end = 4.dp * scale)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp * scale, vertical = 4.dp * scale)) {
                                    Text("$valItem", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TreeLevelRow(label: String, nodes: List<MergeTreeNode>, isActive: Boolean, scale: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = if (isActive) OrangeAccent else TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 4.dp * scale))
        nodes.forEach { node ->
            Surface(
                color = when {
                    isActive && node.isSorted -> Color(0xFF123820)
                    isActive -> Color(0xFF2A1C12)
                    node.isSorted -> Color(0xFF13283A)
                    else -> Color(0xFF181824)
                },
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, when {
                    isActive && node.isSorted -> GreenAccent
                    isActive -> OrangeAccent
                    node.isSorted -> BlueAccent
                    else -> CardBorderColor
                }),
                modifier = Modifier.padding(horizontal = 2.dp * scale)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 6.dp * scale, vertical = 4.dp * scale)) {
                    Text(
                        text = "[${node.elements.joinToString(",")}]",
                        color = if (node.isSorted) GreenAccent else TextPrimary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun MergeSortFullDetailsAndStepsContent(
    step: MergeSortFullTraceStep,
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
                Text("CURRENT OPERATION", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.event) {
                        FullMergeEvent.COMPLETE -> GreenAccent
                        FullMergeEvent.MERGE_LEVEL -> OrangeAccent
                        else -> BlueAccent
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Time: O(N log N) Guaranteed", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("Space: O(N) Auxiliary Buffer", color = BlueAccent, fontSize = 9.sp)
            }
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
