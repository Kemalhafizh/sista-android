package com.sultanagung1.sista.ui.admin.sessions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.mvi.MviViewModel
import com.sultanagung1.sista.core.mvi.UiEffect
import com.sultanagung1.sista.core.mvi.UiEvent
import com.sultanagung1.sista.core.mvi.UiState
import com.sultanagung1.sista.core.storage.SessionManager
import com.sultanagung1.sista.data.model.ClassSessionDto
import com.sultanagung1.sista.data.model.ClassSessionErrorKind
import com.sultanagung1.sista.data.model.ClassSessionResult
import com.sultanagung1.sista.data.model.ClassSessionRules
import com.sultanagung1.sista.data.model.SessionAttendanceDto
import com.sultanagung1.sista.data.model.SessionAttendanceStatus
import com.sultanagung1.sista.data.repository.ClassSessionRepository
import com.sultanagung1.sista.ui.navigation.UserRoles
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/** One row's unsaved correction. */
data class OverrideDraft(
    val target: SessionAttendanceStatus? = null,
    val reason: String = ""
) {
    val canSubmit: Boolean get() = target != null && ClassSessionRules.isOverrideReasonValid(reason)
}

data class AdminAttendanceOverrideState(
    val sessionId: Long = 0,
    val session: ClassSessionDto? = null,
    val rows: List<SessionAttendanceDto> = emptyList(),
    val drafts: Map<Long, OverrideDraft> = emptyMap(),
    val savingAttendanceId: Long? = null,
    val canCorrect: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val notDeployed: Boolean = false
) : UiState {
    /** Rows that can still be corrected first (alpha, then sakit/izin/telat), then by name. */
    val sortedRows: List<SessionAttendanceDto>
        get() = rows.sortedWith(
            compareBy<SessionAttendanceDto> { ORDER.indexOf(it.effectiveStatus) }
                .thenBy { it.studentName.orEmpty().lowercase() }
        )

    fun draftOf(attendanceId: Long): OverrideDraft = drafts[attendanceId] ?: OverrideDraft()

    private companion object {
        val ORDER = listOf(
            SessionAttendanceStatus.ALPHA,
            SessionAttendanceStatus.SAKIT,
            SessionAttendanceStatus.IZIN,
            SessionAttendanceStatus.TELAT,
            SessionAttendanceStatus.HADIR
        )
    }
}

sealed interface AdminAttendanceOverrideEvent : UiEvent {
    data object ScreenStarted : AdminAttendanceOverrideEvent
    data object Refresh : AdminAttendanceOverrideEvent
    data class TargetSelected(val attendanceId: Long, val target: SessionAttendanceStatus) : AdminAttendanceOverrideEvent
    data class ReasonChanged(val attendanceId: Long, val reason: String) : AdminAttendanceOverrideEvent
    data class Submit(val attendanceId: Long) : AdminAttendanceOverrideEvent
    data class Cancel(val attendanceId: Long) : AdminAttendanceOverrideEvent
}

sealed interface AdminAttendanceOverrideEffect : UiEffect {
    data class ShowMessage(val message: String) : AdminAttendanceOverrideEffect
}

/**
 * FASE 77.6.2: correct a recorded attendance, one student at a time, always
 * with a reason (audit log). The allowed transitions come from
 * [ClassSessionRules.overrideTargets]; the server enforces the same rule.
 */
@HiltViewModel
class AdminAttendanceOverrideViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ClassSessionRepository,
    sessionManager: SessionManager
) : MviViewModel<AdminAttendanceOverrideState, AdminAttendanceOverrideEvent, AdminAttendanceOverrideEffect>(
    AdminAttendanceOverrideState(sessionId = savedStateHandle.get<Long>("sessionId") ?: 0L)
) {

    private var started = false

    init {
        setState { copy(session = repository.cachedAdminSession(sessionId)) }
        viewModelScope.launch {
            sessionManager.userRoleFlow.collect { role ->
                setState { copy(canCorrect = UserRoles.canCorrectClassAttendance(role)) }
            }
        }
    }

    override fun onEvent(event: AdminAttendanceOverrideEvent) {
        when (event) {
            AdminAttendanceOverrideEvent.ScreenStarted -> if (!started) {
                started = true
                load()
            }
            AdminAttendanceOverrideEvent.Refresh -> load()
            is AdminAttendanceOverrideEvent.TargetSelected -> updateDraft(event.attendanceId) { copy(target = event.target) }
            is AdminAttendanceOverrideEvent.ReasonChanged -> updateDraft(event.attendanceId) { copy(reason = event.reason.take(500)) }
            is AdminAttendanceOverrideEvent.Cancel -> setState { copy(drafts = drafts - event.attendanceId) }
            is AdminAttendanceOverrideEvent.Submit -> submit(event.attendanceId)
        }
    }

    private fun updateDraft(attendanceId: Long, change: OverrideDraft.() -> OverrideDraft) {
        setState { copy(drafts = drafts + (attendanceId to draftOf(attendanceId).change())) }
    }

    private fun load() {
        viewModelScope.launch {
            when (val result = repository.getSessionAttendances(currentState.sessionId)) {
                is ClassSessionResult.Success -> setState {
                    copy(rows = result.data, isLoading = false, errorMessage = null, notDeployed = false)
                }
                is ClassSessionResult.Failure -> setState {
                    copy(
                        isLoading = false,
                        errorMessage = ClassSessionRules.genericMessage(result.error),
                        notDeployed = result.error.kind == ClassSessionErrorKind.NOT_DEPLOYED
                    )
                }
            }
        }
    }

    private fun submit(attendanceId: Long) {
        val draft = currentState.draftOf(attendanceId)
        val target = draft.target
        val row = currentState.rows.firstOrNull { it.id == attendanceId } ?: return
        if (!currentState.canCorrect || target == null || currentState.savingAttendanceId != null) return
        // Same rule as the server; the UI never offers anything else, but a stale row could.
        if (target !in ClassSessionRules.overrideTargets(row.effectiveStatus)) return
        if (!ClassSessionRules.isOverrideReasonValid(draft.reason)) return

        setState { copy(savingAttendanceId = attendanceId) }
        viewModelScope.launch {
            when (val result = repository.overrideAttendance(attendanceId, target, draft.reason)) {
                is ClassSessionResult.Success -> {
                    // Keep the identity fields if the server echoes a partial row.
                    val updated = result.data.copy(
                        id = attendanceId,
                        studentId = result.data.studentId.takeIf { it != 0L } ?: row.studentId,
                        studentName = result.data.studentName ?: row.studentName,
                        studentNis = result.data.studentNis ?: row.studentNis
                    )
                    setState {
                        copy(
                            rows = rows.map { if (it.id == attendanceId) updated else it },
                            drafts = drafts - attendanceId,
                            savingAttendanceId = null
                        )
                    }
                    emitEffect {
                        AdminAttendanceOverrideEffect.ShowMessage(
                            "Kehadiran ${row.studentName.orEmpty()} dikoreksi menjadi ${ClassSessionRules.label(updated.effectiveStatus)}."
                        )
                    }
                }
                is ClassSessionResult.Failure -> {
                    setState { copy(savingAttendanceId = null) }
                    emitEffect { AdminAttendanceOverrideEffect.ShowMessage(ClassSessionRules.overrideFailureMessage(result.error)) }
                }
            }
        }
    }
}
