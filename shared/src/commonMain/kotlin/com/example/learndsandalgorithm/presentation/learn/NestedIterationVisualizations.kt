package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
internal fun NestedIterationVisualizationCard(
    title: String = "Nested Iteration and Loop Control"
) {
    val steps = NestedIterationTrace.createTrace()
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
        ArrayWindowButton(text = "Open nested iteration visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.action) {
            NestedIterationAction.INIT -> "NESTED ITERATION: ${step.outerLimit} x ${step.innerLimit} LOOPS"
            NestedIterationAction.OUTER_PASS -> "OUTER LOOP PASS i = ${step.i} (RESET j = 1)"
            NestedIterationAction.INNER_PASS -> "INNER PASS (i = ${step.i}, j = ${step.j}) -> PAIR #${step.totalCount}"
            NestedIterationAction.INNER_RESET -> "INNER LOOP RESET"
            NestedIterationAction.FINISHED -> "NESTED ITERATION COMPLETE · ${step.totalCount} PAIRS"
        }

        val statusColor = when (step.action) {
            NestedIterationAction.INNER_PASS, NestedIterationAction.FINISHED -> GreenAccent
            NestedIterationAction.OUTER_PASS -> OrangeAccent
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
                FitToPane(modifier = Modifier.fillMaxSize()) {
                    NestedIterationVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                NestedIterationDetailsAndStepsContent(
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
private fun NestedIterationVisualizationContent(step: NestedIterationStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Active Loop Control Variables Card
        Surface(
            color = Color(0xFF13131C),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, CardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp * scale).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp * scale)) {
                    Text("OUTER LOOP (i)", color = OrangeAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("i = ${step.i} / ${step.outerLimit}", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp * scale)) {
                    Text("INNER LOOP (j)", color = BlueAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("j = ${step.j} / ${step.innerLimit}", color = BlueAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Surface(
                    color = Color(0xFF123820),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, GreenAccent)
                ) {
                    Text(
                        "${step.totalCount} / ${step.outerLimit * step.innerLimit} Pairs",
                        color = GreenAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp * scale, vertical = 4.dp * scale)
                    )
                }
            }
        }

        // Grid Matrix of Printed Coordinates Card
        Surface(
            color = Color(0xFF13131C),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, CardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(10.dp * scale),
                verticalArrangement = Arrangement.spacedBy(6.dp * scale)
            ) {
                Text("NESTED COORDINATES MATRIX (i, j):", color = BlueAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                (1..step.outerLimit).forEach { rowI ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        (1..step.innerLimit).forEach { colJ ->
                            val pair = Pair(rowI, colJ)
                            val isPrinted = step.printedPairs.contains(pair)
                            val isCurrent = step.i == rowI && step.j == colJ && step.action == NestedIterationAction.INNER_PASS

                            Surface(
                                color = when {
                                    isCurrent -> Color(0xFF2A1C12)
                                    isPrinted -> Color(0xFF123820)
                                    else -> Color(0xFF181824)
                                },
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(
                                    1.dp,
                                    when {
                                        isCurrent -> OrangeAccent
                                        isPrinted -> GreenAccent
                                        else -> CardBorderColor
                                    }
                                ),
                                modifier = Modifier.weight(1f).height(40.dp * scale).padding(horizontal = 2.dp * scale)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        "($rowI, $colJ)",
                                        color = if (isPrinted || isCurrent) TextPrimary else TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NestedIterationDetailsAndStepsContent(
    step: NestedIterationStep,
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
                Text("NESTED LOOP DECISION", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.action) {
                        NestedIterationAction.INNER_PASS, NestedIterationAction.FINISHED -> GreenAccent
                        NestedIterationAction.OUTER_PASS -> OrangeAccent
                        else -> BlueAccent
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Complexity: O(M*N) Time, O(1) Space", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
