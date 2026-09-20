package com.sultanagung1.sista.ui.analytics.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.accessibility.AccessibilityFormatters
import com.sultanagung1.sista.core.accessibility.sulaoneInteractiveTouchTarget
import kotlin.math.roundToInt

data class DonutSegment(
    val value: Float,
    val color: Color,
    val label: String
)

@Composable
fun AnimatedDonutChart(
    segments: List<DonutSegment>,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    strokeWidth: Dp = 24.dp,
    centerLabel: String? = null,
    allowTableToggle: Boolean = false
) {
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(segments) {
        animationProgress.animateTo(1f, animationSpec = tween(1200))
    }

    val total = segments.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(1f)
    var showDataTable by remember { mutableStateOf(false) }

    val chartSummary = remember(segments, centerLabel) {
        AccessibilityFormatters.donutChartSummary(
            title = centerLabel ?: "Grafik Donat Distribusi",
            segments = segments.map { it.label to it.value }
        )
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Crossfade(targetState = showDataTable, label = "DonutChartCrossfade") { isTable ->
            if (isTable) {
                // Accessible Tabular Representation of Segments
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center
                ) {
                    segments.forEach { segment ->
                        val pct = ((segment.value / total) * 100f).roundToInt()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(segment.color)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = segment.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "$pct%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .matchParentSize()
                            .semantics(mergeDescendants = true) {
                                role = Role.Image
                                contentDescription = chartSummary
                            }
                    ) {
                        var currentAngle = -90f
                        segments.forEach { segment ->
                            val sweep = (segment.value / total * 360f) * animationProgress.value
                            drawArc(
                                color = segment.color,
                                startAngle = currentAngle,
                                sweepAngle = sweep,
                                useCenter = false,
                                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                            )
                            currentAngle += sweep
                        }
                    }

                    centerLabel?.let { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
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
                            "Kembali ke tampilan grafik donat"
                        else
                            "Beralih ke tabel data donat"
                    }
            ) {
                Icon(
                    imageVector = if (showDataTable) Icons.Default.PieChart else Icons.Default.TableRows,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
