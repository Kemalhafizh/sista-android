package com.sultanagung1.sista.ui.admin.sessions

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.sultanagung1.sista.core.designsystem.SulaoneDatePicker
import com.sultanagung1.sista.core.designsystem.SulaoneEmptyState
import com.sultanagung1.sista.core.designsystem.SulaoneErrorBanner
import com.sultanagung1.sista.core.designsystem.SulaoneModalBottomSheet
import com.sultanagung1.sista.core.designsystem.SulaonePullToRefreshBox
import com.sultanagung1.sista.core.designsystem.SulaoneTieredLoading
import com.sultanagung1.sista.core.designsystem.SulaoneTopBar
import com.sultanagung1.sista.data.model.ClassSessionDto
import com.sultanagung1.sista.data.model.ClassSessionRules
import com.sultanagung1.sista.data.model.ClassSessionStatus

/**
 * FASE 77.6.1: "Manajemen Sesi Kelas" for admin, principal, Waka Kurikulum and TU.
 */
@Composable
fun AdminSessionManagementScreen(
    viewModel: AdminSessionManagementViewModel,
    onOpenSession: (sessionId: Long) -> Unit,
    onNavigateBack: (() -> Unit)?
) {
    val state by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    LifecycleStartStopEffect(
        onStart = { viewModel.onEvent(AdminSessionManagementEvent.ScreenStarted) },
        onStop = {}
    )

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Manajemen Sesi Kelas",
                subtitle = if (state.canCorrect) "Pantau & koreksi kehadiran per mapel" else "Pantau kehadiran per mapel (lihat saja)",
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        SulaonePullToRefreshBox(
            isRefreshing = state.isLoading && state.sessions.isNotEmpty(),
            onRefresh = { viewModel.onEvent(AdminSessionManagementEvent.Refresh) },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "filters") {
                    Filters(
                        dateLabel = state.dateLabel,
                        status = state.statusFilter,
                        onPickDate = { showDatePicker = true },
                        onStatus = { viewModel.onEvent(AdminSessionManagementEvent.StatusFilterChanged(it)) }
                    )
                }
                when {
                    state.notDeployed -> item(key = "unavailable") {
                        ClassSessionUnavailableState(
                            message = state.errorMessage ?: ClassSessionRules.NOT_DEPLOYED_MESSAGE,
                            onRetry = { viewModel.onEvent(AdminSessionManagementEvent.Refresh) }
                        )
                    }
                    state.isLoading && state.sessions.isEmpty() -> item(key = "loading") {
                        SulaoneTieredLoading(isLoading = true) { SessionCardListSkeleton() }
                    }
                    state.sessions.isEmpty() && state.errorMessage != null -> item(key = "error") {
                        SulaoneErrorBanner(
                            message = state.errorMessage.orEmpty(),
                            onRetry = { viewModel.onEvent(AdminSessionManagementEvent.Refresh) }
                        )
                    }
                    state.sessions.isEmpty() -> item(key = "empty") {
                        SulaoneEmptyState(
                            title = "Tidak Ada Sesi Kelas",
                            description = "Belum ada sesi kelas tercatat pada ${state.dateLabel}" +
                                (state.statusFilter?.let { " dengan status ${ClassSessionRules.label(it)}" } ?: "") + ".",
                            icon = Icons.Default.EventBusy
                        )
                    }
                    else -> {
                        item(key = "count") {
                            Text(
                                "${state.total} sesi pada ${state.dateLabel}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        items(state.sessions, key = { it.sessionId ?: it.scheduleId }) { session ->
                            AdminSessionCard(session = session, canCorrect = state.canCorrect, onOpen = onOpenSession)
                        }
                        if (state.hasMore) {
                            item(key = "more") {
                                LaunchedEffect(state.page) { viewModel.onEvent(AdminSessionManagementEvent.LoadMore) }
                                Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
                item(key = "reports") {
                    ReportsSection(onOpen = { viewModel.onEvent(AdminSessionManagementEvent.OpenReport(it)) })
                }
            }
        }
    }

    SulaoneDatePicker(
        isVisible = showDatePicker,
        onDismiss = { showDatePicker = false },
        onDateSelected = { millis ->
            showDatePicker = false
            millis?.let { viewModel.onEvent(AdminSessionManagementEvent.DateSelected(it)) }
        },
        title = "Pilih Tanggal Sesi"
    )

    SulaoneModalBottomSheet(
        isVisible = state.reportRange != null,
        onDismiss = { viewModel.onEvent(AdminSessionManagementEvent.CloseReport) },
        title = state.reportRange?.label
    ) {
        ReportSheet(state)
    }
}

@Composable
private fun Filters(
    dateLabel: String,
    status: ClassSessionStatus?,
    onPickDate: () -> Unit,
    onStatus: (ClassSessionStatus?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AssistChip(
            onClick = onPickDate,
            label = { Text("Tanggal: $dateLabel") },
            leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) }
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(selected = status == null, onClick = { onStatus(null) }, label = { Text("Semua") })
            listOf(ClassSessionStatus.ACTIVE, ClassSessionStatus.COMPLETED, ClassSessionStatus.AUTO_CLOSED, ClassSessionStatus.CANCELLED).forEach { s ->
                FilterChip(
                    selected = status == s,
                    onClick = { onStatus(if (status == s) null else s) },
                    label = { Text(ClassSessionRules.label(s)) }
                )
            }
        }
    }
}

