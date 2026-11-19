package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

private val SortingCardBg = Color(0xFF181820)
private val SortingCardBorder = Color(0xFF262634)
private val SortingBlue = Color(0xFF38BDF8)
private val SortingGreen = Color(0xFF10B981)

@Composable
internal fun SortingBarVisualizationCard(
    title: String,
    frames: List<SearchSortSceneFrame>,
    launchButtonText: String,
    viewportKey: Any
) {
    val autoExpand = LocalAutoExpandVisualizer.current
    var isExpanded by rememberSaveable { mutableStateOf(autoExpand) }
    val playback = rememberVisualizerPlaybackState(stepCount = frames.size, initialStep = 0, key = viewportKey)
    val frame = frames[playback.safeStepIndex]

    if (!autoExpand) ArrayWindowButton(text = launchButtonText) { isExpanded = true }
    if (!isExpanded) return

    VisualizerModal(
        title = title,
        statusText = frame.meta.statusText,
        statusColor = if (frame.meta.isTerminal) SortingGreen else SortingBlue,
        isCompleted = frame.meta.isTerminal,
        stepIndex = playback.safeStepIndex,
        stepCount = frames.size,
        isPlaying = playback.isPlaying,
        layoutMode = VisualizerLayoutMode.FULL_WIDTH,
        onReset = { playback.reset() },
        onPrevious = { playback.previous() },
        onTogglePlayback = { playback.togglePlayback() },
        onNext = { playback.next() },
        onClose = {
            playback.reset()
            isExpanded = false
        },
        visualContent = { SearchSortSceneRenderer(frame, frames, Modifier.fillMaxSize(), viewportKey) },
        detailsContent = {
            StepsCard(
                label = "Sorting Step",
                explanation = frame.meta.explanation,
                stepIndex = playback.safeStepIndex,
                totalSteps = frames.size,
                isCompleted = frame.meta.isTerminal
            )
        }
    )
}

@Composable
internal fun BubbleSortVisualizationCard() {
    val frames = remember { SearchSortSceneBuilder.buildBubbleSortFrames(BubbleSortTrace.steps) }
    SortingBarVisualizationCard("BUBBLE SORT VISUALIZATION", frames, "Open bubble sort visualizer", "bubble_sort")
}

@Composable
internal fun SelectionSortVisualizationCard() {
    val frames = remember { SearchSortSceneBuilder.buildSelectionSortFrames(SelectionSortTrace.steps) }
    SortingBarVisualizationCard("SELECTION SORT VISUALIZATION", frames, "Open selection sort visualizer", "selection_sort")
}

@Composable
internal fun InsertionSortVisualizationCard() {
    val frames = remember { SearchSortSceneBuilder.buildInsertionSortFrames(InsertionSortTrace.steps) }
    SortingBarVisualizationCard("INSERTION SORT VISUALIZATION", frames, "Open insertion sort visualizer", "insertion_sort")
}

@Composable
internal fun SortingComparisonCard() {
    LessonComparisonCard(
        title = "SORTING ALGORITHMS COMPARISON",
        rows = listOf(
            LessonComparisonRow("Bubble Sort", "Compare & swap adjacent items", "O(n) Best / O(n²) Worst"),
            LessonComparisonRow("Selection Sort", "Find minimum and swap once per pass", "O(n²) Always"),
            LessonComparisonRow("Insertion Sort", "Shift larger items and insert key", "O(n) Best / O(n²) Worst")
        )
    )
}
