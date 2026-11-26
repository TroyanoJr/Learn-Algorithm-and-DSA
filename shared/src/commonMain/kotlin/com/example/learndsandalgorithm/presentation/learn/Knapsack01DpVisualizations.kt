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
import androidx.compose.runtime.remember
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
    val steps = remember { Knapsack01DpTrace.createTrace() }
    val scene = remember(steps) { Knapsack01DpSceneBuilder.build(steps) }
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
        ArrayWindowButton(text = "Open 0/1 knapsack visualizer") {
            isExpanded = true
        }
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
            detailsScrollable = false,
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
                Knapsack01DpSceneRenderer(scene, scene.frames[stepIndex])
            },
            detailsContent = {
                DynamicProgrammingDetailsPanel("0/1 KNAPSACK DECISION", step.badgeText, statusColor,
                    "Time O(N·W) · space O(W)", step.explanation, stepIndex, steps.size, isCompleted)
            }
        )
    }
}
