package com.example.learndsandalgorithm.presentation.practice

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learndsandalgorithm.domain.model.Question
import com.example.learndsandalgorithm.domain.model.QuestionType
import com.example.learndsandalgorithm.domain.model.TopicCategory
import com.example.learndsandalgorithm.domain.repository.ContentRepository
import com.example.learndsandalgorithm.presentation.components.MarkdownContent
import com.example.learndsandalgorithm.presentation.components.TopicIcon
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
private val PurpleAccent = Color(0xFF9E66FF)
private val GreenAccent = Color(0xFF10B981)
private val RedAccent = Color(0xFFEF4444)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9CA3AF)

private fun buildVectorIcon(name: String, block: PathBuilder.() -> Unit): ImageVector {
    return ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(
        fill = SolidColor(Color.White),
        pathBuilder = block
    ).build()
}

private val IconChevronLeft = buildVectorIcon("ChevronLeft") {
    moveTo(15.41f, 7.41f)
    lineTo(14f, 6f)
    lineTo(8f, 12f)
    lineTo(14f, 18f)
    lineTo(15.41f, 16.59f)
    lineTo(10.83f, 12f)
    close()
}

private val IconChevronRight = buildVectorIcon("ChevronRight") {
    moveTo(10f, 6f)
    lineTo(8.59f, 7.41f)
    lineTo(13.17f, 12f)
    lineTo(8.59f, 16.59f)
    lineTo(10f, 18f)
    lineTo(16f, 12f)
    close()
}

private val IconCheck = buildVectorIcon("Check") {
    moveTo(9f, 16.17f)
    lineTo(4.83f, 12f)
    lineTo(3.41f, 13.41f)
    lineTo(9f, 19f)
    lineTo(21f, 7f)
    lineTo(19.59f, 5.59f)
    close()
}

private val IconClose = buildVectorIcon("Close") {
    moveTo(19f, 6.41f)
    lineTo(17.59f, 5f)
    lineTo(12f, 10.59f)
    lineTo(6.41f, 5f)
    lineTo(5f, 6.41f)
    lineTo(10.59f, 12f)
    lineTo(5f, 17.59f)
    lineTo(6.41f, 19f)
    lineTo(12f, 13.41f)
    lineTo(17.59f, 19f)
    lineTo(19f, 17.59f)
    lineTo(13.41f, 12f)
    close()
}

internal data class QuizQuestionState(
    val id: String,
    val statement: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String,
    val type: QuestionType
)

private data class QuizTopicState(
    val id: String,
    val title: String,
    val category: String,
    val questionCount: Int,
    val questions: List<QuizQuestionState>
)

private enum class QuizFlowState {
    TOPIC_SELECTION,
    QUESTION,
    QUIZ_RESULT,
    QUIZ_REVIEW
}

internal fun prepareQuizQuestions(rawQuestions: List<Question>): List<QuizQuestionState> {
    return rawQuestions.map { q ->
        if (q.type == QuestionType.TRUE_FALSE) {
            require(q.options == listOf("True", "False")) {
                "True/False question '${q.id}' must have options [True, False] in that order."
            }
        }
        val indexedOptions = q.options.mapIndexed { index, text -> index to text }
        val shuffled = if (q.type == QuestionType.TRUE_FALSE) indexedOptions else indexedOptions.shuffled()
        val newCorrectIndex = shuffled.indexOfFirst { it.first == q.correctOptionIndex }

        QuizQuestionState(
            id = q.id,
            statement = q.statement,
            options = shuffled.map { it.second },
            correctOptionIndex = if (newCorrectIndex != -1) newCorrectIndex else q.correctOptionIndex,
            explanation = q.explanation,
            type = q.type
        )
    }
}

internal fun shuffleQuizQuestionOrder(
    questions: List<QuizQuestionState>,
    shuffleQuestionOrder: (List<QuizQuestionState>) -> List<QuizQuestionState> = { it.shuffled() }
): List<QuizQuestionState> = shuffleQuestionOrder(questions)

