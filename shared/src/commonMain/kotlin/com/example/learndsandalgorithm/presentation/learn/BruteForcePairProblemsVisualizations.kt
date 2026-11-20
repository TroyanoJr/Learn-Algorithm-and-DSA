package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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

private val PairOrange = Color(0xFFFA6E13)
private val PairBlue = Color(0xFF38BDF8)
private val PairGreen = Color(0xFF10B981)
private val PairRed = Color(0xFFEF4444)
private val PairSecondary = Color(0xFF9CA3AF)

@Composable
internal fun BruteForcePairProblemsVisualizationCard(title: String = "Brute Force for Pair Problems") {
    val steps = BruteForcePairProblemsTrace.stepsForFindPair()
    val frames = remember(steps) { BruteForceSceneBuilder.pairs(steps) }
    val autoExpand = LocalAutoExpandVisualizer.current
    var isExpanded by rememberSaveable { mutableStateOf(autoExpand) }
    val playback = rememberVisualizerPlaybackState(steps.size, initialStep = 0, key = "brute_force_pairs")
    val stepIndex = playback.safeStepIndex
    val step = steps[stepIndex]

    if (!autoExpand) ArrayWindowButton(text = "Open pair problems visualizer") { isExpanded = true }
    if (!isExpanded) return

    VisualizerModal(
        title = title,
        statusText = when (step.event) {
            PairSearchEvent.START -> "NESTED PAIR SEARCH · TARGET = ${step.target}"
            PairSearchEvent.EVALUATE_PAIR -> "TESTING PAIR (i=${step.currentI}, j=${step.currentJ}): SUM = ${step.currentSum}"
            PairSearchEvent.PAIR_FOUND -> "MATCH FOUND AT INDICES (${step.foundPair?.first}, ${step.foundPair?.second})"
            PairSearchEvent.NO_PAIR_FOUND -> "NO PAIR FOUND"
        },
        statusColor = when (step.event) {
            PairSearchEvent.PAIR_FOUND -> PairGreen
            PairSearchEvent.NO_PAIR_FOUND -> PairRed
            PairSearchEvent.EVALUATE_PAIR -> PairOrange
            else -> PairBlue
        },
        isCompleted = playback.isCompleted,
        stepIndex = stepIndex,
        stepCount = steps.size,
        isPlaying = playback.isPlaying,
        layoutMode = VisualizerLayoutMode.FULL_WIDTH,
        onReset = playback::reset,
        onPrevious = playback::previous,
        onTogglePlayback = playback::togglePlayback,
        onNext = playback::next,
        onClose = { playback.reset(); isExpanded = false },
        visualContent = { BruteForceSceneRenderer(frames[stepIndex], frames, Modifier.fillMaxSize(), "brute_force_pairs") },
        detailsContent = { BruteForcePairDetails(step, stepIndex, steps.size) }
    )
}

@Composable
private fun BruteForcePairDetails(step: BruteForcePairProblemsTraceStep, stepIndex: Int, totalSteps: Int) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("CURRENT PAIR STEP", color = PairSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(step.badgeText, color = if (step.foundPair != null) PairGreen else PairOrange, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Text("Worst-case: O(N²) checks", color = PairGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        StepsCard("Steps", step.explanation, stepIndex, totalSteps, stepIndex == totalSteps - 1)
    }
}
