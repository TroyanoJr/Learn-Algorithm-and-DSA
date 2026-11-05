package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
internal fun UniquePathsDpVisualizationCard(
    title: String = "2D Subproblem Grids: Unique Paths in a Grid"
) {
    val steps = UniquePathsDpTrace.createTrace()
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val playback = rememberVisualizerPlaybackState(
        stepCount = steps.size,
        initialStep = 0
    )
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val step = steps[stepIndex]

    ArrayWindowButton(text = "Open unique paths visualizer") {
        isExpanded = true
    }

    if (isExpanded) {
        val statusText = when (step.action) {
            UniquePathsDpAction.INIT -> "2D GRID DP: ${step.rows}x${step.cols} MATRIX"
            UniquePathsDpAction.FILL_BASE_CASES -> "SET BASE CASING: FIRST ROW & COL = 1"
            UniquePathsDpAction.COMPUTE_CELL -> "COMPUTE dp[${step.currentR}][${step.currentC}] = TOP (${step.topVal}) + LEFT (${step.leftVal})"
            UniquePathsDpAction.FINISHED -> "GRID DP COMPLETE · ${step.dpGrid[step.rows - 1][step.cols - 1]} UNIQUE PATHS"
        }

        val statusColor = when (step.action) {
            UniquePathsDpAction.COMPUTE_CELL, UniquePathsDpAction.FINISHED -> GreenAccent
            UniquePathsDpAction.FILL_BASE_CASES -> OrangeAccent
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
                    UniquePathsDpVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                UniquePathsDpDetailsAndStepsContent(
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
private fun UniquePathsDpVisualizationContent(step: UniquePathsDpStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // 2D Grid Render Card
        Surface(
            color = Color(0xFF13131C),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, CardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(10.dp * scale),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp * scale)
            ) {
                Text("${step.rows}x${step.cols} 2D DP GRID VALUES:", color = OrangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                step.dpGrid.forEachIndexed { r, row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp * scale)) {
                        row.forEachIndexed { c, cellVal ->
                            val isCurrent = step.currentR == r && step.currentC == c
                            val isTop = step.currentR != null && step.currentC != null && r == step.currentR - 1 && c == step.currentC
                            val isLeft = step.currentR != null && step.currentC != null && r == step.currentR && c == step.currentC - 1
                            val isComputed = cellVal > 0

                            Surface(
                                color = when {
                                    isCurrent -> Color(0xFF123820)
                                    isTop || isLeft -> Color(0xFF2A1C12)
                                    isComputed -> Color(0xFF122C38)
                                    else -> Color(0xFF181824)
                                },
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(
                                    1.dp,
                                    when {
                                        isCurrent -> GreenAccent
                                        isTop || isLeft -> OrangeAccent
                                        isComputed -> BlueAccent
                                        else -> CardBorderColor
                                    }
                                ),
                                modifier = Modifier.size(46.dp * scale)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "$cellVal",
                                        color = if (isComputed) TextPrimary else TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Cell Computation Summary Card
        Surface(
            color = Color(0xFF13131C),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, CardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(8.dp * scale).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Computing Cell (${step.currentR ?: 0}, ${step.currentC ?: 0})",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Top: ${step.topVal ?: "-"} + Left: ${step.leftVal ?: "-"}",
                    color = GreenAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun UniquePathsDpDetailsAndStepsContent(
    step: UniquePathsDpStep,
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
                Text("2D GRID DP DECISION", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.action) {
                        UniquePathsDpAction.COMPUTE_CELL, UniquePathsDpAction.FINISHED -> GreenAccent
                        else -> OrangeAccent
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Complexity: O(R*C) Time, O(C) Space", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
