package com.sultanagung1.sista.ui.teacher.sessions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.designsystem.ClassSessionStatusChip
import com.sultanagung1.sista.core.designsystem.ClassSessionUnavailableState
import com.sultanagung1.sista.core.designsystem.LifecycleStartStopEffect
import com.sultanagung1.sista.core.designsystem.SessionAttendanceSummary
import com.sultanagung1.sista.core.designsystem.SessionCardListSkeleton
import com.sultanagung1.sista.core.designsystem.SulaoneButton
import com.sultanagung1.sista.core.designsystem.SulaoneButtonVariant
import com.sultanagung1.sista.core.designsystem.SulaoneCard
import com.sultanagung1.sista.core.designsystem.SulaoneEmptyState
import com.sultanagung1.sista.core.designsystem.SulaoneErrorBanner
import com.sultanagung1.sista.core.designsystem.SulaonePullToRefreshBox
import com.sultanagung1.sista.core.designsystem.SulaoneTextField
import com.sultanagung1.sista.core.designsystem.SulaoneTieredLoading
import com.sultanagung1.sista.core.designsystem.SulaoneTopBar
import com.sultanagung1.sista.data.model.ClassSessionDto
import com.sultanagung1.sista.data.model.ClassSessionRules
import com.sultanagung1.sista.data.model.ClassSessionRules.StartAction
import com.sultanagung1.sista.data.model.ClassSessionStatus

/**
 * FASE 77.2: "Sesi Kelas Hari Ini" — the teacher's "Mengajar" tab.
 */
@Composable
fun TeacherTodaySessionsScreen(
    viewModel: TeacherTodaySessionsViewModel,
    onOpenActiveSession: (sessionId: Long) -> Unit,
    onOpenWeeklySchedule: () -> Unit,
    onNavigateBack: (() -> Unit)? = null
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var startTarget by remember { mutableStateOf<ClassSessionDto?>(null) }

    LifecycleStartStopEffect(
        onStart = { viewModel.onEvent(TeacherTodaySessionsEvent.ScreenStarted) },
        onStop = { viewModel.onEvent(TeacherTodaySessionsEvent.ScreenStopped) }
    )
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is TeacherTodaySessionsEffect.OpenActiveSession -> onOpenActiveSession(effect.sessionId)
                is TeacherTodaySessionsEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Sesi Kelas Hari Ini",
                subtitle = state.todayLabel,
                onNavigateBack = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        SulaonePullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.onEvent(TeacherTodaySessionsEvent.Refresh) },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when {
                    state.isLoading && state.sessions.isEmpty() -> item(key = "loading") {
                        SulaoneTieredLoading(isLoading = true) { SessionCardListSkeleton() }
                    }
                    state.notDeployed -> item(key = "unavailable") {
                        ClassSessionUnavailableState(
                            message = state.errorMessage ?: ClassSessionRules.NOT_DEPLOYED_MESSAGE,
                            onRetry = { viewModel.onEvent(TeacherTodaySessionsEvent.Refresh) }
                        )
                    }
                    state.sessions.isEmpty() && state.errorMessage != null -> item(key = "error") {
                        SulaoneErrorBanner(
                            message = state.errorMessage.orEmpty(),
                            onRetry = { viewModel.onEvent(TeacherTodaySessionsEvent.Refresh) }
                        )
                    }
                    state.sessions.isEmpty() -> item(key = "empty") {
                        SulaoneEmptyState(
                            title = "Tidak Ada Jadwal Mengajar Hari Ini",
                            description = "Jadwal mengajar Anda untuk hari ini kosong.",
                            icon = Icons.Default.EventBusy,
                            ctaLabel = "Lihat Jadwal Minggu Ini",
                            ctaIcon = null,
                            onCtaClick = onOpenWeeklySchedule
                        )
                    }
                    else -> {
                        // A failed background poll keeps the list; say so above it.
                        state.errorMessage?.let { message ->
                            item(key = "stale") {
                                SulaoneErrorBanner(
                                    message = "Gagal memperbarui: $message",
                                    onRetry = { viewModel.onEvent(TeacherTodaySessionsEvent.Refresh) }
                                )
                            }
                        }
                        items(state.sessions, key = { "${it.scheduleId}-${it.sessionId}" }) { session ->
                            SessionCard(
                                session = session,
                                startAction = ClassSessionRules.startActionFor(session, state.nowMinutes),
                                isStarting = state.startingScheduleId == session.scheduleId,
                                onStart = { startTarget = session },
                                onOpen = { id -> viewModel.onEvent(TeacherTodaySessionsEvent.OpenSession(id)) }
                            )
                        }
                    }
                }
            }
        }
    }

    startTarget?.let { target ->
        StartSessionDialog(
            session = target,
            onDismiss = { startTarget = null },
            onConfirm = { topic ->
                startTarget = null
                viewModel.onEvent(TeacherTodaySessionsEvent.StartSession(target.scheduleId, topic))
            }
        )
    }
}

