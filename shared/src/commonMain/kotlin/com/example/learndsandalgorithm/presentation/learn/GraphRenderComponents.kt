package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal val GraphCardBorderColor = Color(0xFF262634)
internal val GraphOrangeAccent = Color(0xFFFA6E13)
internal val GraphBlueAccent = Color(0xFF38BDF8)
internal val GraphGreenAccent = Color(0xFF10B981)
internal val GraphRedAccent = Color(0xFFEF4444)
internal val GraphTextPrimary = Color(0xFFFFFFFF)
internal val GraphTextSecondary = Color(0xFF9CA3AF)

@Composable
internal fun GraphVertexCircle(
    label: String,
    isActive: Boolean = false,
    isVisited: Boolean = false,
    isQueued: Boolean = false,
    isCycle: Boolean = false,
    badgeText: String? = null,
    vertexSize: Dp = 38.dp
) {
    val bgColor = when {
        isCycle -> Color(0xFF381212)
        isActive -> Color(0xFF2A1C12)
        isVisited -> Color(0xFF123820)
        isQueued -> Color(0xFF122838)
        else -> Color(0xFF13233A)
    }

    val borderColor = when {
        isCycle -> GraphRedAccent
        isActive -> GraphOrangeAccent
        isVisited -> GraphGreenAccent
        isQueued -> GraphBlueAccent
        else -> GraphCardBorderColor
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        if (badgeText != null) {
            Text(
                text = badgeText,
                color = if (isCycle) GraphRedAccent else if (isActive) GraphOrangeAccent else GraphBlueAccent,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Surface(
            color = bgColor,
            shape = CircleShape,
            border = BorderStroke(1.5.dp, borderColor),
            modifier = Modifier.size(vertexSize)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = label,
                    color = if (isCycle) GraphRedAccent else if (isActive) GraphOrangeAccent else GraphTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun GraphQueueRow(
    title: String = "QUEUE (FRONT → REAR):",
    items: List<String>,
    accentColor: Color = GraphBlueAccent
) {
    Surface(
        color = Color(0xFF13131C),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, GraphCardBorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, color = accentColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            if (items.isEmpty()) {
                Text("Empty", color = GraphTextSecondary, fontSize = 9.sp)
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items.forEach { item ->
                        Surface(
                            color = Color(0xFF122838),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, accentColor)
                        ) {
                            Text(
                                text = item,
                                color = accentColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
