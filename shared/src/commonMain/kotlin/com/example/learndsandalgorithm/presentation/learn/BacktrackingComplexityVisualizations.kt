package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
internal fun BacktrackingComplexityVisualizationCard(
    title: String = "Backtracking Complexity & Pruning"
) {
    val steps = BacktrackingComplexityTrace.createTrace()
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
        ArrayWindowButton(text = "Open complexity & pruning visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.action) {
            ComplexityAction.INIT -> "BACKTRACKING COMPLEXITY & PRUNING IMPACT"
            ComplexityAction.EVALUATE_UNPRUNED -> "UNPRUNED EVALUATION AT INDEX ${step.currentIndex}"
            ComplexityAction.EVALUATE_PRUNED -> "PRUNED EVALUATION: ${step.currentValue} ≤ ${step.limit} (VALID)"
            ComplexityAction.PRUNED_HALT -> "PRUNED HALT: ${step.currentValue} > ${step.limit} (ABANDON SUBTREE)"
            ComplexityAction.FINISHED -> "COMPARISON COMPLETE · SAVED ${step.unprunedCalls - step.prunedCalls} CALL(S)"
        }

        val statusColor = when (step.action) {
            ComplexityAction.EVALUATE_PRUNED, ComplexityAction.FINISHED -> GreenAccent
            ComplexityAction.PRUNED_HALT -> RedAccent
            ComplexityAction.EVALUATE_UNPRUNED -> OrangeAccent
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
                    BacktrackingComplexityVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                BacktrackingComplexityDetailsAndStepsContent(
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
private fun BacktrackingComplexityVisualizationContent(step: BacktrackingComplexityStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Target Header Card
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
                    Text("PRUNING EFFICIENCY TEST", color = OrangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("Array: ${step.array} · Condition: x ≤ ${step.limit}", color = TextSecondary, fontSize = 10.sp)
                }

                Surface(
                    color = Color(0xFF123820),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, GreenAccent)
                ) {
                    Text(
                        text = "STACK SPACE O(H)",
                        color = GreenAccent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp * scale, vertical = 4.dp * scale)
                    )
                }
            }
        }

        // Side by Side Comparison
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp * scale)
        ) {
            // 1. Unpruned Strategy Column
            Surface(
                color = Color(0xFF13131C),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, CardBorderColor),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp * scale),
                    verticalArrangement = Arrangement.spacedBy(6.dp * scale),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("UNPRUNED (BRUTE FORCE)", color = OrangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("${step.unprunedCalls} Calls", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Evaluates all 4 elements unconditionally", color = TextSecondary, fontSize = 9.sp)
                }
            }

            // 2. Pruned Strategy Column
            Surface(
                color = Color(0xFF13131C),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, if (step.prunedCalls > 0) GreenAccent else CardBorderColor),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp * scale),
                    verticalArrangement = Arrangement.spacedBy(6.dp * scale),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("PRUNED (BACKTRACKING)", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("${step.prunedCalls} Calls", color = GreenAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Halts recursion when val > ${step.limit}", color = TextSecondary, fontSize = 9.sp)
                }
            }
        }

        // Paradigm & Pruning Explanation Card
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
                Text("PRUNING & WORST-CASE COMPLEXITY:", color = BlueAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("• This comparison illustrates pruning efficiency on this specific input instance, not a universal performance guarantee.", color = TextPrimary, fontSize = 9.sp)
                Text("• Pruning reduces work when a valid condition safely eliminates branches early.", color = TextPrimary, fontSize = 9.sp)
                Text("• In the worst case (when no branches are pruned), worst-case time complexity remains exponential (O(2^N) or O(N!)).", color = TextPrimary, fontSize = 9.sp)
            }
        }
    }
}

@Composable
private fun BacktrackingComplexityDetailsAndStepsContent(
    step: BacktrackingComplexityStep,
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
                Text("COMPLEXITY & PRUNING VERDICT", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.action) {
                        ComplexityAction.EVALUATE_PRUNED, ComplexityAction.FINISHED -> GreenAccent
                        ComplexityAction.PRUNED_HALT -> RedAccent
                        else -> OrangeAccent
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("Pruning Efficiency", color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
