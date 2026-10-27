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
import androidx.compose.foundation.layout.Spacer
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
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9CA3AF)

internal data class SortingStep(
    val array: List<Int>,
    val highlightedIndices: List<Int>,
    val sortedIndices: List<Int>,
    val message: String
)

@Composable
internal fun SortingBarVisualizationCard(
    title: String,
    algorithmBadge: String,
    steps: List<SortingStep>,
    launchButtonText: String
) {
    var stepIndex by remember(steps) { mutableStateOf(0) }
    var isPlaying by remember(steps) { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    val totalSteps = steps.size
    val currentStep = steps.getOrElse(stepIndex) { steps.last() }
    val isFinished = stepIndex >= totalSteps - 1

    LaunchedEffect(isPlaying, stepIndex, steps) {
        if (isPlaying && stepIndex < totalSteps - 1) {
            delay(1000L)
            if (stepIndex < totalSteps - 1) {
                stepIndex++
            } else {
                isPlaying = false
            }
        } else if (stepIndex >= totalSteps - 1) {
            isPlaying = false
        }
    }

    ArrayWindowButton(text = launchButtonText) { isExpanded = true }

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
                val scale = minOf(maxWidth / 800.dp, maxHeight / 320.dp).coerceIn(0.85f, 1f)
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp * scale),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = title,
                                color = OrangeAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                modifier = Modifier.weight(1f)
                            )

                            val badgeText = when {
                                isFinished -> "Sorted ✓"
                                stepIndex == 0 -> algorithmBadge
                                else -> "STEP ${stepIndex + 1} OF $totalSteps"
                            }

                            val badgeColor = when {
                                isFinished -> GreenAccent
                                else -> BlueAccent
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isFinished) Color(0xFF092317) else Color(0xFF13233A))
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

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp * scale),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            currentStep.array.forEachIndexed { index, value ->
                                val isHighlighted = index in currentStep.highlightedIndices
                                val isSorted = index in currentStep.sortedIndices

                                val barColor = when {
                                    isSorted -> GreenAccent
                                    isHighlighted -> OrangeAccent
                                    else -> Color(0xFF262638)
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = value.toString(),
                                        color = if (isHighlighted || isSorted) TextPrimary else TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.7f)
                                            .height((value * 6).dp * scale)
                                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                            .background(barColor)
                                            .border(
                                                width = if (isHighlighted || isSorted) 1.5.dp else 0.dp,
                                                color = if (isSorted) GreenAccent else if (isHighlighted) OrangeAccent else Color.Transparent,
                                                shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                            )
                                    )
                                }
                            }
                        }

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
                                Text(
                                    text = currentStep.message,
                                    color = if (isFinished) GreenAccent else TextPrimary,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(3.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(Color(0xFF262634))
                                    ) {
                                        val progressFloat = if (totalSteps > 1) stepIndex.toFloat() / (totalSteps - 1) else 1f
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(progressFloat.coerceIn(0f, 1f))
                                                .height(3.dp)
                                                .background(if (isFinished) GreenAccent else OrangeAccent, RoundedCornerShape(2.dp))
                                        )
                                    }

                                    Text(
                                        text = "STEP ${stepIndex + 1} / $totalSteps",
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp * scale),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = DarkCardBgColor,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CardBorderColor),
                                modifier = Modifier
                                    .size((44.dp * scale).coerceAtLeast(38.dp))
                                    .clickable {
                                        isPlaying = false
                                        stepIndex = 0
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

                            Surface(
                                color = DarkCardBgColor,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CardBorderColor),
                                modifier = Modifier
                                    .size((44.dp * scale).coerceAtLeast(38.dp))
                                    .clickable(enabled = stepIndex > 0) {
                                        isPlaying = false
                                        if (stepIndex > 0) {
                                            stepIndex--
                                        }
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "‹",
                                        color = if (stepIndex > 0) TextPrimary else TextSecondary.copy(alpha = 0.3f),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Surface(
                                color = if (isFinished) Color(0xFF22222E) else OrangeAccent,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height((44.dp * scale).coerceAtLeast(38.dp))
                                    .clickable(enabled = !isFinished) {
                                        if (!isFinished) {
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

                            Surface(
                                color = DarkCardBgColor,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CardBorderColor),
                                modifier = Modifier
                                    .size((44.dp * scale).coerceAtLeast(38.dp))
                                    .clickable(enabled = !isFinished) {
                                        isPlaying = false
                                        if (stepIndex < totalSteps - 1) {
                                            stepIndex++
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
internal fun BubbleSortVisualizationCard() {
    val steps = BubbleSortTrace.steps.map { step ->
        SortingStep(
            array = step.array,
            highlightedIndices = step.highlightedIndices,
            sortedIndices = step.sortedIndices,
            message = step.message
        )
    }

    SortingBarVisualizationCard(
        title = "BUBBLE SORT VISUALIZATION",
        algorithmBadge = "O(n²) Worst Case",
        steps = steps,
        launchButtonText = "Open bubble sort visualizer"
    )
}

@Composable
internal fun SelectionSortVisualizationCard() {
    val steps = SelectionSortTrace.steps.map { step ->
        SortingStep(
            array = step.array,
            highlightedIndices = step.highlightedIndices,
            sortedIndices = step.sortedIndices,
            message = step.message
        )
    }

    SortingBarVisualizationCard(
        title = "SELECTION SORT VISUALIZATION",
        algorithmBadge = "O(n²) Comparisons",
        steps = steps,
        launchButtonText = "Open selection sort visualizer"
    )
}

@Composable
internal fun InsertionSortVisualizationCard() {
    val steps = InsertionSortTrace.steps.map { step ->
        SortingStep(
            array = step.array,
            highlightedIndices = step.highlightedIndices,
            sortedIndices = step.sortedIndices,
            message = step.message
        )
    }

    SortingBarVisualizationCard(
        title = "INSERTION SORT VISUALIZATION",
        algorithmBadge = "O(n) Best Case",
        steps = steps,
        launchButtonText = "Open insertion sort visualizer"
    )
}

@Composable
internal fun SortingComparisonCard() {
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
            Text(
                text = "SORTING ALGORITHMS COMPARISON",
                color = OrangeAccent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            val rows = listOf(
                Triple("Bubble Sort", "O(n) Best / O(n²) Worst", "Compare & swap adjacent items"),
                Triple("Selection Sort", "O(n²) Always", "Find minimum and swap once per pass"),
                Triple("Insertion Sort", "O(n) Best / O(n²) Worst", "Shift larger items and insert key")
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                rows.forEach { (name, complexity, strategy) ->
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
                                text = name,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = strategy,
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
                                text = complexity,
                                color = OrangeAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
