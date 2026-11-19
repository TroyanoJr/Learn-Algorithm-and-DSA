package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal data class LessonComparisonRow(
    val name: String,
    val description: String,
    val complexity: String,
    val positive: Boolean = false
)

private val OuterBackground = Color(0xFF181820)
private val InnerBackground = Color(0xFF121219)
private val Border = Color(0xFF30303E)
private val Orange = Color(0xFFFF8A00)
private val Blue = Color(0xFF38BDF8)
private val Green = Color(0xFF10B981)
private val Primary = Color.White
private val Secondary = Color(0xFF9CA3AF)

@Composable
internal fun LessonComparisonCard(
    title: String,
    rows: List<LessonComparisonRow>,
    badge: String? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        color = OuterBackground,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Border),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = Orange, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                if (badge != null) {
                    Surface(color = Color(0xFF13233A), shape = RoundedCornerShape(5.dp)) {
                        Text(badge, color = Blue, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }
            rows.forEach { row ->
                Surface(
                    color = InnerBackground,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(row.name, color = Primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(row.description, color = Secondary, fontSize = 12.sp, lineHeight = 17.sp)
                        Text(row.complexity, color = if (row.positive) Green else Orange,
                            fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
