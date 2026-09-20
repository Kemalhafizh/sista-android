package com.sultanagung1.sista.ui.counseling

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.SavedStateHandle
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
    val errorMessage: String? = null,
    // FASE 69.1: an in-progress counseling note is sensitive, hard-to-recreate
    // work — surviving process death instead of silently wiping it matters here.
    val draftStudentId: String = "",
    val draftCategory: String = "akademik",
    val draftNotes: String = "",
    val draftActionPlan: String = "",
    val draftIsConfidential: Boolean = true
)


@HiltViewModel
class CounselingViewModel @Inject constructor(
    private val repository: CounselingRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CounselingUiState(
            draftStudentId = savedStateHandle.get<String>(KEY_DRAFT_STUDENT_ID) ?: "",
            draftCategory = savedStateHandle.get<String>(KEY_DRAFT_CATEGORY) ?: "akademik",
            draftNotes = savedStateHandle.get<String>(KEY_DRAFT_NOTES) ?: "",
            draftActionPlan = savedStateHandle.get<String>(KEY_DRAFT_ACTION_PLAN) ?: "",
            draftIsConfidential = savedStateHandle.get<Boolean>(KEY_DRAFT_CONFIDENTIAL) ?: true
        )
    )
    val uiState: StateFlow<CounselingUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
        loadStudentAppointments()
    }

    fun updateDraftStudentId(value: String) {
        savedStateHandle[KEY_DRAFT_STUDENT_ID] = value
        _uiState.update { it.copy(draftStudentId = value) }
    }

    fun updateDraftCategory(value: String) {
        savedStateHandle[KEY_DRAFT_CATEGORY] = value
        _uiState.update { it.copy(draftCategory = value) }
    }

    fun updateDraftNotes(value: String) {
        savedStateHandle[KEY_DRAFT_NOTES] = value
        _uiState.update { it.copy(draftNotes = value) }
    }

    fun updateDraftActionPlan(value: String) {
        savedStateHandle[KEY_DRAFT_ACTION_PLAN] = value
        _uiState.update { it.copy(draftActionPlan = value) }
    }

    fun updateDraftConfidential(value: Boolean) {
        savedStateHandle[KEY_DRAFT_CONFIDENTIAL] = value
        _uiState.update { it.copy(draftIsConfidential = value) }
    }

    private fun clearDraft() {
        savedStateHandle.remove<String>(KEY_DRAFT_STUDENT_ID)
        savedStateHandle.remove<String>(KEY_DRAFT_CATEGORY)
        savedStateHandle.remove<String>(KEY_DRAFT_NOTES)
        savedStateHandle.remove<String>(KEY_DRAFT_ACTION_PLAN)
        savedStateHandle.remove<Boolean>(KEY_DRAFT_CONFIDENTIAL)
        _uiState.update {
            it.copy(
                draftStudentId = "",
                draftCategory = "akademik",
                draftNotes = "",
                draftActionPlan = "",
                draftIsConfidential = true
            )
        }
    }

    private companion object {
        const val KEY_DRAFT_STUDENT_ID = "counseling_draft_student_id"
        const val KEY_DRAFT_CATEGORY = "counseling_draft_category"
        const val KEY_DRAFT_NOTES = "counseling_draft_notes"
        const val KEY_DRAFT_ACTION_PLAN = "counseling_draft_action_plan"
        const val KEY_DRAFT_CONFIDENTIAL = "counseling_draft_confidential"
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
                        clearDraft()
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
