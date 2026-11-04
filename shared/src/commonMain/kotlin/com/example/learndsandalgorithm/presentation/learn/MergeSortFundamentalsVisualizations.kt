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
internal fun MergeSortFundamentalsVisualizationCard(
    title: String = "Merge Sort Fundamentals"
) {
    val steps = MergeSortFundamentalsTrace.stepsForMerge()
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val playback = rememberVisualizerPlaybackState(
        stepCount = steps.size,
        initialStep = 0
    )
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val step = steps[stepIndex]

    ArrayWindowButton(text = "Open merge step visualizer") {
        isExpanded = true
    }

    if (isExpanded) {
        val statusText = when (step.event) {
            MergeStepEvent.START -> "READY · MERGING TWO SORTED HALVES"
            MergeStepEvent.COMPARE -> "COMPARING Left[${step.i}] vs Right[${step.j}]"
            MergeStepEvent.TAKE_LEFT -> "TAKING LEFT: ${step.takenVal}"
            MergeStepEvent.TAKE_RIGHT -> "TAKING RIGHT: ${step.takenVal}"
            MergeStepEvent.FLUSH_REMAINING -> "FLUSHING REMAINING: ${step.takenVal}"
            MergeStepEvent.COMPLETE -> "MERGE COMPLETE"
        }

        val statusColor = when (step.event) {
            MergeStepEvent.COMPLETE -> GreenAccent
            MergeStepEvent.TAKE_LEFT, MergeStepEvent.TAKE_RIGHT -> OrangeAccent
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
                    MergeSortFundamentalsVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                MergeSortFundamentalsDetailsAndStepsContent(
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
private fun MergeSortFundamentalsVisualizationContent(step: MergeFundamentalsTraceStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Two Sorted Halves Display (Left & Right)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp * scale)
        ) {
            // Left Array Card
            Surface(
                color = Color(0xFF13131C),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, CardBorderColor),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp * scale),
                    verticalArrangement = Arrangement.spacedBy(6.dp * scale)
                ) {
                    Text("LEFT SORTED HALF (i = ${step.i}):", color = OrangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        step.leftArr.forEachIndexed { idx, valItem ->
                            val isPointer = idx == step.i
                            val isTaken = idx < step.i
                            Surface(
                                color = when {
                                    isPointer -> Color(0xFF2A1C12)
                                    isTaken -> Color(0xFF181824)
                                    else -> Color(0xFF13233A)
                                },
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, if (isPointer) OrangeAccent else CardBorderColor),
                                modifier = Modifier.weight(1f).padding(horizontal = 2.dp * scale)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp * scale)) {
                                    Text("$valItem", color = if (isTaken) TextSecondary.copy(alpha = 0.4f) else TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Right Array Card
            Surface(
                color = Color(0xFF13131C),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, CardBorderColor),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp * scale),
                    verticalArrangement = Arrangement.spacedBy(6.dp * scale)
                ) {
                    Text("RIGHT SORTED HALF (j = ${step.j}):", color = BlueAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        step.rightArr.forEachIndexed { idx, valItem ->
                            val isPointer = idx == step.j
                            val isTaken = idx < step.j
                            Surface(
                                color = when {
                                    isPointer -> Color(0xFF122838)
                                    isTaken -> Color(0xFF181824)
                                    else -> Color(0xFF13233A)
                                },
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, if (isPointer) BlueAccent else CardBorderColor),
                                modifier = Modifier.weight(1f).padding(horizontal = 2.dp * scale)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp * scale)) {
                                    Text("$valItem", color = if (isTaken) TextSecondary.copy(alpha = 0.4f) else TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Merged Result Output Array
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
                Text("COMBINED MERGED RESULT ARRAY (O(N) Merge Work):", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                if (step.mergedResult.isEmpty()) {
                    Text("Result array empty", color = TextSecondary, fontSize = 11.sp)
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        step.mergedResult.forEachIndexed { idx, valItem ->
                            val isJustAdded = idx == step.mergedResult.lastIndex
                            Surface(
                                color = if (isJustAdded) Color(0xFF123820) else Color(0xFF181824),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, if (isJustAdded) GreenAccent else CardBorderColor),
                                modifier = Modifier.padding(end = 6.dp * scale)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp * scale, vertical = 6.dp * scale)) {
                                    Text("$valItem", color = GreenAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
private fun MergeSortFundamentalsDetailsAndStepsContent(
    step: MergeFundamentalsTraceStep,
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
                        MergeStepEvent.COMPLETE -> GreenAccent
                        MergeStepEvent.TAKE_LEFT, MergeStepEvent.TAKE_RIGHT -> OrangeAccent
                        else -> BlueAccent
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Merge Step: O(N) Linear Time", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
