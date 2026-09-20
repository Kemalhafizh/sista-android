package com.sultanagung1.sista.ui.home.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.util.DateUtils
import com.sultanagung1.sista.data.model.ScheduleItem
import kotlinx.coroutines.delay

/**
 * FASE 71.1 — "Widget Pembuka: countdown jadwal pelajaran berikutnya"
 * ("Fisika: Lab 2 — 15 menit lagi"). Ticks every 30s and recomputes against
 * [DateUtils.nowMinutesOfDay] (server-corrected — see [DateUtils]), so a
 * student changing their phone's clock can't make this widget lie about how
 * much time is actually left.
 */
@Composable
internal fun HomeNextClassCountdown(todaySchedules: List<ScheduleItem>) {
    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000L)
            tick++
        }
    }

    val nowMinutes = remember(tick) { DateUtils.nowMinutesOfDay() }

    val next = remember(todaySchedules, nowMinutes) {
        todaySchedules
            .mapNotNull { item ->
                val start = DateUtils.parseMinutesOfDay(item.startTime) ?: return@mapNotNull null
                val end = DateUtils.parseMinutesOfDay(item.endTime) ?: return@mapNotNull null
                Triple(item, start, end)
            }
            .filter { (_, _, end) -> end > nowMinutes }
            .minByOrNull { (_, start, _) -> start }
    }

    val (item, start, end) = next ?: return
    val isOngoing = nowMinutes >= start
    val minutesUntil = (start - nowMinutes).coerceAtLeast(0)
    val minutesRemaining = (end - nowMinutes).coerceAtLeast(0)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isOngoing) Emerald50 else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            if (isOngoing) Emerald200 else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (isOngoing) Emerald700 else Slate100),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = if (isOngoing) androidx.compose.ui.graphics.Color.White else Slate600,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${item.subjectName}: ${item.room}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isOngoing) {
                        "Sedang berlangsung — $minutesRemaining menit lagi selesai"
                    } else {
                        "$minutesUntil menit lagi"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = if (isOngoing) Emerald800 else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isOngoing) {
                LiveStatusChip("Berlangsung", color = Emerald700)
            }
        }
    }
}
