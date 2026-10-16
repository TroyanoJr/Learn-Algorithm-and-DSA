package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learndsandalgorithm.data.repository.ProgressRepository
import com.example.learndsandalgorithm.domain.model.Lesson
import com.example.learndsandalgorithm.presentation.components.TopicIcon
import com.example.learndsandalgorithm.domain.model.Topic
import com.example.learndsandalgorithm.domain.model.TopicCategory
import com.example.learndsandalgorithm.domain.model.TopicLevel
import com.example.learndsandalgorithm.domain.repository.ContentRepository

private val DarkBgColor = Color(0xFF111115)
private val DarkCardBgColor = Color(0xFF181820)
private val CardBorderColor = Color(0xFF262634)
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

private val IconChevronUp = buildVectorIcon("ChevronUp") {
    moveTo(7.41f, 15.41f)
    lineTo(12f, 10.83f)
    lineTo(16.59f, 15.41f)
    lineTo(18f, 14f)
    lineTo(12f, 8f)
    lineTo(6f, 14f)
    close()
}

private val IconChevronDown = buildVectorIcon("ChevronDown") {
    moveTo(7.41f, 8.59f)
    lineTo(12f, 13.17f)
    lineTo(16.59f, 8.59f)
    lineTo(18f, 10f)
    lineTo(12f, 16f)
    lineTo(6f, 10f)
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

private enum class TopicStatus {
    COMPLETED, IN_PROGRESS, NOT_STARTED
}

private data class LearnTopicItem(
    val topicId: String,
    val title: String,
    val level: TopicLevel,
    val lessonProgressText: String,
    val status: TopicStatus,
    val isLocked: Boolean
)

private data class CourseCategory(
    val id: String,
    val title: String,
    val topicsCountText: String,
    val percentageProgressText: String,
    val progressFloat: Float,
    val completedText: String,
    val inProgressText: String,
    val progressColor: Color,
    val topics: List<LearnTopicItem>
)

private data class LearnOverviewData(
    val totalCompletedTopics: Int,
    val totalAvailableTopics: Int,
    val totalAvailableLessons: Int,
    val totalCompletedLessons: Int,
    val overallPercent: Int
)

@Composable
fun LearnScreen(
    contentRepository: ContentRepository,
    progressRepository: ProgressRepository,
    selectedTopicId: String? = null,
    expandedCategoryId: String? = null,
    onCategoryExpandChange: (String?) -> Unit = {},
    onTopicClick: (topicId: String, topicTitle: String) -> Unit = { _, _ -> }
) {
    var categories by remember { mutableStateOf<List<CourseCategory>>(emptyList()) }
    var overviewData by remember { mutableStateOf(LearnOverviewData(0, 0, 0, 0, 0)) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(selectedTopicId) {
        isLoading = true
        val completedIds = progressRepository.getCompletedLessonIds()
        val allTopics = contentRepository.getTopics()

        val topicLessonsMap = mutableMapOf<String, List<Lesson>>()
        allTopics.forEach { topic ->
            topicLessonsMap[topic.id] = contentRepository.getLessonsByTopic(topic.id)
        }

        fun mapToTopicItem(topic: Topic): LearnTopicItem {
            val lessons = topicLessonsMap[topic.id] ?: emptyList()
            val isLocked = lessons.isEmpty()
            val total = lessons.size
            val completed = lessons.count { it.id in completedIds }

            val status = when {
                isLocked || total == 0 -> TopicStatus.NOT_STARTED
                completed == total -> TopicStatus.COMPLETED
                completed > 0 -> TopicStatus.IN_PROGRESS
                else -> TopicStatus.NOT_STARTED
            }

            val progressText = when {
                isLocked -> getEstimatedLessonsText(topic.id)
                completed == total -> "All $total lessons complete"
                completed > 0 -> "$completed of $total lessons complete"
                else -> "$total lessons"
            }

            return LearnTopicItem(
                topicId = topic.id,
                title = topic.title,
                level = topic.level,
                lessonProgressText = progressText,
                status = status,
                isLocked = isLocked
            )
        }

        fun buildCategory(
            categoryId: String,
            title: String,
            categoryEnum: TopicCategory,
            progressColor: Color
        ): CourseCategory {
            val categoryTopics = allTopics.filter { it.category == categoryEnum }
            val topicItems = categoryTopics.map { mapToTopicItem(it) }

            val implementedTopics = categoryTopics.filter { (topicLessonsMap[it.id]?.size ?: 0) > 0 }
            val categoryAvailableLessons = implementedTopics.flatMap { topicLessonsMap[it.id] ?: emptyList() }
            val categoryTotalLessons = categoryAvailableLessons.size
            val categoryCompletedLessons = categoryAvailableLessons.count { it.id in completedIds }

            val progressFloat = if (categoryTotalLessons > 0) categoryCompletedLessons.toFloat() / categoryTotalLessons else 0f
            val percentInt = (progressFloat * 100).toInt().coerceIn(0, 100)

            val completedTopicsCount = implementedTopics.count { topic ->
                val lessons = topicLessonsMap[topic.id] ?: emptyList()
                lessons.isNotEmpty() && lessons.all { it.id in completedIds }
            }
            val inProgressTopicsCount = implementedTopics.count { topic ->
                val lessons = topicLessonsMap[topic.id] ?: emptyList()
                lessons.any { it.id in completedIds } && !lessons.all { it.id in completedIds }
            }

            return CourseCategory(
                id = categoryId,
                title = title,
                topicsCountText = "${categoryTopics.size} topics",
                percentageProgressText = "$percentInt%",
                progressFloat = progressFloat.coerceIn(0f, 1f),
                completedText = "$completedTopicsCount completed",
                inProgressText = "$inProgressTopicsCount in progress",
                progressColor = progressColor,
                topics = topicItems
            )
        }

        val dsCategory = buildCategory("data_structures", "DATA STRUCTURES", TopicCategory.DATA_STRUCTURES, OrangeAccent)
        val algoCategory = buildCategory("algorithms", "ALGORITHMS", TopicCategory.ALGORITHMS, PurpleAccent)

        val allImplementedTopics = allTopics.filter { (topicLessonsMap[it.id]?.size ?: 0) > 0 }
        val allAvailableLessons = allImplementedTopics.flatMap { topicLessonsMap[it.id] ?: emptyList() }
        val totalAvailableLessons = allAvailableLessons.size
        val totalCompletedLessons = allAvailableLessons.count { it.id in completedIds }
        val totalCompletedTopics = allImplementedTopics.count { topic ->
            val lessons = topicLessonsMap[topic.id] ?: emptyList()
            lessons.isNotEmpty() && lessons.all { it.id in completedIds }
        }
        val overallPercent = if (totalAvailableLessons > 0) (totalCompletedLessons * 100 / totalAvailableLessons).coerceIn(0, 100) else 0

        overviewData = LearnOverviewData(
            totalCompletedTopics = totalCompletedTopics,
            totalAvailableTopics = allImplementedTopics.size,
            totalAvailableLessons = totalAvailableLessons,
            totalCompletedLessons = totalCompletedLessons,
            overallPercent = overallPercent
        )
        categories = listOf(dsCategory, algoCategory)
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBgColor)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LearnHeader()

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = OrangeAccent)
            }
        } else {
            CourseProgressOverview(overviewData)

            Spacer(modifier = Modifier.height(4.dp))

            categories.forEach { category ->
                val isExpanded = category.id == expandedCategoryId
                CourseCategoryCard(
                    category = category,
                    isExpanded = isExpanded,
                    onToggleExpand = {
                        onCategoryExpandChange(if (isExpanded) null else category.id)
                    },
                    onTopicClick = { topicId, topicTitle ->
                        onCategoryExpandChange(category.id)
                        onTopicClick(topicId, topicTitle)
                    }
                )
            }
        }
    }
}

