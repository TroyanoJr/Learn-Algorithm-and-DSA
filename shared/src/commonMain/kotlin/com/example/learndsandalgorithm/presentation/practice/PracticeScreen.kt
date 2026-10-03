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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkBgColor = Color(0xFF111115)
private val DarkCardBgColor = Color(0xFF181820)
private val CardBorderColor = Color(0xFF262634)
private val HighlightedBorderColor = Color(0xFFFA6E13).copy(alpha = 0.35f)
private val OrangeAccent = Color(0xFFFA6E13)
private val BlueAccent = Color(0xFF38BDF8)
private val PurpleAccent = Color(0xFF9E66FF)
private val GreenAccent = Color(0xFF10B981)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9CA3AF)

private data class PracticeMode(
    val id: String,
    val title: String,
    val description: String,
    val metaText: String,
    val isHighlightedMeta: Boolean,
    val iconStr: String,
    val iconBgColor: Color,
    val iconBorderColor: Color,
    val iconTextColor: Color,
    val featuredHeader: String,
    val featuredBadge: String,
    val featuredTitle: String,
    val stat1Main: String,
    val stat1Sub: String,
    val stat2Main: String,
    val stat2Sub: String,
    val buttonText: String
)

@Composable
fun PracticeScreen(
    onOpenTopicQuiz: () -> Unit = {}
) {
    var selectedModeId by remember { mutableStateOf("quick_quiz") }

    val practiceModes = remember {
        listOf(
            PracticeMode(
                id = "quick_quiz",
                title = "Quick Quiz",
                description = "Test your knowledge in 5 minutes",
                metaText = "5 questions · 5 min",
                isHighlightedMeta = true,
                iconStr = "⚡",
                iconBgColor = Color(0xFF2A1A10),
                iconBorderColor = OrangeAccent,
                iconTextColor = OrangeAccent,
                featuredHeader = "QUICK QUIZ",
                featuredBadge = "84% last score",
                featuredTitle = "Warm up with a focused set",
                stat1Main = "5 questions",
                stat1Sub = "questions",
                stat2Main = "5 min",
                stat2Sub = "estimated",
                buttonText = "Start Quick Quiz ➔"
            ),
            PracticeMode(
                id = "topic_quiz",
                title = "Topic Quiz",
                description = "Focus on a specific concept",
                metaText = "10 questions · 10 min",
                isHighlightedMeta = false,
                iconStr = "🧠",
                iconBgColor = Color(0xFF1E162A),
                iconBorderColor = PurpleAccent,
                iconTextColor = PurpleAccent,
                featuredHeader = "TOPIC QUIZ",
                featuredBadge = "10 questions",
                featuredTitle = "Focus on a specific concept",
                stat1Main = "10 questions",
                stat1Sub = "questions",
                stat2Main = "10 min",
                stat2Sub = "estimated",
                buttonText = "Start Topic Quiz ➔"
            ),
            PracticeMode(
                id = "exams",
                title = "Exams",
                description = "Put your skills to the test",
                metaText = "30 questions · 35 min",
                isHighlightedMeta = false,
                iconStr = "🏅",
                iconBgColor = Color(0xFF122038),
                iconBorderColor = BlueAccent,
                iconTextColor = BlueAccent,
                featuredHeader = "EXAMS",
                featuredBadge = "30 questions",
                featuredTitle = "Put your skills to the test",
                stat1Main = "30 questions",
                stat1Sub = "questions",
                stat2Main = "35 min",
                stat2Sub = "estimated",
                buttonText = "Start Exam ➔"
            ),
            PracticeMode(
                id = "challenges",
                title = "Challenges",
                description = "Solve real coding problems",
                metaText = "1 problem · 20 min",
                isHighlightedMeta = false,
                iconStr = "</>",
                iconBgColor = Color(0xFF0E281E),
                iconBorderColor = GreenAccent,
                iconTextColor = GreenAccent,
                featuredHeader = "CHALLENGES",
                featuredBadge = "3 completed",
                featuredTitle = "Solve a problem from scratch",
                stat1Main = "1 problem",
                stat1Sub = "questions",
                stat2Main = "20 min",
                stat2Sub = "estimated",
                buttonText = "Start Challenges ➔"
            )
        )
    }

    val selectedMode = practiceModes.find { it.id == selectedModeId } ?: practiceModes.first()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBgColor)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        PracticeHeader()
        RecommendedCard()
        PracticeModesSection(
            modes = practiceModes,
            selectedModeId = selectedModeId,
            onSelectMode = { modeId -> selectedModeId = modeId },
            onStartMode = { modeId ->
                if (modeId == "topic_quiz") {
                    onOpenTopicQuiz()
                }
            }
        )
        QuickQuizFeaturedCard(
            mode = selectedMode,
            onStart = {
                if (selectedMode.id == "topic_quiz") {
                    onOpenTopicQuiz()
                }
            }
        )
        MomentumStreakCard()
    }
}

