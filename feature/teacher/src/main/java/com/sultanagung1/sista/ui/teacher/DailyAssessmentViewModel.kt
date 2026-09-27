package com.sultanagung1.sista.ui.teacher

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.*
import com.sultanagung1.sista.data.repository.DailyAssessmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DailyAssessmentUiState(
    val isLoading: Boolean = false,
    val assessments: List<DailyAssessmentItem> = emptyList(),
    val remedials: List<RemedialItem> = emptyList(),
    val studentHistory: List<StudentAssessmentHistoryItem> = emptyList(),
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)


@HiltViewModel
class DailyAssessmentViewModel @Inject constructor(
    private val repository: DailyAssessmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyAssessmentUiState())
    val uiState: StateFlow<DailyAssessmentUiState> = _uiState.asStateFlow()

    init {
        fetchTeacherAssessments()
    }

    fun fetchTeacherAssessments() {
        viewModelScope.launch {
            repository.getTeacherAssessments().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                    is NetworkResult.Success -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        assessments = result.data,
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

    fun fetchStudentRemedials() {
        viewModelScope.launch {
            repository.getStudentRemedials().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                    is NetworkResult.Success -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        remedials = result.data,
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

    fun fetchStudentHistory() {
        viewModelScope.launch {
            repository.getStudentAssessmentHistory().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                    is NetworkResult.Success -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        studentHistory = result.data,
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

    fun submitScores(assessmentId: Long, scores: List<StudentScoreInput>) {
        viewModelScope.launch {
            repository.submitBatchScores(assessmentId, scores).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.value = _uiState.value.copy(isSubmitting = true)
                    is NetworkResult.Success -> _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        successMessage = "Nilai ${result.data.totalSaved} siswa berhasil disimpan. ${result.data.studentsUnderKkm} siswa terdeteksi di bawah KKM."
                    )
                    is NetworkResult.Error -> _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun triggerAutoRemedial(assessmentId: Long, deadline: String? = null) {
        viewModelScope.launch {
            repository.autoAssignRemedial(assessmentId, deadline).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.value = _uiState.value.copy(isSubmitting = true)
                    is NetworkResult.Success -> _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        successMessage = result.data
                    )
                    is NetworkResult.Error -> _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
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
