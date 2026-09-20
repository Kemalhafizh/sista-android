package com.sultanagung1.sista.core.document

import com.sultanagung1.sista.data.model.OcrScanResult
import kotlinx.coroutines.delay

object OcrProcessor {

    suspend fun processDocumentImage(docType: String): OcrScanResult {
        delay(1200) // Simulate OCR neural inference

        return when (docType) {
            "RECEIPT" -> {
                OcrScanResult(
                    scannedText = "YAYASAN BADAN WAKAF SULTAN AGUNG\nBUKTI PEMBAYARAN SPP & INFAQ\nNomor: KWT/2026/08/8892\nNama: Ahmad Kemal Hafizh\nNISN: 0071829102\nTotal: Rp 850.000 (LUNAS)",
                    confidence = 0.96f,
                    extractedFields = mapOf(
                        "Lembaga" to "YBWSA Semarang",
                        "Nama Siswa" to "Ahmad Kemal Hafizh",
                        "NISN" to "0071829102",
                        "Jumlah Pembayaran" to "Rp 850.000",
                        "Status" to "LUNAS & VALID"
                    )
                )
            }
            "ASSIGNMENT" -> {
                OcrScanResult(
                    scannedText = "LEMBAR JAWABAN TUGAS FISIKA\nNama: Ahmad Kemal Hafizh - XII MIPA 1\nMateri: Gelombang Elektromagnetik\nSkor Evaluasi: 95 / 100",
                    confidence = 0.94f,
                    extractedFields = mapOf(
                        "Mata Pelajaran" to "Fisika Modern",
                        "Topik" to "Gelombang Elektromagnetik",
                        "Nama Siswa" to "Ahmad Kemal Hafizh",
                        "Kelas" to "XII MIPA 1",
                        "Skor Otomatis AI" to "95 (Sangat Baik)"
                    )
                )
            }
            else -> {
                OcrScanResult(
                    scannedText = "SURAT KETERANGAN AKTIF BELAJAR\nSMA Islam Sultan Agung 1 Semarang\nMenerangkan bahwa siswa tersebut terdaftar aktif pada Tahun Ajaran 2025/2026.",
                    confidence = 0.98f,
                    extractedFields = mapOf(
                        "Dokumen" to "Surat Keterangan Aktif",
                        "Sekolah" to "SMA Islam Sultan Agung 1 Semarang",
                        "Status" to "Terverifikasi Resmi"
                    )
                )
            }
        }
    }
}
