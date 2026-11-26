package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal data class DpDetailsFit(val operationHeight: Float, val stepsHeight: Float, val spacing: Float, val available: Float) {
    val required: Float get() = operationHeight + stepsHeight + spacing
    val deficit: Float get() = (required - available).coerceAtLeast(0f)
    val fits: Boolean get() = deficit == 0f
}

@Composable
internal fun DynamicProgrammingDetailsPanel(
    heading: String, operation: String, operationColor: Color, metadata: String,
    explanation: String, stepIndex: Int, totalSteps: Int, isCompleted: Boolean
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val base = LocalTextStyle.current
        val measurer = rememberTextMeasurer()
        val width = with(density) { maxWidth.roundToPx().coerceAtLeast(1) }
        val fit = remember(heading, operation, metadata, explanation, totalSteps, maxWidth, maxHeight,
            density.density, density.fontScale, base, measurer) {
            fun height(text: String, style: TextStyle) = with(density) {
                measurer.measure(AnnotatedString(text), style, constraints = Constraints(maxWidth = width)).size.height.toDp().value
            }
            val header = base.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold)
            val badge = base.copy(fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight.Bold)
            val meta = base.copy(fontSize = 10.sp, lineHeight = 13.sp, fontWeight = FontWeight.Bold)
            val step = base.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold)
            val body = base.copy(fontSize = 12.sp, lineHeight = 16.sp)
            val operationHeight = height(heading, header) + height(operation, badge) + height(metadata, meta) + 4f
            val stepsHeight = height("Steps · STEP $totalSteps / $totalSteps", step) + height(explanation, body) + 2f
            DpDetailsFit(operationHeight, stepsHeight, 6f, maxHeight.value)
        }
        Column(
            Modifier.fillMaxSize().let { if (fit.fits) it else it.verticalScroll(rememberScrollState()) },
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(heading, color = Color(0xFF9CA3AF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(operation, color = operationColor, fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
            Text(metadata, color = Color(0xFF10B981), fontSize = 10.sp, lineHeight = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth()) {
                Text("Steps · STEP ${stepIndex + 1} / $totalSteps", color = if (isCompleted) Color(0xFF10B981) else Color(0xFFFA6E13), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Text(explanation, color = Color.White, fontSize = 12.sp, lineHeight = 16.sp, modifier = Modifier.fillMaxWidth())
        }
    }
}
