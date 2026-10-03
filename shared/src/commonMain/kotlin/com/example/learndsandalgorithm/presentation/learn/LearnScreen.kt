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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkBgColor = Color(0xFF111115)
private val DarkCardBgColor = Color(0xFF181820)
private val CardBorderColor = Color(0xFF262634)
private val OrangeAccent = Color(0xFFFA6E13)
private val BlueAccent = Color(0xFF38BDF8)
private val PurpleAccent = Color(0xFF9E66FF)
private val GreenAccent = Color(0xFF10B981)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9CA3AF)

private enum class TopicStatus {
    COMPLETED, IN_PROGRESS, NOT_STARTED
}

private enum class TopicLevel {
    FOUNDATION, INTERMEDIATE, ADVANCED
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

@Composable
fun LearnScreen(
    onTopicClick: (topicId: String, topicTitle: String) -> Unit = { _, _ -> }
) {
    var expandedCategoryId by remember { mutableStateOf<String?>("data_structures") }

    val categories = remember {
        listOf(
            CourseCategory(
                id = "data_structures",
                title = "DATA STRUCTURES",
                topicsCountText = "10 topics",
                percentageProgressText = "20%",
                progressFloat = 0.20f,
                completedText = "1 in progress",
                inProgressText = "9 not started",
                progressColor = OrangeAccent,
                topics = listOf(
                    LearnTopicItem(
                        topicId = "introduction_ds",
                        title = "Introduction",
                        level = TopicLevel.FOUNDATION,
                        lessonProgressText = "3 lessons",
                        status = TopicStatus.NOT_STARTED,
                        isLocked = true
                    ),
                    LearnTopicItem(
                        topicId = "arrays",
                        title = "Arrays",
                        level = TopicLevel.FOUNDATION,
                        lessonProgressText = "5 lessons complete",
                        status = TopicStatus.IN_PROGRESS,
                        isLocked = false
                    ),
                    LearnTopicItem(
                        topicId = "structures",
                        title = "Structures",
                        level = TopicLevel.FOUNDATION,
                        lessonProgressText = "4 lessons",
                        status = TopicStatus.NOT_STARTED,
                        isLocked = true
                    ),
                    LearnTopicItem(
                        topicId = "linked_list",
                        title = "Linked List",
                        level = TopicLevel.FOUNDATION,
                        lessonProgressText = "7 lessons",
                        status = TopicStatus.NOT_STARTED,
                        isLocked = true
                    ),
                    LearnTopicItem(
                        topicId = "stack",
                        title = "Stack",
                        level = TopicLevel.FOUNDATION,
                        lessonProgressText = "5 lessons",
                        status = TopicStatus.NOT_STARTED,
                        isLocked = true
                    ),
                    LearnTopicItem(
                        topicId = "queues",
                        title = "Queues",
                        level = TopicLevel.FOUNDATION,
                        lessonProgressText = "5 lessons",
                        status = TopicStatus.NOT_STARTED,
                        isLocked = true
                    ),
                    LearnTopicItem(
                        topicId = "hash_tables",
                        title = "Hash Tables",
                        level = TopicLevel.INTERMEDIATE,
                        lessonProgressText = "6 lessons",
                        status = TopicStatus.NOT_STARTED,
                        isLocked = true
                    ),
                    LearnTopicItem(
                        topicId = "trees",
                        title = "Trees",
                        level = TopicLevel.INTERMEDIATE,
                        lessonProgressText = "8 lessons",
                        status = TopicStatus.NOT_STARTED,
                        isLocked = true
                    ),
                    LearnTopicItem(
                        topicId = "heap",
                        title = "Heap",
                        level = TopicLevel.INTERMEDIATE,
                        lessonProgressText = "5 lessons",
                        status = TopicStatus.NOT_STARTED,
                        isLocked = true
                    ),
                    LearnTopicItem(
                        topicId = "graphs",
                        title = "Graphs",
                        level = TopicLevel.ADVANCED,
                        lessonProgressText = "8 lessons",
                        status = TopicStatus.NOT_STARTED,
                        isLocked = true
                    )
                )
            ),
            CourseCategory(
                id = "algorithms",
                title = "ALGORITHMS",
                topicsCountText = "10 topics",
                percentageProgressText = "10%",
                progressFloat = 0.10f,
                completedText = "1 completed",
                inProgressText = "9 not started",
                progressColor = PurpleAccent,
                topics = listOf(
                    LearnTopicItem(
                        topicId = "introduction_algo",
                        title = "Introduction",
                        level = TopicLevel.FOUNDATION,
                        lessonProgressText = "3 lessons",
                        status = TopicStatus.NOT_STARTED,
                        isLocked = true
                    ),
                    LearnTopicItem(
                        topicId = "linear_search",
                        title = "Searching",
                        level = TopicLevel.FOUNDATION,
                        lessonProgressText = "3 lessons",
                        status = TopicStatus.COMPLETED,
                        isLocked = false
                    ),
                    LearnTopicItem(
                        topicId = "sorting",
                        title = "Sorting",
                        level = TopicLevel.FOUNDATION,
                        lessonProgressText = "3 lessons",
                        status = TopicStatus.IN_PROGRESS,
                        isLocked = false
                    ),
                    LearnTopicItem(
                        topicId = "brute_force",
                        title = "Brute Force",
                        level = TopicLevel.FOUNDATION,
                        lessonProgressText = "4 lessons",
                        status = TopicStatus.NOT_STARTED,
                        isLocked = true
                    ),
                    LearnTopicItem(
                        topicId = "iteration_recursion",
                        title = "Iteration & Recursion",
                        level = TopicLevel.INTERMEDIATE,
                        lessonProgressText = "5 lessons",
                        status = TopicStatus.NOT_STARTED,
                        isLocked = true
                    ),
                    LearnTopicItem(
                        topicId = "divide_conquer",
                        title = "Divide and Conquer",
                        level = TopicLevel.INTERMEDIATE,
                        lessonProgressText = "6 lessons",
                        status = TopicStatus.NOT_STARTED,
                        isLocked = true
                    ),
                    LearnTopicItem(
                        topicId = "greedy",
                        title = "Greedy",
                        level = TopicLevel.INTERMEDIATE,
                        lessonProgressText = "5 lessons",
                        status = TopicStatus.NOT_STARTED,
                        isLocked = true
                    ),
                    LearnTopicItem(
                        topicId = "backtracking",
                        title = "Backtracking",
                        level = TopicLevel.ADVANCED,
                        lessonProgressText = "6 lessons",
                        status = TopicStatus.NOT_STARTED,
                        isLocked = true
                    ),
                    LearnTopicItem(
                        topicId = "dynamic_programming",
                        title = "Dynamic Programming",
                        level = TopicLevel.ADVANCED,
                        lessonProgressText = "8 lessons",
                        status = TopicStatus.NOT_STARTED,
                        isLocked = true
                    ),
                    LearnTopicItem(
                        topicId = "branch_bound",
                        title = "Branch and Bound",
                        level = TopicLevel.ADVANCED,
                        lessonProgressText = "5 lessons",
                        status = TopicStatus.NOT_STARTED,
                        isLocked = true
                    )
                )
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBgColor)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        LearnHeader()
        CourseProgressOverview()
        
        Spacer(modifier = Modifier.height(4.dp))

        categories.forEach { category ->
            val isExpanded = category.id == expandedCategoryId
            CourseCategoryCard(
                category = category,
                isExpanded = isExpanded,
                onToggleExpand = {
                    expandedCategoryId = if (isExpanded) null else category.id
                },
                onTopicClick = onTopicClick
            )
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
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "What will you",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 32.sp
            )
            Text(
                text = "learn today?",
                color = OrangeAccent,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 32.sp
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
private fun CourseProgressOverview() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "2 of 20 topics complete",
                color = TextSecondary,
                fontSize = 14.sp
            )
            Text(
                text = "10%",
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
                    .fillMaxWidth(0.10f)
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
                .padding(18.dp)
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
                            .height(28.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(BlueAccent)
                    )

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                    Text(
                        text = if (isExpanded) "▲" else "▼",
                        color = BlueAccent,
                        fontSize = 12.sp
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))

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

                Spacer(modifier = Modifier.height(10.dp))

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

                Spacer(modifier = Modifier.height(16.dp))
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
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Status Icon
            TopicStatusIcon(status = topic.status)

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = topic.title,
                    color = if (topic.isLocked) Color(0xFF9CA3AF) else TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E1E28)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🔒",
                    fontSize = 12.sp
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2A1C12)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "›",
                    color = OrangeAccent,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
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
                Text(
                    text = "✓",
                    color = GreenAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        TopicStatus.IN_PROGRESS -> {
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
