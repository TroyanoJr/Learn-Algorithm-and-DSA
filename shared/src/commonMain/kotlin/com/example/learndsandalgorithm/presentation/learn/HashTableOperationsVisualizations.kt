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
internal fun HashTableOperationsVisualizationCard(
    title: String = "Hash Table Operations and Complexity"
) {
    val steps = HashTableOperationsTrace.stepsForOperations()
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val playback = rememberVisualizerPlaybackState(
        stepCount = steps.size,
        initialStep = 0
    )
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val step = steps[stepIndex]

    ArrayWindowButton(text = "Open operations visualizer") {
        isExpanded = true
    }

    if (isExpanded) {
        val statusText = when (step.activeOp) {
            HashOpType.INSERT -> "INSERT: ${step.activeKey ?: "READY"}"
            HashOpType.LOOKUP -> "LOOKUP: ${step.activeKey}"
            HashOpType.DELETE -> "DELETE: ${step.activeKey}"
            HashOpType.RESIZE_REHASH -> if (step.isThresholdExceeded) "RESIZE & REHASH (CAP ${step.oldCapacity} → ${step.capacity})" else "REHASHING"
        }

        val statusColor = when (step.activeOp) {
            HashOpType.RESIZE_REHASH -> RedAccent
            HashOpType.LOOKUP -> GreenAccent
            HashOpType.DELETE -> BlueAccent
            HashOpType.INSERT -> OrangeAccent
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
                    HashTableOperationsVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                HashTableOperationsDetailsAndStepsContent(
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
private fun HashTableOperationsVisualizationContent(step: OperationsTraceStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Load Factor Header Card
        Surface(
            color = Color(0xFF13131C),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, if (step.isThresholdExceeded) RedAccent else CardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp * scale).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp * scale)) {
                    Text(
                        text = "LOAD FACTOR: alpha = k / N = ${step.size} / ${step.capacity} = ${step.loadFactor}",
                        color = if (step.isThresholdExceeded) RedAccent else OrangeAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Threshold: alpha <= 0.75 ${if (step.isThresholdExceeded) "➔ EXCEEDED! Resizing N → 2N" else "(Collisions Low)"}",
                        color = if (step.isThresholdExceeded) RedAccent else TextSecondary,
                        fontSize = 10.sp
                    )
                }

                Surface(
                    color = when (step.activeOp) {
                        HashOpType.RESIZE_REHASH -> Color(0xFF381212)
                        HashOpType.LOOKUP -> Color(0xFF123820)
                        HashOpType.DELETE -> Color(0xFF122838)
                        HashOpType.INSERT -> Color(0xFF2A1C12)
                    },
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(
                        1.dp,
                        when (step.activeOp) {
                            HashOpType.RESIZE_REHASH -> RedAccent
                            HashOpType.LOOKUP -> GreenAccent
                            HashOpType.DELETE -> BlueAccent
                            HashOpType.INSERT -> OrangeAccent
                        }
                    )
                ) {
                    Text(
                        text = step.activeOp.name,
                        color = TextPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp * scale, vertical = 4.dp * scale)
                    )
                }
            }
        }

        // Display Old Array during Resize/Rehash if applicable
        if (step.oldBuckets != null && step.oldCapacity != null) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp * scale)
            ) {
                Text(
                    "OLD ARRAY (Capacity ${step.oldCapacity}):",
                    color = TextSecondary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    step.oldBuckets.forEachIndexed { idx, entry ->
                        Surface(
                            color = Color(0xFF1B1B26),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, CardBorderColor),
                            modifier = Modifier.weight(1f).height(32.dp * scale).padding(horizontal = 2.dp * scale)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (entry != null) "${entry.key} [${idx}]" else "·",
                                    color = TextSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Current Main Array (Capacity = step.capacity)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp * scale)
        ) {
            Text(
                "CURRENT HASH TABLE (Capacity ${step.capacity}):",
                color = OrangeAccent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                step.buckets.forEachIndexed { index, entry ->
                    val isHighlighted = index == step.highlightedIndex
                    val isTarget = index == step.targetIndex
                    val isOccupied = entry != null

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp * scale),
                        modifier = Modifier.weight(1f).padding(horizontal = 2.dp * scale)
                    ) {
                        Text(
                            text = if (isTarget || isHighlighted) "↓" else " ",
                            color = if (step.activeOp == HashOpType.DELETE) RedAccent else OrangeAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Surface(
                            color = when {
                                isHighlighted && step.activeOp == HashOpType.DELETE -> Color(0xFF381212)
                                isHighlighted -> Color(0xFF2A1C12)
                                isOccupied -> Color(0xFF13233A)
                                else -> Color(0xFF13131C)
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(
                                1.dp,
                                when {
                                    isHighlighted && step.activeOp == HashOpType.DELETE -> RedAccent
                                    isHighlighted -> OrangeAccent
                                    isOccupied -> BlueAccent
                                    else -> CardBorderColor
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp * scale)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(2.dp * scale),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                if (entry != null) {
                                    Text(entry.key, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text(entry.value, color = BlueAccent, fontSize = 9.sp, maxLines = 1)
                                } else {
                                    Text("·", color = TextSecondary.copy(alpha = 0.5f), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Text(
                            text = "[$index]",
                            color = if (isHighlighted) OrangeAccent else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HashTableOperationsDetailsAndStepsContent(
    step: OperationsTraceStep,
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
                Text("OPERATION", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = when (step.activeOp) {
                        HashOpType.RESIZE_REHASH -> RedAccent
                        HashOpType.LOOKUP -> GreenAccent
                        HashOpType.DELETE -> BlueAccent
                        HashOpType.INSERT -> OrangeAccent
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Size: ${step.size} / ${step.capacity}", color = TextPrimary, fontSize = 11.sp)
                val complexityStr = when (step.activeOp) {
                    HashOpType.RESIZE_REHASH -> "Cost: O(N) rehash"
                    HashOpType.INSERT -> "Complexity: O(1) avg (amortized)"
                    HashOpType.LOOKUP -> "Complexity: O(1) avg"
                    HashOpType.DELETE -> "Complexity: O(1) avg"
                }
                Text(complexityStr, color = GreenAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
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
