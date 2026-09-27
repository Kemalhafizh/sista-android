package com.sultanagung1.sista.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*
import androidx.compose.material.icons.Icons
import com.sultanagung1.sista.data.model.JournalScheduleItem

@Composable
fun TeachingJournalMobileScreen(
    viewModel: JournalMobileViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToForm: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val tabs = listOf("Hari Ini", "Minggu Ini", "Bulan Ini")

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Jurnal Mengajar Guru",
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { viewModel.loadSchedules() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Muat Ulang", tint = Emerald700)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Emerald700
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.selectedTab == index,
                        onClick = { viewModel.setSelectedTab(index) },
                        text = {
                            Text(
                                title,
                                fontWeight = if (uiState.selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                // Monthly Compliance Card
                item {
                    val compliance = uiState.compliance
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
                                        Text("Kepatuhan Jurnal KBM Guru", color = Gold300, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text("${compliance.filledCount} dari ${compliance.totalScheduled} Sesi Terisi", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Gold500.copy(alpha = 0.25f)
                                    ) {
                                        Text(
                                            "${compliance.compliancePercentage}%",
                                            color = Gold300,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                LinearProgressIndicator(
                                    progress = { compliance.compliancePercentage / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = Gold400,
                                    trackColor = Emerald900
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    "Terintegrasi otomatis dengan E-Kinerja Guru & Supervisi Kurikulum Merdeka",
                                    color = Slate200,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Warning Badge if uncompleted
                if (uiState.compliance.unfilledDaysWarning) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = AccentAmber.copy(alpha = 0.1f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentAmber.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    "Ada sesi mengajar yang belum diisi jurnalnya. Mohon lengkapi sebelum pukul 15:00 WIB agar tersinkron ke laporan KBM harian.",
                                    fontSize = 12.sp,
                                    color = Slate800,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                item {
                    Text("Jadwal & Status Pengisian Jurnal", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                if (uiState.isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Emerald700)
                        }
                    }
                } else if (uiState.errorMessage != null) {
                    item {
                        SulaoneErrorBanner(
                            message = uiState.errorMessage ?: "Gagal memuat jadwal & jurnal mengajar.",
                            onRetry = { viewModel.loadSchedules() }
                        )
                    }
                } else if (uiState.currentTabSchedules.isEmpty()) {
                    item {
                        SulaoneEmptyState(
                            title = if (uiState.selectedTab == 0) "Tidak Ada Jadwal Hari Ini" else "Belum Ada Jurnal",
                            description = if (uiState.selectedTab == 0) "Anda tidak memiliki jadwal mengajar terjadwal untuk hari ini." else "Belum ada jurnal mengajar yang tercatat pada periode ini.",
                            icon = Icons.Default.MenuBook
                        )
                    }
                } else {
                    items(uiState.currentTabSchedules) { schedule ->
                        JournalScheduleCard(
                            schedule = schedule,
                            onFillClick = { onNavigateToForm(schedule.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JournalScheduleCard(
    schedule: JournalScheduleItem,
    onFillClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (schedule.isFilled) expanded = !expanded else onFillClick()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        schedule.subject,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Kelas ${schedule.className} • ${schedule.timeSlot}",
                        fontSize = 12.sp,
                        color = Slate600
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (schedule.isFilled) Emerald100 else AccentAmber.copy(alpha = 0.15f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            if (schedule.isFilled) Icons.Default.CheckCircle else Icons.Default.Pending,
                            contentDescription = null,
                            tint = if (schedule.isFilled) Emerald700 else AccentAmber,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (schedule.isFilled) "Terisi" else "Belum Terisi",
                            color = if (schedule.isFilled) Emerald800 else AccentAmber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            if (schedule.isFilled) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Slate200)
                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = Emerald700, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Materi Pokok:", fontSize = 11.sp, color = Slate500)
                        Text(schedule.topic ?: "-", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Slate800)
                    }
                }

                if (expanded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Metode & Media:", fontSize = 11.sp, color = Slate500)
                            Text("${schedule.method ?: "-"} • Media: ${schedule.media ?: "-"}", fontSize = 12.sp, color = Slate700)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.People, contentDescription = null, tint = Emerald600, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Kehadiran Siswa:", fontSize = 11.sp, color = Slate500)
                            Text("Hadir: ${schedule.attendancePresent ?: "-"} siswa | Absen: ${schedule.attendanceAbsent ?: "-"} siswa", fontSize = 12.sp, color = Slate700)
                        }
                    }

                    if (!schedule.notes.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Catatan: ${schedule.notes}", fontSize = 11.sp, color = Slate600)
                    }

                    if (!schedule.followUp.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Tindak Lanjut: ${schedule.followUp}", fontSize = 11.sp, color = Emerald800, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (schedule.isEditable) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { onFillClick() }) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit Jurnal", fontSize = 12.sp)
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onFillClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                ) {
                    Icon(Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Isi Jurnal Mengajar Sekarang", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
