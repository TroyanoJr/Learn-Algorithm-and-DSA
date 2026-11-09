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
internal fun BruteForcePairProblemsVisualizationCard(
    title: String = "Brute Force for Pair Problems"
) {
    val steps = BruteForcePairProblemsTrace.stepsForFindPair()
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
        ArrayWindowButton(text = "Open pair problems visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.event) {
            PairSearchEvent.START -> "NESTED PAIR SEARCH · TARGET = ${step.target}"
            PairSearchEvent.EVALUATE_PAIR -> "TESTING PAIR (i=${step.currentI}, j=${step.currentJ}): SUM = ${step.currentSum}"
            PairSearchEvent.PAIR_FOUND -> "MATCH FOUND AT INDICES (${step.foundPair?.first}, ${step.foundPair?.second})"
            PairSearchEvent.NO_PAIR_FOUND -> "NO PAIR FOUND"
        }

        val statusColor = when (step.event) {
            PairSearchEvent.PAIR_FOUND -> GreenAccent
            PairSearchEvent.NO_PAIR_FOUND -> RedAccent
            PairSearchEvent.EVALUATE_PAIR -> OrangeAccent
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
                    BruteForcePairProblemsVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                BruteForcePairProblemsDetailsAndStepsContent(
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
private fun BruteForcePairProblemsVisualizationContent(step: BruteForcePairProblemsTraceStep, scale: Float) {
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
                        text = "TWO-SUM PAIR SEARCH (TARGET = ${step.target})",
                        color = OrangeAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Pointers: i = ${step.currentI ?: "—"}, j = ${step.currentJ ?: "—"} | Current Sum = ${step.currentSum ?: "—"}",
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

        // Array Values with i and j Pointers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            step.values.forEachIndexed { index, value ->
                val isI = index == step.currentI
                val isJ = index == step.currentJ
                val isMatch = step.foundPair != null && (index == step.foundPair.first || index == step.foundPair.second)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp * scale),
                    modifier = Modifier.weight(1f).padding(horizontal = 2.dp * scale)
                ) {
                    Text(
                        text = when {
                            isMatch -> "MATCH"
                            isI && isJ -> "i/j"
                            isI -> "i"
                            isJ -> "j"
                            else -> " "
                        },
                        color = when {
                            isMatch -> GreenAccent
                            isI -> OrangeAccent
                            isJ -> BlueAccent
                            else -> Color.Transparent
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        color = when {
                            isMatch -> Color(0xFF123820)
                            isI || isJ -> Color(0xFF2A1C12)
                            else -> Color(0xFF13131C)
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            1.dp,
                            when {
                                isMatch -> GreenAccent
                                isI -> OrangeAccent
                                isJ -> BlueAccent
                                else -> CardBorderColor
                            }
                        ),
                        modifier = Modifier.fillMaxWidth().height(52.dp * scale)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("$value", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text("[$index]", color = TextSecondary, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun BruteForcePairProblemsDetailsAndStepsContent(
    step: BruteForcePairProblemsTraceStep,
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
                Text("CURRENT PAIR STEP", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = if (step.foundPair != null) GreenAccent else OrangeAccent,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Worst-case: O(N²) checks", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
