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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.learndsandalgorithm.core.platform.LandscapeOrientationEffect

private val DarkBgColor = Color(0xFF111115)
private val DarkCardBgColor = Color(0xFF181820)
private val CardBorderColor = Color(0xFF262634)
private val OrangeAccent = Color(0xFFFA6E13)
private val BlueAccent = Color(0xFF38BDF8)
private val GreenAccent = Color(0xFF10B981)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9CA3AF)

@Composable
internal fun ArrayVisualizationCard() {
    val arrayItems = listOf(
        "10", "20", "30", "40", "50", "60", "70", "80", "90", "100"
    )

    Surface(
        color = DarkCardBgColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "ARRAY INDEX EXAMPLE",
                color = OrangeAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Each index labels the value in its cell. Index 2 contains 30, so numbers[2] returns 30 directly in O(1) time.",
                color = TextPrimary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
            ArrayElementRow(
                values = arrayItems,
                highlightedIndices = setOf(2),
                cellHeight = 44.dp,
                spacing = 6.dp
            )
        }
    }
}

@Composable
internal fun ArrayElementRow(
    values: List<String?>,
    highlightedIndices: Set<Int> = emptySet(),
    cellHeight: Dp = 36.dp,
    startIndex: Int = 0,
    spacing: Dp = 14.dp
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val cellWidth = (maxWidth - spacing * (values.size - 1).toFloat()) / values.size.toFloat()
        val valueFontSize = 15.sp
        val indexFontSize = 11.sp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            values.forEachIndexed { offset, value ->
                val index = startIndex + offset
                val isHighlighted = index in highlightedIndices
                val isEmpty = value.isNullOrBlank()
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(cellHeight)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    isHighlighted -> Color(0xFF2A1C12)
                                    isEmpty -> Color(0xFF101015)
                                    else -> Color(0xFF13131C)
                                }
                            )
                            .border(
                                width = if (isHighlighted) 2.dp else 1.dp,
                                color = if (isHighlighted) OrangeAccent else CardBorderColor,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            value?.takeUnless { it.isBlank() } ?: "·",
                            color = when {
                                isHighlighted -> OrangeAccent
                                isEmpty -> TextSecondary
                                else -> TextPrimary
                            },
                            fontSize = valueFontSize,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    Text(
                        index.toString(),
                        color = if (isHighlighted) OrangeAccent else TextSecondary,
                        fontSize = indexFontSize,
                        fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
internal fun ArrayStorageRow(
    label: String,
    slots: List<String?>,
    highlightedIndices: Set<Int> = emptySet(),
    cellHeight: Dp = 36.dp
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "$label · capacity ${slots.size} · size ${slots.count { it != null }}",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        val chunkSize = when {
            slots.size <= 8 -> 8
            slots.size <= 16 -> 8
            else -> 10
        }

        slots.chunked(chunkSize).forEachIndexed { rowIndex, rowSlots ->
            val startIdx = rowIndex * chunkSize
            if (rowIndex > 0) {
                Text(
                    text = "Continue at index $startIdx ↓",
                    color = BlueAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            ArrayElementRow(
                values = rowSlots,
                highlightedIndices = highlightedIndices,
                startIndex = startIdx,
                cellHeight = cellHeight,
                spacing = 4.dp
            )
        }
    }
}

@Composable
internal fun ArrayResizeVisualizationCard() {
    var isExpanded by remember { mutableStateOf(false) }

    val steps = remember { ArrayTrace.resizeSteps() }
    val totalSteps = 4
    val playback = rememberVisualizerPlaybackState(stepCount = steps.size, initialStep = 0)
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted

    val currentStep = steps[stepIndex.coerceIn(0, steps.lastIndex)]
    val stageLabel = currentStep.stageLabel
    val storageSlots = currentStep.storageSlots
    val preservedStorageSlots = currentStep.preservedStorageSlots
    val highlightedIndices = currentStep.highlightedIndices
    val explanationText = currentStep.explanationText

    ArrayWindowButton(text = "Open resize visualizer") { isExpanded = true }

    if (isExpanded) {
        ArrayResizeExpandedDialog(
            stepIndex = stepIndex,
            totalSteps = totalSteps,
            isPlaying = isPlaying,
            isCompleted = isCompleted,
            stageLabel = stageLabel,
            storageSlots = storageSlots,
            preservedStorageSlots = preservedStorageSlots,
            highlightedIndices = highlightedIndices,
            explanationText = explanationText,
            onReset = { playback.reset() },
            onPrevious = { playback.previous() },
            onPlayPause = { playback.togglePlayback() },
            onNext = { playback.next() },
            onClose = {
                playback.reset()
                isExpanded = false
            }
        )
    }
}

@Composable
internal fun ArrayResizeExpandedDialog(
    stepIndex: Int,
    totalSteps: Int,
    isPlaying: Boolean,
    isCompleted: Boolean,
    stageLabel: String,
    storageSlots: List<String?>,
    preservedStorageSlots: List<String?>?,
    highlightedIndices: Set<Int>,
    explanationText: String,
    onReset: () -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit
) {
    val statusText = when {
        stepIndex == 0 -> "READY"
        isCompleted -> "COMPLETED"
        else -> "STEP $stepIndex OF $totalSteps"
     }

    VisualizerModal(
        title = "DYNAMIC ARRAY RESIZE",
        statusText = statusText,
        isCompleted = isCompleted,
        stepIndex = stepIndex,
        stepCount = totalSteps,
        isPlaying = isPlaying,
        layoutMode = VisualizerLayoutMode.FULL_WIDTH,
        fitMaxScale = 1.0f,
        fitFillFraction = 0.85f,
        onReset = onReset,
        onPrevious = onPrevious,
        onTogglePlayback = onPlayPause,
        onNext = onNext,
        onClose = onClose,
        visualContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (preservedStorageSlots != null) {
                    ArrayStorageRow(
                        label = "OLD ARRAY (PRESERVED)",
                        slots = preservedStorageSlots,
                        cellHeight = 36.dp
                    )
                    ArrayStorageRow(
                        label = stageLabel,
                        slots = storageSlots,
                        highlightedIndices = highlightedIndices,
                        cellHeight = 36.dp
                    )
                } else {
                    ArrayStorageRow(
                        label = stageLabel,
                        slots = storageSlots,
                        highlightedIndices = highlightedIndices,
                        cellHeight = 36.dp
                    )
                }
            }
        },
        detailsContent = {
            StepsCard(
                label = "Size vs Capacity",
                explanation = explanationText,
                stepIndex = stepIndex,
                totalSteps = totalSteps,
                isCompleted = isCompleted,
                footer = "SIZE = values stored | CAPACITY = allocated slots"
            )
        }
    )
}

@Composable
internal fun ArrayTraversalVisualizationCard() {
    var isExpanded by remember { mutableStateOf(false) }

    val steps = remember { ArrayTrace.traversalSteps() }
    val totalElements = ArrayTrace.defaultTraversalData.size
    val playback = rememberVisualizerPlaybackState(stepCount = steps.size, initialStep = -1)
    val currentIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted

    val stepIdx = (currentIndex + 1).coerceIn(0, steps.lastIndex)
    val currentStep = steps[stepIdx]
    val arrayData = currentStep.arrayData
    val visitedCount = currentStep.visitedCount
    val badgeText = currentStep.badgeText
    val badgeColor = when {
        currentIndex < 0 -> TextSecondary
        isCompleted -> GreenAccent
        else -> BlueAccent
    }
    val explanationText = currentStep.explanationText

    ArrayWindowButton(text = "Open traversal visualizer") { isExpanded = true }

    if (isExpanded) {
        ArrayTraversalExpandedDialog(
            arrayData = arrayData,
            currentIndex = currentIndex,
            isPlaying = isPlaying,
            isCompleted = isCompleted,
            totalElements = totalElements,
            visitedCount = visitedCount,
            badgeText = badgeText,
            badgeColor = badgeColor,
            explanationText = explanationText,
            onReset = { playback.reset() },
            onPrevious = { playback.previous() },
            onPlayPause = { playback.togglePlayback() },
            onNext = { playback.next() },
            onClose = {
                playback.reset()
                isExpanded = false
            }
        )
    }
}

@Composable
internal fun ArrayTraversalExpandedDialog(
    arrayData: List<String>,
    currentIndex: Int,
    isPlaying: Boolean,
    isCompleted: Boolean,
    totalElements: Int,
    visitedCount: Int,
    badgeText: String,
    badgeColor: Color,
    explanationText: String,
    onReset: () -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit
) {
    VisualizerModal(
        title = "ARRAY TRAVERSAL",
        statusText = badgeText,
        statusColor = badgeColor,
        isCompleted = isCompleted,
        stepIndex = if (currentIndex < 0) 0 else currentIndex + 1,
        stepCount = totalElements + 1,
        isPlaying = isPlaying,
        layoutMode = VisualizerLayoutMode.FULL_WIDTH,
        fitMaxScale = 1.0f,
        fitFillFraction = 0.85f,
        onReset = onReset,
        onPrevious = onPrevious,
        onTogglePlayback = onPlayPause,
        onNext = onNext,
        onClose = onClose,
        visualContent = {
            ArrayElementRow(
                values = arrayData,
                highlightedIndices = if (currentIndex in arrayData.indices) setOf(currentIndex) else emptySet(),
                cellHeight = 36.dp,
                spacing = 14.dp
            )
        },
        detailsContent = {
            StepsCard(
                label = "Traversal Status",
                explanation = explanationText,
                stepIndex = if (currentIndex < 0) -1 else currentIndex,
                totalSteps = totalElements,
                isCompleted = isCompleted,
                footer = "$visitedCount / $totalElements VISITED"
            )
        }
    )
}

@Composable
internal fun ArrayInsertDeleteVisualizationCard() {
    var operationMode by remember { mutableStateOf("INSERT") }
    var isExpanded by remember { mutableStateOf(false) }

    val steps = remember(operationMode) { ArrayTrace.insertDeleteSteps(operationMode) }
    val totalSteps = steps.lastIndex

    val playback = rememberVisualizerPlaybackState(stepCount = steps.size, initialStep = 0, key = operationMode)
    val stepIndex = playback.safeStepIndex
    val isPlaying = playback.isPlaying
    val isCompleted = playback.isCompleted

    val currentStep = steps[stepIndex.coerceIn(0, steps.lastIndex)]
    val displayItems = currentStep.displayItems
    val highlightedIndices = currentStep.highlightedIndices
    val explanationText = currentStep.explanationText
    val badgeText = currentStep.badgeText
    val badgeColor = when (stepIndex) {
        0 -> TextSecondary
        totalSteps -> GreenAccent
        else -> BlueAccent
    }

    ArrayWindowButton(text = "Open insert / delete visualizer") { isExpanded = true }

    if (isExpanded) {
        ArrayInsertDeleteExpandedDialog(
            operationMode = operationMode,
            stepIndex = stepIndex,
            totalSteps = totalSteps,
            isPlaying = isPlaying,
            isCompleted = isCompleted,
            displayItems = displayItems,
            highlightedIndices = highlightedIndices,
            explanationText = explanationText,
            badgeText = badgeText,
            badgeColor = badgeColor,
            onOperationModeChange = { newMode ->
                operationMode = newMode
                playback.reset()
            },
            onReset = { playback.reset() },
            onPrevious = { playback.previous() },
            onPlayPause = { playback.togglePlayback() },
            onNext = { playback.next() },
            onClose = {
                playback.reset()
                isExpanded = false
            }
        )
    }
}

@Composable
internal fun ArrayInsertDeleteExpandedDialog(
    operationMode: String,
    stepIndex: Int,
    totalSteps: Int,
    isPlaying: Boolean,
    isCompleted: Boolean,
    displayItems: List<String>,
    highlightedIndices: Set<Int>,
    explanationText: String,
    badgeText: String,
    badgeColor: Color,
    onOperationModeChange: (String) -> Unit,
    onReset: () -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit
) {
    VisualizerModal(
        title = "INSERT & DELETE",
        statusText = badgeText,
        statusColor = badgeColor,
        isCompleted = isCompleted,
        stepIndex = stepIndex,
        stepCount = totalSteps,
        isPlaying = isPlaying,
        layoutMode = VisualizerLayoutMode.FULL_WIDTH,
        fitMaxScale = 1.0f,
        fitFillFraction = 0.85f,
        onReset = onReset,
        onPrevious = onPrevious,
        onTogglePlayback = onPlayPause,
        onNext = onNext,
        onClose = onClose,
        topContent = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("INSERT", "DELETE").forEach { mode ->
                    val isSelected = operationMode == mode
                    Surface(
                        color = if (isSelected) OrangeAccent else Color(0xFF22222E),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { onOperationModeChange(mode) }
                    ) {
                        Text(
                            text = mode.lowercase().replaceFirstChar { it.uppercase() },
                            color = if (isSelected) DarkBgColor else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        },
        visualContent = {
            ArrayElementRow(
                values = displayItems,
                highlightedIndices = highlightedIndices,
                cellHeight = 36.dp,
                spacing = 14.dp
            )
        },
        detailsContent = {
            StepsCard(
                label = if (operationMode == "INSERT") "Insertion Step" else "Deletion Step",
                explanation = explanationText,
                stepIndex = stepIndex,
                totalSteps = totalSteps,
                isCompleted = isCompleted
            )
        }
    )
}

@Composable
internal fun ArrayComplexityComparisonCard() {
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
                text = "ARRAY COMPLEXITY SUMMARY",
                color = OrangeAccent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            val complexityRows = listOf(
                Triple("Access by Index", "O(1)", "Constant time via direct memory offset"),
                Triple("Sequential Traversal", "O(n)", "Linear time to inspect all n elements"),
                Triple("Middle Insertion", "O(n)", "Linear time due to element shifting right"),
                Triple("Middle Deletion", "O(n)", "Linear time due to element shifting left")
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                complexityRows.forEach { (operation, complexity, note) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF13131C))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = operation,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = note,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (complexity == "O(1)") Color(0xFF092317) else Color(0xFF2A1C12))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = complexity,
                                color = if (complexity == "O(1)") GreenAccent else OrangeAccent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
