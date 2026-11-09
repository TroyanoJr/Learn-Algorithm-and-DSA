package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
internal fun ActivitySelectionVisualizationCard(
    title: String = "Activity Selection"
) {
    val steps = ActivitySelectionTrace.createTrace()
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
        ArrayWindowButton(text = "Open activity selection visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.status) {
            ActivitySelectionStatus.INIT -> "EARLIEST-FINISH-TIME STRATEGY"
            ActivitySelectionStatus.SORTED -> "SORTED ACTIVITIES BY FINISH TIME"
            ActivitySelectionStatus.CHECKING -> "CHECKING ACTIVITY ${step.currentActivity?.name}"
            ActivitySelectionStatus.SELECTED -> "SELECTED ACTIVITY ${step.currentActivity?.name}"
            ActivitySelectionStatus.REJECTED -> "REJECTED OVERLAPPING ACTIVITY ${step.currentActivity?.name}"
            ActivitySelectionStatus.FINISHED -> "SELECTION COMPLETE · ${step.selectedActivities.size} ACTIVITIES"
        }

        val statusColor = when (step.status) {
            ActivitySelectionStatus.SELECTED, ActivitySelectionStatus.FINISHED -> GreenAccent
            ActivitySelectionStatus.REJECTED -> RedAccent
            ActivitySelectionStatus.CHECKING, ActivitySelectionStatus.SORTED -> OrangeAccent
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
                    ActivitySelectionVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                ActivitySelectionDetailsAndStepsContent(
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
private fun ActivitySelectionVisualizationContent(step: ActivitySelectionStep, scale: Float) {
    val maxTime = 16

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Last Finish Header Card
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
                    text = "EARLIEST FINISH RULE",
                    color = OrangeAccent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Last Finish Threshold: ${if (step.lastFinish < 0) "None (-1)" else step.lastFinish}",
                    color = GreenAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Timeline visualization box
        Surface(
            color = Color(0xFF13131C),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, CardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(8.dp * scale),
                verticalArrangement = Arrangement.spacedBy(4.dp * scale)
            ) {
                // Time axis ruler (0 .. 16)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(0, 4, 8, 12, 16).forEach { t ->
                        Text(
                            text = "$t",
                            color = TextSecondary,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Activity interval rows
                val displayList = if (step.sortedActivities.isNotEmpty()) step.sortedActivities else step.allActivities
                displayList.forEach { act ->
                    val isSelected = step.selectedActivities.any { it.name == act.name }
                    val isRejected = step.rejectedActivities.any { it.name == act.name }
                    val isCurrent = step.currentActivity?.name == act.name

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = act.name,
                            color = when {
                                isSelected -> GreenAccent
                                isRejected -> RedAccent
                                isCurrent -> OrangeAccent
                                else -> TextPrimary
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(18.dp * scale)
                        )

                        // Timeline bar container for this activity
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(16.dp * scale)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF181824))
                        ) {
                            val startFraction = act.start.toFloat() / maxTime
                            val endFraction = act.finish.toFloat() / maxTime
                            val widthFraction = (endFraction - startFraction).coerceAtLeast(0.02f)

                            Row(modifier = Modifier.fillMaxSize()) {
                                if (startFraction > 0f) {
                                    Spacer(modifier = Modifier.fillMaxWidth(startFraction))
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(widthFraction / (1f - startFraction).coerceAtLeast(0.001f))
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            when {
                                                isSelected -> GreenAccent
                                                isRejected -> RedAccent.copy(alpha = 0.7f)
                                                isCurrent -> OrangeAccent
                                                else -> BlueAccent.copy(alpha = 0.5f)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${act.start}-${act.finish}",
                                        color = if (isSelected || isRejected) Color(0xFF111115) else TextPrimary,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selected Activities Row
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
                    text = "Selected Set: [${step.selectedActivities.joinToString { it.name }}]",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Max Activities: ${step.selectedActivities.size}",
                    color = GreenAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ActivitySelectionDetailsAndStepsContent(
    step: ActivitySelectionStep,
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
                        ActivitySelectionStatus.SELECTED, ActivitySelectionStatus.FINISHED -> GreenAccent
                        ActivitySelectionStatus.REJECTED -> RedAccent
                        else -> OrangeAccent
                    },
                    fontSize = 13.sp,
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
