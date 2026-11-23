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

private val OrangeAccent = Color(0xFFFA6E13)
private val BlueAccent = Color(0xFF38BDF8)
private val GreenAccent = Color(0xFF10B981)
private val RedAccent = Color(0xFFEF4444)
private val TextSecondary = Color(0xFF9CA3AF)

@Composable
internal fun RecursiveBinarySearchVisualizationCard(
    title: String = "Recursive Binary Search"
) {
    val steps = RecursiveBinarySearchTrace.stepsForSearch()
    val sceneFrames = remember(steps) { RecursiveBinarySearchSceneBuilder.build(steps) }
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
        ArrayWindowButton(text = "Open binary search visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.event) {
            BinarySearchEvent.START -> "READY · TARGET = ${step.target}"
            BinarySearchEvent.CALL_ENTER -> "CALL DEPTH ${step.callDepth} · [${step.left}..${step.right}]"
            BinarySearchEvent.CALCULATE_MID -> "MID = INDEX ${step.mid}"
            BinarySearchEvent.COMPARE_TARGET -> "COMPARE arr[${step.mid}] WITH TARGET"
            BinarySearchEvent.RECURSE_LEFT -> "RECURSE LEFT"
            BinarySearchEvent.RECURSE_RIGHT -> "RECURSE RIGHT"
            BinarySearchEvent.TARGET_FOUND -> "MATCH FOUND AT INDEX ${step.resultIndex}"
            BinarySearchEvent.TARGET_NOT_FOUND -> "EMPTY RANGE · TARGET ABSENT"
            BinarySearchEvent.RETURN -> if (step.isTerminal) "RESULT RETURNED TO INITIAL CALLER" else "RETURN FROM CALL #${step.returnedCall?.callId}"
        }

        val statusColor = when (step.event) {
            BinarySearchEvent.TARGET_FOUND -> GreenAccent
            BinarySearchEvent.TARGET_NOT_FOUND -> RedAccent
            BinarySearchEvent.RETURN -> if (step.returnValue == -1) RedAccent else GreenAccent
            BinarySearchEvent.CALCULATE_MID -> OrangeAccent
            BinarySearchEvent.COMPARE_TARGET -> OrangeAccent
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
                RecursiveBinarySearchSceneRenderer(
                    frame = sceneFrames[stepIndex],
                    sequence = sceneFrames,
                    modifier = Modifier.fillMaxSize()
                )
            },
            detailsContent = {
                RecursiveBinarySearchDetailsAndStepsContent(
                    step = step,
                    stepIndex = stepIndex,
                    totalSteps = steps.size,
                    scale = 1f
                )
            }
        )
    }
}

@Composable
private fun RecursiveBinarySearchDetailsAndStepsContent(
    step: BinarySearchTraceStep,
    stepIndex: Int,
    totalSteps: Int,
    scale: Float
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp * scale)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("CURRENT OPERATION", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.event) {
                        BinarySearchEvent.TARGET_FOUND -> GreenAccent
                        BinarySearchEvent.TARGET_NOT_FOUND -> RedAccent
                        BinarySearchEvent.RETURN -> if (step.returnValue == -1) RedAccent else GreenAccent
                        BinarySearchEvent.CALCULATE_MID -> OrangeAccent
                        BinarySearchEvent.COMPARE_TARGET -> OrangeAccent
                        else -> BlueAccent
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Time: O(log N)", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("Recursive space: O(log N)", color = BlueAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
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
