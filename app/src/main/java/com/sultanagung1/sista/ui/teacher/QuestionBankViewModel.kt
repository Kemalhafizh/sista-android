package com.sultanagung1.sista.ui.teacher

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.*
import com.sultanagung1.sista.data.repository.QuestionBankRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QuestionBankUiState(
    val isLoading: Boolean = false,
    val categories: List<QuestionBankCategory> = emptyList(),
    val generatedExam: AutoGenerateExamResponse? = null,
    val isGenerating: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)


@HiltViewModel
class QuestionBankViewModel @Inject constructor(
    private val repository: QuestionBankRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuestionBankUiState())
    val uiState: StateFlow<QuestionBankUiState> = _uiState.asStateFlow()

    init {
        fetchCategories()
    }

    fun fetchCategories(subjectId: Long? = null) {
        viewModelScope.launch {
            repository.getCategories(subjectId).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                    is NetworkResult.Success -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        categories = result.data,
                        errorMessage = null
                    )
                    is NetworkResult.Error -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun autoGenerate(request: AutoGenerateExamRequest) {
        viewModelScope.launch {
            repository.autoGenerateExam(request).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.value = _uiState.value.copy(isGenerating = true)
                    is NetworkResult.Success -> _uiState.value = _uiState.value.copy(
                        isGenerating = false,
                        generatedExam = result.data,
                        successMessage = "Ujian berhasil digenerate (${result.data.totalGenerated} butir soal)"
                    )
                    is NetworkResult.Error -> _uiState.value = _uiState.value.copy(
                        isGenerating = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
