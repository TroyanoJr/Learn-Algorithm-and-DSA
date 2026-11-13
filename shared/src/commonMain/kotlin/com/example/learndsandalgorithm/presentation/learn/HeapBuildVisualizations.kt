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

@Composable
internal fun HeapBuildVisualizationCard(
    title: String = "Building a Heap",
    useSceneArchitecture: Boolean = true
) {
    val steps = HeapBuildTrace.stepsForBuild()
    val scenes = remember(steps) { HeapSceneBuilder.buildScenes(steps) }
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
    val currentScene = if (scenes.isNotEmpty() && stepIndex in scenes.indices) scenes[stepIndex] else null

    if (!autoExpand) {
        ArrayWindowButton(text = "Open build heap visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.event) {
            BuildHeapEvent.START -> "READY · BOTTOM-UP BUILD HEAP (O(N) TIME)"
            BuildHeapEvent.START_INDEX_SIFT -> "PROCESSING PARENT INDEX ${step.activeParentIndex}"
            BuildHeapEvent.COMPARE_CHILDREN -> "COMPARING WITH SMALLER CHILD AT INDEX ${step.smallerChildIndex}"
            BuildHeapEvent.SWAP_DOWN -> "SIFTING DOWN: SWAPPED WITH CHILD"
            BuildHeapEvent.INDEX_SIFT_DONE -> "INDEX ${step.activeParentIndex} SIFT-DOWN DONE"
            BuildHeapEvent.BUILD_COMPLETE -> "BUILD HEAP COMPLETE"
        }

        val statusColor = when (step.event) {
            BuildHeapEvent.BUILD_COMPLETE -> GreenAccent
            BuildHeapEvent.SWAP_DOWN, BuildHeapEvent.START_INDEX_SIFT -> OrangeAccent
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
                if (useSceneArchitecture && currentScene != null) {
                    HeapSceneRenderer(
                        scene = currentScene,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    FitToPane(modifier = Modifier.fillMaxSize()) {
                        HeapBuildVisualizationContent(step = step, scale = 1f)
                    }
                }
            },
            detailsContent = {
                HeapBuildDetailsAndStepsContent(
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
private fun HeapBuildVisualizationContent(step: BuildHeapTraceStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Build Status Header Card
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
                        text = "BOTTOM-UP BUILD HEAP (N = ${step.array.size})",
                        color = OrangeAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Start at floor(N/2)-1 = ${step.array.size / 2 - 1} down to 0. Total Cost = O(N)",
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
                        text = step.event.name,
                        color = OrangeAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp * scale, vertical = 4.dp * scale)
                    )
                }
            }
        }

        // Tree View
        Surface(
            color = Color(0xFF13131C),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, CardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(10.dp * scale).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp * scale)
            ) {
                Text("MIN-HEAP TREE VIEW:", color = BlueAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                // Level 0: [0]
                if (step.array.isNotEmpty()) {
                    val isSift = step.currentSiftIndex == 0
                    val isChild = step.smallerChildIndex == 0
                    val isSwapped = step.swappedIndices != null && (step.swappedIndices.first == 0 || step.swappedIndices.second == 0)
                    HeapTreeNode(step.array[0], 0, isHighlighted = isSift, isParent = isChild, isSwapped = isSwapped)
                    TreeBranchConnector(label = "↙     ↘")
                }

                // Level 1: [1], [2]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (step.array.size > 1) {
                        val isSift = step.currentSiftIndex == 1
                        val isChild = step.smallerChildIndex == 1
                        val isSwapped = step.swappedIndices != null && (step.swappedIndices.first == 1 || step.swappedIndices.second == 1)
                        HeapTreeNode(step.array[1], 1, isHighlighted = isSift, isParent = isChild, isSwapped = isSwapped)
                    }
                    if (step.array.size > 2) {
                        val isSift = step.currentSiftIndex == 2
                        val isChild = step.smallerChildIndex == 2
                        val isSwapped = step.swappedIndices != null && (step.swappedIndices.first == 2 || step.swappedIndices.second == 2)
                        HeapTreeNode(step.array[2], 2, isHighlighted = isSift, isParent = isChild, isSwapped = isSwapped)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TreeBranchConnector(label = "↙   ↘")
                    TreeBranchConnector(label = "↙")
                }

                // Level 2: [3], [4], [5]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    (3..5).forEach { idx ->
                        if (step.array.size > idx) {
                            val isSift = step.currentSiftIndex == idx
                            val isChild = step.smallerChildIndex == idx
                            val isSwapped = step.swappedIndices != null && (step.swappedIndices.first == idx || step.swappedIndices.second == idx)
                            HeapTreeNode(step.array[idx], idx, isHighlighted = isSift, isParent = isChild, isSwapped = isSwapped)
                        }
                    }
                }
            }
        }

        // Array Row Component
        HeapArrayRow(
            array = step.array,
            highlightedIndex = step.currentSiftIndex,
            parentIndex = step.smallerChildIndex,
            swappedIndices = step.swappedIndices
        )
    }
}

@Composable
private fun HeapBuildDetailsAndStepsContent(
    step: BuildHeapTraceStep,
    stepIndex: Int,
    totalSteps: Int,
    scale: Float
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp * scale)
    ) {
        Surface(
            color = Color(0xFF161622),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("CURRENT OPERATION", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Surface(
                        color = Color(0xFF22222E),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Text(
                            text = "Complexity: O(N) Linear Time",
                            color = GreenAccent,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = step.badgeText,
                    color = when (step.event) {
                        BuildHeapEvent.BUILD_COMPLETE -> GreenAccent
                        BuildHeapEvent.SWAP_DOWN, BuildHeapEvent.START_INDEX_SIFT -> OrangeAccent
                        else -> BlueAccent
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 18.sp
                )
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