@Composable
private fun AdminSessionCard(session: ClassSessionDto, canCorrect: Boolean, onOpen: (Long) -> Unit) {
    val sessionId = session.sessionId
    val counts = ClassSessionRules.countsOf(session)
    SulaoneCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = sessionId?.let { id -> { onOpen(id) } }
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    listOfNotNull(session.subjectName, session.classroomName).joinToString(" • "),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                ClassSessionStatusChip(session.effectiveStatus)
            }
            Text(
                listOfNotNull(
                    session.teacherName,
                    session.jamKe?.let { "Jam $it" },
                    ClassSessionRules.timeRange(session.scheduledStart, session.scheduledEnd)
                ).joinToString(" • "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SessionAttendanceSummary(counts, showBreakdown = false)
            Text(
                "${ClassSessionRules.presencePercent(counts.presenceRate)}% hadir • ${counts.alpha} alpha",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (sessionId != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        if (canCorrect) "Detail & Koreksi" else "Detail",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun ReportsSection(onOpen: (ReportRange) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
        HorizontalDivider()
        Text("Laporan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        ReportRange.values().forEach { range ->
            SulaoneButton(
                text = range.label,
                onClick = { onOpen(range) },
                icon = Icons.Default.Assessment,
                variant = SulaoneButtonVariant.SecondaryOutlined,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ReportSheet(state: AdminSessionManagementState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        when {
            state.isReportLoading -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { CircularProgressIndicator() }
            state.reportError != null -> SulaoneErrorBanner(message = state.reportError)
            else -> {
                val summary = state.report?.summary
                if (summary == null || summary.totalSessions == 0) {
                    Text("Belum ada sesi kelas pada rentang ini.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text(
                        "${summary.totalSessions} sesi • rata-rata ${ClassSessionRules.presencePercent(summary.averagePresenceRate)}% hadir",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Hadir ${summary.totalHadir} • Telat ${summary.totalTelat} • Sakit ${summary.totalSakit} • Izin ${summary.totalIzin} • Alpha ${summary.totalAlpha}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider()
                    Text("Per kelas", style = MaterialTheme.typography.labelLarge)
                    state.report?.details.orEmpty()
                        .sortedBy { it.presenceRate }
                        .forEach { row ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(row.label ?: "—", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "${ClassSessionRules.presencePercent(row.presenceRate)}% • ${row.alpha} alpha",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                }
            }
        }
        Spacer(modifier = Modifier.padding(bottom = 16.dp))
    }
}
