package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

    var currentIndex by remember { mutableStateOf(-1) }
    var isPlaying by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    val currentStep = traceSteps.getOrNull(currentIndex)
    val arrayData = currentStep?.values ?: LinearSearchTrace.values
    val isFound = currentStep?.result == LinearSearchResult.FOUND
    val isNotFound = currentStep?.result == LinearSearchResult.NOT_FOUND
    val isFinished = currentStep?.isTerminal == true
    val inspectedCount = currentStep?.inspectedCount ?: 0

    // Auto-play timer mechanism
    LaunchedEffect(isPlaying, currentIndex) {
        if (isPlaying && !isFinished) {
            delay(1000L)
            if (currentIndex < 0) {
                currentIndex = 0
            } else if (currentIndex < traceSteps.lastIndex) {
                currentIndex++
            } else {
                isPlaying = false
            }
        } else if (isFinished) {
            isPlaying = false
        }
    }

    ArrayWindowButton(text = "Open linear search visualizer") { isExpanded = true }

    if (isExpanded) {
        Dialog(
            onDismissRequest = {
                isPlaying = false
                isExpanded = false
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(6.dp)) {
                LandscapeOrientationEffect(enabled = true)
                val scale = minOf(maxWidth / 800.dp, maxHeight / 340.dp).coerceIn(0.85f, 1f)
                Surface(
                    color = DarkCardBgColor,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CardBorderColor),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp * scale),
                        verticalArrangement = Arrangement.spacedBy(10.dp * scale)
                    ) {
                        // Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp * scale),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LINEAR SEARCH",
                                color = OrangeAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                modifier = Modifier.weight(1f)
                            )

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

                            val badgeBg = when {
                                isFound -> Color(0xFF092317)
                                isNotFound -> Color(0xFF210C0D)
                                else -> Color(0xFF13233A)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(badgeBg)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    color = badgeColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            ArrayWindowButton(text = "Close", icon = "✕") {
                                isPlaying = false
                                isExpanded = false
                            }
                        }

                        // Array Cells Row (Indices 0..9)
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
                                    verticalArrangement = Arrangement.spacedBy(4.dp * scale)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp * scale)
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
                                            fontSize = 16.sp,
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

                        // Explanation Panel & Track Line
                        Surface(
                            color = Color(0xFF13131C),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, CardBorderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp * scale),
                                verticalArrangement = Arrangement.spacedBy(10.dp * scale)
                            ) {
                                val explanationText = if (currentIndex < 0) {
                                    "Target = $target. Inspect elements sequentially from index 0 until found."
                                } else {
                                    currentStep?.explanation.orEmpty()
                                }

                                Text(
                                    text = explanationText,
                                    color = if (isFound) GreenAccent else TextPrimary,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )

                                // Track & Step Indicator Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp * scale)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(3.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(Color(0xFF262634))
                                    ) {
                                        val progressFloat = if (totalItems == 0) {
                                            if (isFinished) 1f else 0f
                                        } else {
                                            (inspectedCount.toFloat() / totalItems).coerceIn(0f, 1f)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(progressFloat)
                                                .height(3.dp)
                                                .background(if (isFound) GreenAccent else OrangeAccent, RoundedCornerShape(2.dp))
                                        )
                                    }

                                    Text(
                                        text = "$inspectedCount / $totalItems CHECKED",
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }

                        // Controls Row (Reset, Previous, Play/Pause, Next)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp * scale),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Reset (↻)
                            Surface(
                                color = DarkCardBgColor,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CardBorderColor),
                                modifier = Modifier
                                    .size((44.dp * scale).coerceAtLeast(38.dp))
                                    .clickable {
                                        isPlaying = false
                                        currentIndex = -1
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "↻",
                                        color = TextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Previous (‹)
                            Surface(
                                color = DarkCardBgColor,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CardBorderColor),
                                modifier = Modifier
                                    .size((44.dp * scale).coerceAtLeast(38.dp))
                                    .clickable(enabled = currentIndex >= 0) {
                                        isPlaying = false
                                        if (currentIndex > 0) {
                                            currentIndex--
                                        } else {
                                            currentIndex = -1
                                        }
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "‹",
                                        color = if (currentIndex >= 0) TextPrimary else TextSecondary.copy(alpha = 0.3f),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Play / Pause (▶ Play / ⏸ Pause)
                            Surface(
                                color = if (isFinished) Color(0xFF22222E) else OrangeAccent,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height((44.dp * scale).coerceAtLeast(38.dp))
                                    .clickable(enabled = !isFinished) {
                                        if (!isFinished) {
                                            if (currentIndex < 0) {
                                                currentIndex = 0
                                            }
                                            isPlaying = !isPlaying
                                        }
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp * scale)
                                    ) {
                                        Text(
                                            text = if (isPlaying) "⏸" else "▶",
                                            color = if (isFinished) TextSecondary else Color(0xFF111115),
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = if (isPlaying) "Pause" else "Play",
                                            color = if (isFinished) TextSecondary else Color(0xFF111115),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Next (›)
                            Surface(
                                color = DarkCardBgColor,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CardBorderColor),
                                modifier = Modifier
                                    .size((44.dp * scale).coerceAtLeast(38.dp))
                                    .clickable(enabled = !isFinished) {
                                        isPlaying = false
                                        if (currentIndex < 0) {
                                            currentIndex = 0
                                        } else if (currentIndex < traceSteps.lastIndex) {
                                            currentIndex++
                                        }
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "›",
                                        color = if (!isFinished) TextPrimary else TextSecondary.copy(alpha = 0.3f),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
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
internal fun BinarySearchVisualizationCard() {
    val target = BinarySearchTrace.target
    val totalSteps = BinarySearchTrace.steps.size

    var stepIndex by remember { mutableStateOf(-1) }
    var isPlaying by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    val isCompleted = stepIndex >= totalSteps - 1
    val currentStep = BinarySearchTrace.steps.getOrNull(stepIndex)
    val arrayData = currentStep?.values ?: BinarySearchTrace.values
    val isFound = currentStep?.foundIndex != null

    // Auto-play timer mechanism
    LaunchedEffect(isPlaying, stepIndex) {
        if (isPlaying && stepIndex < totalSteps - 1) {
            delay(1000L)
            if (stepIndex < 0) {
                stepIndex = 0
            } else if (stepIndex < totalSteps - 1) {
                stepIndex++
            } else {
                isPlaying = false
            }
        } else if (isCompleted) {
            isPlaying = false
        }
    }

    ArrayWindowButton(text = "Open binary search visualizer") { isExpanded = true }

    if (isExpanded) {
        Dialog(
            onDismissRequest = {
                isPlaying = false
                isExpanded = false
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            LandscapeOrientationEffect(enabled = true)
            BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(6.dp)) {
                val scale = minOf(maxWidth / 800.dp, maxHeight / 360.dp).coerceIn(0.85f, 1f)
                Surface(
                    color = DarkCardBgColor,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CardBorderColor),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp * scale),
                        verticalArrangement = Arrangement.spacedBy(10.dp * scale)
                    ) {
                        // Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp * scale),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BINARY SEARCH",
                                color = OrangeAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                modifier = Modifier.weight(1f)
                            )

                            val badgeText = when {
                                stepIndex < 0 -> "TARGET: $target (SORTED)"
                                isFound -> "FOUND AT INDEX ${currentStep?.foundIndex} ✓"
                                else -> "STEP ${stepIndex + 1} OF $totalSteps"
                            }

                            val badgeColor = when {
                                isFound -> GreenAccent
                                else -> BlueAccent
                            }

                            val badgeBg = when {
                                isFound -> Color(0xFF092317)
                                else -> Color(0xFF13233A)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(badgeBg)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    color = badgeColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            ArrayWindowButton(text = "Close", icon = "✕") {
                                isPlaying = false
                                isExpanded = false
                            }
                        }

                        // Array Cells Row with L, M, R indicators & Index labels
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
                                    verticalArrangement = Arrangement.spacedBy(4.dp * scale)
                                ) {
                                    Text(
                                        text = pointerLabel,
                                        color = if (isMid) OrangeAccent else if (isLeft || isRight) BlueAccent else Color.Transparent,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp * scale)
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
                                            fontSize = 14.sp,
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

                        // Explanation Panel & Track Line
                        Surface(
                            color = Color(0xFF13131C),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, CardBorderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp * scale),
                                verticalArrangement = Arrangement.spacedBy(10.dp * scale)
                            ) {
                                val explanationText = currentStep?.explanation
                                    ?: "Target = $target (Sorted array). Compare midpoint with target to eliminate half."

                                Text(
                                    text = explanationText,
                                    color = if (isFound) GreenAccent else TextPrimary,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )

                                // Track & Step Indicator Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp * scale)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(3.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(Color(0xFF262634))
                                    ) {
                                        val progressFloat = if (stepIndex < 0) 0f else (stepIndex + 1) / totalSteps.toFloat()
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(progressFloat)
                                                .height(3.dp)
                                                .background(if (isFound) GreenAccent else OrangeAccent, RoundedCornerShape(2.dp))
                                        )
                                    }

                                    val stepCountDisplay = if (stepIndex < 0) 0 else stepIndex + 1
                                    Text(
                                        text = "$stepCountDisplay / $totalSteps STEPS",
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }

                        // Controls Row (Reset, Previous, Play/Pause, Next)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp * scale),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Reset (↻)
                            Surface(
                                color = DarkCardBgColor,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CardBorderColor),
                                modifier = Modifier
                                    .size((44.dp * scale).coerceAtLeast(38.dp))
                                    .clickable {
                                        isPlaying = false
                                        stepIndex = -1
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "↻",
                                        color = TextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Previous (‹)
                            Surface(
                                color = DarkCardBgColor,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CardBorderColor),
                                modifier = Modifier
                                    .size((44.dp * scale).coerceAtLeast(38.dp))
                                    .clickable(enabled = stepIndex >= 0) {
                                        isPlaying = false
                                        stepIndex--
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "‹",
                                        color = if (stepIndex >= 0) TextPrimary else TextSecondary.copy(alpha = 0.3f),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Play / Pause (▶ Play / ⏸ Pause)
                            Surface(
                                color = if (isCompleted) Color(0xFF22222E) else OrangeAccent,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height((44.dp * scale).coerceAtLeast(38.dp))
                                    .clickable(enabled = !isCompleted) {
                                        if (!isCompleted) {
                                            if (stepIndex < 0) {
                                                stepIndex = 0
                                            }
                                            isPlaying = !isPlaying
                                        }
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp * scale)
                                    ) {
                                        Text(
                                            text = if (isPlaying) "⏸" else "▶",
                                            color = if (isCompleted) TextSecondary else Color(0xFF111115),
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = if (isPlaying) "Pause" else "Play",
                                            color = if (isCompleted) TextSecondary else Color(0xFF111115),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Next (›)
                            Surface(
                                color = DarkCardBgColor,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CardBorderColor),
                                modifier = Modifier
                                    .size((44.dp * scale).coerceAtLeast(38.dp))
                                    .clickable(enabled = !isCompleted) {
                                        isPlaying = false
                                        if (stepIndex < 0) {
                                            stepIndex = 0
                                        } else if (!isCompleted) {
                                            stepIndex++
                                        }
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "›",
                                        color = if (!isCompleted) TextPrimary else TextSecondary.copy(alpha = 0.3f),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
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
