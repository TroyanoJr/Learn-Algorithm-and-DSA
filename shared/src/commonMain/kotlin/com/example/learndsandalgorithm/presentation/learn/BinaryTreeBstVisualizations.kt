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
import androidx.compose.runtime.remember
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

internal enum class BstPresentationState { CHECKING, VALID, INVALID }

internal fun bstPresentationState(step: BinaryTreeBstTraceStep): BstPresentationState = when {
    !step.isValidBst -> BstPresentationState.INVALID
    step.scenario == BstComparisonScenario.INVALID_BST_VIOLATION -> BstPresentationState.CHECKING
    else -> BstPresentationState.VALID
}

private val BstPresentationState.label: String get() = when (this) {
    BstPresentationState.CHECKING -> "CHECKING BST"
    BstPresentationState.VALID -> "VALID BST"
    BstPresentationState.INVALID -> "INVALID BST"
}

private val BstPresentationState.accent: Color get() = when (this) {
    BstPresentationState.CHECKING -> OrangeAccent
    BstPresentationState.VALID -> GreenAccent
    BstPresentationState.INVALID -> RedAccent
}

@Composable
internal fun BinaryTreeBstVisualizationCard(
    title: String = "Binary Trees and Binary Search Trees"
) {
    var selectedScenario by rememberSaveable { mutableStateOf(BstComparisonScenario.VALID_BST) }
    val steps = BinaryTreeBstTrace.stepsForScenario(selectedScenario)
    val autoExpand = LocalAutoExpandVisualizer.current
    var isExpanded by rememberSaveable { mutableStateOf(autoExpand) }

    val playback = rememberVisualizerPlaybackState(
        stepCount = steps.size,
        initialStep = 0,
        key = selectedScenario
    )
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val step = steps[stepIndex]

    val bstScenes = remember(selectedScenario) {
        val s = BinaryTreeBstTrace.stepsForScenario(selectedScenario)
        if (s.isNotEmpty()) {
            BstSceneBuilder.buildBstComparisonScenes(s)
        } else {
            emptyList()
        }
    }

    if (!autoExpand) {
        ArrayWindowButton(text = "Open binary tree vs BST visualizer") {
            isExpanded = true
        }
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
                val currentScene = if (stepIndex in bstScenes.indices) bstScenes[stepIndex] else null
                if (currentScene != null) {
                    TreeSceneRenderer(
                        scene = currentScene,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    FitToPane(modifier = Modifier.fillMaxSize()) {
                        BinaryTreeBstVisualizationContent(step = step, scale = 1f)
                    }
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
    val presentation = bstPresentationState(step)
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
                    color = when (presentation) {
                        BstPresentationState.INVALID -> Color(0xFF381212)
                        BstPresentationState.VALID -> Color(0xFF123820)
                        BstPresentationState.CHECKING -> Color(0xFF2A1C12)
                    },
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, presentation.accent)
                ) {
                    Text(
                        text = presentation.label,
                        color = presentation.accent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp * scale, vertical = 4.dp * scale)
                    )
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
    val presentation = bstPresentationState(step)
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Surface(color = Color(0xFF13131C), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("BST ORDERING RULE: left < parent < right", color = OrangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(presentation.label, color = presentation.accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                when (step.scenario) {
                    BstComparisonScenario.VALID_BST -> Text("Root 5 · < 5 left · > 5 right", color = TextSecondary, fontSize = 9.sp)
                    BstComparisonScenario.INVALID_BST_VIOLATION -> {
                        if (step.violationMessage != null) Text(step.violationMessage, color = RedAccent, fontSize = 9.sp)
                    }
                    BstComparisonScenario.BALANCED_VS_SKEWED -> {
                        Text("SKEWED BINARY TREE (SORTED INPUT 1,2,3,4,5):", color = OrangeAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("↘ right: 1 → 2 → 3 → 4 → 5 · leaf: 5", color = TextPrimary, fontSize = 9.sp)
                    }
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("CURRENT CHECK  ", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(step.badgeText, color = presentation.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, lineHeight = 13.sp)
                }
            }
        }

        Surface(color = Color(0xFF161622), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(horizontal = 8.dp, vertical = 5.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Steps", color = OrangeAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("STEP ${stepIndex + 1} / $totalSteps", color = if (stepIndex == totalSteps - 1) GreenAccent else TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Text(step.explanation, color = TextPrimary, fontSize = 12.sp, lineHeight = 15.sp)
            }
        }
    }
}
