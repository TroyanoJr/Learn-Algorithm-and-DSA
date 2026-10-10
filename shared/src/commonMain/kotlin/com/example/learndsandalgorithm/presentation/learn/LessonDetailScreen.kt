package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learndsandalgorithm.presentation.components.MarkdownContent
import kotlinx.coroutines.delay

private val DarkBgColor = Color(0xFF111115)
private val DarkCardBgColor = Color(0xFF181820)
private val CardBorderColor = Color(0xFF262634)
private val OrangeAccent = Color(0xFFFA6E13)
private val BlueAccent = Color(0xFF38BDF8)
private val GreenAccent = Color(0xFF10B981)
private val RedAccent = Color(0xFFEF4444)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9CA3AF)

private data class OptionStyle(
    val borderColor: Color,
    val bgColor: Color,
    val textColor: Color,
    val iconStr: String?
)

private data class SortingStep(
    val array: List<Int>,
    val highlightedIndices: List<Int>,
    val sortedIndices: List<Int>,
    val message: String
)

@Composable
fun LessonDetailScreen(
    lessonId: String,
    viewModel: LessonViewModel,
    onBack: () -> Unit,
    onNavigateToLesson: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedQuestionOption by remember { mutableStateOf<Int?>(null) }
    var isLessonCompleted by remember { mutableStateOf(false) }

    LaunchedEffect(lessonId) {
        viewModel.loadLesson(lessonId)
        selectedQuestionOption = null
    }

    LaunchedEffect(uiState) {
        val state = uiState
        if (state is LessonUiState.Success) {
            isLessonCompleted = state.isCompleted
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBgColor)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Back Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DarkCardBgColor)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "←",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = "LESSON",
                        color = OrangeAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = (uiState as? LessonUiState.Success)?.lesson?.title ?: "Lesson Detail",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // XP and Duration Badges
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF22222E))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "4 min",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF2A1C12))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    val xp = (uiState as? LessonUiState.Success)?.lesson?.xp ?: 30
                    Text(
                        text = "$xp XP",
                        color = OrangeAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        when (val state = uiState) {
            is LessonUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = OrangeAccent)
                }
            }

            is LessonUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Error Loading Lesson",
                            color = OrangeAccent,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = state.message,
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                        Surface(
                            color = OrangeAccent,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable { viewModel.loadLesson(lessonId) }
                        ) {
                            Text(
                                text = "Retry",
                                color = DarkBgColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            is LessonUiState.Success -> {
                var showCelebrationOverlay by remember { mutableStateOf(false) }
                val scrollState = rememberScrollState()

                LaunchedEffect(lessonId) {
                    scrollState.scrollTo(0)
                    showCelebrationOverlay = false
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Theory & Key Concepts Markdown Content
                        MarkdownContent(content = state.markdownContent)

                        // Lesson Specific Visualizations
                        when (state.lesson.id) {
                            "lesson_arrays_1" -> ArrayVisualizationCard()
                            "lesson_arrays_2" -> ArrayInsertDeleteVisualizationCard()
                            "lesson_arrays_3" -> ArrayTraversalVisualizationCard()
                            "lesson_arrays_4" -> ArrayInsertDeleteVisualizationCard()
                            "lesson_arrays_5" -> ArrayComplexityComparisonCard()
                            "searching_2" -> LinearSearchVisualizationCard()
                            "searching_3" -> BinarySearchVisualizationCard()
                            "searching_4" -> SearchComparisonCard()
                            "sorting_2" -> BubbleSortVisualizationCard()
                            "sorting_3" -> SelectionSortVisualizationCard()
                            "sorting_4" -> InsertionSortVisualizationCard()
                            "sorting_5" -> SortingComparisonCard()
                            else -> {
                                if (state.lesson.topicId == "arrays") {
                                    ArrayVisualizationCard()
                                }
                            }
                        }

                        // Check Your Understanding Quiz Section
                        if (state.questions.isNotEmpty()) {
                            val question = state.questions.first()
                            CheckYourUnderstandingSection(
                                statement = question.statement,
                                options = question.options,
                                correctIndex = question.correctOptionIndex,
                                explanation = question.explanation,
                                selectedOption = selectedQuestionOption,
                                onSelectOption = { selectedQuestionOption = it }
                            )
                        }

                        // Finish Lesson Section with Next Lesson capability
                        FinishLessonSection(
                            isCompleted = isLessonCompleted,
                            nextLessonId = state.nextLessonId,
                            onComplete = {
                                viewModel.markLessonCompleted()
                                isLessonCompleted = true
                                showCelebrationOverlay = true
                            },
                            onNavigateToNextLesson = onNavigateToLesson,
                            onBackToTopic = onBack
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Full-Screen Celebration Overlay (Figma Reference)
                    if (showCelebrationOverlay) {
                        LessonCompletionCelebrationOverlay(
                            lessonTitle = state.lesson.title,
                            xpAmount = state.lesson.xp,
                            nextLessonId = state.nextLessonId,
                            onContinue = {
                                showCelebrationOverlay = false
                                if (state.nextLessonId != null) {
                                    onNavigateToLesson(state.nextLessonId)
                                } else {
                                    onBack()
                                }
                            },
                            onReview = {
                                showCelebrationOverlay = false
                            }
                        )
                    }
                }
            }
        }
    }
}

