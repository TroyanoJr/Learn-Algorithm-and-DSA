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
internal fun BestFirstSearchVisualizationCard(
    title: String = "Best-First Search and Priority Queues"
) {
    val steps = BestFirstSearchTrace.createTrace()
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val playback = rememberVisualizerPlaybackState(
        stepCount = steps.size,
        initialStep = 0
    )
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val step = steps[stepIndex]

    ArrayWindowButton(text = "Open best-first search visualizer") {
        isExpanded = true
    }

    if (isExpanded) {
        val statusText = when (step.action) {
            BestFirstAction.INIT -> "BEST-FIRST SEARCH: MIN-HEAP PRIORITY QUEUE"
            BestFirstAction.PUSH_NODES -> "ENQUEUE ACTIVE NODES SORTED BY BOUND"
            BestFirstAction.EXTRACT_BEST -> "POP TOP NODE ${step.extractedNode?.id} (MOST PROMISING BOUND ${step.extractedNode?.bound})"
            BestFirstAction.FOUND_FEASIBLE -> "FEASIBLE SOLUTION FOUND! INCUMBENT = ${step.incumbentCost}"
            BestFirstAction.PRUNE_REMAINING_PQ -> "PRUNED ALL REMAINING PQ NODES (BOUNDS >= ${step.incumbentCost})"
            BestFirstAction.FINISHED -> "SEARCH COMPLETE · OPTIMAL COST = ${step.incumbentCost}"
        }

        val statusColor = when (step.action) {
            BestFirstAction.FOUND_FEASIBLE, BestFirstAction.FINISHED -> GreenAccent
            BestFirstAction.PRUNE_REMAINING_PQ -> RedAccent
            BestFirstAction.EXTRACT_BEST, BestFirstAction.PUSH_NODES -> OrangeAccent
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
                    BestFirstSearchVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                BestFirstSearchDetailsAndStepsContent(
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
private fun BestFirstSearchVisualizationContent(step: BestFirstSearchStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Incumbent & Active Node Header Card
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
                    Text("CURRENT MIN INCUMBENT", color = OrangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("Cost: ${step.incumbentCost ?: "INF"}", color = GreenAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Surface(
                    color = Color(0xFF122C38),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, BlueAccent)
                ) {
                    Text(
                        text = "Extracted: ${step.extractedNode?.id ?: "None"}",
                        color = BlueAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp * scale, vertical = 4.dp * scale)
                    )
                }
            }
        }

        // Priority Queue (Min-Heap) Card
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
                Text("PRIORITY QUEUE (MIN-HEAP ORDERED BY BOUND):", color = BlueAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)

                if (step.priorityQueue.isEmpty()) {
                    Text(
                        text = if (step.prunedNodes.isNotEmpty()) "Priority queue emptied via pruning!" else "Priority queue empty.",
                        color = if (step.prunedNodes.isNotEmpty()) RedAccent else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp * scale)
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        step.priorityQueue.forEachIndexed { idx, node ->
                            val isTop = idx == 0

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp * scale),
                                modifier = Modifier.weight(1f).padding(horizontal = 2.dp * scale)
                            ) {
                                Text(
                                    text = if (isTop) "TOP (MIN)" else "pos #$idx",
                                    color = if (isTop) OrangeAccent else TextSecondary,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Surface(
                                    color = if (isTop) Color(0xFF2A1C12) else Color(0xFF181824),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, if (isTop) OrangeAccent else CardBorderColor),
                                    modifier = Modifier.fillMaxWidth().height(42.dp * scale)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(node.id, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text("b:${node.bound}", color = OrangeAccent, fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Pruned Nodes Summary Card
        if (step.prunedNodes.isNotEmpty()) {
            Surface(
                color = Color(0xFF13131C),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, RedAccent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp * scale).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pruned PQ Items: ${step.prunedNodes.map { "${it.id}(b:${it.bound})" }}",
                        color = RedAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${step.prunedNodes.size} Pruned",
                        color = RedAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun BestFirstSearchDetailsAndStepsContent(
    step: BestFirstSearchStep,
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
                Text("BEST-FIRST DECISION", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.action) {
                        BestFirstAction.FOUND_FEASIBLE, BestFirstAction.FINISHED -> GreenAccent
                        BestFirstAction.PRUNE_REMAINING_PQ -> RedAccent
                        else -> OrangeAccent
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Priority Queue Best-First", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
