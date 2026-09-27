package com.sultanagung1.sista.ui.teacher

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CoPresent
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.accessibility.sulaoneInteractiveTouchTarget
import com.sultanagung1.sista.core.designsystem.AccentBlue
import com.sultanagung1.sista.core.designsystem.AccentPurple
import com.sultanagung1.sista.core.designsystem.AccentRose
import com.sultanagung1.sista.core.designsystem.BentoHeroSplit
import com.sultanagung1.sista.core.designsystem.BentoHeroSplitSkeleton
import com.sultanagung1.sista.core.designsystem.MetricCardSkeleton
import com.sultanagung1.sista.core.designsystem.SessionCardListSkeleton
import com.sultanagung1.sista.core.designsystem.SulaoneTieredLoading
import com.sultanagung1.sista.core.designsystem.Emerald200
import com.sultanagung1.sista.core.designsystem.Emerald50
import com.sultanagung1.sista.core.designsystem.Emerald500
import com.sultanagung1.sista.core.designsystem.Emerald600
import com.sultanagung1.sista.core.designsystem.Emerald700
import com.sultanagung1.sista.core.designsystem.Emerald800
import com.sultanagung1.sista.core.designsystem.Gold50
import com.sultanagung1.sista.core.designsystem.Gold700
import com.sultanagung1.sista.core.designsystem.Gold800
import com.sultanagung1.sista.core.designsystem.Slate100
import com.sultanagung1.sista.core.designsystem.Slate200
import com.sultanagung1.sista.core.designsystem.Slate400
import com.sultanagung1.sista.core.designsystem.Slate600
import com.sultanagung1.sista.core.designsystem.Slate700
import com.sultanagung1.sista.core.designsystem.SulaoneBentoHeroTile
import com.sultanagung1.sista.core.designsystem.SulaoneEmptyState
import com.sultanagung1.sista.core.designsystem.SulaoneErrorBanner
import com.sultanagung1.sista.core.designsystem.SulaoneGlassTopBar
import com.sultanagung1.sista.core.designsystem.SulaoneMetricCard
import com.sultanagung1.sista.core.designsystem.rememberIsItemScrolledOff
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import com.sultanagung1.sista.core.motion.springPressable
import com.sultanagung1.sista.data.model.TeacherScheduleSlot
import com.sultanagung1.sista.data.model.TeachingJournalEntry
import com.sultanagung1.sista.ui.common.HeaderMetadataChip
import com.sultanagung1.sista.ui.common.SulaoneExecutiveHeader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun TeacherDashboardScreen(
    viewModel: TeacherViewModel,
    onNavigateToAttendance: (classroomId: Long, scheduleId: Long, className: String) -> Unit,
    onNavigateToJournal: () -> Unit,
    onNavigateRoute: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val headerScrolledOff by rememberIsItemScrolledOff(listState, HEADER_ITEM_KEY)
    // FASE 77.7.2: session status changes during the day; refresh the card on return.
    com.sultanagung1.sista.core.designsystem.LifecycleStartStopEffect(
        onStart = viewModel::loadClassSessions,
        onStop = {}
    )
    // Nothing loaded yet (a retry with data already on screen keeps the data).
    val isFirstLoad = uiState.isLoading && uiState.todaySchedules.isEmpty() && uiState.recentJournals.isEmpty()

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Executive Top App Bar (Unified Professional Design)
                item(key = HEADER_ITEM_KEY) {
                    SulaoneExecutiveHeader(
                        userName = uiState.teacherName.ifBlank { "Guru" },
                        titlePrefix = "Assalamu'alaikum,",
                        chips = listOf(
                            HeaderMetadataChip(
                                text = "Pendidik / Guru",
                                icon = Icons.Default.CoPresent,
                                containerColor = Emerald50,
                                borderColor = Emerald200,
                                textColor = Emerald800,
                                iconColor = Emerald700
                            ),
                            HeaderMetadataChip(
                                text = "Guru Aktif",
                                isLiveDot = true,
                                dotColor = Emerald500,
                                containerColor = Emerald50,
                                borderColor = Emerald200,
                                textColor = Emerald800
                            ),
                            HeaderMetadataChip(
                                text = if (uiState.nip.isNotBlank()) "NIP: ${uiState.nip}" else "NIP belum diatur",
                                containerColor = Slate100,
                                borderColor = Slate200,
                                textColor = Slate700
                            )
                        ),
                        onAvatarClick = { onNavigateRoute("profile") },
                        onQrClick = { onNavigateRoute("scanner") },
                        onNotificationClick = { onNavigateRoute("notifications") }
                    )
                }

                if (uiState.errorMessage != null) {
                    item {
                        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                            SulaoneErrorBanner(
                                message = uiState.errorMessage ?: "Gagal memuat data dashboard guru.",
                                onRetry = { viewModel.loadDashboard() }
                            )
                        }
                    }
                }

                // 1.5 FASE 77.7.2: "Kembali ke Kelas" / "Mulai Kelas" for today's class sessions.
                if (uiState.classSessionsAvailable) {
                    item(key = "class_session_card") {
                        com.sultanagung1.sista.ui.teacher.sessions.TeacherClassSessionCard(
                            sessions = uiState.classSessions,
                            nowMinutes = uiState.nowMinutes,
                            onOpenActive = { id ->
                                onNavigateRoute(com.sultanagung1.sista.ui.navigation.Screen.TeacherActiveSession.createRoute(id))
                            },
                            onOpenSessions = {
                                onNavigateRoute(com.sultanagung1.sista.ui.navigation.Screen.TeacherTodaySessions.route)
                            },
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }

                // 2. FASE 76.2 Bento: today's sessions is the hero (the most
                // time-sensitive thing a teacher opens this screen for); weekly
                // load and class count sit beside it; journals span the width.
                // Previously a uniform 2x2 grid labelled "Bento" in a comment.
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // FASE 76.5: on the first load the hero used to read "0 Sesi"
                        // (an empty list, not a fact) until data arrived.
                        if (isFirstLoad) {
                            SulaoneTieredLoading(isLoading = true) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    BentoHeroSplitSkeleton()
                                    MetricCardSkeleton()
                                }
                            }
                        } else {
                            BentoHeroSplit(
                                hero = { heroModifier ->
                                    SulaoneBentoHeroTile(
                                        modifier = heroModifier,
                                        label = "Jadwal Hari Ini",
                                        value = uiState.todaySchedules.size.toString(),
                                        unit = "Sesi",
                                        caption = rememberTeacherHeroCaption(uiState.todaySchedules),
                                        icon = Icons.Default.CalendarToday,
                                        accent = Gold700
                                    )
                                },
                                top = { tileModifier ->
                                    SulaoneMetricCard(
                                        modifier = tileModifier,
                                        title = "Beban Mengajar",
                                        value = "${uiState.teachingHoursThisWeek.let { if (it % 1.0 == 0.0) it.toInt().toString() else String.format(
                                            Locale.US, "%.1f", it) }} Jam",
                                        subtitle = "Total Jadwal Mingguan",
                                        icon = Icons.Default.AccessTimeFilled,
                                        iconTint = Emerald700,
                                        iconBackground = Emerald50
                                    )
                                },
                                bottom = { tileModifier ->
                                    SulaoneMetricCard(
                                        modifier = tileModifier,
                                        title = "Kelas Diampu",
                                        value = "${uiState.totalClasses} Rombel",
                                        subtitle = "Rombongan Belajar",
                                        icon = Icons.Default.Groups,
                                        iconTint = AccentBlue,
                                        iconBackground = AccentBlue.copy(alpha = 0.12f)
                                    )
                                }
                            )

                            SulaoneMetricCard(
                                modifier = Modifier.fillMaxWidth(),
                                title = "Jurnal Terbaru",
                                value = "${uiState.recentJournals.size} Jurnal",
                                subtitle = "Tersimpan di Server",
                                badgeText = "Terkini",
                                badgeColor = AccentPurple,
                                badgeBackground = AccentPurple.copy(alpha = 0.12f),
                                icon = Icons.AutoMirrored.Filled.FactCheck,
                                iconTint = AccentPurple,
                                iconBackground = AccentPurple.copy(alpha = 0.12f)
                            )
                        }
                    }
                }

                // 3. Quick Action Grid for Teachers (Clean Bento)
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Text(
                            text = "Aksi Cepat Pendidik",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.2).sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TeacherQuickActionCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.AutoMirrored.Filled.FactCheck,
                                title = "Presensi Kelas",
                                subtitle = "Checklist H/I/S/A",
                                containerColor = Emerald50,
                                iconTint = Emerald700,
                                onClick = {
                                    val active = uiState.todaySchedules.firstOrNull()
                                    if (active != null) {
                                        onNavigateToAttendance(active.classroomId, active.id, active.classroomName ?: "Tanpa Kelas")
                                    }
                                }
                            )
                            TeacherQuickActionCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.EditNote,
                                title = "Jurnal KBM",
                                subtitle = "Catat materi KBM",
                                containerColor = Gold50,
                                iconTint = Gold700,
                                onClick = onNavigateToJournal
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TeacherQuickActionCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Security,
                                title = "Pengawas CBT",
                                subtitle = "Monitoring anti-cheat",
                                containerColor = AccentRose.copy(alpha = 0.1f),
                                iconTint = AccentRose,
                                onClick = {
                                    // Opens the teacher's real exam list (GET teacher/cbt/exams), never a
                                    // hardcoded exam id — the list jumps straight to the proctor
                                    // screen when exactly one of their exams is ongoing.
                                    onNavigateRoute(com.sultanagung1.sista.ui.navigation.Screen.TeacherProctorExams.route)
                                }
                            )
                            TeacherQuickActionCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.PostAdd,
                                title = "Bank Soal",
                                subtitle = "Kelola ujian daring",
                                containerColor = AccentBlue.copy(alpha = 0.1f),
                                iconTint = AccentBlue,
                                onClick = {
                                    onNavigateRoute(com.sultanagung1.sista.ui.navigation.Screen.TeacherCreateExam.route)
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        TeacherQuickActionCard(
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.AutoMirrored.Filled.Chat,
                            title = "Pesan & Konsultasi Ortu",
                            subtitle = "Balas pesan wali murid",
                            containerColor = AccentPurple.copy(alpha = 0.1f),
                            iconTint = AccentPurple,
                            onClick = {
                                onNavigateRoute(com.sultanagung1.sista.ui.navigation.Screen.ConversationList.route)
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        TeacherQuickActionCard(
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Default.MenuBook,
                            title = "Evaluasi Setoran Tahsin",
                            subtitle = "Simak rekaman & beri catatan tajwid",
                            containerColor = Emerald50,
                            iconTint = Emerald700,
                            onClick = {
                                onNavigateRoute(com.sultanagung1.sista.ui.navigation.Screen.TahsinTeacherReview.route)
                            }
                        )
                    }
                }

                // 4. Today's Teaching Schedule (Interactive Timeline Cards)
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Jadwal Mengajar Hari Ini",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.2).sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Emerald50)
                                    .padding(horizontal = 9.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "${uiState.todaySchedules.size} Sesi KBM",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Emerald800
                                )
                            }
                        }
                    }
                }

                if (uiState.isLoading && uiState.todaySchedules.isEmpty()) {
                    item {
                        SulaoneTieredLoading(isLoading = true, modifier = Modifier.padding(horizontal = 20.dp)) {
                            SessionCardListSkeleton()
                        }
                    }
                } else if (!uiState.isLoading && uiState.todaySchedules.isEmpty()) {
                    item {
                        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                            SulaoneEmptyState(
                                icon = Icons.Default.CheckCircle,
                                title = "Tidak Ada Jadwal Hari Ini",
                                description = "Alhamdulillah, tidak ada jadwal tatap muka mengajar untuk hari ini."
                            )
                        }
                    }
                } else {
                    items(
                        items = uiState.todaySchedules,
                        key = { it.id },
                        contentType = { "schedule" }
                    ) { schedule ->
                        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                            TeacherScheduleCard(
                                schedule = schedule,
                                onAttendanceClick = { onNavigateToAttendance(schedule.classroomId, schedule.id, schedule.classroomName ?: "Tanpa Kelas") },
                                onJournalClick = onNavigateToJournal
                            )
                        }
                    }
                }

                // 5. Recent Teaching Journals
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Riwayat Jurnal KBM Terakhir",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.2).sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                if (!uiState.isLoading && uiState.recentJournals.isEmpty()) {
                    item {
                        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                            SulaoneEmptyState(
                                icon = Icons.AutoMirrored.Filled.MenuBook,
                                title = "Belum Ada Jurnal Tercatat",
                                description = "Jurnal KBM yang Anda catat akan muncul di sini."
                            )
                        }
                    }
                }

                items(
                    items = uiState.recentJournals,
                    key = { it.uuid },
                    contentType = { "journal" }
                ) { journal ->
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        TeachingJournalCard(journal = journal)
                    }
                }

                // Space at bottom for navigation bar
                item {
                    Spacer(modifier = Modifier.height(88.dp))
                }
            }

            // FASE 76.2: sticky glass bar once the executive header scrolls away.
            SulaoneGlassTopBar(
                visible = headerScrolledOff,
                title = uiState.teacherName.ifBlank { "Guru" },
                subtitle = "Dashboard Guru",
                onClick = { coroutineScope.launch { listState.animateScrollToItem(0) } }
            )
        }
    }
}

