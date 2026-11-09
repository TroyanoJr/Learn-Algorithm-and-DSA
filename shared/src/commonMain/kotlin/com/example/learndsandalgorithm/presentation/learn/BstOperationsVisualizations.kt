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
private val RedAccent = Color(0xFFEF4444)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9CA3AF)

@Composable
internal fun BstOperationsVisualizationCard(
    title: String = "Binary Search Tree Operations, Complexity, and Trade-offs"
) {
    var selectedOp by rememberSaveable { mutableStateOf(BstOpType.SEARCH) }
    val steps = BstOperationsTrace.stepsForOperation(selectedOp)
    val autoExpand = LocalAutoExpandVisualizer.current
    var isExpanded by rememberSaveable { mutableStateOf(autoExpand) }

    val playback = rememberVisualizerPlaybackState(
        stepCount = steps.size,
        initialStep = 0,
        key = selectedOp
    )
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val step = steps[stepIndex]

    if (!autoExpand) {
        ArrayWindowButton(text = "Open BST operations visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.opType) {
            BstOpType.SEARCH -> "BST SEARCH (Target ${step.targetVal})"
            BstOpType.INSERT -> "BST INSERT (Target ${step.targetVal})"
            BstOpType.DELETE_CASE_1_LEAF -> "DELETE CASE 1: LEAF NODE [${step.targetVal}]"
            BstOpType.DELETE_CASE_2_ONE_CHILD -> "DELETE CASE 2: 1 CHILD [${step.targetVal}]"
            BstOpType.DELETE_CASE_3_TWO_CHILDREN -> "DELETE CASE 3: 2 CHILDREN [${step.targetVal}]"
        }

        val statusColor = when (step.opType) {
            BstOpType.SEARCH -> BlueAccent
            BstOpType.INSERT -> OrangeAccent
            else -> RedAccent
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
                BstOperationSelector(
                    selectedOp = selectedOp,
                    onOpSelected = { newOp ->
                        if (newOp != selectedOp) {
                            playback.reset()
                            selectedOp = newOp
                        }
                    }
                )
            },
            visualContent = {
                FitToPane(modifier = Modifier.fillMaxSize()) {
                    BstOperationsVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                BstOperationsDetailsAndStepsContent(
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
private fun BstOperationSelector(
    selectedOp: BstOpType,
    onOpSelected: (BstOpType) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 6.dp)
    ) {
        listOf(
            BstOpType.SEARCH to "Search",
            BstOpType.INSERT to "Insert",
            BstOpType.DELETE_CASE_1_LEAF to "Del Leaf",
            BstOpType.DELETE_CASE_2_ONE_CHILD to "Del 1-Child",
            BstOpType.DELETE_CASE_3_TWO_CHILDREN to "Del 2-Children"
        ).forEach { (op, label) ->
            val isSelected = selectedOp == op
            Surface(
                color = if (isSelected) Color(0xFF2A1C12) else Color(0xFF22222E),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (isSelected) OrangeAccent else CardBorderColor),
                modifier = Modifier
                    .heightIn(min = 34.dp)
                    .clickable { onOpSelected(op) }
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
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
private fun BstOperationsVisualizationContent(step: BstOperationsTraceStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Operation Info Header Card
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
                Column(verticalArrangement = Arrangement.spacedBy(2.dp * scale)) {
                    Text(
                        text = "BST OPERATION: ${step.opType.name}",
                        color = OrangeAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Target Value: ${step.targetVal} | ${if (step.successorVal != null) "In-order Successor: ${step.successorVal}" else "Average Complexity: O(log N)"}",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }

                Surface(
                    color = Color(0xFF123820),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, GreenAccent)
                ) {
                    Text(
                        text = "O(log N) AVG",
                        color = GreenAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp * scale, vertical = 4.dp * scale)
                    )
                }
            }
        }

        // Tree Render Box
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
                step.nodes.chunked(3).forEach { rowNodes ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        rowNodes.forEach { node ->
                            val isInspected = node.id == step.inspectedNodeId
                            TreeNodeCircle(
                                value = "${node.value}",
                                isHighlighted = isInspected || node.isHighlighted,
                                isVisited = node.isSuccessor,
                                badgeLabel = if (node.isSuccessor) "Successor" else null
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BstOperationsDetailsAndStepsContent(
    step: BstOperationsTraceStep,
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
                Text("CURRENT OPERATION STEP", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.opType) {
                        BstOpType.SEARCH -> BlueAccent
                        BstOpType.INSERT -> OrangeAccent
                        else -> RedAccent
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Balanced: O(log N) | Skewed: O(N)", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
