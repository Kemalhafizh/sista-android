package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.api.UtbkApiService
import com.sultanagung1.sista.data.model.AlumniCampusItem
import com.sultanagung1.sista.data.model.MajorRecommendationItem
import com.sultanagung1.sista.data.model.UtbkQuestion
import com.sultanagung1.sista.data.model.UtbkTryoutItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class UtbkRepository(private val apiService: UtbkApiService) {

    fun getUtbkTryouts(): Flow<NetworkResult<List<UtbkTryoutItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getUtbkTryouts()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Success(getSampleTryouts()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(getSampleTryouts()))
        }
    }.flowOn(Dispatchers.IO)

    fun getMajorRecommendations(): Flow<NetworkResult<List<MajorRecommendationItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getMajorRecommendations()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Success(getSampleRecommendations()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(getSampleRecommendations()))
        }
    }.flowOn(Dispatchers.IO)

    fun getCampusAlumniDirectory(): Flow<NetworkResult<List<AlumniCampusItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getCampusAlumniDirectory()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Success(getSampleAlumni()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(getSampleAlumni()))
        }
    }.flowOn(Dispatchers.IO)

    private fun getSampleTryouts(): List<UtbkTryoutItem> = listOf(
        UtbkTryoutItem(
            id = 1,
            title = "Simulasi Akbar UTBK-SNBT 2026 Gelombang 1",
            subtestName = "Tes Potensi Skolastik (TPS) & Penalaran Umum",
            totalQuestions = 30,
            durationMinutes = 45,
            status = "READY",
            irtScore = 685.5,
            nationalPercentile = 94.2
        ),
        UtbkTryoutItem(
            id = 2,
            title = "TryOut Literasi Bahasa Indonesia & Bahasa Inggris",
            subtestName = "Literasi Membaca & Menulis Teks Kritis",
            totalQuestions = 25,
            durationMinutes = 35,
            status = "COMPLETED",
            irtScore = 712.0,
            nationalPercentile = 97.5
        ),
        UtbkTryoutItem(
            id = 3,
            title = "Simulasi Penalaran Matematika Kuantitatif",
            subtestName = "Pemecahan Masalah Matematika Kontekstual",
            totalQuestions = 20,
            durationMinutes = 30,
            status = "READY",
            irtScore = 640.0,
            nationalPercentile = 88.0
        )
    )

    private fun getSampleRecommendations(): List<MajorRecommendationItem> = listOf(
        MajorRecommendationItem(
            universityName = "Universitas Diponegoro (UNDIP) Semarang",
            majorName = "Kedokteran Umum (S1)",
            category = "SAINTEK",
            passProbability = 87,
            probabilityBadge = "TINGGI (Hijau)",
            kktpReportAlignment = "Sangat Selaras dengan Rapor Biologi (94.2) & Kimia (92.0)",
            historicalAlumniCount = 28
        ),
        MajorRecommendationItem(
            universityName = "Institut Teknologi Bandung (ITB)",
            majorName = "Sekolah Teknik Elektro & Informatika (STEI)",
            category = "SAINTEK",
            passProbability = 76,
            probabilityBadge = "SEDANG (Kuning)",
            kktpReportAlignment = "Selaras dengan Nilai Matematika Peminatan (95.0) & Fisika (88.0)",
            historicalAlumniCount = 14
        ),
        MajorRecommendationItem(
            universityName = "Universitas Gadjah Mada (UGM) Yogyakarta",
            majorName = "Farmasi (S1)",
            category = "SAINTEK",
            passProbability = 91,
            probabilityBadge = "TINGGI (Hijau)",
            kktpReportAlignment = "Rata-rata TryOut IRT Kimia 712.0 memenuhi passing grade",
            historicalAlumniCount = 19
        )
    )

    private fun getSampleAlumni(): List<AlumniCampusItem> = listOf(
        AlumniCampusItem(
            id = 1,
            name = "Ahmad Fadhil, S.Ked",
            graduationYear = "Alumni 2023",
            university = "Fakultas Kedokteran UNDIP",
            major = "Pendidikan Dokter",
            admissionPath = "SNBP (Jalur Undangan)",
            contactAvailable = true
        ),
        AlumniCampusItem(
            id = 2,
            name = "Rizka Aulia Putri, S.Kom",
            graduationYear = "Alumni 2024",
            university = "STEI ITB Bandung",
            major = "Teknik Informatika",
            admissionPath = "SNBT (Skor UTBK 745)",
            contactAvailable = true
        ),
        AlumniCampusItem(
            id = 3,
            name = "Muhammad Rayhan, S.T",
            graduationYear = "Alumni 2024",
            university = "Universitas Indonesia (UI)",
            major = "Teknik Industri",
            admissionPath = "SNBT (Skor UTBK 720)",
            contactAvailable = true
        )
    )
}
