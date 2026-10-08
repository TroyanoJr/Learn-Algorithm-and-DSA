package com.example.learndsandalgorithm.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learndsandalgorithm.domain.model.Topic
import com.example.learndsandalgorithm.domain.model.TopicCategory
import com.example.learndsandalgorithm.domain.model.UserStats

private val DarkBgColor = Color(0xFF111115)
private val DarkCardBgColor = Color(0xFF1A1A22)
private val HighlightedCardBorderColor = Color(0xFFFA6E13).copy(alpha = 0.4f)
private val SubtleBorderColor = Color(0xFF262632)
private val OrangeAccent = Color(0xFFFA6E13)
private val PurpleAccent = Color(0xFF9E66FF)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9CA3AF)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onLessonClick: (lessonId: String) -> Unit = {},
    onOpenTopicQuiz: (topicId: String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadHomeData()
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = OrangeAccent)
        }
    } else {
        HomeScreenContent(
            uiState = uiState,
            onLessonClick = onLessonClick,
            onOpenTopicQuiz = onOpenTopicQuiz,
            innerPadding = PaddingValues(0.dp)
        )
    }
}

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onLessonClick: (lessonId: String) -> Unit,
    onOpenTopicQuiz: (topicId: String) -> Unit,
    innerPadding: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HomeHeader()
        HomeStatistics(uiState.userStats)
        if (uiState.continueLearning != null) {
            ContinueLearningSection(
                info = uiState.continueLearning,
                onLessonClick = onLessonClick
            )
        }
        OverallProgressSection(uiState)
        if (uiState.recentTopicsProgress.isNotEmpty()) {
            RecentTopicsSection(uiState)
        }
        if (uiState.recommendedPractice != null) {
            RecommendedPracticeSection(
                info = uiState.recommendedPractice,
                onOpenTopicQuiz = onOpenTopicQuiz
            )
        }
    }
}

@Composable
fun HomeHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "GOOD MORNING",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Ready to learn?",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color(0xFF262632))
                .border(BorderStroke(1.dp, OrangeAccent), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "DSA",
                color = OrangeAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun HomeStatistics(userStats: UserStats) {
    val formattedXp = formatNumberWithCommas(userStats.totalXp)
    val lessonLabel = if (userStats.completedLessons == 1) "lesson\ncompleted" else "lessons\ncompleted"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            value = formattedXp,
            label = "total\nXP",
            iconText = "⚡"
        )
        StatCard(
            modifier = Modifier.weight(1f),
            value = "${userStats.completedLessons}",
            label = lessonLabel,
            iconText = "🏆"
        )
    }
}

private fun formatNumberWithCommas(number: Int): String {
    val str = number.toString()
    val builder = StringBuilder()
    var count = 0
    for (i in str.length - 1 downTo 0) {
        builder.append(str[i])
        count++
        if (count % 3 == 0 && i > 0) {
            builder.append(',')
        }
    }
    return builder.reverse().toString()
}

@Composable
fun StatCard(modifier: Modifier = Modifier, value: String, label: String, iconText: String) {
    Card(
        modifier = modifier
            .border(BorderStroke(1.dp, SubtleBorderColor), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkCardBgColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = iconText, fontSize = 20.sp)
            Column {
                Text(
                    text = value,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = label,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 13.sp
                )
            }
        }
    }
}

@Composable
fun ContinueLearningSection(
    info: ContinueLearningInfo,
    onLessonClick: (lessonId: String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "CONTINUE LEARNING",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, HighlightedCardBorderColor), RoundedCornerShape(20.dp))
                .clickable { onLessonClick(info.lessonId) },
            colors = CardDefaults.cardColors(containerColor = DarkCardBgColor),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(OrangeAccent.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (info.isAllComplete) "🎉" else "📚", fontSize = 24.sp)
                    }
                    Column {
                        Text(
                            text = info.topicTitle,
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (info.isAllComplete) "All 14 lessons complete" else "Lesson ${info.lessonOrder} of ${info.totalTopicLessons} • ${info.categoryName}",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            LinearProgressIndicator(
                                progress = { info.topicProgressFloat.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = OrangeAccent,
                                trackColor = SubtleBorderColor
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(OrangeAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Text("→", color = DarkBgColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun OverallProgressSection(uiState: HomeUiState) {
    val overallPercent = uiState.progress.overallProgress

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "OVERALL PROGRESS",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "$overallPercent% of curriculum complete",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, SubtleBorderColor), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkCardBgColor),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("${uiState.dsCompletedLessons}", color = Color(0xFF60A5FA), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text("/${uiState.dsTotalLessons}", color = TextSecondary, fontSize = 14.sp)
                        }
                        Text("DS Lessons", color = TextSecondary, fontSize = 12.sp)
                        Text("${uiState.dsProgressPercent}%", color = Color(0xFF60A5FA), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("${uiState.algoCompletedLessons}", color = PurpleAccent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text("/${uiState.algoTotalLessons}", color = TextSecondary, fontSize = 14.sp)
                        }
                        Text("Algo Lessons", color = TextSecondary, fontSize = 12.sp)
                        Text("${uiState.algoProgressPercent}%", color = PurpleAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                LinearProgressIndicator(
                    progress = { (overallPercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = PurpleAccent,
                    trackColor = SubtleBorderColor
                )
            }
        }
    }
}

@Composable
fun RecentTopicsSection(uiState: HomeUiState) {
    val recentTopicsList = uiState.topics
        .filter { topic ->
            (uiState.recentTopicsProgress[topic.id] ?: 0) > 0
        }
        .take(3)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "RECENT TOPICS",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            recentTopicsList.forEach { topic ->
                val progress = uiState.recentTopicsProgress[topic.id] ?: 0
                TopicCardItem(topic = topic, progressPercent = progress)
            }
        }
    }
}

@Composable
fun TopicCardItem(topic: Topic, progressPercent: Int) {
    val categoryDisplayName = if (topic.category == TopicCategory.DATA_STRUCTURES) "Data Structures" else "Algorithms"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, SubtleBorderColor), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkCardBgColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = topic.title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = categoryDisplayName, color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "$progressPercent%", color = TextSecondary, fontSize = 11.sp)
            }
            Box(modifier = Modifier.width(100.dp)) {
                LinearProgressIndicator(
                    progress = { (progressPercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = OrangeAccent,
                    trackColor = SubtleBorderColor
                )
            }
        }
    }
}

@Composable
fun RecommendedPracticeSection(
    info: RecommendedPracticeInfo,
    onOpenTopicQuiz: (topicId: String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, HighlightedCardBorderColor), RoundedCornerShape(20.dp))
            .clickable { onOpenTopicQuiz(info.topicId) },
        colors = CardDefaults.cardColors(containerColor = DarkCardBgColor),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "RECOMMENDED PRACTICE",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = info.topicTitle,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = info.description,
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(OrangeAccent),
                contentAlignment = Alignment.Center
            ) {
                Text("→", color = DarkBgColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
