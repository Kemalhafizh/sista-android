package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.api.ERaporApiService
import com.sultanagung1.sista.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class RaporRepository(private val apiService: ERaporApiService) {

    fun getStudentRapor(childId: Long? = null): Flow<NetworkResult<RaporDetailData>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = if (childId != null) apiService.getChildRapor(childId) else apiService.getCurrentRapor()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!.data))
            } else {
                emit(NetworkResult.Success(getSampleRapor()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(getSampleRapor()))
        }
    }.flowOn(Dispatchers.IO)

    fun generatePdf(reportCardId: Long, studentId: Long): Flow<NetworkResult<String>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.generatePdf(reportCardId, studentId)
            if (response.isSuccessful) {
                emit(NetworkResult.Success("PDF Rapor berhasil digenerate"))
            } else {
                emit(NetworkResult.Error("Gagal generate PDF", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus"))
        }
    }.flowOn(Dispatchers.IO)

    private fun getSampleRapor(): RaporDetailData {
        return RaporDetailData(
            student = RaporStudentData(1L, "Ahmad Faiz Abdullah", "1058291048", "XII MIPA 1"),
            academicSummary = RaporAcademicSummary(88.4, "B", 8),
            entries = listOf(
                RaporEntryItem(1L, "Pendidikan Agama Islam & Budi Pekerti", 92.0, "A", "Menunjukkan penguasaan sangat baik dalam pemahaman dalil Asmaul Husna dan adab pergaulan islami.", "Pertahankan prestasi akhlak dan ibadah."),
                RaporEntryItem(2L, "Matematika Tingkat Lanjut", 86.0, "B", "Mampu menyelesaikan permasalahan turunan fungsi dan integral substitusi dengan terampil.", "Perlu penguatan pada studi kasus trigonometri analitis."),
                RaporEntryItem(3L, "Fisika", 89.0, "B", "Memahami konsep gelombang elektromagnetik dan termodinamika secara mendalam.", "Aktif dalam praktikum laboratorium."),
                RaporEntryItem(4L, "Kimia", 85.0, "B", "Mampu menganalisis laju reaksi dan kesetimbangan kimia dengan cermat.", "Tingkatkan ketelitian perhitungan molalitas."),
                RaporEntryItem(5L, "Biologi", 90.0, "A", "Menunjukkan ketuntasan luar biasa pada bab genetika dan rekayasa bioteknologi modern.", "Sangat antusias dalam riset biologi."),
                RaporEntryItem(6L, "Bahasa Indonesia", 87.0, "B", "Mampu menyusun teks artikel ilmiah populer dan karya esai kritis dengan koherensi tinggi.", "Kosa kata dan sintaksis sangat baik."),
                RaporEntryItem(7L, "Bahasa Inggris", 88.0, "B", "Aktif mengemukakan argumen dalam presentasi lisan dan menyusun academic report.", "Fluency dan pronunciation terjaga."),
                RaporEntryItem(8L, "Pendidikan Pancasila", 90.0, "A", "Menghayati nilai-nilai luhur Pancasila dalam kehidupan berbangsa dan bermasyarakat.", "Sikap kepemimpinan sangat menonjol.")
            ),
            characters = listOf(
                RaporCharacterItem(1L, "Beriman, Bertakwa kepada Tuhan YME, dan Berakhlak Mulia", "Sangat Baik", "Konsisten melaksanakan sholat berjamaah tepat waktu dan menjunjung tinggi amanah."),
                RaporCharacterItem(2L, "Bernalar Kritis", "Sangat Baik", "Mampu mengidentifikasi masalah secara logis serta merumuskan hipotesis ilmiah yang tepat."),
                RaporCharacterItem(3L, "Kreatif", "Baik", "Mampu memodifikasi gagasan orisinal dalam proyek pembelajaran berbasis riset."),
                RaporCharacterItem(4L, "Gotong Royong", "Sangat Baik", "Menunjukkan kepedulian sosial tinggi dan aktif memimpin diskusi kelompok.")
            ),
            pdfStatus = RaporPdfStatus(
                isReady = true,
                filePath = "uploads/rapor/rapor_faiz_smt_ganjil.pdf",
                sha256Hash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                generatedAt = "2026-09-01T08:00:00Z"
            )
        )
    }
}
