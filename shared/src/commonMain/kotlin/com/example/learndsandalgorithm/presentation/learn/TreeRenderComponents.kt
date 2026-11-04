package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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

internal val TreeCardBorderColor = Color(0xFF262634)
internal val TreeOrangeAccent = Color(0xFFFA6E13)
internal val TreeBlueAccent = Color(0xFF38BDF8)
internal val TreeGreenAccent = Color(0xFF10B981)
internal val TreeRedAccent = Color(0xFFEF4444)
internal val TreeTextPrimary = Color(0xFFFFFFFF)
internal val TreeTextSecondary = Color(0xFF9CA3AF)

@Composable
internal fun TreeNodeCircle(
    value: String,
    isHighlighted: Boolean = false,
    isVisited: Boolean = false,
    isViolation: Boolean = false,
    isTarget: Boolean = false,
    badgeLabel: String? = null,
    nodeSize: Dp = 42.dp
) {
    val bgColor = when {
        isViolation -> Color(0xFF381212)
        isTarget || isHighlighted -> Color(0xFF2A1C12)
        isVisited -> Color(0xFF123820)
        else -> Color(0xFF13233A)
    }

    val borderColor = when {
        isViolation -> TreeRedAccent
        isTarget || isHighlighted -> TreeOrangeAccent
        isVisited -> TreeGreenAccent
        else -> TreeCardBorderColor
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        if (badgeLabel != null) {
            Text(
                text = badgeLabel,
                color = if (isViolation) TreeRedAccent else if (isHighlighted) TreeOrangeAccent else TreeBlueAccent,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Surface(
            color = bgColor,
            shape = CircleShape,
            border = BorderStroke(1.5.dp, borderColor),
            modifier = Modifier.size(nodeSize)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = value,
                    color = if (isViolation) TreeRedAccent else if (isHighlighted) TreeOrangeAccent else TreeTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
internal fun TreeBranchConnector(
    label: String = "↙   ↘",
    color: Color = TreeBlueAccent
) {
    Text(
        text = label,
        color = color,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 1.dp)
    )
}
