package com.sultanagung1.sista.ui.counseling

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.core.storage.FormDraftStore
import com.sultanagung1.sista.core.storage.SessionManager
import com.sultanagung1.sista.data.model.*
import com.sultanagung1.sista.data.repository.CounselingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    val draftIsConfidential: Boolean = true,
    // FASE 69.2: a persisted draft from a PREVIOUS session (full app close, not just
    // a process-death-and-restore) was found — the screen should offer to restore it.
    val restorableDraftAvailable: Boolean = false
)

/** Serialized shape written to [FormDraftStore] by [CounselingViewModel]. */
private data class CounselingDraftPayload(
    val studentId: String,
    val category: String,
    val notes: String,
    val actionPlan: String,
    val isConfidential: Boolean
)

@HiltViewModel
class CounselingViewModel @Inject constructor(
    private val repository: CounselingRepository,
    private val savedStateHandle: SavedStateHandle,
    private val formDraftStore: FormDraftStore,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val gson = Gson()
    private var cachedUserId: String? = null

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
        checkForRestorableDraft()
    }

    /** Only offers restore when SavedStateHandle came back empty — a live in-memory draft always wins. */
    private fun checkForRestorableDraft() {
        val state = _uiState.value
        val hasLiveDraft = state.draftNotes.isNotBlank() || state.draftActionPlan.isNotBlank() || state.draftStudentId.isNotBlank()
        if (hasLiveDraft) return
        viewModelScope.launch {
            cachedUserId = sessionManager.userIdFlow.first()
            val stored = formDraftStore.getDraft(FORM_ID) ?: return@launch
            _uiState.update { it.copy(restorableDraftAvailable = true) }
        }
    }

    fun restorePersistedDraft() {
        viewModelScope.launch {
            val stored = formDraftStore.getDraft(FORM_ID) ?: run {
                _uiState.update { it.copy(restorableDraftAvailable = false) }
                return@launch
            }
            val payload = try {
                gson.fromJson(stored.payloadJson, CounselingDraftPayload::class.java)
            } catch (_: Exception) {
                null
            }
            if (payload != null) {
                updateDraftStudentId(payload.studentId)
                updateDraftCategory(payload.category)
                updateDraftNotes(payload.notes)
                updateDraftActionPlan(payload.actionPlan)
                updateDraftConfidential(payload.isConfidential)
            }
            _uiState.update { it.copy(restorableDraftAvailable = false) }
        }
    }

    fun discardPersistedDraft() {
        formDraftStore.clearDraft(FORM_ID)
        _uiState.update { it.copy(restorableDraftAvailable = false) }
    }

    private fun persistDraftSnapshot() {
        val state = _uiState.value
        val payload = CounselingDraftPayload(
            studentId = state.draftStudentId,
            category = state.draftCategory,
            notes = state.draftNotes,
            actionPlan = state.draftActionPlan,
            isConfidential = state.draftIsConfidential
        )
        formDraftStore.autoSave(FORM_ID, cachedUserId, gson.toJson(payload))
    }

    fun updateDraftStudentId(value: String) {
        savedStateHandle[KEY_DRAFT_STUDENT_ID] = value
        _uiState.update { it.copy(draftStudentId = value) }
        persistDraftSnapshot()
    }

    fun updateDraftCategory(value: String) {
        savedStateHandle[KEY_DRAFT_CATEGORY] = value
        _uiState.update { it.copy(draftCategory = value) }
        persistDraftSnapshot()
    }

    fun updateDraftNotes(value: String) {
        savedStateHandle[KEY_DRAFT_NOTES] = value
        _uiState.update { it.copy(draftNotes = value) }
        persistDraftSnapshot()
    }

    fun updateDraftActionPlan(value: String) {
        savedStateHandle[KEY_DRAFT_ACTION_PLAN] = value
        _uiState.update { it.copy(draftActionPlan = value) }
        persistDraftSnapshot()
    }

    fun updateDraftConfidential(value: Boolean) {
        savedStateHandle[KEY_DRAFT_CONFIDENTIAL] = value
        _uiState.update { it.copy(draftIsConfidential = value) }
        persistDraftSnapshot()
    }

    private fun clearDraft() {
        savedStateHandle.remove<String>(KEY_DRAFT_STUDENT_ID)
        savedStateHandle.remove<String>(KEY_DRAFT_CATEGORY)
        savedStateHandle.remove<String>(KEY_DRAFT_NOTES)
        savedStateHandle.remove<String>(KEY_DRAFT_ACTION_PLAN)
        savedStateHandle.remove<Boolean>(KEY_DRAFT_CONFIDENTIAL)
        formDraftStore.clearDraft(FORM_ID)
        _uiState.update {
            it.copy(
                draftStudentId = "",
                draftCategory = "akademik",
                draftNotes = "",
                draftActionPlan = "",
                draftIsConfidential = true,
                restorableDraftAvailable = false
            )
        }
    }

    private companion object {
        const val KEY_DRAFT_STUDENT_ID = "counseling_draft_student_id"
        const val KEY_DRAFT_CATEGORY = "counseling_draft_category"
        const val KEY_DRAFT_NOTES = "counseling_draft_notes"
        const val KEY_DRAFT_ACTION_PLAN = "counseling_draft_action_plan"
        const val KEY_DRAFT_CONFIDENTIAL = "counseling_draft_confidential"
        const val FORM_ID = "counseling_session_form"
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
                when (result) {
                    is NetworkResult.Success -> _uiState.update { it.copy(studentAppointments = result.data) }
                    is NetworkResult.Error -> _uiState.update { it.copy(errorMessage = result.message) }
                    is NetworkResult.Loading -> Unit
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
