package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.ceil

private val CoinOrange = Color(0xFFFA6E13)
private val CoinBlue = Color(0xFF38BDF8)
private val CoinGreen = Color(0xFF10B981)
private val CoinGray = Color(0xFF9CA3AF)
private fun coinValue(value: Int?): String = when (value) { null -> "–"; CoinChangeDpTrace.INF -> "∞"; else -> value.toString() }
private fun coinSummary(frame: CoinChangeFrame): List<String> = listOf(
    "Testing coin ${frame.step.currentCoin ?: "–"} for amount ${frame.destination ?: "–"} · ascending amounts",
    "Existing: ${coinValue(frame.existingBefore)} · candidate: ${coinValue(frame.candidate)} · " +
        when (frame.improves) { true -> "improves"; false -> "no improvement"; null -> "no comparison" },
    if (frame.step.action == CoinChangeDpAction.FINISHED && frame.step.candidateVal == -1)
        "Result: no solution" else "Target dp[${frame.step.targetAmount}]: ${coinValue(frame.step.dpTable[frame.step.targetAmount])}"
)

@Composable
internal fun CoinChangeDpSceneRenderer(scene: CoinChangePresentation, frame: CoinChangeFrame) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val measurer = rememberTextMeasurer()
        val density = LocalDensity.current
        fun measure(s: String): Pair<Float, Float> {
            val result = measurer.measure(AnnotatedString(s), TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold), softWrap = false)
            return with(density) { ceil(result.size.width.toDp().value) to ceil(result.size.height.toDp().value) }
        }
        fun maximum(values: List<String>): Pair<Float, Float> {
            val measured = values.map(::measure)
            return (measured.maxOfOrNull { it.first } ?: 0f) to (measured.maxOfOrNull { it.second } ?: 0f)
        }
        val layout = remember(scene, maxWidth, density.density, density.fontScale, measurer) {
            val text = maximum(scene.frames.flatMap(::coinSummary) + "1D DP TABLE dp[w] (MIN COINS):")
            val cells = maximum(scene.frames.flatMap { it.step.dpTable }.map(::coinValue) +
                (0 until scene.tableSize).map { "w=$it" })
            val coins = maximum(scene.frames.flatMap { it.step.coins }.map(Int::toString))
            CoinChangeDpSceneLayout.calculate(scene, maxWidth.value, CoinDpMetrics(text.first, maxOf(text.second, cells.second, coins.second), cells.first, coins.first))
        }
        VisualizerCanvasViewport(modifier = Modifier.fillMaxSize(), mode = VisualizerCanvasMode.HYBRID,
            state = rememberVisualizerCanvasState(key = "dp-coin-change", initialScaleMode = VisualizerCanvasInitialScale.AUTO_FIT)) {
            Canvas(Modifier.size(layout.width.dp, layout.height.dp)) {
                fun text(value: String, rect: CoinDpRect, color: Color, center: Boolean = false) {
                    val result = measurer.measure(AnnotatedString(value), TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold), softWrap = false)
                    val x = rect.x.dp.toPx() + if (center) (rect.width.dp.toPx() - result.size.width) / 2f else 0f
                    drawText(result, topLeft = Offset(x, rect.y.dp.toPx() + (rect.height.dp.toPx() - result.size.height) / 2f), color = color)
                }
                fun card(rect: CoinDpRect, border: Color) {
                    val top = Offset(rect.x.dp.toPx(), rect.y.dp.toPx()); val size = Size(rect.width.dp.toPx(), rect.height.dp.toPx())
                    drawRoundRect(Color(0xFF13131C), top, size, CornerRadius(6.dp.toPx()))
                    drawRoundRect(border, top, size, CornerRadius(6.dp.toPx()), style = Stroke(1.dp.toPx()))
                }
                card(layout.table, Color(0xFF262634))
                text("1D DP TABLE dp[w] (MIN COINS):", layout.heading, CoinOrange)
                frame.step.coins.forEachIndexed { i, coin ->
                    val rect = layout.coins[i]; val center = Offset(rect.centerX.dp.toPx(), (rect.y + rect.height / 2f).dp.toPx())
                    val color = if (frame.step.currentCoinIndex == i && frame.destination != null) CoinOrange else CoinBlue
                    drawCircle(color, minOf(rect.width, rect.height).dp.toPx() / 2f - 1.dp.toPx(), center, style = Stroke(1.dp.toPx()))
                    text(coin.toString(), rect, color, true)
                }
                val source = frame.source; val destination = frame.destination
                if (source != null && destination != null) {
                    val start = layout.cells[source].centerX.dp.toPx(); val end = layout.cells[destination].centerX.dp.toPx()
                    val y = (layout.arrowLane.y + 6f).dp.toPx(); val h = 3.dp.toPx()
                    drawLine(CoinBlue, Offset(start, y), Offset(end, y), 1.dp.toPx())
                    drawLine(CoinBlue, Offset(end, y), Offset(end - h, y - h), 1.dp.toPx())
                    drawLine(CoinBlue, Offset(end, y), Offset(end - h, y + h), 1.dp.toPx())
                }
                frame.step.dpTable.forEachIndexed { i, value ->
                    val color = if (i == destination) CoinOrange else if (i == source || value != CoinChangeDpTrace.INF) CoinBlue else CoinGray
                    text("w=$i", layout.indices[i], color, true)
                    card(layout.cells[i], if (i == destination && frame.step.action == CoinChangeDpAction.UPDATE_DP) CoinGreen else color)
                    text(coinValue(value), layout.cells[i], color, true)
                }
                coinSummary(frame).forEachIndexed { i, s -> text(s, layout.summary[i], if (i == 2) CoinGreen else Color.White) }
            }
        }
    }
}
