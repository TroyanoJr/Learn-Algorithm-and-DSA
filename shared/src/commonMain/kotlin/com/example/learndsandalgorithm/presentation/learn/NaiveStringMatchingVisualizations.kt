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

private val StringOrange = Color(0xFFFA6E13)
private val StringBlue = Color(0xFF38BDF8)
private val StringGreen = Color(0xFF10B981)
private val StringRed = Color(0xFFEF4444)
private val StringSecondary = Color(0xFF9CA3AF)

@Composable
internal fun NaiveStringMatchingVisualizationCard(title: String = "Naive String Pattern Matching") {
    val steps = NaiveStringMatchingTrace.stepsForNaiveMatch()
    val frames = remember(steps) { BruteForceSceneBuilder.stringMatching(steps) }
    val autoExpand = LocalAutoExpandVisualizer.current
    var isExpanded by rememberSaveable { mutableStateOf(autoExpand) }
    val playback = rememberVisualizerPlaybackState(steps.size, initialStep = 0, key = "naive_string_matching")
    val stepIndex = playback.safeStepIndex
    val step = steps[stepIndex]

    if (!autoExpand) ArrayWindowButton(text = "Open naive string matching visualizer") { isExpanded = true }
    if (!isExpanded) return

    VisualizerModal(
        title = title,
        statusText = when (step.event) {
            StringMatchEvent.START -> "NAIVE PATTERN MATCHING · TEXT \"${step.text}\" | PATTERN \"${step.pattern}\""
            StringMatchEvent.COMPARE_CHAR -> "COMPARING CHARACTER AT ALIGNMENT ${step.currentStart}"
            StringMatchEvent.ALIGNMENT_MISMATCH -> "MISMATCH AT ALIGNMENT ${step.currentStart}"
            StringMatchEvent.ALIGNMENT_MATCH -> "FULL PATTERN MATCH AT ALIGNMENT ${step.currentStart}"
            StringMatchEvent.MATCHING_COMPLETE -> "STRING MATCHING COMPLETE"
        },
        statusColor = when (step.event) {
            StringMatchEvent.ALIGNMENT_MATCH, StringMatchEvent.MATCHING_COMPLETE -> StringGreen
            StringMatchEvent.ALIGNMENT_MISMATCH -> StringRed
            StringMatchEvent.COMPARE_CHAR -> StringOrange
            else -> StringBlue
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
        visualContent = { BruteForceSceneRenderer(frames[stepIndex], frames, Modifier.fillMaxSize(), "naive_string_matching") },
        detailsContent = { NaiveStringMatchingDetails(step, stepIndex, steps.size) }
    )
}

@Composable
private fun NaiveStringMatchingDetails(step: NaiveStringMatchingTraceStep, stepIndex: Int, totalSteps: Int) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("CURRENT STEP", color = StringSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(step.badgeText, modifier = Modifier.fillMaxWidth(), color = when (step.event) {
                    StringMatchEvent.ALIGNMENT_MATCH, StringMatchEvent.MATCHING_COMPLETE -> StringGreen
                    StringMatchEvent.ALIGNMENT_MISMATCH -> StringRed
                    else -> StringOrange
                }, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("Worst-case: O(N * M)", modifier = Modifier.fillMaxWidth(), color = StringGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        StepsCard("Steps", step.explanation, stepIndex, totalSteps, stepIndex == totalSteps - 1)
    }
}
