package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

private val OrangeAccent = Color(0xFFFA6E13)
private val BlueAccent = Color(0xFF38BDF8)
private val GreenAccent = Color(0xFF10B981)
private val RedAccent = Color(0xFFEF4444)

@Composable
internal fun BacktrackingFundamentalsVisualizationCard(
    title: String = "Backtracking Fundamentals"
) {
    val steps = remember { BacktrackingFundamentalsTrace.createTrace() }
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
        ArrayWindowButton(text = "Open backtracking fundamentals visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.action) {
            BacktrackingFundamentalsAction.INIT -> "STATE-SPACE TREE: LENGTH ${step.targetLength}"
            BacktrackingFundamentalsAction.CHOOSE_0 -> "CHOICE: APPEND '0' -> '${step.currentString}'"
            BacktrackingFundamentalsAction.CHOOSE_1 -> "CHOICE: APPEND '1' -> '${step.currentString}'"
            BacktrackingFundamentalsAction.SOLUTION_FOUND -> "SOLUTION FOUND: '${step.currentString}'"
            BacktrackingFundamentalsAction.BACKTRACK -> "UNDO / BACKTRACK TO '${step.currentString}'"
            BacktrackingFundamentalsAction.FINISHED -> "SEARCH COMPLETE · ${step.generatedStrings.size} STRINGS"
        }

        val statusColor = when (step.action) {
            BacktrackingFundamentalsAction.SOLUTION_FOUND, BacktrackingFundamentalsAction.FINISHED -> GreenAccent
            BacktrackingFundamentalsAction.BACKTRACK -> RedAccent
            BacktrackingFundamentalsAction.CHOOSE_0, BacktrackingFundamentalsAction.CHOOSE_1 -> OrangeAccent
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
                val presentation = remember(steps) { BacktrackingFundamentalsSceneBuilder.build(steps) }
                BacktrackingFundamentalsSceneRenderer(presentation, presentation.frames[stepIndex])
            },
            detailsContent = {
                BacktrackingDetailsPanel("BACKTRACKING DECISION", step.badgeText,
                    when (step.action) {
                        BacktrackingFundamentalsAction.SOLUTION_FOUND, BacktrackingFundamentalsAction.FINISHED -> GreenAccent
                        BacktrackingFundamentalsAction.BACKTRACK -> RedAccent
                        else -> OrangeAccent
                    }, "Complexity: O(2^N)", step.explanation, stepIndex, steps.size, isCompleted)
            }
        )
    }
}
