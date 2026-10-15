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
    val description: String,
    val questions: List<QuizQuestionState>
)

private enum class QuizFlowState {
    TOPIC_SELECTION,
    QUESTION,
    QUIZ_COMPLETE,
    QUIZ_RESULT
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

private fun getTopicDescription(topicId: String): String = when (topicId) {
    "arrays" -> "Contiguous memory elements with instant index-based lookup."
    "linear_search" -> "Sequential element inspection from start to end."
    "sorting" -> "Arranging elements in a defined ascending or descending order."
    else -> "Practice core computer science concepts."
}

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

    var flowState by remember { mutableStateOf(QuizFlowState.TOPIC_SELECTION) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var correctCount by remember { mutableStateOf(0) }
    var incorrectCount by remember { mutableStateOf(0) }

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
                        description = getTopicDescription(topic.id),
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
                activeTopic = initialTarget.copy(questions = shuffleQuizQuestionOrder(initialTarget.questions))
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
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "No Quiz Topics Available",
                    color = OrangeAccent,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "No quiz questions found in repository.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
                Surface(
                    color = OrangeAccent,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable { onBackToPractice() }
                ) {
                    Text(
                        text = "Back to Practice",
                        color = DarkBgColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
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
                        activeTopic = topic.copy(questions = shuffleQuizQuestionOrder(topic.questions))
                        currentQuestionIndex = 0
                        selectedOptionIndex = null
                        correctCount = 0
                        incorrectCount = 0
                        flowState = QuizFlowState.QUESTION
                    },
                    onBack = onBackToPractice
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
                    onSelectOption = { index ->
                        if (selectedOptionIndex == null) {
                            selectedOptionIndex = index
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
                            flowState = QuizFlowState.QUIZ_COMPLETE
                        }
                    },
                    onBack = { flowState = QuizFlowState.TOPIC_SELECTION }
                )
            }

