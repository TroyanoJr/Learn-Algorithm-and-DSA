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
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
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

private val IconBrain = buildVectorIcon("Brain") {
    moveTo(12f, 3f)
    curveTo(7.03f, 3f, 3f, 7.03f, 3f, 12f)
    curveTo(3f, 16.97f, 7.03f, 21f, 12f, 21f)
    curveTo(16.97f, 21f, 21f, 16.97f, 21f, 12f)
    curveTo(21f, 7.03f, 16.97f, 3f, 12f, 3f)
    close()
    moveTo(12f, 18f)
    curveTo(8.69f, 18f, 6f, 15.31f, 6f, 12f)
    curveTo(6f, 8.69f, 8.69f, 6f, 12f, 6f)
    curveTo(15.31f, 6f, 18f, 8.69f, 18f, 12f)
    curveTo(18f, 15.31f, 15.31f, 18f, 12f, 18f)
    close()
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

private val IconMedal = buildVectorIcon("Medal") {
    moveTo(12f, 2f)
    lineTo(9.19f, 8.63f)
    lineTo(2f, 9.24f)
    lineTo(7.46f, 13.97f)
    lineTo(5.82f, 21f)
    lineTo(12f, 17.27f)
    lineTo(18.18f, 21f)
    lineTo(16.54f, 13.97f)
    lineTo(22f, 9.24f)
    lineTo(14.81f, 8.63f)
    close()
}

private val IconCode = buildVectorIcon("Code") {
    moveTo(9.4f, 16.6f)
    lineTo(4.8f, 12f)
    lineTo(9.4f, 7.4f)
    lineTo(8f, 6f)
    lineTo(2f, 12f)
    lineTo(8f, 18f)
    close()
    moveTo(14.6f, 16.6f)
    lineTo(19.2f, 12f)
    lineTo(14.6f, 7.4f)
    lineTo(16f, 6f)
    lineTo(22f, 12f)
    lineTo(16f, 18f)
    close()
}

private val IconLock = buildVectorIcon("Lock") {
    moveTo(18f, 8f)
    horizontalLineTo(17f)
    verticalLineTo(6f)
    curveTo(17f, 3.24f, 14.76f, 1f, 12f, 1f)
    curveTo(9.24f, 1f, 7f, 3.24f, 7f, 6f)
    verticalLineTo(8f)
    horizontalLineTo(6f)
    curveTo(4.9f, 8f, 4f, 8.9f, 4f, 10f)
    verticalLineTo(20f)
    curveTo(4f, 21.1f, 4.9f, 22f, 6f, 22f)
    horizontalLineTo(18f)
    curveTo(19.1f, 22f, 20f, 21.1f, 20f, 20f)
    verticalLineTo(10f)
    curveTo(20f, 8.9f, 19.1f, 8f, 18f, 8f)
    close()
    moveTo(9f, 6f)
    curveTo(9f, 4.34f, 10.34f, 3f, 12f, 3f)
    curveTo(13.66f, 3f, 15f, 4.34f, 15f, 6f)
    verticalLineTo(8f)
    horizontalLineTo(9f)
    verticalLineTo(6f)
    close()
    moveTo(12f, 17f)
    curveTo(10.9f, 17f, 10f, 16.1f, 10f, 15f)
    curveTo(10f, 13.9f, 10.9f, 13f, 12f, 13f)
    curveTo(13.1f, 13f, 14f, 13.9f, 14f, 15f)
    curveTo(14f, 16.1f, 13.1f, 17f, 12f, 17f)
    close()
}

private val IconPlayArrow = buildVectorIcon("PlayArrow") {
    moveTo(8f, 5f)
    verticalLineTo(19f)
    lineTo(19f, 12f)
    close()
}

private val IconArrowForward = buildVectorIcon("ArrowForward") {
    moveTo(12f, 4f)
    lineTo(10.59f, 5.41f)
    lineTo(16.17f, 11f)
    horizontalLineTo(4f)
    verticalLineTo(13f)
    horizontalLineTo(16.17f)
    lineTo(10.59f, 18.59f)
    lineTo(12f, 20f)
    lineTo(20f, 12f)
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

private data class PracticeMode(
    val id: String,
    val title: String,
    val description: String,
    val metaText: String,
    val isAvailable: Boolean,
    val icon: ImageVector,
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
    var selectedModeId by remember { mutableStateOf("topic_quiz") }

    val practiceModes = remember {
        listOf(
            PracticeMode(
                id = "topic_quiz",
                title = "Topic Quiz",
                description = "Focus on a specific concept",
                metaText = "8 questions per topic · 10 min",
                isAvailable = true,
                icon = IconBrain,
                iconBgColor = Color(0xFF1E162A),
                iconBorderColor = PurpleAccent,
                iconTextColor = PurpleAccent,
                featuredHeader = "TOPIC QUIZ",
                featuredBadge = "Available ✓",
                featuredTitle = "Focus on a specific concept",
                stat1Main = "8 questions",
                stat1Sub = "per topic",
                stat2Main = "10 min",
                stat2Sub = "estimated",
                buttonText = "Start Topic Quiz ➔"
            ),
            PracticeMode(
                id = "quick_quiz",
                title = "Quick Quiz",
                description = "Test your knowledge in 5 minutes",
                metaText = "Coming Soon",
                isAvailable = false,
                icon = IconBolt,
                iconBgColor = Color(0xFF2A1A10),
                iconBorderColor = OrangeAccent,
                iconTextColor = OrangeAccent,
                featuredHeader = "QUICK QUIZ",
                featuredBadge = "Coming Soon",
                featuredTitle = "Warm up with a focused set",
                stat1Main = "5 questions",
                stat1Sub = "questions",
                stat2Main = "5 min",
                stat2Sub = "estimated",
                buttonText = "Coming Soon"
            ),
            PracticeMode(
                id = "exams",
                title = "Exams",
                description = "Put your skills to the test",
                metaText = "Coming Soon",
                isAvailable = false,
                icon = IconMedal,
                iconBgColor = Color(0xFF122038),
                iconBorderColor = BlueAccent,
                iconTextColor = BlueAccent,
                featuredHeader = "EXAMS",
                featuredBadge = "Coming Soon",
                featuredTitle = "Put your skills to the test",
                stat1Main = "30 questions",
                stat1Sub = "questions",
                stat2Main = "35 min",
                stat2Sub = "estimated",
                buttonText = "Coming Soon"
            ),
            PracticeMode(
                id = "challenges",
                title = "Challenges",
                description = "Solve real coding problems",
                metaText = "Coming Soon",
                isAvailable = false,
                icon = IconCode,
                iconBgColor = Color(0xFF0E281E),
                iconBorderColor = GreenAccent,
                iconTextColor = GreenAccent,
                featuredHeader = "CHALLENGES",
                featuredBadge = "Coming Soon",
                featuredTitle = "Solve a problem from scratch",
                stat1Main = "1 problem",
                stat1Sub = "questions",
                stat2Main = "20 min",
                stat2Sub = "estimated",
                buttonText = "Coming Soon"
            )
        )
    }

    val selectedMode = practiceModes.find { it.id == selectedModeId } ?: practiceModes.first()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            PracticeHeader()
            RecommendedCard(onOpenTopicQuiz = onOpenTopicQuiz)
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
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            PracticeFeaturedCard(
                mode = selectedMode,
                onStart = {
                    if (selectedMode.isAvailable) {
                        onOpenTopicQuiz()
                    }
                }
            )
        }
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
                fontSize = 24.sp,
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
                text = "DSA",
                color = OrangeAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun RecommendedCard(onOpenTopicQuiz: () -> Unit) {
    Surface(
        color = DarkCardBgColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, HighlightedBorderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenTopicQuiz() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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
                    Icon(
                        imageVector = IconPlayArrow,
                        contentDescription = null,
                        tint = OrangeAccent,
                        modifier = Modifier.size(20.dp)
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
                        text = "Topic Quiz",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "8 questions per topic · Arrays, Searching & Sorting",
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
                Icon(
                    imageVector = IconArrowForward,
                    contentDescription = null,
                    tint = Color(0xFF111115),
                    modifier = Modifier.size(20.dp)
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
                text = "1 available mode",
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
                    if (mode.isAvailable) {
                        onStartMode(mode.id)
                    }
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
                .padding(12.dp),
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
                        .border(1.dp, mode.iconBorderColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = mode.icon,
                        contentDescription = null,
                        tint = mode.iconTextColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = mode.title,
                            color = if (mode.isAvailable) TextPrimary else TextSecondary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (!mode.isAvailable) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF262634))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Coming Soon",
                                    color = TextSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = mode.description,
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = mode.metaText,
                        color = if (mode.isAvailable) OrangeAccent else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (mode.isAvailable) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clickable(enabled = mode.isAvailable) { onClickArrow() }
            ) {
                if (mode.isAvailable) {
                    Icon(
                        imageVector = IconChevronRight,
                        contentDescription = null,
                        tint = OrangeAccent,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Icon(
                        imageVector = IconLock,
                        contentDescription = "Locked",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PracticeFeaturedCard(
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
                .padding(12.dp),
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

                // Score / Availability Badge Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (mode.isAvailable) Color(0xFF092317) else Color(0xFF22222E))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = mode.featuredBadge,
                        color = if (mode.isAvailable) GreenAccent else TextSecondary,
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

            // Start / Coming Soon Button
            Surface(
                color = if (mode.isAvailable) OrangeAccent else Color(0xFF22222E),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (mode.isAvailable) Color.Transparent else CardBorderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = mode.isAvailable) { onStart() }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.buttonText,
                        color = if (mode.isAvailable) Color(0xFF111115) else TextSecondary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
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
