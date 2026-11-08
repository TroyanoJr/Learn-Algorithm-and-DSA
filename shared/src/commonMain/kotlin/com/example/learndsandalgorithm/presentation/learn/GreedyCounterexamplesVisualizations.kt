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
import androidx.compose.foundation.shape.CircleShape
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
internal fun GreedyCounterexamplesVisualizationCard(
    title: String = "Greedy Counterexamples & Correctness"
) {
    val steps = GreedyCounterexamplesTrace.createTrace()
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val playback = rememberVisualizerPlaybackState(
        stepCount = steps.size,
        initialStep = 0
    )
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val step = steps[stepIndex]

    ArrayWindowButton(text = "Open counterexamples visualizer") {
        isExpanded = true
    }

    if (isExpanded) {
        val statusText = when (step.status) {
            CounterexampleStatus.INIT -> "COUNTEREXAMPLE: TARGET ${step.targetAmount} WITH ${step.denominations}"
            CounterexampleStatus.GREEDY_STEP_1 -> "GREEDY CHOICE 1: PICK 4 (LARGEST)"
            CounterexampleStatus.GREEDY_STEP_2 -> "GREEDY CHOICE 2: PICK 1"
            CounterexampleStatus.GREEDY_STEP_3 -> "GREEDY CHOICE 3: PICK 1 (TOTAL 3 COINS)"
            CounterexampleStatus.COMPARE_OPTIMAL -> "OPTIMAL SOLUTION: [3, 3] (2 COINS)"
            CounterexampleStatus.FINISHED -> "COUNTEREXAMPLE PROVEN · GREEDY FAILS HERE"
        }

        val statusColor = when (step.status) {
            CounterexampleStatus.FINISHED, CounterexampleStatus.GREEDY_STEP_3 -> RedAccent
            CounterexampleStatus.COMPARE_OPTIMAL -> GreenAccent
            CounterexampleStatus.GREEDY_STEP_1, CounterexampleStatus.GREEDY_STEP_2 -> OrangeAccent
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
                    GreedyCounterexamplesVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                GreedyCounterexamplesDetailsAndStepsContent(
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
private fun GreedyCounterexamplesVisualizationContent(step: GreedyCounterexamplesStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Problem Header Card
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
                        text = "COUNTEREXAMPLE DEMO: TARGET AMOUNT ${step.targetAmount}",
                        color = OrangeAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Denominations: [${step.denominations.joinToString()}]",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }

                Surface(
                    color = if (step.status == CounterexampleStatus.FINISHED) Color(0xFF381215) else Color(0xFF13233A),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, if (step.status == CounterexampleStatus.FINISHED) RedAccent else BlueAccent)
                ) {
                    Text(
                        text = if (step.status == CounterexampleStatus.FINISHED) "FAILED GREEDY" else "COMPARING",
                        color = if (step.status == CounterexampleStatus.FINISHED) RedAccent else BlueAccent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp * scale, vertical = 4.dp * scale)
                    )
                }
            }
        }

        // Side by Side Comparison Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp * scale)
        ) {
            // 1. Greedy Choice Column
            Surface(
                color = Color(0xFF13131C),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, if (step.greedyCoins.size == 3) RedAccent else CardBorderColor),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp * scale),
                    verticalArrangement = Arrangement.spacedBy(6.dp * scale),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "GREEDY (LARGEST-FIRST)",
                        color = RedAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp * scale),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (step.greedyCoins.isEmpty()) {
                            Text("-", color = TextSecondary, fontSize = 12.sp)
                        } else {
                            step.greedyCoins.forEach { c ->
                                Surface(
                                    color = Color(0xFF381215),
                                    shape = CircleShape,
                                    border = BorderStroke(1.dp, RedAccent),
                                    modifier = Modifier.size(32.dp * scale)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("$c", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Text(
                        text = "Result: ${step.greedyCoins.size} coins",
                        color = if (step.greedyCoins.size == 3) RedAccent else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 2. Optimal Strategy Column
            Surface(
                color = Color(0xFF13131C),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, if (step.status >= CounterexampleStatus.COMPARE_OPTIMAL) GreenAccent else CardBorderColor),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp * scale),
                    verticalArrangement = Arrangement.spacedBy(6.dp * scale),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "OPTIMAL (NON-GREEDY)",
                        color = GreenAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp * scale),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (step.status < CounterexampleStatus.COMPARE_OPTIMAL) {
                            Text("?", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        } else {
                            step.optimalCoins.forEach { c ->
                                Surface(
                                    color = Color(0xFF123820),
                                    shape = CircleShape,
                                    border = BorderStroke(1.dp, GreenAccent),
                                    modifier = Modifier.size(32.dp * scale)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("$c", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Text(
                        text = if (step.status < CounterexampleStatus.COMPARE_OPTIMAL) "Hidden" else "Result: ${step.optimalCoins.size} coins",
                        color = if (step.status >= CounterexampleStatus.COMPARE_OPTIMAL) GreenAccent else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Key Insight Banner
        Surface(
            color = Color(0xFF13131C),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, CardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(8.dp * scale),
                verticalArrangement = Arrangement.spacedBy(2.dp * scale)
            ) {
                Text(
                    text = "WHY GREEDY FAILS HERE:",
                    color = OrangeAccent,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Picking coin 4 locally leaves amount 2, which requires two 1s (total 3 coins). Picking coin 3 leaves 3, solved by one 3 (total 2 coins). Local best != Global best!",
                    color = TextPrimary,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
private fun GreedyCounterexamplesDetailsAndStepsContent(
    step: GreedyCounterexamplesStep,
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
                Text("GREEDY PROOF VERDICT", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.status) {
                        CounterexampleStatus.FINISHED -> RedAccent
                        CounterexampleStatus.COMPARE_OPTIMAL -> GreenAccent
                        else -> OrangeAccent
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Proof by Counterexample", color = OrangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
