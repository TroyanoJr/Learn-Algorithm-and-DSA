package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val TradeoffLessonBlue = Color(0xFF60A5FA)
private val TradeoffLessonGreen = Color(0xFF4ADE80)
private val TradeoffLessonOrange = Color(0xFFFBBF24)
private val TradeoffLessonSecondary = Color(0xFFB8C3D1)

@Composable
internal fun DivideConquerTradeoffsComparisonLessonContent() {
    val comparisonSteps = remember {
        DivideConquerTradeoffsTrace.stepsForScenario(TradeoffScenario.ALGORITHM_COMPARISON)
    }
    Surface(
        color = Color(0xFF171D27),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("MERGE SORT VS QUICK SORT", color = TradeoffLessonBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            comparisonSteps.forEach { step ->
                Text(step.explanation, color = TradeoffLessonSecondary, fontSize = 12.sp, lineHeight = 17.sp)
            }

            Text("COMPLEXITY & TRADE-OFF MATRIX", color = TradeoffLessonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            DivideConquerTradeoffsSceneBuilder.comparisonRows.forEach { row ->
                Surface(color = Color(0xFF202735), shape = RoundedCornerShape(7.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(row.property, color = TradeoffLessonBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Merge Sort: ${row.mergeSort}", color = TradeoffLessonGreen, fontSize = 11.sp, lineHeight = 15.sp)
                        Text("Quick Sort: ${row.quickSort}", color = TradeoffLessonOrange, fontSize = 11.sp, lineHeight = 15.sp)
                    }
                }
            }

            Text("STABILITY AND PARTITION EXAMPLE", color = TradeoffLessonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(
                "Input: ${DivideConquerTradeoffsSceneBuilder.inputRecords.joinToString(", ") { it.label }}",
                color = TradeoffLessonSecondary,
                fontSize = 11.sp
            )
            Text(
                "Merge Sort stable output: ${DivideConquerTradeoffsSceneBuilder.mergeSortedRecords.joinToString(", ") { it.label }}",
                color = TradeoffLessonGreen,
                fontSize = 11.sp
            )
            Text(
                "Quick Sort after one partition: ${DivideConquerTradeoffsSceneBuilder.quickPartitionRecords.joinToString(", ") { it.label }}",
                color = TradeoffLessonOrange,
                fontSize = 11.sp
            )
            Text(
                "ONE LOMUTO PARTITION ONLY · 5A and 5B swap relative order; array is not fully sorted.",
                color = TradeoffLessonSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

internal fun hasDivideConquerTradeoffsComparisonLessonContent(lessonId: String): Boolean =
    lessonId == "lesson_divide_conquer_6"
