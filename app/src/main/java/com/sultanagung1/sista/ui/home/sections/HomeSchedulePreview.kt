package com.sultanagung1.sista.ui.home.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.accessibility.sulaoneHeading
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.motion.springPressable
import com.sultanagung1.sista.core.motion.sulaoneSharedBounds
import com.sultanagung1.sista.data.model.ScheduleItem

@Composable
internal fun HomeSchedulePreview(
    todaySchedules: List<ScheduleItem>,
    onNavigateToSchedule: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Jadwal Pelajaran Hari Ini",
                modifier = Modifier.sulaoneHeading("Jadwal Pelajaran Hari Ini"),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Lihat Kalender",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Emerald700,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .springPressable { onNavigateToSchedule() }
                    .padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (todaySchedules.isEmpty()) {
            SulaoneEmptyState(
                title = "Tidak Ada Jadwal Hari Ini",
                description = "Belum ada jadwal pelajaran yang tercatat untuk hari ini.",
                icon = Icons.Default.EventBusy
            )
        } else {
            todaySchedules.forEachIndexed { index, item ->
                ModernScheduleCard(
                    time = "${item.startTime} - ${item.endTime}",
                    subject = item.subjectName,
                    teacher = item.teacherName,
                    room = item.room,
                    isLive = index == 0
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
internal fun ModernScheduleCard(
    time: String,
    subject: String,
    teacher: String,
    room: String,
    isLive: Boolean = false
) {
    val isDark = MaterialTheme.colorScheme.surface.isDark()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .sulaoneSharedBounds(key = "schedule_card_${subject}"),
        shape = RoundedCornerShape(16.dp),
        color = if (isLive) (if (isDark) Slate900 else Emerald50.copy(alpha = 0.6f)) else (if (isDark) Slate900 else Color.White),
        border = androidx.compose.foundation.BorderStroke(
            width = 0.5.dp,
            color = if (isLive) Emerald300 else (if (isDark) Slate800 else Slate200)
        ),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isLive) Emerald700 else (if (isDark) Slate800 else Slate100))
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = time,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                    color = if (isLive) Color.White else (if (isDark) Slate200 else Slate700)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = subject,
                        modifier = Modifier.weight(1f, fill = false),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isLive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        LiveStatusChip("Berlangsung", color = Emerald700)
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$teacher • $room",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
