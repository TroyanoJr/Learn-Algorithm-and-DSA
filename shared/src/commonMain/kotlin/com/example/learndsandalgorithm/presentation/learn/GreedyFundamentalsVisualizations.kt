package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun GreedyFundamentalsVisualizationCard(title: String = "Greedy Algorithm Fundamentals") {
    val steps = GreedyFundamentalsTrace.createTrace()
    val playback = rememberVisualizerPlaybackState(stepCount = steps.size, initialStep = 0)
    val frames = remember(steps) { GreedyFundamentalsSceneSequenceBuilder.build(steps) }
    val stepIndex = playback.safeStepIndex
    val frame = frames[stepIndex]
    val step = frame.step
    val tone = when (step.status) {
        GreedyFundamentalsStatus.SELECTED, GreedyFundamentalsStatus.FINISHED -> GreedySceneTone.SELECTED
        GreedyFundamentalsStatus.REJECTED -> GreedySceneTone.REJECTED
        GreedyFundamentalsStatus.EVALUATING, GreedyFundamentalsStatus.SORTING -> GreedySceneTone.ACTIVE
        else -> GreedySceneTone.ACCENT
    }
    val status = greedyFundamentalsStatusText(step)
    val autoExpand = LocalAutoExpandVisualizer.current
    var expanded by rememberSaveable { mutableStateOf(autoExpand) }
    if (!autoExpand) ArrayWindowButton(text = "Open greedy fundamentals visualizer") { expanded = true }
    if (expanded) VisualizerModal(
        title = title,
        statusText = status,
        isCompleted = playback.isCompleted,
        stepIndex = stepIndex,
        stepCount = steps.size,
        isPlaying = playback.isPlaying,
        layoutMode = VisualizerLayoutMode.FULL_WIDTH,
        statusColor = when (tone) {
            GreedySceneTone.SELECTED -> Color(0xFF10B981)
            GreedySceneTone.REJECTED -> Color(0xFFEF4444)
            else -> Color(0xFF38BDF8)
        },
        onReset = { playback.reset() },
        onPrevious = { playback.previous() },
        onTogglePlayback = { playback.togglePlayback() },
        onNext = { playback.next() },
        onClose = { playback.reset(); expanded = false },
        visualContent = { GreedyFundamentalsSceneRenderer(frame) },
        detailsContent = {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("GREEDY DECISION", color = Color(0xFF9CA3AF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(
                            step.badgeText,
                            color = when (step.status) {
                                GreedyFundamentalsStatus.SELECTED, GreedyFundamentalsStatus.FINISHED -> Color(0xFF10B981)
                                GreedyFundamentalsStatus.REJECTED -> Color(0xFFEF4444)
                                else -> Color(0xFFFA6E13)
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("SORTING", color = Color(0xFF9CA3AF), fontSize = 9.sp)
                        Text("O(N log N)", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("SCANNING O(N)", color = Color(0xFF9CA3AF), fontSize = 9.sp)
                    }
                }
                StepsCard("Steps", step.explanation, stepIndex, steps.size, isCompleted = playback.isCompleted)
            }
        }
    )
}

internal fun greedyFundamentalsStatusText(step: GreedyFundamentalsStep): String =
    when (step.status) {
        GreedyFundamentalsStatus.INIT -> "GREEDY FUNDAMENTALS: BUDGET " + step.totalBudget
        GreedyFundamentalsStatus.SORTING -> "SORTING CANDIDATES (ASCENDING)"
        GreedyFundamentalsStatus.EVALUATING -> "EVALUATING DISTANCE " + step.currentCandidateDistance
        GreedyFundamentalsStatus.SELECTED -> "SELECTED DISTANCE " + step.currentCandidateDistance
        GreedyFundamentalsStatus.REJECTED -> "EXCEEDS REMAINING BUDGET"
        GreedyFundamentalsStatus.FINISHED -> "SEARCH COMPLETE · OPTIMAL RESULT"
    }