@Composable
private fun SessionCard(
    session: ClassSessionDto,
    startAction: StartAction,
    isStarting: Boolean,
    onStart: () -> Unit,
    onOpen: (Long) -> Unit
) {
    val status = session.effectiveStatus
    val sessionId = session.sessionId
    SulaoneCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = if (sessionId != null) ({ onOpen(sessionId) }) else null
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ClassSessionStatusChip(status)
                Spacer(modifier = Modifier.weight(1f))
                session.jamKe?.let {
                    Text("Jam ke-$it", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(
                text = session.subjectName ?: "Mata pelajaran",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = listOfNotNull(session.classroomName?.let { "Kelas $it" }, ClassSessionRules.timeRange(session.scheduledStart, session.scheduledEnd))
                    .joinToString(" • "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (status.isFinished && session.actualStart != null) {
                Text(
                    text = "Berlangsung ${ClassSessionRules.timeRange(session.actualStart, session.actualEnd)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (status == ClassSessionStatus.ACTIVE || status.isFinished) {
                Spacer(modifier = Modifier.height(4.dp))
                SessionAttendanceSummary(ClassSessionRules.countsOf(session), showBreakdown = status.isFinished)
            }
            Spacer(modifier = Modifier.height(6.dp))
            when {
                status == ClassSessionStatus.ACTIVE && sessionId != null -> SulaoneButton(
                    text = "Buka Sesi Kelas",
                    onClick = { onOpen(sessionId) },
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    modifier = Modifier.fillMaxWidth()
                )
                status.isFinished && sessionId != null -> SulaoneButton(
                    text = "Lihat Detail",
                    onClick = { onOpen(sessionId) },
                    variant = SulaoneButtonVariant.SecondaryOutlined,
                    modifier = Modifier.fillMaxWidth()
                )
                else -> when (startAction) {
                    StartAction.Available -> SulaoneButton(
                        text = "Mulai Kelas",
                        onClick = onStart,
                        icon = Icons.Default.PlayArrow,
                        isLoading = isStarting,
                        enabled = !isStarting,
                        modifier = Modifier.fillMaxWidth()
                    )
                    is StartAction.NotYet -> {
                        SulaoneButton(text = "Mulai Kelas", onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth())
                        Text(
                            text = "Bisa dimulai pukul ${startAction.opensAt}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    StartAction.Missed -> Text(
                        text = "Jam pelajaran sudah lewat dan kelas tidak dimulai. Hubungi Waka Kurikulum bila perlu dicatat.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    StartAction.Hidden -> Unit
                }
            }
        }
    }
}

@Composable
private fun StartSessionDialog(session: ClassSessionDto, onDismiss: () -> Unit, onConfirm: (String?) -> Unit) {
    var topic by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mulai ${session.subjectName ?: "kelas"}?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Kelas ${session.classroomName.orEmpty()} • ${ClassSessionRules.timeRange(session.scheduledStart, session.scheduledEnd)}. " +
                        "Semua siswa tercatat alpha sampai scan QR atau Anda absen manual."
                )
                SulaoneTextField(
                    value = topic,
                    onValueChange = { topic = it.take(ClassSessionRules.TOPIC_MAX_CHARS) },
                    label = "Topik/materi (opsional)",
                    maxCharacters = ClassSessionRules.TOPIC_MAX_CHARS
                )
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(topic.ifBlank { null }) }) { Text("Mulai") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
