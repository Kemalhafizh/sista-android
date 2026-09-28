package com.sultanagung1.sista.ui.teacher.sessions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.designsystem.KeepScreenAwake
import com.sultanagung1.sista.core.designsystem.LifecycleStartStopEffect
import com.sultanagung1.sista.core.designsystem.SulaoneQrCode
import com.sultanagung1.sista.core.ui.component.ButtonSize
import com.sultanagung1.sista.core.ui.component.ButtonVariant
import com.sultanagung1.sista.core.ui.component.ErrorState
import com.sultanagung1.sista.core.ui.component.IconBadge
import com.sultanagung1.sista.core.ui.component.InlineBanner
import com.sultanagung1.sista.core.ui.component.SistaButton
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SistaTextField
import com.sultanagung1.sista.core.ui.component.SistaTopBar
import com.sultanagung1.sista.core.ui.component.SkeletonList
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.core.ui.theme.colors
import com.sultanagung1.sista.data.model.ClassSessionDto
import com.sultanagung1.sista.data.model.ClassSessionRules
import com.sultanagung1.sista.data.model.ClassSessionRules.QrFreshness
import com.sultanagung1.sista.data.model.ClassSessionRules.TimerTone
import com.sultanagung1.sista.data.model.ClassSessionStatus

/**
 * FASE 77.3: the running class — rotating QR from the server, session timer,
 * live attendance and "Akhiri kelas". A finished session opens here too, as
 * its summary.
 */
@Composable
fun TeacherActiveSessionScreen(
    viewModel: TeacherActiveSessionViewModel,
    onOpenAttendanceList: (sessionId: Long) -> Unit,
    onNavigateBack: () -> Unit,
    onOpenTeachingJournal: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LifecycleStartStopEffect(
        onStart = { viewModel.onEvent(TeacherActiveSessionEvent.ScreenStarted) },
        onStop = { viewModel.onEvent(TeacherActiveSessionEvent.ScreenStopped) },
    )
    // 77.3.5: the QR must stay on screen, bright, for the whole class.
    KeepScreenAwake(enabled = state.isLive, maxBrightness = true)
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is TeacherActiveSessionEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    TeacherActiveSessionContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onRetry = { viewModel.onEvent(TeacherActiveSessionEvent.Retry) },
        onOpenAttendanceList = { onOpenAttendanceList(state.sessionId) },
        onEndConfirmed = { notes, topic -> viewModel.onEvent(TeacherActiveSessionEvent.EndSessionConfirmed(notes, topic)) },
        onDismissTimeUp = { viewModel.onEvent(TeacherActiveSessionEvent.DismissTimeUp) },
        onContinueAfterTimeUp = { viewModel.onEvent(TeacherActiveSessionEvent.ContinueAfterTimeUp) },
        onOpenTeachingJournal = onOpenTeachingJournal,
        onNavigateBack = onNavigateBack,
    )
}

/** The session screen without a ViewModel, for previews and screenshots. */
@Composable
fun TeacherActiveSessionContent(
    state: TeacherActiveSessionState,
    onRetry: () -> Unit,
    onOpenAttendanceList: () -> Unit,
    onEndConfirmed: (notes: String?, topic: String?) -> Unit,
    onDismissTimeUp: () -> Unit,
    onContinueAfterTimeUp: () -> Unit,
    onOpenTeachingJournal: () -> Unit,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    qrContent: @Composable (payload: String, dimmed: Boolean) -> Unit = { payload, dimmed ->
        SulaoneQrCode(
            payload = payload,
            contentDescription = "Kode QR presensi sesi kelas. Siswa memindai dari layar ini.",
            dimmed = dimmed,
            modifier = Modifier.fillMaxWidth(),
        )
    },
) {
    var showEndDialog by remember { mutableStateOf(false) }
    val session = state.session

    ShellTheme {
        Scaffold(
            topBar = {
                SistaTopBar(
                    title = if (state.status?.isFinished == true) "Ringkasan Sesi Kelas" else "Sesi Kelas Aktif",
                    subtitle = session?.let { listOfNotNull(it.subjectName, it.classroomName).joinToString(" · ") },
                    onBack = onNavigateBack,
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = SistaTheme.colors.background,
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.screen, vertical = Spacing.sm),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.lg),
            ) {
                when {
                    state.isLoading && session == null -> SkeletonList(rows = 3)
                    state.notDeployed -> SessionsUnavailable(state.errorMessage ?: ClassSessionRules.NOT_DEPLOYED_MESSAGE, onRetry)
                    session == null -> ErrorState(title = "Sesi tidak ditemukan", body = state.errorMessage, onRetry = onRetry)
                    state.isLive -> LiveSession(state, session, qrContent, onOpenAttendanceList) { showEndDialog = true }
                    else -> FinishedSession(state, session, onOpenAttendanceList, onOpenTeachingJournal, onNavigateBack)
                }
            }
        }

        if (state.showTimeUpDialog && !showEndDialog) {
            AlertDialog(
                onDismissRequest = onDismissTimeUp,
                title = { Text("Waktu sesi telah habis") },
                text = {
                    val autoCloseAt = ClassSessionRules.clockOf(session?.autoCloseAt)
                    Text(
                        "Akhiri kelas sekarang? Jika dilanjutkan, sistem menutup sesi otomatis " +
                            (autoCloseAt?.let { "pukul $it." } ?: "5 menit setelah jadwal berakhir."),
                    )
                },
                confirmButton = {
                    SistaButton("Akhiri kelas", {
                        onDismissTimeUp()
                        showEndDialog = true
                    }, variant = ButtonVariant.Text)
                },
                dismissButton = { SistaButton("Lanjutkan 5 menit", onContinueAfterTimeUp, variant = ButtonVariant.Text) },
            )
        }

        if (showEndDialog && session != null) {
            EndSessionDialog(
                session = session,
                alphaCount = state.counts.alpha,
                onDismiss = { showEndDialog = false },
                onConfirm = { notes, topic ->
                    showEndDialog = false
                    onEndConfirmed(notes, topic)
                },
            )
        }
    }
}

