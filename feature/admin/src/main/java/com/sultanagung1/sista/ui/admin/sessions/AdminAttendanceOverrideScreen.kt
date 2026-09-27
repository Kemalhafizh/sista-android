package com.sultanagung1.sista.ui.admin.sessions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.designsystem.AccentAmber
import com.sultanagung1.sista.core.designsystem.ClassSessionUnavailableState
import com.sultanagung1.sista.core.designsystem.LifecycleStartStopEffect
import com.sultanagung1.sista.core.designsystem.SessionAttendanceChip
import com.sultanagung1.sista.core.designsystem.SessionAttendanceSummary
import com.sultanagung1.sista.core.designsystem.SessionCardListSkeleton
import com.sultanagung1.sista.core.designsystem.SulaoneButton
import com.sultanagung1.sista.core.designsystem.SulaoneCard
import com.sultanagung1.sista.core.designsystem.SulaoneEmptyState
import com.sultanagung1.sista.core.designsystem.SulaoneErrorBanner
import com.sultanagung1.sista.core.designsystem.SulaoneTextField
import com.sultanagung1.sista.core.designsystem.SulaoneTieredLoading
import com.sultanagung1.sista.core.designsystem.SulaoneTopBar
import com.sultanagung1.sista.data.model.ClassSessionRules
import com.sultanagung1.sista.data.model.SessionAttendanceDto

/**
 * FASE 77.6.2: "Koreksi Absensi" — every change needs a reason and is logged.
 */
@Composable
fun AdminAttendanceOverrideScreen(
    viewModel: AdminAttendanceOverrideViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LifecycleStartStopEffect(
        onStart = { viewModel.onEvent(AdminAttendanceOverrideEvent.ScreenStarted) },
        onStop = {}
    )
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AdminAttendanceOverrideEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    val session = state.session
    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = if (state.canCorrect) "Koreksi Absensi" else "Detail Kehadiran Sesi",
                subtitle = session?.let {
                    listOfNotNull(
                        listOfNotNull(it.subjectName, it.classroomName).joinToString(" • ").ifBlank { null },
                        ClassSessionRules.formatDateId(it.sessionDate),
                        it.teacherName?.let { t -> "Guru: $t" }
                    ).joinToString(" • ")
                },
                onNavigateBack = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "mode") { ModeBanner(canCorrect = state.canCorrect) }
            when {
                state.notDeployed -> item(key = "unavailable") {
                    ClassSessionUnavailableState(
                        message = state.errorMessage ?: ClassSessionRules.NOT_DEPLOYED_MESSAGE,
                        onRetry = { viewModel.onEvent(AdminAttendanceOverrideEvent.Refresh) }
                    )
                }
                state.isLoading && state.rows.isEmpty() -> item(key = "loading") {
                    SulaoneTieredLoading(isLoading = true) { SessionCardListSkeleton() }
                }
                state.rows.isEmpty() && state.errorMessage != null -> item(key = "error") {
                    SulaoneErrorBanner(
                        message = state.errorMessage.orEmpty(),
                        onRetry = { viewModel.onEvent(AdminAttendanceOverrideEvent.Refresh) }
                    )
                }
                state.rows.isEmpty() -> item(key = "empty") {
                    SulaoneEmptyState(title = "Tidak ada data kehadiran", description = "Sesi ini belum punya daftar siswa.")
                }
                else -> {
                    item(key = "summary") { SessionAttendanceSummary(ClassSessionRules.countsOf(state.rows)) }
                    items(state.sortedRows, key = { it.id }) { row ->
                        OverrideRow(
                            row = row,
                            state = state,
                            onTarget = { viewModel.onEvent(AdminAttendanceOverrideEvent.TargetSelected(row.id, it)) },
                            onReason = { viewModel.onEvent(AdminAttendanceOverrideEvent.ReasonChanged(row.id, it)) },
                            onSubmit = { viewModel.onEvent(AdminAttendanceOverrideEvent.Submit(row.id)) },
                            onCancel = { viewModel.onEvent(AdminAttendanceOverrideEvent.Cancel(row.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeBanner(canCorrect: Boolean) {
    SulaoneCard(modifier = Modifier.fillMaxWidth(), backgroundColor = AccentAmber.copy(alpha = 0.12f)) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(if (canCorrect) Icons.Default.Warning else Icons.Default.Visibility, contentDescription = null, tint = AccentAmber)
            Text(
                if (canCorrect) {
                    "Anda memasuki mode koreksi. Setiap perubahan tercatat di log audit dan wajib disertai alasan."
                } else {
                    "Anda dapat melihat kehadiran sesi ini. Koreksi dilakukan oleh Admin, Waka Kurikulum, atau TU."
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun OverrideRow(
    row: SessionAttendanceDto,
    state: AdminAttendanceOverrideState,
    onTarget: (com.sultanagung1.sista.data.model.SessionAttendanceStatus) -> Unit,
    onReason: (String) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit
) {
    val current = row.effectiveStatus
    val targets = ClassSessionRules.overrideTargets(current)
    val draft = state.draftOf(row.id)
    val saving = state.savingAttendanceId == row.id
    SulaoneCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(row.studentName ?: "Siswa", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        listOfNotNull(row.studentNis?.let { "NIS: $it" }, ClassSessionRules.checkInLabel(row)).joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                SessionAttendanceChip(current)
            }

            ClassSessionRules.overrideAuditLabel(row)?.let { audit ->
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.History, contentDescription = null, tint = AccentAmber)
                    Column {
                        Text(audit, style = MaterialTheme.typography.labelMedium, color = AccentAmber)
                        row.overrideReason?.takeIf { it.isNotBlank() }?.let {
                            Text("Alasan: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            when {
                targets.isEmpty() -> Text(
                    "— Tidak perlu koreksi —",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                !state.canCorrect -> Unit
                else -> {
                    Text("Ubah ke:", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        targets.forEach { target ->
                            FilterChip(
                                selected = draft.target == target,
                                onClick = { onTarget(target) },
                                label = { Text(ClassSessionRules.label(target)) },
                                enabled = !saving
                            )
                        }
                    }
                    if (draft.target != null) {
                        val tooShort = draft.reason.isNotEmpty() && !ClassSessionRules.isOverrideReasonValid(draft.reason)
                        SulaoneTextField(
                            value = draft.reason,
                            onValueChange = onReason,
                            label = "Alasan koreksi (wajib)",
                            singleLine = false,
                            isError = tooShort,
                            errorMessage = if (tooShort) "Minimal ${ClassSessionRules.OVERRIDE_REASON_MIN_CHARS} karakter." else null,
                            helperText = "Contoh: terlambat karena upacara pramuka, dikonfirmasi pembina.",
                            enabled = !saving
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = onCancel, enabled = !saving) { Text("Batal") }
                            SulaoneButton(
                                text = "Simpan Koreksi",
                                onClick = onSubmit,
                                isLoading = saving,
                                enabled = draft.canSubmit && !saving && state.savingAttendanceId == null,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