@Composable
fun TopicQuizFlow(
    contentRepository: ContentRepository,
    initialTopicId: String? = null,
    onBackToPractice: () -> Unit
) {
    var topics by remember { mutableStateOf<List<QuizTopicState>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var selectedTopicId by remember { mutableStateOf("arrays") }
    var activeTopic by remember { mutableStateOf<QuizTopicState?>(null) }

    var activeAttemptQuestions by remember { mutableStateOf<List<QuizQuestionState>>(emptyList()) }
    var activeAttemptSelectedAnswers by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    var flowState by remember { mutableStateOf(QuizFlowState.TOPIC_SELECTION) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var correctCount by remember { mutableStateOf(0) }
    var incorrectCount by remember { mutableStateOf(0) }

    fun resetAttempt() {
        activeAttemptQuestions = emptyList()
        activeAttemptSelectedAnswers = emptyMap()
        currentQuestionIndex = 0
        selectedOptionIndex = null
        correctCount = 0
        incorrectCount = 0
    }

    LaunchedEffect(initialTopicId) {
        isLoading = true
        val allTopics = contentRepository.getTopics()
        val loadedTopics = mutableListOf<QuizTopicState>()

        allTopics.forEach { topic ->
            val rawQuestions = contentRepository.getQuestionsByTopic(topic.id)
            if (rawQuestions.isNotEmpty()) {
                val preparedQuestions = prepareQuizQuestions(rawQuestions)
                val categoryName = if (topic.category == TopicCategory.DATA_STRUCTURES) "Data Structures" else "Algorithms"

                loadedTopics.add(
                    QuizTopicState(
                        id = topic.id,
                        title = topic.title,
                        category = categoryName,
                        questionCount = preparedQuestions.size,
                        questions = preparedQuestions
                    )
                )
            }
        }

        topics = loadedTopics
        if (loadedTopics.isNotEmpty()) {
            val initialTarget = if (initialTopicId != null) {
                loadedTopics.find { it.id == initialTopicId } ?: loadedTopics.first()
            } else {
                loadedTopics.first()
            }
            selectedTopicId = initialTarget.id

            if (initialTopicId != null) {
                val shuffled = shuffleQuizQuestionOrder(initialTarget.questions)
                activeTopic = initialTarget.copy(questions = shuffled)
                activeAttemptQuestions = shuffled
                activeAttemptSelectedAnswers = emptyMap()
                currentQuestionIndex = 0
                selectedOptionIndex = null
                correctCount = 0
                incorrectCount = 0
                flowState = QuizFlowState.QUESTION
            }
        }
        isLoading = false
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBgColor),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = OrangeAccent)
        }
    } else if (topics.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBgColor)
                .padding(AppSpacing.large),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
            ) {
                Text(
                    text = "No Quiz Topics Available",
                    style = AppTypography.sectionTitle,
                    color = OrangeAccent
                )
                Text(
                    text = "No quiz questions found in repository.",
                    style = AppTypography.bodySecondary,
                    color = TextSecondary
                )
                Surface(
                    color = OrangeAccent,
                    shape = RoundedCornerShape(AppCornerRadius.compact),
                    modifier = Modifier.clickable { resetAttempt(); onBackToPractice() }
                ) {
                    Text(
                        text = "Back to Practice",
                        color = DarkBgColor,
                        style = AppTypography.bodyPrimary.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = AppSpacing.large, vertical = AppSpacing.small)
                    )
                }
            }
        }
    } else {
        when (flowState) {
            QuizFlowState.TOPIC_SELECTION -> {
                TopicQuizSelectionScreen(
                    topics = topics,
                    selectedTopicId = selectedTopicId,
                    onSelectTopic = { selectedTopicId = it },
                    onStartQuizForTopic = { topic ->
                        val shuffled = shuffleQuizQuestionOrder(topic.questions)
                        activeTopic = topic.copy(questions = shuffled)
                        activeAttemptQuestions = shuffled
                        activeAttemptSelectedAnswers = emptyMap()
                        currentQuestionIndex = 0
                        selectedOptionIndex = null
                        correctCount = 0
                        incorrectCount = 0
                        flowState = QuizFlowState.QUESTION
                    },
                    onBack = {
                        resetAttempt()
                        onBackToPractice()
                    }
                )
            }

            QuizFlowState.QUESTION -> {
                val topic = activeTopic ?: topics.first()
                val question = topic.questions.getOrNull(currentQuestionIndex) ?: topic.questions.first()

                QuizQuestionScreen(
                    topicTitle = topic.title,
                    questionIndex = currentQuestionIndex,
                    totalQuestions = topic.questions.size,
                    question = question,
                    selectedOptionIndex = selectedOptionIndex,
                    isReviewMode = false,
                    onSelectOption = { index ->
                        if (selectedOptionIndex == null) {
                            selectedOptionIndex = index
                            activeAttemptSelectedAnswers = activeAttemptSelectedAnswers + (question.id to index)
                            if (index == question.correctOptionIndex) {
                                correctCount++
                            } else {
                                incorrectCount++
                            }
                        }
                    },
                    onNextQuestion = {
                        if (currentQuestionIndex + 1 < topic.questions.size) {
                            currentQuestionIndex++
                            selectedOptionIndex = null
                        } else {
                            flowState = QuizFlowState.QUIZ_RESULT
                        }
                    },
                    onBack = {
                        resetAttempt()
                        flowState = QuizFlowState.TOPIC_SELECTION
                    }
                )
            }

            QuizFlowState.QUIZ_RESULT -> {
                val topic = activeTopic ?: topics.first()
                val total = topic.questions.size
                val percentage = if (total > 0) (correctCount * 100) / total else 0

                QuizResultScreen(
                    topicTitle = topic.title,
                    correctCount = correctCount,
                    incorrectCount = incorrectCount,
                    totalQuestions = total,
                    percentage = percentage,
                    onTryAgain = {
                        val shuffled = shuffleQuizQuestionOrder(topic.questions)
                        activeTopic = topic.copy(questions = shuffled)
                        activeAttemptQuestions = shuffled
                        activeAttemptSelectedAnswers = emptyMap()
                        currentQuestionIndex = 0
                        selectedOptionIndex = null
                        correctCount = 0
                        incorrectCount = 0
                        flowState = QuizFlowState.QUESTION
                    },
                    onReviewAnswers = {
                        currentQuestionIndex = 0
                        flowState = QuizFlowState.QUIZ_REVIEW
                    },
                    onBackToTopicSelection = {
                        resetAttempt()
                        flowState = QuizFlowState.TOPIC_SELECTION
                    },
                    onBackToPractice = {
                        resetAttempt()
                        onBackToPractice()
                    }
                )
            }

            QuizFlowState.QUIZ_REVIEW -> {
                val topic = activeTopic ?: topics.first()
                val question = activeAttemptQuestions.getOrNull(currentQuestionIndex) ?: activeAttemptQuestions.first()
                val userSelectedIdx = activeAttemptSelectedAnswers[question.id]

                QuizQuestionScreen(
                    topicTitle = "${topic.title} (Review)",
                    questionIndex = currentQuestionIndex,
                    totalQuestions = activeAttemptQuestions.size,
                    question = question,
                    selectedOptionIndex = userSelectedIdx,
                    isReviewMode = true,
                    onSelectOption = { /* Read-only: no-op */ },
                    onNextQuestion = {
                        if (currentQuestionIndex + 1 < activeAttemptQuestions.size) {
                            currentQuestionIndex++
                        } else {
                            flowState = QuizFlowState.QUIZ_RESULT
                        }
                    },
                    onBack = { flowState = QuizFlowState.QUIZ_RESULT }
                )
            }
        }
    }
}

