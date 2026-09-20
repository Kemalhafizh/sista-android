package com.sultanagung1.sista.ui.analytics.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.accessibility.AccessibilityFormatters
import com.sultanagung1.sista.core.designsystem.Emerald500

@Composable
fun SparklineChart(
    points: List<Float>,
    modifier: Modifier = Modifier,
    width: Dp = 60.dp,
    height: Dp = 24.dp,
    color: Color = Emerald500
) {
    val chartSummary = remember(points) {
        AccessibilityFormatters.lineChartSummary("Tren Cepat", points)
    }

    Canvas(
        modifier = modifier
            .size(width, height)
            .semantics(mergeDescendants = true) {
                role = Role.Image
                contentDescription = chartSummary
            }
    ) {
        if (points.size < 2) return@Canvas
        val path = Path()
        val stepX = size.width / (points.size - 1)
        val min = points.minOrNull() ?: 0f
        val max = points.maxOrNull() ?: 1f
        val range = (max - min).coerceAtLeast(0.001f)

        points.forEachIndexed { i, p ->
            val x = i * stepX
            val y = size.height - ((p - min) / range * size.height)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
