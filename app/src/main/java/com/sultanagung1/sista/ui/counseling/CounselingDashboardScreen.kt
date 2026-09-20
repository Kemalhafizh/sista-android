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
import com.sultanagung1.sista.data.model.CounselingAtRiskStudentItem
import androidx.compose.material.icons.Icons
import com.sultanagung1.sista.data.model.CounselingSessionItem

@Composable
fun CounselingDashboardScreen(
    viewModel: CounselingViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToNewSession: (Long?) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val stats = uiState.dashboardData?.statistics

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Bimbingan Konseling & At-Risk",
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNavigateToNewSession(null) },
                containerColor = Emerald700,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Catat Sesi BK", fontWeight = FontWeight.Bold) }
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
                // Banner BK
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
                                color = Gold500.copy(alpha = 0.2f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Psychology,
                                        contentDescription = null,
                                        tint = Gold400,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Pusat Layanan Konseling & Bimbingan",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                Text(
                                    "Early warning system santri & pendampingan intensif",
                                    fontSize = 12.sp,
                                    color = Emerald100
                                )
                            }
                        }
                    }
                }
            }

            // Stats Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CounselingStatCard(
                        title = "Sesi Bulan Ini",
                        value = "${stats?.totalThisMonth ?: 0}",
                        icon = Icons.Default.EventNote,
                        color = Emerald700,
                        modifier = Modifier.weight(1f)
                    )
                    CounselingStatCard(
                        title = "Tuntas",
                        value = "${stats?.completedThisMonth ?: 0}",
                        icon = Icons.Default.CheckCircle,
                        color = AccentGreen,
                        modifier = Modifier.weight(1f)
                    )
                    CounselingStatCard(
                        title = "Permintaan",
                        value = "${stats?.pendingRequests ?: 0}",
                        icon = Icons.Default.HourglassTop,
                        color = AccentAmber,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // At-Risk Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Radar Deteksi Dini Siswa (At-Risk)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AccentRose.copy(alpha = 0.12f)
                    ) {
                        Text(
                            "${uiState.atRiskStudents.size} Perlu Perhatian",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentRose,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            items(uiState.atRiskStudents) { student ->
                AtRiskStudentCard(
                    student = student,
                    onScheduleClick = { onNavigateToNewSession(student.studentId) }
                )
            }

            // Today's Sessions Section
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Jadwal Sesi Konseling Hari Ini",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            val todaySessions = uiState.dashboardData?.todaySessions ?: emptyList()
            if (todaySessions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Tidak ada jadwal sesi konseling hari ini.", fontSize = 13.sp, color = Slate400)
                        }
                    }
                }
            } else {
                items(todaySessions) { session ->
                    CounselingSessionCard(session = session)
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Composable
fun CounselingStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(title, fontSize = 11.sp, color = Slate500, maxLines = 1)
        }
    }
}

@Composable
fun AtRiskStudentCard(
    student: CounselingAtRiskStudentItem,
    onScheduleClick: () -> Unit
) {
    val isHighRisk = student.riskLevel.lowercase() == "tinggi"

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
                Column {
                    Text(student.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("${student.className} • NISN: ${student.nisn}", fontSize = 11.sp, color = Slate400)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isHighRisk) AccentRose.copy(alpha = 0.15f) else AccentAmber.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isHighRisk) "RISIKO TINGGI" else "RISIKO SEDANG",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isHighRisk) AccentRose else AccentAmber,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = if (isHighRisk) AccentRose else AccentAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(student.reason, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Skor Kedisiplinan: ${student.disciplineScore}/100",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (student.disciplineScore < 70) AccentRose else Emerald700
                )
                FilledTonalButton(
                    onClick = onScheduleClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Emerald50,
                        contentColor = Emerald800
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Jadwalkan Konseling", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CounselingSessionCard(session: CounselingSessionItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Emerald50,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = Emerald700, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(session.studentName ?: "Konsultasi Siswa", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(session.reason ?: "Bimbingan berkala", fontSize = 12.sp, color = Slate500, maxLines = 1)
                Spacer(modifier = Modifier.height(2.dp))
                Text(session.scheduledAt ?: "Hari ini", fontSize = 11.sp, color = Emerald700, fontWeight = FontWeight.Medium)
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Emerald100
            ) {
                Text(
                    session.category.replaceFirstChar { it.uppercase() },
                    fontSize = 11.sp,
                    color = Emerald800,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
