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
internal fun HeapSortVisualizationCard(
    title: String = "Priority Queues and Heap Sort",
    useSceneArchitecture: Boolean = true
) {
    val steps = remember { HeapSortTrace.stepsForHeapSort() }
    val scenes = remember(steps) { HeapSceneBuilder.buildSortScenes(steps) }
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
        ArrayWindowButton(text = "Open heap sort visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.event) {
            HeapSortEvent.START -> "READY · HEAP SORT (IN-PLACE O(N log N))"
            HeapSortEvent.BUILD_MAX_HEAP -> "MAX-HEAP BUILT · ROOT HAS MAXIMUM VALUE"
            HeapSortEvent.SWAP_ROOT_TO_END -> "SWAPPED ROOT MAX TO END OF UNPRECEDENTED BOUNDARY"
            HeapSortEvent.SIFT_DOWN_ROOT -> "SIFTING NEW ROOT DOWN IN SHRINKING HEAP"
            HeapSortEvent.SORT_COMPLETE -> "HEAP SORT COMPLETE"
        }

        val statusColor = when (step.event) {
            HeapSortEvent.SORT_COMPLETE -> GreenAccent
            HeapSortEvent.SWAP_ROOT_TO_END, HeapSortEvent.SIFT_DOWN_ROOT -> OrangeAccent
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
                        HeapSortVisualizationContent(step = step, scale = 1f)
                    }
                }
            },
            detailsContent = {
                HeapSortDetailsAndStepsContent(
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
private fun HeapSortVisualizationContent(step: HeapSortTraceStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Status Header Card
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
                        text = "HEAP SORT (IN-PLACE MAX-HEAP)",
                        color = OrangeAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Active Heap Size = ${step.heapBoundaryIndex + 1} | Sorted Suffix = indices [${step.heapBoundaryIndex + 1}..${step.array.lastIndex}]",
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

        // Tree View for active Heap Prefix
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
                Text("ACTIVE MAX-HEAP PREFIX VIEW:", color = BlueAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                if (step.array.isNotEmpty()) {
                    val isSwapped = step.swappedIndices != null && (step.swappedIndices.first == 0 || step.swappedIndices.second == 0)
                    HeapTreeNode(step.array[0], 0, isHighlighted = step.currentRootIndex == 0, isSwapped = isSwapped)
                    TreeBranchConnector(label = "↙     ↘")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    (1..2).forEach { idx ->
                        if (step.array.size > idx) {
                            val inHeap = idx <= step.heapBoundaryIndex
                            val isSwapped = step.swappedIndices != null && (step.swappedIndices.first == idx || step.swappedIndices.second == idx)
                            if (inHeap) {
                                HeapTreeNode(step.array[idx], idx, isHighlighted = step.currentRootIndex == idx, isSwapped = isSwapped)
                            }
                        }
                    }
                }

                if (step.array.size > 3 && step.heapBoundaryIndex >= 3) {
                    TreeBranchConnector(label = "↙")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val isSwapped = step.swappedIndices != null && (step.swappedIndices.first == 3 || step.swappedIndices.second == 3)
                        HeapTreeNode(step.array[3], 3, isHighlighted = step.currentRootIndex == 3, isSwapped = isSwapped)
                    }
                }
            }
        }

        // Array Row Component
        HeapArrayRow(
            array = step.array,
            highlightedIndex = step.currentRootIndex,
            parentIndex = step.largerChildIndex,
            swappedIndices = step.swappedIndices,
            heapSize = step.heapBoundaryIndex + 1
        )
    }
}

@Composable
private fun HeapSortDetailsAndStepsContent(
    step: HeapSortTraceStep,
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
                        HeapSortEvent.SORT_COMPLETE -> GreenAccent
                        HeapSortEvent.SWAP_ROOT_TO_END, HeapSortEvent.SIFT_DOWN_ROOT -> OrangeAccent
                        else -> BlueAccent
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Time: O(N log N) Guaranteed", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("Aux Space: O(1) In-Place", color = BlueAccent, fontSize = 9.sp)
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
