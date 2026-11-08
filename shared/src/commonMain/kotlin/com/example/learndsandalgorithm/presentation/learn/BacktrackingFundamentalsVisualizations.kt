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
internal fun BacktrackingFundamentalsVisualizationCard(
    title: String = "Backtracking Fundamentals"
) {
    val steps = BacktrackingFundamentalsTrace.createTrace()
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val playback = rememberVisualizerPlaybackState(
        stepCount = steps.size,
        initialStep = 0
    )
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val step = steps[stepIndex]

    ArrayWindowButton(text = "Open backtracking fundamentals visualizer") {
        isExpanded = true
    }

    if (isExpanded) {
        val statusText = when (step.action) {
            BacktrackingFundamentalsAction.INIT -> "STATE-SPACE TREE: LENGTH ${step.targetLength}"
            BacktrackingFundamentalsAction.CHOOSE_0 -> "CHOICE: APPEND '0' -> '${step.currentString}'"
            BacktrackingFundamentalsAction.CHOOSE_1 -> "CHOICE: APPEND '1' -> '${step.currentString}'"
            BacktrackingFundamentalsAction.SOLUTION_FOUND -> "SOLUTION FOUND: '${step.currentString}'"
            BacktrackingFundamentalsAction.BACKTRACK -> "UNDO / BACKTRACK TO '${step.currentString}'"
            BacktrackingFundamentalsAction.FINISHED -> "SEARCH COMPLETE · ${step.generatedStrings.size} STRINGS"
        }

        val statusColor = when (step.action) {
            BacktrackingFundamentalsAction.SOLUTION_FOUND, BacktrackingFundamentalsAction.FINISHED -> GreenAccent
            BacktrackingFundamentalsAction.BACKTRACK -> RedAccent
            BacktrackingFundamentalsAction.CHOOSE_0, BacktrackingFundamentalsAction.CHOOSE_1 -> OrangeAccent
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
                    BacktrackingFundamentalsVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                BacktrackingFundamentalsDetailsAndStepsContent(
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
private fun BacktrackingFundamentalsVisualizationContent(step: BacktrackingFundamentalsStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Active Partial Candidate Box
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
                    Text("CURRENT CANDIDATE STATE", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (step.currentString.isEmpty()) "(root: \"\")" else "\"${step.currentString}\"",
                        color = OrangeAccent,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = when (step.action) {
                        BacktrackingFundamentalsAction.SOLUTION_FOUND -> Color(0xFF123820)
                        BacktrackingFundamentalsAction.BACKTRACK -> Color(0xFF381215)
                        else -> Color(0xFF2A1C12)
                    },
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(
                        1.dp,
                        when (step.action) {
                            BacktrackingFundamentalsAction.SOLUTION_FOUND -> GreenAccent
                            BacktrackingFundamentalsAction.BACKTRACK -> RedAccent
                            else -> OrangeAccent
                        }
                    )
                ) {
                    Text(
                        text = "Level ${step.decisionLevel} / ${step.targetLength}",
                        color = TextPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp * scale, vertical = 4.dp * scale)
                    )
                }
            }
        }

        // State-Space Tree Visualization
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
                Text("STATE-SPACE TREE (BINARY DECISION TREE):", color = BlueAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                // Level 0 (Root)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val isRootActive = step.currentString == ""
                    Surface(
                        color = if (isRootActive) Color(0xFF2A1C12) else Color(0xFF181824),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, if (isRootActive) OrangeAccent else CardBorderColor)
                    ) {
                        Text("Root (\"\")", color = TextPrimary, fontSize = 10.sp, modifier = Modifier.padding(6.dp * scale))
                    }
                }

                // Level 1 Nodes ("0", "1")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    listOf("0", "1").forEach { node ->
                        val isActive = step.currentString == node || step.currentString.startsWith(node)
                        Surface(
                            color = if (isActive) Color(0xFF2A1C12) else Color(0xFF181824),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, if (isActive) OrangeAccent else CardBorderColor)
                        ) {
                            Text("Choice '$node'", color = if (isActive) OrangeAccent else TextSecondary, fontSize = 10.sp, modifier = Modifier.padding(6.dp * scale))
                        }
                    }
                }

                // Level 2 Nodes ("00", "01", "10", "11")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("00", "01", "10", "11").forEach { leaf ->
                        val isMatched = step.generatedStrings.contains(leaf)
                        val isCurrentLeaf = step.currentString == leaf

                        Surface(
                            color = when {
                                isCurrentLeaf && step.action == BacktrackingFundamentalsAction.SOLUTION_FOUND -> Color(0xFF123820)
                                isMatched -> Color(0xFF123820)
                                isCurrentLeaf -> Color(0xFF2A1C12)
                                else -> Color(0xFF181824)
                            },
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(
                                1.dp,
                                when {
                                    isMatched -> GreenAccent
                                    isCurrentLeaf -> OrangeAccent
                                    else -> CardBorderColor
                                }
                            )
                        ) {
                            Text(
                                leaf,
                                color = if (isMatched) GreenAccent else TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp * scale, vertical = 4.dp * scale)
                            )
                        }
                    }
                }
            }
        }

        // Generated Solutions Summary Card
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
                    text = "Generated: [${step.generatedStrings.joinToString()}]",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${step.generatedStrings.size} / 4 Solutions",
                    color = GreenAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun BacktrackingFundamentalsDetailsAndStepsContent(
    step: BacktrackingFundamentalsStep,
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
                Text("BACKTRACKING DECISION", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.action) {
                        BacktrackingFundamentalsAction.SOLUTION_FOUND, BacktrackingFundamentalsAction.FINISHED -> GreenAccent
                        BacktrackingFundamentalsAction.BACKTRACK -> RedAccent
                        else -> OrangeAccent
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Complexity: O(2^N)", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
