package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
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
internal fun GreedyFundamentalsVisualizationCard(
    title: String = "Greedy Algorithm Fundamentals"
) {
    val steps = GreedyFundamentalsTrace.createTrace()
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
        ArrayWindowButton(text = "Open greedy fundamentals visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.status) {
            GreedyFundamentalsStatus.INIT -> "GREEDY FUNDAMENTALS: BUDGET ${step.totalBudget}"
            GreedyFundamentalsStatus.SORTING -> "SORTING CANDIDATES (ASCENDING)"
            GreedyFundamentalsStatus.EVALUATING -> "EVALUATING DISTANCE ${step.currentCandidateDistance}"
            GreedyFundamentalsStatus.SELECTED -> "SELECTED DISTANCE ${step.currentCandidateDistance}"
            GreedyFundamentalsStatus.REJECTED -> "EXCEEDS REMAINING BUDGET"
            GreedyFundamentalsStatus.FINISHED -> "SEARCH COMPLETE · OPTIMAL RESULT"
        }

        val statusColor = when (step.status) {
            GreedyFundamentalsStatus.SELECTED, GreedyFundamentalsStatus.FINISHED -> GreenAccent
            GreedyFundamentalsStatus.REJECTED -> RedAccent
            GreedyFundamentalsStatus.EVALUATING, GreedyFundamentalsStatus.SORTING -> OrangeAccent
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
                    GreedyFundamentalsVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                GreedyFundamentalsDetailsAndStepsContent(
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
private fun GreedyFundamentalsVisualizationContent(step: GreedyFundamentalsStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Budget Header Card
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DELIVERY ROBOT DISTANCE BUDGET",
                        color = OrangeAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Remaining: ${step.remainingBudget} / ${step.totalBudget}",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Budget Progress Bar
                val progress = (step.remainingBudget.toFloat() / step.totalBudget).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp * scale)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFF22222E))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(10.dp * scale)
                            .clip(RoundedCornerShape(5.dp))
                            .background(if (step.remainingBudget > 0) BlueAccent else RedAccent)
                    )
                }
            }
        }

        // Candidate Stop Cards
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
                Text(
                    text = "CANDIDATE STOPS (SHORTEST FIRST):",
                    color = BlueAccent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    step.sortedDistances.forEachIndexed { idx, dist ->
                        val isCurrent = step.currentCandidateIndex == idx
                        val isChosen = step.chosenDistances.contains(dist)
                        val isRejected = isCurrent && step.status == GreedyFundamentalsStatus.REJECTED

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp * scale),
                            modifier = Modifier.weight(1f).padding(horizontal = 2.dp * scale)
                        ) {
                            Text(
                                text = when {
                                    isChosen -> "CHOSEN"
                                    isRejected -> "REJECT"
                                    isCurrent -> "TESTING"
                                    else -> "PENDING"
                                },
                                color = when {
                                    isChosen -> GreenAccent
                                    isRejected -> RedAccent
                                    isCurrent -> OrangeAccent
                                    else -> TextSecondary
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Surface(
                                color = when {
                                    isChosen -> Color(0xFF123820)
                                    isRejected -> Color(0xFF381215)
                                    isCurrent -> Color(0xFF2A1C12)
                                    else -> Color(0xFF181824)
                                },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(
                                    1.dp,
                                    when {
                                        isChosen -> GreenAccent
                                        isRejected -> RedAccent
                                        isCurrent -> OrangeAccent
                                        else -> CardBorderColor
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth().height(48.dp * scale)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text("Stop #${idx + 1}", color = TextSecondary, fontSize = 9.sp)
                                    Text("Dist: $dist", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Summary Card
        Surface(
            color = Color(0xFF13131C),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, CardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(8.dp * scale).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Selected Stops: [${step.chosenDistances.joinToString()}]",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Count: ${step.chosenDistances.size}",
                    color = GreenAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun GreedyFundamentalsDetailsAndStepsContent(
    step: GreedyFundamentalsStep,
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
                Text("GREEDY DECISION", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.status) {
                        GreedyFundamentalsStatus.SELECTED, GreedyFundamentalsStatus.FINISHED -> GreenAccent
                        GreedyFundamentalsStatus.REJECTED -> RedAccent
                        else -> OrangeAccent
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Complexity: O(N log N)", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
