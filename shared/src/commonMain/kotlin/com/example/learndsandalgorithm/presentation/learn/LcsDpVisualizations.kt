package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
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

private val CardBorderColor = Color(0xFF262634)
private val OrangeAccent = Color(0xFFFA6E13)
private val BlueAccent = Color(0xFF38BDF8)
private val GreenAccent = Color(0xFF10B981)
private val RedAccent = Color(0xFFEF4444)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9CA3AF)

@Composable
internal fun LcsDpVisualizationCard(
    title: String = "Sequence Alignment: Longest Common Subsequence (LCS)"
) {
    val steps = remember { LcsDpTrace.createTrace() }
    val scene = remember(steps) { LcsDpSceneBuilder.build(steps) }
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
        ArrayWindowButton(text = "Open LCS visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.action) {
            LcsAction.INIT -> "LCS DP: '${step.text1}' vs '${step.text2}'"
            LcsAction.COMPUTE_CELL -> if (step.isMatch == true) "MATCH! '${step.text1[step.currentI!! - 1]}' == '${step.text2[step.currentJ!! - 1]}'" else "MISMATCH! '${step.text1[step.currentI!! - 1]}' != '${step.text2[step.currentJ!! - 1]}'"
            LcsAction.RECONSTRUCT -> "RECONSTRUCTING LCS SEQUENCE: '${step.lcsString}'"
            LcsAction.FINISHED -> "LCS COMPLETE · LENGTH = ${step.dpMatrix[step.text1.length][step.text2.length]} ('${step.lcsString}')"
        }

        val statusColor = when (step.action) {
            LcsAction.RECONSTRUCT, LcsAction.FINISHED -> GreenAccent
            LcsAction.COMPUTE_CELL -> if (step.isMatch == true) GreenAccent else OrangeAccent
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
                LcsDpSceneRenderer(scene, scene.frames[stepIndex])
            },
            detailsContent = {
                DynamicProgrammingDetailsPanel("LCS DECISION", step.badgeText, statusColor,
                    "Time O(M·N) · space O(M·N)", step.explanation, stepIndex, steps.size, isCompleted)
            }
        )
    }
}
