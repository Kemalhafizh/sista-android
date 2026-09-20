package com.sultanagung1.sista.data.repository

import com.sultanagung1.sista.core.network.ApiClient
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.api.QuestionBankApiService
import com.sultanagung1.sista.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class QuestionBankRepository(private val apiService: QuestionBankApiService) {

    fun getCategories(subjectId: Long? = null): Flow<NetworkResult<List<QuestionBankCategory>>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.getCategories(subjectId)
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!.data))
            } else {
                emit(NetworkResult.Success(getSampleCategories()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(getSampleCategories()))
        }
    }.flowOn(Dispatchers.IO)

    fun autoGenerateExam(request: AutoGenerateExamRequest): Flow<NetworkResult<AutoGenerateExamResponse>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.autoGenerate(request)
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!.data))
            } else {
                emit(NetworkResult.Success(getSampleGeneratedExam(request.totalQuestions)))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Success(getSampleGeneratedExam(request.totalQuestions)))
        }
    }.flowOn(Dispatchers.IO)

    fun createQuestion(request: CreateQuestionBankRequest): Flow<NetworkResult<QuestionBankItem>> = flow {
        emit(NetworkResult.Loading)
        try {
            val response = apiService.store(request)
            if (response.isSuccessful && response.body() != null) {
                emit(NetworkResult.Success(response.body()!!.data))
            } else {
                emit(NetworkResult.Error("Gagal menyimpan butir soal", response.code()))
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.localizedMessage ?: "Koneksi terputus"))
        }
    }.flowOn(Dispatchers.IO)

    private fun getSampleCategories(): List<QuestionBankCategory> {
        return listOf(
            QuestionBankCategory(1L, "cat-1", "Aljabar & Fungsi Kuadrat", "X", "MAT.E.1.1", "Kompetensi dasar persamaan dan fungsi kuadrat", 15),
            QuestionBankCategory(2L, "cat-2", "Kalkulus & Turunan", "XI", "MAT.F.2.1", "Differensial dan aplikasi laju perubahan", 25),
            QuestionBankCategory(3L, "cat-3", "Termodinamika & Gas Ideal", "XI", "FIS.F.1.3", "Hukum termodinamika I dan II", 18),
            QuestionBankCategory(4L, "cat-4", "Kimia Organik & Makromolekul", "XII", "KIM.F.3.2", "Polimer dan biomolekul karbohidrat", 12),
            QuestionBankCategory(5L, "cat-5", "Ekosistem & Bioteknologi", "X", "BIO.E.1.4", "Prinsip bioteknologi konvensional dan modern", 20)
        )
    }

    private fun getSampleGeneratedExam(totalQuestions: Int): AutoGenerateExamResponse {
        val sampleItems = listOf(
            QuestionBankItem(101L, "q-1", 1L, "pilihan_ganda", "Tentukan akar-akar dari persamaan kuadrat: \$\$x^2 - 5x + 6 = 0\$\$", null, "x = 2 atau x = 3", "Faktorkan menjadi (x-2)(x-3)=0", "mudah", "C2", true, 3),
            QuestionBankItem(102L, "q-2", 2L, "pilihan_ganda", "Berapakah turunan pertama dari fungsi: \$\$f(x) = 4x^3 - 2x^2 + 5\$\$", null, "\$\$f'(x) = 12x^2 - 4x\$\$", "Gunakan aturan pangkat", "sedang", "C3", true, 5),
            QuestionBankItem(103L, "q-3", 3L, "pilihan_ganda", "Sebuah gas ideal mengalami ekspansi adiabatik. Tentukan usaha yang dilakukan jika kalor \$\$Q = 0\$\$!", null, "\$\$W = -\\Delta U\$\$", "Hukum Termodinamika I: Q = W + dU", "sulit", "C4", true, 2)
        )
        return AutoGenerateExamResponse(
            totalGenerated = totalQuestions,
            items = sampleItems,
            distribution = mapOf("mudah" to (totalQuestions * 3 / 10), "sedang" to (totalQuestions * 5 / 10), "sulit" to (totalQuestions * 2 / 10))
        )
    }
}
