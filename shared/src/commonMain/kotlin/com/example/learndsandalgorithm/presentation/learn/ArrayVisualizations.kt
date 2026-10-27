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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.learndsandalgorithm.core.platform.LandscapeOrientationEffect
import kotlinx.coroutines.delay

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
    cellHeight: Dp = 48.dp,
    startIndex: Int = 0,
    spacing: Dp = 4.dp
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val cellWidth = (maxWidth - spacing * (values.size - 1).toFloat()) / values.size.toFloat()
        val valueFontSize = minOf(cellWidth.value * 0.4f, cellHeight.value * 0.6f).coerceIn(11f, 22f).sp
        val indexFontSize = (valueFontSize.value * 0.85f).coerceAtLeast(10f).sp
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
                    verticalArrangement = Arrangement.spacedBy(spacing.coerceAtMost(5.dp))
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
    cellHeight: Dp = 40.dp
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "$label · capacity ${slots.size} · size ${slots.count { it != null }}",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        slots.chunked(11).forEachIndexed { rowIndex, rowSlots ->
            if (rowIndex > 0) {
                Text(
                    text = "Continue at index ${rowIndex * 11} ↓",
                    color = BlueAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            ArrayElementRow(
                values = rowSlots,
                highlightedIndices = highlightedIndices,
                startIndex = rowIndex * 11,
                cellHeight = cellHeight,
                spacing = 4.dp
            )
        }
    }
}

@Composable
internal fun ArrayResizeVisualizationCard() {
    var stepIndex by remember { mutableStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    val totalSteps = 4
    val isCompleted = stepIndex >= totalSteps
    val initialValues = listOf<String?>("10", "20", "30", "40", "50", "60", "70", "80", "90", "100")
    val fullArray = initialValues + "110"
    val copiedArray = fullArray + List(11) { null }
    val resizeSteps: List<Pair<String, List<String?>>> = when (stepIndex) {
        0 -> listOf("CURRENT STORAGE" to (initialValues + null))
        1 -> listOf("CAPACITY FULL" to fullArray)
        2 -> listOf("NEW STORAGE" to List(22) { null })
        3 -> listOf("11 VALUES COPIED" to copiedArray)
        else -> listOf(
            "120 APPENDED" to (fullArray + "120" + List(10) { null })
        )
    }

    val explanationText = when (stepIndex) {
        0 -> "Ten values use ten of eleven slots. Append 110 into the free slot at index 10."
        1 -> "All eleven slots are occupied. Appending 120 now requires a larger array."
        2 -> "Allocate new storage with 22 slots. The old eleven-value array is still intact."
        3 -> "Copy each of the eleven existing values to the same index in the new storage."
        else -> "Append 120 at index 11. Size is 12 and capacity is 22."
    }
    val highlightedIndices = when (stepIndex) {
        0, 1 -> setOf(10)
        4 -> setOf(11)
        3 -> (0..10).toSet()
        else -> emptySet()
    }

    LaunchedEffect(isPlaying, stepIndex) {
        if (isPlaying && stepIndex < totalSteps) {
            delay(1000L)
            if (stepIndex < totalSteps) {
                stepIndex++
                if (stepIndex >= totalSteps) isPlaying = false
            }
        }
    }

    ArrayWindowButton(text = "Open resize visualizer") { isExpanded = true }

    if (isExpanded) {
        val (stageLabel, storageSlots) = resizeSteps.single()
        ArrayResizeExpandedDialog(
            stepIndex = stepIndex,
            totalSteps = totalSteps,
            isPlaying = isPlaying,
            isCompleted = isCompleted,
            stageLabel = stageLabel,
            storageSlots = storageSlots,
            preservedStorageSlots = fullArray.takeIf { stepIndex == 2 },
            highlightedIndices = highlightedIndices,
            explanationText = explanationText,
            onReset = {
                isPlaying = false
                stepIndex = 0
            },
            onPrevious = {
                isPlaying = false
                stepIndex = (stepIndex - 1).coerceAtLeast(0)
            },
            onPlayPause = { if (!isCompleted) isPlaying = !isPlaying },
            onNext = {
                isPlaying = false
                stepIndex = (stepIndex + 1).coerceAtMost(totalSteps)
            },
            onClose = {
                isPlaying = false
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
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        LandscapeOrientationEffect(enabled = true)
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(6.dp)) {
            val scale = minOf(maxWidth / 800.dp, maxHeight / 360.dp).coerceIn(0.68f, 1.05f)
            val viewportHeight = maxHeight
            val cellHeight = (viewportHeight * 0.08f).coerceIn(20.dp, 30.dp)
            Surface(
                color = DarkCardBgColor,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CardBorderColor),
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(8.dp * scale),
                    verticalArrangement = Arrangement.spacedBy(6.dp * scale)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "DYNAMIC ARRAY RESIZE",
                                color = OrangeAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = when {
                                    stepIndex == 0 -> "READY"
                                    isCompleted -> "COMPLETED"
                                    else -> "STEP $stepIndex OF $totalSteps"
                                },
                                color = if (isCompleted) GreenAccent else BlueAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        ArrayWindowButton(text = "Close", icon = "✕", onClick = onClose)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp * scale),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.8f)) {
                            if (preservedStorageSlots != null) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp * scale)) {
                                    ArrayStorageRow(
                                        label = "OLD ARRAY (PRESERVED)",
                                        slots = preservedStorageSlots,
                                        cellHeight = cellHeight
                                    )
                                    ArrayStorageRow(
                                        label = stageLabel,
                                        slots = storageSlots,
                                        highlightedIndices = highlightedIndices,
                                        cellHeight = cellHeight
                                    )
                                }
                            } else {
                                ArrayStorageRow(
                                    label = stageLabel,
                                    slots = storageSlots,
                                    highlightedIndices = highlightedIndices,
                                    cellHeight = cellHeight
                                )
                            }
                        }

                        Surface(
                            color = Color(0xFF13131C),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, CardBorderColor),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(9.dp * scale),
                                verticalArrangement = Arrangement.spacedBy(6.dp * scale)
                            ) {
                                Text(
                                    text = "SIZE = values stored\nCAPACITY = allocated slots",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = explanationText,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }

                    ArrayStepControlsRow(
                        isPlaying = isPlaying,
                        isCompleted = isCompleted,
                        canGoPrevious = stepIndex > 0,
                        onReset = onReset,
                        onPrevious = onPrevious,
                        onPlayPause = onPlayPause,
                        onNext = onNext,
                        controlSize = (40.dp * scale).coerceIn(32.dp, 44.dp),
                        spacing = 6.dp * scale
                    )
                }
            }
        }
    }
}

