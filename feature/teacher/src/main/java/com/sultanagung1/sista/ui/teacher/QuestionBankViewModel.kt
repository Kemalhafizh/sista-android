package com.sultanagung1.sista.ui.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.AutoGenerateExamRequest
import com.sultanagung1.sista.data.model.AutoGenerateExamResponse
import com.sultanagung1.sista.data.model.QuestionBankCategory
import com.sultanagung1.sista.data.repository.QuestionBankRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuestionBankUiState(
    val isLoading: Boolean = true,
    val categories: List<QuestionBankCategory> = emptyList(),
    /** The server's message when the categories could not be loaded. */
    val errorMessage: String? = null,
    val isGenerating: Boolean = false,
    /** The questions the server picked for the last preview; nothing is saved. */
    val generatedExam: AutoGenerateExamResponse? = null,
    val generateError: String? = null,
)

/**
 * The question bank (`question-bank/categories`) and its preview: picking a
 * set of questions by difficulty with `question-bank/auto-generate`. Without
 * an exam id the server only returns the set; an exam is made in "Buat ujian".
 */
@HiltViewModel
class QuestionBankViewModel @Inject constructor(
    private val repository: QuestionBankRepository,
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
                    is NetworkResult.Loading -> _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    is NetworkResult.Success -> _uiState.update { it.copy(isLoading = false, categories = result.data) }
                    is NetworkResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun autoGenerate(request: AutoGenerateExamRequest) {
        viewModelScope.launch {
            repository.autoGenerateExam(request).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.update { it.copy(isGenerating = true, generateError = null) }
                    is NetworkResult.Success -> _uiState.update { it.copy(isGenerating = false, generatedExam = result.data) }
                    is NetworkResult.Error -> _uiState.update { it.copy(isGenerating = false, generateError = result.message) }
                }
            }
        }
    }
}