@Composable
private fun TopicQuizSelectionScreen(
    topics: List<QuizTopicState>,
    selectedTopicId: String,
    onSelectTopic: (String) -> Unit,
    onStartQuizForTopic: (QuizTopicState) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBgColor)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.large, vertical = AppSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium)
        ) {
            Box(
                modifier = Modifier
                    .size(AppIconSize.boxSmall)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkCardBgColor)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = IconChevronLeft, contentDescription = "Back", tint = TextPrimary, modifier = Modifier.size(16.dp))
            }
            Text(
                text = "TOPIC QUIZ",
                style = AppTypography.sectionHeader,
                color = OrangeAccent
            )
        }

        // Header Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "CHOOSE A TOPIC",
                    style = AppTypography.sectionHeader,
                    color = OrangeAccent
                )
                Spacer(modifier = Modifier.height(AppSpacing.extraSmall))
                Text(
                    text = "What do you want to practice?",
                    style = AppTypography.displayTitle,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Pick a focused set and sharpen one concept at a time.",
                    style = AppTypography.bodySecondary,
                    color = TextSecondary
                )
            }

            Box(
                modifier = Modifier
                    .size(AppIconSize.boxMedium)
                    .clip(CircleShape)
                    .background(Color(0xFF1E1E28))
                    .border(1.dp, CardBorderColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "DSA",
                    style = AppTypography.bodySecondary.copy(fontWeight = FontWeight.Bold),
                    color = OrangeAccent
                )
            }
        }

        // Topic List Cards
        topics.forEach { topic ->
            val isSelected = topic.id == selectedTopicId
            Surface(
                color = DarkCardBgColor,
                shape = RoundedCornerShape(AppCornerRadius.card),
                border = BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) OrangeAccent else CardBorderColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = AppTouchTarget.minimum)
                    .clickable {
                        onSelectTopic(topic.id)
                        onStartQuizForTopic(topic)
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppCardPadding.compact),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
                        modifier = Modifier.weight(1f)
                    ) {
                        TopicIcon(
                            topicId = topic.id,
                            containerSize = AppIconSize.boxSmall,
                            iconSize = 18.dp
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                            Text(
                                text = topic.title,
                                style = AppTypography.cardTitle,
                                color = TextPrimary
                            )
                            Text(
                                text = topic.category,
                                style = AppTypography.caption,
                                color = TextSecondary
                            )
                            Text(
                                text = "${topic.questionCount} questions",
                                style = AppTypography.badge,
                                color = TextPrimary
                            )
                        }
                    }

                    // Chevron action button
                    Box(
                        modifier = Modifier
                            .padding(start = AppSpacing.small)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF262634)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = IconChevronRight,
                            contentDescription = "Start topic quiz",
                            tint = TextPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizQuestionScreen(
    topicTitle: String,
    questionIndex: Int,
    totalQuestions: Int,
    question: QuizQuestionState,
    selectedOptionIndex: Int?,
    isReviewMode: Boolean = false,
    onSelectOption: (Int) -> Unit,
    onNextQuestion: () -> Unit,
    onBack: () -> Unit
) {
    val optionLabels = listOf("A", "B", "C", "D")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBgColor)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.large, vertical = AppSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium)
            ) {
                Box(
                    modifier = Modifier
                        .size(AppIconSize.boxSmall)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkCardBgColor)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = IconChevronLeft, contentDescription = "Back", tint = TextPrimary, modifier = Modifier.size(16.dp))
                }

                Column {
                    Text(
                        text = if (isReviewMode) "REVIEW ANSWERS" else "TOPIC QUIZ",
                        style = AppTypography.sectionHeader,
                        color = OrangeAccent
                    )
                    Text(
                        text = topicTitle,
                        style = AppTypography.sectionTitle,
                        color = TextPrimary
                    )
                }
            }

            Text(
                text = "${questionIndex + 1} / $totalQuestions",
                style = AppTypography.bodyPrimary.copy(fontWeight = FontWeight.Bold),
                color = OrangeAccent
            )
        }

        // Progress Bar
        val progressFloat = (questionIndex + 1).toFloat() / totalQuestions.toFloat()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF262634))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progressFloat)
                    .height(4.dp)
                    .background(OrangeAccent, RoundedCornerShape(2.dp))
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.extraSmall))

        // Question Statement
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.extraSmall)) {
            Text(
                text = "QUESTION ${questionIndex + 1} · ${question.type.name.replace('_', ' ')}",
                style = AppTypography.sectionHeader,
                color = OrangeAccent
            )
            if (question.type == QuestionType.CODE_OUTPUT) {
                MarkdownContent(content = question.statement)
            } else {
                Text(
                    text = question.statement,
                    style = AppTypography.sectionTitle,
                    color = TextPrimary
                )
            }
        }

        // Answer Option Cards
        question.options.forEachIndexed { index, optionText ->
            val isSelected = selectedOptionIndex == index
            val isCorrectIndex = index == question.correctOptionIndex
            val isAnswered = selectedOptionIndex != null

            val (borderColor, bgColor, letterBgColor, letterTextColor, iconVector, iconColor) = when {
                !isAnswered -> {
                    Tuple6(
                        CardBorderColor,
                        DarkCardBgColor,
                        Color(0xFF22222E),
                        TextSecondary,
                        null,
                        Color.Unspecified
                    )
                }
                isCorrectIndex -> {
                    Tuple6(
                        GreenAccent,
                        Color(0xFF092317),
                        GreenAccent,
                        Color(0xFF111115),
                        IconCheck,
                        GreenAccent
                    )
                }
                isSelected -> {
                    Tuple6(
                        RedAccent,
                        Color(0xFF210C0D),
                        RedAccent,
                        TextPrimary,
                        IconClose,
                        RedAccent
                    )
                }
                else -> {
                    Tuple6(
                        CardBorderColor,
                        DarkCardBgColor,
                        Color(0xFF22222E),
                        Color(0xFF4B5563),
                        null,
                        Color.Unspecified
                    )
                }
            }

            Surface(
                color = bgColor,
                shape = RoundedCornerShape(AppCornerRadius.card),
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = AppTouchTarget.minimum)
                    .clickable(enabled = !isAnswered && !isReviewMode) { onSelectOption(index) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppCardPadding.standard),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.extraSmall)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(letterBgColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = optionLabels.getOrElse(index) { "?" },
                                    color = letterTextColor,
                                    style = AppTypography.badge
                                )
                            }

                            Text(
                                text = optionText,
                                style = AppTypography.bodyPrimary.copy(fontWeight = FontWeight.Medium),
                                color = TextPrimary
                            )
                        }

                        if (iconVector != null) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (isSelected) {
                        Text(
                            text = question.explanation,
                            style = AppTypography.caption,
                            color = TextPrimary.copy(alpha = 0.9f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }

        // Navigation Action Button
        if (selectedOptionIndex != null || isReviewMode) {
            Surface(
                color = OrangeAccent,
                shape = RoundedCornerShape(AppCornerRadius.card),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = AppTouchTarget.buttonHeight)
                    .clickable { onNextQuestion() }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = AppSpacing.medium),
                    contentAlignment = Alignment.Center
                ) {
                    val nextButtonText = when {
                        isReviewMode && questionIndex + 1 == totalQuestions -> "Back to Results ➔"
                        questionIndex + 1 < totalQuestions -> "Next Question ➔"
                        else -> "Finish Quiz ➔"
                    }
                    Text(
                        text = nextButtonText,
                        color = Color(0xFF111115),
                        style = AppTypography.cardTitle
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizResultScreen(
    topicTitle: String,
    correctCount: Int,
    incorrectCount: Int,
    totalQuestions: Int,
    percentage: Int,
    onTryAgain: () -> Unit,
    onReviewAnswers: () -> Unit,
    onBackToTopicSelection: () -> Unit,
    onBackToPractice: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBgColor)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.large, vertical = AppSpacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onBackToTopicSelection() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.extraSmall)
            ) {
                Icon(imageVector = IconChevronLeft, contentDescription = "Back", tint = TextSecondary, modifier = Modifier.size(16.dp))
                Text(text = "Back", style = AppTypography.bodySecondary, color = TextSecondary)
            }

            Text(
                text = "PRACTICE · QUIZ RESULT",
                style = AppTypography.sectionHeader,
                color = OrangeAccent
            )

            // Topic Name & Fraction
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.extraSmall)
            ) {
                Text(
                    text = topicTitle.uppercase(),
                    style = AppTypography.sectionHeader,
                    color = TextSecondary
                )

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$correctCount",
                        color = TextPrimary,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " / $totalQuestions",
                        style = AppTypography.displayTitle,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Text(
                    text = "$percentage%",
                    style = AppTypography.headerTitle,
                    color = OrangeAccent
                )

                Text(
                    text = if (percentage >= 70) "Great job! Keep up the good work." else "Good progress. Review the misses and try again.",
                    style = AppTypography.bodySecondary,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            // Stats Grid Card
            Surface(
                color = DarkCardBgColor,
                shape = RoundedCornerShape(AppCornerRadius.card),
                border = BorderStroke(1.dp, CardBorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppCardPadding.standard),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "CORRECT",
                                style = AppTypography.sectionHeader,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$correctCount",
                                color = GreenAccent,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(40.dp)
                                .background(CardBorderColor)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "INCORRECT",
                                style = AppTypography.sectionHeader,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$incorrectCount",
                                color = RedAccent,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(CardBorderColor)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppSpacing.large, vertical = AppSpacing.medium),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$topicTitle · $totalQuestions questions",
                            style = AppTypography.caption,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Action Buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AppSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
        ) {
            Surface(
                color = OrangeAccent,
                shape = RoundedCornerShape(AppCornerRadius.card),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = AppTouchTarget.buttonHeight)
                    .clickable { onReviewAnswers() }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = AppSpacing.medium),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Review Answers",
                        color = Color(0xFF111115),
                        style = AppTypography.cardTitle
                    )
                }
            }

            Surface(
                color = Color(0xFF22222E),
                shape = RoundedCornerShape(AppCornerRadius.card),
                border = BorderStroke(1.dp, CardBorderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = AppTouchTarget.buttonHeight)
                    .clickable { onTryAgain() }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = AppSpacing.medium),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Try Again",
                        color = TextPrimary,
                        style = AppTypography.cardTitle
                    )
                }
            }

            Surface(
                color = DarkCardBgColor,
                shape = RoundedCornerShape(AppCornerRadius.card),
                border = BorderStroke(1.dp, CardBorderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = AppTouchTarget.buttonHeight)
                    .clickable { onBackToPractice() }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = AppSpacing.medium),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Back to Practice",
                        color = TextSecondary,
                        style = AppTypography.cardTitle
                    )
                }
            }
        }
    }
}

private data class Tuple6<A, B, C, D, E, F>(
    val a: A,
    val b: B,
    val c: C,
    val d: D,
    val e: E,
    val f: F
)
