package com.sultanagung1.sista.ui.academic

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.mvi.UiEffect
import com.sultanagung1.sista.core.mvi.UiEvent
import com.sultanagung1.sista.core.mvi.UiState
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.GradeEntry
import com.sultanagung1.sista.data.model.ScheduleItem
import com.sultanagung1.sista.data.repository.StudentRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AcademicUiEvent : UiEvent {
    data class SelectDay(val day: String) : AcademicUiEvent
    data object RefreshSchedule : AcademicUiEvent
    data object RefreshGrades : AcademicUiEvent
}

sealed interface AcademicUiEffect : UiEffect {
    data class ShowToast(val message: String) : AcademicUiEffect
}

data class AcademicUiState(
    val isLoading: Boolean = false,
    val selectedDay: String = "Senin",
    val allSchedules: List<ScheduleItem> = emptyList(),
    val grades: List<GradeEntry> = emptyList(),
    val errorMessage: String? = null
) : UiState


@HiltViewModel
class AcademicViewModel @Inject constructor(private val studentRepository: StudentRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AcademicUiState())
    val uiState: StateFlow<AcademicUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<AcademicUiEffect>()
    val effect: SharedFlow<AcademicUiEffect> = _effect.asSharedFlow()

    init {
        loadSchedule()
        loadGrades()
    }

    fun onEvent(event: AcademicUiEvent) {
        when (event) {
            is AcademicUiEvent.SelectDay -> selectDay(event.day)
            is AcademicUiEvent.RefreshSchedule -> loadSchedule()
            is AcademicUiEvent.RefreshGrades -> loadGrades()
        }
    }

    fun selectDay(day: String) {
        _uiState.value = _uiState.value.copy(selectedDay = day)
    }

    fun loadSchedule() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            studentRepository.getSchedule().collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            allSchedules = result.data
                        )
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                    is NetworkResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    fun loadGrades() {
        viewModelScope.launch {
            studentRepository.getGrades().collect { result ->
                when (result) {
                    is NetworkResult.Success -> _uiState.value = _uiState.value.copy(grades = result.data)
                    is NetworkResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }
}