private const val HEADER_ITEM_KEY = "executive_header"

/**
 * Hero caption for "Jadwal Hari Ini", derived only from the real schedule
 * list and server-corrected time (same source isScheduleActiveNow uses) —
 * re-evaluated every 30s like TeacherScheduleCard so "sedang berlangsung"
 * doesn't go stale while the dashboard stays open.
 */
@Composable
private fun rememberTeacherHeroCaption(schedules: List<TeacherScheduleSlot>): String {
    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000L)
            tick++
        }
    }
    return remember(schedules, tick) {
        if (schedules.isEmpty()) return@remember "Tidak ada sesi tatap muka hari ini"
        val now = com.sultanagung1.sista.core.util.DateUtils.nowMinutesOfDay()
        val active = schedules.firstOrNull { isScheduleActiveNow(it, now) }
        if (active != null) {
            return@remember "Sedang berlangsung: ${active.subjectName ?: "Mapel"} • ${active.classroomName ?: "-"}"
        }
        val next = schedules
            .mapNotNull { slot ->
                com.sultanagung1.sista.core.util.DateUtils.parseMinutesOfDay(slot.sessionStart)?.let { start -> start to slot }
            }
            .filter { (start, _) -> start > now }
            .minByOrNull { (start, _) -> start }
            ?.second
        if (next != null) {
            "Berikutnya ${next.sessionStart} • ${next.subjectName ?: "Mapel"}"
        } else {
            "Semua sesi hari ini sudah selesai"
        }
    }
}

