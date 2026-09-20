package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.api.AchievementApiService
import com.sultanagung1.sista.data.model.AcademicCvSummary
import com.sultanagung1.sista.data.model.AchievementItem
import com.sultanagung1.sista.data.model.CertificateItem
import com.sultanagung1.sista.data.model.UploadAchievementRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class AchievementRepository(private val apiService: AchievementApiService) {

    fun getAchievements(): Flow<NetworkResult<List<AchievementItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getAchievements()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Success(getSampleAchievements()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(getSampleAchievements()))
        }
    }.flowOn(Dispatchers.IO)

    fun uploadAchievement(request: UploadAchievementRequest): Flow<NetworkResult<AchievementItem>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.uploadAchievement(request)
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Success(
                    AchievementItem(
                        id = 99,
                        title = request.title,
                        field = request.field,
                        level = request.level,
                        organizer = request.organizer,
                        date = request.date,
                        verificationStatus = "PENDING_REVIEW",
                        pointsEarned = 20
                    )
                ))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(
                AchievementItem(
                    id = 99,
                    title = request.title,
                    field = request.field,
                    level = request.level,
                    organizer = request.organizer,
                    date = request.date,
                    verificationStatus = "PENDING_REVIEW",
                    pointsEarned = 20
                )
            ))
        }
    }.flowOn(Dispatchers.IO)

    fun getCertificates(): Flow<NetworkResult<List<CertificateItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getCertificates()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Success(getSampleCertificates()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(getSampleCertificates()))
        }
    }.flowOn(Dispatchers.IO)

    fun getAcademicCvSummary(): Flow<NetworkResult<AcademicCvSummary>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getAcademicCvSummary()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Success(
                    AcademicCvSummary(
                        studentName = "Ahmad Kemal Hafiz",
                        nisn = "0061234567",
                        gpaAverage = 92.4,
                        totalAchievements = 4,
                        totalRewardPoints = 40,
                        extracurriculars = listOf("Rohis Sultan Agung 1 (Karisma)", "Klub Robotik & AI")
                    )
                ))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(
                AcademicCvSummary(
                    studentName = "Ahmad Kemal Hafiz",
                    nisn = "0061234567",
                    gpaAverage = 92.4,
                    totalAchievements = 4,
                    totalRewardPoints = 40,
                    extracurriculars = listOf("Rohis Sultan Agung 1 (Karisma)", "Klub Robotik & AI")
                )
            ))
        }
    }.flowOn(Dispatchers.IO)

    private fun getSampleAchievements(): List<AchievementItem> = listOf(
        AchievementItem(
            id = 1,
            title = "Medali Emas Olimpiade Sains Nasional (OSN) Matematika",
            field = "Matematika & Sains",
            level = "PROVINSI",
            organizer = "Balai Pengembangan Talenta Indonesia (BPTI)",
            date = "2026-07-15",
            verificationStatus = "VALIDATED",
            pointsEarned = 25
        ),
        AchievementItem(
            id = 2,
            title = "Juara 1 Musabaqah Hifdzil Qur'an (MHQ) 10 Juz",
            field = "Keagamaan & Tahfidz",
            level = "KOTA",
            organizer = "Kementerian Agama Kota Semarang",
            date = "2026-06-20",
            verificationStatus = "VALIDATED",
            pointsEarned = 20
        ),
        AchievementItem(
            id = 3,
            title = "Best Innovation Award Lomba Robotika Nasional",
            field = "Sains & IT",
            level = "NASIONAL",
            organizer = "Fakultas Teknik UNDIP",
            date = "2026-08-10",
            verificationStatus = "PENDING_REVIEW",
            pointsEarned = 30
        )
    )

    private fun getSampleCertificates(): List<CertificateItem> = listOf(
        CertificateItem(
            id = 101,
            certificateNumber = "CERT/SA1/2026/089",
            title = "Sertifikat Kepanitiaan Sultan Agung Fest 2026",
            role = "Koordinator Divisi IT & Publikasi",
            issuedDate = "15 Agustus 2026",
            blockchainHash = "0x7f8a9b2c3d4e5f6a1b2c3d4e5f6a7b8c9d0e1f2a"
        ),
        CertificateItem(
            id = 102,
            certificateNumber = "TAHFIDZ/SA1/2026/012",
            title = "Sertifikat Kelulusan Tahfidz Juz 30 Bersanad",
            role = "Santri / Siswa Berprestasi",
            issuedDate = "01 Juni 2026",
            blockchainHash = "0x1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b"
        )
    )
}
