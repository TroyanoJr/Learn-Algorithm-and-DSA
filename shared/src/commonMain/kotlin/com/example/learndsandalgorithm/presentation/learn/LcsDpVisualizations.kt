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
    val steps = LcsDpTrace.createTrace()
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val playback = rememberVisualizerPlaybackState(
        stepCount = steps.size,
        initialStep = 0
    )
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val step = steps[stepIndex]

    ArrayWindowButton(text = "Open LCS visualizer") {
        isExpanded = true
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
                    LcsDpVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                LcsDpDetailsAndStepsContent(
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
private fun LcsDpVisualizationContent(step: LcsDpStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // 2D LCS Matrix Render Card
        Surface(
            color = Color(0xFF13131C),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, CardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(10.dp * scale),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp * scale)
            ) {
                Text("2D LCS DP MATRIX dp[i][j]:", color = OrangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                // Header Row for text2
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp * scale)) {
                    Surface(color = Color.Transparent, modifier = Modifier.size(36.dp * scale)) {}
                    Surface(color = Color.Transparent, modifier = Modifier.size(36.dp * scale)) {
                        Box(contentAlignment = Alignment.Center) { Text("Ø", color = TextSecondary, fontSize = 10.sp) }
                    }
                    step.text2.forEach { char2 ->
                        Surface(color = Color.Transparent, modifier = Modifier.size(36.dp * scale)) {
                            Box(contentAlignment = Alignment.Center) { Text("$char2", color = BlueAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        }
                    }
                }

                step.dpMatrix.forEachIndexed { i, row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp * scale)) {
                        // Left header for text1
                        Surface(color = Color.Transparent, modifier = Modifier.size(36.dp * scale)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (i == 0) "Ø" else "${step.text1[i - 1]}",
                                    color = if (i == 0) TextSecondary else BlueAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        row.forEachIndexed { j, valVal ->
                            val isCurrent = step.currentI == i && step.currentJ == j
                            val isMatchCell = isCurrent && step.isMatch == true

                            Surface(
                                color = when {
                                    isMatchCell -> Color(0xFF123820)
                                    isCurrent -> Color(0xFF2A1C12)
                                    valVal > 0 -> Color(0xFF122C38)
                                    else -> Color(0xFF181824)
                                },
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(
                                    1.dp,
                                    when {
                                        isMatchCell -> GreenAccent
                                        isCurrent -> OrangeAccent
                                        valVal > 0 -> BlueAccent
                                        else -> CardBorderColor
                                    }
                                ),
                                modifier = Modifier.size(36.dp * scale)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "$valVal",
                                        color = if (isMatchCell) GreenAccent else TextPrimary,
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

        // Reconstructed LCS Sequence Card
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
                    text = "LCS Subsequence: '${step.lcsString}'",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Length: ${step.dpMatrix[step.text1.length][step.text2.length]}",
                    color = GreenAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun LcsDpDetailsAndStepsContent(
    step: LcsDpStep,
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
                Text("LCS ALIGNMENT DECISION", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.action) {
                        LcsAction.RECONSTRUCT, LcsAction.FINISHED -> GreenAccent
                        LcsAction.COMPUTE_CELL -> if (step.isMatch == true) GreenAccent else OrangeAccent
                        else -> BlueAccent
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Complexity: O(M*N) Time & Space", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
