package com.example.learndsandalgorithm.presentation.learn

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learndsandalgorithm.data.repository.ProgressRepository
import com.example.learndsandalgorithm.domain.model.Lesson
import com.example.learndsandalgorithm.domain.repository.ContentRepository

private val DarkBgColor = Color(0xFF111115)
private val DarkCardBgColor = Color(0xFF181820)
private val CardBorderColor = Color(0xFF262634)
private val OrangeAccent = Color(0xFFFA6E13)
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

private val IconArrowBack = buildVectorIcon("ArrowBack") {
    moveTo(20f, 11f)
    horizontalLineTo(7.83f)
    lineTo(13.42f, 5.41f)
    lineTo(12f, 4f)
    lineTo(4f, 12f)
    lineTo(12f, 20f)
    lineTo(13.41f, 18.59f)
    lineTo(7.83f, 13f)
    horizontalLineTo(20f)
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

@Composable
fun TopicLessonsScreen(
    topicId: String,
    topicTitle: String,
    contentRepository: ContentRepository,
    progressRepository: ProgressRepository,
    selectedLessonId: String? = null,
    onLessonClick: (Lesson) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var lessons by remember { mutableStateOf<List<Lesson>>(emptyList()) }
    var completedLessonIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(topicId, selectedLessonId) {
        isLoading = true
        lessons = contentRepository.getLessonsByTopic(topicId)
        completedLessonIds = progressRepository.getCompletedLessonIds()
        isLoading = false
    }

    val completedCount = lessons.count { it.id in completedLessonIds }
    val totalCount = lessons.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBgColor)
    ) {
        // Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DarkCardBgColor)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = IconArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = "TOPIC LESSONS",
                    color = OrangeAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = topicTitle,
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = OrangeAccent)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (totalCount > 0) "$completedCount of $totalCount lessons complete" else "0 lessons available",
                    color = if (completedCount > 0) GreenAccent else TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (completedCount > 0) FontWeight.Bold else FontWeight.Normal
                )

                lessons.forEach { lesson ->
                    val isCompleted = lesson.id in completedLessonIds
                    LessonCardRow(
                        lesson = lesson,
                        isCompleted = isCompleted,
                        onClick = { onLessonClick(lesson) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LessonCardRow(
    lesson: Lesson,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = DarkCardBgColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (isCompleted) GreenAccent.copy(alpha = 0.4f) else CardBorderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF2A1C12))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Lesson ${lesson.order}",
                            color = OrangeAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF13233A))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "+${lesson.xp} XP",
                            color = Color(0xFF38BDF8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (isCompleted) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF092317))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "✓ Complete",
                                color = GreenAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = lesson.title,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isCompleted) Color(0xFF092317) else Color(0xFF2A1C12))
                    .border(width = if (isCompleted) 1.dp else 0.dp, color = if (isCompleted) GreenAccent else Color.Transparent, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCompleted) IconCheck else IconChevronRight,
                    contentDescription = if (isCompleted) "Completed" else "Open lesson",
                    tint = if (isCompleted) GreenAccent else OrangeAccent,
                    modifier = Modifier.size(if (isCompleted) 16.dp else 18.dp)
                )
            }
        }
    }
}
