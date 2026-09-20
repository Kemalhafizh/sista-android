package com.sultanagung1.sista.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*

@Composable
fun TeachingJournalScreen(
    scheduleId: String,
    className: String,
    subjectName: String,
    viewModel: TeacherViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val windowWidthClass = LocalWindowWidthSizeClass.current

    var topic by remember { mutableStateOf("") }
    var competencyCode by remember { mutableStateOf("TP-3.4") }
    var notes by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.journalSavedSuccess) {
        if (uiState.journalSavedSuccess) {
            showSuccessDialog = true
        }
    }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Jurnal Mengajar KBM",
                subtitle = "$className • $subjectName",
                onNavigateBack = onNavigateBack
            )
        },
        bottomBar = {
            if (windowWidthClass == WindowWidthSizeClass.Compact) {
                Surface(
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        SulaoneButton(
                            text = "Simpan Jurnal KBM",
                            onClick = {
                                if (topic.isNotBlank()) {
                                    viewModel.storeTeachingJournal(
                                        scheduleId = scheduleId,
                                        className = className,
                                        subjectName = subjectName,
                                        topic = topic,
                                        competencyCode = competencyCode,
                                        notes = notes.ifEmpty { "KBM terlaksana dengan tertib dan lancar sesuai modul ajar." }
                                    )
                                }
                            },
                            isLoading = uiState.isLoading,
                            enabled = topic.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        if (windowWidthClass == WindowWidthSizeClass.Compact) {
            // Mode Smartphone: Single Scrollable Column
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    SessionInfoCard(className = className, subjectName = subjectName)
                }
                item {
                    JournalFormFields(
                        topic = topic,
                        onTopicChange = { topic = it },
                        competencyCode = competencyCode,
                        onCompetencyCodeChange = { competencyCode = it },
                        notes = notes,
                        onNotesChange = { notes = it }
                    )
                }
            }
        } else {
            // Mode Tablet & Foldable: Dual-Pane Layout (FASE 55.1)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
            ) {
                // Pane Kiri (Informasi Sesi & Jadwal Terjadwal)
                Box(
                    modifier = Modifier
                        .width(360.dp)
                        .fillMaxHeight()
                        .padding(16.dp)
                ) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        item {
                            SessionInfoCard(className = className, subjectName = subjectName)
                        }
                        item {
                            SulaoneCard {
                                Column {
                                    Text("Status Kurikulum Merdeka", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("• Tingkat: Fase F (Kelas XI - XII)", style = MaterialTheme.typography.bodySmall, color = Slate700)
                                    Text("• Alokasi: 2 Jam Pelajaran (90 Menit)", style = MaterialTheme.typography.bodySmall, color = Slate700)
                                    Text("• Validasi: Buku Induk Digital YBWSA", style = MaterialTheme.typography.bodySmall, color = Slate700)
                                }
                            }
                        }
                    }
                }

                VerticalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    thickness = 1.dp
                )

                // Pane Kanan (Formulir Pengisian Jurnal)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(24.dp)
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text("Formulir Entri Jurnal Mengajar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Isi materi pokok dan capaian pembelajaran yang telah diajarkan pada sesi ini.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        item {
                            JournalFormFields(
                                topic = topic,
                                onTopicChange = { topic = it },
                                competencyCode = competencyCode,
                                onCompetencyCodeChange = { competencyCode = it },
                                notes = notes,
                                onNotesChange = { notes = it }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            SulaoneButton(
                                text = "Simpan Jurnal KBM",
                                onClick = {
                                    if (topic.isNotBlank()) {
                                        viewModel.storeTeachingJournal(
                                            scheduleId = scheduleId,
                                            className = className,
                                            subjectName = subjectName,
                                            topic = topic,
                                            competencyCode = competencyCode,
                                            notes = notes.ifEmpty { "KBM terlaksana dengan tertib dan lancar sesuai modul ajar." }
                                        )
                                    }
                                },
                                isLoading = uiState.isLoading,
                                enabled = topic.isNotBlank(),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                viewModel.resetFlags()
                onNavigateBack()
            },
            icon = {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Emerald600, modifier = Modifier.size(48.dp))
            },
            title = { Text(text = "Jurnal KBM Tersimpan", fontWeight = FontWeight.Bold) },
            text = { Text(text = "Jurnal mengajar kelas $className untuk materi '$topic' telah berhasil direkam ke buku induk kurikulum.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        viewModel.resetFlags()
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                ) {
                    Text(text = "Selesai")
                }
            }
        )
    }
}

@Composable
private fun SessionInfoCard(className: String, subjectName: String) {
    SulaoneCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Informasi Sesi KBM",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "• Kelas: $className", style = MaterialTheme.typography.bodyMedium, color = Slate700)
            Text(text = "• Mata Pelajaran: $subjectName", style = MaterialTheme.typography.bodyMedium, color = Slate700)
            Text(text = "• Tanggal: 25 Agustus 2026", style = MaterialTheme.typography.bodyMedium, color = Slate700)
            Text(text = "• Kurikulum: Merdeka (Fase F)", style = MaterialTheme.typography.bodyMedium, color = Slate700)
        }
    }
}

@Composable
private fun JournalFormFields(
    topic: String,
    onTopicChange: (String) -> Unit,
    competencyCode: String,
    onCompetencyCodeChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column {
            Text(
                text = "Materi Pokok / Topik Pembelajaran *",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = topic,
                onValueChange = onTopicChange,
                placeholder = { Text("Contoh: Kalkulus Integral & Luas Daerah Tertutup") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Emerald700,
                    focusedLabelColor = Emerald700
                )
            )
        }

        Column {
            Text(
                text = "Kode Capaian Pembelajaran (TP) *",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = competencyCode,
                onValueChange = onCompetencyCodeChange,
                placeholder = { Text("Contoh: TP-3.4 atau KKTP-2.1") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Emerald700,
                    focusedLabelColor = Emerald700
                )
            )
        }

        Column {
            Text(
                text = "Catatan Kejadian / Refleksi Mengajar",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                placeholder = { Text("Catat progres pemahaman siswa, keaktifan diskusi, atau kendala fasilitas...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Emerald700,
                    focusedLabelColor = Emerald700
                )
            )
        }
    }
}
