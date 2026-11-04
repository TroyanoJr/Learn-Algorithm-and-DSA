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
internal fun BinaryTreeBstVisualizationCard(
    title: String = "Binary Trees and Binary Search Trees"
) {
    var selectedScenario by rememberSaveable { mutableStateOf(BstComparisonScenario.VALID_BST) }
    val steps = BinaryTreeBstTrace.stepsForScenario(selectedScenario)
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val playback = rememberVisualizerPlaybackState(
        stepCount = steps.size,
        initialStep = 0,
        key = selectedScenario
    )
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val step = steps[stepIndex]

    ArrayWindowButton(text = "Open binary tree vs BST visualizer") {
        isExpanded = true
    }

    if (isExpanded) {
        val statusText = when (step.scenario) {
            BstComparisonScenario.VALID_BST -> "VALID BINARY SEARCH TREE (left < node < right)"
            BstComparisonScenario.INVALID_BST_VIOLATION -> if (!step.isValidBst) "BST VIOLATION DETECTED!" else "BST INVARIANT CHECK"
            BstComparisonScenario.BALANCED_VS_SKEWED -> "SKEWED TREE DEGENERATION (O(N) HEIGHT)"
        }

        val statusColor = when (step.scenario) {
            BstComparisonScenario.INVALID_BST_VIOLATION -> if (!step.isValidBst) RedAccent else OrangeAccent
            BstComparisonScenario.BALANCED_VS_SKEWED -> OrangeAccent
            BstComparisonScenario.VALID_BST -> GreenAccent
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
                BstScenarioSelector(
                    selectedScenario = selectedScenario,
                    onScenarioSelected = { newScenario ->
                        if (newScenario != selectedScenario) {
                            playback.reset()
                            selectedScenario = newScenario
                        }
                    }
                )
            },
            visualContent = {
                FitToPane(modifier = Modifier.fillMaxSize()) {
                    BinaryTreeBstVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                BinaryTreeBstDetailsAndStepsContent(
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
private fun BstScenarioSelector(
    selectedScenario: BstComparisonScenario,
    onScenarioSelected: (BstComparisonScenario) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 6.dp)
    ) {
        listOf(
            BstComparisonScenario.VALID_BST to "1. Valid BST Ordering",
            BstComparisonScenario.INVALID_BST_VIOLATION to "2. BST Invariant Violation",
            BstComparisonScenario.BALANCED_VS_SKEWED to "3. Balanced vs Skewed Tree"
        ).forEach { (scenario, label) ->
            val isSelected = selectedScenario == scenario
            Surface(
                color = if (isSelected) Color(0xFF2A1C12) else Color(0xFF22222E),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (isSelected) OrangeAccent else CardBorderColor),
                modifier = Modifier
                    .heightIn(min = 38.dp)
                    .clickable { onScenarioSelected(scenario) }
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) OrangeAccent else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun BinaryTreeBstVisualizationContent(step: BinaryTreeBstTraceStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Status Header Card
        Surface(
            color = Color(0xFF13131C),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, if (!step.isValidBst) RedAccent else CardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp * scale).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp * scale)) {
                    Text(
                        text = "BST ORDERING RULE: left < parent < right",
                        color = if (!step.isValidBst) RedAccent else OrangeAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (step.violationMessage != null) {
                        Text(
                            text = step.violationMessage,
                            color = RedAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text("Every node strictly satisfies left < node < right.", color = TextSecondary, fontSize = 10.sp)
                    }
                }

                Surface(
                    color = if (!step.isValidBst) Color(0xFF381212) else Color(0xFF123820),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, if (!step.isValidBst) RedAccent else GreenAccent)
                ) {
                    Text(
                        text = if (!step.isValidBst) "INVALID BST" else "VALID BST",
                        color = if (!step.isValidBst) RedAccent else GreenAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp * scale, vertical = 4.dp * scale)
                    )
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
                if (step.scenario == BstComparisonScenario.BALANCED_VS_SKEWED) {
                    // Skewed Tree Display (1 -> 2 -> 3 -> 4 -> 5)
                    Text("SKEWED BINARY TREE (SORTED INPUT 1,2,3,4,5):", color = OrangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    step.nodes.forEach { node ->
                        TreeNodeCircle(
                            value = "${node.value}",
                            isHighlighted = node.id == step.inspectedNodeId,
                            badgeLabel = if (node.rightId != null) "↘ right" else "leaf"
                        )
                    }
                } else {
                    // Tree Layout for Valid / Invalid BST
                    val root = step.nodes.firstOrNull { it.id == "N5" }
                    if (root != null) {
                        TreeNodeCircle(
                            value = "${root.value}",
                            isHighlighted = root.id == step.inspectedNodeId,
                            badgeLabel = "Root 5"
                        )
                        TreeBranchConnector(label = "↙     ↘")

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            val n3 = step.nodes.firstOrNull { it.id == "N3" }
                            val n7 = step.nodes.firstOrNull { it.id == "N7" }
                            if (n3 != null) {
                                TreeNodeCircle(
                                    value = "${n3.value}",
                                    isHighlighted = n3.id == step.inspectedNodeId,
                                    badgeLabel = "< 5"
                                )
                            }
                            if (n7 != null) {
                                TreeNodeCircle(
                                    value = "${n7.value}",
                                    isHighlighted = n7.id == step.inspectedNodeId,
                                    badgeLabel = "> 5"
                                )
                            }
                        }

                        TreeBranchConnector(label = "↙   ↘     ↙   ↘")

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            val n2 = step.nodes.firstOrNull { it.id == "N2" }
                            val n4 = step.nodes.firstOrNull { it.id == "N4" || it.id == "N4_bad" }
                            val n6 = step.nodes.firstOrNull { it.id == "N6" }
                            val n8 = step.nodes.firstOrNull { it.id == "N8" }

                            if (n2 != null) TreeNodeCircle("${n2.value}", isHighlighted = n2.id == step.inspectedNodeId)
                            if (n4 != null) TreeNodeCircle("${n4.value}", isHighlighted = n4.id == step.inspectedNodeId, isViolation = n4.isViolation)
                            if (n6 != null) TreeNodeCircle("${n6.value}", isHighlighted = n6.id == step.inspectedNodeId)
                            if (n8 != null) TreeNodeCircle("${n8.value}", isHighlighted = n8.id == step.inspectedNodeId)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BinaryTreeBstDetailsAndStepsContent(
    step: BinaryTreeBstTraceStep,
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
                Text("CURRENT CHECK", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = if (!step.isValidBst) RedAccent else GreenAccent,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("BST Invariant Rule", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
