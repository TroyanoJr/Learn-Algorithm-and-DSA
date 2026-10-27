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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learndsandalgorithm.domain.model.QuestionType
import com.example.learndsandalgorithm.presentation.components.MarkdownContent

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
                            "lesson_stack_1" -> StackVisualizationCard(StackDemonstration.FUNDAMENTALS, title = state.lesson.title)
                            "lesson_stack_2" -> StackVisualizationCard(StackDemonstration.OPERATIONS, title = state.lesson.title)
                            "lesson_stack_3" -> StackVisualizationCard(StackDemonstration.IMPLEMENTATIONS, title = state.lesson.title)
                            "lesson_stack_4" -> StackVisualizationCard(StackDemonstration.DELIMITER_MATCHING, title = state.lesson.title)
                            "lesson_queues_1" -> QueueVisualizationCard(QueueDemonstration.FUNDAMENTALS, title = state.lesson.title)
                            "lesson_queues_2" -> QueueVisualizationCard(QueueDemonstration.OPERATIONS, title = state.lesson.title)
                            "lesson_queues_3" -> QueueVisualizationCard(QueueDemonstration.IMPLEMENTATIONS, title = state.lesson.title)
                            "lesson_queues_4" -> QueueVisualizationCard(QueueDemonstration.CIRCULAR_BUFFER, title = state.lesson.title)
                            "lesson_arrays_1" -> ArrayVisualizationCard()
                            "lesson_arrays_2" -> ArrayResizeVisualizationCard()
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
