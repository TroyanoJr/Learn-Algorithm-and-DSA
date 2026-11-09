package com.example.learndsandalgorithm.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learndsandalgorithm.domain.model.Topic
import com.example.learndsandalgorithm.domain.model.TopicCategory
import com.example.learndsandalgorithm.domain.model.UserStats
import com.example.learndsandalgorithm.presentation.components.TopicIcon
import com.example.learndsandalgorithm.presentation.theme.AppCardPadding
import com.example.learndsandalgorithm.presentation.theme.AppCornerRadius
import com.example.learndsandalgorithm.presentation.theme.AppIconSize
import com.example.learndsandalgorithm.presentation.theme.AppSpacing
import com.example.learndsandalgorithm.presentation.theme.AppTypography

private val DarkBgColor = Color(0xFF111115)
private val DarkCardBgColor = Color(0xFF1A1A22)
private val HighlightedCardBorderColor = Color(0xFFFA6E13).copy(alpha = 0.4f)
private val SubtleBorderColor = Color(0xFF262632)
private val OrangeAccent = Color(0xFFFA6E13)
private val PurpleAccent = Color(0xFF9E66FF)
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

private val IconBolt = buildVectorIcon("Bolt") {
    moveTo(11f, 21f)
    lineTo(12.5f, 13f)
    horizontalLineTo(7f)
    lineTo(13f, 3f)
    lineTo(11.5f, 11f)
    horizontalLineTo(17f)
    lineTo(11f, 21f)
    close()
}

private val IconTrophy = buildVectorIcon("Trophy") {
    moveTo(19f, 5f)
    horizontalLineTo(16f)
    verticalLineTo(3f)
    horizontalLineTo(8f)
    verticalLineTo(5f)
    horizontalLineTo(5f)
    curveTo(3.9f, 5f, 3f, 5.9f, 3f, 7f)
    verticalLineTo(8f)
    curveTo(3f, 10.21f, 4.79f, 12f, 7f, 12f)
    horizontalLineTo(7.1f)
    curveTo(7.8f, 13.77f, 9.51f, 15f, 11f, 15.35f)
    verticalLineTo(18f)
    horizontalLineTo(8f)
    verticalLineTo(20f)
    horizontalLineTo(16f)
    verticalLineTo(18f)
    horizontalLineTo(13f)
    verticalLineTo(15.35f)
    curveTo(14.49f, 15f, 16.2f, 13.77f, 16.9f, 12f)
    horizontalLineTo(17f)
    curveTo(19.21f, 12f, 21f, 10.21f, 21f, 8f)
    verticalLineTo(7f)
    curveTo(21f, 5.9f, 20.1f, 5f, 19f, 5f)
    close()
    moveTo(5f, 8f)
    verticalLineTo(7f)
    horizontalLineTo(7f)
    verticalLineTo(10f)
    curveTo(5.9f, 10f, 5f, 9.1f, 5f, 8f)
    close()
    moveTo(19f, 8f)
    curveTo(19f, 9.1f, 18.1f, 10f, 17f, 10f)
    verticalLineTo(7f)
    horizontalLineTo(19f)
    verticalLineTo(8f)
    close()
}

