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
internal fun IterationFundamentalsVisualizationCard(
    title: String = "Iteration Fundamentals"
) {
    val steps = IterationFundamentalsTrace.createTrace()
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val playback = rememberVisualizerPlaybackState(
        stepCount = steps.size,
        initialStep = 0
    )
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val step = steps[stepIndex]

    ArrayWindowButton(text = "Open iteration visualizer") {
        isExpanded = true
    }

    if (isExpanded) {
        val statusText = when (step.action) {
            IterationAction.INIT -> "ITERATION FUNDAMENTALS: LOOP 1 TO ${step.limit}"
            IterationAction.CHECK_CONDITION -> "CHECK CONDITION: i = ${step.i} <= ${step.limit} (TRUE)"
            IterationAction.EXECUTE_BODY -> "EXECUTE BODY: total += ${step.i} -> ${step.total}"
            IterationAction.UPDATE_STATE -> "UPDATE STATE: i++ -> ${step.i}"
            IterationAction.TERMINATED -> "LOOP TERMINATED: i = ${step.i} > ${step.limit} · TOTAL = ${step.total}"
        }

        val statusColor = when (step.action) {
            IterationAction.TERMINATED -> GreenAccent
            IterationAction.EXECUTE_BODY -> OrangeAccent
            IterationAction.UPDATE_STATE -> BlueAccent
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
                    IterationFundamentalsVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                IterationFundamentalsDetailsAndStepsContent(
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
private fun IterationFundamentalsVisualizationContent(step: IterationFundamentalsStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Loop State Variables Card
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
                    Text("LOOP CONTROL VARIABLE", color = OrangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("i = ${step.i} (Limit: ${step.limit})", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Surface(
                    color = Color(0xFF123820),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, GreenAccent)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp * scale, vertical = 4.dp * scale),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("ACCUMULATED TOTAL", color = TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text("${step.total}", color = GreenAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Loop Sequence Steps Render Card
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
                Text("LOOP PASS PROGRESS (1 TO ${step.limit}):", color = BlueAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    (1..step.limit).forEach { passVal ->
                        val isCurrentPass = step.passNumber == passVal && step.action != IterationAction.TERMINATED
                        val isPassed = (step.passNumber > passVal) || (step.action == IterationAction.TERMINATED)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp * scale),
                            modifier = Modifier.weight(1f).padding(horizontal = 2.dp * scale)
                        ) {
                            Text(
                                text = "Pass $passVal",
                                color = if (isCurrentPass) OrangeAccent else TextSecondary,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Surface(
                                color = when {
                                    isCurrentPass -> Color(0xFF2A1C12)
                                    isPassed -> Color(0xFF123820)
                                    else -> Color(0xFF181824)
                                },
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(
                                    1.dp,
                                    when {
                                        isCurrentPass -> OrangeAccent
                                        isPassed -> GreenAccent
                                        else -> CardBorderColor
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth().height(42.dp * scale)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "+$passVal",
                                        color = if (isPassed || isCurrentPass) TextPrimary else TextSecondary,
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
    }
}

@Composable
private fun IterationFundamentalsDetailsAndStepsContent(
    step: IterationFundamentalsStep,
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
                Text("ITERATION DECISION", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.action) {
                        IterationAction.TERMINATED -> GreenAccent
                        IterationAction.EXECUTE_BODY -> OrangeAccent
                        else -> BlueAccent
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Complexity: O(N) Time, O(1) Space", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