@Composable
private fun LiveSession(
    state: TeacherActiveSessionState,
    session: ClassSessionDto,
    qrContent: @Composable (String, Boolean) -> Unit,
    onOpenAttendanceList: () -> Unit,
    onEnd: () -> Unit,
) {
    QrPanel(state, qrContent)
    TimerCard(state, session)
    SistaCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Text("Kehadiran saat ini", style = SistaTheme.typography.titleMedium)
            AttendanceSummary(state.counts)
            SistaButton(
                "Lihat & absen manual",
                onOpenAttendanceList,
                variant = ButtonVariant.Outlined,
                leadingIcon = Icons.AutoMirrored.Outlined.ArrowForward,
                fullWidth = true,
            )
        }
    }
    SistaButton(
        "Akhiri kelas",
        onEnd,
        variant = ButtonVariant.Danger,
        size = ButtonSize.Large,
        leadingIcon = Icons.Outlined.Stop,
        loading = state.isEnding,
        enabled = !state.isEnding,
        fullWidth = true,
        modifier = Modifier.testTag("end_session_button"),
    )
}

@Composable
private fun QrPanel(state: TeacherActiveSessionState, qrContent: @Composable (String, Boolean) -> Unit) {
    val freshness = state.qrFreshness
    val payload = state.qrToken
    SistaCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Box(
                Modifier
                    .widthIn(max = 300.dp)
                    .fillMaxWidth()
                    .height(if (payload != null && freshness != QrFreshness.UNAVAILABLE) 300.dp else 220.dp),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    payload != null && freshness != QrFreshness.UNAVAILABLE -> qrContent(payload, freshness == QrFreshness.STALE)
                    freshness == QrFreshness.UNAVAILABLE -> Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    ) {
                        IconBadge(Icons.Outlined.WifiOff, tone = StatusTone.Danger, size = 56.dp)
                        Text(
                            "Tidak bisa memperbarui QR — periksa koneksi internet.",
                            textAlign = TextAlign.Center,
                            style = SistaTheme.typography.bodyMedium,
                        )
                    }
                    else -> CircularProgressIndicator()
                }
            }
            Text(
                when (freshness) {
                    QrFreshness.FRESH -> "QR diperbarui dalam ${state.qrSecondsLeft} dtk"
                    QrFreshness.STALE -> "Mungkin kedaluwarsa — sedang memperbarui…"
                    QrFreshness.UNAVAILABLE -> "Siswa bisa diabsen manual dari daftar hadir."
                    QrFreshness.LOADING -> "Menyiapkan QR…"
                },
                style = SistaTheme.typography.labelLarge,
                color = if (freshness == QrFreshness.FRESH) SistaTheme.colors.onSurfaceVariant else StatusTone.Warning.colors().content,
            )
            if (freshness == QrFreshness.FRESH) {
                LinearProgressIndicator(
                    progress = { state.qrSecondsLeft / state.qrRotationSeconds.coerceAtLeast(1).toFloat() },
                    modifier = Modifier
                        .widthIn(max = 260.dp)
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    drawStopIndicator = {},
                )
            }
            Text(
                "QR berganti tiap ${state.qrRotationSeconds} detik — siswa memindai langsung dari layar ini.",
                style = SistaTheme.typography.bodySmall,
                color = SistaTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun TimerCard(state: TeacherActiveSessionState, session: ClassSessionDto) {
    val remaining = state.remainingSeconds
    val tone = when (state.timerTone) {
        TimerTone.NORMAL -> StatusTone.Brand
        TimerTone.WARNING -> StatusTone.Warning
        TimerTone.CRITICAL, TimerTone.OVERTIME -> StatusTone.Danger
    }
    SistaCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Outlined.Timer, tone = tone)
            Spacer(Modifier.width(Spacing.md))
            Column {
                Text(
                    when {
                        remaining == null -> "Sisa waktu sesi: —"
                        state.timerTone == TimerTone.OVERTIME -> "Lewat jadwal ${ClassSessionRules.formatCountdown(-remaining)}"
                        else -> "Sisa waktu ${ClassSessionRules.formatCountdown(remaining)}"
                    },
                    style = SistaTheme.typography.titleMedium,
                    color = if (state.timerTone == TimerTone.NORMAL) SistaTheme.colors.onSurface else tone.colors().content,
                    // Read out only when the tone changes, not every second.
                    modifier = Modifier.semantics { if (state.timerTone != TimerTone.NORMAL) liveRegion = LiveRegionMode.Polite },
                )
                Text(
                    ClassSessionRules.timeRange(session.scheduledStart, session.scheduledEnd) + (session.jamKe?.let { " · Jam ke-$it" } ?: ""),
                    style = SistaTheme.typography.bodySmall,
                    color = SistaTheme.colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun FinishedSession(
    state: TeacherActiveSessionState,
    session: ClassSessionDto,
    onOpenAttendanceList: () -> Unit,
    onOpenTeachingJournal: () -> Unit,
    onBack: () -> Unit,
) {
    val status = session.effectiveStatus
    SistaCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            SessionStatusPill(status)
            Text(session.subjectName ?: "Sesi kelas", style = SistaTheme.typography.titleLarge)
            Text(
                "Jadwal ${ClassSessionRules.timeRange(session.scheduledStart, session.scheduledEnd)}" +
                    (session.actualStart?.let { " · Berlangsung ${ClassSessionRules.timeRange(it, session.actualEnd)}" } ?: ""),
                style = SistaTheme.typography.bodyMedium,
                color = SistaTheme.colors.onSurfaceVariant,
            )
            val counts = if (state.counts.total > 0) state.counts else ClassSessionRules.countsOf(session)
            AttendanceSummary(counts)
            session.topic?.takeIf { it.isNotBlank() }?.let { Text("Topik: $it", style = SistaTheme.typography.bodyMedium) }
            session.notes?.takeIf { it.isNotBlank() }?.let { Text("Catatan: $it", style = SistaTheme.typography.bodyMedium) }
        }
    }
    if (status == ClassSessionStatus.AUTO_CLOSED) {
        InlineBanner(
            message = "Sesi ditutup otomatis oleh sistem karena tidak diakhiri hingga 5 menit setelah jadwal.",
            tone = StatusTone.Warning,
        )
    }
    // The server creates a teaching-journal draft when a session ends with a topic.
    if (session.teachingJournalId != null) {
        InlineBanner(
            title = "Draf jurnal mengajar sudah dibuat",
            message = "Dibuat dari topik sesi ini. Lengkapi kegiatan dan metode pembelajarannya.",
            tone = StatusTone.Info,
            actionLabel = "Buka Jurnal KBM",
            onAction = onOpenTeachingJournal,
        )
    }
    Text(
        "Setelah sesi selesai, koreksi kehadiran dilakukan oleh Waka Kurikulum/TU.",
        style = SistaTheme.typography.bodySmall,
        color = SistaTheme.colors.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
    SistaButton("Lihat daftar hadir", onOpenAttendanceList, variant = ButtonVariant.Outlined, fullWidth = true)
    SistaButton("Kembali ke daftar sesi", onBack, fullWidth = true)
}

@Composable
private fun EndSessionDialog(
    session: ClassSessionDto,
    alphaCount: Int,
    onDismiss: () -> Unit,
    onConfirm: (notes: String?, topic: String?) -> Unit,
) {
    var notes by remember { mutableStateOf(session.notes.orEmpty()) }
    var topic by remember { mutableStateOf(session.topic.orEmpty()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.EditNote, contentDescription = null) },
        title = { Text("Akhiri sesi kelas?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Text(
                    "Akhiri ${session.subjectName.orEmpty()} ${session.classroomName.orEmpty()}? " +
                        if (alphaCount > 0) "$alphaCount siswa yang belum terabsen akan tercatat ALPHA." else "Semua siswa sudah terabsen.",
                    style = SistaTheme.typography.bodyMedium,
                )
                SistaTextField(
                    value = topic,
                    onValueChange = { topic = it.take(ClassSessionRules.TOPIC_MAX_CHARS) },
                    label = "Topik/materi (opsional)",
                    helperText = "Dengan topik, draf jurnal mengajar dibuat otomatis.",
                )
                SistaTextField(
                    value = notes,
                    onValueChange = { notes = it.take(ClassSessionRules.END_NOTES_MAX_CHARS) },
                    label = "Catatan KBM (opsional)",
                    singleLine = false,
                )
            }
        },
        confirmButton = { SistaButton("Akhiri kelas", { onConfirm(notes.ifBlank { null }, topic.ifBlank { null }) }, variant = ButtonVariant.Danger) },
        dismissButton = { SistaButton("Batal", onDismiss, variant = ButtonVariant.Text) },
    )
}
