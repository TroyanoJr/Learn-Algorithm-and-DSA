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
internal fun RecursionFundamentalsVisualizationCard(
    title: String = "Recursion Fundamentals"
) {
    val steps = RecursionFundamentalsTrace.createTrace()
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
        ArrayWindowButton(text = "Open recursion visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.action) {
            RecursionAction.INIT -> "RECURSION FUNDAMENTALS: sumTo(${step.inputN})"
            RecursionAction.CALL_DESCENT -> "DESCENT: CALL sumTo(${step.currentN})"
            RecursionAction.BASE_CASE_REACHED -> "BASE CASE REACHED: sumTo(0) = 0"
            RecursionAction.RETURN_ASCENT -> "ASCENT: RETURN sumTo(${step.currentN}) = ${step.currentReturnValue}"
            RecursionAction.FINISHED -> "RECURSION COMPLETE · sumTo(${step.inputN}) = ${step.currentReturnValue}"
        }

        val statusColor = when (step.action) {
            RecursionAction.BASE_CASE_REACHED, RecursionAction.RETURN_ASCENT, RecursionAction.FINISHED -> GreenAccent
            RecursionAction.CALL_DESCENT -> OrangeAccent
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
                    RecursionFundamentalsVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                RecursionFundamentalsDetailsAndStepsContent(
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
private fun RecursionFundamentalsVisualizationContent(step: RecursionFundamentalsStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Call Stack Visualization Card
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
                    Text("RECURSIVE CALL STACK (DEPTH ${step.callStack.size}):", color = OrangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("Base case: n == 0", color = TextSecondary, fontSize = 10.sp)
                }

                if (step.callStack.isEmpty()) {
                    Text("Call stack empty (unwound).", color = GreenAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp * scale)
                    ) {
                        step.callStack.reversed().forEachIndexed { stackIdx, frameN ->
                            val isTopFrame = stackIdx == 0
                            val frameReturn = step.returnValues.firstOrNull { it.first == frameN }

                            Surface(
                                color = if (isTopFrame) Color(0xFF2A1C12) else Color(0xFF181824),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, if (isTopFrame) OrangeAccent else CardBorderColor),
                                modifier = Modifier.fillMaxWidth().height(32.dp * scale)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp * scale).fillMaxSize(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Frame: sumTo($frameN)",
                                        color = if (isTopFrame) OrangeAccent else TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (frameReturn != null) "Return = ${frameReturn.second}" else "Waiting...",
                                        color = if (frameReturn != null) GreenAccent else TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Return Values History Card
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
                    text = "Unwound Returns: ${step.returnValues.map { "sumTo(${it.first})=${it.second}" }}",
                    color = TextPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Result: ${step.currentReturnValue ?: "-"}",
                    color = GreenAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun RecursionFundamentalsDetailsAndStepsContent(
    step: RecursionFundamentalsStep,
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
                Text("RECURSION DECISION", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.action) {
                        RecursionAction.BASE_CASE_REACHED, RecursionAction.RETURN_ASCENT, RecursionAction.FINISHED -> GreenAccent
                        RecursionAction.CALL_DESCENT -> OrangeAccent
                        else -> BlueAccent
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Complexity: O(N) Time & Stack", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
