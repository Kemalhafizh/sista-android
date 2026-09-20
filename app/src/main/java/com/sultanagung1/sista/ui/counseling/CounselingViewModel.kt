package com.sultanagung1.sista.ui.counseling

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.*
import com.sultanagung1.sista.data.repository.CounselingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CounselingUiState(
    val isLoading: Boolean = false,
    val dashboardData: CounselorDashboardData? = null,
    val atRiskStudents: List<CounselingAtRiskStudentItem> = emptyList(),
    val studentAppointments: List<CounselingSessionItem> = emptyList(),
    val isSubmitting: Boolean = false,
    val sessionCreatedSuccess: Boolean = false,
    val appointmentRequestedSuccess: Boolean = false,
    val errorMessage: String? = null
)


@HiltViewModel
class CounselingViewModel @Inject constructor(
    private val repository: CounselingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CounselingUiState())
    val uiState: StateFlow<CounselingUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
        loadStudentAppointments()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            repository.getCounselorDashboard().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is NetworkResult.Success -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            dashboardData = result.data,
                            atRiskStudents = result.data.atRiskStudents
                        )
                    }
                    is NetworkResult.Error -> _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun loadStudentAppointments() {
        viewModelScope.launch {
            repository.getMyStudentAppointments().collect { result ->
                if (result is NetworkResult.Success) {
                    _uiState.update { it.copy(studentAppointments = result.data) }
                }
            }
        }
    }

    fun createSession(
        studentId: Long,
        category: String,
        notes: String,
        actionPlan: String,
        isConfidential: Boolean,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val request = CreateCounselingSessionRequest(
                studentId = studentId,
                category = category,
                notes = notes,
                actionPlan = actionPlan,
                isConfidential = isConfidential
            )
            repository.createSession(request).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _uiState.update {
                            it.copy(isSubmitting = false, sessionCreatedSuccess = true)
                        }
                        onSuccess()
                    }
                    is NetworkResult.Error -> {
                        _uiState.update {
                            it.copy(isSubmitting = false, errorMessage = result.message)
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun requestAppointment(
        topic: String,
        preferredDate: String,
        category: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val request = RequestAppointmentRequest(
                topic = topic,
                preferredDate = preferredDate,
                category = category
            )
            repository.requestAppointment(request).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _uiState.update {
                            val updated = it.studentAppointments.toMutableList()
                            updated.add(0, result.data)
                            it.copy(
                                isSubmitting = false,
                                appointmentRequestedSuccess = true,
                                studentAppointments = updated
                            )
                        }
                        onSuccess()
                    }
                    is NetworkResult.Error -> {
                        _uiState.update {
                            it.copy(isSubmitting = false, errorMessage = result.message)
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}