// --- SHARED BAR VISUALIZATION RENDERER FOR SORTING ALGORITHMS ---
@Composable
private fun SortingBarVisualizationCard(
    title: String,
    algorithmBadge: String,
    steps: List<SortingStep>
) {
    var stepIndex by remember { mutableStateOf(0) }
    val currentStep = steps.getOrElse(stepIndex) { steps.last() }
    val isFinished = stepIndex >= steps.size - 1

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
                    text = title,
                    color = OrangeAccent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isFinished) Color(0xFF092317) else Color(0xFF13233A))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isFinished) "Sorted ✓" else algorithmBadge,
                        color = if (isFinished) GreenAccent else BlueAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Shared Bars & Array Elements Row
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
                                .height((value * 8).dp)
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

            // Status Text and Next Step Control
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentStep.message,
                    color = if (isFinished) GreenAccent else TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = if (isFinished) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.weight(1f, fill = false),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Surface(
                    color = if (isFinished) Color(0xFF22222E) else OrangeAccent,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable {
                        if (isFinished) {
                            stepIndex = 0
                        } else {
                            stepIndex++
                        }
                    }
                ) {
                    Text(
                        text = if (isFinished) "Restart ↻" else "Next Step →",
                        color = if (isFinished) TextPrimary else DarkBgColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

// --- SPECIFIC SORTING ALGORITHM STEP GENERATORS USING SHARED RENDERER ---

@Composable
private fun BubbleSortVisualizationCard() {
    val steps = listOf(
        SortingStep(listOf(5, 3, 8, 4, 2), listOf(0, 1), emptyList(), "Compare 5 & 3 → 5 > 3, swap needed"),
        SortingStep(listOf(3, 5, 8, 4, 2), listOf(1, 2), emptyList(), "Compare 5 & 8 → 5 < 8, no swap"),
        SortingStep(listOf(3, 5, 8, 4, 2), listOf(2, 3), emptyList(), "Compare 8 & 4 → 8 > 4, swap needed"),
        SortingStep(listOf(3, 5, 4, 8, 2), listOf(3, 4), emptyList(), "Compare 8 & 2 → 8 > 2, swap needed"),
        SortingStep(listOf(3, 5, 4, 2, 8), emptyList(), listOf(4), "8 bubbled to the end! Pass 1 complete"),
        SortingStep(listOf(2, 3, 4, 5, 8), emptyList(), listOf(0, 1, 2, 3, 4), "Array is fully sorted! ✓")
    )

    SortingBarVisualizationCard(
        title = "BUBBLE SORT VISUALIZATION",
        algorithmBadge = "O(n²) Worst Case",
        steps = steps
    )
}

@Composable
private fun SelectionSortVisualizationCard() {
    val steps = listOf(
        SortingStep(listOf(5, 3, 8, 4, 2), listOf(4), emptyList(), "Scan unsorted region → Minimum is 2 at index 4"),
        SortingStep(listOf(2, 3, 8, 4, 5), emptyList(), listOf(0), "Swap 2 into index 0 → Sorted: [2]"),
        SortingStep(listOf(2, 3, 8, 4, 5), listOf(1), listOf(0, 1), "Minimum in remaining is 3 → Already in place"),
        SortingStep(listOf(2, 3, 4, 8, 5), emptyList(), listOf(0, 1, 2), "Swap 4 into index 2 → Sorted: [2, 3, 4]"),
        SortingStep(listOf(2, 3, 4, 5, 8), emptyList(), listOf(0, 1, 2, 3, 4), "Swap 5 into index 3 → Array is fully sorted! ✓")
    )

    SortingBarVisualizationCard(
        title = "SELECTION SORT VISUALIZATION",
        algorithmBadge = "O(n²) Comparisons",
        steps = steps
    )
}

@Composable
private fun InsertionSortVisualizationCard() {
    val steps = listOf(
        SortingStep(listOf(5, 3, 8, 4, 2), listOf(1), listOf(0), "Take key 3 → Compare with 5 in sorted region"),
        SortingStep(listOf(3, 5, 8, 4, 2), emptyList(), listOf(0, 1), "Shift 5 right & insert 3 → Sorted: [3, 5]"),
        SortingStep(listOf(3, 5, 8, 4, 2), listOf(2), listOf(0, 1, 2), "Take key 8 → 8 > 5, already in place"),
        SortingStep(listOf(3, 5, 4, 8, 2), listOf(3), listOf(0, 1, 2), "Take key 4 → Shift 8 right, shift 5 right"),
        SortingStep(listOf(3, 4, 5, 8, 2), emptyList(), listOf(0, 1, 2, 3), "Insert 4 at index 1 → Sorted: [3, 4, 5, 8]"),
        SortingStep(listOf(2, 3, 4, 5, 8), emptyList(), listOf(0, 1, 2, 3, 4), "Insert 2 at index 0 → Array is fully sorted! ✓")
    )

    SortingBarVisualizationCard(
        title = "INSERTION SORT VISUALIZATION",
        algorithmBadge = "O(n) Best Case",
        steps = steps
    )
}

@Composable
private fun SortingComparisonCard() {
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

// --- EXISTING VISUALIZATIONS ---

@Composable
private fun ArrayVisualizationCard() {
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
                    text = "ARRAY VISUALIZATION",
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
                        text = "O(1) Time Complexity",
                        color = BlueAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Array Cells Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val arrayItems = listOf(
                    Triple(0, "10", false),
                    Triple(1, "20", false),
                    Triple(2, "30", true), // Highlighted target index 2 -> 30
                    Triple(3, "40", false)
                )

                arrayItems.forEach { (index, value, isTarget) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Index $index",
                            color = if (isTarget) OrangeAccent else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isTarget) FontWeight.Bold else FontWeight.Normal
                        )
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isTarget) Color(0xFF2A1C12) else Color(0xFF13131C))
                                .border(
                                    width = if (isTarget) 2.dp else 1.dp,
                                    color = if (isTarget) OrangeAccent else CardBorderColor,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = value,
                                color = if (isTarget) OrangeAccent else TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Text(
                text = "numbers[2] → 30 (Access is instant regardless of array length)",
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ArrayTraversalVisualizationCard() {
    val arrayData = listOf("10", "25", "42", "67", "91")
    var currentIndex by remember { mutableStateOf(-1) }
    var isPlaying by remember { mutableStateOf(false) }

    val totalElements = arrayData.size
    val isCompleted = currentIndex >= totalElements
    val visitedCount = if (currentIndex < 0) 0 else (currentIndex + 1).coerceAtMost(totalElements)

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

    Surface(
        color = DarkCardBgColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ARRAY TRAVERSAL",
                    color = OrangeAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )

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

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isCompleted) Color(0xFF092317) else Color(0xFF13233A))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Array Cells Row (Indices 0..4 below cells)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                arrayData.forEachIndexed { index, value ->
                    val isCurrent = index == currentIndex && !isCompleted

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isCurrent) Color(0xFF2A1C12) else Color(0xFF13131C))
                                .border(
                                    width = if (isCurrent) 2.dp else 1.dp,
                                    color = if (isCurrent) OrangeAccent else CardBorderColor,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = value,
                                color = if (isCurrent) OrangeAccent else TextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = index.toString(),
                            color = if (isCurrent) OrangeAccent else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Explanation Panel with Visited Counter
            Surface(
                color = Color(0xFF13131C),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CardBorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val explanationText = when {
                        currentIndex < 0 -> "Start at index 0, then visit each element from left to right."
                        isCompleted -> "Traversal complete! All $totalElements elements have been visited."
                        else -> "Visiting index $currentIndex: value = ${arrayData[currentIndex]}."
                    }

                    Text(
                        text = explanationText,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    // Track & Visited Counter Row
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
                            val progressFloat = (visitedCount.toFloat() / totalElements).coerceIn(0f, 1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progressFloat)
                                    .height(3.dp)
                                    .background(OrangeAccent, RoundedCornerShape(2.dp))
                            )
                        }

                        Text(
                            text = "$visitedCount / $totalElements VISITED",
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
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset (↻)
                Surface(
                    color = DarkCardBgColor,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CardBorderColor),
                    modifier = Modifier
                        .size(44.dp)
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
                        .size(44.dp)
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
                    color = if (isCompleted) Color(0xFF22222E) else OrangeAccent,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clickable(enabled = !isCompleted) {
                            if (!isCompleted) {
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
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                        .size(44.dp)
                        .clickable(enabled = !isCompleted) {
                            isPlaying = false
                            if (currentIndex < 0) {
                                currentIndex = 0
                            } else if (currentIndex < totalElements - 1) {
                                currentIndex++
                            } else {
                                currentIndex = totalElements
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

@Composable
private fun ArrayInsertDeleteVisualizationCard() {
    var operationMode by remember { mutableStateOf("INSERT") } // "INSERT" or "DELETE"
    var stepIndex by remember { mutableStateOf(0) } // 0: READY, 1: STEP 1 OF 2, 2: COMPLETED
    var isPlaying by remember { mutableStateOf(false) }

    val totalSteps = 2
    val isCompleted = stepIndex >= totalSteps

    // Auto-play timer mechanism
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

    val displayItems = when (operationMode) {
        "INSERT" -> when (stepIndex) {
            0 -> listOf("10", "20", "30", "40")
            1 -> listOf("10", "20", "  ", "30", "40")
            else -> listOf("10", "20", "25", "30", "40")
        }
        else -> when (stepIndex) {
            0 -> listOf("10", "20", "30", "40")
            1 -> listOf("10", "  ", "30", "40")
            else -> listOf("10", "30", "40")
        }
    }

    val explanationText = when (operationMode) {
        "INSERT" -> when (stepIndex) {
            0 -> "Target: Insert 25 at index 2."
            1 -> "Step 1: Elements at index 2 and above shift right to make room."
            else -> "Step 2: Inserted 25 at index 2. Array now contains 5 elements."
        }
        else -> when (stepIndex) {
            0 -> "Target: Delete 20 at index 1."
            1 -> "Step 1: Removed 20 at index 1, leaving a gap."
            else -> "Step 2: Subsequent elements shift left to fill the gap. Array now contains 3 elements."
        }
    }

    val badgeText = when (stepIndex) {
        0 -> "READY"
        1 -> "STEP 1 OF 2"
        else -> "COMPLETED"
    }

    val badgeColor = when (stepIndex) {
        0 -> TextSecondary
        1 -> BlueAccent
        else -> GreenAccent
    }

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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row with Title, Badge, and Mode Selector Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "INSERT & DELETE",
                        color = OrangeAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isCompleted) Color(0xFF092317) else Color(0xFF13233A))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = badgeColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Mode Selector Toggle (Insert / Delete)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        color = if (operationMode == "INSERT") OrangeAccent else Color(0xFF22222E),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clickable {
                            isPlaying = false
                            operationMode = "INSERT"
                            stepIndex = 0
                        }
                    ) {
                        Text(
                            text = "Insert",
                            color = if (operationMode == "INSERT") DarkBgColor else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        color = if (operationMode == "DELETE") OrangeAccent else Color(0xFF22222E),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clickable {
                            isPlaying = false
                            operationMode = "DELETE"
                            stepIndex = 0
                        }
                    ) {
                        Text(
                            text = "Delete",
                            color = if (operationMode == "DELETE") DarkBgColor else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Array Cells Row (Size 44.dp for compact 360dp width fit)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                displayItems.forEachIndexed { index, value ->
                    val isHighlighted = (operationMode == "INSERT" && index == 2 && stepIndex == 2) ||
                            (operationMode == "DELETE" && index == 1 && stepIndex == 1)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isHighlighted) Color(0xFF2A1C12) else Color(0xFF13131C))
                                .border(
                                    width = if (isHighlighted) 1.5.dp else 1.dp,
                                    color = if (isHighlighted) OrangeAccent else CardBorderColor,
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = value,
                                color = if (isHighlighted) OrangeAccent else TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = index.toString(),
                            color = if (isHighlighted) OrangeAccent else TextSecondary,
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
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = explanationText,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    // Track & Step Indicator Row
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
                            val progressFloat = stepIndex / 2f
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progressFloat)
                                    .height(3.dp)
                                    .background(OrangeAccent, RoundedCornerShape(2.dp))
                            )
                        }

                        Text(
                            text = "STEP $stepIndex / 2",
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
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset (↻)
                Surface(
                    color = DarkCardBgColor,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CardBorderColor),
                    modifier = Modifier
                        .size(44.dp)
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

                // Previous (‹)
                Surface(
                    color = DarkCardBgColor,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CardBorderColor),
                    modifier = Modifier
                        .size(44.dp)
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

                // Play / Pause (▶ Play / ⏸ Pause)
                Surface(
                    color = if (isCompleted) Color(0xFF22222E) else OrangeAccent,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clickable(enabled = !isCompleted) {
                            if (!isCompleted) {
                                isPlaying = !isPlaying
                            }
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                        .size(44.dp)
                        .clickable(enabled = !isCompleted) {
                            isPlaying = false
                            if (stepIndex < totalSteps) {
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

@Composable
private fun ArrayComplexityComparisonCard() {
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

@Composable
private fun LinearSearchVisualizationCard() {
    val arrayData = listOf("4", "8", "2", "7", "5")
    val target = 7
    var currentIndex by remember { mutableStateOf(0) }
    val isFound = currentIndex < arrayData.size && arrayData[currentIndex].toInt() == target
    val isFinished = isFound || currentIndex >= arrayData.size

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
                    text = "LINEAR SEARCH VISUALIZATION",
                    color = OrangeAccent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isFound) Color(0xFF092317) else Color(0xFF13233A))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Target: $target",
                        color = if (isFound) GreenAccent else BlueAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Array Cells Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                arrayData.forEachIndexed { index, value ->
                    val isCurrent = index == currentIndex && !isFinished
                    val isTargetMatch = index == currentIndex && isFound

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isCurrent || isTargetMatch) "↑ index $index" else "Index $index",
                            color = if (isTargetMatch) GreenAccent else if (isCurrent) OrangeAccent else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isCurrent || isTargetMatch) FontWeight.Bold else FontWeight.Normal
                        )
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when {
                                        isTargetMatch -> Color(0xFF092317)
                                        isCurrent -> Color(0xFF2A1C12)
                                        else -> Color(0xFF13131C)
                                    }
                                )
                                .border(
                                    width = if (isCurrent || isTargetMatch) 2.dp else 1.dp,
                                    color = when {
                                        isTargetMatch -> GreenAccent
                                        isCurrent -> OrangeAccent
                                        else -> CardBorderColor
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = value,
                                color = when {
                                    isTargetMatch -> GreenAccent
                                    isCurrent -> OrangeAccent
                                    else -> TextPrimary
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Status and Control
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when {
                        isFound -> "Target 7 found at index $currentIndex! ✓"
                        currentIndex >= arrayData.size -> "Target not found in array"
                        else -> "Checking index $currentIndex: ${arrayData[currentIndex]} != 7"
                    },
                    color = if (isFound) GreenAccent else TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = if (isFound) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.weight(1f, fill = false),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Surface(
                    color = if (isFinished) Color(0xFF22222E) else OrangeAccent,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable {
                        if (isFinished) {
                            currentIndex = 0
                        } else {
                            currentIndex++
                        }
                    }
                ) {
                    Text(
                        text = if (isFinished) "Restart ↻" else "Next Step →",
                        color = if (isFinished) TextPrimary else DarkBgColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BinarySearchVisualizationCard() {
    val arrayData = listOf("1", "3", "5", "7", "9", "11", "13")
    val target = 11
    var stepIndex by remember { mutableStateOf(0) } // 0, 1

    // Step 0: L=0, R=6, M=3 (val 7)
    // Step 1: L=4, R=6, M=5 (val 11 - Found!)
    val (left, right, mid) = when (stepIndex) {
        0 -> Triple(0, 6, 3)
        else -> Triple(4, 6, 5)
    }

    val isFound = stepIndex == 1

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
                    text = "BINARY SEARCH VISUALIZATION",
                    color = OrangeAccent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isFound) Color(0xFF092317) else Color(0xFF13233A))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Target: $target (Sorted)",
                        color = if (isFound) GreenAccent else BlueAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Array Cells Row with L, M, R indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                arrayData.forEachIndexed { index, value ->
                    val isMid = index == mid
                    val isLeft = index == left
                    val isRight = index == right
                    val isEliminated = index < left || index > right

                    val pointerLabel = when {
                        isMid -> "M"
                        isLeft -> "L"
                        isRight -> "R"
                        else -> " "
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = pointerLabel,
                            color = if (isMid) OrangeAccent else if (isLeft || isRight) BlueAccent else Color.Transparent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when {
                                        isFound && isMid -> Color(0xFF092317)
                                        isMid -> Color(0xFF2A1C12)
                                        isEliminated -> Color(0xFF0F0F14)
                                        else -> Color(0xFF13131C)
                                    }
                                )
                                .border(
                                    width = if (isMid) 2.dp else 1.dp,
                                    color = when {
                                        isFound && isMid -> GreenAccent
                                        isMid -> OrangeAccent
                                        isEliminated -> Color(0xFF1E1E26)
                                        else -> CardBorderColor
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = value,
                                color = when {
                                    isFound && isMid -> GreenAccent
                                    isMid -> OrangeAccent
                                    isEliminated -> Color(0xFF4B5563)
                                    else -> TextPrimary
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Status and Control
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (stepIndex == 0) {
                        "Mid=3 (val 7) < 11 → Eliminate left half [1, 3, 5, 7]"
                    } else {
                        "Mid=5 (val 11) == 11 → Found 11 at index 5! ✓"
                    },
                    color = if (isFound) GreenAccent else TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = if (isFound) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.weight(1f, fill = false),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Surface(
                    color = if (isFound) Color(0xFF22222E) else OrangeAccent,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable {
                        if (isFound) {
                            stepIndex = 0
                        } else {
                            stepIndex++
                        }
                    }
                ) {
                    Text(
                        text = if (isFound) "Restart ↻" else "Next Step →",
                        color = if (isFound) TextPrimary else DarkBgColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchComparisonCard() {
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
                        text = "Target: 44 (11 items)",
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
                            text = "Inspects items 2 → 5 → 8 → ... → 44",
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
                            text = "10 Steps  O(n)",
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
                            text = "Halves search space: Mid 21 → Mid 38 → Mid 44",
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
                            text = "3 Steps  O(log n)",
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

@Composable
private fun CheckYourUnderstandingSection(
    statement: String = "What is the index of the first element in a typical array?",
    options: List<String> = listOf("0", "1", "-1", "2"),
    correctIndex: Int = 0,
    explanation: String = "Arrays use zero-based indexing, so the first element is always stored at index 0.",
    selectedOption: Int?,
    onSelectOption: (Int) -> Unit
) {
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "CHECK YOUR UNDERSTANDING",
                color = OrangeAccent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            Text(
                text = statement,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 22.sp
            )

            // Options list
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                options.forEachIndexed { index, optionText ->
                    val isSelected = selectedOption == index
                    val isCorrectOption = index == correctIndex
                    val isAnswered = selectedOption != null

                    val style = when {
                        !isAnswered -> OptionStyle(CardBorderColor, Color(0xFF13131C), TextPrimary, null)
                        isCorrectOption -> OptionStyle(GreenAccent, Color(0xFF092317), GreenAccent, "✓")
                        isSelected -> OptionStyle(RedAccent, Color(0xFF210C0D), RedAccent, "×")
                        else -> OptionStyle(CardBorderColor, Color(0xFF13131C), TextSecondary, null)
                    }

                    Surface(
                        color = style.bgColor,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, style.borderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isAnswered) { onSelectOption(index) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = optionText,
                                color = style.textColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (style.iconStr != null) {
                                Text(
                                    text = style.iconStr,
                                    color = style.textColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Explanation Feedback
            if (selectedOption != null) {
                val isCorrectAnswer = selectedOption == correctIndex
                Surface(
                    color = if (isCorrectAnswer) Color(0xFF092317) else Color(0xFF210C0D),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (isCorrectAnswer) GreenAccent else RedAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isCorrectAnswer) "Correct!" else "Incorrect",
                            color = if (isCorrectAnswer) GreenAccent else RedAccent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = explanation,
                            color = Color(0xFFD1D5DB),
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FinishLessonSection(
    isCompleted: Boolean,
    nextLessonId: String?,
    onComplete: () -> Unit,
    onNavigateToNextLesson: (String) -> Unit,
    onBackToTopic: () -> Unit
) {
    if (isCompleted) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = Color(0xFF092317),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, GreenAccent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(GreenAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✓",
                            color = Color(0xFF111115),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column {
                        Text(
                            text = "Lesson Completed",
                            color = GreenAccent,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Great job! You've mastered this section.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Next Lesson or Back to Topic Button
            Surface(
                color = OrangeAccent,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (nextLessonId != null) {
                            onNavigateToNextLesson(nextLessonId)
                        } else {
                            onBackToTopic()
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (nextLessonId != null) "Next Lesson →" else "Back to Topic →",
                        color = Color(0xFF111115),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    } else {
        Surface(
            color = OrangeAccent,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onComplete() }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 11.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Complete Lesson ➔",
                    color = Color(0xFF111115),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun LessonCompletionCelebrationOverlay(
    lessonTitle: String,
    xpAmount: Int,
    nextLessonId: String?,
    onContinue: () -> Unit,
    onReview: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF2111115))
            .clickable(enabled = false) {}
            .padding(horizontal = 24.dp, vertical = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Center Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Celebration Badge Container with spark accents
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "✨",
                        fontSize = 18.sp,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(end = 40.dp)
                    )
                    Text(
                        text = "⭐",
                        fontSize = 16.sp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(start = 40.dp)
                    )

                    Surface(
                        color = Color(0xFF221A15),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.5.dp, OrangeAccent),
                        modifier = Modifier.size(88.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Surface(
                                color = Color.Transparent,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(2.dp, OrangeAccent),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "✓",
                                        color = OrangeAccent,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "LESSON COMPLETE",
                    color = OrangeAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                Text(
                    text = "Lesson completed!",
                    color = TextPrimary,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = lessonTitle,
                    color = TextSecondary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(6.dp))

                // XP Reward Card
                Surface(
                    color = Color(0xFF1E1713),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, OrangeAccent.copy(alpha = 0.35f)),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 36.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "LESSON REWARD",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "+$xpAmount XP",
                            color = OrangeAccent,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Saved Progress Confirmation Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF064E3B))
                            .border(1.dp, GreenAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✓",
                            color = GreenAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Your progress has been saved.",
                        color = Color(0xFFD1D5DB),
                        fontSize = 13.sp
                    )
                }
            }

            // Bottom Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Primary: Continue to next lesson
                Surface(
                    color = OrangeAccent,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onContinue() }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 15.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (nextLessonId != null) "Continue to next lesson →" else "Back to Topic →",
                            color = Color(0xFF111115),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Secondary: Review lesson
                Surface(
                    color = DarkCardBgColor,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, CardBorderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onReview() }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 15.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Review lesson",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
