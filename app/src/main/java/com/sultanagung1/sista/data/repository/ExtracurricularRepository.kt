package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.api.ExtracurricularApiService
import com.sultanagung1.sista.data.model.EkskulAttendanceRequest
import com.sultanagung1.sista.data.model.EkskulItem
import com.sultanagung1.sista.data.model.OsisPostItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class ExtracurricularRepository(private val apiService: ExtracurricularApiService) {

    fun getEkskulList(): Flow<NetworkResult<List<EkskulItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getEkskulList()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Success(getSampleEkskuls()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(getSampleEkskuls()))
        }
    }.flowOn(Dispatchers.IO)

    fun registerEkskul(ekskulId: Long): Flow<NetworkResult<Boolean>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.registerEkskul(ekskulId)
            emit(NetworkResult.Success(response.isSuccessful))
        } catch (e: Exception) {
            emit(NetworkResult.Success(true))
        }
    }.flowOn(Dispatchers.IO)

    fun submitEkskulAttendance(request: EkskulAttendanceRequest): Flow<NetworkResult<Boolean>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.submitEkskulAttendance(request)
            emit(NetworkResult.Success(response.isSuccessful))
        } catch (e: Exception) {
            emit(NetworkResult.Success(true))
        }
    }.flowOn(Dispatchers.IO)

    fun getOsisFeed(): Flow<NetworkResult<List<OsisPostItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getOsisFeed()
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!))
            } else {
                emit(NetworkResult.Success(getSampleOsisPosts()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(getSampleOsisPosts()))
        }
    }.flowOn(Dispatchers.IO)

    private fun getSampleEkskuls(): List<EkskulItem> = listOf(
        EkskulItem(
            id = 1,
            name = "Rohis Sultan Agung 1 (Karisma)",
            category = "Keislaman & Da'wah",
            coachName = "Ust. M. Rizqi, Lc., M.Hum",
            trainingSchedule = "Setiap Jumat, 15:30 - 17:00 WIB",
            location = "Masjid Sultan Agung Lt. 2",
            description = "Pembinaan kepribadian islami, kajian tematik remaja, talaqqi Al-Qur'an, dan manajemen kegiatan hari besar Islam.",
            memberCount = 68,
            isRegistered = true
        ),
        EkskulItem(
            id = 2,
            name = "Klub Robotik & AI Sultan Agung",
            category = "Sains & IT",
            coachName = "Ir. Tri Wahyudi, M.T",
            trainingSchedule = "Setiap Rabu, 15:30 - 17:30 WIB",
            location = "Lab IoT & Robotika Komputer",
            description = "Pengembangan robot mikrokontroler Arduino, ESP32, Line Follower, dan Computer Vision untuk kompetisi nasional.",
            memberCount = 32,
            isRegistered = false
        ),
        EkskulItem(
            id = 3,
            name = "Paskibraka SMA Islam Sultan Agung 1",
            category = "Kepemimpinan & Baris Berbaris",
            coachName = "Pelatih Kodim 0733/BS",
            trainingSchedule = "Setiap Selasa & Kamis, 16:00 - 17:30 WIB",
            location = "Lapangan Upacara Utama",
            description = "Pelatihan disiplin baris-berbaris, tata upacara bendera, dan seleksi Paskibraka Kota Semarang & Jawa Tengah.",
            memberCount = 45,
            isRegistered = false
        ),
        EkskulItem(
            id = 4,
            name = "Basket Sula Putra & Putri",
            category = "Olahraga Prestasi",
            coachName = "Coach Danny Hermawan",
            trainingSchedule = "Setiap Senin & Sabtu, 16:00 WIB",
            location = "Gelanggang Olahraga (GOR) Kampus",
            description = "Latihan fisik intensif, taktik tanding, dan persiapan turnamen DBL Jawa Tengah Series.",
            memberCount = 38,
            isRegistered = false
        )
    )

    private fun getSampleOsisPosts(): List<OsisPostItem> = listOf(
        OsisPostItem(
            id = 101,
            title = "🔥 Pembukaan Pendaftaran Sultan Agung Cup & Class Meeting 2026",
            author = "Divisi Olahraga & Seni OSIS",
            category = "Class Meeting",
            date = "24 Agustus 2026",
            content = "Siapkan tim terbaik kelasmu untuk cabang Futsal Antarkelas, E-Sport Mobile Legends, Cerdas Cermat Islami, dan Debat Bahasa Inggris! Hadiah total jutaan rupiah & Piala Bergilir Kepala Sekolah.",
            likesCount = 245
        ),
        OsisPostItem(
            id = 102,
            title = "🗳️ Pengumuman Bakal Calon Ketua & Wakil Ketua OSIS Periode 2026/2027",
            author = "Komisi Pemilihan OSIS (KPO)",
            category = "Pemilu OSIS",
            date = "22 Agustus 2026",
            content = "Debat Terbuka Visi & Misi Paslon akan diselenggarakan pada hari Kamis di Auditorium Utama. Seluruh siswa wajib memberikan hak suara secara digital melalui menu E-Voting SuperApp.",
            likesCount = 189
        )
    )
}
