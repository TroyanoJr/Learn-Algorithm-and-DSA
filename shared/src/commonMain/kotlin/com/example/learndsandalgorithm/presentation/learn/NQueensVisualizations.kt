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
internal fun NQueensVisualizationCard(
    title: String = "The N-Queens Problem"
) {
    val steps = NQueensTrace.createTrace()
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val playback = rememberVisualizerPlaybackState(
        stepCount = steps.size,
        initialStep = 0
    )
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val step = steps[stepIndex]

    ArrayWindowButton(text = "Open N-Queens visualizer") {
        isExpanded = true
    }

    if (isExpanded) {
        val statusText = when (step.action) {
            NQueensAction.INIT -> "N-QUEENS: ${step.boardSize}x${step.boardSize} BOARD"
            NQueensAction.TRY_POSITION -> "TESTING ROW ${step.currentRow}, COL ${step.testingCol}"
            NQueensAction.PLACE_QUEEN -> "SAFE! PLACED QUEEN AT (${step.currentRow}, ${step.testingCol})"
            NQueensAction.CONFLICT_PRUNE -> "CONFLICT AT (${step.currentRow}, ${step.testingCol}) · PRUNE"
            NQueensAction.BACKTRACK -> "BACKTRACK FROM ROW ${step.currentRow}"
            NQueensAction.SOLUTION_FOUND -> "VALID N-QUEENS SOLUTION FOUND!"
            NQueensAction.FINISHED -> "SEARCH COMPLETE · ${step.solutions.size} SOLUTIONS"
        }

        val statusColor = when (step.action) {
            NQueensAction.SOLUTION_FOUND, NQueensAction.FINISHED -> GreenAccent
            NQueensAction.CONFLICT_PRUNE, NQueensAction.BACKTRACK -> RedAccent
            NQueensAction.PLACE_QUEEN, NQueensAction.TRY_POSITION -> OrangeAccent
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
                    NQueensVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                NQueensDetailsAndStepsContent(
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
private fun NQueensVisualizationContent(step: NQueensStep, scale: Float) {
    val n = step.boardSize

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Chessboard Grid Card
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
                Text("${n}x${n} CHESSBOARD STATE:", color = OrangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                (0 until n).forEach { r ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp * scale)
                    ) {
                        (0 until n).forEach { c ->
                            val hasQueen = step.queenColumns.getOrNull(r) == c
                            val isTesting = step.currentRow == r && step.testingCol == c
                            val isDarkSquare = (r + c) % 2 == 1

                            Surface(
                                color = when {
                                    hasQueen -> Color(0xFF123820)
                                    isTesting && step.action == NQueensAction.CONFLICT_PRUNE -> Color(0xFF381215)
                                    isTesting -> Color(0xFF2A1C12)
                                    isDarkSquare -> Color(0xFF1A1A26)
                                    else -> Color(0xFF242436)
                                },
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(
                                    1.dp,
                                    when {
                                        hasQueen -> GreenAccent
                                        isTesting && step.action == NQueensAction.CONFLICT_PRUNE -> RedAccent
                                        isTesting -> OrangeAccent
                                        else -> CardBorderColor
                                    }
                                ),
                                modifier = Modifier.size(42.dp * scale)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = when {
                                            hasQueen -> "👑"
                                            isTesting && step.action == NQueensAction.CONFLICT_PRUNE -> "❌"
                                            isTesting -> "🔍"
                                            else -> ""
                                        },
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Solutions Found Card
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
                    text = "Solutions: ${step.solutions.map { it.toString() }}",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${step.solutions.size} Valid",
                    color = GreenAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun NQueensDetailsAndStepsContent(
    step: NQueensStep,
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
                Text("N-QUEENS DECISION", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.action) {
                        NQueensAction.SOLUTION_FOUND, NQueensAction.FINISHED -> GreenAccent
                        NQueensAction.CONFLICT_PRUNE, NQueensAction.BACKTRACK -> RedAccent
                        else -> OrangeAccent
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Complexity: O(N!)", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