@Composable
private fun TeacherQuickActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    containerColor: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    val haptics = rememberHapticFeedbackHelper()

    Surface(
        modifier = modifier
            .sulaoneInteractiveTouchTarget(48.dp)
            .springPressable {
                haptics.tapLight()
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        shadowElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Compares [slot]'s real session_start/session_end against the current
 * time — server-corrected via [com.sultanagung1.sista.core.util.DateUtils]
 * rather than the device clock, so a teacher (or a student peeking at a
 * shared device) can't spoof "sedang berlangsung" by changing the phone's
 * clock/date.
 */
private fun isScheduleActiveNow(slot: TeacherScheduleSlot, nowMinutes: Int): Boolean {
    val start = com.sultanagung1.sista.core.util.DateUtils.parseMinutesOfDay(slot.sessionStart) ?: return false
    val end = com.sultanagung1.sista.core.util.DateUtils.parseMinutesOfDay(slot.sessionEnd) ?: return false
    return nowMinutes in start..end
}

@Composable
private fun TeacherScheduleCard(
    schedule: TeacherScheduleSlot,
    onAttendanceClick: () -> Unit,
    onJournalClick: () -> Unit
) {
    val haptics = rememberHapticFeedbackHelper()
    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000L)
            tick++
        }
    }
    val isActiveNow = remember(schedule.id, tick) {
        isScheduleActiveNow(schedule, com.sultanagung1.sista.core.util.DateUtils.nowMinutesOfDay())
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isActiveNow) 1.dp else 0.5.dp,
            color = if (isActiveNow) Emerald600 else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        shadowElevation = 0.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isActiveNow) Emerald500 else Slate400)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isActiveNow) "SEKARANG DI KELAS" else "${schedule.sessionStart} - ${schedule.sessionEnd} WIB",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.4.sp
                        ),
                        color = if (isActiveNow) Emerald700 else Slate600
                    )
                }

                val roomName = schedule.roomName
                if (roomName != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Slate100)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = roomName,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = Slate700
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = schedule.subjectName ?: "Tanpa Mata Pelajaran",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Rombongan Belajar: ${schedule.classroomName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        haptics.tapMedium()
                        onAttendanceClick()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .sulaoneInteractiveTouchTarget(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.FactCheck,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Presensi Siswa", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        haptics.tapLight()
                        onJournalClick()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .sulaoneInteractiveTouchTarget(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.8.dp, Emerald700),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Emerald700),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Isi Jurnal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TeachingJournalCard(journal: TeachingJournalEntry) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        shadowElevation = 0.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${journal.classroomName} • ${journal.teachingDate ?: "-"}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Gold50)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = journal.status?.replaceFirstChar { it.uppercase() } ?: "Draft",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = Gold800
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = journal.topic ?: "Tanpa Materi",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            val notes = journal.notes
            if (!notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Emerald600,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Hadir: ${journal.studentsPresent} • Absen: ${journal.studentsAbsent}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Emerald700
                )
            }
        }
    }
}
