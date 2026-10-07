package com.example.learndsandalgorithm.presentation.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppDestination.entries.forEach { destination ->
                BottomNavItem(
                    text = destination.title,
                    iconStr = destination.icon,
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
    iconStr: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Text(
            text = iconStr,
            color = if (isSelected) OrangeAccent else TextSecondary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = text,
            color = if (isSelected) OrangeAccent else TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
