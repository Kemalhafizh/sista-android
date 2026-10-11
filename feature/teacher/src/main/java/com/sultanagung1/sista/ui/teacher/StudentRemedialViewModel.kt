package com.sultanagung1.sista.ui.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.RemedialItem
import com.sultanagung1.sista.data.repository.DailyAssessmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentRemedialUiState(
    val isLoading: Boolean = true,
    /** Null until the server answered. */
    val remedials: List<RemedialItem>? = null,
    val errorMessage: String? = null,
)

/** "Remedial Saya": only GET assessments/student/remedials, the student's own. */
@HiltViewModel
class StudentRemedialViewModel @Inject constructor(
    private val repository: DailyAssessmentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentRemedialUiState())
    val uiState: StateFlow<StudentRemedialUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            repository.getStudentRemedials().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    is NetworkResult.Success -> _uiState.update {
                        it.copy(isLoading = false, remedials = StudentRemedialRules.ordered(result.data), errorMessage = null)
                    }
                    is NetworkResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }
}
