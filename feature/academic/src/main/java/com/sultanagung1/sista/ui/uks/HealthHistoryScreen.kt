package com.sultanagung1.sista.ui.uks

import androidx.compose.material.icons.Icons

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.data.model.HealthRecord

@Composable
fun HealthHistoryScreen(
    studentId: String? = null,
    viewModel: UksViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val screening = uiState.screeningSummary

    // This screen gets its own fresh ViewModel instance (Hilt scopes it to
    // this destination), so the studentId nav arg has to be re-applied here
    // rather than relying on UksVisitScreen's already-loaded state.
    androidx.compose.runtime.LaunchedEffect(studentId) {
        if (!studentId.isNullOrBlank()) {
            viewModel.loadData(studentId)
        }
    }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Riwayat Kesehatan & Skrining",
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, "Fitur unduh PDF rekam medis belum tersedia.", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Download, contentDescription = "Unduh PDF", tint = Emerald700)
                    }
                }
            )
        }
    ) { paddingValues ->
        if (studentId.isNullOrBlank()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Pilih siswa terlebih dahulu di formulir Catat Pasien UKS untuk melihat riwayat kesehatannya.",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = Slate500
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Profile & Screening Card
                if (screening != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Emerald800)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        listOf(Emerald900, Emerald700)
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(screening.studentName ?: "-", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                        Text("NISN: ${screening.nisn ?: "-"}", color = Gold300, fontSize = 12.sp)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Gold500.copy(alpha = 0.25f)
                                    ) {
                                        Text(
                                            "Gol. Darah: ${screening.bloodType ?: "Belum tercatat"}",
                                            color = Gold300,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Metrics Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    MetricCol(label = "Tinggi", value = screening.heightCm?.let { "$it cm" } ?: "Belum diperiksa")
                                    MetricCol(label = "Berat", value = screening.weightKg?.let { "$it kg" } ?: "Belum diperiksa")
                                    MetricCol(label = "BMI", value = screening.bmi?.let { "$it (${screening.bmiCategory})" } ?: "Belum diperiksa")
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Visus Mata: R ${screening.visionRight ?: "-"} / L ${screening.visionLeft ?: "-"}", color = Slate200, fontSize = 11.sp)
                                    Text("Gigi: ${screening.dentalHealth ?: "Belum diperiksa"}", color = Slate200, fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    if (screening.screener != null) "Pemeriksa: ${screening.screener} (${screening.lastScreenedAt})" else "Belum ada riwayat skrining kesehatan",
                                    color = Gold200,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            // Allergy Alert Card
            if (screening != null && screening.allergies.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = AccentRose.copy(alpha = 0.08f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentRose.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WarningAmber, contentDescription = null, tint = AccentRose, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Riwayat Alergi Khusus Siswa", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AccentRose)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                screening.allergies.forEach { allergy ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = AccentRose.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            allergy,
                                            fontSize = 11.sp,
                                            color = AccentRose,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Visits Timeline Header
            item {
                Text("Rekam Medis & Riwayat Penanganan UKS", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            items(uiState.healthHistory) { record ->
                HealthRecordCard(record = record)
            }

            item {
                Button(
                    onClick = {
                        Toast.makeText(context, "Fitur unduh Surat Keterangan Sakit belum tersedia.", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                ) {
                    Icon(Icons.Default.Article, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unduh Surat Izin Sakit UKS (PDF)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun MetricCol(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(label, color = Slate300, fontSize = 11.sp)
    }
}

@Composable
fun HealthRecordCard(record: HealthRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EventNote, contentDescription = null, tint = Emerald700, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(record.date, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Emerald800)
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AccentBlue.copy(alpha = 0.15f)
                ) {
                    Text(
                        record.diagnosis ?: "-",
                        color = AccentBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text("Keluhan: ${record.complaint}", fontSize = 13.sp, color = Slate800, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Tindakan: ${record.action}", fontSize = 12.sp, color = Slate700)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Obat yang Diberikan: ${record.medicines}", fontSize = 12.sp, color = Emerald700, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MedicalInformation, contentDescription = null, tint = Slate400, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Petugas / Dokter: ${record.officer}", fontSize = 11.sp, color = Slate500)
            }
        }
    }
}
