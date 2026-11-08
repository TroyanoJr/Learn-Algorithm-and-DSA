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
private val RedAccent = Color(0xFFEF4444)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9CA3AF)

@Composable
internal fun RecursiveBinarySearchVisualizationCard(
    title: String = "Recursive Binary Search"
) {
    val steps = RecursiveBinarySearchTrace.stepsForSearch()
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
        ArrayWindowButton(text = "Open binary search visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.event) {
            BinarySearchEvent.START -> "READY · TARGET = ${step.target}"
            BinarySearchEvent.CALCULATE_MID -> "MID = INDEX ${step.mid}"
            BinarySearchEvent.COMPARE_TARGET, BinarySearchEvent.RECURSE_LEFT, BinarySearchEvent.RECURSE_RIGHT -> "COMPARING TARGET WITH MID"
            BinarySearchEvent.TARGET_FOUND -> "MATCH FOUND AT INDEX ${step.resultIndex}"
            BinarySearchEvent.TARGET_NOT_FOUND -> "TARGET ABSENT (RETURN -1)"
        }

        val statusColor = when (step.event) {
            BinarySearchEvent.TARGET_FOUND -> GreenAccent
            BinarySearchEvent.TARGET_NOT_FOUND -> RedAccent
            BinarySearchEvent.CALCULATE_MID -> OrangeAccent
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
                    RecursiveBinarySearchVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                RecursiveBinarySearchDetailsAndStepsContent(
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
private fun RecursiveBinarySearchVisualizationContent(step: BinarySearchTraceStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Range & Target Header Card
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
                        text = "SEARCH TARGET: ${step.target}",
                        color = OrangeAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Active Range [left..right]: [${step.left}..${step.right}] (Eliminates 1/2 space per step)",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }

                if (step.mid != null) {
                    Surface(
                        color = Color(0xFF122838),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, BlueAccent)
                    ) {
                        Text(
                            text = "mid = ${step.mid} (arr[${step.mid}] = ${step.array[step.mid]})",
                            color = BlueAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp * scale, vertical = 4.dp * scale)
                        )
                    }
                }
            }
        }

        // Array Visualization with Left/Mid/Right Indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            step.array.forEachIndexed { index, value ->
                val isInRange = index in step.left..step.right
                val isMid = index == step.mid
                val isResult = index == step.resultIndex

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp * scale),
                    modifier = Modifier.weight(1f).padding(horizontal = 2.dp * scale)
                ) {
                    Text(
                        text = when {
                            isResult -> "MATCH"
                            isMid -> "MID"
                            index == step.left && index == step.right -> "L/R"
                            index == step.left -> "L"
                            index == step.right -> "R"
                            else -> " "
                        },
                        color = when {
                            isResult -> GreenAccent
                            isMid -> OrangeAccent
                            isInRange -> BlueAccent
                            else -> Color.Transparent
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        color = when {
                            isResult -> Color(0xFF123820)
                            isMid -> Color(0xFF2A1C12)
                            isInRange -> Color(0xFF13233A)
                            else -> Color(0xFF13131C)
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            1.dp,
                            when {
                                isResult -> GreenAccent
                                isMid -> OrangeAccent
                                isInRange -> BlueAccent
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
                                color = if (isInRange) TextPrimary else TextSecondary.copy(alpha = 0.3f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "[$index]",
                        color = if (isInRange) TextPrimary else TextSecondary.copy(alpha = 0.4f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun RecursiveBinarySearchDetailsAndStepsContent(
    step: BinarySearchTraceStep,
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
                        BinarySearchEvent.TARGET_FOUND -> GreenAccent
                        BinarySearchEvent.TARGET_NOT_FOUND -> RedAccent
                        BinarySearchEvent.CALCULATE_MID -> OrangeAccent
                        else -> BlueAccent
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Time Complexity: O(log N)", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
