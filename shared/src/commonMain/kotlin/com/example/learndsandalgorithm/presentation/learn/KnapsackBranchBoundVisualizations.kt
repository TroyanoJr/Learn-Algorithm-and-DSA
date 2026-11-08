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
internal fun KnapsackBranchBoundVisualizationCard(
    title: String = "0/1 Knapsack with Branch and Bound"
) {
    val steps = KnapsackBranchBoundTrace.createTrace()
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val playback = rememberVisualizerPlaybackState(
        stepCount = steps.size,
        initialStep = 0
    )
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val step = steps[stepIndex]

    ArrayWindowButton(text = "Open knapsack B&B visualizer") {
        isExpanded = true
    }

    if (isExpanded) {
        val statusText = when (step.action) {
            KnapsackBbAction.INIT -> "0/1 KNAPSACK B&B: CAPACITY ${step.capacity}"
            KnapsackBbAction.COMPUTE_UPPER_BOUND -> "COMPUTE FRACTIONAL UPPER BOUND = ${step.currentNode?.upperBound}"
            KnapsackBbAction.BRANCH_INCLUDE -> "BRANCH INCLUDE: ${step.currentNode?.id}"
            KnapsackBbAction.BRANCH_EXCLUDE -> "BRANCH EXCLUDE: ${step.currentNode?.id}"
            KnapsackBbAction.UPDATE_INCUMBENT -> "UPDATED 0/1 INCUMBENT VALUE = ${step.incumbentValue}"
            KnapsackBbAction.PRUNE_NODE -> "PRUNED NODE: BOUND ${step.currentNode?.upperBound} <= INCUMBENT ${step.incumbentValue}"
            KnapsackBbAction.FINISHED -> "KNAPSACK B&B COMPLETE · OPTIMAL VALUE = ${step.incumbentValue}"
        }

        val statusColor = when (step.action) {
            KnapsackBbAction.UPDATE_INCUMBENT, KnapsackBbAction.FINISHED -> GreenAccent
            KnapsackBbAction.PRUNE_NODE -> RedAccent
            KnapsackBbAction.BRANCH_INCLUDE, KnapsackBbAction.BRANCH_EXCLUDE, KnapsackBbAction.COMPUTE_UPPER_BOUND -> OrangeAccent
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
                    KnapsackBranchBoundVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                KnapsackBranchBoundDetailsAndStepsContent(
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
private fun KnapsackBranchBoundVisualizationContent(step: KnapsackBranchBoundStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Incumbent Threshold Card
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
                    Text("CURRENT 0/1 INCUMBENT VALUE", color = OrangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (step.incumbentItems.isEmpty()) "0 (None)" else "${step.incumbentValue} (${step.incumbentItems.joinToString()})",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    color = Color(0xFF123820),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, GreenAccent)
                ) {
                    Text(
                        text = "CAP ${step.capacity}",
                        color = GreenAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp * scale, vertical = 4.dp * scale)
                    )
                }
            }
        }

        // Items Ratio Row Card
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
                Text("ITEMS SORTED BY VALUE/WEIGHT RATIO:", color = BlueAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    step.items.forEach { item ->
                        val isSelected = step.incumbentItems.contains(item.name)

                        Surface(
                            color = if (isSelected) Color(0xFF123820) else Color(0xFF181824),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, if (isSelected) GreenAccent else CardBorderColor),
                            modifier = Modifier.weight(1f).padding(horizontal = 2.dp * scale)
                        ) {
                            Column(
                                modifier = Modifier.padding(6.dp * scale),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(item.name, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("w:${item.weight} v:${item.value}", color = TextSecondary, fontSize = 9.sp)
                                Text("Ratio: ${item.ratio}", color = OrangeAccent, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Active Node Fractional Upper Bound Summary Card
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
                    text = "Node: ${step.currentNode?.id ?: "None"}",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Fractional UB: ${step.currentNode?.upperBound ?: "-"}",
                    color = OrangeAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun KnapsackBranchBoundDetailsAndStepsContent(
    step: KnapsackBranchBoundStep,
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
                Text("KNAPSACK B&B DECISION", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.action) {
                        KnapsackBbAction.UPDATE_INCUMBENT, KnapsackBbAction.FINISHED -> GreenAccent
                        KnapsackBbAction.PRUNE_NODE -> RedAccent
                        else -> OrangeAccent
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Fractional Relaxation Bound", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
