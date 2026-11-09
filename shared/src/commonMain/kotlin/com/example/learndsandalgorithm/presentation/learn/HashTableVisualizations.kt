package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9CA3AF)

@Composable
internal fun HashTableVisualizationCard(
    title: String = "Hash Table Fundamentals"
) {
    val steps = HashTableTrace.stepsForFundamentals()
    val autoExpand = LocalAutoExpandVisualizer.current
    var isExpanded by rememberSaveable { mutableStateOf(autoExpand) }

    val playback = rememberVisualizerPlaybackState(stepCount = steps.size, initialStep = 0)
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val step = steps[stepIndex]

    if (!autoExpand) {
        ArrayWindowButton(text = "Open hash table visualizer") {
            isExpanded = true
        }
    }

    if (isExpanded) {
        val statusText = when (step.event) {
            HashTableEvent.STRUCTURE_OVERVIEW -> "STRUCTURE OVERVIEW"
            HashTableEvent.SELECT_KEY -> "KEY: ${step.activeKey}"
            HashTableEvent.HASH_COMPUTATION -> "HASH SUM = ${step.hashSum}"
            HashTableEvent.INDEX_MODULO -> "INDEX = ${step.calculatedIndex}"
            HashTableEvent.HIGHLIGHT_BUCKET -> "TARGET BUCKET [${step.calculatedIndex}]"
            HashTableEvent.INSERT_ENTRY -> "STORED AT [${step.calculatedIndex}]"
            HashTableEvent.COMPLETE -> "COMPLETE"
        }

        val statusColor = when (step.event) {
            HashTableEvent.COMPLETE -> GreenAccent
            HashTableEvent.INSERT_ENTRY -> OrangeAccent
            HashTableEvent.INDEX_MODULO, HashTableEvent.HIGHLIGHT_BUCKET -> BlueAccent
            else -> TextSecondary
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
                    HashTableVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                HashTableDetailsAndStepsContent(
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
private fun HashTableVisualizationContent(step: HashTableTraceStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Top Section: Hash Function Calculation Card
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
                    Text(
                        "HASH FUNCTION MAPPING",
                        color = OrangeAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "index = hash(key) % capacity",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }

                if (step.activeKey != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp * scale),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Key Input badge
                        Surface(
                            color = Color(0xFF1F1F2C),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, OrangeAccent)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp * scale, vertical = 4.dp * scale),
                                horizontalArrangement = Arrangement.spacedBy(4.dp * scale),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Key:", color = TextSecondary, fontSize = 10.sp)
                                Text("\"${step.activeKey}\"", color = OrangeAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("→ Value:", color = TextSecondary, fontSize = 10.sp)
                                Text("\"${step.activeValue}\"", color = BlueAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Hash Calculation status
                        if (step.charBreakdown != null) {
                            Text("=", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            val calcStr = step.charBreakdown.joinToString("+") { "${it.first}(${it.second})" }
                            Surface(
                                color = Color(0xFF1A1A26),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, CardBorderColor)
                            ) {
                                Text(
                                    text = calcStr,
                                    color = TextPrimary,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp * scale, vertical = 4.dp * scale)
                                )
                            }
                        }

                        if (step.hashSum != null) {
                            Text("=", color = TextSecondary, fontSize = 12.sp)
                            Text("${step.hashSum}", color = GreenAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        if (step.calculatedIndex != null) {
                            Text("% ${step.capacity} =", color = TextSecondary, fontSize = 12.sp)
                            Surface(
                                color = Color(0xFF2A1C12),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, OrangeAccent)
                            ) {
                                Text(
                                    text = "Index [${step.calculatedIndex}]",
                                    color = OrangeAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp * scale, vertical = 4.dp * scale)
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        "Select a key to view hash calculation step-by-step.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Bottom Section: Table Buckets (0..capacity-1)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            step.buckets.forEachIndexed { index, entry ->
                val isHighlighted = index == step.highlightedBucketIndex
                val isTarget = index == step.calculatedIndex
                val isOccupied = entry != null

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp * scale),
                    modifier = Modifier.weight(1f).padding(horizontal = 2.dp * scale)
                ) {
                    // Target indicator arrow
                    Text(
                        text = if (isTarget || isHighlighted) "↓" else " ",
                        color = if (isHighlighted) OrangeAccent else BlueAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Bucket Card
                    Surface(
                        color = when {
                            isHighlighted -> Color(0xFF2A1C12)
                            isOccupied -> Color(0xFF13233A)
                            else -> Color(0xFF13131C)
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            1.dp,
                            when {
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
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(2.dp * scale),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (entry != null) {
                                Text(
                                    text = entry.key,
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = entry.value,
                                    color = BlueAccent,
                                    fontSize = 9.sp,
                                    maxLines = 1
                                )
                            } else {
                                Text(
                                    text = "·",
                                    color = TextSecondary.copy(alpha = 0.5f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Bucket Index Label
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

@Composable
private fun HashTableDetailsAndStepsContent(
    step: HashTableTraceStep,
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
                Text("CURRENT OPERATION", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(
                    step.badgeText,
                    color = if (step.event == HashTableEvent.COMPLETE) GreenAccent else OrangeAccent,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                val occupiedCount = step.buckets.count { it != null }
                Text("Occupied: $occupiedCount / ${step.capacity} buckets", color = TextPrimary, fontSize = 11.sp)
                Text("Capacity: ${step.capacity}", color = TextSecondary, fontSize = 10.sp)
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
