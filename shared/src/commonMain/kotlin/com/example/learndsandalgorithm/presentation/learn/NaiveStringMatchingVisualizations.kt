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
internal fun NaiveStringMatchingVisualizationCard(
    title: String = "Naive String Pattern Matching"
) {
    val steps = NaiveStringMatchingTrace.stepsForNaiveMatch()
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val playback = rememberVisualizerPlaybackState(
        stepCount = steps.size,
        initialStep = 0
    )
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val step = steps[stepIndex]

    ArrayWindowButton(text = "Open naive string matching visualizer") {
        isExpanded = true
    }

    if (isExpanded) {
        val statusText = when (step.event) {
            StringMatchEvent.START -> "NAIVE PATTERN MATCHING · TEXT \"${step.text}\" | PATTERN \"${step.pattern}\""
            StringMatchEvent.COMPARE_CHAR -> "COMPARING CHARACTER AT ALIGNMENT ${step.currentStart}"
            StringMatchEvent.ALIGNMENT_MISMATCH -> "MISMATCH AT ALIGNMENT ${step.currentStart}"
            StringMatchEvent.ALIGNMENT_MATCH -> "FULL PATTERN MATCH AT ALIGNMENT ${step.currentStart}"
            StringMatchEvent.MATCHING_COMPLETE -> "STRING MATCHING COMPLETE"
        }

        val statusColor = when (step.event) {
            StringMatchEvent.ALIGNMENT_MATCH, StringMatchEvent.MATCHING_COMPLETE -> GreenAccent
            StringMatchEvent.ALIGNMENT_MISMATCH -> RedAccent
            StringMatchEvent.COMPARE_CHAR -> OrangeAccent
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
                    NaiveStringMatchingVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                NaiveStringMatchingDetailsAndStepsContent(
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
private fun NaiveStringMatchingVisualizationContent(step: NaiveStringMatchingTraceStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Status Header Card
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
                    Text(
                        text = "SLIDING WINDOW: Text \"${step.text}\" vs Pattern \"${step.pattern}\"",
                        color = OrangeAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Alignment Start = ${step.currentStart ?: "—"} | Offset = ${step.currentOffset ?: "—"}",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }

                Surface(
                    color = when (step.event) {
                        StringMatchEvent.ALIGNMENT_MATCH -> Color(0xFF123820)
                        StringMatchEvent.ALIGNMENT_MISMATCH -> Color(0xFF381212)
                        else -> Color(0xFF2A1C12)
                    },
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(
                        1.dp,
                        when (step.event) {
                            StringMatchEvent.ALIGNMENT_MATCH -> GreenAccent
                            StringMatchEvent.ALIGNMENT_MISMATCH -> RedAccent
                            else -> OrangeAccent
                        }
                    )
                ) {
                    Text(
                        text = step.event.name,
                        color = when (step.event) {
                            StringMatchEvent.ALIGNMENT_MATCH -> GreenAccent
                            StringMatchEvent.ALIGNMENT_MISMATCH -> RedAccent
                            else -> OrangeAccent
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp * scale, vertical = 4.dp * scale)
                    )
                }
            }
        }

        // Text Row
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
                Text("TEXT ARRAY:", color = BlueAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    step.text.forEachIndexed { idx, char ->
                        val isStart = idx == step.currentStart
                        val isComparing = step.currentStart != null && step.currentOffset != null && idx == (step.currentStart + step.currentOffset)
                        val isMatchedStart = idx in step.foundMatches

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp * scale),
                            modifier = Modifier.weight(1f).padding(horizontal = 2.dp * scale)
                        ) {
                            Text(
                                text = when {
                                    isMatchedStart -> "MATCH"
                                    isComparing -> "COMPARE"
                                    isStart -> "START"
                                    else -> " "
                                },
                                color = when {
                                    isMatchedStart -> GreenAccent
                                    isComparing -> OrangeAccent
                                    isStart -> BlueAccent
                                    else -> Color.Transparent
                                },
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Surface(
                                color = when {
                                    isMatchedStart -> Color(0xFF123820)
                                    isComparing -> Color(0xFF2A1C12)
                                    else -> Color(0xFF13131C)
                                },
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(
                                    1.dp,
                                    when {
                                        isMatchedStart -> GreenAccent
                                        isComparing -> OrangeAccent
                                        else -> CardBorderColor
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth().height(42.dp * scale)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("'$char'", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Text("[$idx]", color = TextSecondary, fontSize = 9.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NaiveStringMatchingDetailsAndStepsContent(
    step: NaiveStringMatchingTraceStep,
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
                Text("CURRENT STEP", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.event) {
                        StringMatchEvent.ALIGNMENT_MATCH, StringMatchEvent.MATCHING_COMPLETE -> GreenAccent
                        StringMatchEvent.ALIGNMENT_MISMATCH -> RedAccent
                        else -> OrangeAccent
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Worst-case: O(N * M)", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
