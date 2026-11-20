package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val FundamentalsOrange = Color(0xFFFA6E13)
private val FundamentalsBlue = Color(0xFF38BDF8)
private val FundamentalsGreen = Color(0xFF10B981)
private val FundamentalsSecondary = Color(0xFF9CA3AF)

@Composable
internal fun BruteForceFundamentalsVisualizationCard(title: String = "Brute Force Fundamentals") {
    val steps = BruteForceFundamentalsTrace.stepsForSquareSearch()
    val frames = remember(steps) { BruteForceSceneBuilder.fundamentals(steps) }
    val autoExpand = LocalAutoExpandVisualizer.current
    var isExpanded by rememberSaveable { mutableStateOf(autoExpand) }
    val playback = rememberVisualizerPlaybackState(steps.size, initialStep = 0, key = "brute_force_fundamentals")
    val stepIndex = playback.safeStepIndex
    val step = steps[stepIndex]

    if (!autoExpand) ArrayWindowButton(text = "Open brute force visualizer") { isExpanded = true }
    if (!isExpanded) return

    VisualizerModal(
        title = title,
        statusText = when (step.event) {
            FundamentalsSearchEvent.START -> "EXHAUSTIVE SEARCH RANGE [${step.rangeMin}..${step.rangeMax}]"
            FundamentalsSearchEvent.EVALUATE_CANDIDATE -> "TESTING CANDIDATE x = ${step.currentCandidate}"
            FundamentalsSearchEvent.SOLUTION_FOUND -> "SOLUTION FOUND: x = ${step.foundSolution}"
            FundamentalsSearchEvent.SEARCH_COMPLETE -> "SEARCH COMPLETE"
        },
        statusColor = when (step.event) {
            FundamentalsSearchEvent.SOLUTION_FOUND -> FundamentalsGreen
            FundamentalsSearchEvent.EVALUATE_CANDIDATE -> FundamentalsOrange
            else -> FundamentalsBlue
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
        visualContent = { BruteForceSceneRenderer(frames[stepIndex], frames, Modifier.fillMaxSize(), "brute_force_fundamentals") },
        detailsContent = {
            BruteForceFundamentalsDetails(step, stepIndex, steps.size)
        }
    )
}

@Composable
private fun BruteForceFundamentalsDetails(step: BruteForceFundamentalsTraceStep, stepIndex: Int, totalSteps: Int) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("SEARCH STEP", color = FundamentalsSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(step.badgeText, modifier = Modifier.fillMaxWidth(), color = if (step.foundSolution != null) FundamentalsGreen else FundamentalsOrange, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("Time: O(k) candidates", modifier = Modifier.fillMaxWidth(), color = FundamentalsGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        StepsCard("Steps", step.explanation, stepIndex, totalSteps, stepIndex == totalSteps - 1)
    }
}
