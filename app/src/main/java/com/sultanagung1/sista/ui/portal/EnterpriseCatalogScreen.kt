package com.sultanagung1.sista.ui.portal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.data.model.EnterpriseModuleItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterpriseCatalogScreen(
    userRole: String? = "student",
    onNavigateBack: () -> Unit,
    onNavigateRoute: (String) -> Unit
) {
    val isUserExecutive = userRole?.contains("admin", true) == true ||
            userRole?.contains("superadmin", true) == true ||
            userRole?.contains("kepsek", true) == true ||
            userRole?.contains("principal", true) == true
    val isUserTeacherOrAbove = isUserExecutive ||
            userRole?.contains("teacher", true) == true ||
            userRole?.contains("guru", true) == true
    val isUserParentOrAbove = isUserExecutive ||
            userRole?.contains("parent", true) == true ||
            userRole?.contains("ortu", true) == true

    val categories = remember(userRole) {
        val list = mutableListOf("Semua")
        if (isUserExecutive) {
            list.add("Rahasia Pimpinan")
        }
        if (isUserTeacherOrAbove) {
            list.add("Khusus Guru")
        }
        if (isUserParentOrAbove) {
            list.add("Wali Murid")
        }
        list.addAll(
            listOf(
                "Khusus Siswa",
                "Akademik & LMS",
                "Presensi & IoT",
                "Kecerdasan Buatan (AI Edukasi)",
                "Kesiswaan & Ibadah",
                "Keuangan & Tata Usaha"
            )
        )
        list
    }
    var selectedCategory by remember { mutableStateOf("Semua") }
    var searchQuery by remember { mutableStateOf("") }
    val favoriteSet = remember { mutableStateListOf("m4", "m5", "m9", "m18", "m22") }

    val modules = remember {
        listOf(
            EnterpriseModuleItem("m1", 1, "Manajemen Siswa & Induk", "Database induk siswa NISN", "Akademik & LMS", "grades", "School"),
            EnterpriseModuleItem("m2", 2, "Kurikulum Merdeka & RPP", "Struktur mata pelajaran & silabus", "Akademik & LMS", "schedule", "MenuBook"),
            EnterpriseModuleItem("m3", 3, "Jadwal Pelajaran & Kalender", "Penjadwalan mingguan & semester", "Akademik & LMS", "schedule", "CalendarMonth"),
            EnterpriseModuleItem("m4", 4, "Presensi GPS Geofence", "Presensi lokasi radius 250m", "Presensi & IoT", "geofence_attendance", "LocationOn"),
            EnterpriseModuleItem("m5", 5, "QR Presensi Dinamis (TOTP)", "Kode berputar anti-joki", "Presensi & IoT", "dynamic_qr", "QrCode2"),
            EnterpriseModuleItem("m6", 6, "Pemindai Kamera Multi-Mode", "Scan QR Presensi & ISBN Buku", "Presensi & IoT", "qr_scanner", "QrCodeScanner"),
            EnterpriseModuleItem("m7", 7, "Keamanan Sidik Jari", "Sensor Biometrik & Keystore Vault", "Keamanan & Akses", "face_enrollment", "Fingerprint"),
            EnterpriseModuleItem("m8", 8, "Ujian CBT Anti-Cheat", "Computer-Based Testing native", "Akademik & LMS", "cbt_list", "Quiz"),
            EnterpriseModuleItem("m9", 9, "Sultan AI Socratic Tutor", "Bimbingan AI 24/7", "Kecerdasan Buatan (AI Edukasi)", "ai_tutor", "SmartToy"),
            EnterpriseModuleItem("m10", 10, "Koreksi Esai NLP AI", "Penilaian otomatis tata bahasa esai", "Kecerdasan Buatan (AI Edukasi)", "ai_tutor", "AutoAwesome"),
            EnterpriseModuleItem("m11", 11, "E-Ijazah & Transkrip Resmi", "Arsip transkrip nilai terverifikasi", "Akademik & LMS", "pdf_viewer", "Verified"),
            EnterpriseModuleItem("m12", 12, "Mutabaah Yaumiyah", "Checklist amalan yaumiyah siswa", "Kesiswaan & Ibadah", "mutabaah", "Mosque"),
            EnterpriseModuleItem("m13", 13, "Setoran Tahsin & Tahfidz", "Perekam suara hafalan Qur'an", "Kesiswaan & Ibadah", "tahsin_recorder", "Mic"),
            EnterpriseModuleItem("m14", 14, "Pesan & Konsultasi BK", "Obrolan real-time dengan Guru BK", "Kesiswaan & Ibadah", "conversation_list", "Chat"),
            EnterpriseModuleItem("m15", 15, "Tombol Darurat SOS Panik", "Pusat darurat krisis anti-bullying", "Kesiswaan & Ibadah", "antibullying_sos", "Emergency"),
            EnterpriseModuleItem("m16", 16, "Keuangan & SPP Virtual Account", "Pembayaran tagihan bank syariah", "Keuangan & Tata Usaha", "billing", "AccountBalanceWallet"),
            EnterpriseModuleItem("m17", 17, "Feed Pengumuman Sekolah", "Edaran resmi & info kegiatan", "Keuangan & Tata Usaha", "announcement_feed", "Campaign"),
            EnterpriseModuleItem("m18", 18, "Rapor & Dokumen PDF Viewer", "Kop surat YBWSA & nilai KKTP", "Akademik & LMS", "pdf_viewer", "PictureAsPdf"),
            EnterpriseModuleItem("m19", 19, "Manajer Unduhan Berkas", "Arsip dokumen & kuitansi", "Keuangan & Tata Usaha", "download_history", "Download"),
            EnterpriseModuleItem("m20", 20, "Pemindai Dokumen Kamera & OCR", "Ekstraksi teks tugas & nota", "Akademik & LMS", "document_scanner", "DocumentScanner"),
            EnterpriseModuleItem("m21", 21, "Tanda Tangan Digital Pengesahan", "Pengesahan rapor & persetujuan", "Keuangan & Tata Usaha", "digital_signature", "Draw"),
            EnterpriseModuleItem("m22", 22, "Analitik Akademik Siswa (Radar)", "Spider chart 6-sumbu KKTP", "Akademik & LMS", "academic_analytics", "AutoGraph"),
            EnterpriseModuleItem("m23", 23, "Analitik Hasil Belajar Kelas", "Histogram nilai & remedial siswa", "Akademik & LMS", "class_analytics", "Analytics"),
            EnterpriseModuleItem("m24", 24, "Heatmap & Pantau Capaian Anak", "Kalender presensi & tahfidz", "Kesiswaan & Ibadah", "child_progress", "CalendarMonth"),
            EnterpriseModuleItem("m25", 25, "Dashboard Eksekutif & KPI Kampus", "Statistik yayasan & kehadiran live", "Keuangan & Tata Usaha", "executive_analytics", "Dashboard"),
            EnterpriseModuleItem("m26", 26, "Portal Web Terpadu (SSO)", "Single Sign-On web container", "Sistem & Portal", "sso_webview", "Language"),
            EnterpriseModuleItem("m27", 27, "Pusat Modul Favorit", "Kumpulan modul yang dipin", "Sistem & Portal", "module_favorites", "Star"),
            EnterpriseModuleItem("m28", 28, "Pusat Pembaruan Aplikasi (OTA)", "Pembaruan versi & changelog", "Sistem & Portal", "in_app_update", "SystemUpdate"),
            EnterpriseModuleItem("m29", 29, "Buku Saku Poin & SP Digital", "Pencatatan pelanggaran & tanda tangan SP", "Kesiswaan & Ibadah", "discipline", "Gavel"),
            EnterpriseModuleItem("m30", 30, "Simulasi UTBK & Analisis PTN", "TryOut IRT Rasch & rekomendasi prodi AI", "Akademik & LMS", "utbk_tryout", "Psychology"),
            EnterpriseModuleItem("m31", 31, "E-Pustaka Pintar Perpustakaan", "Katalog buku fisik & self-checkout QR", "Akademik & LMS", "library_catalog", "LocalLibrary"),
            EnterpriseModuleItem("m32", 32, "Ekstrakurikuler & Info OSIS", "Pendaftaran ekskul & presensi latihan QR", "Kesiswaan & Ibadah", "extracurricular", "SportsSoccer"),
            EnterpriseModuleItem("m33", 33, "Portofolio Prestasi & e-Sertifikat", "Unggah juara lomba & export CV SNBP", "Kesiswaan & Ibadah", "achievement_upload", "EmojiEvents"),
            EnterpriseModuleItem("m34", 34, "Evaluasi Guru & E-Voting OSIS", "Kuesioner EKG anonim & pemilu digital", "Kesiswaan & Ibadah", "teacher_evaluation", "HowToVote"),
            EnterpriseModuleItem("m35", 35, "Mode Hemat Kuota (Lite Mode)", "Optimasi HP RAM 2GB & materi offline", "Sistem & Portal", "lite_mode_settings", "Speed"),
            EnterpriseModuleItem("m36", 36, "Pusat Notifikasi & Siaran Native", "Riwayat notifikasi push 5 saluran", "Sistem & Portal", "notification_center", "Notifications")
        )
    }

    val authorizedModules = remember(userRole) {
        modules.filter { item ->
            val isExecutiveOnly = item.route == "executive_analytics" || item.route == "admin_dashboard" || item.id == "m25"
            val isTeacherOnly = item.route.startsWith("teacher") || item.route == "question_bank" || item.route == "auto_generate_exam" || item.route == "class_analytics" || item.id == "m23" || item.route == "digital_signature" || item.id == "m21"
            val isParentOnly = item.id == "m24" || item.route == "child_progress"

            when {
                isExecutiveOnly -> isUserExecutive
                isTeacherOnly -> isUserTeacherOrAbove
                isParentOnly -> isUserParentOrAbove
                else -> true
            }
        }
    }

    val filteredModules = authorizedModules.filter { item ->
        val isExecutiveOnly = item.route == "executive_analytics" || item.route == "admin_dashboard" || item.id == "m25"
        val isTeacherOnly = item.route.startsWith("teacher") || item.route == "question_bank" || item.route == "auto_generate_exam" || item.route == "class_analytics" || item.id == "m23" || item.route == "digital_signature" || item.id == "m21"
        val isParentOnly = item.id == "m24" || item.route == "child_progress"

        val matchesCategory = when (selectedCategory) {
            "Semua" -> true
            "Khusus Siswa" -> !isExecutiveOnly && !isTeacherOnly && !isParentOnly
            "Khusus Guru" -> isTeacherOnly
            "Rahasia Pimpinan" -> isExecutiveOnly
            "Wali Murid" -> isParentOnly
            else -> item.category == selectedCategory
        }

        val matchesQuery = if (searchQuery.isBlank()) true else {
            item.title.contains(searchQuery, ignoreCase = true) || item.subtitle.contains(searchQuery, ignoreCase = true)
        }
        matchesCategory && matchesQuery
    }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Direktori Modul SISTA",
                subtitle = "Ekosistem Terintegrasi (${authorizedModules.size} Modul Aktif)",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // Live Search Bar & Favorites Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari modul (contoh: CBT, Presensi, Rapor)...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Emerald700) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { onNavigateRoute("module_favorites") },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Gold100)
                ) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = "Favorit", tint = Gold800)
                }
            }

            // Category Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Emerald700 else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Modules List (Strictly authorized only - unauthorized modules completely absent)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredModules, key = { it.id }) { item ->
                    val isFav = favoriteSet.contains(item.id)

                    SulaoneCard(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 2.dp,
                        onClick = {
                            onNavigateRoute(item.route)
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Emerald50),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "F${item.phaseNumber}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Emerald800
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = item.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = {
                                    if (isFav) favoriteSet.remove(item.id) else favoriteSet.add(item.id)
                                }
                            ) {
                                Icon(
                                    imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Pin Favorite",
                                    tint = if (isFav) Gold600 else Slate400
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Slate400
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
