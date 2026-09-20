package com.sultanagung1.sista.ui.teacher

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.*
import com.sultanagung1.sista.data.repository.TeacherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TeacherUiState(
    val isLoading: Boolean = false,
    val dashboardData: TeacherDashboardData? = null,
    val activeClassStudents: List<StudentAttendanceInputItem> = emptyList(),
    val attendanceSubmittedSuccess: Boolean = false,
    val journalSavedSuccess: Boolean = false,
    val errorMessage: String? = null
)


@HiltViewModel
class TeacherViewModel @Inject constructor(private val teacherRepository: TeacherRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherUiState())
    val uiState: StateFlow<TeacherUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            teacherRepository.getDashboard().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                    is NetworkResult.Success -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        dashboardData = result.data,
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

    fun loadClassStudents(classId: String) {
        viewModelScope.launch {
            teacherRepository.getClassStudents(classId).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                    is NetworkResult.Success -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        activeClassStudents = result.data
                    )
                    is NetworkResult.Error -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun updateStudentStatus(nisn: String, newStatus: String) {
        val updated = _uiState.value.activeClassStudents.map {
            if (it.nisn == nisn) it.copy(status = newStatus) else it
        }
        _uiState.value = _uiState.value.copy(activeClassStudents = updated)
    }

    fun markAllPresent() {
        val updated = _uiState.value.activeClassStudents.map { it.copy(status = "Hadir") }
        _uiState.value = _uiState.value.copy(activeClassStudents = updated)
    }

    fun submitClassAttendance(scheduleId: String, classId: String) {
        viewModelScope.launch {
            val request = ClassAttendanceSubmitRequest(
                scheduleId = scheduleId,
                classId = classId,
                date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                attendances = _uiState.value.activeClassStudents
            )
            teacherRepository.submitClassAttendance(request).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                    is NetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            attendanceSubmittedSuccess = true
                        )
                        loadDashboard()
                    }
                    is NetworkResult.Error -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun storeTeachingJournal(
        scheduleId: String,
        className: String,
        subjectName: String,
        topic: String,
        competencyCode: String,
        notes: String
    ) {
        viewModelScope.launch {
            val request = TeachingJournalCreateRequest(
                scheduleId = scheduleId,
                className = className,
                subjectName = subjectName,
                topic = topic,
                competencyCode = competencyCode,
                notes = notes
            )
            teacherRepository.storeJournal(request).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                    is NetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            journalSavedSuccess = true
                        )
                        loadDashboard()
                    }
                    is NetworkResult.Error -> _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun resetFlags() {
        _uiState.value = _uiState.value.copy(
            attendanceSubmittedSuccess = false,
            journalSavedSuccess = false,
            errorMessage = null
        )
    }
}
