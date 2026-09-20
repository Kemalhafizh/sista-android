package com.sultanagung1.sista.ui.schoolops

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*
import androidx.compose.material.icons.Icons
import com.sultanagung1.sista.data.model.SchoolTeachingJournalItem

@Composable
fun TeachingJournalScreen(
    viewModel: SchoolOperationsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    var classNameInput by remember { mutableStateOf("XII MIPA 1") }
    var subjectNameInput by remember { mutableStateOf("Matematika Tingkat Lanjut") }
    var topicInput by remember { mutableStateOf("") }
    var dateInput by remember { mutableStateOf("2026-09-06") }
    var notesInput by remember { mutableStateOf("") }
    var presentInput by remember { mutableStateOf("35") }
    var absentInput by remember { mutableStateOf("1") }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Jurnal Mengajar Guru",
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Emerald700,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Isi Jurnal Baru", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Emerald800)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Emerald900, Emerald700)
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Gold500.copy(alpha = 0.25f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = Gold400,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Agenda & Jurnal Tatap Muka",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                Text(
                                    "Rekam materi, presensi kelas & kendala pembelajaran",
                                    fontSize = 12.sp,
                                    color = Emerald100
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text("Riwayat Jurnal Mengajar Saya", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            if (uiState.teachingJournals.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("Belum ada data jurnal mengajar yang diisi.", fontSize = 13.sp, color = Slate400)
                        }
                    }
                }
            } else {
                items(uiState.teachingJournals) { journal ->
                    TeachingJournalCard(journal = journal)
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Isi Jurnal Mengajar Kelas", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = classNameInput,
                        onValueChange = { classNameInput = it },
                        label = { Text("Kelas") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = subjectNameInput,
                        onValueChange = { subjectNameInput = it },
                        label = { Text("Mata Pelajaran") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = topicInput,
                        onValueChange = { topicInput = it },
                        label = { Text("Materi Pokok / Bahasan Hari Ini") },
                        placeholder = { Text("Contoh: Turunan Fungsi Trigonometri") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = presentInput,
                            onValueChange = { presentInput = it },
                            label = { Text("Hadir") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = absentInput,
                            onValueChange = { absentInput = it },
                            label = { Text("Tidak Hadir") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("Catatan / Kendala Kelas (Opsional)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pres = presentInput.toIntOrNull() ?: 35
                        val abs = absentInput.toIntOrNull() ?: 0
                        viewModel.storeJournal(
                            className = classNameInput,
                            subjectName = subjectNameInput,
                            topic = topicInput,
                            date = dateInput,
                            notes = notesInput,
                            present = pres,
                            absent = abs
                        ) {
                            showAddDialog = false
                            topicInput = ""
                            notesInput = ""
                        }
                    },
                    enabled = topicInput.isNotBlank() && !uiState.isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                ) {
                    Text("Simpan Jurnal", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun TeachingJournalCard(journal: SchoolTeachingJournalItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Emerald50
                ) {
                    Text(
                        "${journal.className} • ${journal.subjectName}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Emerald800,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(
                    journal.date,
                    fontSize = 11.sp,
                    color = Slate400
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                journal.topic,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (!journal.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    journal.notes,
                    fontSize = 12.sp,
                    color = Slate500,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Hadir: ${journal.attendancePresent}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Emerald700
                    )
                    Text(
                        "Absen: ${journal.attendanceAbsent}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (journal.attendanceAbsent > 0) AccentRose else Slate500
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Emerald50
                ) {
                    Text(
                        "TERVERIFIKASI",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Emerald700,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
