package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
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
internal fun FractionalKnapsackVisualizationCard(
    title: String = "Fractional Knapsack"
) {
    val steps = FractionalKnapsackTrace.createTrace()
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val playback = rememberVisualizerPlaybackState(
        stepCount = steps.size,
        initialStep = 0
    )
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val step = steps[stepIndex]

    ArrayWindowButton(text = "Open fractional knapsack visualizer") {
        isExpanded = true
    }

    if (isExpanded) {
        val statusText = when (step.status) {
            FractionalKnapsackStatus.INIT -> "FRACTIONAL KNAPSACK: CAPACITY ${step.capacity.toInt()}"
            FractionalKnapsackStatus.SORTED -> "SORTED ITEMS BY VALUE/WEIGHT RATIO"
            FractionalKnapsackStatus.EVALUATING -> "EVALUATING ITEM ${step.currentItem?.name}"
            FractionalKnapsackStatus.TAKE_FULL -> "TAKEN 100% OF ${step.currentItem?.name}"
            FractionalKnapsackStatus.TAKE_FRACTION -> "TAKEN FRACTION OF ${step.currentItem?.name}"
            FractionalKnapsackStatus.FINISHED -> "KNAPSACK FILLED · TOTAL VALUE ${step.totalValue.toInt()}"
        }

        val statusColor = when (step.status) {
            FractionalKnapsackStatus.TAKE_FULL, FractionalKnapsackStatus.TAKE_FRACTION, FractionalKnapsackStatus.FINISHED -> GreenAccent
            FractionalKnapsackStatus.EVALUATING, FractionalKnapsackStatus.SORTED -> OrangeAccent
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
                    FractionalKnapsackVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                FractionalKnapsackDetailsAndStepsContent(
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
private fun FractionalKnapsackVisualizationContent(step: FractionalKnapsackStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Knapsack Capacity Fill Card
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
                val filledWeight = step.capacity - step.remainingCapacity
                val fillProgress = (filledWeight / step.capacity).toFloat().coerceIn(0f, 1f)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "KNAPSACK CAPACITY: ${step.capacity.toInt()} W",
                        color = OrangeAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Filled: ${filledWeight.toInt()} / ${step.capacity.toInt()}",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Capacity Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp * scale)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF22222E))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fillProgress)
                            .height(12.dp * scale)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (fillProgress >= 1f) GreenAccent else BlueAccent)
                    )
                }
            }
        }

        // Items Ratio Row
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
                Text("ITEMS SORTED BY VALUE / WEIGHT RATIO:", color = BlueAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    step.sortedItems.forEachIndexed { idx, item ->
                        val isCurrent = step.currentItemIndex == idx
                        val selection = step.selections.firstOrNull { it.item.name == item.name }
                        val isFull = selection?.fraction == 1.0
                        val isFractional = selection != null && selection.fraction < 1.0

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp * scale),
                            modifier = Modifier.weight(1f).padding(horizontal = 2.dp * scale)
                        ) {
                            Text(
                                text = when {
                                    isFull -> "100%"
                                    isFractional -> "${(selection.fraction * 100).toInt()}%"
                                    isCurrent -> "TESTING"
                                    else -> "PENDING"
                                },
                                color = when {
                                    isFull -> GreenAccent
                                    isFractional -> BlueAccent
                                    isCurrent -> OrangeAccent
                                    else -> TextSecondary
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Surface(
                                color = when {
                                    isFull -> Color(0xFF123820)
                                    isFractional -> Color(0xFF122C38)
                                    isCurrent -> Color(0xFF2A1C12)
                                    else -> Color(0xFF181824)
                                },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(
                                    1.dp,
                                    when {
                                        isFull -> GreenAccent
                                        isFractional -> BlueAccent
                                        isCurrent -> OrangeAccent
                                        else -> CardBorderColor
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth().height(52.dp * scale)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text("Item ${item.name}", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("w:${item.weight.toInt()} v:${item.value.toInt()}", color = TextSecondary, fontSize = 9.sp)
                                    Text("Ratio: ${item.ratio}", color = OrangeAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Total Value Summary Card
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
                    Text("TOTAL KNAPSACK VALUE", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${step.totalValue.toInt()} Value Units",
                        color = GreenAccent,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = Color(0xFF123820),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, GreenAccent)
                ) {
                    Text(
                        text = "OPTIMAL FOR FRACTIONAL",
                        color = GreenAccent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp * scale, vertical = 4.dp * scale)
                    )
                }
            }
        }
    }
}

@Composable
private fun FractionalKnapsackDetailsAndStepsContent(
    step: FractionalKnapsackStep,
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
                Text("GREEDY DECISION", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.status) {
                        FractionalKnapsackStatus.TAKE_FULL, FractionalKnapsackStatus.TAKE_FRACTION, FractionalKnapsackStatus.FINISHED -> GreenAccent
                        else -> OrangeAccent
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Complexity: O(N log N)", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
