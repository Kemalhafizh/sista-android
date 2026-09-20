package com.sultanagung1.sista.ui.scanner

enum class ScanMode(val title: String, val description: String) {
    ATTENDANCE("Presensi QR", "Pindai QR Gerbang & Kelas"),
    LIBRARY_BOOK("Buku Perpus", "Pindai Barcode ISBN Buku"),
    EVENT_TICKET("Tiket Acara", "Pindai Tiket Kajian / Seminar"),
    VISITOR_PASS("Buku Tamu", "Pindai QR Akses Tamu Yayasan")
}

data class ParsedScanResult(
    val title: String,
    val subtitle: String,
    val type: ScanMode,
    val rawPayload: String,
    val isValid: Boolean = true,
    val details: Map<String, String> = emptyMap()
)

object ScanResultHandler {

    fun parse(rawCode: String, mode: ScanMode): ParsedScanResult {
        return when (mode) {
            ScanMode.ATTENDANCE -> {
                ParsedScanResult(
                    title = "Presensi Berhasil Diverifikasi",
                    subtitle = "Siswa Kelas XII MIPA 1 • SMA Islam Sultan Agung 1",
                    type = mode,
                    rawPayload = rawCode,
                    details = mapOf(
                        "Status" to "Hadir Tepat Waktu (06:45 WIB)",
                        "Lokasi Gerbang" to "Gerbang Utama Jl. Mataram",
                        "Token Validasi" to rawCode.take(16)
                    )
                )
            }
            ScanMode.LIBRARY_BOOK -> {
                ParsedScanResult(
                    title = "Buku Perpustakaan Teridentifikasi",
                    subtitle = "Fisika Modern Kelas XII (Kurikulum Merdeka)",
                    type = mode,
                    rawPayload = rawCode,
                    details = mapOf(
                        "ISBN" to "978-602-244-800-6",
                        "Penerbit" to "Pusat Kurikulum dan Perbukuan Kemendikbudristek",
                        "Status Stok" to "Tersedia (Rak B-04)"
                    )
                )
            }
            ScanMode.EVENT_TICKET -> {
                ParsedScanResult(
                    title = "Tiket Acara Terverifikasi",
                    subtitle = "Kajian Akbar & Maulid Nabi Muhammad SAW 1448 H",
                    type = mode,
                    rawPayload = rawCode,
                    details = mapOf(
                        "Nama Peserta" to "Muhammad Rizky Pratama",
                        "Seat / Area" to "Auditorium Utama Lantai 2",
                        "Status Tiket" to "LUNAS & VALID"
                    )
                )
            }
            ScanMode.VISITOR_PASS -> {
                ParsedScanResult(
                    title = "Akses Buku Tamu Terdaftar",
                    subtitle = "Kunjungan Wali Murid / Tamu Kedinasan",
                    type = mode,
                    rawPayload = rawCode,
                    details = mapOf(
                        "Keperluan" to "Konsultasi Akademik Wali Kelas",
                        "Masa Berlaku" to "Hari Ini (07:00 - 15:00 WIB)"
                    )
                )
            }
        }
    }
}
