package com.sultanagung1.sista.ui.achievement

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import com.sultanagung1.sista.core.motion.springPressable
import com.sultanagung1.sista.core.motion.sulaoneSharedBounds

@Composable
fun AchievementUploadScreen(
    viewModel: AchievementViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptics = rememberHapticFeedbackHelper()

    var showUploadModal by remember { mutableStateOf(false) }
    var titleInput by remember { mutableStateOf("") }
    var fieldInput by remember { mutableStateOf("Matematika & Sains") }
    var levelInput by remember { mutableStateOf("PROVINSI") }
    var organizerInput by remember { mutableStateOf("") }

    val tabs = listOf("Portofolio Kejuaraan", "E-Sertifikat Digital", "CV Akademik (SNBP)")

    val heroGradient = remember {
        Brush.linearGradient(
            colors = listOf(Gold800, Gold600, Gold400)
        )
    }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Portofolio Prestasi & e-Sertifikat",
                subtitle = "Rekam Jejak Keunggulan Akademik & Non-Akademik",
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            if (uiState.selectedTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = {
                        haptics.tapHeavy()
                        showUploadModal = true
                    },
                    containerColor = Gold500,
                    contentColor = Slate950,
                    shape = RoundedCornerShape(16.dp),
                    icon = { Icon(Icons.Default.CloudUpload, contentDescription = null) },
                    text = { Text("Unggah Prestasi Baru", fontWeight = FontWeight.Bold) }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // Tab Switcher
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tabs.indices.toList()) { index ->
                    val isSelected = uiState.selectedTab == index
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) Gold500 else MaterialTheme.colorScheme.surface)
                            .border(
                                1.dp,
                                if (isSelected) Gold400 else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                RoundedCornerShape(14.dp)
                            )
                            .springPressable {
                                haptics.tapLight()
                                viewModel.selectTab(index)
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tabs[index],
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium),
                            color = if (isSelected) Slate950 else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (uiState.selectedTab) {
                    0 -> {
                        // Hero Summary Card
                        item {
                            ModernBentoCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(22.dp),
                                backgroundColor = Color.Transparent,
                                glowColor = GoldGlow
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(heroGradient)
                                        .padding(20.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "TOTAL PRESTASI TERVALIDASI",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Slate950
                                            )
                                            Text(
                                                text = "${uiState.achievements.size} Kejuaraan",
                                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                                color = Slate950
                                            )
                                            Text(
                                                text = "Tervalidasi Waka Kesiswaan untuk Jalur SNBP & Beasiswa",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Slate900
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.3f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Slate950, modifier = Modifier.size(34.dp))
                                        }
                                    }
                                }
                            }
                        }

                        items(uiState.achievements) { ach ->
                            val isValidated = ach.verificationStatus == "VALIDATED"
                            ModernBentoCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .sulaoneSharedBounds(key = "achievement_card_${ach.id}"),
                                shape = RoundedCornerShape(18.dp),
                                backgroundColor = MaterialTheme.colorScheme.surface,
                                borderColor = if (isValidated) Gold400 else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Gold100
                                        ) {
                                            Text(
                                                text = "Tingkat ${ach.level}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Gold900,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                        LiveStatusChip(
                                            text = if (isValidated) "✓ Disahkan Sekolah" else "⏳ Menunggu Verifikasi",
                                            color = if (isValidated) Emerald700 else AccentAmber
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = ach.title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Penyelenggara: ${ach.organizer} • Tanggal: ${ach.date}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Slate100
                                    ) {
                                        Text(
                                            text = "🏅 Bidang: ${ach.field} (+${ach.pointsEarned} Poin Portofolio SNBP)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Slate700,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // E-Sertifikat Digital Tab
                        item {
                            Text(
                                text = "Koleksi Sertifikat Digital Berbasis Kriptografis",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        items(uiState.certificates) { cert ->
                            ModernBentoCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .sulaoneSharedBounds(key = "cert_card_${cert.id}"),
                                shape = RoundedCornerShape(18.dp),
                                backgroundColor = MaterialTheme.colorScheme.surface,
                                borderColor = Emerald300
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = cert.certificateNumber,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Emerald700
                                        )
                                        Text(
                                            text = cert.issuedDate,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Slate500
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = cert.title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Peran: ${cert.role}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Slate100
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Verified, contentDescription = null, tint = Emerald700, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Blockchain Hash: ${cert.blockchainHash.take(18)}...",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = Slate600
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // Preview CV Akademik SNBP
                        item {
                            val cv = uiState.cvSummary
                            ModernBentoCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                backgroundColor = MaterialTheme.colorScheme.surface,
                                borderColor = Emerald300,
                                glowColor = EmeraldGlow
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(
                                        text = "Curriculum Vitae Akademik Siswa",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = Emerald900
                                    )
                                    Text(
                                        text = "Format Standar Jalur Undangan SNBP & Beasiswa Unggulan",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate600
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    if (cv != null) {
                                        CvRow("Nama Lengkap", cv.studentName)
                                        CvRow("NISN Siswa", cv.nisn)
                                        CvRow("Rata-Rata Rapor Fase F", "${cv.gpaAverage} (Predikat A)")
                                        CvRow("Total Sertifikat Kejuaraan", "${cv.totalAchievements} Prestasi")
                                        CvRow("Poin Kedisiplinan & Kebaikan", "+${cv.totalRewardPoints} Poin")
                                        CvRow("Ekstrakurikuler Aktif", cv.extracurriculars.joinToString(", "))
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    SulaoneButton(
                                        text = "Export CV Akademik (PDF Resmi)",
                                        onClick = {
                                            haptics.success()
                                        },
                                        icon = Icons.Default.PictureAsPdf
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Modal Upload Prestasi
    if (showUploadModal) {
        Dialog(onDismissRequest = { showUploadModal = false }) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Unggah Bukti Prestasi Siswa",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Data akan diverifikasi langsung oleh Waka Kesiswaan",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Nama Lomba / Kejuaraan") },
                        placeholder = { Text("Contoh: Juara 1 OSN Matematika") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = organizerInput,
                        onValueChange = { organizerInput = it },
                        label = { Text("Lembaga Penyelenggara") },
                        placeholder = { Text("Contoh: BPTI Kemendikbudristek") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Certificate Image Attachment Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Slate100)
                            .border(1.dp, Slate300, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Emerald700, modifier = Modifier.size(30.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Lampirkan Foto Piagam / Sertifikat (Max 5MB)", style = MaterialTheme.typography.labelSmall, color = Slate600)
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showUploadModal = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Batal")
                        }

                        Button(
                            onClick = {
                                if (titleInput.isNotBlank()) {
                                    haptics.success()
                                    viewModel.uploadAchievement(titleInput, fieldInput, levelInput, organizerInput, "2026-08-26")
                                    showUploadModal = false
                                }
                            },
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                        ) {
                            Text("Kirim Validasi", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CvRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Slate600)
        Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Slate900)
    }
}
