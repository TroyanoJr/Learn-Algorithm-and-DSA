package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Text

private val MergeBlueAccent = Color(0xFF38BDF8)
private val MergeOrangeAccent = Color(0xFFFA6E13)
private val MergeGreenAccent = Color(0xFF10B981)
private val MergeTextSecondary = Color(0xFF9CA3AF)

@Composable
internal fun MergeSortFundamentalsVisualizationCard(
    title: String = "Merge Sort Fundamentals"
) {
    val steps = remember { MergeSortFundamentalsTrace.stepsForMerge() }
    val sceneFrames = remember(steps) { MergeSortFundamentalsSceneBuilder.build(steps) }
    val autoExpand = LocalAutoExpandVisualizer.current
    var isExpanded by rememberSaveable { mutableStateOf(autoExpand) }
    val playback = rememberVisualizerPlaybackState(stepCount = steps.size, initialStep = 0)
    val stepIndex = playback.safeStepIndex
    val step = steps[stepIndex]

    if (!autoExpand) {
        ArrayWindowButton(text = "Open merge step visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusColor = when (step.event) {
            MergeStepEvent.COMPLETE -> MergeGreenAccent
            MergeStepEvent.TAKE_LEFT, MergeStepEvent.TAKE_RIGHT -> MergeOrangeAccent
            else -> MergeBlueAccent
        }
        VisualizerModal(
            title = title,
            statusText = when (step.event) {
                MergeStepEvent.START -> "READY · MERGING TWO SORTED HALVES"
                MergeStepEvent.COMPARE -> "COMPARING Left[${step.i}] vs Right[${step.j}]"
                MergeStepEvent.TAKE_LEFT -> "TAKING LEFT: ${step.takenVal}"
                MergeStepEvent.TAKE_RIGHT -> "TAKING RIGHT: ${step.takenVal}"
                MergeStepEvent.FLUSH_REMAINING -> "FLUSHING REMAINING: ${step.takenVal}"
                MergeStepEvent.COMPLETE -> "MERGE COMPLETE"
            },
            isCompleted = playback.isCompleted,
            stepIndex = stepIndex,
            stepCount = steps.size,
            isPlaying = playback.isPlaying,
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
                MergeSortFundamentalsSceneRenderer(
                    frame = sceneFrames[stepIndex],
                    sequence = sceneFrames,
                    modifier = Modifier.fillMaxSize()
                )
            },
            detailsContent = {
                MergeSortFundamentalsDetailsAndStepsContent(
                    step = step,
                    stepIndex = stepIndex,
                    totalSteps = steps.size
                )
            }
        )
    }
}

@Composable
private fun MergeSortFundamentalsDetailsAndStepsContent(
    step: MergeFundamentalsTraceStep,
    stepIndex: Int,
    totalSteps: Int
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(2.dp)) {
                Text("CURRENT OPERATION", color = MergeTextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.event) {
                        MergeStepEvent.COMPLETE -> MergeGreenAccent
                        MergeStepEvent.TAKE_LEFT, MergeStepEvent.TAKE_RIGHT -> MergeOrangeAccent
                        else -> MergeBlueAccent
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Merge Step: O(N) Linear Time", color = MergeGreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
