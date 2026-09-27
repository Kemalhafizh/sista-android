package com.sultanagung1.sista.ui.counseling

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
import com.sultanagung1.sista.data.model.CounselingSessionItem

@Composable
fun StudentCounselingScreen(
    viewModel: CounselingViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showRequestDialog by remember { mutableStateOf(false) }

    var topicInput by remember { mutableStateOf("") }
    var preferredDateInput by remember { mutableStateOf("") }
    val categories = listOf("akademik", "pribadi", "sosial", "karir")
    var selectedCategory by remember { mutableStateOf("karir") }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "",
                onNavigateBack = onNavigateBack
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
                // Banner Konseling Aman & Terpercaya
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
                                    listOf(Emerald800, Emerald600)
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Gold500.copy(alpha = 0.2f),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.SupportAgent, contentDescription = null, tint = Gold400, modifier = Modifier.size(22.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Ruang Konseling & Curhat Aman", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                                    Text("Privat, menjunjung etika kerahasiaan & islami", fontSize = 11.sp, color = Emerald100)
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { showRequestDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Gold600, contentColor = Slate950)
                            ) {
                                Icon(Icons.Default.AddComment, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ajukan Janji Temu Konseling", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "Jadwal & Riwayat Konseling Saya",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (uiState.studentAppointments.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Belum ada riwayat permohonan konseling.", fontSize = 13.sp, color = Slate400)
                        }
                    }
                }
            } else {
                items(uiState.studentAppointments) { session ->
                    StudentSessionCard(session = session)
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showRequestDialog) {
        AlertDialog(
            onDismissRequest = { showRequestDialog = false },
            title = { Text("Ajukan Sesi Konseling BK", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Pilih Kategori Topik:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat.replaceFirstChar { it.uppercase() }, fontSize = 11.sp) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = topicInput,
                        onValueChange = { topicInput = it },
                        label = { Text("Topik / Hal yang ingin dikonsultasikan") },
                        placeholder = { Text("Contoh: Bingung memilih jurusan kuliah...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = preferredDateInput,
                        onValueChange = { preferredDateInput = it },
                        label = { Text("Waktu Pilihan (Opsional)") },
                        placeholder = { Text("Contoh: Kamis jam istirahat ke-2") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.requestAppointment(
                            topic = topicInput,
                            preferredDate = preferredDateInput.ifBlank { "Besok" },
                            category = selectedCategory
                        ) {
                            showRequestDialog = false
                            topicInput = ""
                            preferredDateInput = ""
                        }
                    },
                    enabled = topicInput.isNotBlank() && !uiState.isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                ) {
                    Text("Ajukan", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRequestDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun StudentSessionCard(session: CounselingSessionItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (session.status.lowercase()) {
                        "completed" -> Emerald50
                        "scheduled" -> Gold50
                        else -> Slate100
                    }
                ) {
                    Text(
                        text = when (session.status.lowercase()) {
                            "completed" -> "Selesai"
                            "scheduled" -> "Terjadwal"
                            else -> "Menunggu Konfirmasi"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (session.status.lowercase()) {
                            "completed" -> Emerald700
                            "scheduled" -> AccentAmber
                            else -> Slate600
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = session.scheduledAt ?: session.createdAt ?: "-",
                    fontSize = 11.sp,
                    color = Slate400
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = session.reason ?: "Topik Konsultasi",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (!session.sessionNotes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Emerald50,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            "Hasil Konseling:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Emerald800
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            session.sessionNotes,
                            fontSize = 12.sp,
                            color = Emerald900
                        )
                    }
                }
            }
        }
    }
}
