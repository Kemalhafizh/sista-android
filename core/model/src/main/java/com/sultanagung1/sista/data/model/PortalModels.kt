package com.sultanagung1.sista.data.model

import com.google.gson.annotations.SerializedName

data class ChildProfile(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("nisn") val nisn: String,
    @SerializedName("classroom") val classroom: String,
    @SerializedName("attendance_today") val attendanceToday: String,
    @SerializedName("current_gpa") val currentGpa: Double,
    @SerializedName("unpaid_billings_count") val unpaidBillingsCount: Int
)

data class TeacherClassroom(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("grade_level") val gradeLevel: String,
    @SerializedName("total_students") val totalStudents: Int
)

data class StudentRosterItem(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("nisn") val nisn: String,
    var attendanceStatus: String = "H" // H, S, I, A
)

data class BlockchainCredentialItem(
    @SerializedName("id") val id: Long,
    @SerializedName("certificate_number") val certificateNumber: String,
    @SerializedName("title") val title: String,
    @SerializedName("issuer") val issuer: String,
    @SerializedName("issue_date") val issueDate: String,
    @SerializedName("blockchain_tx_hash") val txHash: String,
    @SerializedName("is_verified") val isVerified: Boolean
)

data class EnterpriseModuleItem(
    val id: String,
    val phaseNumber: Int,
    val title: String,
    val subtitle: String,
    val category: String,
    val route: String,
    val iconName: String,
    val isReady: Boolean = true
)

/**
 * Single source of truth for the module directory shown in
 * EnterpriseCatalogScreen and ModuleFavoritesScreen — previously each
 * screen hardcoded its own independent copy that drifted out of sync
 * (different categories for the same module id, different item counts).
 * This is legitimately static app config, not backend data: like a phone
 * launcher's own app list, the school's internal module directory doesn't
 * need a network round-trip.
 */
object EnterpriseModuleCatalog {
    val ALL: List<EnterpriseModuleItem> = listOf(
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
        EnterpriseModuleItem("m12", 12, "Mutabaah Yaumiyah", "Amalan yaumiyah siswa", "Kesiswaan & Ibadah", "mutabaah", "Mosque"),
        EnterpriseModuleItem("m13", 13, "Setoran Tahsin & Tahfidz", "Perekam suara hafalan Qur'an", "Kesiswaan & Ibadah", "tahsin_recorder", "Mic"),
        EnterpriseModuleItem("m14", 14, "Pesan & Konsultasi BK", "Obrolan real-time dengan Guru BK", "Kesiswaan & Ibadah", "conversation_list", "Chat"),
        EnterpriseModuleItem("m15", 15, "Tombol Darurat SOS Panik", "Pusat darurat krisis anti-bullying", "Kesiswaan & Ibadah", "antibullying_sos", "Emergency"),
        EnterpriseModuleItem("m16", 16, "Keuangan & SPP Virtual Account", "Pembayaran tagihan via bank", "Keuangan & Tata Usaha", "billing", "AccountBalanceWallet"),
        EnterpriseModuleItem("m17", 17, "Feed Pengumuman Sekolah", "Edaran resmi & info kegiatan", "Keuangan & Tata Usaha", "announcement_feed", "Campaign"),
        EnterpriseModuleItem("m18", 18, "Rapor & Dokumen PDF Viewer", "Kop surat YBWSA & nilai KKTP", "Akademik & LMS", "pdf_viewer", "PictureAsPdf"),
        EnterpriseModuleItem("m19", 19, "Manajer Unduhan Berkas", "Arsip dokumen & kuitansi", "Keuangan & Tata Usaha", "download_history", "Download"),
        EnterpriseModuleItem("m20", 20, "Pemindai Dokumen Kamera & OCR", "Ekstraksi teks tugas & nota", "Akademik & LMS", "document_scanner", "DocumentScanner"),
        EnterpriseModuleItem("m21", 21, "Tanda Tangan Digital Pengesahan", "Pengesahan rapor & persetujuan", "Keuangan & Tata Usaha", "digital_signature", "Draw"),
        EnterpriseModuleItem("m22", 22, "Analitik Akademik Siswa (Radar)", "Spider chart 6-sumbu KKTP", "Akademik & LMS", "academic_analytics", "AutoGraph"),
        EnterpriseModuleItem("m23", 23, "Analitik Hasil Belajar Kelas", "Histogram nilai & remedial siswa", "Akademik & LMS", "class_analytics", "Analytics"),
        EnterpriseModuleItem("m24", 24, "Heatmap & Pantau Capaian Anak", "Kalender presensi & tahfidz", "Kesiswaan & Ibadah", "child_progress", "CalendarMonth"),
        EnterpriseModuleItem("m25", 25, "Dashboard Eksekutif & KPI Kampus", "Statistik yayasan & kehadiran", "Keuangan & Tata Usaha", "executive_analytics", "Dashboard"),
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
        EnterpriseModuleItem("m36", 36, "Pusat Notifikasi & Siaran Native", "Riwayat notifikasi push", "Sistem & Portal", "notification_center", "Notifications")
    )

    val EXECUTIVE_ONLY_IDS = setOf("m25")
    val TEACHER_ONLY_IDS = setOf("m21", "m23")
    val PARENT_ONLY_IDS = setOf("m24")
}
