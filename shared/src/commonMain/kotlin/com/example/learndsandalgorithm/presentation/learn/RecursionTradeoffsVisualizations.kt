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
internal fun RecursionTradeoffsVisualizationCard(
    title: String = "Recursion Patterns and Trade-offs"
) {
    val steps = RecursionTradeoffsTrace.createTrace()
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
        ArrayWindowButton(text = "Open tradeoffs visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.action) {
            TradeoffAction.INIT -> "RECURSION TRADE-OFFS: ITERATION VS RECURSION"
            TradeoffAction.ITERATIVE_LOOP -> "ITERATION PASS i = ${step.iterI} · TOTAL = ${step.iterTotal}"
            TradeoffAction.RECURSIVE_DESCENT -> "RECURSIVE DESCENT: CALL sumTo(${step.recurCallStack.lastOrNull()})"
            TradeoffAction.RECURSIVE_BASE -> "RECURSIVE BASE CASE: sumTo(0) = 0"
            TradeoffAction.RECURSIVE_ASCENT -> "RECURSIVE ASCENT: RETURN ${step.recurReturnVal}"
            TradeoffAction.FINISHED -> "TRADE-OFFS COMPLETE · BOTH SUM = ${step.iterTotal}"
        }

        val statusColor = when (step.action) {
            TradeoffAction.RECURSIVE_BASE, TradeoffAction.RECURSIVE_ASCENT, TradeoffAction.FINISHED -> GreenAccent
            TradeoffAction.ITERATIVE_LOOP, TradeoffAction.RECURSIVE_DESCENT -> OrangeAccent
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
                    RecursionTradeoffsVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                RecursionTradeoffsDetailsAndStepsContent(
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
private fun RecursionTradeoffsVisualizationContent(step: RecursionTradeoffsStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Target Header Card
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
                    Text("TRADE-OFFS COMPARISON DEMO", color = OrangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("Task: Compute sum of 1 through ${step.inputN}", color = TextSecondary, fontSize = 10.sp)
                }

                Surface(
                    color = Color(0xFF123820),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, GreenAccent)
                ) {
                    Text(
                        "Result: ${step.iterTotal}",
                        color = GreenAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp * scale, vertical = 4.dp * scale)
                    )
                }
            }
        }

        // Side-by-Side Iteration vs Recursion Comparison Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp * scale)
        ) {
            // Iteration Card
            Surface(
                color = Color(0xFF13131C),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, if (step.action == TradeoffAction.ITERATIVE_LOOP) OrangeAccent else CardBorderColor),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp * scale),
                    verticalArrangement = Arrangement.spacedBy(4.dp * scale)
                ) {
                    Text("ITERATIVE LOOP", color = OrangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("Total: ${step.iterTotal}", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Auxiliary Space: O(1)", color = GreenAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("No call-stack overhead", color = TextSecondary, fontSize = 8.sp)
                }
            }

            // Recursion Card
            Surface(
                color = Color(0xFF13131C),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, if (step.action >= TradeoffAction.RECURSIVE_DESCENT) BlueAccent else CardBorderColor),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp * scale),
                    verticalArrangement = Arrangement.spacedBy(4.dp * scale)
                ) {
                    Text("DIRECT RECURSION", color = BlueAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("Stack Depth: ${step.recurCallStack.size}", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Auxiliary Space: O(N)", color = OrangeAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("Stack frames on JVM", color = TextSecondary, fontSize = 8.sp)
                }
            }
        }

        // Key Tradeoff Note Card
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
                Text("JVM RECURSION NOTE:", color = BlueAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Standard recursion on the JVM creates active call stack frames for each call (O(N) stack space). The JVM does NOT perform automatic tail-call optimization (TCO) for general recursive methods.",
                    color = TextPrimary,
                    fontSize = 9.sp,
                    lineHeight = 13.sp
                )
            }
        }
    }
}

@Composable
private fun RecursionTradeoffsDetailsAndStepsContent(
    step: RecursionTradeoffsStep,
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
                Text("TRADE-OFFS DECISION", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.action) {
                        TradeoffAction.RECURSIVE_BASE, TradeoffAction.RECURSIVE_ASCENT, TradeoffAction.FINISHED -> GreenAccent
                        TradeoffAction.ITERATIVE_LOOP, TradeoffAction.RECURSIVE_DESCENT -> OrangeAccent
                        else -> BlueAccent
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Iteration O(1) vs Recursion O(N) Space", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
