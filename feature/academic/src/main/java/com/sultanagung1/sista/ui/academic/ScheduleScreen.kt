package com.sultanagung1.sista.ui.academic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.designsystem.SulaonePullToRefreshBox
import com.sultanagung1.sista.core.motion.sulaoneSharedBounds
import com.sultanagung1.sista.core.ui.component.CardVariant
import com.sultanagung1.sista.core.ui.component.EmptyState
import com.sultanagung1.sista.core.ui.component.ErrorState
import com.sultanagung1.sista.core.ui.component.FilterChipRow
import com.sultanagung1.sista.core.ui.component.InlineBanner
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SistaTopBar
import com.sultanagung1.sista.core.ui.component.SkeletonList
import com.sultanagung1.sista.core.ui.component.StatusPill
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.core.util.DateUtils
import com.sultanagung1.sista.data.model.ScheduleItem
import com.sultanagung1.sista.data.model.ScheduleRules
import com.sultanagung1.sista.data.model.ScheduleRules.LessonStatus
import kotlinx.coroutines.delay
import java.util.Calendar

/**
 * The student's timetable from `student/schedule`: one day at a time, in
 * time order, with the lesson going on now (or the next one) marked from
 * the clock. Pull down to reload from the server.
 */
@Composable
fun ScheduleScreen(
    viewModel: AcademicViewModel,
    onNavigateBack: (() -> Unit)? = null,
) {
    val uiState by viewModel.uiState.collectAsState()

    // The clock drives "Berlangsung"/"Berikutnya"; a minute is plenty of resolution.
    var now by remember { mutableStateOf(DateUtils.nowCalendar()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            now = DateUtils.nowCalendar()
        }
    }

    // The spinner stays until the server has actually answered.
    var refreshRequested by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.isScheduleRefreshing) {
        if (!uiState.isScheduleRefreshing) refreshRequested = false
    }

    ScheduleContent(
        schedules = uiState.allSchedules,
        selectedDay = uiState.selectedDay,
        todayName = ScheduleRules.dayName(now.get(Calendar.DAY_OF_WEEK)),
        nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE),
        isLoading = uiState.isLoading,
        errorMessage = uiState.scheduleErrorMessage,
        refreshing = refreshRequested && uiState.isScheduleRefreshing,
        onSelectDay = viewModel::selectDay,
        onRefresh = {
            refreshRequested = true
            viewModel.loadSchedule()
        },
        onNavigateBack = onNavigateBack,
    )
}

