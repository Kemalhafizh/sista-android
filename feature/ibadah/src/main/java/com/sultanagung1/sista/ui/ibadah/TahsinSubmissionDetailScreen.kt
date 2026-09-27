package com.sultanagung1.sista.ui.ibadah

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.audio.AudioRecorderManager
import com.sultanagung1.sista.core.designsystem.*
import java.util.Locale

/**
 * Shared by the student (read-only: sees teacher's timestamped feedback) and
 * the teacher (can tap the waveform to pin a new note, and mark evaluation
 * done) — [isTeacherMode] gates the write actions; the backend independently
 * enforces who may actually annotate/mark-reviewed a given submission.
 */
@Composable
fun TahsinSubmissionDetailScreen(
    submissionId: Long,
    isTeacherMode: Boolean,
    recorderManager: AudioRecorderManager,
    viewModel: TahsinViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var pendingAnnotateTimestamp by remember { mutableStateOf<Int?>(null) }
    var noteInput by remember { mutableStateOf("") }
    var categoryInput by remember { mutableStateOf("") }

    LaunchedEffect(submissionId) { viewModel.loadDetail(submissionId) }
    DisposableEffect(Unit) { onDispose { recorderManager.release() } }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Detail Setoran Tahsin",
                subtitle = uiState.detail?.surahName ?: "Memuat...",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        val detail = uiState.detail

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                uiState.isLoadingDetail && detail == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Emerald600)
                }
                uiState.detailErrorMessage != null && detail == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    SulaoneErrorBanner(
                        message = uiState.detailErrorMessage ?: "Gagal memuat detail setoran",
                        onRetry = { viewModel.loadDetail(submissionId) },
                        modifier = Modifier.padding(24.dp)
                    )
                }
                detail != null -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SulaoneCard(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(detail.surahName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                if (detail.startAyah != null) {
                                    Text(
                                        "Ayat ${detail.startAyah}${detail.endAyah?.let { "-$it" } ?: ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isTeacherMode && detail.studentName != null) {
                                    Text(
                                        "Siswa: ${detail.studentName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            SulaoneBadge(
                                text = if (detail.status == "reviewed") "Selesai" else "Menunggu",
                                containerColor = if (detail.status == "reviewed") Emerald100 else Gold100,
                                contentColor = if (detail.status == "reviewed") Emerald800 else Gold800
                            )
                        }
                    }

                    TahsinAudioPlayer(
                        recorderManager = recorderManager,
                        audioUrl = detail.audioUrl,
                        durationSeconds = detail.durationSeconds,
                        annotations = detail.annotations.orEmpty(),
                        isTeacherMode = isTeacherMode,
                        onRequestAnnotate = { timestamp ->
                            pendingAnnotateTimestamp = timestamp
                            noteInput = ""
                            categoryInput = ""
                        }
                    )

                    if (isTeacherMode) {
                        Text(
                            "Ketuk bagian mana pun pada gelombang suara untuk menyematkan catatan tajwid di detik tersebut.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        uiState.annotateErrorMessage?.let {
                            SulaoneErrorBanner(message = it, modifier = Modifier.fillMaxWidth())
                        }

                        if (detail.status != "reviewed") {
                            SulaoneButton(
                                text = "Tandai Evaluasi Selesai",
                                onClick = { viewModel.markReviewed(submissionId) },
                                isLoading = uiState.isMarkingReviewed,
                                enabled = !uiState.isMarkingReviewed
                            )
                        }
                    }

                    val annotations = detail.annotations
                    Text(
                        "Catatan Tajwid (${annotations?.size ?: 0})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (annotations.isNullOrEmpty()) {
                        Text(
                            "Belum ada catatan tajwid pada rekaman ini.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        annotations.forEach { annotation ->
                            SulaoneCard(modifier = Modifier.fillMaxWidth(), elevation = 1.dp) {
                                val ts = annotation.timestampSeconds
                                Text(
                                    "${String.format(Locale.US, "%02d:%02d", ts / 60, ts % 60)}" +
                                        (annotation.tajwidCategory?.let { " · $it" } ?: ""),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Gold800
                                )
                                Text(annotation.note, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }

    val timestamp = pendingAnnotateTimestamp
    if (timestamp != null) {
        AlertDialog(
            onDismissRequest = { if (!uiState.isAnnotating) pendingAnnotateTimestamp = null },
            title = {
                Text("Catatan di ${String.format(Locale.US, "%02d:%02d", timestamp / 60, timestamp % 60)}", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        label = { Text("Catatan tajwid") },
                        placeholder = { Text("Contoh: Ghunnah kurang panjang") },
                        minLines = 2,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                    )
                    OutlinedTextField(
                        value = categoryInput,
                        onValueChange = { categoryInput = it },
                        label = { Text("Kategori (opsional)") },
                        placeholder = { Text("makhraj, ghunnah, mad, waqaf...") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.annotate(
                            submissionId,
                            timestamp,
                            noteInput.trim(),
                            categoryInput.trim().ifBlank { null }
                        )
                        pendingAnnotateTimestamp = null
                    },
                    enabled = noteInput.isNotBlank() && !uiState.isAnnotating,
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingAnnotateTimestamp = null }) { Text("Batal") }
            }
        )
    }
}
