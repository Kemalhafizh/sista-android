package com.sultanagung1.sista.ui.teacher.sessions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.designsystem.AccentAmber
import com.sultanagung1.sista.core.designsystem.ClassSessionUnavailableState
import com.sultanagung1.sista.core.designsystem.LifecycleStartStopEffect
import com.sultanagung1.sista.core.designsystem.SessionAttendanceChip
import com.sultanagung1.sista.core.designsystem.SessionCardListSkeleton
import com.sultanagung1.sista.core.designsystem.SulaoneButton
import com.sultanagung1.sista.core.designsystem.SulaoneEmptyState
import com.sultanagung1.sista.core.designsystem.SulaoneErrorBanner
import com.sultanagung1.sista.core.designsystem.SulaoneTextField
import com.sultanagung1.sista.core.designsystem.SulaoneTieredLoading
import com.sultanagung1.sista.core.designsystem.SulaoneTopBar
import com.sultanagung1.sista.data.model.ClassSessionRules
import com.sultanagung1.sista.data.model.SessionAttendanceDto
import com.sultanagung1.sista.data.model.SessionAttendanceStatus

/**
 * FASE 77.4: "Daftar Hadir" — who scanned, who did not, and manual marks.
 */
@Composable
fun TeacherAttendanceListScreen(
    viewModel: TeacherAttendanceListViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LifecycleStartStopEffect(
        onStart = { viewModel.onEvent(TeacherAttendanceListEvent.ScreenStarted) },
        onStop = { viewModel.onEvent(TeacherAttendanceListEvent.ScreenStopped) }
    )
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is TeacherAttendanceListEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    val session = state.session
    val pending = state.pendingChanges
    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Daftar Hadir",
                subtitle = session?.let {
                    listOfNotNull(it.subjectName, it.classroomName, it.jamKe?.let { j -> "Jam ke-$j" }).joinToString(" • ")
                },
                onNavigateBack = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (state.rows.isNotEmpty()) {
                Surface(tonalElevation = 3.dp) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val c = state.previewCounts
                        Text(
                            "Ringkasan: ${c.hadir} Hadir • ${c.telat} Telat • ${c.sakit} Sakit • ${c.izin} Izin • ${c.alpha} Alpha",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (pending.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = { viewModel.onEvent(TeacherAttendanceListEvent.DiscardChanges) }) { Text("Batalkan") }
                                Spacer(modifier = Modifier.width(8.dp))
                                SulaoneButton(
                                    text = "Simpan Perubahan (${pending.size} diubah)",
                                    onClick = { viewModel.onEvent(TeacherAttendanceListEvent.Save) },
                                    isLoading = state.isSaving,
                                    enabled = !state.isSaving,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            when {
                state.isLoading && state.rows.isEmpty() -> item {
                    SulaoneTieredLoading(isLoading = true, modifier = Modifier.padding(horizontal = 20.dp)) { SessionCardListSkeleton() }
                }
                state.notDeployed -> item {
                    ClassSessionUnavailableState(
                        message = state.errorMessage ?: ClassSessionRules.NOT_DEPLOYED_MESSAGE,
                        onRetry = { viewModel.onEvent(TeacherAttendanceListEvent.Refresh) }
                    )
                }
                state.rows.isEmpty() && state.errorMessage != null -> item {
                    SulaoneErrorBanner(
                        message = state.errorMessage.orEmpty(),
                        onRetry = { viewModel.onEvent(TeacherAttendanceListEvent.Refresh) },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
                else -> {
                    if (session != null && !state.isEditable) {
                        item {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = AccentAmber)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Sesi sudah berakhir. Koreksi kehadiran melalui Waka Kurikulum/TU.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    item {
                        SulaoneTextField(
                            value = state.query,
                            onValueChange = { viewModel.onEvent(TeacherAttendanceListEvent.QueryChanged(it)) },
                            label = "Cari nama siswa atau NIS",
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                    item { StatusFilterRow(state.filter) { viewModel.onEvent(TeacherAttendanceListEvent.FilterChanged(it)) } }
                    val visible = state.visibleRows
                    if (visible.isEmpty()) {
                        item {
                            SulaoneEmptyState(
                                title = "Tidak ada siswa",
                                description = if (state.rows.isEmpty()) "Belum ada siswa terdaftar di sesi ini." else "Tidak ada siswa yang cocok dengan pencarian/filter."
                            )
                        }
                    }
                    itemsIndexed(visible, key = { _, row -> row.studentId }) { index, row ->
                        StudentRow(
                            number = index + 1,
                            row = row,
                            status = state.statusOf(row),
                            isEdited = pending.containsKey(row.studentId),
                            editable = state.isEditable && !state.isSaving,
                            onMark = { viewModel.onEvent(TeacherAttendanceListEvent.Mark(row.studentId, it)) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusFilterRow(selected: SessionAttendanceStatus?, onSelect: (SessionAttendanceStatus?) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text("Semua") })
        SessionAttendanceStatus.values().forEach { status ->
            FilterChip(
                selected = selected == status,
                onClick = { onSelect(if (selected == status) null else status) },
                label = { Text(ClassSessionRules.label(status)) }
            )
        }
    }
}

@Composable
private fun StudentRow(
    number: Int,
    row: SessionAttendanceDto,
    status: SessionAttendanceStatus,
    isEdited: Boolean,
    editable: Boolean,
    onMark: (SessionAttendanceStatus) -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "$number. ${row.studentName ?: "Siswa"}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                listOfNotNull(row.studentNis?.let { "NIS: $it" }, ClassSessionRules.checkInLabel(row)).joinToString(" • ") +
                    if (isEdited) " • diubah, belum disimpan" else "",
                style = MaterialTheme.typography.bodySmall,
                color = if (isEdited) AccentAmber else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box {
            Row(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .then(
                        if (editable) {
                            Modifier
                                .clickable(role = Role.DropdownList) { menuOpen = true }
                                .semantics { contentDescription = "Ubah status ${row.studentName.orEmpty()}, sekarang ${ClassSessionRules.label(status)}" }
                        } else {
                            Modifier
                        }
                    )
                    .padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SessionAttendanceChip(status)
                if (editable) Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                ClassSessionRules.TEACHER_MARK_OPTIONS.forEach { option ->
                    DropdownMenuItem(
                        text = { SessionAttendanceChip(option) },
                        onClick = {
                            menuOpen = false
                            onMark(option)
                        }
                    )
                }
            }
        }
    }
}