@Composable
internal fun ArrayTraversalVisualizationCard() {
    val arrayData = listOf("10", "25", "42", "67", "91", "116", "135", "158", "176", "204")
    var currentIndex by remember { mutableStateOf(-1) }
    var isPlaying by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    val totalElements = arrayData.size
    val isCompleted = currentIndex >= totalElements
    val visitedCount = if (currentIndex < 0) 0 else (currentIndex + 1).coerceAtMost(totalElements)
    val badgeText = when {
        currentIndex < 0 -> "READY"
        isCompleted -> "COMPLETED"
        else -> "STEP ${currentIndex + 1} OF $totalElements"
    }
    val badgeColor = when {
        currentIndex < 0 -> TextSecondary
        isCompleted -> GreenAccent
        else -> BlueAccent
    }
    val explanationText = when {
        currentIndex < 0 -> "Start at index 0, then visit each element from left to right."
        isCompleted -> "Traversal complete! All $totalElements elements have been visited."
        else -> "Visiting index $currentIndex: value = ${arrayData[currentIndex]}."
    }

    LaunchedEffect(isPlaying, currentIndex) {
        if (isPlaying && currentIndex < totalElements) {
            delay(1000L)
            if (currentIndex < 0) {
                currentIndex = 0
            } else if (currentIndex < totalElements - 1) {
                currentIndex++
            } else {
                currentIndex = totalElements
                isPlaying = false
            }
        }
    }

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
            onReset = {
                isPlaying = false
                currentIndex = -1
            },
            onPrevious = {
                isPlaying = false
                currentIndex = if (currentIndex > 0) currentIndex - 1 else -1
            },
            onPlayPause = {
                if (!isCompleted) {
                    if (currentIndex < 0) currentIndex = 0
                    isPlaying = !isPlaying
                }
            },
            onNext = {
                isPlaying = false
                currentIndex = when {
                    currentIndex < 0 -> 0
                    currentIndex < totalElements - 1 -> currentIndex + 1
                    else -> totalElements
                }
            },
            onClose = {
                isPlaying = false
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
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        LandscapeOrientationEffect(enabled = true)
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(6.dp)) {
            val viewportHeight = maxHeight
            val compact = viewportHeight < 300.dp
            val scale = minOf(maxWidth / 800.dp, viewportHeight / 360.dp).coerceIn(0.68f, 1.05f)
            Surface(
                color = DarkCardBgColor,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CardBorderColor),
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding((if (compact) 8.dp else 11.dp) * scale),
                    verticalArrangement = Arrangement.spacedBy((if (compact) 5.dp else 8.dp) * scale)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "ARRAY TRAVERSAL",
                                color = OrangeAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(text = badgeText, color = badgeColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        ArrayWindowButton(text = "Close", icon = "✕", onClick = onClose)
                    }

                    Text(
                        text = "Each index labels its value; orange marks the current visit.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )

                    ArrayElementRow(
                        values = arrayData,
                        highlightedIndices = if (currentIndex in arrayData.indices) setOf(currentIndex) else emptySet(),
                        cellHeight = (viewportHeight * (if (compact) 0.15f else 0.18f)).coerceIn(
                            if (compact) 32.dp else 40.dp,
                            if (compact) 48.dp else 60.dp
                        ),
                        spacing = 6.dp
                    )

                    Surface(
                        color = Color(0xFF13131C),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CardBorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding((if (compact) 6.dp else 8.dp) * scale),
                            verticalArrangement = Arrangement.spacedBy((if (compact) 5.dp else 7.dp) * scale)
                        ) {
                            Text(explanationText, color = TextPrimary, fontSize = 12.sp, lineHeight = 17.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp * scale),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color(0xFF262634))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth((visitedCount.toFloat() / totalElements).coerceIn(0f, 1f))
                                            .height(3.dp)
                                            .background(OrangeAccent, RoundedCornerShape(2.dp))
                                    )
                                }
                                Text(
                                    "$visitedCount / $totalElements VISITED",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    ArrayStepControlsRow(
                        isPlaying = isPlaying,
                        isCompleted = isCompleted,
                        canGoPrevious = currentIndex >= 0,
                        onReset = onReset,
                        onPrevious = onPrevious,
                        onPlayPause = onPlayPause,
                        onNext = onNext,
                        controlSize = (40.dp * scale).coerceIn(32.dp, 44.dp),
                        spacing = 6.dp * scale
                    )
                }
            }
        }
    }
}