private val IconCheckCircle = buildVectorIcon("CheckCircle") {
    moveTo(12f, 2f)
    curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
    curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
    curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
    curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
    close()
    moveTo(10f, 17f)
    lineTo(5f, 12f)
    lineTo(6.41f, 10.59f)
    lineTo(10f, 14.17f)
    lineTo(17.59f, 6.58f)
    lineTo(19f, 8f)
    lineTo(10f, 17f)
    close()
}

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
            .padding(horizontal = AppSpacing.large, vertical = AppSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
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
                style = AppTypography.sectionHeader,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(AppSpacing.extraSmall))
            Text(
                text = "Ready to learn?",
                style = AppTypography.displayTitle,
                color = TextPrimary
            )
        }
        Box(
            modifier = Modifier
                .size(AppIconSize.boxMedium)
                .clip(CircleShape)
                .background(Color(0xFF262632))
                .border(BorderStroke(1.dp, OrangeAccent), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "DSA",
                style = AppTypography.bodySecondary.copy(fontWeight = FontWeight.Bold),
                color = OrangeAccent
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
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            value = formattedXp,
            label = "total\nXP",
            icon = IconBolt,
            iconTint = OrangeAccent
        )
        StatCard(
            modifier = Modifier.weight(1f),
            value = "${userStats.completedLessons}",
            label = lessonLabel,
            icon = IconTrophy,
            iconTint = OrangeAccent
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
fun StatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    icon: ImageVector,
    iconTint: Color = OrangeAccent
) {
    Card(
        modifier = modifier
            .border(BorderStroke(1.dp, SubtleBorderColor), RoundedCornerShape(AppCornerRadius.card)),
        colors = CardDefaults.cardColors(containerColor = DarkCardBgColor),
        shape = RoundedCornerShape(AppCornerRadius.card)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppCardPadding.compact, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(AppIconSize.medium)
            )
            Column {
                Text(
                    text = value,
                    style = AppTypography.sectionTitle,
                    color = TextPrimary
                )
                Text(
                    text = label,
                    style = AppTypography.badge.copy(fontWeight = FontWeight.Normal, lineHeight = 13.sp),
                    color = TextSecondary
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
            style = AppTypography.sectionHeader,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(AppSpacing.small))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, HighlightedCardBorderColor), RoundedCornerShape(AppCornerRadius.cardLarge))
                .clickable { onLessonClick(info.lessonId) },
            colors = CardDefaults.cardColors(containerColor = DarkCardBgColor),
            shape = RoundedCornerShape(AppCornerRadius.cardLarge)
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
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium)
                ) {
                    if (info.isAllComplete) {
                        Box(
                            modifier = Modifier
                                .size(AppIconSize.boxLarge)
                                .clip(RoundedCornerShape(14.dp))
                                .background(OrangeAccent.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = IconCheckCircle,
                                contentDescription = null,
                                tint = OrangeAccent,
                                modifier = Modifier.size(AppIconSize.medium)
                            )
                        }
                    } else {
                        TopicIcon(
                            topicId = info.topicId,
                            containerSize = AppIconSize.boxLarge,
                            iconSize = AppIconSize.medium
                        )
                    }
                    Column {
                        Text(
                            text = info.topicTitle,
                            style = AppTypography.sectionTitle,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.extraSmall))
                        Text(
                            text = if (info.isAllComplete) "All 14 lessons complete" else "Lesson ${info.lessonOrder} of ${info.totalTopicLessons} • ${info.categoryName}",
                            style = AppTypography.caption,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
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
                        .size(40.dp)
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
                style = AppTypography.sectionHeader,
                color = TextSecondary
            )
            Text(
                text = "$overallPercent% of curriculum complete",
                style = AppTypography.caption,
                color = TextSecondary
            )
        }
        Spacer(modifier = Modifier.height(AppSpacing.small))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, SubtleBorderColor), RoundedCornerShape(AppCornerRadius.cardLarge)),
            colors = CardDefaults.cardColors(containerColor = DarkCardBgColor),
            shape = RoundedCornerShape(AppCornerRadius.cardLarge)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppCardPadding.standard)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("${uiState.dsCompletedLessons}", color = Color(0xFF60A5FA), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text("/${uiState.dsTotalLessons}", style = AppTypography.bodyPrimary, color = TextSecondary)
                        }
                        Text("DS Lessons", style = AppTypography.caption, color = TextSecondary)
                        Text("${uiState.dsProgressPercent}%", style = AppTypography.badge, color = Color(0xFF60A5FA))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("${uiState.algoCompletedLessons}", color = PurpleAccent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text("/${uiState.algoTotalLessons}", style = AppTypography.bodyPrimary, color = TextSecondary)
                        }
                        Text("Algo Lessons", style = AppTypography.caption, color = TextSecondary)
                        Text("${uiState.algoProgressPercent}%", style = AppTypography.badge, color = PurpleAccent)
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                LinearProgressIndicator(
                    progress = { (overallPercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
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
            style = AppTypography.sectionHeader,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(AppSpacing.small))
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
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
            .border(BorderStroke(1.dp, SubtleBorderColor), RoundedCornerShape(AppCornerRadius.card)),
        colors = CardDefaults.cardColors(containerColor = DarkCardBgColor),
        shape = RoundedCornerShape(AppCornerRadius.card)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppCardPadding.compact),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
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
                Column {
                    Text(text = topic.title, style = AppTypography.cardTitle, color = TextPrimary)
                    Spacer(modifier = Modifier.height(AppSpacing.extraSmall))
                    Text(text = categoryDisplayName, style = AppTypography.caption, color = TextSecondary)
                    Spacer(modifier = Modifier.height(AppSpacing.extraSmall))
                    Text(text = "$progressPercent%", style = AppTypography.badge, color = TextSecondary)
                }
            }
            Box(
                modifier = Modifier
                    .widthIn(min = 60.dp, max = 80.dp)
                    .padding(start = AppSpacing.small)
            ) {
                LinearProgressIndicator(
                    progress = { (progressPercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
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
            .border(BorderStroke(1.dp, HighlightedCardBorderColor), RoundedCornerShape(AppCornerRadius.cardLarge))
            .clickable { onOpenTopicQuiz(info.topicId) },
        colors = CardDefaults.cardColors(containerColor = DarkCardBgColor),
        shape = RoundedCornerShape(AppCornerRadius.cardLarge)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppCardPadding.standard),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "RECOMMENDED PRACTICE",
                    style = AppTypography.sectionHeader,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = info.topicTitle,
                    style = AppTypography.sectionTitle,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(AppSpacing.extraSmall))
                Text(
                    text = info.description,
                    style = AppTypography.bodySecondary,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.width(AppSpacing.medium))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(OrangeAccent),
                contentAlignment = Alignment.Center
            ) {
                Text("→", color = DarkBgColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
