package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
internal fun QuickSortPartitionVisualizationCard(
    title: String = "Quick Sort Fundamentals and Partitioning"
) {
    val steps = QuickSortPartitionTrace.stepsForPartition()
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
        ArrayWindowButton(text = "Open quick sort visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.event) {
            QuickPartitionEvent.START -> "READY · LOMUTO PARTITIONING"
            QuickPartitionEvent.SELECT_PIVOT -> "PIVOT = ${step.pivotValue} AT INDEX ${step.pivotIndex}"
            QuickPartitionEvent.SCAN_COMPARE -> "SCAN j = ${step.j}: COMPARING WITH PIVOT ${step.pivotValue}"
            QuickPartitionEvent.SWAP_ELEMENTS -> "SWAPPING arr[${step.swappedIndices?.first}] ↔ arr[${step.swappedIndices?.second}]"
            QuickPartitionEvent.PLACE_PIVOT -> "PLACING PIVOT AT INDEX ${step.finalPivotPosition}"
            QuickPartitionEvent.PARTITION_COMPLETE, QuickPartitionEvent.SORT_COMPLETE -> "PARTITION COMPLETE AT INDEX ${step.finalPivotPosition}"
        }

        val statusColor = when (step.event) {
            QuickPartitionEvent.PARTITION_COMPLETE, QuickPartitionEvent.SORT_COMPLETE -> GreenAccent
            QuickPartitionEvent.SWAP_ELEMENTS, QuickPartitionEvent.PLACE_PIVOT -> OrangeAccent
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
                    QuickSortPartitionVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                QuickSortPartitionDetailsAndStepsContent(
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
private fun QuickSortPartitionVisualizationContent(step: QuickPartitionTraceStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Pivot & Pointers Status Card
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
                        text = "LOMUTO PARTITIONING (PIVOT = ${step.pivotValue})",
                        color = OrangeAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "i = ${step.i} (boundary <= pivot) | j = ${step.j} (scanning pointer)",
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
                        text = "PIVOT: ${step.pivotValue}",
                        color = OrangeAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp * scale, vertical = 4.dp * scale)
                    )
                }
            }
        }

        // Array with Pointer Indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            step.array.forEachIndexed { index, value ->
                val isPivot = index == step.pivotIndex || index == step.finalPivotPosition
                val isI = index == step.i
                val isJ = index == step.j
                val isSwapped = step.swappedIndices != null && (index == step.swappedIndices.first || index == step.swappedIndices.second)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp * scale),
                    modifier = Modifier.weight(1f).padding(horizontal = 2.dp * scale)
                ) {
                    Text(
                        text = when {
                            isPivot -> "PIVOT"
                            isI && isJ -> "i/j"
                            isI -> "i"
                            isJ -> "j"
                            else -> " "
                        },
                        color = when {
                            isPivot -> OrangeAccent
                            isI -> GreenAccent
                            isJ -> BlueAccent
                            else -> Color.Transparent
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        color = when {
                            isPivot -> Color(0xFF2A1C12)
                            isSwapped -> Color(0xFF2A1C12)
                            isJ -> Color(0xFF13283A)
                            isI -> Color(0xFF123820)
                            else -> Color(0xFF13131C)
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            1.dp,
                            when {
                                isPivot -> OrangeAccent
                                isSwapped -> OrangeAccent
                                isJ -> BlueAccent
                                isI -> GreenAccent
                                else -> CardBorderColor
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp * scale)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$value",
                                color = if (isPivot) OrangeAccent else TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text("[$index]", color = TextSecondary, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun QuickSortPartitionDetailsAndStepsContent(
    step: QuickPartitionTraceStep,
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
                        QuickPartitionEvent.PARTITION_COMPLETE, QuickPartitionEvent.SORT_COMPLETE -> GreenAccent
                        QuickPartitionEvent.SWAP_ELEMENTS, QuickPartitionEvent.PLACE_PIVOT -> OrangeAccent
                        else -> BlueAccent
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("In-Place Partitioning", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("Aux Space: O(1) in-place", color = BlueAccent, fontSize = 9.sp)
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
