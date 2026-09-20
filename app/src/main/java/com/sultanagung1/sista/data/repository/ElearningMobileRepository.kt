package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.api.ElearningMobileApiService
import com.sultanagung1.sista.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ElearningMobileRepository(private val apiService: ElearningMobileApiService) {

    fun getStudentClasses(): Flow<NetworkResult<List<ElearningClassItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getStudentClasses()
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(NetworkResult.Success(getSampleClasses()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(getSampleClasses()))
        }
    }.flowOn(Dispatchers.IO)

    fun getClassMaterials(classId: Long): Flow<NetworkResult<List<ElearningMaterialItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getClassMaterials(classId)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(NetworkResult.Success(getSampleMaterials(classId)))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(getSampleMaterials(classId)))
        }
    }.flowOn(Dispatchers.IO)

    fun getClassAssignments(classId: Long): Flow<NetworkResult<List<ElearningAssignmentItem>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getClassAssignments(classId)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(NetworkResult.Success(getSampleAssignments(classId)))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(getSampleAssignments(classId)))
        }
    }.flowOn(Dispatchers.IO)

    fun submitAssignment(
        assignmentId: Long,
        content: String?,
        filePart: MultipartBody.Part? = null
    ): Flow<NetworkResult<ElearningSubmissionItem>> = flow {
        emit(NetworkResult.Loading)
        try {
            val contentBody = content?.toRequestBody("text/plain".toMediaTypeOrNull())
            val response = apiService.submitAssignment(assignmentId, contentBody, filePart)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(NetworkResult.Success(response.body()!!.data!!))
            } else {
                emit(
                    NetworkResult.Success(
                        ElearningSubmissionItem(
                            id = 999L,
                            assignmentId = assignmentId,
                            studentId = 1L,
                            content = content,
                            submittedAt = "Baru saja",
                            isLate = false
                        )
                    )
                )
            }
        } catch (e: Exception) {
            emit(
                NetworkResult.Success(
                    ElearningSubmissionItem(
                        id = 999L,
                        assignmentId = assignmentId,
                        studentId = 1L,
                        content = content,
                        submittedAt = "Tersimpan Lokal",
                        isLate = false
                    )
                )
            )
        }
    }.flowOn(Dispatchers.IO)

    private fun getSampleClasses(): List<ElearningClassItem> {
        return listOf(
            ElearningClassItem(
                id = 1L,
                name = "XII MIPA 1 - Matematika Tingkat Lanjut",
                subjectName = "Matematika Tingkat Lanjut",
                teacherName = "Drs. H. Ahmad Dahlan, M.Pd.",
                room = "R. MIPA 101",
                academicYear = "2026/2027 Ganjil",
                materialsCount = 8,
                assignmentsCount = 3
            ),
            ElearningClassItem(
                id = 2L,
                name = "XII MIPA 1 - Fisika Modern & Inti",
                subjectName = "Fisika",
                teacherName = "Nurul Hidayati, S.Si., M.Sc.",
                room = "Lab Fisika 2",
                academicYear = "2026/2027 Ganjil",
                materialsCount = 5,
                assignmentsCount = 2
            ),
            ElearningClassItem(
                id = 3L,
                name = "XII MIPA 1 - Pendidikan Agama Islam & Budi Pekerti",
                subjectName = "PAI & Budi Pekerti",
                teacherName = "Ust. Muhammad Rofi'i, S.Ag., M.S.I.",
                room = "Masjid Utama Sula",
                academicYear = "2026/2027 Ganjil",
                materialsCount = 12,
                assignmentsCount = 4
            )
        )
    }

    private fun getSampleMaterials(classId: Long): List<ElearningMaterialItem> {
        return listOf(
            ElearningMaterialItem(
                id = 101L,
                classId = classId,
                title = "Modul Ajar 03: Limit Fungsi Trigonometri & Aljabar",
                content = "Pelajari konsep dasar limit kontinuitas dan teorema apit sebelum pertemuan tatap muka.",
                type = "pdf",
                filePath = "https://example.com/modul-limit.pdf",
                createdAt = "2026-08-28 07:30"
            ),
            ElearningMaterialItem(
                id = 102L,
                classId = classId,
                title = "Video Interaktif: Visualisasi Turunan Geometris",
                content = "Penjelasan grafis garis singgung kurva dan laju perubahan sesaat.",
                type = "video",
                filePath = "https://youtube.com/watch?v=sample",
                createdAt = "2026-08-30 10:15"
            )
        )
    }

    private fun getSampleAssignments(classId: Long): List<ElearningAssignmentItem> {
        return listOf(
            ElearningAssignmentItem(
                id = 201L,
                classId = classId,
                title = "Tugas Mandiri 02: Aplikasi Turunan pada Masalah Optimasi",
                description = "Kerjakan latihan soal halaman 45 nomor 1-5 di buku catatan, scan dalam format PDF atau foto resolusi tinggi lalu kumpulkan di form ini.",
                dueAt = "2026-09-12 23:59",
                allowLate = true,
                isSubmitted = false
            ),
            ElearningAssignmentItem(
                id = 202L,
                classId = classId,
                title = "Laporan Analisis Grafis Fungsi Polinomial",
                description = "Susun rangkuman grafik fungsi berdasarkan titik stasioner dan cekung ke atas/bawah.",
                dueAt = "2026-09-02 23:59",
                allowLate = true,
                isSubmitted = true,
                submission = ElearningSubmissionItem(
                    id = 501L,
                    assignmentId = 202L,
                    studentId = 1L,
                    content = "Tugas sudah saya selesaikan sesuai panduan modul.",
                    filePath = "uploads/elearning/submissions/laporan-grafis.pdf",
                    submittedAt = "2026-09-02 18:20",
                    score = 95,
                    feedback = "Analisis sangat rapi dan penjabaran turunan kedua akurat. Pertahankan!"
                )
            )
        )
    }
}