@Composable
private fun LearnHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                text = "COURSE CATALOG",
                color = OrangeAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "What will you",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 28.sp
            )
            Text(
                text = "learn today?",
                color = OrangeAccent,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 28.sp
            )
        }

        Box(
            modifier = Modifier
                .size(38.dp)
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
private fun CourseProgressOverview(overviewData: LearnOverviewData) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${overviewData.totalCompletedTopics} of ${overviewData.totalAvailableTopics} topics complete",
                color = TextSecondary,
                fontSize = 14.sp
            )
            Text(
                text = "${overviewData.overallPercent}%",
                color = OrangeAccent,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Custom Progress Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF262634))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((overviewData.overallPercent / 100f).coerceIn(0f, 1f))
                    .height(4.dp)
                    .background(OrangeAccent, RoundedCornerShape(2.dp))
            )
        }

        // Status legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusLegendItem(dotColor = GreenAccent, label = "Completed")
            StatusLegendItem(dotColor = OrangeAccent, label = "In progress")
            StatusLegendItem(dotColor = Color(0xFF6B7280), label = "Not started")
        }

        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFF1E1E2A))
        )
    }
}

@Composable
private fun StatusLegendItem(dotColor: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun CourseCategoryCard(
    category: CourseCategory,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onTopicClick: (topicId: String, topicTitle: String) -> Unit
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
                .padding(12.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Left Accent Line
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(BlueAccent)
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = category.title,
                            color = BlueAccent,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = category.topicsCountText,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = category.percentageProgressText,
                        color = GreenAccent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Expand/Collapse Chevron
                    Icon(
                        imageVector = if (isExpanded) IconChevronUp else IconChevronDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = BlueAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))

                // Progress bar inside expanded card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF262634))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(category.progressFloat)
                            .height(4.dp)
                            .background(category.progressColor, RoundedCornerShape(2.dp))
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Status counts row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = category.completedText,
                        color = GreenAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = category.inProgressText,
                        color = OrangeAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFF232330))
                )

                // Topic Items
                category.topics.forEachIndexed { index, topic ->
                    TopicItemRow(
                        topic = topic,
                        onClick = {
                            if (!topic.isLocked) {
                                onTopicClick(topic.topicId, topic.title)
                            }
                        }
                    )
                    if (index < category.topics.size - 1) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFF1E1E28))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopicItemRow(
    topic: LearnTopicItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !topic.isLocked, onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Topic Vector Icon
            TopicIcon(
                topicId = topic.topicId,
                containerSize = 32.dp,
                iconSize = 16.dp,
                isLocked = topic.isLocked
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = topic.title,
                    color = if (topic.isLocked) Color(0xFF9CA3AF) else TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TopicStatusIcon(status = topic.status)
                    TopicLevelBadge(level = topic.level)
                    Text(
                        text = topic.lessonProgressText,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Action Icon (Chevron Arrow or Lock)
        if (topic.isLocked) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E1E28)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = IconLock,
                    contentDescription = "Locked",
                    tint = TextSecondary,
                    modifier = Modifier.size(13.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2A1C12)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = IconChevronRight,
                    contentDescription = null,
                    tint = OrangeAccent,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@Composable
private fun TopicStatusIcon(status: TopicStatus) {
    when (status) {
        TopicStatus.COMPLETED -> {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF064E3B))
                    .border(1.dp, GreenAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = IconCheck,
                    contentDescription = "Completed",
                    tint = GreenAccent,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        TopicStatus.IN_PROGRESS -> {
            val pulseTransition = rememberInfiniteTransition()
            val dotAlpha by pulseTransition.animateFloat(
                initialValue = 0.35f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )

            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, OrangeAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(OrangeAccent)
                        .graphicsLayer { alpha = dotAlpha }
                )
            }
        }
        TopicStatus.NOT_STARTED -> {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, Color(0xFF374151), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF374151))
                )
            }
        }
    }
}

@Composable
private fun TopicLevelBadge(level: TopicLevel) {
    val (bgColor, textColor, label) = when (level) {
        TopicLevel.FOUNDATION -> Triple(Color(0xFF13233A), BlueAccent, "FOUNDATION")
        TopicLevel.INTERMEDIATE -> Triple(Color(0xFF3B1C08), Color(0xFFF97316), "INTERMEDIATE")
        TopicLevel.ADVANCED -> Triple(Color(0xFF2E103E), PurpleAccent, "ADVANCED")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp
        )
    }
}

private fun getEstimatedLessonsText(topicId: String): String = when (topicId) {
    "introduction_ds", "introduction_algo" -> "3 lessons"
    "brute_force" -> "4 lessons"
    "stack", "queues", "heap", "iteration_recursion", "greedy", "branch_bound" -> "5 lessons"
    "hash_tables", "divide_conquer", "backtracking" -> "6 lessons"
    "linked_list" -> "7 lessons"
    "trees", "graphs", "dynamic_programming" -> "8 lessons"
    else -> "5 lessons"
}
