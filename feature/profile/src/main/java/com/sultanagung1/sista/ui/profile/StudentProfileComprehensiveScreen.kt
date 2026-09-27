package com.sultanagung1.sista.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.sultanagung1.sista.core.motion.sulaoneSharedElement
import androidx.compose.material.icons.Icons
import com.sultanagung1.sista.data.model.*

@Composable
fun StudentProfileComprehensiveScreen(
    studentId: Long? = null,
    viewModel: StudentProfileViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val tabs = listOf("Akademik", "Ibadah & Tahfidz", "Disiplin", "Prestasi", "Kesehatan")

    LaunchedEffect(studentId) {
        viewModel.loadProfile(studentId)
    }

    val profile = uiState.profile ?: StudentProfile360Data()

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Profil Siswa 360°",
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
                // Profile Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Emerald800)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Emerald900, Emerald700)
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Gold500.copy(alpha = 0.25f),
                                modifier = Modifier
                                    .size(64.dp)
                                    .sulaoneSharedElement(key = "student_avatar")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Gold400,
                                        modifier = Modifier.size(38.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    profile.biodata.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "${profile.biodata.className ?: "-"} • NISN ${profile.biodata.nisn ?: "-"}",
                                    fontSize = 12.sp,
                                    color = Emerald100
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Gold600.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        "Kategori Siswa: ${profile.disciplineSummary.category}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Gold400,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Scrollable Tab Row
            item {
                ScrollableTabRow(
                    selectedTabIndex = uiState.selectedTab,
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    contentColor = Emerald700,
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = uiState.selectedTab == index,
                            onClick = { viewModel.selectTab(index) },
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
            }

            when (uiState.selectedTab) {
                0 -> item { AcademicSummarySection(profile = profile) }
                1 -> item { IbadahSummarySection(profile = profile) }
                2 -> item { DisciplineSummarySection(profile = profile) }
                3 -> item { AchievementsAndEkskulSection(profile = profile) }
                4 -> item { HealthSummarySection(profile = profile) }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun AcademicSummarySection(profile: StudentProfile360Data) {
    val acad = profile.academicSummary
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProfileInfoCard(
                title = "Rata-Rata Nilai",
                value = "${acad.averageScore}",
                subtitle = "Skala 100",
                icon = Icons.Default.Grade,
                color = Emerald700,
                modifier = Modifier.weight(1f)
            )
            ProfileInfoCard(
                title = "Peringkat Kelas",
                value = "#${acad.rankInClass}",
                subtitle = "Dari ${acad.totalClassStudents} Siswa",
                icon = Icons.Default.EmojiEvents,
                color = Gold600,
                modifier = Modifier.weight(1f)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Analisis Minat & Mata Pelajaran", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ThumbUp, contentDescription = null, tint = Emerald700, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Paling Unggul: ", fontSize = 12.sp, color = Slate500)
                    Text(acad.strongestSubject ?: "Belum ada data", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Emerald800)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Perlu Ditingkatkan: ", fontSize = 12.sp, color = Slate500)
                    Text(acad.improvementNeeded ?: "Belum ada data", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentAmber)
                }
            }
        }
    }
}

@Composable
fun IbadahSummarySection(profile: StudentProfile360Data) {
    val ibd = profile.ibadahSummary
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                    Text("Capaian Tahfidz Al-Qur'an", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("${ibd.tahfidzJuzCompleted} / ${ibd.targetJuz} Juz", fontWeight = FontWeight.Bold, color = Emerald700)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { ibd.tahfidzProgressPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Emerald600,
                    trackColor = Emerald100
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Hafalan Saat Ini: ${ibd.currentSurah ?: "Belum ada setoran tercatat"}", fontSize = 12.sp, color = Slate500)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProfileInfoCard(
                title = "Sholat Jamaah",
                value = "${ibd.sholatJamaahPercent}%",
                subtitle = "Di Masjid Sekolah",
                icon = Icons.Default.Mosque,
                color = Emerald700,
                modifier = Modifier.weight(1f)
            )
            ProfileInfoCard(
                title = "Streak Mutaba'ah",
                value = "${ibd.mutabaahWeeklyStreak} Mgg",
                subtitle = "Konsistensi Harian",
                icon = Icons.Default.Whatshot,
                color = AccentAmber,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun DisciplineSummarySection(profile: StudentProfile360Data) {
    val dsp = profile.disciplineSummary
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProfileInfoCard(
                title = "Poin Kebaikan (+)",
                value = "+${dsp.totalPositivePoints}",
                subtitle = "Aktivitas Positif",
                icon = Icons.Default.Star,
                color = Emerald700,
                modifier = Modifier.weight(1f)
            )
            ProfileInfoCard(
                title = "Poin Pelanggaran (-)",
                value = "-${dsp.totalViolationPoints}",
                subtitle = "Pelanggaran Tatib",
                icon = Icons.Default.ErrorOutline,
                color = AccentRose,
                modifier = Modifier.weight(1f)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Status Kedisiplinan & Integritas", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Poin Bersih (Netto): ${dsp.netPoints} Poin", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text("Sanksi / Pembinaan Aktif: ${dsp.activeSanctions}", fontSize = 13.sp, color = if (dsp.activeSanctions == 0) Emerald700 else AccentRose)
            }
        }
    }
}

@Composable
fun AchievementsAndEkskulSection(profile: StudentProfile360Data) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Prestasi & Penghargaan", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        profile.achievementList.forEach { ach ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = Gold600, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(ach.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Tingkat ${ach.level} • ${ach.year ?: "-"}", fontSize = 11.sp, color = Slate500)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text("Ekstrakurikuler & Organisasi", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        profile.extracurricularList.forEach { eks ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Groups, contentDescription = null, tint = Emerald700, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(eks.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("${eks.role} • Bergabung ${eks.joinedYear}", fontSize = 11.sp, color = Slate500)
                    }
                }
            }
        }
    }
}

@Composable
fun HealthSummarySection(profile: StudentProfile360Data) {
    val hlt = profile.healthSummary
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProfileInfoCard(
                title = "Golongan Darah",
                value = hlt.bloodType ?: "-",
                subtitle = "Data UKS",
                icon = Icons.Default.Bloodtype,
                color = AccentRose,
                modifier = Modifier.weight(1f)
            )
            ProfileInfoCard(
                title = "Tinggi / Berat",
                value = if (hlt.heightCm != null && hlt.weightKg != null) "${hlt.heightCm}cm / ${hlt.weightKg}kg" else "Belum tercatat",
                subtitle = "BMI Ideal",
                icon = Icons.Default.AccessibilityNew,
                color = Emerald700,
                modifier = Modifier.weight(1f)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Rekam Medis & Kunjungan UKS", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Total Kunjungan UKS: ${hlt.totalUksVisits} Kali", fontSize = 12.sp)
                Text("Kunjungan Terakhir: ${hlt.lastVisitDate ?: "Belum pernah"}", fontSize = 12.sp, color = Slate500)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Catatan Alergi: ${hlt.allergies.takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "Tidak ada catatan alergi"}",
                    fontSize = 12.sp,
                    color = Slate500
                )
            }
        }
    }
}

@Composable
fun ProfileInfoCard(
    title: String,
    value: String,
    subtitle: String,
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
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Slate700)
            Text(subtitle, fontSize = 10.sp, color = Slate400)
        }
    }
}
