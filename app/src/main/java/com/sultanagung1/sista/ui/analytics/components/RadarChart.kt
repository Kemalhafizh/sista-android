package com.sultanagung1.sista.ui.analytics.components

import androidx.compose.animation.Crossfade
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.accessibility.AccessibilityFormatters
import com.sultanagung1.sista.core.accessibility.sulaoneInteractiveTouchTarget
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.data.model.RadarAxisPoint
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun RadarChart(
    data: List<RadarAxisPoint>,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(260.dp),
    allowTableToggle: Boolean = true
) {
    if (data.isEmpty()) return

    val numAxes = data.size
    val angleStep = (2 * Math.PI / numAxes).toFloat()
    var showDataTable by remember { mutableStateOf(false) }

    val chartSummary = remember(data) {
        AccessibilityFormatters.radarChartSummary(
            "Grafik Radar Kompetensi",
            data.map { it.label to it.value }
        )
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Crossfade(targetState = showDataTable, label = "RadarChartCrossfade") { isTable ->
            if (isTable) {
                // Accessible Tabular Representation (WCAG 1.3.1 Info and Relationships)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Tabel Capaian Kompetensi KKTP",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    data.forEach { point ->
                        val isPassed = point.value >= 75f
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = point.label,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${point.value.roundToInt()} Poin",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isPassed) Emerald700 else AccentRose
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            SulaoneBadge(
                                text = if (isPassed) "Tuntas" else "Remedial",
                                containerColor = if (isPassed) Emerald100 else AccentRose.copy(alpha = 0.15f),
                                contentColor = if (isPassed) Emerald800 else AccentRose
                            )
                        }
                    }
                }
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                        .semantics(mergeDescendants = true) {
                            role = Role.Image
                            contentDescription = chartSummary
                        }
                ) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = minOf(size.width, size.height) / 2 * 0.85f

                    // 1. Draw Concentric Polygon Web Levels (25%, 50%, 75% KKTP, 100%)
                    val levels = listOf(0.25f, 0.5f, 0.75f, 1.0f)
                    levels.forEach { level ->
                        val levelRadius = radius * level
                        val polygonPath = Path()

                        for (i in 0 until numAxes) {
                            val angle = -Math.PI.toFloat() / 2 + i * angleStep
                            val x = center.x + levelRadius * cos(angle)
                            val y = center.y + levelRadius * sin(angle)
                            if (i == 0) polygonPath.moveTo(x, y) else polygonPath.lineTo(x, y)
                        }
                        polygonPath.close()

                        drawPath(
                            path = polygonPath,
                            color = if (level == 0.75f) Gold500.copy(alpha = 0.6f) else Slate300.copy(alpha = 0.5f),
                            style = Stroke(width = if (level == 0.75f) 2.dp.toPx() else 1.dp.toPx())
                        )
                    }

                    // 2. Draw Spokes (Center to Each Vertex)
                    for (i in 0 until numAxes) {
                        val angle = -Math.PI.toFloat() / 2 + i * angleStep
                        val x = center.x + radius * cos(angle)
                        val y = center.y + radius * sin(angle)
                        drawLine(
                            color = Slate300.copy(alpha = 0.6f),
                            start = center,
                            end = Offset(x, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // 3. Draw Actual Data Polygon (Fill & Outline)
                    val dataPath = Path()
                    val dataPoints = mutableListOf<Offset>()

                    data.forEachIndexed { i, point ->
                        val normValue = (point.value / 100f).coerceIn(0f, 1f)
                        val pointRadius = radius * normValue
                        val angle = -Math.PI.toFloat() / 2 + i * angleStep
                        val x = center.x + pointRadius * cos(angle)
                        val y = center.y + pointRadius * sin(angle)
                        val pt = Offset(x, y)
                        dataPoints.add(pt)

                        if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
                    }
                    dataPath.close()

                    // Fill
                    drawPath(
                        path = dataPath,
                        color = Emerald600.copy(alpha = 0.35f),
                        style = Fill
                    )

                    // Border
                    drawPath(
                        path = dataPath,
                        color = Emerald700,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Points
                    dataPoints.forEach { pt ->
                        drawCircle(
                            color = Emerald800,
                            radius = 5.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.5.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }
        }

        // Accessibility Table Toggle Button (WCAG 2.2 AA Minimum 48x48dp touch target)
        if (allowTableToggle) {
            IconButton(
                onClick = { showDataTable = !showDataTable },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .sulaoneInteractiveTouchTarget(48.dp)
                    .semantics {
                        contentDescription = if (showDataTable)
                            "Kembali ke tampilan grafik radar visual"
                        else
                            "Beralih ke tabel data alternatif aksesibilitas"
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