/** The timetable without state of its own, so it can be previewed and screenshot-tested. */
@Composable
fun ScheduleContent(
    schedules: List<ScheduleItem>,
    selectedDay: String,
    todayName: String,
    nowMinutes: Int,
    isLoading: Boolean,
    errorMessage: String?,
    /** True only while a pull-to-refresh the user started waits for the server. */
    refreshing: Boolean,
    onSelectDay: (String) -> Unit,
    onRefresh: () -> Unit,
    onNavigateBack: (() -> Unit)?,
) {
    val days = ScheduleRules.daysToShow(schedules)
    val lessons = ScheduleRules.lessonsFor(schedules, selectedDay)
    val isToday = selectedDay == todayName
    val focus = ScheduleRules.focusOf(lessons, isToday, nowMinutes)
    val className = ScheduleRules.classNameOf(schedules)

    ShellTheme {
        Scaffold(
            topBar = {
                SistaTopBar(
                    title = "Jadwal Pelajaran",
                    subtitle = className?.let { "Kelas $it" },
                    onBack = onNavigateBack,
                )
            },
            containerColor = SistaTheme.colors.background,
        ) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                FilterChipRow(
                    options = days,
                    selected = selectedDay,
                    onSelect = onSelectDay,
                    label = { day -> if (day == todayName) "${ScheduleRules.shortDay(day)} · Hari ini" else ScheduleRules.shortDay(day) },
                    modifier = Modifier
                        .padding(vertical = Spacing.sm)
                        .testTag("schedule_day_chips"),
                )

                SulaonePullToRefreshBox(
                    isRefreshing = refreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("schedule_list"),
                        contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, bottom = Spacing.xxl),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    ) {
                        when {
                            schedules.isEmpty() && isLoading -> item { SkeletonList(rows = 4) }
                            schedules.isEmpty() && errorMessage != null -> item {
                                ErrorState(
                                    title = "Jadwal belum bisa dimuat",
                                    body = errorMessage,
                                    onRetry = onRefresh,
                                )
                            }
                            schedules.isEmpty() -> item {
                                EmptyState(
                                    title = "Jadwal belum tersedia",
                                    body = "Sekolah belum memasukkan jadwal untuk kelas Anda.",
                                    icon = Icons.Outlined.EventNote,
                                )
                            }
                            else -> {
                                if (errorMessage != null) {
                                    item {
                                        InlineBanner(
                                            message = "Menampilkan jadwal tersimpan. $errorMessage",
                                            tone = StatusTone.Warning,
                                            actionLabel = "Muat ulang",
                                            onAction = onRefresh,
                                        )
                                    }
                                }
                                item {
                                    Text(
                                        if (lessons.isEmpty()) selectedDay else "$selectedDay · ${lessons.size} pelajaran",
                                        style = SistaTheme.typography.titleMedium,
                                        color = SistaTheme.colors.onSurface,
                                        modifier = Modifier.padding(top = Spacing.xs),
                                    )
                                }
                                if (lessons.isEmpty()) {
                                    item {
                                        EmptyState(
                                            title = "Tidak ada pelajaran",
                                            body = "Tidak ada jadwal pada hari $selectedDay.",
                                            icon = Icons.Outlined.EventBusy,
                                        )
                                    }
                                } else {
                                    items(lessons, key = { it.id }) { lesson ->
                                        val status = ScheduleRules.statusOf(lesson, isToday, nowMinutes)
                                        LessonCard(
                                            lesson = lesson,
                                            status = status,
                                            isNext = focus?.first?.id == lesson.id && focus.second == LessonStatus.UPCOMING,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonCard(lesson: ScheduleItem, status: LessonStatus, isNext: Boolean) {
    val ongoing = status == LessonStatus.ONGOING
    val done = status == LessonStatus.DONE
    val start = ScheduleRules.displayTime(lesson.startTime)
    val end = ScheduleRules.displayTime(lesson.endTime)
    SistaCard(
        variant = if (ongoing) CardVariant.Highlighted else CardVariant.Filled,
        modifier = Modifier
            .fillMaxWidth()
            // Continues the card the Home preview shows for the same lesson.
            .sulaoneSharedBounds(key = "schedule_card_${lesson.subjectName}")
            .semantics(mergeDescendants = true) {
                contentDescription = buildString {
                    append("${lesson.subjectName}, $start sampai $end")
                    if (ongoing) append(", sedang berlangsung")
                    if (isNext) append(", berikutnya")
                    if (done) append(", selesai")
                }
            },
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.width(56.dp)) {
                Text(start, style = SistaTheme.typography.titleSmall)
                Text(
                    end,
                    style = SistaTheme.typography.bodySmall,
                    color = if (ongoing) SistaTheme.colors.onPrimaryContainer else SistaTheme.colors.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(Spacing.md))
            Column(Modifier.weight(1f)) {
                Text(
                    lesson.subjectName,
                    style = SistaTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = if (done) SistaTheme.colors.onSurfaceVariant else Color.Unspecified,
                )
                Spacer(Modifier.height(Spacing.xxs))
                Text(
                    listOf(lesson.teacherName, ScheduleRules.locationOf(lesson)).filter { it.isNotBlank() && it != "—" }.joinToString(" · "),
                    style = SistaTheme.typography.bodyMedium,
                    color = if (ongoing) SistaTheme.colors.onPrimaryContainer else SistaTheme.colors.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            when {
                ongoing -> StatusPill("Berlangsung", StatusTone.Success)
                isNext -> StatusPill("Berikutnya", StatusTone.Info)
                done -> StatusPill("Selesai", StatusTone.Neutral)
            }
        }
    }
}
