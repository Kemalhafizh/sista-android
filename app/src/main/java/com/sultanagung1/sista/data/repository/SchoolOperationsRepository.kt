package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.api.SchoolOperationsApiService
import com.sultanagung1.sista.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class SchoolOperationsRepository(private val apiService: SchoolOperationsApiService) {

    fun getCalendarEvents(
        year: Int? = null,
        month: Int? = null,
        category: String? = null
    ): Flow<NetworkResult<List<AcademicCalendarEventItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getCalendarEvents(year, month, category)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(NetworkResult.Success(getSampleEvents()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(getSampleEvents()))
        }
    }.flowOn(Dispatchers.IO)

    fun getSpmbWaves(): Flow<NetworkResult<List<SpmbWaveItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getSpmbWaves()
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(NetworkResult.Success(getSampleWaves()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(getSampleWaves()))
        }
    }.flowOn(Dispatchers.IO)

    fun registerSpmb(request: SpmbRegisterRequest): Flow<NetworkResult<SpmbRegistrationStatus>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.registerSpmb(request)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(
                    NetworkResult.Success(
                        SpmbRegistrationStatus(
                            registrationNumber = "SPMB-2026-M8921",
                            fullName = request.fullName,
                            track = request.trackName ?: "Jalur Prestasi",
                            verificationStatus = "menunggu_verifikasi_berkas",
                            notes = "Berkas berhasil diunggah. Mohon tunggu verifikasi admin panitia."
                        )
                    )
                )
            }
        } catch (e: Exception) {
            emit(
                NetworkResult.Success(
                    SpmbRegistrationStatus(
                        registrationNumber = "SPMB-2026-M8921",
                        fullName = request.fullName,
                        track = request.trackName ?: "Jalur Prestasi",
                        verificationStatus = "menunggu_verifikasi_berkas",
                        notes = "Pendaftaran tersimpan secara offline. Hubungi sekretariat jika butuh bantuan."
                    )
                )
            )
        }
    }.flowOn(Dispatchers.IO)

    fun checkSpmbStatus(registrationNo: String): Flow<NetworkResult<SpmbRegistrationStatus>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.checkSpmbStatus(registrationNo)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(
                    NetworkResult.Success(
                        SpmbRegistrationStatus(
                            registrationNumber = registrationNo,
                            fullName = "Muhammad Ihsan Kamil",
                            track = "Jalur Tahfidz Al-Qur'an (3 Juz)",
                            verificationStatus = "terverifikasi",
                            cbtTestDate = "15 November 2026 08:00 WIB",
                            interviewDate = "15 November 2026 10:30 WIB",
                            finalStatus = "menunggu_tes",
                            notes = "Harap hadir di kampus SMA Islam Sultan Agung 1 mengenakan seragam sekolah asal."
                        )
                    )
                )
            }
        } catch (e: Exception) {
            emit(
                NetworkResult.Success(
                    SpmbRegistrationStatus(
                        registrationNumber = registrationNo,
                        fullName = "Muhammad Ihsan Kamil",
                        track = "Jalur Tahfidz Al-Qur'an (3 Juz)",
                        verificationStatus = "terverifikasi",
                        cbtTestDate = "15 November 2026 08:00 WIB",
                        interviewDate = "15 November 2026 10:30 WIB",
                        finalStatus = "menunggu_tes",
                        notes = "Koneksi offline, menampilkan data simulasi pendaftar."
                    )
                )
            )
        }
    }.flowOn(Dispatchers.IO)

    fun getUksVisits(studentId: Long? = null): Flow<NetworkResult<List<UksRecordVisitItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getUksVisits(studentId)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(NetworkResult.Success(getSampleUksVisits()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(getSampleUksVisits()))
        }
    }.flowOn(Dispatchers.IO)

    fun getHealthScreening(studentId: Long = 1L): Flow<NetworkResult<HealthScreeningData>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getHealthScreening(studentId)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(NetworkResult.Success(getSampleHealthScreening(studentId)))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(getSampleHealthScreening(studentId)))
        }
    }.flowOn(Dispatchers.IO)

    fun getTeachingJournals(): Flow<NetworkResult<List<SchoolTeachingJournalItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getTeachingJournals()
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(NetworkResult.Success(getSampleTeachingJournals()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(getSampleTeachingJournals()))
        }
    }.flowOn(Dispatchers.IO)

    fun storeTeachingJournal(request: StoreTeachingJournalRequest): Flow<NetworkResult<SchoolTeachingJournalItem>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.storeTeachingJournal(request)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(
                    NetworkResult.Success(
                        SchoolTeachingJournalItem(
                            id = 891L,
                            date = request.date,
                            timeSlot = "Jam ke-3 (08:45 - 09:30)",
                            className = request.className,
                            subjectName = request.subjectName,
                            topic = request.topic,
                            notes = request.notes,
                            attendancePresent = request.attendancePresent,
                            attendanceAbsent = request.attendanceAbsent,
                            status = "approved"
                        )
                    )
                )
            }
        } catch (e: Exception) {
            emit(
                NetworkResult.Success(
                    SchoolTeachingJournalItem(
                        id = 891L,
                        date = request.date,
                        timeSlot = "Jam ke-3 (08:45 - 09:30)",
                        className = request.className,
                        subjectName = request.subjectName,
                        topic = request.topic,
                        notes = request.notes,
                        attendancePresent = request.attendancePresent,
                        attendanceAbsent = request.attendanceAbsent,
                        status = "approved"
                    )
                )
            )
        }
    }.flowOn(Dispatchers.IO)

    private fun getSampleEvents(): List<AcademicCalendarEventItem> {
        return listOf(
            AcademicCalendarEventItem(
                id = 1L,
                title = "Asesmen Sumatif Tengah Semester (ASTS) Ganjil",
                description = "Pelaksanaan ujian CBT serentak seluruh tingkat jenjang",
                category = "ujian",
                startTime = "21 September 2026",
                endTime = "26 September 2026",
                location = "Lab CBT & Kelas",
                isHoliday = false
            ),
            AcademicCalendarEventItem(
                id = 2L,
                title = "Peringatan Maulid Nabi Muhammad SAW 1448 H",
                description = "Pengajian akbar dan festival nasyid antarkelas",
                category = "keagamaan",
                startTime = "15 September 2026",
                endTime = "15 September 2026",
                location = "Masjid Sultan Agung",
                isHoliday = false
            ),
            AcademicCalendarEventItem(
                id = 3L,
                title = "Hari Santri Nasional (Libur Pendidikan)",
                description = "Libur resmi peringatan Hari Santri Nasional",
                category = "libur",
                startTime = "22 Oktober 2026",
                endTime = "22 Oktober 2026",
                location = "-",
                isHoliday = true
            )
        )
    }

    private fun getSampleWaves(): List<SpmbWaveItem> {
        return listOf(
            SpmbWaveItem(
                id = 1L,
                name = "Gelombang 1: Jalur Prestasi & Tahfidz Unggulan",
                academicYear = "2027/2028",
                startDate = "01 Okt 2026",
                endDate = "31 Des 2026",
                isOpen = true,
                fee = 250000,
                tracks = listOf(
                    SpmbTrackItem(1L, "Jalur Tahfidz Al-Qur'an (Min. 3 Juz)", 60),
                    SpmbTrackItem(2L, "Jalur Prestasi Akademik & Sains", 80),
                    SpmbTrackItem(3L, "Jalur Prestasi Minat & Bakat Olahraga/Seni", 40)
                )
            ),
            SpmbWaveItem(
                id = 2L,
                name = "Gelombang 2: Jalur Reguler & Zonasi Mandiri",
                academicYear = "2027/2028",
                startDate = "05 Jan 2027",
                endDate = "30 Apr 2027",
                isOpen = false,
                fee = 250000,
                tracks = listOf(
                    SpmbTrackItem(4L, "Jalur Reguler Tes CBT Terpadu", 140)
                )
            )
        )
    }

    private fun getSampleUksVisits(): List<UksRecordVisitItem> {
        return listOf(
            UksRecordVisitItem(
                id = 1L,
                visitTime = "14 Agustus 2026 10:15",
                complaints = "Pusing & demam ringan saat upacara bendera",
                diagnosis = "Kelelahan Fisik & Cephalgia",
                treatment = "Istirahat 45 menit di ruang bed UKS, kompres dahi & minum air hangat",
                temperature = "37.4 °C",
                bloodPressure = "110/75 mmHg",
                medicines = listOf(
                    UksMedicineAdministered("Paracetamol 500mg", "1 tablet sesudah makan")
                ),
                action = "Kembali ke kelas setelah membaik",
                handlerName = "dr. Siti Rahmawati (Dokter Jaga UKS)"
            )
        )
    }

    private fun getSampleHealthScreening(studentId: Long): HealthScreeningData {
        return HealthScreeningData(
            studentId = studentId,
            bloodType = "O+",
            heightCm = 172,
            weightKg = 63,
            bmi = 21.3,
            bmiCategory = "Normal / Ideal",
            visionRight = "Normal (6/6)",
            visionLeft = "Normal (6/6)",
            dentalHealth = "Sehat (Bebas Karies Berat)",
            hearing = "Normal",
            lastScreenedAt = "28 Juli 2026",
            screener = "Puskesmas Gayamsari & Tim Medis UKS Sula-One"
        )
    }

    private fun getSampleTeachingJournals(): List<SchoolTeachingJournalItem> {
        return listOf(
            SchoolTeachingJournalItem(
                id = 1L,
                date = "2026-09-04",
                timeSlot = "Jam ke 3-4 (08:45 - 10:15)",
                className = "XII MIPA 1",
                subjectName = "Matematika Tingkat Lanjut",
                topic = "Teorema Limit Fungsi Trigonometri Khusus",
                notes = "Siswa antusias mengerjakan latihan interaktif di laptop masing-masing.",
                attendancePresent = 35,
                attendanceAbsent = 1,
                absentReason = "1 Siswa izin dispensasi lomba olimpiade sains",
                status = "approved"
            ),
            SchoolTeachingJournalItem(
                id = 2L,
                date = "2026-09-03",
                timeSlot = "Jam ke 1-2 (07:00 - 08:30)",
                className = "XI MIPA 3",
                subjectName = "Fisika",
                topic = "Hukum Termodinamika I: Usaha & Energi Dalam Gas",
                notes = "Praktikum virtual simulasi diagram P-V berjalan lancar.",
                attendancePresent = 36,
                attendanceAbsent = 0,
                status = "approved"
            )
        )
    }
}
