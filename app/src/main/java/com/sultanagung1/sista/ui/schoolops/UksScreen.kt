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
import com.sultanagung1.sista.data.model.HealthScreeningData
import androidx.compose.material.icons.Icons
import com.sultanagung1.sista.data.model.UksRecordVisitItem

@Composable
fun UksScreen(
    viewModel: SchoolOperationsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val screening = uiState.healthScreening

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "UKS & Buku Kesehatan",
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
                                        Icons.Default.HealthAndSafety,
                                        contentDescription = null,
                                        tint = Gold400,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Layanan Medis & UKS Digital",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                Text(
                                    "Rekam kesehatan santri & pemantauan berkala",
                                    fontSize = 12.sp,
                                    color = Emerald100
                                )
                            }
                        }
                    }
                }
            }

            // Health Screening Stats Cards
            if (screening != null) {
                item {
                    Text("Hasil Skrining Kesehatan Terakhir", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Gol. Darah", fontSize = 11.sp, color = Slate400)
                                Text(screening.bloodType, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AccentRose)
                                Text("BMI: ${screening.bmi} (${screening.bmiCategory})", fontSize = 10.sp, color = Slate500)
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Pemeriksaan Mata", fontSize = 11.sp, color = Slate400)
                                Text("Visus: ${screening.visionRight}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Emerald700)
                                Text("Gigi: ${screening.dentalHealth}", fontSize = 10.sp, color = Slate500)
                            }
                        }
                    }
                }
            }

            // UKS Visits Section
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Riwayat Kunjungan & Keluhan UKS", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            if (uiState.uksVisits.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("Tidak ada riwayat kunjungan ke ruang UKS.", fontSize = 13.sp, color = Slate400)
                        }
                    }
                }
            } else {
                items(uiState.uksVisits) { visit ->
                    UksVisitCard(visit = visit)
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun UksVisitCard(visit: UksRecordVisitItem) {
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
                Text(
                    text = visit.visitTime,
                    fontSize = 11.sp,
                    color = Slate400,
                    fontWeight = FontWeight.Medium
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Emerald50
                ) {
                    Text(
                        text = visit.action?.replace('_', ' ')?.uppercase() ?: "SELESAI",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Emerald700,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Keluhan: ${visit.complaints}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (!visit.diagnosis.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Diagnosis: ${visit.diagnosis}",
                    fontSize = 12.sp,
                    color = Slate700
                )
            }

            if (!visit.treatment.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Penanganan: ${visit.treatment}",
                    fontSize = 12.sp,
                    color = Slate500
                )
            }

            if (visit.medicines.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Medication, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Obat Diberikan: " + visit.medicines.joinToString(", ") { "${it.name} (${it.dose})" },
                        fontSize = 11.sp,
                        color = Slate700
                    )
                }
            }

            if (!visit.handlerName.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Pemeriksa: ${visit.handlerName}",
                    fontSize = 11.sp,
                    color = Emerald800,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
