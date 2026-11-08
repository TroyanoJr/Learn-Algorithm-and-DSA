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
internal fun PermutationsVisualizationCard(
    title: String = "Permutations & Unique Arrangements"
) {
    val steps = PermutationsTrace.createTrace()
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val playback = rememberVisualizerPlaybackState(
        stepCount = steps.size,
        initialStep = 0
    )
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val step = steps[stepIndex]

    ArrayWindowButton(text = "Open permutations visualizer") {
        isExpanded = true
    }

    if (isExpanded) {
        val statusText = when (step.action) {
            PermutationAction.INIT -> "PERMUTATIONS: GENERATING N! ARRANGEMENTS"
            PermutationAction.SWAP_CHOICE -> "SWAPPING arr[${step.swapI}] ↔ arr[${step.swapJ}]"
            PermutationAction.RECURSE -> "RECURSING TO INDEX ${step.startIndex}"
            PermutationAction.SOLUTION_FOUND -> "PERMUTATION FOUND: ${step.currentPermutation}"
            PermutationAction.BACKTRACK_SWAP -> "RESTORE ARRAY: SWAP BACK arr[${step.swapI}] ↔ arr[${step.swapJ}]"
            PermutationAction.FINISHED -> "PERMUTATIONS COMPLETE · ${step.completedPermutations.size} ARRANGEMENTS"
        }

        val statusColor = when (step.action) {
            PermutationAction.SOLUTION_FOUND, PermutationAction.FINISHED -> GreenAccent
            PermutationAction.BACKTRACK_SWAP -> RedAccent
            PermutationAction.SWAP_CHOICE -> OrangeAccent
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
                    PermutationsVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                PermutationsDetailsAndStepsContent(
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
private fun PermutationsVisualizationContent(step: PermutationStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Active Array State Card
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("WORKING ARRAY STATE:", color = OrangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("StartIndex: ${step.startIndex}", color = TextSecondary, fontSize = 10.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    step.currentPermutation.forEachIndexed { idx, valNum ->
                        val isSwapI = step.swapI == idx
                        val isSwapJ = step.swapJ == idx
                        val isStart = step.startIndex == idx

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp * scale),
                            modifier = Modifier.weight(1f).padding(horizontal = 2.dp * scale)
                        ) {
                            Text(
                                text = when {
                                    isSwapI && isSwapJ -> "FIX"
                                    isSwapI || isSwapJ -> "SWAP"
                                    isStart -> "START"
                                    else -> "idx $idx"
                                },
                                color = when {
                                    isSwapI || isSwapJ -> OrangeAccent
                                    isStart -> BlueAccent
                                    else -> TextSecondary
                                },
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Surface(
                                color = when {
                                    step.action == PermutationAction.SOLUTION_FOUND -> Color(0xFF123820)
                                    isSwapI || isSwapJ -> Color(0xFF2A1C12)
                                    else -> Color(0xFF181824)
                                },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(
                                    1.dp,
                                    when {
                                        step.action == PermutationAction.SOLUTION_FOUND -> GreenAccent
                                        isSwapI || isSwapJ -> OrangeAccent
                                        else -> CardBorderColor
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth().height(44.dp * scale)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("$valNum", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Completed Permutations Grid Card
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("GENERATED PERMUTATIONS (TOTAL ${step.completedPermutations.size} / 6):", color = BlueAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                if (step.completedPermutations.isEmpty()) {
                    Text("No permutations generated yet.", color = TextSecondary, fontSize = 10.sp, modifier = Modifier.padding(vertical = 4.dp * scale))
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp * scale)
                    ) {
                        step.completedPermutations.forEach { perm ->
                            Surface(
                                color = Color(0xFF123820),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, GreenAccent)
                            ) {
                                Text(
                                    "[${perm.joinToString(",")}]",
                                    color = GreenAccent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp * scale, vertical = 4.dp * scale)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermutationsDetailsAndStepsContent(
    step: PermutationStep,
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
                Text("PERMUTATION DECISION", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.action) {
                        PermutationAction.SOLUTION_FOUND, PermutationAction.FINISHED -> GreenAccent
                        PermutationAction.BACKTRACK_SWAP -> RedAccent
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
