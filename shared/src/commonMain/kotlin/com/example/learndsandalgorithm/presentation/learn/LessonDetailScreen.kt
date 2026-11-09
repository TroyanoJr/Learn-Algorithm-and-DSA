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
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learndsandalgorithm.domain.model.QuestionType
import com.example.learndsandalgorithm.presentation.components.MarkdownContent
import com.example.learndsandalgorithm.presentation.theme.AppCardPadding
import com.example.learndsandalgorithm.presentation.theme.AppCornerRadius
import com.example.learndsandalgorithm.presentation.theme.AppIconSize
import com.example.learndsandalgorithm.presentation.theme.AppSpacing
import com.example.learndsandalgorithm.presentation.theme.AppTouchTarget
import com.example.learndsandalgorithm.presentation.theme.AppTypography

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
                .padding(horizontal = AppSpacing.large, vertical = AppSpacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium),
                modifier = Modifier.weight(1f)
            ) {
                // Back Button
                Box(
                    modifier = Modifier
                        .size(AppIconSize.boxSmall)
                        .clip(CircleShape)
                        .background(DarkCardBgColor)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "←",
                        color = TextPrimary,
                        style = AppTypography.cardTitle
                    )
                }

                Column {
                    val lessonOrder = (uiState as? LessonUiState.Success)?.lesson?.order ?: 1
                    Text(
                        text = "LESSON $lessonOrder",
                        style = AppTypography.sectionHeader,
                        color = OrangeAccent
                    )
                    Text(
                        text = (uiState as? LessonUiState.Success)?.lesson?.title ?: "Lesson Detail",
                        style = AppTypography.sectionTitle,
                        color = TextPrimary
                    )
                }
            }

            // XP and Duration Badges
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.extraSmall)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF22222E))
                        .padding(horizontal = AppSpacing.small, vertical = 4.dp)
                ) {
                    Text(
                        text = "4 min",
                        color = TextSecondary,
                        style = AppTypography.badge.copy(fontWeight = FontWeight.Medium)
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF2A1C12))
                        .padding(horizontal = AppSpacing.small, vertical = 4.dp)
                ) {
                    val xp = (uiState as? LessonUiState.Success)?.lesson?.xp ?: 30
                    Text(
                        text = "$xp XP",
                        color = OrangeAccent,
                        style = AppTypography.badge
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
                        .padding(AppSpacing.large),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
                    ) {
                        Text(
                            text = "Error Loading Lesson",
                            style = AppTypography.sectionTitle,
                            color = OrangeAccent
                        )
                        Text(
                            text = state.message,
                            style = AppTypography.bodySecondary,
                            color = TextSecondary
                        )
                        Surface(
                            color = OrangeAccent,
                            shape = RoundedCornerShape(AppCornerRadius.compact),
                            modifier = Modifier.clickable { viewModel.loadLesson(lessonId) }
                        ) {
                            Text(
                                text = "Retry",
                                color = DarkBgColor,
                                style = AppTypography.bodyPrimary.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = AppSpacing.large, vertical = AppSpacing.small)
                            )
                        }
                    }
                }
            }

            is LessonUiState.Success -> {
                var showCelebrationOverlay by remember { mutableStateOf(false) }
                var showVisualizerModal by remember { mutableStateOf(false) }
                var showQuestionsModal by remember { mutableStateOf(false) }

                val scrollState = rememberScrollState()

                LaunchedEffect(lessonId) {
                    scrollState.scrollTo(0)
                    showCelebrationOverlay = false
                    showVisualizerModal = false
                    showQuestionsModal = false
                }

                var animProgress by remember { mutableStateOf(0f) }
                LaunchedEffect(lessonId) {
                    animProgress = 0f
                    animProgress = 1f
                }
                val transitionAlpha by animateFloatAsState(
                    targetValue = animProgress,
                    animationSpec = tween(300)
                )
                val transitionTranslation by animateFloatAsState(
                    targetValue = (1f - animProgress) * 40f,
                    animationSpec = tween(300)
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            alpha = transitionAlpha
                            translationX = transitionTranslation
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = AppSpacing.large, vertical = AppSpacing.medium),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
                    ) {
                        // Theory & Key Concepts Markdown Content
                        MarkdownContent(content = state.markdownContent)

                        // 1. Independent Interactive Visualizer Section (When lesson has visualizer)
                        if (hasVisualizerForLesson(state.lesson.id)) {
                            Surface(
                                color = DarkCardBgColor,
                                shape = RoundedCornerShape(AppCornerRadius.card),
                                border = BorderStroke(1.dp, CardBorderColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(AppCardPadding.standard),
                                    verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "INTERACTIVE VISUALIZER",
                                            style = AppTypography.sectionHeader,
                                            color = OrangeAccent
                                        )
                                        Surface(
                                            color = Color(0xFF22222E),
                                            shape = RoundedCornerShape(AppCornerRadius.compact)
                                        ) {
                                            Text(
                                                text = "Interactive",
                                                color = TextSecondary,
                                                style = AppTypography.badge.copy(fontWeight = FontWeight.Medium),
                                                modifier = Modifier.padding(horizontal = AppSpacing.small, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Interactive Data Structure Visualization",
                                        style = AppTypography.cardTitle,
                                        color = TextPrimary
                                    )

                                    Text(
                                        text = "Step through algorithm operations interactively with custom trace controls and step-by-step state logs.",
                                        style = AppTypography.bodySecondary,
                                        color = TextSecondary
                                    )

                                    Surface(
                                        color = OrangeAccent,
                                        shape = RoundedCornerShape(AppCornerRadius.card),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = AppTouchTarget.buttonHeight)
                                            .clickable { showVisualizerModal = true }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = AppSpacing.medium),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Launch Visualizer ➔",
                                                color = Color(0xFF111115),
                                                style = AppTypography.cardTitle
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 2. Independent Lesson Questions Section (When lesson has questions)
                        if (state.questions.isNotEmpty()) {
                            val answeredCount = state.answeredQuestionIds.size
                            val totalQuestions = state.questions.size
                            val cardBorder = when {
                                state.completionBlocked -> BorderStroke(1.5.dp, RedAccent)
                                state.allQuestionsAnswered -> BorderStroke(1.dp, GreenAccent)
                                else -> BorderStroke(1.dp, CardBorderColor)
                            }

                            var wasAllAnswered by remember { mutableStateOf(state.allQuestionsAnswered) }
                            var playCompletionPulse by remember { mutableStateOf(false) }

                            LaunchedEffect(state.allQuestionsAnswered) {
                                if (state.allQuestionsAnswered && !wasAllAnswered) {
                                    playCompletionPulse = true
                                }
                                wasAllAnswered = state.allQuestionsAnswered
                            }

                            val badgeScale by animateFloatAsState(
                                targetValue = if (playCompletionPulse) 1.22f else 1.0f,
                                animationSpec = tween(durationMillis = 320),
                                finishedListener = { playCompletionPulse = false }
                            )

                            Surface(
                                color = DarkCardBgColor,
                                shape = RoundedCornerShape(AppCornerRadius.card),
                                border = cardBorder,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(AppCardPadding.standard),
                                    verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "LESSON QUESTIONS",
                                            style = AppTypography.sectionHeader,
                                            color = OrangeAccent
                                        )
                                        Surface(
                                            color = if (state.allQuestionsAnswered) Color(0xFF092317) else Color(0xFF2A1C12),
                                            shape = RoundedCornerShape(AppCornerRadius.compact),
                                            border = BorderStroke(1.dp, if (state.allQuestionsAnswered) GreenAccent else OrangeAccent),
                                            modifier = Modifier.graphicsLayer {
                                                scaleX = badgeScale
                                                scaleY = badgeScale
                                            }
                                        ) {
                                            Text(
                                                text = if (state.allQuestionsAnswered) "Completed ✓" else "$answeredCount of $totalQuestions Answered",
                                                color = if (state.allQuestionsAnswered) GreenAccent else OrangeAccent,
                                                style = AppTypography.badge,
                                                modifier = Modifier.padding(horizontal = AppSpacing.small, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Check Your Understanding",
                                        style = AppTypography.cardTitle,
                                        color = TextPrimary
                                    )

                                    if (state.completionBlocked) {
                                        Surface(
                                            color = Color(0xFF210C0D),
                                            shape = RoundedCornerShape(AppCornerRadius.compact),
                                            border = BorderStroke(1.dp, RedAccent),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Please answer all $totalQuestions questions before completing the lesson.",
                                                color = RedAccent,
                                                style = AppTypography.caption.copy(fontWeight = FontWeight.Bold),
                                                modifier = Modifier.padding(horizontal = AppSpacing.small, vertical = 6.dp)
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "Test your knowledge with multiple-choice and code output questions.",
                                            style = AppTypography.bodySecondary,
                                            color = TextSecondary
                                        )
                                    }

                                    Surface(
                                        color = if (state.allQuestionsAnswered) GreenAccent else OrangeAccent,
                                        shape = RoundedCornerShape(AppCornerRadius.card),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = AppTouchTarget.buttonHeight)
                                            .clickable { showQuestionsModal = true }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = AppSpacing.medium),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (state.allQuestionsAnswered) "Review Questions ➔" else "Start Quiz ➔",
                                                color = Color(0xFF111115),
                                                style = AppTypography.cardTitle
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Finish Lesson Section with Next Lesson capability
                        FinishLessonSection(
                            isCompleted = isLessonCompleted,
                            nextLessonId = state.nextLessonId,
                            onComplete = {
                                viewModel.markLessonCompleted()
                                val latestState = viewModel.uiState.value
                                if (latestState is LessonUiState.Success && latestState.isCompleted) {
                                    isLessonCompleted = true
                                    showCelebrationOverlay = true
                                }
                            },
                            onNavigateToNextLesson = onNavigateToLesson,
                            onBackToTopic = onBack
                        )

                        Spacer(modifier = Modifier.height(AppSpacing.large))
                    }

                    // Direct Interactive Fullscreen Visualizer Dialog
                    if (showVisualizerModal) {
                        CompositionLocalProvider(
                            LocalAutoExpandVisualizer provides true,
                            LocalVisualizerClose provides { showVisualizerModal = false }
                        ) {
                            RenderVisualizerCardForLesson(lessonId = state.lesson.id, title = state.lesson.title)
                        }
                    }

                    // Independent Lesson Questions Quiz Window
                    if (showQuestionsModal && state.questions.isNotEmpty()) {
                        val hasQuestionSequence = state.questions.size > 1
                        val questionIndex = if (hasQuestionSequence) {
                            lessonQuestionIndex.coerceIn(0, state.questions.lastIndex)
                        } else {
                            0
                        }
                        val question = state.questions[questionIndex]

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(DarkBgColor)
                                .clickable(enabled = false) {}
                                .padding(AppSpacing.medium),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(AppCornerRadius.cardLarge))
                                    .background(DarkCardBgColor)
                                    .border(1.dp, CardBorderColor, RoundedCornerShape(AppCornerRadius.cardLarge))
                                    .padding(AppCardPadding.standard),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Top Header Bar
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "LESSON QUIZ",
                                            style = AppTypography.sectionHeader,
                                            color = OrangeAccent
                                        )
                                        Text(
                                            text = "Question ${questionIndex + 1} of ${state.questions.size}",
                                            style = AppTypography.cardTitle,
                                            color = TextPrimary
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(AppIconSize.boxSmall)
                                            .clip(CircleShape)
                                            .background(Color(0xFF22222E))
                                            .clickable { showQuestionsModal = false },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "✕",
                                            style = AppTypography.bodyPrimary.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                    }
                                }

                                // Scrollable Center Question Content
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .verticalScroll(rememberScrollState())
                                        .padding(vertical = AppSpacing.small)
                                ) {
                                    CheckYourUnderstandingSection(
                                        statement = question.statement,
                                        options = question.options,
                                        correctIndex = question.correctOptionIndex,
                                        explanation = question.explanation,
                                        selectedOption = state.displayedSelectedOptions[question.id],
                                        onSelectOption = { displayedOptionIdx ->
                                            viewModel.selectQuestionAnswer(question.id, displayedOptionIdx)
                                        },
                                        questionType = question.type,
                                        questionNumber = questionIndex + 1,
                                        questionCount = if (hasQuestionSequence) state.questions.size else 1,
                                        showProgress = false,
                                        onPreviousQuestion = null,
                                        onNextQuestion = null
                                    )
                                }

                                // Bottom Navigation Bar
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = if (questionIndex > 0) Color(0xFF22222E) else Color(0xFF14141A),
                                        shape = RoundedCornerShape(AppCornerRadius.compact),
                                        border = BorderStroke(1.dp, if (questionIndex > 0) CardBorderColor else Color(0xFF1E1E28)),
                                        modifier = Modifier
                                            .heightIn(min = AppTouchTarget.buttonHeight)
                                            .clickable(enabled = questionIndex > 0) {
                                                lessonQuestionIndex = (questionIndex - 1).coerceAtLeast(0)
                                            }
                                    ) {
                                        Text(
                                            text = "‹ Previous",
                                            color = if (questionIndex > 0) TextPrimary else TextSecondary.copy(alpha = 0.4f),
                                            style = AppTypography.bodySecondary.copy(fontWeight = FontWeight.Bold),
                                            modifier = Modifier.padding(horizontal = AppSpacing.large, vertical = AppSpacing.small)
                                        )
                                    }

                                    Surface(
                                        color = OrangeAccent,
                                        shape = RoundedCornerShape(AppCornerRadius.compact),
                                        modifier = Modifier
                                            .heightIn(min = AppTouchTarget.buttonHeight)
                                            .clickable {
                                                if (questionIndex < state.questions.lastIndex) {
                                                    lessonQuestionIndex = (questionIndex + 1).coerceAtMost(state.questions.lastIndex)
                                                } else {
                                                    showQuestionsModal = false
                                                }
                                            }
                                    ) {
                                        Text(
                                            text = if (questionIndex < state.questions.lastIndex) "Next Question ›" else "Finish Review ➔",
                                            color = Color(0xFF111115),
                                            style = AppTypography.bodySecondary.copy(fontWeight = FontWeight.Bold),
                                            modifier = Modifier.padding(horizontal = AppSpacing.large, vertical = AppSpacing.small)
                                        )
                                    }
                                }
                            }
                        }
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
        shape = RoundedCornerShape(AppCornerRadius.card),
        border = BorderStroke(1.dp, CardBorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppCardPadding.standard),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
        ) {
            Text(
                text = "CHECK YOUR UNDERSTANDING",
                style = AppTypography.sectionHeader,
                color = OrangeAccent
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
                        style = AppTypography.badge,
                        color = TextSecondary,
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
                    style = AppTypography.cardTitle,
                    color = TextPrimary
                )
            }

            // Options list
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.extraSmall)) {
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
                        shape = RoundedCornerShape(AppCornerRadius.compact),
                        border = BorderStroke(1.dp, style.borderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = AppTouchTarget.minimum)
                            .clickable { onSelectOption(index) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppSpacing.medium, vertical = AppSpacing.small),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.extraSmall)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = optionText,
                                    color = style.textColor,
                                    style = AppTypography.bodyPrimary.copy(fontWeight = FontWeight.SemiBold),
                                    modifier = Modifier.weight(1f)
                                )
                                if (style.iconStr != null) {
                                    Text(
                                        text = style.iconStr,
                                        color = style.textColor,
                                        style = AppTypography.cardTitle
                                    )
                                }
                            }

                            if (isSelected) {
                                Text(
                                    text = explanation,
                                    color = style.textColor.copy(alpha = 0.9f),
                                    style = AppTypography.caption,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
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
        shape = RoundedCornerShape(AppCornerRadius.compact),
        border = BorderStroke(1.dp, if (enabled) CardBorderColor else Color(0xFF202027)),
        modifier = Modifier
            .size(AppIconSize.boxSmall)
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = symbol,
                color = if (enabled) TextPrimary else TextSecondary.copy(alpha = 0.45f),
                style = AppTypography.headerTitle,
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
            verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
        ) {
            Surface(
                color = Color(0xFF092317),
                shape = RoundedCornerShape(AppCornerRadius.card),
                border = BorderStroke(1.dp, GreenAccent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppCardPadding.standard),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium)
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
                            style = AppTypography.bodyPrimary.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Column {
                        Text(
                            text = "Lesson Completed",
                            color = GreenAccent,
                            style = AppTypography.bodyPrimary.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Great job! You've mastered this section.",
                            color = TextSecondary,
                            style = AppTypography.caption
                        )
                    }
                }
            }

            // Next Lesson or Back to Topic Button
            Surface(
                color = OrangeAccent,
                shape = RoundedCornerShape(AppCornerRadius.card),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = AppTouchTarget.buttonHeight)
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
                        .padding(vertical = AppSpacing.medium),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (nextLessonId != null) "Next Lesson →" else "Back to Topic →",
                        color = Color(0xFF111115),
                        style = AppTypography.cardTitle
                    )
                }
            }
        }
    } else {
        Surface(
            color = OrangeAccent,
            shape = RoundedCornerShape(AppCornerRadius.card),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = AppTouchTarget.buttonHeight)
                .clickable { onComplete() }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = AppSpacing.medium),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Complete Lesson ➔",
                    color = Color(0xFF111115),
                    style = AppTypography.cardTitle
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
            .padding(horizontal = AppSpacing.large, vertical = AppSpacing.medium),
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
            Spacer(modifier = Modifier.height(AppSpacing.extraSmall))

            // Center Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
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
                                shape = RoundedCornerShape(AppCornerRadius.card),
                                border = BorderStroke(2.dp, OrangeAccent),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "✓",
                                        color = OrangeAccent,
                                        style = AppTypography.displayTitle
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "LESSON COMPLETE",
                    style = AppTypography.sectionHeader,
                    color = OrangeAccent
                )

                Text(
                    text = "Lesson completed!",
                    style = AppTypography.displayTitle,
                    color = TextPrimary
                )

                Text(
                    text = lessonTitle,
                    style = AppTypography.bodyPrimary.copy(fontWeight = FontWeight.Medium),
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(AppSpacing.extraSmall))

                // XP Reward Card
                Surface(
                    color = Color(0xFF1E1713),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, OrangeAccent.copy(alpha = 0.35f)),
                    modifier = Modifier.padding(horizontal = AppSpacing.large)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 26.dp, vertical = AppSpacing.small),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.extraSmall)
                    ) {
                        Text(
                            text = "LESSON REWARD",
                            style = AppTypography.sectionHeader,
                            color = TextSecondary
                        )
                        Text(
                            text = "+$xpAmount XP",
                            style = AppTypography.displayTitle,
                            color = OrangeAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.extraSmall))

                // Saved Progress Confirmation Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
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
                            style = AppTypography.badge
                        )
                    }
                    Text(
                        text = "Your progress has been saved.",
                        style = AppTypography.bodySecondary,
                        color = Color(0xFFD1D5DB)
                    )
                }
            }

            // Bottom Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
            ) {
                // Primary: Continue to next lesson
                Surface(
                    color = OrangeAccent,
                    shape = RoundedCornerShape(AppCornerRadius.card),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = AppTouchTarget.buttonHeight)
                        .clickable { onContinue() }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppSpacing.medium),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (nextLessonId != null) "Continue to next lesson →" else "Back to Topic →",
                            color = Color(0xFF111115),
                            style = AppTypography.cardTitle
                        )
                    }
                }

                // Secondary: Review lesson
                Surface(
                    color = DarkCardBgColor,
                    shape = RoundedCornerShape(AppCornerRadius.card),
                    border = BorderStroke(1.dp, CardBorderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = AppTouchTarget.buttonHeight)
                        .clickable { onReview() }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppSpacing.medium),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Review lesson",
                            color = TextPrimary,
                            style = AppTypography.cardTitle
                        )
                    }
                }
            }
        }
    }
}

