/**
 * FASE 67
 */
package com.sultanagung1.sista.ui.academic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import com.sultanagung1.sista.core.motion.springPressable
import com.sultanagung1.sista.core.motion.sulaoneSharedBounds
import com.sultanagung1.sista.core.util.DateUtils
import com.sultanagung1.sista.data.model.ScheduleItem
import com.sultanagung1.sista.data.model.ScheduleRules
import com.sultanagung1.sista.data.model.ScheduleRules.LessonStatus
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * The student's timetable, straight from `student/schedule`.
 *
 * It used to fall back to four made-up lessons whenever the list was empty
 * (or failed to load), show a hardcoded date and class badge, ignore the
 * selected day, mark the first card as
 * "Berlangsung" at any hour, and "refresh" by re-selecting the day for 600 ms
 * without asking the server anything.
 */
@Composable
fun ScheduleScreen(
    viewModel: AcademicViewModel,
    onNavigateBack: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptics = rememberHapticFeedbackHelper()
    val isDark = isSystemInDarkTheme()

    // The clock drives "Berlangsung"/"Selesai"; a minute is plenty of resolution.
    var now by remember { mutableStateOf(DateUtils.nowCalendar()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            now = DateUtils.nowCalendar()
        }
    }
    val todayName = ScheduleRules.dayName(now.get(Calendar.DAY_OF_WEEK))
    val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

    val days = ScheduleRules.daysToShow(uiState.allSchedules)
    val lessons = ScheduleRules.lessonsFor(uiState.allSchedules, uiState.selectedDay)
    val isToday = uiState.selectedDay == todayName
    val className = ScheduleRules.classNameOf(uiState.allSchedules)

    val borderColor = if (isDark) Slate800 else Slate200
    val backgroundColor = if (isDark) MaterialTheme.colorScheme.background else Slate50
    val textMain = if (isDark) Slate50 else Slate900
    val textSub = if (isDark) Slate400 else Slate500
    val textMuted = if (isDark) Slate500 else Slate400

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Jadwal Pelajaran",
                subtitle = className?.let { "Kelas $it" },
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
        ) {
            ModernBentoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                backgroundColor = if (isDark) MaterialTheme.colorScheme.surface else Slate50,
                borderColor = borderColor,
                elevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isToday) {
                                SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID")).format(now.time)
                            } else {
                                "Jadwal hari ${uiState.selectedDay}"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textMain
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when {
                                uiState.allSchedules.isEmpty() && uiState.isLoading -> "Memuat jadwal…"
                                lessons.isEmpty() -> "Tidak ada pelajaran"
                                isToday -> "${lessons.size} pelajaran hari ini"
                                else -> "${lessons.size} pelajaran"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = textSub
                        )
                    }
                    if (className != null) {
                        SulaoneBadge(
                            text = className,
                            containerColor = Emerald600,
                            contentColor = Slate50
                        )
                    }
                }
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(days) { day ->
                    val isSelected = uiState.selectedDay == day
                    val count = ScheduleRules.lessonsFor(uiState.allSchedules, day).size
                    Box(
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (isSelected) Emerald600 else Color.Transparent)
                            .border(
                                width = 0.5.dp,
                                color = if (isSelected) Emerald600 else borderColor,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .springPressable {
                                haptics.tapLight()
                                viewModel.selectDay(day)
                            }
                            .semantics {
                                contentDescription = buildString {
                                    append(day)
                                    if (day == todayName) append(", hari ini")
                                    append(", $count pelajaran")
                                    if (isSelected) append(", dipilih")
                                }
                            }
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (day == todayName) "HARI INI" else day.take(3).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Slate50 else textSub
                            )
                            Text(
                                text = day,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Slate50 else if (isDark) Slate400 else Slate600
                            )
                        }
                    }
                }
            }

            // The spinner stays until the server has actually answered.
            var refreshRequested by remember { mutableStateOf(false) }
            LaunchedEffect(uiState.isScheduleRefreshing) {
                if (!uiState.isScheduleRefreshing) refreshRequested = false
            }

            SulaonePullToRefreshBox(
                isRefreshing = refreshRequested && uiState.isScheduleRefreshing,
                onRefresh = {
                    refreshRequested = true
                    viewModel.loadSchedule()
                },
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                ) {
                    val error = uiState.scheduleErrorMessage
                    when {
                        uiState.allSchedules.isEmpty() && uiState.isLoading -> items(4) {
                            ShimmerSkeleton(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(84.dp),
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                        uiState.allSchedules.isEmpty() && error != null -> item {
                            SulaoneEmptyState(
                                title = "Jadwal belum bisa dimuat",
                                description = error,
                                icon = Icons.Default.CloudOff,
                                ctaLabel = "Coba Lagi",
                                onCtaClick = viewModel::loadSchedule
                            )
                        }
                        uiState.allSchedules.isEmpty() -> item {
                            SulaoneEmptyState(
                                title = "Belum ada jadwal",
                                description = "Kelasmu belum memiliki jadwal pelajaran di sistem. Hubungi wali kelas atau Tata Usaha.",
                                icon = Icons.Default.EventBusy,
                                ctaLabel = "Muat Ulang",
                                onCtaClick = viewModel::loadSchedule
                            )
                        }
                        lessons.isEmpty() -> item {
                            SulaoneEmptyState(
                                title = "Tidak ada pelajaran",
                                description = "Tidak ada jadwal pada hari ${uiState.selectedDay}.",
                                icon = Icons.Default.EventAvailable
                            )
                        }
                        else -> {
                            if (error != null) {
                                item {
                                    SulaoneErrorBanner(
                                        message = "Menampilkan jadwal tersimpan. $error",
                                        onRetry = viewModel::loadSchedule
                                    )
                                }
                            }
                            itemsIndexed(lessons, key = { _, lesson -> lesson.id }) { index, item ->
                                ScheduleLessonCard(
                                    order = index + 1,
                                    item = item,
                                    status = ScheduleRules.statusOf(item, isToday, nowMinutes),
                                    isDark = isDark,
                                    borderColor = borderColor,
                                    textMain = textMain,
                                    textSub = textSub,
                                    textMuted = textMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleLessonCard(
    order: Int,
    item: ScheduleItem,
    status: LessonStatus,
    isDark: Boolean,
    borderColor: Color,
    textMain: Color,
    textSub: Color,
    textMuted: Color
) {
    val ongoing = status == LessonStatus.ONGOING
    val done = status == LessonStatus.DONE
    val cardBg = when {
        ongoing -> if (isDark) Emerald900.copy(alpha = 0.4f) else Emerald50
        else -> if (isDark) MaterialTheme.colorScheme.surface else Slate50
    }
    val start = ScheduleRules.displayTime(item.startTime)
    val end = ScheduleRules.displayTime(item.endTime)

    ModernBentoCard(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (done) 0.6f else 1f)
            .sulaoneSharedBounds(key = "schedule_card_${item.id}")
            .semantics(mergeDescendants = true) {
                contentDescription = buildString {
                    append("Jam ke-$order, ${item.subjectName}, $start sampai $end, ${item.teacherName}, ${ScheduleRules.locationOf(item)}")
                    if (ongoing) append(", sedang berlangsung")
                    if (done) append(", selesai")
                }
            },
        shape = RoundedCornerShape(20.dp),
        backgroundColor = cardBg,
        borderColor = if (ongoing) Emerald600 else borderColor,
        elevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = order.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (ongoing) Emerald600 else textMuted,
                modifier = Modifier.padding(end = 12.dp)
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isDark) Slate800 else Slate200)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = start,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = textMain
                    )
                    Text(
                        text = end,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = textSub
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.subjectName,
                        modifier = Modifier.weight(1f, fill = false),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = textMain,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (ongoing) {
                        Spacer(modifier = Modifier.width(8.dp))
                        LiveStatusChip("Berlangsung", color = Emerald600)
                    } else if (done) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Selesai", style = MaterialTheme.typography.labelSmall, color = textMuted)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = textSub,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.teacherName,
                        style = MaterialTheme.typography.bodySmall,
                        color = textSub,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Room,
                        contentDescription = null,
                        tint = textMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = ScheduleRules.locationOf(item),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
