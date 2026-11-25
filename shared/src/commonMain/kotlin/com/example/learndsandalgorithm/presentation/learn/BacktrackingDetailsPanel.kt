package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
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

internal data class BacktrackingDetailsAllocation(val operationHeight: Float, val stepsHeight: Float, val gap: Float)

internal fun backtrackingDetailsAllocation(availableHeight: Float, preferredOperation: Float = Float.POSITIVE_INFINITY,
                                           minimumSteps: Float = 0f): BacktrackingDetailsAllocation {
    val height = availableHeight.coerceAtLeast(0f)
    val gap = minOf(6f, height / 10f)
    val content = height - gap
    val minimum = if (minimumSteps <= 0f) content * .56f else minimumSteps.coerceIn(0f, content)
    val operationBudget = if (minimum <= content * .65f) content - minimum else content * .35f
    val operation = minOf(preferredOperation.coerceAtLeast(0f), operationBudget)
    return BacktrackingDetailsAllocation(operation, content - operation, gap)
}

/** Each finite region owns its own scroll; playback remains in the surrounding modal. */
@Composable
internal fun BacktrackingDetailsPanel(
    heading: String, operation: String, operationColor: Color, metadata: String,
    explanation: String, stepIndex: Int, totalSteps: Int, isCompleted: Boolean
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val measurer = rememberTextMeasurer()
        val baseStyle = LocalTextStyle.current
        val width = with(density) { maxWidth.roundToPx() }.coerceAtLeast(1)
        val (allocation, stackedHeader) = remember(heading, operation, metadata, explanation, totalSteps, maxWidth, maxHeight, density.density, density.fontScale, measurer, baseStyle) {
            fun height(text: String, style: TextStyle, widthPx: Int): Float = with(density) {
                measurer.measure(AnnotatedString(text), style, constraints = Constraints(maxWidth = widthPx.coerceAtLeast(1))).size.height.toDp().value
            }
            val operationHeight = height(heading, baseStyle.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), width) +
                height(operation, baseStyle.copy(fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold), width) +
                height(metadata, baseStyle.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), width) + 4f
            val stepsWidth = (width - with(density) { 16.dp.roundToPx() }).coerceAtLeast(1)
            val labelStyle = baseStyle.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold)
            val counterStyle = baseStyle.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold)
            val labelHeight = height("Steps", labelStyle, stepsWidth)
            val counterHeight = height("STEP $totalSteps / $totalSteps", counterStyle, stepsWidth)
            val labelWidth = measurer.measure(AnnotatedString("Steps"), labelStyle, softWrap = false).size.width
            val counterWidth = measurer.measure(AnnotatedString("STEP $totalSteps / $totalSteps"), counterStyle, softWrap = false).size.width
            val stacked = labelWidth + counterWidth + with(density) { 8.dp.roundToPx() } > stepsWidth
            val stepsHeader = if (stacked) labelHeight + counterHeight else maxOf(labelHeight, counterHeight)
            val firstLine = with(density) { measurer.measure(AnnotatedString(explanation), baseStyle.copy(fontSize = 13.sp, lineHeight = 18.sp),
                maxLines = 1, constraints = Constraints(maxWidth = stepsWidth)).size.height.toDp().value }
            backtrackingDetailsAllocation(maxHeight.value, operationHeight, stepsHeader + firstLine + 14f) to stacked
        }
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(allocation.gap.dp)) {
            Column(
                Modifier.fillMaxWidth().height(allocation.operationHeight.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(heading, color = Color(0xFF9CA3AF), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Text(operation, color = operationColor, fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                Text(metadata, color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
            }
            Box(Modifier.fillMaxWidth().height(allocation.stepsHeight.dp).verticalScroll(rememberScrollState())) {
                Surface(color = Color(0xFF161622), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        if (stackedHeader) {
                            Column {
                                Text("Steps", color = Color(0xFFFA6E13), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("STEP ${stepIndex + 1} / $totalSteps", color = if (isCompleted) Color(0xFF10B981) else Color(0xFF9CA3AF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Steps", color = Color(0xFFFA6E13), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("STEP ${stepIndex + 1} / $totalSteps", color = if (isCompleted) Color(0xFF10B981) else Color(0xFF9CA3AF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(explanation, color = Color.White, fontSize = 13.sp, lineHeight = 18.sp)
                    }
                }
            }
        }
    }
}
