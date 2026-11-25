package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun FractionalKnapsackVisualizationCard(title: String = "Fractional Knapsack") {
    val steps = FractionalKnapsackTrace.createTrace()
    val playback = rememberVisualizerPlaybackState(stepCount = steps.size, initialStep = 0)
    val presentation = remember(steps) { DedicatedFractionalKnapsackSceneBuilder.build(steps) }
    val stepIndex = playback.safeStepIndex
    val frame = presentation.frames[stepIndex]
    val step = frame.step
    val tone = when (step.status) {
        FractionalKnapsackStatus.TAKE_FULL, FractionalKnapsackStatus.TAKE_FRACTION, FractionalKnapsackStatus.FINISHED -> GreedySceneTone.SELECTED
        FractionalKnapsackStatus.EVALUATING, FractionalKnapsackStatus.SORTED -> GreedySceneTone.ACTIVE
        else -> GreedySceneTone.ACCENT
    }
    val statusText = fractionalKnapsackStatusText(step)
    val autoExpand = LocalAutoExpandVisualizer.current
    var expanded by rememberSaveable { mutableStateOf(autoExpand) }
    if (!autoExpand) ArrayWindowButton(text = "Open fractional knapsack visualizer") { expanded = true }
    if (expanded) {
        VisualizerModal(
            title = title,
            statusText = statusText,
            isCompleted = playback.isCompleted,
            stepIndex = stepIndex,
            stepCount = steps.size,
            isPlaying = playback.isPlaying,
            layoutMode = VisualizerLayoutMode.FULL_WIDTH,
            statusColor = when (tone) {
                GreedySceneTone.SELECTED -> Color(0xFF10B981)
                GreedySceneTone.REJECTED -> Color(0xFFEF4444)
                GreedySceneTone.ACCENT -> Color(0xFF38BDF8)
                else -> Color(0xFFFA6E13)
            },
            onReset = { playback.reset() },
            onPrevious = { playback.previous() },
            onTogglePlayback = { playback.togglePlayback() },
            onNext = { playback.next() },
            onClose = { playback.reset(); expanded = false },
            visualContent = { FractionalKnapsackSceneRenderer(presentation, frame) },
            detailsContent = {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("GREEDY DECISION", color = Color(0xFF9CA3AF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                step.badgeText,
                                color = if (tone == GreedySceneTone.SELECTED) Color(0xFF10B981) else Color(0xFFFA6E13),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text("Complexity: O(N log N)", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    StepsCard("Steps", step.explanation, stepIndex, steps.size, isCompleted = playback.isCompleted)
                }
            }
        )
    }
}

internal fun fractionalKnapsackStatusText(step: FractionalKnapsackStep): String =
    when (step.status) {
        FractionalKnapsackStatus.INIT -> "FRACTIONAL KNAPSACK: CAPACITY " + formatGreedyDecimal(step.capacity)
        FractionalKnapsackStatus.SORTED -> "SORTED ITEMS BY VALUE/WEIGHT RATIO"
        FractionalKnapsackStatus.EVALUATING -> "EVALUATING ITEM " + step.currentItem?.name
        FractionalKnapsackStatus.TAKE_FULL -> "TAKEN 100% OF " + step.currentItem?.name
        FractionalKnapsackStatus.TAKE_FRACTION -> "TAKEN FRACTION OF " + step.currentItem?.name
        FractionalKnapsackStatus.FINISHED -> if (step.remainingCapacity <= 0.0) {
            "KNAPSACK FILLED · TOTAL VALUE " + formatGreedyDecimal(step.totalValue)
        } else {
            "SELECTION COMPLETE · TOTAL VALUE " + formatGreedyDecimal(step.totalValue)
        }
    }
