package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learndsandalgorithm.domain.model.QuestionType
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

    val selectedQuestionAnswers = remember(lessonId) { mutableStateMapOf<String, Int>() }
    var lessonQuestionIndex by remember(lessonId) { mutableStateOf(0) }
    var isLessonCompleted by remember { mutableStateOf(false) }

    LaunchedEffect(lessonId) {
        viewModel.loadLesson(lessonId)
        lessonQuestionIndex = 0
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
                            "lesson_linked_list_1" -> LinkedListIntroductionVisualizationCard()
                            "lesson_linked_list_2" -> LinkedListTraversalSearchAccessVisualizationCard()
                            "lesson_linked_list_3" -> LinkedListInsertionVisualizationCard()
                            "lesson_linked_list_4" -> LinkedListRemovalVisualizationCard()
                            "lesson_linked_list_5" -> LinkedListPerformanceComparisonVisualizationCard()
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
                            val hasQuestionSequence = state.questions.size > 1
                            val questionIndex = if (hasQuestionSequence) {
                                lessonQuestionIndex.coerceIn(0, state.questions.lastIndex)
                            } else {
                                0
                            }
                            val question = state.questions[questionIndex]
                            CheckYourUnderstandingSection(
                                statement = question.statement,
                                options = question.options,
                                correctIndex = question.correctOptionIndex,
                                explanation = question.explanation,
                                selectedOption = selectedQuestionAnswers[question.id],
                                onSelectOption = { selectedQuestionAnswers[question.id] = it },
                                questionType = question.type,
                                questionNumber = questionIndex + 1,
                                questionCount = if (hasQuestionSequence) state.questions.size else 1,
                                showProgress = hasQuestionSequence,
                                onPreviousQuestion = if (hasQuestionSequence) {
                                    { lessonQuestionIndex = (questionIndex - 1).coerceAtLeast(0) }
                                } else null,
                                onNextQuestion = if (hasQuestionSequence) {
                                    { lessonQuestionIndex = (questionIndex + 1).coerceAtMost(state.questions.lastIndex) }
                                } else null
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
    var stepIndex by remember(steps) { mutableStateOf(0) }
    var isPlaying by remember(steps) { mutableStateOf(false) }

    val totalSteps = steps.size
    val currentStep = steps.getOrElse(stepIndex) { steps.last() }
    val isFinished = stepIndex >= totalSteps - 1

    // Auto-play timer mechanism
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
            // Header Row with Title and Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = OrangeAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
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
                        text = currentStep.message,
                        color = if (isFinished) GreenAccent else TextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
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
                    color = if (isFinished) Color(0xFF22222E) else OrangeAccent,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clickable(enabled = !isFinished) {
                            if (!isFinished) {
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
                        .size(44.dp)
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

// --- SPECIFIC SORTING ALGORITHM STEP GENERATORS USING SHARED RENDERER ---

@Composable
private fun BubbleSortVisualizationCard() {
    val steps = listOf(
        SortingStep(listOf(5, 3, 8, 4, 2), listOf(0, 1), emptyList(), "Compare 5 & 3 → 5 > 3, swap needed"),
        SortingStep(listOf(3, 5, 8, 4, 2), listOf(1, 2), emptyList(), "Compare 5 & 8 → 5 < 8, no swap"),
        SortingStep(listOf(3, 5, 8, 4, 2), listOf(2, 3), emptyList(), "Compare 8 & 4 → 8 > 4, swap needed"),
        SortingStep(listOf(3, 5, 4, 8, 2), listOf(3, 4), emptyList(), "Compare 8 & 2 → 8 > 2, swap needed"),
        SortingStep(listOf(3, 5, 4, 2, 8), emptyList(), listOf(4), "8 bubbled to the end! Pass 1 complete"),
        SortingStep(listOf(3, 5, 4, 2, 8), listOf(0, 1), listOf(4), "Pass 2: Compare 3 & 5 → 3 < 5, no swap"),
        SortingStep(listOf(3, 5, 4, 2, 8), listOf(1, 2), listOf(4), "Compare 5 & 4 → 5 > 4, swap needed"),
        SortingStep(listOf(3, 4, 5, 2, 8), listOf(2, 3), listOf(4), "Compare 5 & 2 → 5 > 2, swap needed"),
        SortingStep(listOf(3, 4, 2, 5, 8), emptyList(), listOf(3, 4), "Pass 2 complete: 5 joins 8 at the end"),
        SortingStep(listOf(3, 4, 2, 5, 8), listOf(0, 1), listOf(3, 4), "Pass 3: Compare 3 & 4 → 3 < 4, no swap"),
        SortingStep(listOf(3, 4, 2, 5, 8), listOf(1, 2), listOf(3, 4), "Compare 4 & 2 → 4 > 2, swap needed"),
        SortingStep(listOf(3, 2, 4, 5, 8), emptyList(), listOf(2, 3, 4), "Pass 3 complete: 4, 5, and 8 are in place"),
        SortingStep(listOf(3, 2, 4, 5, 8), listOf(0, 1), listOf(2, 3, 4), "Pass 4: Compare 3 & 2 → 3 > 2, swap needed"),
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
        SortingStep(listOf(3, 5, 8, 4, 2), listOf(2), listOf(0, 1), "Take key 8 → 8 > 5, already in place"),
        SortingStep(listOf(3, 5, 4, 8, 2), listOf(2), listOf(0, 1), "Take key 4 → 8 has shifted right; shift 5 right, then insert 4"),
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
private fun LinkedListIntroductionVisualizationCard() {
    val nodeValues = listOf("12", "7", "19")
    var stepIndex by remember { mutableStateOf(0) } // 0: Start at head, 1: Follow next, 2: Reach the end
    var isPlaying by remember { mutableStateOf(false) }

    val totalSteps = 3
    val isCompleted = stepIndex >= totalSteps - 1
    val nodeBringIntoViewRequesters = remember(nodeValues.size) {
        List(nodeValues.size) { BringIntoViewRequester() }
    }
    val nullBringIntoViewRequester = remember { BringIntoViewRequester() }
    var skipInitialBringIntoView by remember { mutableStateOf(true) }

    // Auto-play timer mechanism
    LaunchedEffect(isPlaying, stepIndex) {
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

    LaunchedEffect(stepIndex) {
        if (skipInitialBringIntoView) {
            skipInitialBringIntoView = false
            return@LaunchedEffect
        }

        if (stepIndex == totalSteps - 1) {
            nullBringIntoViewRequester.bringIntoView()
        } else {
            val activeNodeIndex = stepIndex.coerceAtMost(nodeValues.lastIndex)
            nodeBringIntoViewRequesters[activeNodeIndex].bringIntoView()
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
                    text = "LINKED LIST INTRODUCTION",
                    color = OrangeAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )

                val badgeText = when (stepIndex) {
                    0 -> "START AT HEAD"
                    1 -> "FOLLOW NEXT"
                    else -> "REACH END"
                }

                val badgeColor = when {
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

            // Node Diagram Row (Horizontally Scrollable for Narrow Screens & Head Anchored directly to Node 12)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Head Pointer Indicator
                val isHeadHighlighted = stepIndex == 0 || stepIndex == 1
                val headBorderColor = if (isHeadHighlighted) OrangeAccent else CardBorderColor
                val headBgColor = if (isHeadHighlighted) Color(0xFF2A1C12) else Color(0xFF13131C)
                val headTextColor = if (isHeadHighlighted) OrangeAccent else TextSecondary

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(headBgColor)
                        .border(1.dp, headBorderColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "head •",
                        color = headTextColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "──►",
                    color = if (isHeadHighlighted) OrangeAccent else TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                // Horizontal Node Chain Row: Node(12) -> Node(7) -> Node(19) -> null
                nodeValues.forEachIndexed { index, value ->
                    val isNodeHighlighted = when (stepIndex) {
                        0 -> index == 0
                        1 -> true
                        2 -> index == 2
                        else -> false
                    }

                    val nodeBg = when {
                        isNodeHighlighted && stepIndex == 2 -> Color(0xFF092317)
                        isNodeHighlighted -> Color(0xFF2A1C12)
                        else -> Color(0xFF13131C)
                    }

                    val nodeBorder = when {
                        isNodeHighlighted && stepIndex == 2 -> GreenAccent
                        isNodeHighlighted -> OrangeAccent
                        else -> CardBorderColor
                    }

                    // Node Box with Data and Next sections
                    Surface(
                        modifier = Modifier.bringIntoViewRequester(nodeBringIntoViewRequesters[index]),
                        color = nodeBg,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(if (isNodeHighlighted) 1.5.dp else 1.dp, nodeBorder),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            // Data Part
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "DATA",
                                    color = TextSecondary,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = value,
                                    color = if (isNodeHighlighted) TextPrimary else TextSecondary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.size(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(1.dp, 24.dp)
                                    .background(CardBorderColor)
                            )
                            Spacer(modifier = Modifier.size(6.dp))

                            // Next Pointer Part
                            val nextTextColor = when {
                                isNodeHighlighted && stepIndex == 2 -> GreenAccent
                                isNodeHighlighted -> OrangeAccent
                                else -> TextSecondary
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "NEXT",
                                    color = TextSecondary,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "•",
                                    color = nextTextColor,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Connecting Arrow
                    val isArrowHighlighted = when (stepIndex) {
                        0 -> index == 0
                        1 -> index < nodeValues.size - 1
                        2 -> index == nodeValues.size - 1
                        else -> false
                    }

                    Text(
                        text = "─►",
                        color = if (isArrowHighlighted && stepIndex == 2) GreenAccent else if (isArrowHighlighted) OrangeAccent else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Null Terminator Box
                val isNullHighlighted = stepIndex == 2
                Box(
                    modifier = Modifier
                        .bringIntoViewRequester(nullBringIntoViewRequester)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isNullHighlighted) Color(0xFF092317) else Color(0xFF13131C))
                        .border(
                            1.dp,
                            if (isNullHighlighted) GreenAccent else CardBorderColor,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "null",
                        color = if (isNullHighlighted) GreenAccent else TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Explanation Panel
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
                    val explanationText = when (stepIndex) {
                        0 -> "Step 1 — Start at head: head references the first node (12). It is a pointer variable storing the memory address of node 12, not a data node itself."
                        1 -> "Step 2 — Follow next: Each node contains a data value and a next reference that points to the next node in sequence (12 → 7 → 19)."
                        else -> "Step 3 — Reach the end: Node 19's next reference points to null (nullptr in C++, None in Python), marking the end of the list."
                    }

                    Text(
                        text = explanationText,
                        color = if (isCompleted) GreenAccent else TextPrimary,
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
                            val progressFloat = (stepIndex + 1).toFloat() / totalSteps
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progressFloat.coerceIn(0f, 1f))
                                    .height(3.dp)
                                    .background(if (isCompleted) GreenAccent else OrangeAccent, RoundedCornerShape(2.dp))
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
                            if (stepIndex < totalSteps - 1) {
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
private fun LinkedListTraversalSearchAccessVisualizationCard() {
    data class Step(
        val currentNode: Int?,
        val currentPosition: Int?,
        val focusedNode: Int?,
        val visitedNodes: Set<Int>,
        val explanation: String
    )

    val nodeValues = listOf("12", "7", "19", "5", "25")
    var operation by remember { mutableStateOf("TRAVERSE") }
    var searchForExisting by remember { mutableStateOf(true) }
    var accessValidIndex by remember { mutableStateOf(true) }
    val searchTarget = if (searchForExisting) "19" else "99"
    val requestedIndex = if (accessValidIndex) 3 else 7

    fun createSteps(): List<Step> {
        val result = mutableListOf<Step>()
        when (operation) {
            "TRAVERSE" -> {
                result += Step(0, 0, null, emptySet(), "Initialize current = head. The pointer starts at index 0.")
                nodeValues.forEachIndexed { index, value ->
                    val visited = (0..index).toSet()
                    result += Step(index, index, index, visited, "Visit index $index and process value $value.")
                    val nextIndex = index + 1
                    result += Step(
                        if (nextIndex < nodeValues.size) nextIndex else null,
                        nextIndex,
                        null,
                        visited,
                        if (nextIndex < nodeValues.size) {
                            "Advance current = current.next to index $nextIndex."
                        } else {
                            "Advance current = current.next. Now current is null, so traversal stops."
                        }
                    )
                }
            }
            "SEARCH" -> {
                result += Step(0, 0, null, emptySet(), "Initialize current = head to search for $searchTarget.")
                for (index in nodeValues.indices) {
                    val value = nodeValues[index]
                    val visited = (0..index).toSet()
                    val found = value == searchTarget
                    result += Step(
                        index,
                        index,
                        index,
                        visited,
                        if (found) {
                            "Compare $value with $searchTarget: match at index $index. Return $index."
                        } else {
                            "Compare $value with $searchTarget: no match."
                        }
                    )
                    if (!found) {
                        val nextIndex = index + 1
                        result += Step(
                            if (nextIndex < nodeValues.size) nextIndex else null,
                            nextIndex,
                            null,
                            visited,
                            if (nextIndex < nodeValues.size) {
                                "Advance current = current.next to index $nextIndex."
                            } else {
                                "Advance to null after checking every node. $searchTarget was not found; return -1."
                            }
                        )
                    }
                    if (found) break
                }
            }
            else -> {
                result += Step(0, 0, null, emptySet(), "Initialize current = head at index 0 to access index $requestedIndex.")
                for (index in nodeValues.indices) {
                    val value = nodeValues[index]
                    val visited = (0..index).toSet()
                    val reached = index == requestedIndex
                    result += Step(
                        index,
                        index,
                        index,
                        visited,
                        if (reached) {
                            "currentIndex == $requestedIndex. Return value $value at index $index."
                        } else {
                            "Check index $index: it is before the requested index $requestedIndex."
                        }
                    )
                    if (reached) break
                    val nextIndex = index + 1
                    result += Step(
                        if (nextIndex < nodeValues.size) nextIndex else null,
                        nextIndex,
                        null,
                        visited,
                        if (nextIndex < nodeValues.size) {
                            "Move current = current.next to index $nextIndex; currentIndex becomes $nextIndex."
                        } else {
                            "Advance to null at currentIndex $nextIndex. Index $requestedIndex is out of bounds."
                        }
                    )
                }
            }
        }
        return result
    }

    val steps = remember(operation, searchForExisting, accessValidIndex) { createSteps() }
    var stepIndex by remember(operation, searchForExisting, accessValidIndex) { mutableStateOf(0) }
    var isPlaying by remember(operation, searchForExisting, accessValidIndex) { mutableStateOf(false) }
    val currentStep = steps[stepIndex]
    val isCompleted = stepIndex == steps.lastIndex
    val nodeBringIntoViewRequesters = remember(nodeValues.size) {
        List(nodeValues.size) { BringIntoViewRequester() }
    }
    val nullBringIntoViewRequester = remember { BringIntoViewRequester() }
    var skipInitialBringIntoView by remember { mutableStateOf(true) }

    LaunchedEffect(isPlaying, stepIndex, steps) {
        if (isPlaying) {
            if (stepIndex < steps.lastIndex) {
                delay(1000L)
                if (stepIndex < steps.lastIndex) stepIndex++ else isPlaying = false
            } else {
                isPlaying = false
            }
        }
    }

    LaunchedEffect(currentStep.currentNode, currentStep.currentPosition) {
        if (skipInitialBringIntoView) {
            skipInitialBringIntoView = false
            return@LaunchedEffect
        }

        val currentNode = currentStep.currentNode
        if (currentNode != null) {
            nodeBringIntoViewRequesters[currentNode].bringIntoView()
        } else {
            nullBringIntoViewRequester.bringIntoView()
        }
    }

    Surface(
        color = DarkCardBgColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("TRAVERSE · SEARCH · ACCESS", color = OrangeAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(4.dp))
                        .background(if (isCompleted) Color(0xFF092317) else Color(0xFF13233A))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isCompleted) "COMPLETED" else "STEP ${stepIndex + 1} OF ${steps.size}",
                        color = if (isCompleted) GreenAccent else BlueAccent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("TRAVERSE" to "Traverse", "SEARCH" to "Search", "ACCESS" to "By index").forEach { (value, label) ->
                    Surface(
                        color = if (operation == value) OrangeAccent else Color(0xFF22222E),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).clickable { operation = value }
                    ) {
                        Box(Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                            Text(label, color = if (operation == value) DarkBgColor else TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            when (operation) {
                "SEARCH" -> Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(true to "Find 19", false to "Find 99").forEach { (existing, label) ->
                        Surface(
                            color = if (searchForExisting == existing) Color(0xFF2A1C12) else Color(0xFF13131C),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (searchForExisting == existing) OrangeAccent else CardBorderColor),
                            modifier = Modifier.weight(1f).clickable { searchForExisting = existing }
                        ) {
                            Box(Modifier.padding(vertical = 7.dp), contentAlignment = Alignment.Center) {
                                Text(label, color = if (searchForExisting == existing) OrangeAccent else TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                "ACCESS" -> Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(true to "Valid index · 3", false to "Invalid index · 7").forEach { (valid, label) ->
                        Surface(
                            color = if (accessValidIndex == valid) Color(0xFF2A1C12) else Color(0xFF13131C),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (accessValidIndex == valid) OrangeAccent else CardBorderColor),
                            modifier = Modifier.weight(1f).clickable { accessValidIndex = valid }
                        ) {
                            Box(Modifier.padding(vertical = 7.dp), contentAlignment = Alignment.Center) {
                                Text(label, color = if (accessValidIndex == valid) OrangeAccent else TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Surface(
                color = Color(0xFF13131C),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CardBorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Head Pointer Indicator
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "HEAD",
                            color = OrangeAccent,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF2A1C12))
                                .border(1.dp, OrangeAccent, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "head •",
                                color = OrangeAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "──►",
                        color = OrangeAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Node Chain: 0..4
                    nodeValues.forEachIndexed { index, value ->
                        val isCurrent = currentStep.currentNode == index
                        val isFocused = currentStep.focusedNode == index
                        val isVisited = currentStep.visitedNodes.contains(index)

                        Column(
                            modifier = Modifier.bringIntoViewRequester(nodeBringIntoViewRequesters[index]),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            // Top Indicator Stack
                            Box(modifier = Modifier.height(14.dp), contentAlignment = Alignment.Center) {
                                when {
                                    isCurrent -> Text("CURRENT", color = BlueAccent, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    isFocused -> Text("CHECK", color = OrangeAccent, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    else -> {}
                                }
                            }

                            // Node Card Box
                            Surface(
                                color = when {
                                    isFocused -> Color(0xFF2A1C12)
                                    isVisited -> Color(0xFF172238)
                                    else -> DarkCardBgColor
                                },
                                shape = RoundedCornerShape(9.dp),
                                border = BorderStroke(
                                    if (isFocused || isCurrent) 1.5.dp else 1.dp,
                                    when {
                                        isFocused -> OrangeAccent
                                        isCurrent -> BlueAccent
                                        else -> CardBorderColor
                                    }
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // DATA
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("DATA", color = TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            value,
                                            color = if (isFocused || isCurrent) TextPrimary else TextSecondary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Box(Modifier.width(1.dp).height(24.dp).background(CardBorderColor))

                                    // NEXT
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("NEXT", color = TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            "•",
                                            color = if (isCurrent) BlueAccent else TextSecondary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Bottom Index Label
                            Text(
                                text = if (operation == "ACCESS") "idx $index" else "i:$index",
                                color = TextSecondary,
                                fontSize = 9.sp
                            )
                        }

                        // Connecting Arrow
                        val isArrowActive = isCurrent && index < nodeValues.lastIndex
                        Text(
                            text = "─►",
                            color = if (isArrowActive) BlueAccent else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Null Terminator Box
                    val isNullActive = currentStep.currentNode == null
                    Column(
                        modifier = Modifier.bringIntoViewRequester(nullBringIntoViewRequester),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Box(modifier = Modifier.height(14.dp), contentAlignment = Alignment.Center) {
                            if (isNullActive) {
                                Text("CURRENT", color = BlueAccent, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isNullActive) Color(0xFF092317) else DarkCardBgColor)
                                .border(
                                    1.dp,
                                    if (isNullActive) GreenAccent else CardBorderColor,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "null",
                                color = if (isNullActive) GreenAccent else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = if (operation == "ACCESS") "idx ${currentStep.currentPosition ?: nodeValues.size}" else "",
                            color = TextSecondary,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            Surface(color = Color(0xFF13131C), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, CardBorderColor), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(currentStep.explanation, color = TextPrimary, fontSize = 13.sp, lineHeight = 18.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1f).height(3.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF262634))) {
                            Box(Modifier.fillMaxWidth(if (steps.lastIndex == 0) 1f else stepIndex.toFloat() / steps.lastIndex).height(3.dp).background(if (isCompleted) GreenAccent else OrangeAccent, RoundedCornerShape(2.dp)))
                        }
                        Text("STEP ${stepIndex + 1} / ${steps.size}", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(color = DarkCardBgColor, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, CardBorderColor), modifier = Modifier.size(44.dp).clickable { isPlaying = false; stepIndex = 0 }) {
                    Box(contentAlignment = Alignment.Center) { Text("↻", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                }
                Surface(color = DarkCardBgColor, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, CardBorderColor), modifier = Modifier.size(44.dp).clickable(enabled = stepIndex > 0) { isPlaying = false; if (stepIndex > 0) stepIndex-- }) {
                    Box(contentAlignment = Alignment.Center) { Text("‹", color = if (stepIndex > 0) TextPrimary else TextSecondary.copy(alpha = 0.3f), fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                }
                Surface(color = if (isCompleted) Color(0xFF22222E) else OrangeAccent, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f).height(44.dp).clickable(enabled = !isCompleted) { if (!isCompleted) isPlaying = !isPlaying }) {
                    Box(contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(if (isPlaying) "⏸" else "▶", color = if (isCompleted) TextSecondary else Color(0xFF111115), fontSize = 14.sp)
                            Text(if (isPlaying) "Pause" else "Play", color = if (isCompleted) TextSecondary else Color(0xFF111115), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Surface(color = DarkCardBgColor, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, CardBorderColor), modifier = Modifier.size(44.dp).clickable(enabled = !isCompleted) { isPlaying = false; if (stepIndex < steps.lastIndex) stepIndex++ }) {
                    Box(contentAlignment = Alignment.Center) { Text("›", color = if (!isCompleted) TextPrimary else TextSecondary.copy(alpha = 0.3f), fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
private fun LinkedListInsertionVisualizationCard() {
    data class Step(
        val title: String,
        val explanation: String,
        val order: List<String>,
        val links: Set<Pair<String, String>>,
        val head: String,
        val highlighted: Set<String>,
        val activeNode: String,
        val badges: Map<String, String> = emptyMap()
    )

    var scenario by remember { mutableStateOf("HEAD") }

    fun createSteps(): List<Step> = when (scenario) {
        "HEAD" -> listOf(
            Step(
                "Choose the insertion point",
                "Insert 5 before the current head, 12. The existing links remain unchanged for now.",
                listOf("12", "7", "19"),
                setOf("12" to "7", "7" to "19", "19" to "null"),
                "12",
                setOf("12"),
                "12"
            ),
            Step(
                "Create the new node",
                "Create node 5 at the head insertion point. Its NEXT link is not connected yet.",
                listOf("5", "12", "7", "19"),
                setOf("12" to "7", "7" to "19", "19" to "null"),
                "12",
                setOf("5", "12"),
                "5",
                mapOf("5" to "NEW")
            ),
            Step(
                "Connect the new node to the old head",
                "Set newNode.next = head. Node 5 now points to node 12; head still points to 12.",
                listOf("5", "12", "7", "19"),
                setOf("5" to "12", "12" to "7", "7" to "19", "19" to "null"),
                "12",
                setOf("5", "12"),
                "5",
                mapOf("5" to "NEW")
            ),
            Step(
                "Move head to the new node",
                "Set head = newNode only after newNode.next is connected. The result is 5 → 12 → 7 → 19 → null.",
                listOf("5", "12", "7", "19"),
                setOf("5" to "12", "12" to "7", "7" to "19", "19" to "null"),
                "5",
                setOf("5", "12"),
                "5",
                mapOf("5" to "NEW")
            )
        )
        else -> listOf(
            Step(
                "Choose the predecessor",
                "Insert 15 after node 7. Node 19 is its current successor; keep 7 → 19 until the new link is ready.",
                listOf("12", "7", "19"),
                setOf("12" to "7", "7" to "19", "19" to "null"),
                "12",
                setOf("7", "19"),
                "7",
                mapOf("7" to "PREV", "19" to "NEXT")
            ),
            Step(
                "Create the new node",
                "Create node 15 between predecessor 7 and successor 19. The old 7 → 19 link still preserves the list.",
                listOf("12", "7", "15", "19"),
                setOf("12" to "7", "7" to "19", "19" to "null"),
                "12",
                setOf("7", "15", "19"),
                "15",
                mapOf("7" to "PREV", "15" to "NEW", "19" to "NEXT")
            ),
            Step(
                "Connect the new node to its successor",
                "First set newNode.next = prevNode.next. Now 15 points to 19 while 7 still points to 19.",
                listOf("12", "7", "15", "19"),
                setOf("12" to "7", "7" to "19", "15" to "19", "19" to "null"),
                "12",
                setOf("7", "15", "19"),
                "15",
                mapOf("7" to "PREV", "15" to "NEW", "19" to "NEXT")
            ),
            Step(
                "Point the predecessor to the new node",
                "Then set prevNode.next = newNode. The final chain is 12 → 7 → 15 → 19 → null.",
                listOf("12", "7", "15", "19"),
                setOf("12" to "7", "7" to "15", "15" to "19", "19" to "null"),
                "12",
                setOf("7", "15", "19"),
                "7",
                mapOf("7" to "PREV", "15" to "NEW", "19" to "NEXT")
            )
        )
    }

    val steps = remember(scenario) { createSteps() }
    var stepIndex by remember(scenario) { mutableStateOf(0) }
    var isPlaying by remember(scenario) { mutableStateOf(false) }
    var visibilityResetKey by remember(scenario) { mutableStateOf(0) }
    val currentStep = steps[stepIndex]
    val isCompleted = stepIndex == steps.lastIndex
    val visibilityTargets = buildList {
        currentStep.badges.filterValues { it == "PREV" || it == "NEXT" }.keys.forEach(::add)
        currentStep.links.firstOrNull { it.first == currentStep.activeNode }?.second
            ?.takeIf { it != "null" }
            ?.let(::add)
        if (isEmpty()) {
            currentStep.order.getOrNull(currentStep.order.indexOf(currentStep.activeNode) + 1)?.let(::add)
        }
    }.distinct().filter { it != currentStep.activeNode }

    LaunchedEffect(isPlaying, stepIndex, steps) {
        if (isPlaying && stepIndex < steps.lastIndex) {
            delay(1000L)
            if (stepIndex < steps.lastIndex) stepIndex++ else isPlaying = false
        } else if (isPlaying) {
            isPlaying = false
        }
    }

    Surface(
        color = DarkCardBgColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LinkedListVisualizerHeader("LINKED LIST INSERTION", stepIndex, steps.size, isCompleted)
            LinkedListScenarioSelector(
                options = listOf("HEAD" to "At head", "AFTER" to "After node 7"),
                selected = scenario,
                onSelected = { scenario = it }
            )
            LinkedListOperationDiagram(
                nodeOrder = currentStep.order,
                links = currentStep.links,
                headValue = currentStep.head,
                highlightedNodes = currentStep.highlighted,
                activeNode = currentStep.activeNode,
                visibilityTargets = visibilityTargets,
                nodeBadges = currentStep.badges,
                stepKey = scenario to (stepIndex to visibilityResetKey)
            )
            LinkedListStepExplanation(currentStep.title, currentStep.explanation, stepIndex, steps.size)
            LinkedListPlaybackControls(
                stepIndex = stepIndex,
                stepCount = steps.size,
                isPlaying = isPlaying,
                onReset = { isPlaying = false; stepIndex = 0; visibilityResetKey++ },
                onPrevious = { isPlaying = false; if (stepIndex > 0) stepIndex-- },
                onTogglePlayback = { isPlaying = !isPlaying },
                onNext = { isPlaying = false; if (stepIndex < steps.lastIndex) stepIndex++ }
            )
        }
    }
}

@Composable
private fun LinkedListRemovalVisualizationCard() {
    data class Step(
        val title: String,
        val explanation: String,
        val order: List<String>,
        val links: Set<Pair<String, String>>,
        val head: String,
        val highlighted: Set<String>,
        val activeNode: String,
        val detachedNodes: List<String> = emptyList(),
        val badges: Map<String, String> = emptyMap()
    )

    var scenario by remember { mutableStateOf("INTERIOR") }

    fun createSteps(): List<Step> = when (scenario) {
        "HEAD" -> listOf(
            Step(
                "Identify the head target",
                "The requested target is the first node, 12. No predecessor search is needed for head removal.",
                listOf("12", "7", "19"),
                setOf("12" to "7", "7" to "19", "19" to "null"),
                "12",
                setOf("12"),
                "12",
                badges = mapOf("12" to "TARGET")
            ),
            Step(
                "Advance head",
                "Set head = head.next. Head now points to 7; node 12 is detached from the list.",
                listOf("7", "19"),
                setOf("7" to "19", "19" to "null"),
                "7",
                setOf("7", "12"),
                "7",
                detachedNodes = listOf("12"),
                badges = mapOf("12" to "REMOVED")
            )
        )
        "MISSING" -> listOf(
            Step(
                "Start at head",
                "Search for 99 from head. Set current = head before checking nodes.",
                listOf("12", "7", "19"),
                setOf("12" to "7", "7" to "19", "19" to "null"),
                "12",
                setOf("12"),
                "12",
                badges = mapOf("12" to "CURRENT")
            ),
            Step(
                "Check node 12",
                "12 does not match 99. Advance current to 7; the list links are unchanged.",
                listOf("12", "7", "19"),
                setOf("12" to "7", "7" to "19", "19" to "null"),
                "12",
                setOf("12", "7"),
                "7",
                badges = mapOf("7" to "CURRENT")
            ),
            Step(
                "Check node 7",
                "7 does not match 99. Advance current to 19; the list links are unchanged.",
                listOf("12", "7", "19"),
                setOf("12" to "7", "7" to "19", "19" to "null"),
                "12",
                setOf("7", "19"),
                "19",
                badges = mapOf("19" to "CURRENT")
            ),
            Step(
                "Reach the end",
                "19 does not match 99. Advance current to null after checking the last node.",
                listOf("12", "7", "19"),
                setOf("12" to "7", "7" to "19", "19" to "null"),
                "12",
                setOf("19", "null"),
                "null",
                badges = mapOf("19" to "CURRENT")
            ),
            Step(
                "Leave the list unchanged",
                "Current is null, so 99 was not found. No links change; the original chain remains intact.",
                listOf("12", "7", "19"),
                setOf("12" to "7", "7" to "19", "19" to "null"),
                "12",
                setOf("12", "7", "19"),
                "null"
            )
        )
        else -> listOf(
            Step(
                "Start at head",
                "Search for 7 from head. Begin with current = 12 and no predecessor.",
                listOf("12", "7", "19"),
                setOf("12" to "7", "7" to "19", "19" to "null"),
                "12",
                setOf("12"),
                "12",
                badges = mapOf("12" to "CURRENT")
            ),
            Step(
                "Advance predecessor and current",
                "12 is not the target. Set prev = 12 and advance current to 7.",
                listOf("12", "7", "19"),
                setOf("12" to "7", "7" to "19", "19" to "null"),
                "12",
                setOf("12", "7"),
                "7",
                badges = mapOf("12" to "PREV", "7" to "CURRENT")
            ),
            Step(
                "Find the target and its predecessor",
                "Current matches 7. Its predecessor is 12, the node whose NEXT link must change.",
                listOf("12", "7", "19"),
                setOf("12" to "7", "7" to "19", "19" to "null"),
                "12",
                setOf("12", "7", "19"),
                "7",
                badges = mapOf("12" to "PREV", "7" to "TARGET")
            ),
            Step(
                "Bypass the target",
                "Set prev.next = target.next. Link 12 directly to 19, then show 7 outside the chain.",
                listOf("12", "19"),
                setOf("12" to "19", "19" to "null"),
                "12",
                setOf("12", "19"),
                "12",
                detachedNodes = listOf("7"),
                badges = mapOf("7" to "REMOVED")
            )
        )
    }

    val steps = remember(scenario) { createSteps() }
    var stepIndex by remember(scenario) { mutableStateOf(0) }
    var isPlaying by remember(scenario) { mutableStateOf(false) }
    val currentStep = steps[stepIndex]
    val isCompleted = stepIndex == steps.lastIndex

    LaunchedEffect(isPlaying, stepIndex, steps) {
        if (isPlaying && stepIndex < steps.lastIndex) {
            delay(1000L)
            if (stepIndex < steps.lastIndex) stepIndex++ else isPlaying = false
        } else if (isPlaying) {
            isPlaying = false
        }
    }

    Surface(
        color = DarkCardBgColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LinkedListVisualizerHeader("LINKED LIST REMOVAL", stepIndex, steps.size, isCompleted)
            LinkedListScenarioSelector(
                options = listOf(
                    "INTERIOR" to "Remove 7",
                    "HEAD" to "Remove head",
                    "MISSING" to "Missing 99"
                ),
                selected = scenario,
                onSelected = { scenario = it }
            )
            LinkedListOperationDiagram(
                nodeOrder = currentStep.order,
                links = currentStep.links,
                headValue = currentStep.head,
                highlightedNodes = currentStep.highlighted,
                activeNode = currentStep.activeNode,
                detachedNodes = currentStep.detachedNodes,
                nodeBadges = currentStep.badges,
                stepKey = scenario to stepIndex
            )
            LinkedListStepExplanation(currentStep.title, currentStep.explanation, stepIndex, steps.size)
            LinkedListPlaybackControls(
                stepIndex = stepIndex,
                stepCount = steps.size,
                isPlaying = isPlaying,
                onReset = { isPlaying = false; stepIndex = 0 },
                onPrevious = { isPlaying = false; if (stepIndex > 0) stepIndex-- },
                onTogglePlayback = { isPlaying = !isPlaying },
                onNext = { isPlaying = false; if (stepIndex < steps.lastIndex) stepIndex++ }
            )
        }
    }
}

@Composable
private fun LinkedListPerformanceComparisonVisualizationCard() {
    Surface(
        color = DarkCardBgColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "SINGLY VS. DOUBLY LINKED LISTS",
                color = OrangeAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            LinkedListTypeCard(
                title = "SINGLY LINKED NODE",
                description = "DATA + NEXT · forward traversal",
                doubly = false
            )
            LinkedListTypeCard(
                title = "DOUBLY LINKED NODE",
                description = "PREV + DATA + NEXT · traverse both directions",
                doubly = true
            )
            Surface(
                color = Color(0xFF13131C),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CardBorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("TIME COMPLEXITY", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    LinkedListComplexityRow("Index access", "Array O(1)", "List O(n)")
                    LinkedListComplexityRow("Linear search", "Array O(n)", "List O(n)")
                    LinkedListComplexityRow("Insert/remove at head", "Array O(n)", "List O(1)")
                    LinkedListComplexityRow("After a known node", "Array O(n)", "List O(1)")
                    LinkedListComplexityRow("Tail append, singly + head only", "—", "O(n)")
                    Text(
                        "A maintained list tail makes singly linked append O(1). Doubly linked nodes use extra memory for each PREV reference.",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun LinkedListTypeCard(title: String, description: String, doubly: Boolean) {
    Surface(
        color = Color(0xFF13131C),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                if (doubly) Text("←", color = BlueAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Surface(color = DarkCardBgColor, shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, CardBorderColor)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        if (doubly) LinkedListNodeField("PREV", "•")
                        LinkedListNodeField("DATA", "12")
                        LinkedListNodeField("NEXT", "•")
                    }
                }
                Text("→", color = OrangeAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(if (doubly) "tail" else "null", color = TextSecondary, fontSize = 10.sp)
            }
            Text(description, color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}

@Composable
private fun LinkedListNodeField(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextSecondary, fontSize = 7.sp, fontWeight = FontWeight.Bold)
        Text(value, color = BlueAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LinkedListComplexityRow(operation: String, arrayCost: String, listCost: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(operation, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(arrayCost, color = BlueAccent, fontSize = 10.sp)
            Text("·", color = TextSecondary, fontSize = 10.sp)
                Text("Singly linked $listCost", color = GreenAccent, fontSize = 10.sp)
        }
    }
}

@Composable
private fun LinkedListVisualizerHeader(title: String, stepIndex: Int, stepCount: Int, completed: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = OrangeAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        Text(
            if (completed) "COMPLETE" else "STEP ${stepIndex + 1} / $stepCount",
            color = if (completed) GreenAccent else BlueAccent,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LinkedListScenarioSelector(
    options: List<Pair<String, String>>,
    selected: String,
    onSelected: (String) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        options.forEach { (value, label) ->
            val isSelected = value == selected
            Surface(
                color = if (isSelected) Color(0xFF2A1C12) else Color(0xFF22222E),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (isSelected) OrangeAccent else CardBorderColor),
                modifier = Modifier.weight(1f).clickable { onSelected(value) }
            ) {
                Box(Modifier.padding(horizontal = 4.dp, vertical = 7.dp), contentAlignment = Alignment.Center) {
                    Text(
                        label,
                        color = if (isSelected) OrangeAccent else TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun LinkedListOperationDiagram(
    nodeOrder: List<String>,
    links: Set<Pair<String, String>>,
    headValue: String,
    highlightedNodes: Set<String>,
    activeNode: String,
    visibilityTargets: List<String> = emptyList(),
    detachedNodes: List<String> = emptyList(),
    nodeBadges: Map<String, String> = emptyMap(),
    stepKey: Any
) {
    val diagramItems = remember(nodeOrder, detachedNodes) {
        (nodeOrder + "null" + detachedNodes).distinct()
    }
    val requesters = remember(diagramItems) {
        diagramItems.associateWith { BringIntoViewRequester() }
    }
    var skipInitialVisibilityRequest by remember { mutableStateOf(true) }

    LaunchedEffect(stepKey, activeNode, diagramItems) {
        if (skipInitialVisibilityRequest) {
            skipInitialVisibilityRequest = false
            return@LaunchedEffect
        }
        visibilityTargets.forEach { value -> requesters[value]?.bringIntoView() }
        requesters[activeNode]?.bringIntoView()
    }

    Surface(
        color = Color(0xFF13131C),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(0xFF2A1C12),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, OrangeAccent)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("HEAD", color = OrangeAccent, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Text(headValue, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            nodeOrder.forEachIndexed { index, value ->
                LinkedListOperationNode(
                    value = value,
                    nextTarget = links.firstOrNull { it.first == value }?.second ?: "—",
                    badge = nodeBadges[value],
                    highlighted = value in highlightedNodes,
                    requester = requesters.getValue(value)
                )
                val nextValue = nodeOrder.getOrNull(index + 1) ?: "null"
                val isLinked = (value to nextValue) in links
                Text(
                    if (isLinked) "─►" else "···",
                    color = if (isLinked && (value in highlightedNodes || nextValue in highlightedNodes)) BlueAccent else TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .bringIntoViewRequester(requesters.getValue("null"))
                    .clip(RoundedCornerShape(8.dp))
                    .background(if ("null" in highlightedNodes) Color(0xFF092317) else DarkCardBgColor)
                    .border(1.dp, if ("null" in highlightedNodes) GreenAccent else CardBorderColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 9.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("null", color = if ("null" in highlightedNodes) GreenAccent else TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            detachedNodes.forEach { value ->
                LinkedListOperationNode(
                    value = value,
                    nextTarget = links.firstOrNull { it.first == value }?.second ?: "—",
                    badge = nodeBadges[value] ?: "DETACHED",
                    highlighted = value in highlightedNodes,
                    requester = requesters.getValue(value)
                )
            }
        }
    }
}

@Composable
private fun LinkedListOperationNode(
    value: String,
    nextTarget: String,
    badge: String?,
    highlighted: Boolean,
    requester: BringIntoViewRequester
) {
    Column(
        modifier = Modifier.bringIntoViewRequester(requester),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Box(modifier = Modifier.height(13.dp), contentAlignment = Alignment.Center) {
            if (badge != null) Text(badge, color = if (badge == "REMOVED") RedAccent else OrangeAccent, fontSize = 7.sp, fontWeight = FontWeight.Bold)
        }
        Surface(
            color = if (highlighted) Color(0xFF2A1C12) else DarkCardBgColor,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(if (highlighted) 1.5.dp else 1.dp, if (highlighted) OrangeAccent else CardBorderColor)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LinkedListNodeField("DATA", value)
                Box(Modifier.width(1.dp).height(22.dp).background(CardBorderColor))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("NEXT", color = TextSecondary, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                    Text(nextTarget, color = if (nextTarget == "—") TextSecondary else BlueAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun LinkedListStepExplanation(title: String, explanation: String, stepIndex: Int, stepCount: Int) {
    Surface(
        color = Color(0xFF13131C),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(11.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(title, color = OrangeAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(explanation, color = TextPrimary, fontSize = 12.sp, lineHeight = 17.sp)
            Text("STEP ${stepIndex + 1} / $stepCount", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun LinkedListPlaybackControls(
    stepIndex: Int,
    stepCount: Int,
    isPlaying: Boolean,
    onReset: () -> Unit,
    onPrevious: () -> Unit,
    onTogglePlayback: () -> Unit,
    onNext: () -> Unit
) {
    val isCompleted = stepIndex == stepCount - 1
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(
            color = DarkCardBgColor,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, CardBorderColor),
            modifier = Modifier.size(40.dp).clickable { onReset() }
        ) { Box(contentAlignment = Alignment.Center) { Text("↻", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold) } }
        Surface(
            color = DarkCardBgColor,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, CardBorderColor),
            modifier = Modifier.size(40.dp).clickable(enabled = stepIndex > 0) { onPrevious() }
        ) { Box(contentAlignment = Alignment.Center) { Text("‹", color = if (stepIndex > 0) TextPrimary else TextSecondary.copy(alpha = 0.3f), fontSize = 20.sp, fontWeight = FontWeight.Bold) } }
        Surface(
            color = if (isCompleted) Color(0xFF22222E) else OrangeAccent,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f).height(40.dp).clickable(enabled = !isCompleted) { onTogglePlayback() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(if (isPlaying) "⏸" else "▶", color = if (isCompleted) TextSecondary else Color(0xFF111115), fontSize = 12.sp)
                    Text(if (isPlaying) "Pause" else "Play", color = if (isCompleted) TextSecondary else Color(0xFF111115), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Surface(
            color = DarkCardBgColor,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, CardBorderColor),
            modifier = Modifier.size(40.dp).clickable(enabled = stepIndex < stepCount - 1) { onNext() }
        ) { Box(contentAlignment = Alignment.Center) { Text("›", color = if (stepIndex < stepCount - 1) TextPrimary else TextSecondary.copy(alpha = 0.3f), fontSize = 20.sp, fontWeight = FontWeight.Bold) } }
    }
}

@Composable
private fun LinearSearchVisualizationCard() {
    val arrayData = listOf("4", "8", "2", "7", "5")
    val target = 7
    val totalItems = arrayData.size

    var currentIndex by remember { mutableStateOf(-1) }
    var isPlaying by remember { mutableStateOf(false) }

    val isFound = currentIndex in arrayData.indices && arrayData[currentIndex].toInt() == target
    val isFinished = isFound || currentIndex >= totalItems
    val inspectedCount = if (currentIndex < 0) 0 else (currentIndex + 1).coerceAtMost(totalItems)

    // Auto-play timer mechanism
    LaunchedEffect(isPlaying, currentIndex) {
        if (isPlaying && !isFinished) {
            delay(1000L)
            if (currentIndex < 0) {
                currentIndex = 0
            } else if (!isFinished && currentIndex < totalItems - 1) {
                currentIndex++
            } else {
                isPlaying = false
            }
        } else if (isFinished) {
            isPlaying = false
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
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LINEAR SEARCH",
                    color = OrangeAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )

                val badgeText = when {
                    currentIndex < 0 -> "TARGET: $target"
                    isFound -> "FOUND AT INDEX $currentIndex ✓"
                    currentIndex >= totalItems -> "NOT FOUND"
                    else -> "STEP ${currentIndex + 1} OF $totalItems"
                }

                val badgeColor = when {
                    isFound -> GreenAccent
                    currentIndex >= totalItems -> RedAccent
                    else -> BlueAccent
                }

                val badgeBg = when {
                    isFound -> Color(0xFF092317)
                    currentIndex >= totalItems -> Color(0xFF210C0D)
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
            }

            // Array Cells Row (Indices 0..4)
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
                                .size(46.dp)
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
                                text = value,
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
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val explanationText = when {
                        currentIndex < 0 -> "Target = $target. Inspect elements sequentially from index 0 until found."
                        isFound -> "Target $target found at index $currentIndex! Linear Search complete. ✓"
                        currentIndex >= totalItems -> "Target $target not found in array after inspecting all $totalItems items."
                        else -> "Checking index $currentIndex: value ${arrayData[currentIndex]} != $target. Move to next index."
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
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFF262634))
                        ) {
                            val progressFloat = (inspectedCount.toFloat() / totalItems).coerceIn(0f, 1f)
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
                    color = if (isFinished) Color(0xFF22222E) else OrangeAccent,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
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
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                        .size(44.dp)
                        .clickable(enabled = !isFinished) {
                            isPlaying = false
                            if (currentIndex < 0) {
                                currentIndex = 0
                            } else if (!isFinished) {
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

@Composable
private fun BinarySearchVisualizationCard() {
    val arrayData = listOf("1", "3", "5", "7", "9", "11", "13")
    val target = 11
    val totalSteps = 2

    var stepIndex by remember { mutableStateOf(-1) } // -1: READY, 0: Step 1, 1: Step 2
    var isPlaying by remember { mutableStateOf(false) }

    val isCompleted = stepIndex >= totalSteps - 1
    val isFound = stepIndex == 1

    // Step parameters: (Left, Right, Mid)
    val (left, right, mid) = when (stepIndex) {
        0 -> Triple(0, 6, 3)
        1 -> Triple(4, 6, 5)
        else -> Triple(0, 6, -1)
    }

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
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BINARY SEARCH",
                    color = OrangeAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )

                val badgeText = when {
                    stepIndex < 0 -> "TARGET: $target (SORTED)"
                    isFound -> "FOUND AT INDEX 5 ✓"
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
            }

            // Array Cells Row with L, M, R indicators & Index labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                arrayData.forEachIndexed { index, value ->
                    val isMid = stepIndex >= 0 && index == mid
                    val isLeft = stepIndex >= 0 && index == left
                    val isRight = stepIndex >= 0 && index == right
                    val isEliminated = stepIndex >= 0 && (index < left || index > right)

                    val pointerLabel = when {
                        isMid -> "M"
                        isLeft && isRight -> "L,R"
                        isLeft -> "L"
                        isRight -> "R"
                        else -> " "
                    }

                    val cellBg = when {
                        isFound && isMid -> Color(0xFF092317)
                        isMid -> Color(0xFF2A1C12)
                        isEliminated -> Color(0xFF0F0F14)
                        else -> Color(0xFF13131C)
                    }

                    val borderColor = when {
                        isFound && isMid -> GreenAccent
                        isMid -> OrangeAccent
                        isEliminated -> Color(0xFF1E1E26)
                        else -> CardBorderColor
                    }

                    val textColor = when {
                        isFound && isMid -> GreenAccent
                        isMid -> OrangeAccent
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
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(cellBg)
                                .border(
                                    width = if (isMid) 1.5.dp else 1.dp,
                                    color = borderColor,
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = value,
                                color = textColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = index.toString(),
                            color = if (isFound && isMid) GreenAccent else if (isMid) OrangeAccent else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = if (isMid) FontWeight.Bold else FontWeight.Normal
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
                    val explanationText = when {
                        stepIndex < 0 -> "Target = $target (Sorted array). Compare midpoint with target to eliminate half."
                        stepIndex == 0 -> "Mid = index 3 (val 7) < 11. Target is greater → Eliminate left half [1, 3, 5, 7]."
                        else -> "Mid = index 5 (val 11) == 11. Target 11 found at index 5! Binary Search complete. ✓"
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
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFF262634))
                        ) {
                            val progressFloat = if (stepIndex < 0) 0f else (stepIndex + 1) / 2f
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
                        .size(44.dp)
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
                        .height(44.dp)
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

@Composable
private fun CheckYourUnderstandingSection(
    statement: String = "What is the index of the first element in a typical array?",
    options: List<String> = listOf("0", "1", "-1", "2"),
    correctIndex: Int = 0,
    explanation: String = "Arrays use zero-based indexing, so the first element is always stored at index 0.",
    selectedOption: Int?,
    onSelectOption: (Int) -> Unit,
    questionType: QuestionType = QuestionType.MULTIPLE_CHOICE,
    questionNumber: Int = 1,
    questionCount: Int = 1,
    showProgress: Boolean = false,
    onPreviousQuestion: (() -> Unit)? = null,
    onNextQuestion: (() -> Unit)? = null
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

            if (showProgress) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuestionNavigationArrow(
                        symbol = "‹",
                        enabled = questionNumber > 1,
                        onClick = { onPreviousQuestion?.invoke() }
                    )
                    Text(
                        text = "Question $questionNumber of $questionCount · ${when (questionType) {
                            QuestionType.TRUE_FALSE -> "TRUE / FALSE"
                            QuestionType.CODE_OUTPUT -> "CODE OUTPUT"
                            QuestionType.MULTIPLE_CHOICE -> "MULTIPLE CHOICE"
                        }}",
                        color = TextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    QuestionNavigationArrow(
                        symbol = "›",
                        enabled = questionNumber < questionCount,
                        onClick = { onNextQuestion?.invoke() }
                    )
                }
            }

            if (questionType == QuestionType.CODE_OUTPUT) {
                MarkdownContent(content = statement)
            } else {
                Text(
                    text = statement,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp
                )
            }

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
                            .clickable { onSelectOption(index) }
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
private fun QuestionNavigationArrow(
    symbol: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (enabled) Color(0xFF22222C) else Color(0xFF13131C),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (enabled) CardBorderColor else Color(0xFF202027)),
        modifier = Modifier
            .size(32.dp)
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = symbol,
                color = if (enabled) TextPrimary else TextSecondary.copy(alpha = 0.45f),
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 22.sp
            )
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
    var hasEntered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { hasEntered = true }
    val entranceProgress by animateFloatAsState(
        targetValue = if (hasEntered) 1f else 0f,
        animationSpec = tween(durationMillis = 240)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF2111115))
            .clickable(enabled = false) {}
            .padding(horizontal = 18.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .graphicsLayer {
                    alpha = entranceProgress
                    translationY = 10.dp.toPx() * (1f - entranceProgress)
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // Center Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(9.dp)
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
                        modifier = Modifier.size(68.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Surface(
                                color = Color.Transparent,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(2.dp, OrangeAccent),
                                modifier = Modifier.size(36.dp)
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

                Spacer(modifier = Modifier.height(2.dp))

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
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = lessonTitle,
                    color = TextSecondary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                // XP Reward Card
                Surface(
                    color = Color(0xFF1E1713),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, OrangeAccent.copy(alpha = 0.35f)),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 26.dp, vertical = 9.dp),
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
                            fontSize = 24.sp,
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
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                // Primary: Continue to next lesson
                Surface(
                    color = OrangeAccent,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 44.dp)
                        .clickable { onContinue() }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
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
                        .heightIn(min = 44.dp)
                        .clickable { onReview() }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
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
