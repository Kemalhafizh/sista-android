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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.designsystem.AccentAmber
import com.sultanagung1.sista.core.designsystem.AccentRose
import com.sultanagung1.sista.core.designsystem.ClassSessionStatusChip
import com.sultanagung1.sista.core.designsystem.ClassSessionUnavailableState
import com.sultanagung1.sista.core.designsystem.KeepScreenAwake
import com.sultanagung1.sista.core.designsystem.LifecycleStartStopEffect
import com.sultanagung1.sista.core.designsystem.SessionAttendanceSummary
import com.sultanagung1.sista.core.designsystem.SessionCardListSkeleton
import com.sultanagung1.sista.core.designsystem.SulaoneButton
import com.sultanagung1.sista.core.designsystem.SulaoneButtonVariant
import com.sultanagung1.sista.core.designsystem.SulaoneCard
import com.sultanagung1.sista.core.designsystem.SulaoneErrorBanner
import com.sultanagung1.sista.core.designsystem.SulaoneQrCode
import com.sultanagung1.sista.core.designsystem.SulaoneTextField
import com.sultanagung1.sista.core.designsystem.SulaoneTieredLoading
import com.sultanagung1.sista.core.designsystem.SulaoneTopBar
import com.sultanagung1.sista.data.model.ClassSessionDto
import com.sultanagung1.sista.data.model.ClassSessionRules
import com.sultanagung1.sista.data.model.ClassSessionRules.QrFreshness
import com.sultanagung1.sista.data.model.ClassSessionRules.TimerTone
import com.sultanagung1.sista.data.model.ClassSessionStatus

/**
 * FASE 77.3: the running class — rotating QR, session timer, live attendance
 * and "Akhiri Kelas". A finished session opens here too, as its summary.
 */
@Composable
fun TeacherActiveSessionScreen(
    viewModel: TeacherActiveSessionViewModel,
    onOpenAttendanceList: (sessionId: Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showEndDialog by remember { mutableStateOf(false) }

    LifecycleStartStopEffect(
        onStart = { viewModel.onEvent(TeacherActiveSessionEvent.ScreenStarted) },
        onStop = { viewModel.onEvent(TeacherActiveSessionEvent.ScreenStopped) }
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

    val session = state.session
    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = if (state.status?.isFinished == true) "Ringkasan Sesi Kelas" else "Sesi Kelas Aktif",
                subtitle = session?.let { listOfNotNull(it.subjectName, it.classroomName).joinToString(" • ") },
                onNavigateBack = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                state.isLoading && session == null -> SulaoneTieredLoading(isLoading = true) { SessionCardListSkeleton(rows = 2) }
                state.notDeployed -> ClassSessionUnavailableState(
                    message = state.errorMessage ?: ClassSessionRules.NOT_DEPLOYED_MESSAGE,
                    onRetry = { viewModel.onEvent(TeacherActiveSessionEvent.Retry) }
                )
                session == null -> SulaoneErrorBanner(
                    message = state.errorMessage ?: "Sesi tidak ditemukan.",
                    onRetry = { viewModel.onEvent(TeacherActiveSessionEvent.Retry) }
                )
                state.isLive -> LiveSessionContent(
                    state = state,
                    session = session,
                    onOpenAttendanceList = { onOpenAttendanceList(state.sessionId) },
                    onEnd = { showEndDialog = true }
                )
                else -> FinishedSessionContent(
                    state = state,
                    session = session,
                    onOpenAttendanceList = { onOpenAttendanceList(state.sessionId) },
                    onBack = onNavigateBack
                )
            }
        }
    }

    if (state.showTimeUpDialog && !showEndDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(TeacherActiveSessionEvent.DismissTimeUp) },
            title = { Text("Waktu sesi telah habis") },
            text = { Text("Akhiri kelas sekarang? Jika dilanjutkan, sistem menutup sesi otomatis 5 menit setelah jadwal berakhir.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onEvent(TeacherActiveSessionEvent.DismissTimeUp)
                    showEndDialog = true
                }) { Text("Akhiri Kelas") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(TeacherActiveSessionEvent.ContinueAfterTimeUp) }) { Text("Lanjutkan 5 menit") }
            }
        )
    }

    if (showEndDialog && session != null) {
        EndSessionDialog(
            session = session,
            alphaCount = state.counts.alpha,
            onDismiss = { showEndDialog = false },
            onConfirm = { notes, topic ->
                showEndDialog = false
                viewModel.onEvent(TeacherActiveSessionEvent.EndSessionConfirmed(notes, topic))
            }
        )
    }
}

