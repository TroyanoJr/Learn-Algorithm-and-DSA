package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Branch-and-Bound-only details arrangement: badges wrap naturally instead of competing in SpaceBetween. */
@Composable
internal fun BranchBoundResponsiveDetails(
    operationTitle: String,
    badge: String,
    metadata: String,
    explanation: String,
    stepIndex: Int,
    totalSteps: Int,
    completed: Boolean,
    accent: Color
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val compact = maxWidth < 340.dp
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (compact) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(operationTitle, color = Color(0xFF9CA3AF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(badge, color = accent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(metadata, color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(operationTitle, color = Color(0xFF9CA3AF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(badge, color = accent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(metadata, color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            StepsCard("Steps", explanation, stepIndex, totalSteps, isCompleted = completed)
        }
    }
}
