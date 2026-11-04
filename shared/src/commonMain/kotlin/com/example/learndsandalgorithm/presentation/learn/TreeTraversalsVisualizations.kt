package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9CA3AF)

@Composable
internal fun TreeTraversalsVisualizationCard(
    title: String = "Tree Traversals and Their Applications"
) {
    var selectedOrder by rememberSaveable { mutableStateOf(TraversalOrder.IN_ORDER) }
    val steps = TreeTraversalsTrace.stepsForOrder(selectedOrder)
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val playback = rememberVisualizerPlaybackState(
        stepCount = steps.size,
        initialStep = 0,
        key = selectedOrder
    )
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val step = steps[stepIndex]

    ArrayWindowButton(text = "Open tree traversals visualizer") {
        isExpanded = true
    }

    if (isExpanded) {
        val statusText = when (step.order) {
            TraversalOrder.PRE_ORDER -> "PRE-ORDER TRAVERSAL (Root → Left → Right)"
            TraversalOrder.IN_ORDER -> "IN-ORDER TRAVERSAL (Left → Root → Right)"
            TraversalOrder.POST_ORDER -> "POST-ORDER TRAVERSAL (Left → Right → Root)"
            TraversalOrder.LEVEL_ORDER -> "LEVEL-ORDER TRAVERSAL (BFS Queue)"
        }

        val statusColor = when (step.order) {
            TraversalOrder.IN_ORDER -> GreenAccent
            TraversalOrder.LEVEL_ORDER -> BlueAccent
            else -> OrangeAccent
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
            topContent = {
                TraversalOrderSelector(
                    selectedOrder = selectedOrder,
                    onOrderSelected = { newOrder ->
                        if (newOrder != selectedOrder) {
                            playback.reset()
                            selectedOrder = newOrder
                        }
                    }
                )
            },
            visualContent = {
                FitToPane(modifier = Modifier.fillMaxSize()) {
                    TreeTraversalsVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                TreeTraversalsDetailsAndStepsContent(
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
private fun TraversalOrderSelector(
    selectedOrder: TraversalOrder,
    onOrderSelected: (TraversalOrder) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 6.dp)
    ) {
        listOf(
            TraversalOrder.IN_ORDER to "In-order",
            TraversalOrder.PRE_ORDER to "Pre-order",
            TraversalOrder.POST_ORDER to "Post-order",
            TraversalOrder.LEVEL_ORDER to "Level-order (BFS)"
        ).forEach { (order, label) ->
            val isSelected = selectedOrder == order
            Surface(
                color = if (isSelected) Color(0xFF2A1C12) else Color(0xFF22222E),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (isSelected) OrangeAccent else CardBorderColor),
                modifier = Modifier
                    .heightIn(min = 36.dp)
                    .clickable { onOrderSelected(order) }
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) OrangeAccent else TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun TreeTraversalsVisualizationContent(step: TreeTraversalsTraceStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // BFS Queue Card (if Level Order)
        if (step.order == TraversalOrder.LEVEL_ORDER) {
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
                    Text("BFS QUEUE (FRONT → REAR):", color = BlueAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp * scale)) {
                        if (step.queueValues.isEmpty()) {
                            Text("Queue empty", color = TextSecondary, fontSize = 10.sp)
                        } else {
                            step.queueValues.forEach { qVal ->
                                Surface(
                                    color = Color(0xFF122838),
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(1.dp, BlueAccent)
                                ) {
                                    Text(
                                        text = "[$qVal]",
                                        color = BlueAccent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp * scale, vertical = 2.dp * scale)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Tree Visual Layout
        Surface(
            color = Color(0xFF13131C),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, CardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(10.dp * scale).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp * scale)
            ) {
                TreeNodeCircle(
                    value = "5",
                    isHighlighted = "N5" == step.currentNodeId,
                    isVisited = 5 in step.visitedNodes,
                    badgeLabel = "Root 5"
                )
                TreeBranchConnector(label = "↙     ↘")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TreeNodeCircle(
                        value = "3",
                        isHighlighted = "N3" == step.currentNodeId,
                        isVisited = 3 in step.visitedNodes
                    )
                    TreeNodeCircle(
                        value = "7",
                        isHighlighted = "N7" == step.currentNodeId,
                        isVisited = 7 in step.visitedNodes
                    )
                }

                TreeBranchConnector(label = "↙   ↘     ↙   ↘")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TreeNodeCircle("2", isHighlighted = "N2" == step.currentNodeId, isVisited = 2 in step.visitedNodes)
                    TreeNodeCircle("4", isHighlighted = "N4" == step.currentNodeId, isVisited = 4 in step.visitedNodes)
                    TreeNodeCircle("6", isHighlighted = "N6" == step.currentNodeId, isVisited = 6 in step.visitedNodes)
                    TreeNodeCircle("8", isHighlighted = "N8" == step.currentNodeId, isVisited = 8 in step.visitedNodes)
                }
            }
        }

        // Visited Sequence Output Array Card
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
                Text(
                    text = if (step.order == TraversalOrder.IN_ORDER) "VISITED SEQUENCE (SORTED ASCENDING):" else "VISITED SEQUENCE OUTPUT:",
                    color = GreenAccent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                if (step.visitedNodes.isEmpty()) {
                    Text("No nodes visited yet", color = TextSecondary, fontSize = 10.sp)
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        step.visitedNodes.forEachIndexed { idx, valItem ->
                            val isJustVisited = idx == step.visitedNodes.lastIndex
                            Surface(
                                color = if (isJustVisited) Color(0xFF123820) else Color(0xFF181824),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, if (isJustVisited) GreenAccent else CardBorderColor),
                                modifier = Modifier.padding(end = 4.dp * scale)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 10.dp * scale, vertical = 4.dp * scale)) {
                                    Text("$valItem", color = GreenAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
private fun TreeTraversalsDetailsAndStepsContent(
    step: TreeTraversalsTraceStep,
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
                Text("CURRENT TRAVERSAL STEP", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.order) {
                        TraversalOrder.IN_ORDER -> GreenAccent
                        TraversalOrder.LEVEL_ORDER -> BlueAccent
                        else -> OrangeAccent
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Visited: ${step.visitedNodes.size} / 7", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
