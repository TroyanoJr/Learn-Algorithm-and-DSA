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

private val SearchCardBg = Color(0xFF181820)
private val SearchCardBorder = Color(0xFF262634)
private val SearchBlue = Color(0xFF38BDF8)
private val SearchGreen = Color(0xFF10B981)
private val SearchRed = Color(0xFFEF4444)

@Composable
internal fun LinearSearchVisualizationCard() {
    val trace = LinearSearchTrace.steps
    val frames = remember(trace) {
        SearchSortSceneBuilder.buildLinearSearchFrames(trace, LinearSearchTrace.values, LinearSearchTrace.target)
    }
    SearchVisualizationModal(
        title = "LINEAR SEARCH",
        launchButtonText = "Open linear search visualizer",
        frames = frames,
        detailsLabel = "Linear Search",
        totalTeachingSteps = LinearSearchTrace.values.size,
        footer = { frame -> "${frame.meta.checkedCount} / ${frame.values.size} CHECKED" },
        viewportKey = "linear_search"
    )
}

@Composable
internal fun BinarySearchVisualizationCard() {
    val trace = BinarySearchTrace.steps
    val frames = remember(trace) {
        SearchSortSceneBuilder.buildBinarySearchFrames(trace, BinarySearchTrace.values, BinarySearchTrace.target)
    }
    SearchVisualizationModal(
        title = "BINARY SEARCH",
        launchButtonText = "Open binary search visualizer",
        frames = frames,
        detailsLabel = "Binary Search",
        totalTeachingSteps = trace.size,
        footer = { frame -> frame.meta.foundIndex?.let { "Target found at index $it" } },
        viewportKey = "binary_search"
    )
}

@Composable
private fun SearchVisualizationModal(
    title: String,
    launchButtonText: String,
    frames: List<SearchSortSceneFrame>,
    detailsLabel: String,
    totalTeachingSteps: Int,
    footer: (SearchSortSceneFrame) -> String?,
    viewportKey: Any
) {
    val autoExpand = LocalAutoExpandVisualizer.current
    var isExpanded by rememberSaveable { mutableStateOf(autoExpand) }
    val playback = rememberVisualizerPlaybackState(stepCount = frames.size, initialStep = 0, key = viewportKey)
    val frame = frames[playback.safeStepIndex]

    if (!autoExpand) ArrayWindowButton(text = launchButtonText) { isExpanded = true }
    if (!isExpanded) return

    val statusColor = when {
        frame.meta.foundIndex != null -> SearchGreen
        frame.meta.notFound -> SearchRed
        else -> SearchBlue
    }
    VisualizerModal(
        title = title,
        statusText = frame.meta.statusText,
        statusColor = statusColor,
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
                label = detailsLabel,
                explanation = frame.meta.explanation,
                stepIndex = SearchSortSceneBuilder.teachingStepIndex(frame.scene.stepIndex, frames.size),
                totalSteps = frames.size,
                isCompleted = frame.meta.isTerminal,
                footer = footer(frame)
            )
        }
    )
}

@Composable
internal fun SearchComparisonCard() {
    LessonComparisonCard(
        title = "LINEAR VS BINARY SEARCH",
        badge = "Target: 72 (11 items)",
        rows = listOf(
            LessonComparisonRow("Linear Search", "Checks 4 → 12 → 19 → ... → 72", "9 Comparisons  O(n)"),
            LessonComparisonRow("Binary Search", "Checks midpoint 44 → midpoint 72", "2 Comparisons  O(log n)", positive = true)
        )
    )
}
