package com.sultanagung1.sista.ui.teacher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Class
import androidx.compose.material.icons.outlined.CoPresent
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Grade
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Source
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.designsystem.LifecycleStartStopEffect
import com.sultanagung1.sista.core.designsystem.SulaonePullToRefreshBox
import com.sultanagung1.sista.core.ui.component.ButtonVariant
import com.sultanagung1.sista.core.ui.component.EmptyState
import com.sultanagung1.sista.core.ui.component.ErrorState
import com.sultanagung1.sista.core.ui.component.FeatureTile
import com.sultanagung1.sista.core.ui.component.GreetingHeader
import com.sultanagung1.sista.core.ui.component.InlineBanner
import com.sultanagung1.sista.core.ui.component.SectionHeader
import com.sultanagung1.sista.core.ui.component.SistaButton
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SkeletonList
import com.sultanagung1.sista.core.ui.component.StatTile
import com.sultanagung1.sista.core.ui.component.StatusPill
import com.sultanagung1.sista.core.ui.component.greetingFor
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.core.util.DateUtils
import com.sultanagung1.sista.data.model.TeacherScheduleSlot
import com.sultanagung1.sista.data.model.TeachingJournalEntry
import com.sultanagung1.sista.ui.navigation.LocalCapabilityState
import com.sultanagung1.sista.ui.navigation.Screen
import com.sultanagung1.sista.ui.navigation.canOpen
import com.sultanagung1.sista.ui.teacher.sessions.TeacherClassSessionCard
import java.util.Calendar
import java.util.Locale

/** A teacher shortcut. Shown only when the account may open [route]. */
@Immutable
data class TeacherShortcut(val title: String, val icon: ImageVector, val route: String)

val TEACHER_SHORTCUTS = listOf(
    TeacherShortcut("Sesi kelas", Icons.Outlined.CoPresent, Screen.TeacherTodaySessions.route),
    TeacherShortcut("Jurnal mengajar", Icons.Outlined.EditNote, Screen.TeachingJournalMobile.route),
    TeacherShortcut("Penilaian harian", Icons.Outlined.Grade, Screen.DailyAssessmentList.route),
    TeacherShortcut("Ujian & pengawasan", Icons.Outlined.Quiz, Screen.TeacherProctorExams.route),
    TeacherShortcut("Bank soal", Icons.Outlined.Source, Screen.QuestionBank.route),
    TeacherShortcut("Kelas online", Icons.Outlined.Class, Screen.ElearningClassList.route),
    TeacherShortcut("Pesan orang tua", Icons.AutoMirrored.Outlined.Chat, Screen.ConversationList.route),
    TeacherShortcut("Koreksi tahsin", Icons.Outlined.RecordVoiceOver, Screen.TahsinTeacherReview.route),
)

/**
 * The teacher's Beranda — the same layout as every role's home: who is
 * signed in, what to do now (the class session), today's teaching slots,
 * shortcuts this account may use, and the latest journals. All from
 * `teacher/classes`, `teacher/schedule`, `teacher/journals` and
 * `teacher/class-sessions/today`.
 */
@Composable
fun TeacherDashboardScreen(
    viewModel: TeacherViewModel,
    onNavigateToAttendance: (classroomId: Long, scheduleId: Long, className: String) -> Unit,
    onNavigateToJournal: () -> Unit,
    onNavigateRoute: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val capabilities = LocalCapabilityState.current
    // FASE 77.7.2: session status changes during the day; refresh the card on return.
    LifecycleStartStopEffect(onStart = viewModel::loadClassSessions, onStop = {})

    // Pull-to-refresh follows the real reload and stops when the server answers.
    var refreshRequested by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.isLoading) { if (!uiState.isLoading) refreshRequested = false }

    val hour = remember(uiState.isLoading) { DateUtils.nowCalendar().get(Calendar.HOUR_OF_DAY) }
    TeacherHomeContent(
        greeting = greetingFor(hour),
        state = uiState,
        shortcuts = TEACHER_SHORTCUTS.filter { capabilities.canOpen(it.route) },
        refreshing = refreshRequested && uiState.isLoading,
        onRefresh = {
            refreshRequested = true
            viewModel.loadDashboard()
        },
        onOpenRoute = onNavigateRoute,
        onAttendance = { slot -> onNavigateToAttendance(slot.classroomId, slot.id, slot.classroomName ?: "Tanpa kelas") },
        onJournal = onNavigateToJournal,
    )
}

