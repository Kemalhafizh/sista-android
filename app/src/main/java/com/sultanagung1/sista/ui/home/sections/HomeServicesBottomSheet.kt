package com.sultanagung1.sista.ui.home.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import com.sultanagung1.sista.core.motion.springPressable
import com.sultanagung1.sista.ui.navigation.Screen

data class ServiceEntry(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String,
    val iconTint: Color = Emerald700,
    val bgTint: Color = Emerald50
)

data class ServiceCategory(
    val categoryName: String,
    val services: List<ServiceEntry>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeServicesBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onNavigateRoute: ((String) -> Unit)?
) {
    if (!isVisible) return

    val haptics = rememberHapticFeedbackHelper()
    val isDark = MaterialTheme.colorScheme.surface.isDark()

    val categories = remember {
        listOf(
            ServiceCategory(
                categoryName = "Akademik & Ujian",
                services = listOf(
                    ServiceEntry("Jadwal KBM", "Kalender Pelajaran", Icons.Default.CalendarMonth, Screen.Schedule.route, AccentCyan, AccentCyan.copy(alpha = 0.12f)),
                    ServiceEntry("Ujian CBT", "Ujian Anti-Cheat", Icons.Default.Quiz, Screen.CbtList.route, AccentPurple, AccentPurple.copy(alpha = 0.12f)),
                    ServiceEntry("Rapor KKTP", "Capaian & Nilai", Icons.Default.AutoGraph, Screen.Grades.route, AccentGreen, AccentGreen.copy(alpha = 0.12f)),
                    ServiceEntry("E-Learning", "Tugas & Materi LMS", Icons.Default.School, Screen.ElearningClassList.route, Emerald700, Emerald50),
                    ServiceEntry("Bank Soal", "Latihan & Tryout", Icons.Default.MenuBook, Screen.QuestionBank.route, Gold600, Gold100)
                )
            ),
            ServiceCategory(
                categoryName = "Keuangan & Presensi",
                services = listOf(
                    ServiceEntry("Tagihan SPP", "BSI Virtual Account", Icons.Default.AccountBalanceWallet, Screen.Billing.route, AccentAmber, AccentAmber.copy(alpha = 0.12f)),
                    ServiceEntry("Presensi GPS", "Check-in Kampus", Icons.Default.LocationOn, Screen.GeofenceAttendance.route, Emerald700, Emerald50),
                    ServiceEntry("QR Dinamis", "TOTP Anti-Joki", Icons.Default.QrCodeScanner, Screen.DynamicQr.route, Gold600, Gold100),
                    ServiceEntry("Kuitansi PDF", "Arsip Pembayaran", Icons.Default.Receipt, Screen.DownloadHistory.route, Slate600, Slate100)
                )
            ),
            ServiceCategory(
                categoryName = "Kesiswaan & Pembinaan",
                services = listOf(
                    ServiceEntry("Buku Saku Poin", "Pelanggaran & SP", Icons.Default.Gavel, Screen.Discipline.route, AccentRose, AccentRose.copy(alpha = 0.12f)),
                    ServiceEntry("Simulasi UTBK", "Tryout IRT & PTN", Icons.Default.Psychology, Screen.UtbkTryout.route, AccentPurple, AccentPurple.copy(alpha = 0.12f)),
                    ServiceEntry("E-Pustaka Pintar", "Katalog Buku Digital", Icons.Default.LocalLibrary, Screen.LibraryCatalog.route, Emerald700, Emerald50),
                    ServiceEntry("Ekskul & OSIS", "Pendaftaran & Jadwal", Icons.Default.SportsSoccer, Screen.Extracurricular.route, AccentGreen, AccentGreen.copy(alpha = 0.12f)),
                    ServiceEntry("Portofolio Prestasi", "Upload Sertifikat", Icons.Default.EmojiEvents, Screen.AchievementUpload.route, Gold600, Gold100)
                )
            ),
            ServiceCategory(
                categoryName = "Bimbingan & Layanan",
                services = listOf(
                    ServiceEntry("Konseling BK", "Konsultasi Guru BK", Icons.Default.Chat, Screen.ConversationList.route, Emerald700, Emerald50),
                    ServiceEntry("Evaluasi Guru", "Kuesioner Anonim", Icons.Default.HowToVote, Screen.TeacherEvaluation.route, AccentCyan, AccentCyan.copy(alpha = 0.12f)),
                    ServiceEntry("UKS Digital", "Skrining Kesehatan", Icons.Default.LocalHospital, Screen.UksVisit.route, AccentRose, AccentRose.copy(alpha = 0.12f)),
                    ServiceEntry("Mode Hemat Kuota", "Optimasi RAM 2GB", Icons.Default.Speed, Screen.LiteModeSettings.route, Slate600, Slate100)
                )
            )
        )
    }

    SulaoneModalBottomSheet(
        isVisible = isVisible,
        onDismiss = onDismiss,
        title = "Semua Layanan SISTA",
        fullHeight = true
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Text(
                    text = "Akses terpadu seluruh fasilitas dan layanan SMA Islam Sultan Agung 1 Semarang",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            categories.forEach { category ->
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = category.categoryName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Grid 2 Columns for Services
                        category.services.chunked(2).forEach { pair ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                pair.forEach { service ->
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(14.dp))
                                            .springPressable {
                                                haptics.tapLight()
                                                onDismiss()
                                                onNavigateRoute?.invoke(service.route)
                                            },
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isDark) Slate900 else Slate50,
                                        border = androidx.compose.foundation.BorderStroke(
                                            0.5.dp,
                                            if (isDark) Slate800 else Slate200
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(if (isDark) service.iconTint.copy(alpha = 0.2f) else service.bgTint),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = service.icon,
                                                    contentDescription = null,
                                                    tint = service.iconTint,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = service.title,
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = service.subtitle,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                                if (pair.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
