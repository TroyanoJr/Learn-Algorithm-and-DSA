package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val playback = rememberVisualizerPlaybackState(stepCount = steps.size, initialStep = 0, key = steps)
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isFinished = playback.isCompleted

    val totalSteps = steps.size
    val currentStep = steps.getOrElse(stepIndex) { steps.last() }

    ArrayWindowButton(text = launchButtonText) { isExpanded = true }

    if (isExpanded) {
        val badgeText = when {
            isFinished -> "Sorted ✓"
            stepIndex == 0 -> algorithmBadge
            else -> "STEP ${stepIndex + 1} OF $totalSteps"
        }

        val badgeColor = when {
            isFinished -> GreenAccent
            else -> BlueAccent
        }

        VisualizerModal(
            title = title,
            statusText = badgeText,
            statusColor = badgeColor,
            isCompleted = isFinished,
            stepIndex = stepIndex,
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
                val maxVal = currentStep.array.maxOrNull()?.coerceAtLeast(1) ?: 1
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
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
                        val barHeight = (65.dp * (value.toFloat() / maxVal)).coerceAtLeast(6.dp)

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
                                    .height(barHeight)
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
            },
            detailsContent = {
                StepsCard(
                    label = "Sorting Step",
                    explanation = currentStep.message,
                    stepIndex = stepIndex,
                    totalSteps = totalSteps,
                    isCompleted = isFinished
                )
            }
        )
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