/** The teacher home without a ViewModel, for previews and screenshots. */
@Composable
fun TeacherHomeContent(
    greeting: String,
    state: TeacherUiState,
    shortcuts: List<TeacherShortcut>,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    onOpenRoute: (String) -> Unit,
    onAttendance: (TeacherScheduleSlot) -> Unit,
    onJournal: () -> Unit,
) {
    val firstLoad = state.isLoading && state.todaySchedules.isEmpty() && state.recentJournals.isEmpty()
    // A failed load has no numbers to show; "0 kelas" would be a claim, not a fact.
    val statsUnknown = firstLoad ||
        (state.errorMessage != null && state.totalClasses == 0 && state.teachingHoursThisWeek == 0.0)
    ShellTheme {
        Surface(color = SistaTheme.colors.background) {
            SulaonePullToRefreshBox(
                isRefreshing = refreshing,
                onRefresh = onRefresh,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("teacher_home_root"),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = Spacing.xxl),
                    verticalArrangement = Arrangement.spacedBy(Spacing.lg),
                ) {
                    item(key = "header") {
                        GreetingHeader(
                            greeting = greeting,
                            name = state.teacherName.ifBlank { "Guru" },
                            details = listOfNotNull(state.nip.takeIf { it.isNotBlank() }?.let { "NIP $it" }),
                            unreadCount = 0,
                            onOpenNotifications = { onOpenRoute(Screen.NotificationCenter.route) },
                        )
                    }
                    if (state.classSessionsAvailable) {
                        item(key = "class_session_card") {
                            TeacherClassSessionCard(
                                sessions = state.classSessions,
                                nowMinutes = state.nowMinutes,
                                onOpenActive = { id -> onOpenRoute(Screen.TeacherActiveSession.createRoute(id)) },
                                onOpenSessions = { onOpenRoute(Screen.TeacherTodaySessions.route) },
                                modifier = Modifier.padding(horizontal = Spacing.screen),
                            )
                        }
                    }
                    if (state.errorMessage != null && !firstLoad && state.todaySchedules.isNotEmpty()) {
                        item(key = "stale") {
                            InlineBanner(
                                message = "Menampilkan data tersimpan. ${state.errorMessage}",
                                tone = StatusTone.Warning,
                                actionLabel = "Muat ulang",
                                onAction = onRefresh,
                                modifier = Modifier.padding(horizontal = Spacing.screen),
                            )
                        }
                    }
                    item(key = "stats") {
                        Row(
                            Modifier.padding(horizontal = Spacing.screen).height(IntrinsicSize.Max),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                        ) {
                            StatTile(
                                label = "Kelas diampu",
                                value = if (statsUnknown) "–" else state.totalClasses.toString(),
                                supporting = "dari jadwal mengajar",
                                icon = Icons.Outlined.Groups,
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                            )
                            StatTile(
                                label = "Jam mengajar",
                                value = if (statsUnknown) "–" else formatHours(state.teachingHoursThisWeek),
                                supporting = "minggu ini, dari jadwal",
                                icon = Icons.Outlined.Schedule,
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                            )
                        }
                    }
                    item(key = "today", contentType = "schedule") {
                        TodayTeaching(state, firstLoad, state.nowMinutes, onRefresh, onAttendance, onJournal) {
                            onOpenRoute(Screen.TeachingJournalMobile.route)
                        }
                    }
                    if (shortcuts.isNotEmpty()) {
                        item(key = "shortcuts") { Shortcuts(shortcuts, onOpenRoute) }
                    }
                    if (state.recentJournals.isNotEmpty()) {
                        item(key = "journals", contentType = "journal") { RecentJournals(state.recentJournals.take(3), onJournal) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayTeaching(
    state: TeacherUiState,
    firstLoad: Boolean,
    nowMinutes: Int,
    onRetry: () -> Unit,
    onAttendance: (TeacherScheduleSlot) -> Unit,
    onJournal: () -> Unit,
    onOpenJournals: () -> Unit,
) {
    Column(Modifier.padding(horizontal = Spacing.screen)) {
        SectionHeader("Jadwal mengajar hari ini", actionLabel = "Jurnal", onAction = onOpenJournals)
        when {
            firstLoad -> SkeletonList(rows = 3)
            state.todaySchedules.isEmpty() && state.errorMessage != null -> ErrorState(
                title = "Jadwal belum bisa dimuat",
                body = state.errorMessage,
                onRetry = onRetry,
            )
            state.todaySchedules.isEmpty() -> EmptyState(
                title = "Tidak ada jadwal mengajar hari ini",
                body = "Jadwal minggu ini tetap tersedia di Jurnal mengajar.",
                icon = Icons.Outlined.EventAvailable,
            )
            else -> SistaCard(modifier = Modifier.fillMaxWidth()) {
                state.todaySchedules.forEachIndexed { index, slot ->
                    if (index > 0) HorizontalDivider(color = SistaTheme.colors.outlineVariant)
                    ScheduleRow(slot, isNow = isNow(slot, nowMinutes), onAttendance = { onAttendance(slot) }, onJournal = onJournal)
                }
            }
        }
    }
}

@Composable
private fun ScheduleRow(slot: TeacherScheduleSlot, isNow: Boolean, onAttendance: () -> Unit, onJournal: () -> Unit) {
    Column(Modifier.padding(vertical = Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.width(52.dp)) {
                Text(clock(slot.sessionStart), style = SistaTheme.typography.titleSmall)
                Text(clock(slot.sessionEnd), style = SistaTheme.typography.bodySmall, color = SistaTheme.colors.onSurfaceVariant)
            }
            Column(Modifier.weight(1f)) {
                Text(slot.subjectName ?: "Mata pelajaran", style = SistaTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    listOfNotNull(slot.classroomName?.let { "Kelas $it" }, slot.roomName).joinToString(" · "),
                    style = SistaTheme.typography.bodySmall,
                    color = SistaTheme.colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (isNow) StatusPill("Berlangsung", StatusTone.Success)
        }
        Row(Modifier.padding(start = 52.dp), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            SistaButton("Presensi", onAttendance, variant = ButtonVariant.Secondary, leadingIcon = Icons.Outlined.CheckCircle)
            SistaButton("Jurnal", onJournal, variant = ButtonVariant.Outlined, leadingIcon = Icons.Outlined.EditNote)
        }
    }
}

@Composable
private fun Shortcuts(shortcuts: List<TeacherShortcut>, onOpenRoute: (String) -> Unit) {
    Column(Modifier.padding(horizontal = Spacing.screen), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        SectionHeader("Akses cepat", actionLabel = "Semua layanan", onAction = { onOpenRoute(Screen.ServicesHub.route) })
        shortcuts.take(6).chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                row.forEach { item ->
                    FeatureTile(item.title, item.icon, onClick = { onOpenRoute(item.route) }, modifier = Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun RecentJournals(journals: List<TeachingJournalEntry>, onOpen: () -> Unit) {
    Column(Modifier.padding(horizontal = Spacing.screen)) {
        SectionHeader("Jurnal terakhir", actionLabel = "Semua", onAction = onOpen)
        SistaCard(modifier = Modifier.fillMaxWidth(), onClick = onOpen) {
            journals.forEachIndexed { index, journal ->
                if (index > 0) HorizontalDivider(color = SistaTheme.colors.outlineVariant)
                Row(Modifier.padding(vertical = Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            listOfNotNull(journal.subjectName, journal.classroomName).joinToString(" · "),
                            style = SistaTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            listOfNotNull(shortDate(journal.teachingDate), journal.topic?.takeIf { it.isNotBlank() }).joinToString(" · "),
                            style = SistaTheme.typography.bodySmall,
                            color = SistaTheme.colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            "Hadir ${journal.studentsPresent} · Absen ${journal.studentsAbsent}",
                            style = SistaTheme.typography.bodySmall,
                            color = SistaTheme.colors.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.width(Spacing.sm))
                    journalStatus(journal.status)?.let { (label, tone) -> StatusPill(label, tone) }
                }
            }
        }
    }
}

private fun journalStatus(status: String?): Pair<String, StatusTone>? = when (status?.lowercase()) {
    "draft" -> "Draf" to StatusTone.Warning
    "submitted" -> "Terkirim" to StatusTone.Info
    "approved", "verified" -> "Disetujui" to StatusTone.Success
    "rejected" -> "Ditolak" to StatusTone.Danger
    null, "" -> null
    else -> status.replaceFirstChar { it.uppercase() } to StatusTone.Neutral
}

private val MONTHS = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des")

/** "2026-09-25" → "25 Sep"; anything else is shown as it came. */
private fun shortDate(iso: String?): String? {
    val parts = iso?.take(10)?.split("-") ?: return null
    val month = parts.getOrNull(1)?.toIntOrNull()?.let { MONTHS.getOrNull(it - 1) }
    val day = parts.getOrNull(2)?.toIntOrNull()
    return if (month != null && day != null) "$day $month" else iso
}

private fun clock(time: String?): String =
    DateUtils.parseMinutesOfDay(time)?.let { "%02d:%02d".format(it / 60, it % 60) } ?: "—"

private fun isNow(slot: TeacherScheduleSlot, nowMinutes: Int): Boolean {
    val start = DateUtils.parseMinutesOfDay(slot.sessionStart) ?: return false
    val end = DateUtils.parseMinutesOfDay(slot.sessionEnd) ?: return false
    return nowMinutes in start until end
}

/** 12.5 → "12,5 jam"; 12.0 → "12 jam". */
private fun formatHours(hours: Double): String {
    val rounded = Math.round(hours * 10) / 10.0
    val text = if (rounded % 1.0 == 0.0) rounded.toLong().toString() else String.format(Locale.forLanguageTag("id-ID"), "%.1f", rounded)
    return "$text jam"
}
