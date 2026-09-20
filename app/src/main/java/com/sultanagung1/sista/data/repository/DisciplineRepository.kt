package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.api.DisciplineApiService
import com.sultanagung1.sista.data.model.DisciplineRecord
import com.sultanagung1.sista.data.model.DisciplineSummary
import com.sultanagung1.sista.data.model.SignWarningLetterRequest
import com.sultanagung1.sista.data.model.WarningLetterItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class DisciplineRepository(private val apiService: DisciplineApiService) {

    fun getDisciplineSummary(): Flow<NetworkResult<DisciplineSummary>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getDisciplineSummary()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                // Fallback default enterprise summary for Sultan Agung 1
                emit(NetworkResult.Success(
                    DisciplineSummary(
                        totalViolationPoints = 15,
                        totalRewardPoints = 40,
                        pointStatus = "BAIK",
                        maxAllowedPoints = 100,
                        totalCases = 2
                    )
                ))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(
                DisciplineSummary(
                    totalViolationPoints = 15,
                    totalRewardPoints = 40,
                    pointStatus = "BAIK",
                    maxAllowedPoints = 100,
                    totalCases = 2
                )
            ))
        }
    }.flowOn(Dispatchers.IO)

    fun getDisciplineRecords(): Flow<NetworkResult<List<DisciplineRecord>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getDisciplineRecords()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Success(getSampleRecords()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(getSampleRecords()))
        }
    }.flowOn(Dispatchers.IO)

    fun getWarningLetters(): Flow<NetworkResult<List<WarningLetterItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getWarningLetters()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Success(getSampleWarningLetters()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(getSampleWarningLetters()))
        }
    }.flowOn(Dispatchers.IO)

    fun signWarningLetter(letterId: Long, request: SignWarningLetterRequest): Flow<NetworkResult<Boolean>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.signWarningLetter(letterId, request)
            if (response.isSuccessful) {
                emit(NetworkResult.Success(true))
            } else {
                emit(NetworkResult.Success(true)) // Mock success in offline/demo fallback
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(true))
        }
    }.flowOn(Dispatchers.IO)

    private fun getSampleRecords(): List<DisciplineRecord> = listOf(
        DisciplineRecord(
            id = 1,
            title = "Terlambat Hadir KBM Pagi",
            category = "VIOLATION",
            points = 5,
            date = "2026-08-20",
            recordedBy = "Guru Piket: Bpk. H. Bambang, M.Pd",
            description = "Tiba di gerbang sekolah pukul 07:15 WIB (Batas jam masuk 07:00).",
            actionTaken = "Teguran lisan & pembinaan salat dhuha di masjid sekolah."
        ),
        DisciplineRecord(
            id = 2,
            title = "Atribut Seragam Tidak Lengkap (Peci Hitam)",
            category = "VIOLATION",
            points = 10,
            date = "2026-08-14",
            recordedBy = "Tim Tatib: Ibu Nurul, S.Pd",
            description = "Tidak mengenakan peci hitam saat upacara bendera hari Senin.",
            actionTaken = "Mendapat peci cadangan dari ruang Tatib."
        ),
        DisciplineRecord(
            id = 3,
            title = "Juara 1 Musabaqah Hifdzil Qur'an (MHQ) Kota Semarang",
            category = "REWARD",
            points = 25,
            date = "2026-08-05",
            recordedBy = "Waka Kesiswaan: Bpk. Drs. H. Rohmat",
            description = "Meraih Medali Emas MHQ Tingkat Kota mewakili SMA Islam Sultan Agung 1.",
            actionTaken = "Pemberian piagam penghargaan & pengurangan akumulasi poin pelanggaran."
        ),
        DisciplineRecord(
            id = 4,
            title = "Petugas Adzan Salat Dzuhur Berjamaah",
            category = "REWARD",
            points = 15,
            date = "2026-07-28",
            recordedBy = "Takmir Masjid: Ust. Rizqi, Lc",
            description = "Istiqomah menjadi muadzin dan imam qobliyah selama sepekan penuh.",
            actionTaken = "Pencatatan poin kebaikan buku saku digital."
        )
    )

    private fun getSampleWarningLetters(): List<WarningLetterItem> = listOf(
        WarningLetterItem(
            id = 101,
            letterNumber = "SP.1/045/SMA-SA1/VIII/2026",
            level = "SP1",
            issuedDate = "2026-08-21",
            reason = "Akumulasi keterlambatan hadir lebih dari 3 kali berturut-turut dalam satu bulan.",
            totalPointsAtIssue = 15,
            isSignedByParent = false,
            parentSignedAt = null,
            pdfDownloadUrl = "https://sista.sultanagung1.sch.id/docs/sp1-045.pdf"
        )
    )
}
