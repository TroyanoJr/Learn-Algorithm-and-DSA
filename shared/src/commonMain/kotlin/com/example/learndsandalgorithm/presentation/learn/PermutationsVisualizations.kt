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
internal fun PermutationsVisualizationCard(
    title: String = "Permutations & Unique Arrangements"
) {
    val steps = remember { PermutationsTrace.createTrace() }
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
        ArrayWindowButton(text = "Open permutations visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.action) {
            PermutationAction.INIT -> "PERMUTATIONS: GENERATING N! ARRANGEMENTS"
            PermutationAction.SWAP_CHOICE -> "SWAPPING arr[${step.swapI}] ↔ arr[${step.swapJ}]"
            PermutationAction.RECURSE -> "RECURSING TO INDEX ${step.startIndex}"
            PermutationAction.SOLUTION_FOUND -> "PERMUTATION FOUND: ${step.currentPermutation}"
            PermutationAction.BACKTRACK_SWAP -> "RESTORE ARRAY: SWAP BACK arr[${step.swapI}] ↔ arr[${step.swapJ}]"
            PermutationAction.FINISHED -> "PERMUTATIONS COMPLETE · ${step.completedPermutations.size} ARRANGEMENTS"
        }

        val statusColor = when (step.action) {
            PermutationAction.SOLUTION_FOUND, PermutationAction.FINISHED -> GreenAccent
            PermutationAction.BACKTRACK_SWAP -> RedAccent
            PermutationAction.SWAP_CHOICE -> OrangeAccent
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
                val presentation = remember(steps) { PermutationsSceneBuilder.build(steps) }
                PermutationsSceneRenderer(presentation, presentation.frames[stepIndex])
            },
            detailsContent = {
                BacktrackingDetailsPanel("PERMUTATION DECISION", step.badgeText, statusColor,
                    "Complexity: O(N!)", step.explanation, stepIndex, steps.size, isCompleted)
            }
        )
    }
}
