package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9CA3AF)

@Composable
internal fun DivideConquerFundamentalsVisualizationCard(
    title: String = "Divide & Conquer Fundamentals"
) {
    val steps = DivideConquerFundamentalsTrace.stepsForMaxFinder()
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
        ArrayWindowButton(text = "Open divide & conquer visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.phase) {
            DivideConquerPhase.DIVIDE -> "1. DIVIDE SUBPROBLEMS"
            DivideConquerPhase.CONQUER -> "2. CONQUER BASE CASES"
            DivideConquerPhase.COMBINE -> if (step.isTerminal) "3. COMBINE FINAL RESULT" else "3. COMBINE MERGED RESULTS"
        }

        val statusColor = when (step.phase) {
            DivideConquerPhase.DIVIDE -> OrangeAccent
            DivideConquerPhase.CONQUER -> BlueAccent
            DivideConquerPhase.COMBINE -> GreenAccent
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
                    DivideConquerFundamentalsVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                DivideConquerFundamentalsDetailsAndStepsContent(
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
private fun DivideConquerFundamentalsVisualizationContent(step: FundamentalsTraceStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Phase Indicator Header Card
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
                        text = "THE 3 PHASES OF DIVIDE & CONQUER",
                        color = OrangeAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp * scale),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PhaseBadge("1. DIVIDE", step.phase == DivideConquerPhase.DIVIDE, OrangeAccent)
                        PhaseBadge("2. CONQUER", step.phase == DivideConquerPhase.CONQUER, BlueAccent)
                        PhaseBadge("3. COMBINE", step.phase == DivideConquerPhase.COMBINE, GreenAccent)
                    }
                }
            }
        }

        // Subproblem Decomposition Display
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
                Text(
                    text = "RECURSIVE SUBPROBLEM TREE (max(arr, low, high)):",
                    color = BlueAccent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                // Render Nodes
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp * scale)
                ) {
                    step.nodes.chunked(3).forEach { rowNodes ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            rowNodes.forEach { node ->
                                val isActive = node.id == step.activeSubproblemId
                                Surface(
                                    color = when {
                                        isActive && step.phase == DivideConquerPhase.COMBINE -> Color(0xFF123820)
                                        isActive -> Color(0xFF2A1C12)
                                        node.isBaseCase -> Color(0xFF13283A)
                                        else -> Color(0xFF181824)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        when {
                                            isActive && step.phase == DivideConquerPhase.COMBINE -> GreenAccent
                                            isActive -> OrangeAccent
                                            node.isBaseCase -> BlueAccent
                                            else -> CardBorderColor
                                        }
                                    ),
                                    modifier = Modifier.weight(1f).height(46.dp * scale).padding(horizontal = 2.dp * scale)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(2.dp * scale)
                                    ) {
                                        Text(
                                            text = "[${node.elements.joinToString(",")}]",
                                            color = TextPrimary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (node.maxValue != null) {
                                            Text(
                                                text = "max=${node.maxValue}",
                                                color = GreenAccent,
                                                fontSize = 9.sp,
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
    }
}

@Composable
private fun PhaseBadge(text: String, isActive: Boolean, activeColor: Color) {
    Surface(
        color = if (isActive) activeColor.copy(alpha = 0.2f) else Color(0xFF1A1A26),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, if (isActive) activeColor else CardBorderColor)
    ) {
        Text(
            text = text,
            color = if (isActive) activeColor else TextSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun DivideConquerFundamentalsDetailsAndStepsContent(
    step: FundamentalsTraceStep,
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
                Text("CURRENT PHASE", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.phase) {
                        DivideConquerPhase.DIVIDE -> OrangeAccent
                        DivideConquerPhase.CONQUER -> BlueAccent
                        DivideConquerPhase.COMBINE -> GreenAccent
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (step.combinedValue != null) {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("max(${step.leftValue}, ${step.rightValue}) =", color = TextSecondary, fontSize = 10.sp)
                    Text("${step.combinedValue}", color = GreenAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
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