@Composable
private fun LiveSessionContent(
    state: TeacherActiveSessionState,
    session: ClassSessionDto,
    onOpenAttendanceList: () -> Unit,
    onEnd: () -> Unit
) {
    QrPanel(state)

    SessionTimerCard(state, session)

    SulaoneCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Kehadiran Real-time", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            SessionAttendanceSummary(state.counts)
            SulaoneButton(
                text = "Lihat & Absen Manual",
                onClick = onOpenAttendanceList,
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                variant = SulaoneButtonVariant.SecondaryOutlined,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    SulaoneButton(
        text = "Akhiri Kelas",
        onClick = onEnd,
        icon = Icons.Default.Stop,
        isLoading = state.isEnding,
        enabled = !state.isEnding,
        variant = SulaoneButtonVariant.DestructiveRose,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun QrPanel(state: TeacherActiveSessionState) {
    val freshness = state.qrFreshness
    val payload = state.qrPayload
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when {
                payload != null && freshness != QrFreshness.UNAVAILABLE -> SulaoneQrCode(
                    payload = payload,
                    contentDescription = "Kode QR presensi sesi kelas. Siswa memindai dari layar ini.",
                    dimmed = freshness == QrFreshness.STALE,
                    modifier = Modifier.fillMaxWidth()
                )
                freshness == QrFreshness.UNAVAILABLE -> QrPlaceholder {
                    Icon(Icons.Default.WifiOff, contentDescription = null, tint = AccentRose, modifier = Modifier.size(40.dp))
                    Text(
                        "Tidak bisa memperbarui QR — periksa koneksi internet.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                else -> QrPlaceholder { CircularProgressIndicator() }
            }
        }

        val caption = when (freshness) {
            QrFreshness.FRESH -> "QR diperbarui dalam ${state.qrSecondsLeft} dtk"
            QrFreshness.STALE -> "Mungkin kedaluwarsa — sedang memperbarui…"
            QrFreshness.UNAVAILABLE -> "Siswa bisa diabsen manual dari daftar hadir."
            QrFreshness.LOADING -> "Menyiapkan QR…"
        }
        Text(
            text = caption,
            style = MaterialTheme.typography.labelLarge,
            color = if (freshness == QrFreshness.FRESH) MaterialTheme.colorScheme.onSurfaceVariant else AccentAmber
        )
        if (freshness == QrFreshness.FRESH) {
            LinearProgressIndicator(
                progress = { state.qrSecondsLeft / state.qrRotationSeconds.coerceAtLeast(1).toFloat() },
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
        }
        Text(
            text = "QR berganti tiap ${state.qrRotationSeconds} detik — siswa harus memindai langsung dari layar ini.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun QrPlaceholder(content: @Composable () -> Unit) {
    SulaoneCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
        ) { content() }
    }
}

@Composable
private fun SessionTimerCard(state: TeacherActiveSessionState, session: ClassSessionDto) {
    val remaining = state.remainingSeconds
    val tone = state.timerTone
    val color = when (tone) {
        TimerTone.NORMAL -> MaterialTheme.colorScheme.onSurface
        TimerTone.WARNING -> AccentAmber
        TimerTone.CRITICAL, TimerTone.OVERTIME -> AccentRose
    }
    SulaoneCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Timer, contentDescription = null, tint = color)
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = when {
                        remaining == null -> "Sisa waktu sesi: —"
                        tone == TimerTone.OVERTIME -> "Lewat jadwal ${ClassSessionRules.formatCountdown(-remaining)}"
                        else -> "Sisa waktu sesi: ${ClassSessionRules.formatCountdown(remaining)}"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    // Read out only when the tone changes, not every second.
                    modifier = Modifier.semantics { if (tone != TimerTone.NORMAL) liveRegion = LiveRegionMode.Polite }
                )
                Text(
                    text = ClassSessionRules.timeRange(session.scheduledStart, session.scheduledEnd) +
                        (session.jamKe?.let { " (Jam ke-$it)" } ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FinishedSessionContent(
    state: TeacherActiveSessionState,
    session: ClassSessionDto,
    onOpenAttendanceList: () -> Unit,
    onBack: () -> Unit
) {
    val status = session.effectiveStatus
    SulaoneCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ClassSessionStatusChip(status)
            Text(session.subjectName ?: "Sesi kelas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                text = "Jadwal ${ClassSessionRules.timeRange(session.scheduledStart, session.scheduledEnd)}" +
                    (session.actualStart?.let { " • Berlangsung ${ClassSessionRules.timeRange(it, session.actualEnd)}" } ?: ""),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (status == ClassSessionStatus.AUTO_CLOSED) {
                Text(
                    "Sesi ditutup otomatis oleh sistem karena tidak diakhiri hingga 5 menit setelah jadwal.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AccentAmber
                )
            }
            val counts = if (state.counts.total > 0) state.counts else ClassSessionRules.countsOf(session)
            SessionAttendanceSummary(counts)
            session.topic?.takeIf { it.isNotBlank() }?.let { Text("Topik: $it", style = MaterialTheme.typography.bodyMedium) }
            session.notes?.takeIf { it.isNotBlank() }?.let { Text("Catatan: $it", style = MaterialTheme.typography.bodyMedium) }
        }
    }
    Text(
        "Setelah sesi selesai, koreksi kehadiran dilakukan oleh Waka Kurikulum/TU.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
    )
    SulaoneButton(text = "Lihat Daftar Hadir", onClick = onOpenAttendanceList, variant = SulaoneButtonVariant.SecondaryOutlined, modifier = Modifier.fillMaxWidth())
    SulaoneButton(text = "Kembali ke Daftar Sesi", onClick = onBack, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun EndSessionDialog(
    session: ClassSessionDto,
    alphaCount: Int,
    onDismiss: () -> Unit,
    onConfirm: (notes: String?, topic: String?) -> Unit
) {
    var notes by remember { mutableStateOf(session.notes.orEmpty()) }
    var topic by remember { mutableStateOf(session.topic.orEmpty()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Akhiri sesi kelas?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Akhiri ${session.subjectName.orEmpty()} ${session.classroomName.orEmpty()}? " +
                        if (alphaCount > 0) "$alphaCount siswa yang belum terabsen akan tercatat ALPHA." else "Semua siswa sudah terabsen."
                )
                SulaoneTextField(
                    value = topic,
                    onValueChange = { topic = it.take(ClassSessionRules.TOPIC_MAX_CHARS) },
                    label = "Topik/materi yang diajarkan (opsional)",
                    maxCharacters = ClassSessionRules.TOPIC_MAX_CHARS
                )
                SulaoneTextField(
                    value = notes,
                    onValueChange = { notes = it.take(ClassSessionRules.END_NOTES_MAX_CHARS) },
                    label = "Catatan KBM hari ini (opsional)",
                    maxCharacters = ClassSessionRules.END_NOTES_MAX_CHARS,
                    singleLine = false
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(notes.ifBlank { null }, topic.ifBlank { null }) }) {
                Text("Akhiri Kelas", color = AccentRose)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
