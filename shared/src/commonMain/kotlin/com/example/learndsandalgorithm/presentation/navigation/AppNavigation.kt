package com.example.learndsandalgorithm.presentation.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learndsandalgorithm.data.repository.ProgressRepository
import com.example.learndsandalgorithm.domain.repository.ContentRepository
import com.example.learndsandalgorithm.presentation.home.HomeScreen
import com.example.learndsandalgorithm.presentation.home.HomeViewModel
import com.example.learndsandalgorithm.presentation.learn.LearnScreen
import com.example.learndsandalgorithm.presentation.learn.LessonDetailScreen
import com.example.learndsandalgorithm.presentation.learn.LessonViewModel
import com.example.learndsandalgorithm.presentation.learn.TopicLessonsScreen
import com.example.learndsandalgorithm.presentation.practice.PracticeScreen
import com.example.learndsandalgorithm.presentation.practice.TopicQuizFlow

private val DarkBgColor = Color(0xFF111115)
private val DarkCardBgColor = Color(0xFF1A1A22)
private val OrangeAccent = Color(0xFFFA6E13)
private val TextSecondary = Color(0xFF9CA3AF)
private val SubtleBorderColor = Color(0xFF262632)

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

private val IconHome = buildVectorIcon("Home") {
    moveTo(10f, 20f)
    verticalLineTo(14f)
    horizontalLineTo(14f)
    verticalLineTo(20f)
    horizontalLineTo(19f)
    verticalLineTo(11f)
    horizontalLineTo(22f)
    lineTo(12f, 3f)
    lineTo(2f, 11f)
    horizontalLineTo(5f)
    verticalLineTo(20f)
    horizontalLineTo(10f)
    close()
}

private val IconBook = buildVectorIcon("Book") {
    moveTo(18f, 2f)
    horizontalLineTo(6f)
    curveTo(4.9f, 2f, 4f, 2.9f, 4f, 4f)
    verticalLineTo(20f)
    curveTo(4f, 21.1f, 4.9f, 22f, 6f, 22f)
    horizontalLineTo(18f)
    curveTo(19.1f, 22f, 20f, 21.1f, 20f, 20f)
    verticalLineTo(4f)
    curveTo(20f, 2.9f, 19.1f, 2f, 18f, 2f)
    close()
    moveTo(6f, 4f)
    horizontalLineTo(11f)
    verticalLineTo(12f)
    lineTo(8.5f, 10.5f)
    lineTo(6f, 12f)
    verticalLineTo(4f)
    close()
}

private val IconTarget = buildVectorIcon("Target") {
    moveTo(12f, 2f)
    curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
    curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
    curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
    curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
    close()
    moveTo(12f, 18f)
    curveTo(8.69f, 18f, 6f, 15.31f, 6f, 12f)
    curveTo(6f, 8.69f, 8.69f, 6f, 12f, 6f)
    curveTo(15.31f, 6f, 18f, 8.69f, 18f, 12f)
    curveTo(18f, 15.31f, 15.31f, 18f, 12f, 18f)
    close()
    moveTo(12f, 8f)
    curveTo(9.79f, 8f, 8f, 9.79f, 8f, 12f)
    curveTo(8f, 14.21f, 9.79f, 16f, 12f, 16f)
    curveTo(14.21f, 16f, 16f, 14.21f, 16f, 12f)
    curveTo(16f, 9.79f, 14.21f, 8f, 12f, 8f)
    close()
}

private fun getDestinationIcon(destination: AppDestination): ImageVector = when (destination) {
    AppDestination.HOME -> IconHome
    AppDestination.LEARN -> IconBook
    AppDestination.PRACTICE -> IconTarget
}

@Composable
fun AppNavigation(
    homeViewModel: HomeViewModel,
    contentRepository: ContentRepository,
    progressRepository: ProgressRepository
) {
    var currentDestination by remember { mutableStateOf(AppDestination.HOME) }
    var selectedTopicId by remember { mutableStateOf<String?>(null) }
    var selectedTopicTitle by remember { mutableStateOf<String>("") }
    var selectedLessonId by remember { mutableStateOf<String?>(null) }
    var selectedQuizTopicId by remember { mutableStateOf<String?>(null) }
    var isTopicQuizActive by remember { mutableStateOf(false) }
    var learnExpandedCategoryId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        bottomBar = {
            AppBottomNavigation(
                currentDestination = currentDestination,
                onNavigate = { destination ->
                    selectedLessonId = null
                    selectedTopicId = null
                    isTopicQuizActive = false
                    currentDestination = destination
                }
            )
        },
        containerColor = DarkBgColor
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val activeLessonId = selectedLessonId
            val activeTopicId = selectedTopicId

            when {
                activeLessonId != null -> {
                    val lessonViewModel = remember(activeLessonId) {
                        LessonViewModel(contentRepository, progressRepository)
                    }
                    LessonDetailScreen(
                        lessonId = activeLessonId,
                        viewModel = lessonViewModel,
                        onBack = { selectedLessonId = null },
                        onNavigateToLesson = { nextLessonId ->
                            selectedLessonId = nextLessonId
                        }
                    )
                }
                activeTopicId != null -> {
                    TopicLessonsScreen(
                        topicId = activeTopicId,
                        topicTitle = selectedTopicTitle,
                        contentRepository = contentRepository,
                        progressRepository = progressRepository,
                        selectedLessonId = selectedLessonId,
                        onLessonClick = { lesson ->
                            selectedLessonId = lesson.id
                        },
                        onBack = { selectedTopicId = null }
                    )
                }
                isTopicQuizActive -> {
                    TopicQuizFlow(
                        contentRepository = contentRepository,
                        initialTopicId = selectedQuizTopicId,
                        onBackToPractice = {
                            isTopicQuizActive = false
                            selectedQuizTopicId = null
                        }
                    )
                }
                else -> {
                    when (currentDestination) {
                        AppDestination.HOME -> HomeScreen(
                            viewModel = homeViewModel,
                            onLessonClick = { lessonId ->
                                selectedLessonId = lessonId
                            },
                            onOpenTopicQuiz = { topicId ->
                                selectedQuizTopicId = topicId
                                isTopicQuizActive = true
                            }
                        )
                        AppDestination.LEARN -> LearnScreen(
                            contentRepository = contentRepository,
                            progressRepository = progressRepository,
                            selectedTopicId = selectedTopicId,
                            expandedCategoryId = learnExpandedCategoryId,
                            onCategoryExpandChange = { categoryId ->
                                learnExpandedCategoryId = categoryId
                            },
                            onTopicClick = { topicId, topicTitle ->
                                selectedTopicId = topicId
                                selectedTopicTitle = topicTitle
                            }
                        )
                        AppDestination.PRACTICE -> PracticeScreen(
                            onOpenTopicQuiz = { isTopicQuizActive = true }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppBottomNavigation(
    currentDestination: AppDestination,
    onNavigate: (AppDestination) -> Unit
) {
    Surface(
        color = DarkCardBgColor,
        border = BorderStroke(1.dp, SubtleBorderColor),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppDestination.entries.forEach { destination ->
                BottomNavItem(
                    text = destination.title,
                    icon = getDestinationIcon(destination),
                    isSelected = destination == currentDestination,
                    onClick = { onNavigate(destination) }
                )
            }
        }
    }
}

@Composable
fun BottomNavItem(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 1.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = if (isSelected) OrangeAccent else TextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            color = if (isSelected) OrangeAccent else TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
