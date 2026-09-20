package com.sultanagung1.sista.ui.analytics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.data.model.DailyAttendanceHeatmapItem

@Composable
fun HeatmapCalendar(
    items: List<DailyAttendanceHeatmapItem>,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    Column(modifier = modifier) {
        Text(
            text = "Heatmap Presensi Bulan Agustus 2026",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 5 Rows of 6 Days or 4 Rows of 7 Days Grid
        val chunked = items.chunked(7)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            chunked.forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    week.forEach { day ->
                        val cellColor = when (day.status) {
                            "HADIR" -> Emerald500
                            "IZIN" -> Gold400
                            "SAKIT" -> AccentAmber
                            "TERLAMBAT" -> AccentRose
                            else -> Slate200
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(cellColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${day.dayNumber}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (day.status == "LIBUR") Slate600 else Color.White
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(color = Emerald500, label = "Hadir")
            LegendItem(color = Gold400, label = "Izin")
            LegendItem(color = AccentAmber, label = "Sakit")
            LegendItem(color = AccentRose, label = "Terlambat")
            LegendItem(color = Slate200, label = "Libur", textColor = Slate600)
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String, textColor: Color = Color.Unspecified) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 10.sp, color = textColor)
    }
}
