package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.style.TextAlign
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
internal fun HashTableCollisionVisualizationCard(
    title: String = "Collisions and Collision Resolution"
) {
    var selectedStrategy by rememberSaveable { mutableStateOf(CollisionStrategy.SEPARATE_CHAINING) }
    val steps = HashTableCollisionTrace.stepsForStrategy(selectedStrategy)
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val playback = rememberVisualizerPlaybackState(
        stepCount = steps.size,
        initialStep = 0,
        key = selectedStrategy
    )
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted
    val step = steps[stepIndex]

    ArrayWindowButton(text = "Open collision visualizer") {
        isExpanded = true
    }

    if (isExpanded) {
        val statusText = when (step.event) {
            CollisionTraceEvent.START -> "READY · ${if (selectedStrategy == CollisionStrategy.SEPARATE_CHAINING) "CHAINING" else "PROBING"}"
            CollisionTraceEvent.SELECT_KEY -> "KEY: ${step.activeKey}"
            CollisionTraceEvent.COMPUTE_HASH -> "HASH INDEX = ${step.initialHashIndex}"
            CollisionTraceEvent.COLLISION_DETECTED -> "COLLISION AT [${step.highlightedBucketIndex}]!"
            CollisionTraceEvent.CHAIN_NODE -> "CHAINED AT [${step.highlightedBucketIndex}]"
            CollisionTraceEvent.PROBE_NEXT_SLOT -> "PROBING SLOT [${step.currentProbeIndex}]"
            CollisionTraceEvent.INSERT_ENTRY -> "STORED AT [${step.highlightedBucketIndex}]"
            CollisionTraceEvent.COMPLETE -> "COMPLETE"
        }

        val statusColor = when (step.event) {
            CollisionTraceEvent.COLLISION_DETECTED -> RedAccent
            CollisionTraceEvent.COMPLETE -> GreenAccent
            CollisionTraceEvent.CHAIN_NODE, CollisionTraceEvent.INSERT_ENTRY -> OrangeAccent
            CollisionTraceEvent.PROBE_NEXT_SLOT -> BlueAccent
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
            topContent = {
                CollisionStrategySelector(
                    selectedStrategy = selectedStrategy,
                    onStrategySelected = { newStrategy ->
                        if (newStrategy != selectedStrategy) {
                            playback.reset()
                            selectedStrategy = newStrategy
                        }
                    }
                )
            },
            visualContent = {
                FitToPane(modifier = Modifier.fillMaxSize()) {
                    HashTableCollisionVisualizationContent(step = step, scale = 1f)
                }
            },
            detailsContent = {
                HashTableCollisionDetailsAndStepsContent(
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
private fun CollisionStrategySelector(
    selectedStrategy: CollisionStrategy,
    onStrategySelected: (CollisionStrategy) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 6.dp)
    ) {
        listOf(
            CollisionStrategy.SEPARATE_CHAINING to "1. Separate Chaining",
            CollisionStrategy.LINEAR_PROBING to "2. Open Addressing (Linear Probing)"
        ).forEach { (strategy, label) ->
            val isSelected = selectedStrategy == strategy
            Surface(
                color = if (isSelected) Color(0xFF2A1C12) else Color(0xFF22222E),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (isSelected) OrangeAccent else CardBorderColor),
                modifier = Modifier
                    .heightIn(min = 38.dp)
                    .clickable { onStrategySelected(strategy) }
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
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
private fun HashTableCollisionVisualizationContent(step: CollisionTraceStep, scale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Status Card
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
                    Text(
                        text = if (step.strategy == CollisionStrategy.SEPARATE_CHAINING) "SEPARATE CHAINING (LINKED LISTS)" else "OPEN ADDRESSING (LINEAR PROBING)",
                        color = OrangeAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (step.activeKey != null) {
                        Text(
                            text = "Key: \"${step.activeKey}\" → Value: \"${step.activeValue}\" (Hash Index = ${step.initialHashIndex})",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text("Select playback steps to observe collision resolution.", color = TextSecondary, fontSize = 11.sp)
                    }
                }

                if (step.event == CollisionTraceEvent.COLLISION_DETECTED) {
                    Surface(
                        color = Color(0xFF381212),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, RedAccent)
                    ) {
                        Text(
                            "COLLISION DETECTED",
                            color = RedAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp * scale, vertical = 4.dp * scale)
                        )
                    }
                }
            }
        }

        // Table Representation based on Strategy
        if (step.strategy == CollisionStrategy.SEPARATE_CHAINING) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Top
            ) {
                step.chainingBuckets.forEachIndexed { index, chain ->
                    val isHighlighted = index == step.highlightedBucketIndex
                    val isCollision = isHighlighted && step.event == CollisionTraceEvent.COLLISION_DETECTED

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp * scale),
                        modifier = Modifier.weight(1f).padding(horizontal = 2.dp * scale)
                    ) {
                        Text(
                            text = if (isHighlighted) "↓" else " ",
                            color = if (isCollision) RedAccent else OrangeAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Bucket Index Header
                        Surface(
                            color = when {
                                isCollision -> Color(0xFF381212)
                                isHighlighted -> Color(0xFF2A1C12)
                                chain.isNotEmpty() -> Color(0xFF13233A)
                                else -> Color(0xFF13131C)
                            },
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(
                                1.dp,
                                when {
                                    isCollision -> RedAccent
                                    isHighlighted -> OrangeAccent
                                    chain.isNotEmpty() -> BlueAccent
                                    else -> CardBorderColor
                                }
                            ),
                            modifier = Modifier.fillMaxWidth().height(26.dp * scale)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "[$index]",
                                    color = if (isCollision) RedAccent else if (isHighlighted) OrangeAccent else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Linked List Chain Nodes
                        if (chain.isEmpty()) {
                            Text(
                                text = "null",
                                color = TextSecondary.copy(alpha = 0.4f),
                                fontSize = 9.sp
                            )
                        } else {
                            chain.forEachIndexed { nodeIdx, node ->
                                Surface(
                                    color = if (isHighlighted && nodeIdx == chain.lastIndex && step.event == CollisionTraceEvent.CHAIN_NODE) Color(0xFF2A1C12) else Color(0xFF13233A),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, if (isHighlighted && nodeIdx == chain.lastIndex) OrangeAccent else BlueAccent),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(4.dp * scale),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(node.key, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                        Text(node.value, color = BlueAccent, fontSize = 8.sp, maxLines = 1)
                                    }
                                }
                                Text(
                                    text = if (nodeIdx < chain.lastIndex) "↓" else "↓ null",
                                    color = TextSecondary,
                                    fontSize = 8.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Linear Probing UI
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                step.linearSlots.forEachIndexed { index, slot ->
                    val isHighlighted = index == step.highlightedBucketIndex
                    val isProbed = index in step.probedIndices
                    val isCollision = isHighlighted && step.event == CollisionTraceEvent.COLLISION_DETECTED
                    val isProbeTarget = index == step.currentProbeIndex && step.event == CollisionTraceEvent.PROBE_NEXT_SLOT
                    val isOccupied = slot != null

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp * scale),
                        modifier = Modifier.weight(1f).padding(horizontal = 2.dp * scale)
                    ) {
                        Text(
                            text = when {
                                isCollision -> "COLLISION"
                                isProbeTarget -> "PROBE"
                                isHighlighted -> "TARGET"
                                isProbed -> "PROBED"
                                else -> " "
                            },
                            color = when {
                                isCollision -> RedAccent
                                isProbeTarget -> BlueAccent
                                isHighlighted -> OrangeAccent
                                isProbed -> TextSecondary
                                else -> Color.Transparent
                            },
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )

                        Surface(
                            color = when {
                                isCollision -> Color(0xFF381212)
                                isProbeTarget -> Color(0xFF13283A)
                                isHighlighted -> Color(0xFF2A1C12)
                                isOccupied -> Color(0xFF13233A)
                                else -> Color(0xFF13131C)
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(
                                1.dp,
                                when {
                                    isCollision -> RedAccent
                                    isProbeTarget -> BlueAccent
                                    isHighlighted -> OrangeAccent
                                    isOccupied -> BlueAccent
                                    else -> CardBorderColor
                                }
                            ),
                            modifier = Modifier.fillMaxWidth().height(54.dp * scale)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(2.dp * scale),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                if (slot != null) {
                                    Text(slot.key, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text(slot.value, color = BlueAccent, fontSize = 9.sp, maxLines = 1)
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
private fun HashTableCollisionDetailsAndStepsContent(
    step: CollisionTraceStep,
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
                    color = when (step.event) {
                        CollisionTraceEvent.COLLISION_DETECTED -> RedAccent
                        CollisionTraceEvent.COMPLETE -> GreenAccent
                        else -> OrangeAccent
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                val occupied = if (step.strategy == CollisionStrategy.SEPARATE_CHAINING) {
                    step.chainingBuckets.count { it.isNotEmpty() }
                } else {
                    step.linearSlots.count { it != null }
                }
                Text("Occupied buckets: $occupied / ${step.capacity}", color = TextPrimary, fontSize = 11.sp)
                Text(
                    text = if (step.strategy == CollisionStrategy.SEPARATE_CHAINING) "Tradeoff: Extra memory for linked nodes" else "Tradeoff: Primary clustering risk",
                    color = TextSecondary,
                    fontSize = 10.sp
                )
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
