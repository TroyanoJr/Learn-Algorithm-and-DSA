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
internal fun DpMemoizationVisualizationCard(
    title: String = "Dynamic Programming Fundamentals and Memoization"
) {
    val steps = DpMemoizationTrace.createTrace()
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val playback = rememberVisualizerPlaybackState(
        stepCount = steps.size,
        initialStep = 0
    )
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val step = steps[stepIndex]

    ArrayWindowButton(text = "Open memoization visualizer") {
        isExpanded = true
    }

    if (isExpanded) {
        val statusText = when (step.action) {
            MemoAction.INIT -> "TOP-DOWN DP (MEMOIZATION): TARGET FIB(${step.targetN})"
            MemoAction.CALL_SUBPROBLEM -> "CALL SUBPROBLEM FIB(${step.currentN})"
            MemoAction.CACHE_HIT -> "CACHE HIT! FIB(${step.currentN}) REUSED FROM MEMO TABLE"
            MemoAction.COMPUTE_AND_STORE -> "COMPUTE & STORE MEMO[${step.currentN}] = ${step.memoArray[step.currentN ?: 0]}"
            MemoAction.FINISHED -> "MEMOIZATION COMPLETE · FIB(${step.targetN}) = ${step.memoArray[step.targetN]}"
        }

        val statusColor = when (step.action) {
            MemoAction.CACHE_HIT, MemoAction.FINISHED -> GreenAccent
            MemoAction.COMPUTE_AND_STORE -> OrangeAccent
            MemoAction.CALL_SUBPROBLEM -> BlueAccent
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
                    DpMemoizationVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                DpMemoizationDetailsAndStepsContent(
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
private fun DpMemoizationVisualizationContent(step: DpMemoizationStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Memoization Table Array Render Card
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
                    Text("MEMOIZATION LOOKUP TABLE memo[]:", color = OrangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    if (step.cacheHit) {
                        Surface(
                            color = Color(0xFF123820),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, GreenAccent)
                        ) {
                            Text("CACHE HIT!", color = GreenAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp * scale, vertical = 2.dp * scale))
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    step.memoArray.forEachIndexed { idx, value ->
                        val isCurrent = step.currentN == idx
                        val isHit = isCurrent && step.cacheHit
                        val isCached = value != null

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp * scale),
                            modifier = Modifier.weight(1f).padding(horizontal = 2.dp * scale)
                        ) {
                            Text(
                                text = "[$idx]",
                                color = if (isCurrent) OrangeAccent else TextSecondary,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Surface(
                                color = when {
                                    isHit -> Color(0xFF123820)
                                    isCurrent -> Color(0xFF2A1C12)
                                    isCached -> Color(0xFF122C38)
                                    else -> Color(0xFF181824)
                                },
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(
                                    1.dp,
                                    when {
                                        isHit -> GreenAccent
                                        isCurrent -> OrangeAccent
                                        isCached -> BlueAccent
                                        else -> CardBorderColor
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth().height(42.dp * scale)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = value?.toString() ?: "null",
                                        color = when {
                                            isHit -> GreenAccent
                                            isCached -> TextPrimary
                                            else -> TextSecondary
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Call Stack Indicator Card
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
                    text = "Recursive Call Stack: ${step.callStack.map { "Fib($it)" }}",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Depth: ${step.callStack.size}",
                    color = BlueAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DpMemoizationDetailsAndStepsContent(
    step: DpMemoizationStep,
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
                Text("TOP-DOWN MEMOIZATION DECISION", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.action) {
                        MemoAction.CACHE_HIT, MemoAction.FINISHED -> GreenAccent
                        MemoAction.COMPUTE_AND_STORE -> OrangeAccent
                        else -> BlueAccent
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Complexity: O(N) Time, O(N) Space", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
