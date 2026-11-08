package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
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
internal fun CombinationsPruningVisualizationCard(
    title: String = "Combinations & Subsets with Pruning"
) {
    val steps = CombinationsPruningTrace.createTrace()
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
        ArrayWindowButton(text = "Open combination sum visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.action) {
            CombinationAction.INIT -> "COMBINATION SUM: TARGET ${step.target}"
            CombinationAction.TRY_CANDIDATE -> "TRY CANDIDATE -> CURRENT SUM ${step.currentSum}"
            CombinationAction.PRUNE -> "PRUNE BRANCH: SUM ${step.currentSum} > TARGET ${step.target}"
            CombinationAction.SOLUTION_FOUND -> "SOLUTION FOUND: ${step.currentCombination}"
            CombinationAction.BACKTRACK -> "BACKTRACK -> RESTORE SUM ${step.currentSum}"
            CombinationAction.FINISHED -> "SEARCH COMPLETE · ${step.validCombinations.size} SOLUTIONS"
        }

        val statusColor = when (step.action) {
            CombinationAction.SOLUTION_FOUND, CombinationAction.FINISHED -> GreenAccent
            CombinationAction.PRUNE -> RedAccent
            CombinationAction.TRY_CANDIDATE -> OrangeAccent
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
                    CombinationsPruningVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                CombinationsPruningDetailsAndStepsContent(
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
private fun CombinationsPruningVisualizationContent(step: CombinationStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Target & Current Sum Header Card
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("TARGET SUM: ${step.target}", color = OrangeAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("Current Sum: ${step.currentSum}", color = if (step.currentSum > step.target) RedAccent else TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                val progress = (step.currentSum.toFloat() / step.target).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp * scale)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFF22222E))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(10.dp * scale)
                            .clip(RoundedCornerShape(5.dp))
                            .background(
                                when {
                                    step.currentSum == step.target -> GreenAccent
                                    step.currentSum > step.target -> RedAccent
                                    else -> BlueAccent
                                }
                            )
                    )
                }
            }
        }

        // Active Combination Stack Row
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
                Text("CURRENT COMBINATION STACK:", color = BlueAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                if (step.currentCombination.isEmpty()) {
                    Text("[] (empty candidate stack)", color = TextSecondary, fontSize = 11.sp)
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp * scale),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        step.currentCombination.forEach { elem ->
                            Surface(
                                color = when {
                                    step.currentSum == step.target -> Color(0xFF123820)
                                    step.currentSum > step.target -> Color(0xFF381215)
                                    else -> Color(0xFF2A1C12)
                                },
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(
                                    1.dp,
                                    when {
                                        step.currentSum == step.target -> GreenAccent
                                        step.currentSum > step.target -> RedAccent
                                        else -> OrangeAccent
                                    }
                                )
                            ) {
                                Text(
                                    "+$elem",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp * scale, vertical = 4.dp * scale)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Valid Combinations List Summary Card
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
                    text = "Found Solutions: ${step.validCombinations.map { it.toString() }}",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Count: ${step.validCombinations.size}",
                    color = GreenAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CombinationsPruningDetailsAndStepsContent(
    step: CombinationStep,
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
                Text("PRUNING DECISION", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.action) {
                        CombinationAction.SOLUTION_FOUND, CombinationAction.FINISHED -> GreenAccent
                        CombinationAction.PRUNE -> RedAccent
                        else -> OrangeAccent
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Pruning Optimization", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
