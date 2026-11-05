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
internal fun Knapsack01DpVisualizationCard(
    title: String = "0/1 Knapsack Problem"
) {
    val steps = Knapsack01DpTrace.createTrace()
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val playback = rememberVisualizerPlaybackState(
        stepCount = steps.size,
        initialStep = 0
    )
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val step = steps[stepIndex]

    ArrayWindowButton(text = "Open 0/1 knapsack visualizer") {
        isExpanded = true
    }

    if (isExpanded) {
        val statusText = when (step.action) {
            Knapsack01Action.INIT -> "0/1 KNAPSACK DP: CAPACITY ${step.capacity}"
            Knapsack01Action.EVALUATE_ITEM_CAP -> "EVAL ITEM ${step.currentItemIndex?.let { step.items[it].name }} @ CAP ${step.currentCap}: EX=${step.excludeVal} vs INC=${step.includeVal}"
            Knapsack01Action.UPDATE_DP -> "UPDATE dp[${step.currentCap}] = ${step.dpTable[step.currentCap ?: 0]}"
            Knapsack01Action.FINISHED -> "0/1 KNAPSACK COMPLETE · MAX VALUE = ${step.dpTable[step.capacity]}"
        }

        val statusColor = when (step.action) {
            Knapsack01Action.UPDATE_DP, Knapsack01Action.FINISHED -> GreenAccent
            Knapsack01Action.EVALUATE_ITEM_CAP -> OrangeAccent
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
                    Knapsack01DpVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                Knapsack01DpDetailsAndStepsContent(
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
private fun Knapsack01DpVisualizationContent(step: Knapsack01DpStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Items & Capacity Header Card
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
                    Text("0/1 KNAPSACK ITEMS:", color = OrangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(
                        step.items.joinToString(" | ") { "${it.name}(w:${it.weight}, v:${it.value})" },
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }

                Surface(
                    color = Color(0xFF122C38),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, BlueAccent)
                ) {
                    Text(
                        "CAPACITY ${step.capacity}",
                        color = BlueAccent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp * scale, vertical = 4.dp * scale)
                    )
                }
            }
        }

        // 1D DP Array Card
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
                Text("1D BACKWARD DP TABLE dp[c] (MAX VALUE):", color = BlueAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    step.dpTable.forEachIndexed { cap, valVal ->
                        val isCurrentCap = step.currentCap == cap
                        val isNonZero = valVal > 0

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp * scale),
                            modifier = Modifier.weight(1f).padding(horizontal = 1.dp * scale)
                        ) {
                            Text(
                                text = "c=$cap",
                                color = if (isCurrentCap) OrangeAccent else TextSecondary,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Surface(
                                color = when {
                                    isCurrentCap && step.action == Knapsack01Action.UPDATE_DP -> Color(0xFF123820)
                                    isCurrentCap -> Color(0xFF2A1C12)
                                    isNonZero -> Color(0xFF122C38)
                                    else -> Color(0xFF181824)
                                },
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(
                                    1.dp,
                                    when {
                                        isCurrentCap && step.action == Knapsack01Action.UPDATE_DP -> GreenAccent
                                        isCurrentCap -> OrangeAccent
                                        isNonZero -> BlueAccent
                                        else -> CardBorderColor
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth().height(42.dp * scale)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "$valVal",
                                        color = if (isNonZero) TextPrimary else TextSecondary,
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

        // Decision Summary Card
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
                    text = "Exclude: ${step.excludeVal ?: "-"} | Include: ${step.includeVal ?: "-"}",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Max: ${step.dpTable[step.capacity]}",
                    color = GreenAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun Knapsack01DpDetailsAndStepsContent(
    step: Knapsack01DpStep,
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
                Text("0/1 KNAPSACK DECISION", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.action) {
                        Knapsack01Action.UPDATE_DP, Knapsack01Action.FINISHED -> GreenAccent
                        else -> OrangeAccent
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Complexity: O(N*W) Time, O(W) Space", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
