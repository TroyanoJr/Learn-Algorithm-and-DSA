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
internal fun GreedyCoinChangeVisualizationCard(title: String = "Greedy Coin Change") {
    val steps = GreedyCoinChangeTrace.createTrace()
    val playback = rememberVisualizerPlaybackState(stepCount = steps.size, initialStep = 0)
    val presentation = remember(steps) { DedicatedGreedyCoinChangeSceneBuilder.build(steps) }
    val stepIndex = playback.safeStepIndex
    val frame = presentation.frames[stepIndex]
    val step = frame.step
    val badgeTone = when (step.status) {
        CoinChangeStatus.TAKE_COIN, CoinChangeStatus.FINISHED -> GreedySceneTone.SELECTED
        CoinChangeStatus.SKIP_COIN -> GreedySceneTone.REJECTED
        CoinChangeStatus.CONSIDERING -> GreedySceneTone.ACTIVE
        CoinChangeStatus.INIT -> GreedySceneTone.ACCENT
    }
    val status = greedyCoinChangeStatusText(step)
    val autoExpand = LocalAutoExpandVisualizer.current
    var expanded by rememberSaveable { mutableStateOf(autoExpand) }
    if (!autoExpand) ArrayWindowButton(text = "Open coin change visualizer") { expanded = true }
    if (expanded) {
        VisualizerModal(
            title = title,
            statusText = status,
            isCompleted = playback.isCompleted,
            stepIndex = stepIndex,
            stepCount = steps.size,
            isPlaying = playback.isPlaying,
            layoutMode = VisualizerLayoutMode.FULL_WIDTH,
            statusColor = when (badgeTone) {
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
            visualContent = { GreedyCoinChangeSceneRenderer(presentation, frame) },
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
                                color = when (badgeTone) {
                                    GreedySceneTone.SELECTED -> Color(0xFF10B981)
                                    GreedySceneTone.REJECTED -> Color(0xFFEF4444)
                                    GreedySceneTone.ACCENT -> Color(0xFF38BDF8)
                                    else -> Color(0xFFFA6E13)
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text("Time: O(D + K)", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    StepsCard("Steps", step.explanation, stepIndex, steps.size, isCompleted = playback.isCompleted)
                }
            }
        )
    }
}

internal fun greedyCoinChangeStatusText(step: GreedyCoinChangeStep): String =
    when (step.status) {
        CoinChangeStatus.INIT -> "GREEDY COIN CHANGE: TARGET " + step.targetAmount
        CoinChangeStatus.CONSIDERING -> "CONSIDERING COIN " + step.currentCoinValue
        CoinChangeStatus.TAKE_COIN -> "TAKING COIN " + step.currentCoinValue
        CoinChangeStatus.SKIP_COIN -> "SKIPPING COIN " + step.currentCoinValue + " (TOO LARGE)"
        CoinChangeStatus.FINISHED -> "TARGET REACHED · " + step.selectedCoins.size + " COINS"
    }
