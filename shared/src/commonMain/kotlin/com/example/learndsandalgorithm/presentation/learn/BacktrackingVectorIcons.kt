package com.example.learndsandalgorithm.presentation.learn

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.sqrt

internal enum class BacktrackingIcon { QUEEN, SEARCH, CROSS, POSITION, TARGET, PATH, OBSTACLE, FREE }
internal data class BacktrackingIconBounds(val x: Float, val y: Float, val size: Float)
internal data class BacktrackingConnection(val startX: Float, val startY: Float, val endX: Float, val endY: Float)
internal fun backtrackingIconBounds(x: Float, y: Float, width: Float, height: Float): BacktrackingIconBounds {
    val size = minOf(width, height).coerceAtLeast(0f) * .52f
    return BacktrackingIconBounds(x + (width - size) / 2f, y + (height - size) / 2f, size)
}

internal fun backtrackingConnection(a: BacktrackingIconBounds, b: BacktrackingIconBounds): BacktrackingConnection {
    val ax = a.x + a.size / 2f; val ay = a.y + a.size / 2f
    val bx = b.x + b.size / 2f; val by = b.y + b.size / 2f
    val dx = bx - ax; val dy = by - ay
    val distance = sqrt(dx * dx + dy * dy).coerceAtLeast(.001f)
    val first = minOf(a.size * .72f, distance / 3f)
    val second = minOf(b.size * .72f, distance / 3f)
    return BacktrackingConnection(ax + dx / distance * first, ay + dy / distance * first,
        bx - dx / distance * second, by - dy / distance * second)
}

internal fun DrawScope.drawBacktrackingIcon(icon: BacktrackingIcon, bounds: BacktrackingIconBounds, color: Color) {
    val x = bounds.x.dp.toPx(); val y = bounds.y.dp.toPx(); val s = bounds.size.dp.toPx()
    if (s <= 0f) return
    val stroke = minOf(1.8.dp.toPx(), s * .09f)
    fun p(a: Float, b: Float) = Offset(x + s * a, y + s * b)
    fun line(a: Float, b: Float, c: Float, d: Float) = drawLine(color, p(a, b), p(c, d), stroke, cap = StrokeCap.Round)
    when (icon) {
        BacktrackingIcon.QUEEN -> {
            val path = Path().apply { moveTo(x + s * .12f, y + s * .30f); lineTo(x + s * .30f, y + s * .48f); lineTo(x + s * .50f, y + s * .16f); lineTo(x + s * .70f, y + s * .48f); lineTo(x + s * .88f, y + s * .30f); lineTo(x + s * .78f, y + s * .78f); lineTo(x + s * .22f, y + s * .78f); close() }
            drawPath(path, color, style = Stroke(stroke))
            line(.22f, .87f, .78f, .87f)
        }
        BacktrackingIcon.SEARCH -> {
            drawCircle(color, s * .22f, p(.40f, .40f), style = Stroke(stroke))
            line(.56f, .56f, .82f, .82f)
        }
        BacktrackingIcon.CROSS -> { line(.22f, .22f, .78f, .78f); line(.78f, .22f, .22f, .78f) }
        BacktrackingIcon.POSITION -> {
            drawCircle(color, s * .22f, p(.50f, .37f), style = Stroke(stroke))
            line(.30f, .49f, .50f, .84f); line(.70f, .49f, .50f, .84f)
        }
        BacktrackingIcon.TARGET -> {
            drawCircle(color, s * .34f, p(.5f, .5f), style = Stroke(stroke))
            drawCircle(color, s * .17f, p(.5f, .5f), style = Stroke(stroke))
            drawCircle(color, s * .045f, p(.5f, .5f))
        }
        BacktrackingIcon.PATH -> { line(.18f, .50f, .40f, .72f); line(.40f, .72f, .82f, .25f) }
        BacktrackingIcon.OBSTACLE -> {
            drawRect(color, p(.18f, .18f), Size(s * .64f, s * .64f), style = Stroke(stroke))
            line(.25f, .72f, .72f, .25f)
        }
        BacktrackingIcon.FREE -> drawRect(color, p(.22f, .22f), Size(s * .56f, s * .56f), style = Stroke(stroke))
    }
}
