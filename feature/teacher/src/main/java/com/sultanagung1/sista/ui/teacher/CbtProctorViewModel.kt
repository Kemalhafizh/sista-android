package com.sultanagung1.sista.ui.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.core.ui.text.UiText
import com.sultanagung1.sista.core.ui.text.asUiText
import com.sultanagung1.sista.data.model.CbtTokenInfoResponse
import com.sultanagung1.sista.data.model.LockedExamStudent
import com.sultanagung1.sista.data.repository.CbtRepository
import com.sultanagung1.sista.feature.teacher.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CbtProctorUiState(
    val isLoading: Boolean = false,
    val token: CbtTokenInfoResponse? = null,
    /** The server's message when the token could not be loaded or renewed. */
    val errorMessage: String? = null,
    val lockedLoading: Boolean = true,
    val lockedStudents: List<LockedExamStudent> = emptyList(),
    val lockedError: String? = null,
    /** The student whose access is being reopened right now. */
    val unlockingStudentId: Long? = null,
    /** The outcome of the last unlock: who was let back in, or the server's refusal. */
    val unlockMessage: UiText? = null,
    val unlockFailed: Boolean = false,
)

/**
 * The operator's view of one exam (Screen.TeacherProctor): the entry token from
 * `teacher/cbt/exams/{id}/token`, and the students the anti-cheat locked out
 * (`locked-students`), each of whom can be let back in. There is no live
 * progress feed on the server, so none is shown.
 */
@HiltViewModel
class CbtProctorViewModel @Inject constructor(
    private val cbtRepository: CbtRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CbtProctorUiState())
    val uiState: StateFlow<CbtProctorUiState> = _uiState.asStateFlow()

    fun load(examId: Long) {
        loadToken(examId)
        loadLockedStudents(examId)
    }

    fun loadToken(examId: Long) {
        viewModelScope.launch {
            cbtRepository.getProctorToken(examId).collect(::onToken)
        }
    }

    fun regenerateToken(examId: Long) {
        viewModelScope.launch {
            cbtRepository.regenerateProctorToken(examId).collect(::onToken)
        }
    }

    private fun onToken(result: NetworkResult<CbtTokenInfoResponse>) {
        when (result) {
            is NetworkResult.Loading -> _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            is NetworkResult.Success -> _uiState.update { it.copy(isLoading = false, token = result.data) }
            is NetworkResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
        }
    }

    fun loadLockedStudents(examId: Long) {
        viewModelScope.launch {
            cbtRepository.getLockedStudents(examId).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.update { it.copy(lockedLoading = true, lockedError = null) }
                    is NetworkResult.Success -> _uiState.update { it.copy(lockedLoading = false, lockedStudents = result.data) }
                    is NetworkResult.Error -> _uiState.update { it.copy(lockedLoading = false, lockedError = result.message) }
                }
            }
        }
    }

    /** Reopens [student]'s attempt; on success they leave the list and can enter the token again. */
    fun unlock(examId: Long, student: LockedExamStudent) {
        if (_uiState.value.unlockingStudentId != null) return
        _uiState.update { it.copy(unlockingStudentId = student.studentId, unlockMessage = null) }
        viewModelScope.launch {
            cbtRepository.resetStudentAttempt(examId, student.studentId).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> Unit
                    is NetworkResult.Success -> _uiState.update { state ->
                        state.copy(
                            unlockingStudentId = null,
                            lockedStudents = state.lockedStudents.filterNot { it.studentId == student.studentId },
                            unlockMessage = student.name?.let { UiText.Res(R.string.pr_unlocked, it) }
                                ?: UiText.Res(R.string.pr_unlocked_unnamed),
                            unlockFailed = false,
                        )
                    }
                    is NetworkResult.Error -> _uiState.update {
                        it.copy(unlockingStudentId = null, unlockMessage = result.message.asUiText(), unlockFailed = true)
                    }
                }
            }
        }
    }

    fun clearUnlockMessage() {
        _uiState.update { it.copy(unlockMessage = null) }
    }
}
