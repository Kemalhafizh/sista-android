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
import com.sultanagung1.sista.data.model.ScheduleRules
import com.sultanagung1.sista.core.util.DateUtils
import kotlinx.coroutines.Job
import java.util.Calendar
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
    val selectedDay: String = ScheduleRules.defaultDay(DateUtils.nowCalendar().get(Calendar.DAY_OF_WEEK)),
    /** True until `student/schedule` has fully answered (cache first, then network) — drives the pull-to-refresh spinner. */
    val isScheduleRefreshing: Boolean = false,
    /** Why the timetable could not be loaded (kept apart from grade errors). */
    val scheduleErrorMessage: String? = null,
    val allSchedules: List<ScheduleItem> = emptyList(),
    val grades: List<GradeEntry> = emptyList(),
    /** True until `student/grades` has answered (cache first, then network). */
    val isGradesLoading: Boolean = false,
    /** Why the grades could not be loaded (kept apart from schedule errors). */
    val gradesErrorMessage: String? = null
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

    private var scheduleJob: Job? = null

    fun loadSchedule() {
        scheduleJob?.cancel()
        scheduleJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isScheduleRefreshing = true)
            try {
                collectSchedule()
            } finally {
                _uiState.value = _uiState.value.copy(isScheduleRefreshing = false)
            }
        }
    }

    private suspend fun collectSchedule() {
        studentRepository.getSchedule().collect { result ->
            when (result) {
                is NetworkResult.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    allSchedules = result.data,
                    scheduleErrorMessage = null
                )
                is NetworkResult.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    scheduleErrorMessage = result.message
                )
                is NetworkResult.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
            }
        }
    }

    private var gradesJob: Job? = null

    fun loadGrades() {
        gradesJob?.cancel()
        gradesJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGradesLoading = true)
            try {
                studentRepository.getGrades().collect { result ->
                    when (result) {
                        is NetworkResult.Success -> _uiState.value = _uiState.value.copy(
                            grades = result.data,
                            gradesErrorMessage = null
                        )
                        is NetworkResult.Error -> _uiState.value = _uiState.value.copy(gradesErrorMessage = result.message)
                        is NetworkResult.Loading -> Unit
                    }
                }
            } finally {
                _uiState.value = _uiState.value.copy(isGradesLoading = false)
            }
        }
    }
}
