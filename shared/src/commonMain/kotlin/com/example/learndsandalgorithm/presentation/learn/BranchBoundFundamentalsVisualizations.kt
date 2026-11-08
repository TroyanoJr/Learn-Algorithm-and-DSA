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
internal fun BranchBoundFundamentalsVisualizationCard(
    title: String = "Branch and Bound Fundamentals"
) {
    val steps = BranchBoundFundamentalsTrace.createTrace()
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
        ArrayWindowButton(text = "Open branch & bound visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.action) {
            BbFundamentalsAction.INIT -> "STATE-SPACE SEARCH: MINIMIZATION PROBLEM"
            BbFundamentalsAction.EXPLORE_BRANCH -> "EXPLORING ${step.currentNodeId} (LOWER BOUND ${step.currentNodeBound})"
            BbFundamentalsAction.FOUND_FEASIBLE -> "FEASIBLE SOLUTION FOUND: INCUMBENT = ${step.incumbentCost}"
            BbFundamentalsAction.PRUNE_BRANCH -> "PRUNED ${step.currentNodeId}: BOUND ${step.currentNodeBound} >= INCUMBENT ${step.incumbentCost}"
            BbFundamentalsAction.FINISHED -> "SEARCH COMPLETE · OPTIMAL COST = ${step.incumbentCost}"
        }

        val statusColor = when (step.action) {
            BbFundamentalsAction.FOUND_FEASIBLE, BbFundamentalsAction.FINISHED -> GreenAccent
            BbFundamentalsAction.PRUNE_BRANCH -> RedAccent
            BbFundamentalsAction.EXPLORE_BRANCH -> OrangeAccent
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
                    BranchBoundFundamentalsVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                BranchBoundFundamentalsDetailsAndStepsContent(
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
private fun BranchBoundFundamentalsVisualizationContent(step: BranchBoundFundamentalsStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Incumbent Threshold Card
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
                    Text("CURRENT INCUMBENT THRESHOLD", color = OrangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(
                        step.incumbentSolution ?: "INF (No feasible solution yet)",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    color = if (step.incumbentCost != null) Color(0xFF123820) else Color(0xFF2A1C12),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, if (step.incumbentCost != null) GreenAccent else OrangeAccent)
                ) {
                    Text(
                        text = "Cost: ${step.incumbentCost ?: "∞"}",
                        color = if (step.incumbentCost != null) GreenAccent else OrangeAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp * scale, vertical = 4.dp * scale)
                    )
                }
            }
        }

        // Tree Nodes Render Card
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
                Text("STATE-SPACE TREE NODES:", color = BlueAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(
                        Triple("Node A", 12, "Leaf A (15)"),
                        Triple("Node B", 18, "Pruned Subtree"),
                        Triple("Node C", 8, "Leaf C (8)")
                    ).forEach { (nodeName, bound, childInfo) ->
                        val isCurrent = step.currentNodeId == nodeName || step.currentNodeId?.startsWith(nodeName.takeLast(1)) == true
                        val isPruned = step.prunedNodeIds.contains(nodeName)
                        val isExplored = step.exploredNodeIds.contains(nodeName)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp * scale),
                            modifier = Modifier.weight(1f).padding(horizontal = 2.dp * scale)
                        ) {
                            Text(
                                text = when {
                                    isPruned -> "PRUNED"
                                    isCurrent -> "ACTIVE"
                                    isExplored -> "EXPLORED"
                                    else -> "PENDING"
                                },
                                color = when {
                                    isPruned -> RedAccent
                                    isCurrent -> OrangeAccent
                                    isExplored -> GreenAccent
                                    else -> TextSecondary
                                },
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Surface(
                                color = when {
                                    isPruned -> Color(0xFF381215)
                                    isCurrent -> Color(0xFF2A1C12)
                                    isExplored -> Color(0xFF123820)
                                    else -> Color(0xFF181824)
                                },
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(
                                    1.dp,
                                    when {
                                        isPruned -> RedAccent
                                        isCurrent -> OrangeAccent
                                        isExplored -> GreenAccent
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
                                    Text(nodeName, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("Bound: $bound", color = if (isPruned) RedAccent else OrangeAccent, fontSize = 9.sp)
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
private fun BranchBoundFundamentalsDetailsAndStepsContent(
    step: BranchBoundFundamentalsStep,
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
                Text("BRANCH & BOUND DECISION", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.action) {
                        BbFundamentalsAction.FOUND_FEASIBLE, BbFundamentalsAction.FINISHED -> GreenAccent
                        BbFundamentalsAction.PRUNE_BRANCH -> RedAccent
                        else -> OrangeAccent
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Lower Bound Pruning", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
