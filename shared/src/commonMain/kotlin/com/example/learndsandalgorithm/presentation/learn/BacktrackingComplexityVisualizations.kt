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
internal fun BacktrackingComplexityVisualizationCard(
    title: String = "Backtracking Complexity & Pruning"
) {
    val steps = remember { BacktrackingComplexityTrace.createTrace() }
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
        ArrayWindowButton(text = "Open complexity & pruning visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.action) {
            ComplexityAction.INIT -> "BACKTRACKING COMPLEXITY & PRUNING IMPACT"
            ComplexityAction.EVALUATE_UNPRUNED -> "UNPRUNED EVALUATION AT INDEX ${step.currentIndex}"
            ComplexityAction.EVALUATE_PRUNED -> "PRUNED EVALUATION: ${step.currentValue} ≤ ${step.limit} (VALID)"
            ComplexityAction.PRUNED_HALT -> "PRUNED HALT: ${step.currentValue} > ${step.limit} (STOP EVALUATION)"
            ComplexityAction.FINISHED -> "COMPARISON COMPLETE · SAVED ${step.unprunedCalls - step.prunedCalls} CALL(S)"
        }

        val statusColor = when (step.action) {
            ComplexityAction.FINISHED -> GreenAccent
            ComplexityAction.EVALUATE_PRUNED -> OrangeAccent
            ComplexityAction.PRUNED_HALT -> RedAccent
            ComplexityAction.EVALUATE_UNPRUNED -> OrangeAccent
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
                val presentation = remember(steps) { BacktrackingComplexitySceneBuilder.build(steps) }
                BacktrackingComplexitySceneRenderer(presentation, presentation.frames[stepIndex])
            },
            detailsContent = {
                BacktrackingDetailsPanel("COMPLEXITY & PRUNING VERDICT", step.badgeText, statusColor,
                    "Pruning Efficiency", step.explanation, stepIndex, steps.size, isCompleted)
            }
        )
    }
}
