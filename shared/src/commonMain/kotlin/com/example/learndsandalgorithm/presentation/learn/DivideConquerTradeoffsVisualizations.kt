package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
internal fun DivideConquerTradeoffsVisualizationCard(
    title: String = "Divide & Conquer Complexity and Trade-offs"
) {
    var selectedScenario by rememberSaveable { mutableStateOf(TradeoffScenario.BALANCED_VS_UNBALANCED_SPLIT) }
    val steps = DivideConquerTradeoffsTrace.stepsForScenario(selectedScenario)
    val autoExpand = LocalAutoExpandVisualizer.current
    var isExpanded by rememberSaveable { mutableStateOf(autoExpand) }

    val playback = rememberVisualizerPlaybackState(
        stepCount = steps.size,
        initialStep = 0,
        key = selectedScenario
    )
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val step = steps[stepIndex]

    if (!autoExpand) {
        ArrayWindowButton(text = "Open complexity & trade-offs visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.scenario) {
            TradeoffScenario.BALANCED_VS_UNBALANCED_SPLIT -> "PARTITION SPLIT BALANCE (N = ${step.inputN})"
            TradeoffScenario.ALGORITHM_COMPARISON -> "MERGE SORT vs QUICK SORT TRADE-OFFS"
        }

        val statusColor = when (step.scenario) {
            TradeoffScenario.BALANCED_VS_UNBALANCED_SPLIT -> OrangeAccent
            TradeoffScenario.ALGORITHM_COMPARISON -> BlueAccent
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
            topContent = {
                TradeoffScenarioSelector(
                    selectedScenario = selectedScenario,
                    onScenarioSelected = { newScenario ->
                        if (newScenario != selectedScenario) {
                            playback.reset()
                            selectedScenario = newScenario
                        }
                    }
                )
            },
            visualContent = {
                FitToPane(modifier = Modifier.fillMaxSize()) {
                    DivideConquerTradeoffsVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                DivideConquerTradeoffsDetailsAndStepsContent(
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
private fun TradeoffScenarioSelector(
    selectedScenario: TradeoffScenario,
    onScenarioSelected: (TradeoffScenario) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 6.dp)
    ) {
        listOf(
            TradeoffScenario.BALANCED_VS_UNBALANCED_SPLIT to "1. Partition Split Balance",
            TradeoffScenario.ALGORITHM_COMPARISON to "2. Merge vs Quick Comparison"
        ).forEach { (scenario, label) ->
            val isSelected = selectedScenario == scenario
            Surface(
                color = if (isSelected) Color(0xFF2A1C12) else Color(0xFF22222E),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (isSelected) OrangeAccent else CardBorderColor),
                modifier = Modifier
                    .heightIn(min = 38.dp)
                    .clickable { onScenarioSelected(scenario) }
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) OrangeAccent else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DivideConquerTradeoffsVisualizationContent(step: TradeoffsTraceStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        when (step.scenario) {
            TradeoffScenario.BALANCED_VS_UNBALANCED_SPLIT -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp * scale)
                ) {
                    // Balanced Split Card
                    Surface(
                        color = Color(0xFF13131C),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CardBorderColor),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp * scale),
                            verticalArrangement = Arrangement.spacedBy(4.dp * scale)
                        ) {
                            Text("BALANCED SPLIT (N/2, N/2)", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            if (step.goodLeft != null) {
                                Text("Left: ${step.goodLeft} | Right: ${step.goodRight}", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Tree Depth: log₂(${step.inputN}) ≈ ${step.goodTreeDepth} levels", color = GreenAccent, fontSize = 10.sp)
                                Text("Time Complexity: O(N log N)", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Text("Pending split evaluation...", color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }

                    // Unbalanced Split Card
                    Surface(
                        color = Color(0xFF13131C),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CardBorderColor),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp * scale),
                            verticalArrangement = Arrangement.spacedBy(4.dp * scale)
                        ) {
                            Text("UNBALANCED SPLIT (1, N-1)", color = RedAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            if (step.badLeft != null) {
                                Text("Left: ${step.badLeft} | Right: ${step.badRight}", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Tree Depth: N = ${step.badTreeDepth} levels (Linear Chain!)", color = RedAccent, fontSize = 10.sp)
                                Text("Time Complexity: O(N²) Worst Case", color = RedAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Text("Pending split evaluation...", color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            TradeoffScenario.ALGORITHM_COMPARISON -> {
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
                        Text("COMPLEXITY & TRADE-OFF MATRIX:", color = BlueAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                        // Comparison Table Header
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Property", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("Merge Sort", color = GreenAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                            Text("Quick Sort", color = OrangeAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                        }

                        ComparisonRow("Time (Avg)", "O(N log N)", "O(N log N)")
                        ComparisonRow("Time (Worst)", "O(N log N) Guaranteed", "O(N²) Degraded")
                        ComparisonRow("Aux Space", "O(N) Extra Memory", "O(log N) Stack (In-place)")
                        ComparisonRow("Stability", "STABLE (Preserves Order)", "UNSTABLE (Swaps Items)")
                    }
                }
            }
        }
    }
}

@Composable
private fun ComparisonRow(property: String, mergeVal: String, quickVal: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(property, color = TextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text(mergeVal, color = GreenAccent, fontSize = 9.sp, modifier = Modifier.weight(1.2f))
        Text(quickVal, color = OrangeAccent, fontSize = 9.sp, modifier = Modifier.weight(1.2f))
    }
}

@Composable
private fun DivideConquerTradeoffsDetailsAndStepsContent(
    step: TradeoffsTraceStep,
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
                Text("ANALYSIS SCENARIO", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.scenario) {
                        TradeoffScenario.BALANCED_VS_UNBALANCED_SPLIT -> OrangeAccent
                        TradeoffScenario.ALGORITHM_COMPARISON -> BlueAccent
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Divide & Conquer Complexity", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