@Composable
internal fun ArrayInsertDeleteVisualizationCard() {
    var operationMode by remember { mutableStateOf("INSERT") }
    var stepIndex by remember { mutableStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }
    val baseArray = listOf("10", "20", "30", "40", "50", "60", "70", "80", "90", "100")
    val insertionStates = remember {
        val states = mutableListOf(baseArray)
        var working = (baseArray + "  ").toMutableList()
        states.add(working.toList())
        for (sourceIndex in baseArray.lastIndex downTo 2) {
            working = working.toMutableList().apply {
                this[sourceIndex + 1] = this[sourceIndex]
                this[sourceIndex] = "  "
            }
            states.add(working.toList())
        }
        states.add(working.toMutableList().apply { this[2] = "25" })
        states
    }
    val deletionStates = remember {
        val states = mutableListOf(baseArray)
        var working = baseArray.toMutableList().apply { this[1] = "  " }
        states.add(working.toList())
        for (gapIndex in 1 until baseArray.lastIndex) {
            working = working.toMutableList().apply {
                this[gapIndex] = this[gapIndex + 1]
                this[gapIndex + 1] = "  "
            }
            states.add(working.toList())
        }
        states.add(working.dropLast(1))
        states
    }
    val operationStates = if (operationMode == "INSERT") insertionStates else deletionStates
    val totalSteps = operationStates.lastIndex
    val isCompleted = stepIndex >= totalSteps

    LaunchedEffect(isPlaying, stepIndex) {
        if (isPlaying && stepIndex < totalSteps) {
            delay(1000L)
            if (stepIndex < totalSteps - 1) {
                stepIndex++
            } else {
                stepIndex = totalSteps
                isPlaying = false
            }
        }
    }

    val displayItems = operationStates[stepIndex.coerceIn(0, operationStates.lastIndex)]

    val explanationText = when (operationMode) {
        "INSERT" -> when (stepIndex) {
            0 -> "Insert 25 at index 2 in the 10-value array. One free capacity slot is available at index 10."
            1 -> "Create the free slot at index 10. Size remains 10; no values have moved yet."
            in 2..9 -> {
                val sourceIndex = 11 - stepIndex
                "Shift ${baseArray[sourceIndex]} from index $sourceIndex to ${sourceIndex + 1}; the gap moves to index $sourceIndex."
            }
            else -> "Place 25 in the gap at index 2. The resulting array contains 11 elements."
        }
        else -> when (stepIndex) {
            0 -> "Delete 20 at index 1. The following values will shift left."
            1 -> "Remove 20 and leave a temporary gap at index 1."
            in 2..9 -> {
                val sourceIndex = stepIndex
                "Shift ${baseArray[sourceIndex]} from index $sourceIndex to ${sourceIndex - 1}; the gap moves to index $sourceIndex."
            }
            else -> "Drop the unused trailing slot. The resulting array contains 9 elements."
        }
    }

    val highlightedIndices = when (operationMode) {
        "INSERT" -> when (stepIndex) {
            0 -> setOf(2)
            1 -> setOf(10)
            in 2..9 -> {
                val sourceIndex = 11 - stepIndex
                setOf(sourceIndex, sourceIndex + 1)
            }
            else -> setOf(2)
        }
        else -> when (stepIndex) {
            0, 1 -> setOf(1)
            in 2..9 -> setOf(stepIndex - 1, stepIndex)
            else -> setOf(8)
        }
    }

    val badgeText = when (stepIndex) {
        0 -> "READY"
        totalSteps -> "COMPLETED"
        else -> "STEP $stepIndex OF $totalSteps"
    }

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
                isPlaying = false
                operationMode = newMode
                stepIndex = 0
            },
            onReset = {
                isPlaying = false
                stepIndex = 0
            },
            onPrevious = {
                isPlaying = false
                stepIndex = (stepIndex - 1).coerceAtLeast(0)
            },
            onPlayPause = {
                if (!isCompleted) isPlaying = !isPlaying
            },
            onNext = {
                isPlaying = false
                stepIndex = (stepIndex + 1).coerceAtMost(totalSteps)
            },
            onClose = {
                isPlaying = false
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
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        LandscapeOrientationEffect(enabled = true)
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(6.dp)) {
            val viewportHeight = maxHeight
            val compact = viewportHeight < 300.dp
            val scale = minOf(maxWidth / 800.dp, viewportHeight / 360.dp).coerceIn(0.68f, 1.05f)
            Surface(
                color = DarkCardBgColor,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CardBorderColor),
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding((if (compact) 8.dp else 11.dp) * scale),
                    verticalArrangement = Arrangement.spacedBy((if (compact) 5.dp else 7.dp) * scale)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp * scale),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "INSERT & DELETE",
                                color = OrangeAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = badgeText,
                                color = badgeColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp * scale)) {
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
                                        modifier = Modifier.padding(
                                            horizontal = 9.dp * scale,
                                            vertical = 6.dp * scale
                                        )
                                    )
                                }
                            }
                        }
                        Surface(
                            color = Color(0xFF22222E),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, CardBorderColor),
                            modifier = Modifier.clickable(onClick = onClose)
                        ) {
                            Text(
                                text = "Close  ✕",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp * scale, vertical = 6.dp * scale)
                            )
                        }
                    }

                    Text(
                        text = "Orange cells mark the position shifting in this step.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )

                    ArrayElementRow(
                        values = displayItems,
                        highlightedIndices = highlightedIndices,
                        cellHeight = (viewportHeight * (if (compact) 0.12f else 0.13f)).coerceIn(
                            if (compact) 30.dp else 34.dp,
                            if (compact) 44.dp else 50.dp
                        ),
                        spacing = 4.dp * scale
                    )

                    Surface(
                        color = Color(0xFF13131C),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CardBorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding((if (compact) 6.dp else 8.dp) * scale),
                            verticalArrangement = Arrangement.spacedBy((if (compact) 4.dp else 6.dp) * scale)
                        ) {
                            Text(
                                text = explanationText,
                                color = TextPrimary,
                                fontSize = if (compact) 12.sp else 13.sp,
                                lineHeight = if (compact) 16.sp else 18.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp * scale),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color(0xFF262634))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth((stepIndex.toFloat() / totalSteps).coerceIn(0f, 1f))
                                            .height(3.dp)
                                            .background(OrangeAccent, RoundedCornerShape(2.dp))
                                    )
                                }
                            }
                        }
                    }

                    ArrayStepControlsRow(
                        isPlaying = isPlaying,
                        isCompleted = isCompleted,
                        canGoPrevious = stepIndex > 0,
                        onReset = onReset,
                        onPrevious = onPrevious,
                        onPlayPause = onPlayPause,
                        onNext = onNext,
                        controlSize = (40.dp * scale).coerceIn(32.dp, 44.dp),
                        spacing = 6.dp * scale
                    )
                }
            }
        }
    }
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