            QuizFlowState.QUIZ_COMPLETE -> {
                val topic = activeTopic ?: topics.first()
                QuizCompleteScreen(
                    topicTitle = topic.title,
                    totalQuestions = topic.questions.size,
                    onViewResult = { flowState = QuizFlowState.QUIZ_RESULT },
                    onBack = { flowState = QuizFlowState.TOPIC_SELECTION }
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
                        activeTopic = topic.copy(questions = shuffleQuizQuestionOrder(topic.questions))
                        currentQuestionIndex = 0
                        selectedOptionIndex = null
                        correctCount = 0
                        incorrectCount = 0
                        flowState = QuizFlowState.QUESTION
                    },
                    onBackToTopicSelection = {
                        flowState = QuizFlowState.TOPIC_SELECTION
                    },
                    onBackToPractice = onBackToPractice
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
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkCardBgColor)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = IconChevronLeft, contentDescription = "Back", tint = TextPrimary, modifier = Modifier.size(16.dp))
            }
            Text(
                text = "TOPIC QUIZ",
                color = OrangeAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
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
                    color = OrangeAccent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "What do you want to practice?",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Pick a focused set and sharpen one concept at a time.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E1E28))
                    .border(1.dp, CardBorderColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "DSA", color = OrangeAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Topic List Cards
        topics.forEach { topic ->
            val isSelected = topic.id == selectedTopicId
            Surface(
                color = DarkCardBgColor,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) OrangeAccent else CardBorderColor
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Topic content area (clickable to select topic details)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectTopic(topic.id) }
                    ) {
                        TopicIcon(
                            topicId = topic.id,
                            containerSize = 40.dp,
                            iconSize = 20.dp
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = topic.title,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = topic.category,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${topic.questionCount} questions",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Chevron action button (clickable to start quiz)
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF262634))
                            .clickable { onStartQuizForTopic(topic) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = IconChevronRight,
                            contentDescription = "Start topic quiz",
                            tint = TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Topic Details Section for selected topic
        val currentSelectedTopic = topics.find { it.id == selectedTopicId } ?: topics.first()
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
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "TOPIC DETAILS",
                    color = OrangeAccent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = currentSelectedTopic.title,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currentSelectedTopic.description,
                    color = TextSecondary,
                    fontSize = 13.sp
                )
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
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkCardBgColor)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = IconChevronLeft, contentDescription = "Back", tint = TextPrimary, modifier = Modifier.size(16.dp))
                }

                Column {
                    Text(
                        text = "TOPIC QUIZ",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = topicTitle,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "${questionIndex + 1} / $totalQuestions",
                color = OrangeAccent,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
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

        Spacer(modifier = Modifier.height(4.dp))

        // Question Statement
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "QUESTION ${questionIndex + 1} · ${question.type.name.replace('_', ' ')}",
                color = OrangeAccent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            if (question.type == QuestionType.CODE_OUTPUT) {
                MarkdownContent(content = question.statement)
            } else {
                Text(
                    text = question.statement,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 23.sp
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
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, borderColor),
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
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
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = optionText,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
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
            }
        }

        // Feedback Section
        if (selectedOptionIndex != null) {
            val isCorrect = selectedOptionIndex == question.correctOptionIndex
            val feedbackBgColor = if (isCorrect) Color(0xFF092317) else Color(0xFF210C0D)
            val feedbackBorderColor = if (isCorrect) GreenAccent else RedAccent

            Surface(
                color = feedbackBgColor,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, feedbackBorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (isCorrect) "Correct!" else "Incorrect",
                        color = if (isCorrect) GreenAccent else RedAccent,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = question.explanation,
                        color = Color(0xFFD1D5DB),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            // Next Question Button
            Surface(
                color = OrangeAccent,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNextQuestion() }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (questionIndex + 1 < totalQuestions) "Next Question ➔" else "Finish Quiz ➔",
                        color = Color(0xFF111115),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizCompleteScreen(
    topicTitle: String,
    totalQuestions: Int,
    onViewResult: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBgColor)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Back Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onBack() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(imageVector = IconChevronLeft, contentDescription = "Back", tint = TextSecondary, modifier = Modifier.size(16.dp))
            Text(text = "Back", color = TextSecondary, fontSize = 14.sp)
        }

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Circle Checkmark Icon Container
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF2A1A10))
                    .border(1.dp, OrangeAccent, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(2.dp, OrangeAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "✓", color = OrangeAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Text(
                text = "ALL QUESTIONS ANSWERED",
                color = OrangeAccent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            Text(
                text = "Quiz Complete",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "You completed the $topicTitle quiz.",
                color = TextSecondary,
                fontSize = 14.sp
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkCardBgColor)
                    .border(1.dp, CardBorderColor, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "$totalQuestions QUESTIONS COMPLETED",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        // Bottom Button
        Surface(
            color = OrangeAccent,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onViewResult() }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "View Result ➔",
                    color = Color(0xFF111115),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
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
    onBackToTopicSelection: () -> Unit,
    onBackToPractice: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBgColor)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onBackToTopicSelection() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(imageVector = IconChevronLeft, contentDescription = "Back", tint = TextSecondary, modifier = Modifier.size(16.dp))
                Text(text = "Back", color = TextSecondary, fontSize = 14.sp)
            }

            Text(
                text = "PRACTICE · QUIZ RESULT",
                color = OrangeAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            // Topic Name & Fraction
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = topicTitle.uppercase(),
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
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
                        color = TextSecondary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Text(
                    text = "$percentage%",
                    color = OrangeAccent,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (percentage >= 70) "Great job! Keep up the good work." else "Good progress. Review the misses and try again.",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Stats Grid Card
            Surface(
                color = DarkCardBgColor,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CardBorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "CORRECT",
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
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
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
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
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$topicTitle · $totalQuestions questions",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = OrangeAccent,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTryAgain() }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Try Again",
                        color = Color(0xFF111115),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Surface(
                color = DarkCardBgColor,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CardBorderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onBackToPractice() }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Back to Practice",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
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
