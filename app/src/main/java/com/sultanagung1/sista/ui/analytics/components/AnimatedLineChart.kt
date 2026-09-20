package com.sultanagung1.sista.ui.analytics.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.accessibility.AccessibilityFormatters
import com.sultanagung1.sista.core.accessibility.sulaoneInteractiveTouchTarget
import com.sultanagung1.sista.core.designsystem.Emerald500
import kotlin.math.roundToInt

@Composable
fun AnimatedLineChart(
    points: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = Emerald500,
    title: String = "Grafik Garis Perkembangan",
    allowTableToggle: Boolean = false
) {
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        animationProgress.animateTo(1f, animationSpec = tween(1000))
    }

    var showDataTable by remember { mutableStateOf(false) }
    val chartSummary = remember(points, title) {
        AccessibilityFormatters.lineChartSummary(title, points)
    }

    Box(modifier = modifier) {
        Crossfade(targetState = showDataTable, label = "LineChartCrossfade") { isTable ->
            if (isTable) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Tabel Rincian Data Garis",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    points.forEachIndexed { index, point ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Titik ${index + 1}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${point.roundToInt()}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = lineColor
                            )
                        }
                    }
                }
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics(mergeDescendants = true) {
                            role = Role.Image
                            contentDescription = chartSummary
                        }
                ) {
                    if (points.isEmpty()) return@Canvas
                    val path = Path()
                    val stepX = size.width / (points.size - 1).coerceAtLeast(1)
                    val maxPoint = points.maxOrNull() ?: 1f
                    points.forEachIndexed { index, point ->
                        val x = index * stepX
                        val y = size.height - (point / maxPoint * size.height) * animationProgress.value
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }

        if (allowTableToggle) {
            IconButton(
                onClick = { showDataTable = !showDataTable },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .sulaoneInteractiveTouchTarget(48.dp)
                    .semantics {
                        contentDescription = if (showDataTable)
                            "Kembali ke grafik garis visual"
                        else
                            "Beralih ke tabel data garis"
                    }
            ) {
                Icon(
                    imageVector = if (showDataTable) Icons.Default.ShowChart else Icons.Default.TableRows,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
