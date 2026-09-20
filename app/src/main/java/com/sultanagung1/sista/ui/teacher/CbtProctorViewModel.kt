package com.sultanagung1.sista.ui.teacher

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.CbtTokenInfoResponse
import com.sultanagung1.sista.data.repository.CbtRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CbtProctorUiState(
    val isLoading: Boolean = false,
    val token: CbtTokenInfoResponse? = null,
    val errorMessage: String? = null,
    val isResettingStudent: Boolean = false,
    val resetResultMessage: String? = null
)

/**
 * Backs the teacher's live-exam entry token (Screen.TeacherProctor). The backend
 * currently exposes token get/regenerate and a per-student force-close reset —
 * there is no live roster/progress-feed endpoint yet, so this screen intentionally
 * does not show a participant list (see TeacherProctorDashboardScreen).
 */
@HiltViewModel
class CbtProctorViewModel @Inject constructor(
    private val cbtRepository: CbtRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CbtProctorUiState())
    val uiState: StateFlow<CbtProctorUiState> = _uiState.asStateFlow()

    fun loadToken(examId: Long) {
        viewModelScope.launch {
            cbtRepository.getProctorToken(examId).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                    is NetworkResult.Success -> _uiState.value = _uiState.value.copy(isLoading = false, token = result.data)
                    is NetworkResult.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun regenerateToken(examId: Long) {
        viewModelScope.launch {
            cbtRepository.regenerateProctorToken(examId).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                    is NetworkResult.Success -> _uiState.value = _uiState.value.copy(isLoading = false, token = result.data)
                    is NetworkResult.Error -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun resetStudent(examId: Long, studentId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isResettingStudent = true, resetResultMessage = null)
            cbtRepository.resetStudentAttempt(examId, studentId).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> Unit
                    is NetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isResettingStudent = false,
                            resetResultMessage = "Siswa (ID $studentId) berhasil direset. Mereka dapat memasukkan token untuk masuk kembali."
                        )
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isResettingStudent = false,
                            resetResultMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun clearResetResult() {
        _uiState.value = _uiState.value.copy(resetResultMessage = null)
    }
}