private fun hasVisualizerForLesson(lessonId: String): Boolean {
    return when (lessonId) {
        "lesson_introduction_ds_1",
        "lesson_introduction_ds_2",
        "lesson_introduction_algo_1",
        "lesson_introduction_algo_2",
        "searching_1",
        "sorting_1" -> false
        else -> true
    }
}

@Composable
private fun RenderVisualizerCardForLesson(lessonId: String, title: String) {
    when (lessonId) {
        "lesson_stack_1" -> StackVisualizationCard(StackDemonstration.FUNDAMENTALS, title = title)
        "lesson_stack_2" -> StackVisualizationCard(StackDemonstration.OPERATIONS, title = title)
        "lesson_stack_3" -> StackVisualizationCard(StackDemonstration.IMPLEMENTATIONS, title = title)
        "lesson_stack_4" -> StackVisualizationCard(StackDemonstration.DELIMITER_MATCHING, title = title)
        "lesson_queues_1" -> QueueVisualizationCard(QueueDemonstration.FUNDAMENTALS, title = title)
        "lesson_queues_2" -> QueueVisualizationCard(QueueDemonstration.OPERATIONS, title = title)
        "lesson_queues_3" -> QueueVisualizationCard(QueueDemonstration.IMPLEMENTATIONS, title = title)
        "lesson_queues_4" -> QueueVisualizationCard(QueueDemonstration.CIRCULAR_BUFFER, title = title)
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
        "lesson_hash_tables_1" -> HashTableVisualizationCard(title = title)
        "lesson_hash_tables_2" -> HashTableCollisionVisualizationCard(title = title)
        "lesson_hash_tables_3" -> HashTableOperationsVisualizationCard(title = title)
        "lesson_hash_tables_4" -> HashTableApplicationsVisualizationCard(title = title)
        "lesson_divide_conquer_1" -> DivideConquerFundamentalsVisualizationCard(title = title)
        "lesson_divide_conquer_2" -> RecursiveBinarySearchVisualizationCard(title = title)
        "lesson_divide_conquer_3" -> MergeSortFundamentalsVisualizationCard(title = title)
        "lesson_divide_conquer_4" -> MergeSortFullVisualizationCard(title = title)
        "lesson_divide_conquer_5" -> QuickSortPartitionVisualizationCard(title = title)
        "lesson_divide_conquer_6" -> DivideConquerTradeoffsVisualizationCard(title = title)
        "lesson_trees_1" -> TreeFundamentalsVisualizationCard(title = title)
        "lesson_trees_2" -> BinaryTreeBstVisualizationCard(title = title)
        "lesson_trees_3" -> TreeTraversalsVisualizationCard(title = title)
        "lesson_trees_4" -> BstOperationsVisualizationCard(title = title)
        "lesson_heap_1" -> HeapPropertiesVisualizationCard(title = title)
        "lesson_heap_2" -> HeapInsertSiftUpVisualizationCard(title = title)
        "lesson_heap_3" -> HeapExtractSiftDownVisualizationCard(title = title)
        "lesson_heap_4" -> HeapBuildVisualizationCard(title = title)
        "lesson_heap_5" -> HeapSortVisualizationCard(title = title)
        "lesson_graphs_1" -> GraphFundamentalsVisualizationCard(title = title)
        "lesson_graphs_2" -> GraphRepresentationsVisualizationCard(title = title)
        "lesson_graphs_3" -> GraphBfsVisualizationCard(title = title)
        "lesson_graphs_4" -> GraphDfsVisualizationCard(title = title)
        "lesson_graphs_5" -> GraphComponentsCycleVisualizationCard(title = title)
        "lesson_graphs_6" -> GraphUnweightedShortestPathVisualizationCard(title = title)
        "lesson_graphs_7" -> DijkstraVisualizationCard(title = title)
        "lesson_graphs_8" -> TopologicalSortVisualizationCard(title = title)
        "lesson_brute_force_1" -> BruteForceFundamentalsVisualizationCard(title = title)
        "lesson_brute_force_2" -> BruteForcePairProblemsVisualizationCard(title = title)
        "lesson_brute_force_3" -> NaiveStringMatchingVisualizationCard(title = title)
        "lesson_greedy_1" -> GreedyFundamentalsVisualizationCard(title = title)
        "lesson_greedy_2" -> GreedyCoinChangeVisualizationCard(title = title)
        "lesson_greedy_3" -> ActivitySelectionVisualizationCard(title = title)
        "lesson_greedy_4" -> FractionalKnapsackVisualizationCard(title = title)
        "lesson_greedy_5" -> GreedyCounterexamplesVisualizationCard(title = title)
        "lesson_backtracking_1" -> BacktrackingFundamentalsVisualizationCard(title = title)
        "lesson_backtracking_2" -> PermutationsVisualizationCard(title = title)
        "lesson_backtracking_3" -> CombinationsPruningVisualizationCard(title = title)
        "lesson_backtracking_4" -> NQueensVisualizationCard(title = title)
        "lesson_backtracking_5" -> GridPathfindingVisualizationCard(title = title)
        "lesson_backtracking_6" -> BacktrackingComplexityVisualizationCard(title = title)
        "lesson_dynamic_programming_1" -> DpMemoizationVisualizationCard(title = title)
        "lesson_dynamic_programming_2" -> DpTabulationVisualizationCard(title = title)
        "lesson_dynamic_programming_3" -> CoinChangeDpVisualizationCard(title = title)
        "lesson_dynamic_programming_4" -> UniquePathsDpVisualizationCard(title = title)
        "lesson_dynamic_programming_5" -> Knapsack01DpVisualizationCard(title = title)
        "lesson_dynamic_programming_6" -> LcsDpVisualizationCard(title = title)
        "lesson_branch_bound_1" -> BranchBoundFundamentalsVisualizationCard(title = title)
        "lesson_branch_bound_2" -> BoundsPruningVisualizationCard(title = title)
        "lesson_branch_bound_3" -> BestFirstSearchVisualizationCard(title = title)
        "lesson_branch_bound_4" -> KnapsackBranchBoundVisualizationCard(title = title)
        "lesson_iteration_recursion_1" -> IterationFundamentalsVisualizationCard(title = title)
        "lesson_iteration_recursion_2" -> NestedIterationVisualizationCard(title = title)
        "lesson_iteration_recursion_3" -> RecursionFundamentalsVisualizationCard(title = title)
        "lesson_iteration_recursion_4" -> RecursionTradeoffsVisualizationCard(title = title)
        else -> ArrayVisualizationCard()
    }
}
