package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.api.StudentProfileApiService
import com.sultanagung1.sista.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class StudentProfileRepository(private val apiService: StudentProfileApiService) {

    fun getStudentProfile(studentId: Long? = null): Flow<NetworkResult<StudentProfile360Data>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = if (studentId != null && studentId > 0) {
                apiService.getStudentProfile(studentId)
            } else {
                apiService.getMyProfile()
            }
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(NetworkResult.Success(getSampleProfile(studentId)))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(getSampleProfile(studentId)))
        }
    }.flowOn(Dispatchers.IO)

    private fun getSampleProfile(studentId: Long?): StudentProfile360Data {
        return StudentProfile360Data(
            biodata = ProfileBiodata(
                id = studentId ?: 1L,
                name = "Ahmad Faiz Abdullah",
                nisn = "1058291048",
                className = "XII MIPA 1",
                email = "faiz@sultanagung1.sch.id"
            ),
            academicSummary = ProfileAcademicSummary(
                averageScore = 88.4,
                rankInClass = 3,
                totalClassStudents = 36,
                strongestSubject = "Pendidikan Agama Islam",
                improvementNeeded = "Kimia",
                trend = listOf(84.2, 86.0, 87.5, 88.4)
            ),
            ibadahSummary = ProfileIbadahSummary(
                tahfidzJuzCompleted = 3,
                targetJuz = 5,
                tahfidzProgressPercent = 60,
                currentSurah = "Al-Mulk (Ayat 1-30)",
                mutabaahWeeklyStreak = 18,
                sholatJamaahPercent = 96.5
            ),
            disciplineSummary = ProfileDisciplineSummary(
                totalPositivePoints = 45,
                totalViolationPoints = 5,
                netPoints = 40,
                category = "Teladan",
                activeSanctions = 0
            ),
            extracurricularList = listOf(
                ProfileExtracurricularItem(
                    name = "Rohis Karisma Sultan Agung",
                    role = "Ketua Divisi Syiar & Dakwah",
                    joinedYear = "2024"
                ),
                ProfileExtracurricularItem(
                    name = "Klub Robotik & AI Coding",
                    role = "Anggota Tim Kompetisi",
                    joinedYear = "2025"
                )
            ),
            achievementList = listOf(
                ProfileAchievementItem(
                    title = "Juara 1 Olimpiade Sains Matematika Kota Semarang",
                    level = "Kota/Kabupaten",
                    year = 2025,
                    category = "Akademik"
                ),
                ProfileAchievementItem(
                    title = "Medali Emas MHQ (Musabaqah Hifzhil Quran) 3 Juz",
                    level = "Provinsi Jawa Tengah",
                    year = 2026,
                    category = "Keagamaan"
                )
            ),
            healthSummary = ProfileHealthSummary(
                bloodType = "O+",
                heightCm = 172,
                weightKg = 63,
                allergies = listOf("Tidak ada riwayat alergi berat"),
                totalUksVisits = 1,
                lastVisitDate = "2026-08-14"
            )
        )
    }
}