@Composable
private fun PracticeHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                text = "SHARPEN YOUR SKILLS",
                color = OrangeAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Practice",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Turn knowledge into confidence.",
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
            Text(
                text = "AL",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun RecommendedCard() {
    Surface(
        color = DarkCardBgColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, HighlightedBorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Play Icon Box
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2A180E)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "▶",
                        color = OrangeAccent,
                        fontSize = 18.sp
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "RECOMMENDED FOR YOU",
                        color = OrangeAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Arrays & complexity",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "10 questions · Medium · 72% last score",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(OrangeAccent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "➔",
                    color = Color(0xFF111115),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PracticeModesSection(
    modes: List<PracticeMode>,
    selectedModeId: String,
    onSelectMode: (String) -> Unit,
    onStartMode: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CHOOSE YOUR PACE",
                    color = OrangeAccent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Practice modes",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "4 ways to grow",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        modes.forEach { mode ->
            val isSelected = mode.id == selectedModeId
            PracticeModeCard(
                mode = mode,
                isSelected = isSelected,
                onClickMode = { onSelectMode(mode.id) },
                onClickArrow = {
                    onSelectMode(mode.id)
                    onStartMode(mode.id)
                }
            )
        }
    }
}

@Composable
private fun PracticeModeCard(
    mode: PracticeMode,
    isSelected: Boolean,
    onClickMode: () -> Unit,
    onClickArrow: () -> Unit
) {
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
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onClickMode() }
            ) {
                // Icon Box
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(mode.iconBgColor)
                        .border(1.dp, mode.iconBorderColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.iconStr,
                        color = mode.iconTextColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = mode.title,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = mode.description,
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = mode.metaText,
                        color = if (mode.isHighlightedMeta) OrangeAccent else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (mode.isHighlightedMeta) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clickable { onClickArrow() }
            ) {
                Text(
                    text = "›",
                    color = if (isSelected) OrangeAccent else TextSecondary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun QuickQuizFeaturedCard(
    mode: PracticeMode,
    onStart: () -> Unit
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
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = mode.featuredHeader,
                    color = OrangeAccent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )

                // Score Badge Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF3A1A0E))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = mode.featuredBadge,
                        color = OrangeAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = mode.featuredTitle,
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            // 2 Stat Tiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatTile(
                    modifier = Modifier.weight(1f),
                    mainText = mode.stat1Main,
                    subText = mode.stat1Sub
                )
                StatTile(
                    modifier = Modifier.weight(1f),
                    mainText = mode.stat2Main,
                    subText = mode.stat2Sub
                )
            }

            // Start Button
            Surface(
                color = OrangeAccent,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onStart() }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = mode.buttonText,
                            color = Color(0xFF111115),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatTile(
    modifier: Modifier = Modifier,
    mainText: String,
    subText: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E28))
            .padding(vertical = 12.dp, horizontal = 14.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = mainText,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subText,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun MomentumStreakCard() {
    Surface(
        color = DarkCardBgColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CardBorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "YOUR MOMENTUM",
                        color = OrangeAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Keep the streak alive",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "7",
                        color = OrangeAccent,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "day streak",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // Streak Progress Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF262634))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.70f)
                        .height(4.dp)
                        .background(OrangeAccent, RoundedCornerShape(2.dp))
                )
            }
        }
    }
}
