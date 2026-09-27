package com.sultanagung1.sista.ui.teacher

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.TeacherCbtExamItem
import com.sultanagung1.sista.data.repository.CbtRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TeacherProctorExamsUiState(
    val isLoading: Boolean = false,
    val hasLoaded: Boolean = false,
    val exams: List<TeacherCbtExamItem> = emptyList(),
    val errorMessage: String? = null,
    // Set once, after the first successful load, when exactly one of the
    // teacher's exams is ongoing — the screen jumps straight to its proctor view.
    val autoOpenExamId: Long? = null
)

/**
 * Backs Screen.TeacherProctorExams — the "Pengawas CBT" entry point. Lists the
 * exams this teacher really operates (GET teacher/cbt/exams: UTS/UAS as
 * operator, other types as creator), so the proctor screen is only ever opened
 * for a real exam id returned by the backend.
 */
@HiltViewModel
class TeacherProctorExamsViewModel @Inject constructor(
    private val cbtRepository: CbtRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherProctorExamsUiState())
    val uiState: StateFlow<TeacherProctorExamsUiState> = _uiState.asStateFlow()

    private var autoOpenDecided = false

    init {
        loadExams()
    }

    fun loadExams() {
        viewModelScope.launch {
            cbtRepository.getTeacherProctorExams().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                    is NetworkResult.Success -> {
                        val autoOpen = if (autoOpenDecided) null else singleOngoingExamId(result.data)
                        autoOpenDecided = true
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            hasLoaded = true,
                            exams = result.data,
                            autoOpenExamId = autoOpen
                        )
                    }
                    is NetworkResult.Error -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        hasLoaded = true,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun onAutoOpenConsumed() {
        _uiState.value = _uiState.value.copy(autoOpenExamId = null)
    }

    companion object {
        /** The id of the only ongoing exam, or null if there are zero or several. */
        fun singleOngoingExamId(exams: List<TeacherCbtExamItem>): Long? =
            exams.filter { it.isOngoing }.singleOrNull()?.id
    }
}
