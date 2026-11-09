package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.learndsandalgorithm.core.platform.LandscapeOrientationEffect
import kotlinx.coroutines.delay

private val DarkCardBgColor = Color(0xFF181820)
private val CardBorderColor = Color(0xFF262634)
private val OrangeAccent = Color(0xFFFA6E13)
private val BlueAccent = Color(0xFF38BDF8)
private val GreenAccent = Color(0xFF10B981)
private val RedAccent = Color(0xFFEF4444)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9CA3AF)

@Composable
internal fun LinearSearchVisualizationCard() {
    val traceSteps = LinearSearchTrace.steps
    val target = LinearSearchTrace.target
    val totalItems = LinearSearchTrace.values.size

    val autoExpand = LocalAutoExpandVisualizer.current
    var isExpanded by rememberSaveable { mutableStateOf(autoExpand) }

    val playback = rememberVisualizerPlaybackState(stepCount = traceSteps.size, initialStep = -1)
    val currentIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying

    val currentStep = traceSteps.getOrNull(currentIndex)
    val arrayData = currentStep?.values ?: LinearSearchTrace.values
    val isFound = currentStep?.result == LinearSearchResult.FOUND
    val isNotFound = currentStep?.result == LinearSearchResult.NOT_FOUND
    val isFinished = currentStep?.isTerminal == true
    val inspectedCount = currentStep?.inspectedCount ?: 0

    if (!autoExpand) {
        ArrayWindowButton(text = "Open linear search visualizer") { isExpanded = true }
    }

    if (isExpanded) {
        val badgeText = when {
            currentIndex < 0 -> "TARGET: $target"
            isFound -> "FOUND AT INDEX $currentIndex ✓"
            isNotFound -> "NOT FOUND"
            else -> "STEP ${currentIndex + 1} OF $totalItems"
        }

        val badgeColor = when {
            isFound -> GreenAccent
            isNotFound -> RedAccent
            else -> BlueAccent
        }

        val explanationText = if (currentIndex < 0) {
            "Target = $target. Inspect elements sequentially from index 0 until found."
        } else {
            currentStep?.explanation.orEmpty()
        }

        VisualizerModal(
            title = "LINEAR SEARCH",
            statusText = badgeText,
            statusColor = badgeColor,
            isCompleted = isFinished,
            stepIndex = if (currentIndex < 0) 0 else currentIndex,
            stepCount = traceSteps.size,
            isPlaying = isPlaying,
            layoutMode = VisualizerLayoutMode.FULL_WIDTH,
            onReset = { playback.reset() },
            onPrevious = { playback.previous() },
            onTogglePlayback = { playback.togglePlayback() },
            onNext = { playback.next() },
            onClose = {
                playback.reset()
                isExpanded = false
            },
            visualContent = {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val cellSize = ((maxWidth - 20.dp) / 10).coerceIn(28.dp, 44.dp)
                    val valueFontSize = (cellSize.value * 0.38f).sp
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        arrayData.forEachIndexed { index, value ->
                            val isCurrent = index == currentIndex && !isFinished
                            val isTargetMatch = index == currentIndex && isFound
                            val isInspected = index <= currentIndex && currentIndex >= 0

                            val cellBg = when {
                                isTargetMatch -> Color(0xFF092317)
                                isCurrent -> Color(0xFF2A1C12)
                                else -> Color(0xFF13131C)
                            }

                            val borderColor = when {
                                isTargetMatch -> GreenAccent
                                isCurrent -> OrangeAccent
                                else -> CardBorderColor
                            }

                            val textColor = when {
                                isTargetMatch -> GreenAccent
                                isCurrent -> OrangeAccent
                                isInspected -> TextSecondary
                                else -> TextPrimary
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(cellSize)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(cellBg)
                                        .border(
                                            width = if (isCurrent || isTargetMatch) 1.5.dp else 1.dp,
                                            color = borderColor,
                                            shape = RoundedCornerShape(10.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = value.toString(),
                                        color = textColor,
                                        fontSize = valueFontSize,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = index.toString(),
                                    color = if (isTargetMatch) GreenAccent else if (isCurrent) OrangeAccent else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = if (isCurrent || isTargetMatch) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            },
            detailsContent = {
                StepsCard(
                    label = "Linear Search",
                    explanation = explanationText,
                    stepIndex = if (currentIndex < 0) -1 else currentIndex,
                    totalSteps = totalItems,
                    isCompleted = isFinished,
                    footer = "$inspectedCount / $totalItems CHECKED"
                )
            }
        )
    }
}

@Composable
internal fun BinarySearchVisualizationCard() {
    val target = BinarySearchTrace.target
    val totalSteps = BinarySearchTrace.steps.size

    val autoExpand = LocalAutoExpandVisualizer.current
    var isExpanded by rememberSaveable { mutableStateOf(autoExpand) }

    val playback = rememberVisualizerPlaybackState(stepCount = totalSteps, initialStep = -1)
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted

    val currentStep = BinarySearchTrace.steps.getOrNull(stepIndex)
    val arrayData = currentStep?.values ?: BinarySearchTrace.values
    val isFound = currentStep?.foundIndex != null

    if (!autoExpand) {
        ArrayWindowButton(text = "Open binary search visualizer") { isExpanded = true }
    }

    if (isExpanded) {
        val badgeText = when {
            stepIndex < 0 -> "TARGET: $target (SORTED)"
            isFound -> "FOUND AT INDEX ${currentStep?.foundIndex} ✓"
            else -> "STEP ${stepIndex + 1} OF $totalSteps"
        }

        val badgeColor = when {
            isFound -> GreenAccent
            else -> BlueAccent
        }

        val explanationText = currentStep?.explanation
            ?: "Target = $target (Sorted array). Compare midpoint with target to eliminate half."

        VisualizerModal(
            title = "BINARY SEARCH",
            statusText = badgeText,
            statusColor = badgeColor,
            isCompleted = isCompleted,
            stepIndex = if (stepIndex < 0) 0 else stepIndex,
            stepCount = totalSteps,
            isPlaying = isPlaying,
            layoutMode = VisualizerLayoutMode.FULL_WIDTH,
            onReset = { playback.reset() },
            onPrevious = { playback.previous() },
            onTogglePlayback = { playback.togglePlayback() },
            onNext = { playback.next() },
            onClose = {
                playback.reset()
                isExpanded = false
            },
            visualContent = {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val cellSize = ((maxWidth - 20.dp) / 10).coerceIn(28.dp, 40.dp)
                    val valueFontSize = (cellSize.value * 0.38f).sp
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        arrayData.forEachIndexed { index, value ->
                            val isMid = currentStep?.midIndex == index
                            val isLeft = currentStep?.leftIndex == index
                            val isRight = currentStep?.rightIndex == index
                            val isEliminated = currentStep != null && index !in currentStep.activeRange
                            val isHighlighted = currentStep?.highlightedIndices?.contains(index) == true

                            val pointerLabel = when {
                                isMid -> "M"
                                isLeft && isRight -> "L,R"
                                isLeft -> "L"
                                isRight -> "R"
                                else -> " "
                            }

                            val cellBg = when {
                                isFound && isHighlighted -> Color(0xFF092317)
                                isHighlighted -> Color(0xFF2A1C12)
                                isEliminated -> Color(0xFF0F0F14)
                                else -> Color(0xFF13131C)
                            }

                            val borderColor = when {
                                isFound && isHighlighted -> GreenAccent
                                isHighlighted -> OrangeAccent
                                isEliminated -> Color(0xFF1E1E26)
                                else -> CardBorderColor
                            }

                            val textColor = when {
                                isFound && isHighlighted -> GreenAccent
                                isHighlighted -> OrangeAccent
                                isEliminated -> Color(0xFF4B5563)
                                else -> TextPrimary
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = pointerLabel,
                                    color = if (isMid) OrangeAccent else if (isLeft || isRight) BlueAccent else Color.Transparent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Box(
                                    modifier = Modifier
                                        .size(cellSize)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(cellBg)
                                        .border(
                                            width = if (isHighlighted) 1.5.dp else 1.dp,
                                            color = borderColor,
                                            shape = RoundedCornerShape(10.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = value.toString(),
                                        color = textColor,
                                        fontSize = valueFontSize,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = index.toString(),
                                    color = if (isFound && isHighlighted) GreenAccent else if (isHighlighted) OrangeAccent else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            },
            detailsContent = {
                StepsCard(
                    label = "Binary Search",
                    explanation = explanationText,
                    stepIndex = if (stepIndex < 0) -1 else stepIndex,
                    totalSteps = totalSteps,
                    isCompleted = isCompleted,
                    footer = if (isFound) "Target found at index ${currentStep?.foundIndex}" else null
                )
            }
        )
    }
}

@Composable
internal fun SearchComparisonCard() {
    Surface(
        color = DarkCardBgColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LINEAR VS BINARY SEARCH",
                    color = OrangeAccent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF13233A))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Target: 72 (11 items)",
                        color = BlueAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Comparison Summary Table
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Linear Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF13131C))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Linear Search",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Checks 4 → 12 → 19 → ... → 72",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF2A1C12))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "9 Comparisons  O(n)",
                            color = OrangeAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Binary Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF13131C))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Binary Search",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Checks midpoint 44 → midpoint 72",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF092317))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "2 Comparisons  O(log n)",
                            color = GreenAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
