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
internal fun HeapInsertSiftUpVisualizationCard(
    title: String = "Heap Insertion and Sift Up",
    useSceneArchitecture: Boolean = true
) {
    val steps = remember { HeapInsertSiftUpTrace.stepsForInsert() }
    val scenes = remember(steps) { HeapSceneBuilder.buildInsertScenes(steps) }
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
        ArrayWindowButton(text = "Open heap insertion visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.event) {
            InsertSiftUpEvent.START -> "READY · INSERT $step.insertedValue"
            InsertSiftUpEvent.APPEND_VALUE -> "APPENDED $step.insertedValue AT INDEX ${step.currentIndex}"
            InsertSiftUpEvent.COMPARE_PARENT -> "COMPARING WITH PARENT AT INDEX ${step.parentIndex}"
            InsertSiftUpEvent.SWAP_UP -> "SIFTING UP: SWAPPED WITH PARENT"
            InsertSiftUpEvent.SIFT_UP_COMPLETE -> "INSERTION COMPLETE"
        }

        val statusColor = when (step.event) {
            InsertSiftUpEvent.SIFT_UP_COMPLETE -> GreenAccent
            InsertSiftUpEvent.SWAP_UP, InsertSiftUpEvent.APPEND_VALUE -> OrangeAccent
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
                        HeapInsertSiftUpVisualizationContent(step = step, scale = 1f)
                    }
                }
            },
            detailsContent = {
                HeapInsertSiftUpDetailsAndStepsContent(
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
private fun HeapInsertSiftUpVisualizationContent(step: InsertSiftUpTraceStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Insertion Status Header Card
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
                        text = "INSERTING VALUE: ${step.insertedValue}",
                        color = OrangeAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Current Index = ${step.currentIndex ?: "—"} | Parent Index = ${step.parentIndex ?: "—"}",
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

        // Tree Representation Box
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
                if (step.heap.isNotEmpty()) {
                    val isCurr = step.currentIndex == 0
                    val isParent = step.parentIndex == 0
                    val isSwapped = step.swappedIndices != null && (step.swappedIndices.first == 0 || step.swappedIndices.second == 0)
                    HeapTreeNode(step.heap[0], 0, isHighlighted = isCurr, isParent = isParent, isSwapped = isSwapped)
                    TreeBranchConnector(label = "↙     ↘")
                }

                // Level 1: [1], [2]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (step.heap.size > 1) {
                        val isCurr = step.currentIndex == 1
                        val isParent = step.parentIndex == 1
                        val isSwapped = step.swappedIndices != null && (step.swappedIndices.first == 1 || step.swappedIndices.second == 1)
                        HeapTreeNode(step.heap[1], 1, isHighlighted = isCurr, isParent = isParent, isSwapped = isSwapped)
                    }
                    if (step.heap.size > 2) {
                        val isCurr = step.currentIndex == 2
                        val isParent = step.parentIndex == 2
                        val isSwapped = step.swappedIndices != null && (step.swappedIndices.first == 2 || step.swappedIndices.second == 2)
                        HeapTreeNode(step.heap[2], 2, isHighlighted = isCurr, isParent = isParent, isSwapped = isSwapped)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TreeBranchConnector(label = "↙   ↘")
                    TreeBranchConnector(label = "↙   ↘")
                }

                // Level 2: [3], [4], [5], [6]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    (3..6).forEach { idx ->
                        if (step.heap.size > idx) {
                            val isCurr = step.currentIndex == idx
                            val isParent = step.parentIndex == idx
                            val isSwapped = step.swappedIndices != null && (step.swappedIndices.first == idx || step.swappedIndices.second == idx)
                            HeapTreeNode(step.heap[idx], idx, isHighlighted = isCurr, isParent = isParent, isSwapped = isSwapped)
                        }
                    }
                }
            }
        }

        // Array Row Component
        HeapArrayRow(
            array = step.heap,
            highlightedIndex = step.currentIndex,
            parentIndex = step.parentIndex,
            swappedIndices = step.swappedIndices
        )
    }
}

@Composable
private fun HeapInsertSiftUpDetailsAndStepsContent(
    step: InsertSiftUpTraceStep,
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
                Text("CURRENT STEP", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.event) {
                        InsertSiftUpEvent.SIFT_UP_COMPLETE -> GreenAccent
                        InsertSiftUpEvent.SWAP_UP, InsertSiftUpEvent.APPEND_VALUE -> OrangeAccent
                        else -> BlueAccent
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Insertion Time: O(log N)", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
