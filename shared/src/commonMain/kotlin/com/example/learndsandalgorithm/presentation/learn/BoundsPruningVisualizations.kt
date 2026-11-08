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
internal fun BoundsPruningVisualizationCard(
    title: String = "Bounds, Incumbents, and Pruning"
) {
    val steps = BoundsPruningTrace.createTrace()
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
        ArrayWindowButton(text = "Open bounds & pruning visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.action) {
            BoundsPruningAction.INIT -> "BOUNDS & PRUNING RULES EVALUATION"
            BoundsPruningAction.EVALUATE_MINIMIZATION -> "MINIMIZATION: EVAL ${step.evaluatedNodeId} (BOUND ${step.evaluatedBound})"
            BoundsPruningAction.EVALUATE_MAXIMIZATION -> "MAXIMIZATION: EVAL ${step.evaluatedNodeId} (BOUND ${step.evaluatedBound})"
            BoundsPruningAction.UPDATE_INCUMBENT -> "UPDATED MAX INCUMBENT = ${step.maxIncumbent}"
            BoundsPruningAction.FINISHED -> "EVALUATION COMPLETE · ALL PRUNING RULES VERIFIED"
        }

        val statusColor = when (step.action) {
            BoundsPruningAction.UPDATE_INCUMBENT, BoundsPruningAction.FINISHED -> GreenAccent
            BoundsPruningAction.EVALUATE_MINIMIZATION, BoundsPruningAction.EVALUATE_MAXIMIZATION -> if (step.isPruned == true) RedAccent else GreenAccent
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
                    BoundsPruningVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                BoundsPruningDetailsAndStepsContent(
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
private fun BoundsPruningVisualizationContent(step: BoundsPruningStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Incumbent Thresholds Card
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
                    Text("MINIMIZATION THRESHOLD", color = BlueAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("Min Incumbent = ${step.minIncumbent}", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Rule: Prune if Lower Bound ≥ ${step.minIncumbent}", color = TextSecondary, fontSize = 9.sp)
                }

                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp * scale)) {
                    Text("MAXIMIZATION THRESHOLD", color = GreenAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text("Max Incumbent = ${step.maxIncumbent}", color = GreenAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Rule: Prune if Upper Bound ≤ ${step.maxIncumbent}", color = TextSecondary, fontSize = 9.sp)
                }
            }
        }

        // Side-by-Side Minimization vs Maximization Rule Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp * scale)
        ) {
            // Minimization Evaluator
            Surface(
                color = Color(0xFF13131C),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, if (step.problemType == ProblemType.MINIMIZATION) BlueAccent else CardBorderColor),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp * scale),
                    verticalArrangement = Arrangement.spacedBy(4.dp * scale)
                ) {
                    Text("MINIMIZATION (LOWER BOUNDS)", color = BlueAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)

                    listOf(
                        Triple("Node A", 8, false),
                        Triple("Node B", 12, true)
                    ).forEach { (node, bound, pruned) ->
                        val isTesting = step.evaluatedNodeId == node
                        Surface(
                            color = when {
                                isTesting && pruned -> Color(0xFF381215)
                                isTesting -> Color(0xFF123820)
                                pruned -> Color(0xFF22181B)
                                else -> Color(0xFF181824)
                            },
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, if (pruned) RedAccent else GreenAccent),
                            modifier = Modifier.fillMaxWidth().height(36.dp * scale)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp * scale).fillMaxSize(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("$node (L=$bound)", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                Text(if (pruned) "PRUNED" else "KEEP", color = if (pruned) RedAccent else GreenAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Maximization Evaluator
            Surface(
                color = Color(0xFF13131C),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, if (step.problemType == ProblemType.MAXIMIZATION) GreenAccent else CardBorderColor),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp * scale),
                    verticalArrangement = Arrangement.spacedBy(4.dp * scale)
                ) {
                    Text("MAXIMIZATION (UPPER BOUNDS)", color = GreenAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)

                    listOf(
                        Triple("Node X", 50, false),
                        Triple("Node Y", 35, true)
                    ).forEach { (node, bound, pruned) ->
                        val isTesting = step.evaluatedNodeId == node
                        Surface(
                            color = when {
                                isTesting && pruned -> Color(0xFF381215)
                                isTesting -> Color(0xFF123820)
                                pruned -> Color(0xFF22181B)
                                else -> Color(0xFF181824)
                            },
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, if (pruned) RedAccent else GreenAccent),
                            modifier = Modifier.fillMaxWidth().height(36.dp * scale)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp * scale).fillMaxSize(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("$node (U=$bound)", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                Text(if (pruned) "PRUNED" else "KEEP", color = if (pruned) RedAccent else GreenAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoundsPruningDetailsAndStepsContent(
    step: BoundsPruningStep,
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
                Text("PRUNING RULE VERDICT", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when {
                        step.action == BoundsPruningAction.UPDATE_INCUMBENT || step.action == BoundsPruningAction.FINISHED -> GreenAccent
                        step.isPruned == true -> RedAccent
                        else -> OrangeAccent
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Optimistic Bounds", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
