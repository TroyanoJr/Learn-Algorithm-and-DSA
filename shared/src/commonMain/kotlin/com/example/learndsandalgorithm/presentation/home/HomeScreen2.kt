package com.example.learndsandalgorithm.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.learndsandalgorithm.domain.model.Activity
import com.example.learndsandalgorithm.domain.model.Challenge
import com.example.learndsandalgorithm.domain.model.Progress
import com.example.learndsandalgorithm.domain.model.Topic
import com.example.learndsandalgorithm.domain.model.TopicCategory
import com.example.learndsandalgorithm.domain.model.UserStats

private val DarkBgColor = Color(0xFF111115)
private val DarkCardBgColor = Color(0xFF1A1A22)
private val HighlightedCardBorderColor = Color(0xFFFA6E13).copy(alpha = 0.4f)
private val SubtleBorderColor = Color(0xFF262632)
private val OrangeAccent = Color(0xFFFA6E13)
private val PurpleAccent = Color(0xFF9E66FF)
private val GreenAccent = Color(0xFF10B981)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9CA3AF)

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = OrangeAccent)
        }
    } else {
        HomeScreenContent(uiState = uiState, innerPadding = PaddingValues(0.dp))
    }
}

@Composable
fun HomeScreenContent(uiState: HomeUiState, innerPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        HomeHeader(uiState.userStats)
        HomeStatistics(uiState.userStats)
        ContinueLearningSection(uiState.topics.firstOrNull { it.id == "arrays" } ?: uiState.topics.firstOrNull())
        OverallProgressSection(uiState.progress)
        RecentTopicsSection(uiState.topics)
        RecentActivitySection(uiState.activities)
        RecommendedPracticeSection(uiState.recommendedChallenge)
    }
}

@Composable
fun HomeHeader(userStats: UserStats) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Ready to learn, ",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Alex?",
                    color = OrangeAccent,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
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
                text = "AL",
                color = OrangeAccent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun HomeStatistics(userStats: UserStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            value = "${userStats.streakDays}",
            label = "day\nstreak",
            iconText = "🔥"
        )
        StatCard(
            modifier = Modifier.weight(1.2f),
            value = "2,480",
            label = "\ntotal XP",
            iconText = "⚡"
        )
        StatCard(
            modifier = Modifier.weight(1.1f),
            value = "${userStats.completedLessons}",
            label = "lessons\ncompleted",
            iconText = "🏆"
        )
    }
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
                .padding(horizontal = 12.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = iconText, fontSize = 18.sp)
            Column {
                Text(
                    text = value,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = label,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 12.sp
                )
            }
        }
    }
}

@Composable
fun ContinueLearningSection(currentTopic: Topic?) {
    val topicName = currentTopic?.title ?: "Arrays"
    
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
                .border(BorderStroke(1.dp, HighlightedCardBorderColor), RoundedCornerShape(20.dp)),
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
                        Text("🥞", fontSize = 24.sp)
                    }
                    Column {
                        Text(
                            text = topicName,
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Lesson 1 of 8 • Data Structures",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(modifier = Modifier.width(160.dp)) {
                            LinearProgressIndicator(
                                progress = { 0.35f },
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
fun OverallProgressSection(progress: Progress) {
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
                text = "32% of curriculum complete",
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
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("6", color = Color(0xFF60A5FA), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text("/18", color = TextSecondary, fontSize = 14.sp)
                        }
                        Text("DS Topics", color = TextSecondary, fontSize = 12.sp)
                    }
                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("4", color = PurpleAccent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text("/12", color = TextSecondary, fontSize = 14.sp)
                        }
                        Text("Algorithms", color = TextSecondary, fontSize = 12.sp)
                    }
                    Column {
                        Text("32%", color = GreenAccent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("Quiz Score", color = TextSecondary, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                LinearProgressIndicator(
                    progress = { 0.32f },
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
fun RecentTopicsSection(topics: List<Topic>) {
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
            listOf("Searching", "Arrays", "Sorting").forEach { name ->
                val topic = topics.find { it.title.equals(name, ignoreCase = true) }
                if (topic != null) {
                    TopicCardItem(topic)
                }
            }
        }
    }
}

@Composable
fun TopicCardItem(topic: Topic) {
    val categoryDisplayName = if (topic.category == TopicCategory.DATA_STRUCTURES) "Data Structures" else "Algorithms"
    val dummyProgress = when (topic.title) {
        "Searching" -> 80
        "Arrays" -> 55
        "Sorting" -> 30
        else -> 0
    }

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
                Text(text = "$dummyProgress%", color = TextSecondary, fontSize = 11.sp)
            }
            Box(modifier = Modifier.width(100.dp)) {
                LinearProgressIndicator(
                    progress = { dummyProgress / 100f },
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
fun RecentActivitySection(activities: List<Activity>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent activity",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "View all ",
                    color = OrangeAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "›",
                    color = OrangeAccent,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, SubtleBorderColor), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkCardBgColor),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                activities.forEachIndexed { index, activity ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isCompletion = activity.type == "completion"
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isCompletion) GreenAccent.copy(alpha = 0.15f) else OrangeAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isCompletion) "✓" else "▶",
                                color = if (isCompletion) GreenAccent else OrangeAccent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = activity.title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isCompletion) "Yesterday • Algorithms" else "Today • Data Structures",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                    if (index < activities.lastIndex) {
                        Divider(color = SubtleBorderColor, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendedPracticeSection(challenge: Challenge?) {
    val title = challenge?.title ?: "Arrays & complexity"
    val desc = challenge?.description ?: "5 questions to reinforce your latest lesson."

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, HighlightedCardBorderColor), RoundedCornerShape(20.dp)),
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
                    text = title,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = desc,
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


