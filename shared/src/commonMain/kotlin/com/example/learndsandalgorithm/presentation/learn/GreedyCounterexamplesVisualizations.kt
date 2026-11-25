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
internal fun GreedyCounterexamplesVisualizationCard(title: String = "Greedy Counterexamples & Correctness") {
    val steps = GreedyCounterexamplesTrace.createTrace()
    val playback = rememberVisualizerPlaybackState(stepCount = steps.size, initialStep = 0)
    val presentation = remember(steps) { DedicatedGreedyCounterexamplesSceneBuilder.build(steps) }
    val stepIndex = playback.safeStepIndex
    val frame = presentation.frames[stepIndex]
    val step = frame.step
    val tone = when (step.status) {
        CounterexampleStatus.FINISHED -> GreedySceneTone.REJECTED
        CounterexampleStatus.COMPARE_OPTIMAL -> GreedySceneTone.SELECTED
        CounterexampleStatus.GREEDY_STEP_1, CounterexampleStatus.GREEDY_STEP_2, CounterexampleStatus.GREEDY_STEP_3 -> GreedySceneTone.ACTIVE
        else -> GreedySceneTone.ACCENT
    }
    val autoExpand = LocalAutoExpandVisualizer.current
    var expanded by rememberSaveable { mutableStateOf(autoExpand) }
    if (!autoExpand) ArrayWindowButton(text = "Open counterexamples visualizer") { expanded = true }
    if (expanded) {
        VisualizerModal(
            title = title,
            statusText = greedyCounterexampleStatusText(step),
            isCompleted = playback.isCompleted,
            stepIndex = stepIndex,
            stepCount = steps.size,
            isPlaying = playback.isPlaying,
            layoutMode = VisualizerLayoutMode.FULL_WIDTH,
            statusColor = when (tone) {
                GreedySceneTone.REJECTED -> Color(0xFFEF4444)
                GreedySceneTone.SELECTED -> Color(0xFF10B981)
                GreedySceneTone.ACCENT -> Color(0xFF38BDF8)
                else -> Color(0xFFFA6E13)
            },
            onReset = { playback.reset() },
            onPrevious = { playback.previous() },
            onTogglePlayback = { playback.togglePlayback() },
            onNext = { playback.next() },
            onClose = { playback.reset(); expanded = false },
            visualContent = { GreedyCounterexamplesSceneRenderer(presentation, frame) },
            detailsContent = {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("GREEDY PROOF VERDICT", color = Color(0xFF9CA3AF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                step.badgeText,
                                color = when (tone) {
                                    GreedySceneTone.REJECTED -> Color(0xFFEF4444)
                                    GreedySceneTone.SELECTED -> Color(0xFF10B981)
                                    GreedySceneTone.ACCENT -> Color(0xFF38BDF8)
                                    else -> Color(0xFFFA6E13)
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text("Proof by Counterexample", color = Color(0xFFFA6E13), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    StepsCard("Steps", step.explanation, stepIndex, steps.size, isCompleted = playback.isCompleted)
                }
            }
        )
    }
}

internal fun greedyCounterexampleStatusText(step: GreedyCounterexamplesStep): String =
    when (step.status) {
        CounterexampleStatus.INIT -> "COUNTEREXAMPLE: TARGET " + step.targetAmount + " WITH " + step.denominations
        CounterexampleStatus.GREEDY_STEP_1 -> "GREEDY CHOICE 1: PICK 4 (LARGEST)"
        CounterexampleStatus.GREEDY_STEP_2 -> "GREEDY CHOICE 2: PICK 1"
        CounterexampleStatus.GREEDY_STEP_3 -> "GREEDY CHOICE 3: PICK 1 (TOTAL 3 COINS)"
        CounterexampleStatus.COMPARE_OPTIMAL -> "OPTIMAL SOLUTION: [3, 3] (2 COINS)"
        CounterexampleStatus.FINISHED -> "COUNTEREXAMPLE PROVEN · GREEDY FAILS HERE"
    }
