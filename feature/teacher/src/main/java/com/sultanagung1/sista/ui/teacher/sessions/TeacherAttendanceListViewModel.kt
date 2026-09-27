package com.sultanagung1.sista.ui.teacher.sessions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.mvi.MviViewModel
import com.sultanagung1.sista.core.mvi.UiEffect
import com.sultanagung1.sista.core.mvi.UiEvent
import com.sultanagung1.sista.core.mvi.UiState
import com.sultanagung1.sista.data.model.ClassSessionRejection
import com.sultanagung1.sista.data.model.ClassSessionDto
import com.sultanagung1.sista.data.model.ClassSessionErrorKind
import com.sultanagung1.sista.data.model.ClassSessionResult
import com.sultanagung1.sista.data.model.ClassSessionRules
import com.sultanagung1.sista.data.model.ClassSessionStatus
import com.sultanagung1.sista.data.model.SessionAttendanceDto
import com.sultanagung1.sista.data.model.SessionAttendanceStatus
import com.sultanagung1.sista.data.repository.ClassSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeacherAttendanceListState(
    val sessionId: Long = 0,
    val session: ClassSessionDto? = null,
    /** As the server has them; refreshed every 10 s. */
    val rows: List<SessionAttendanceDto> = emptyList(),
    /** The teacher's unsaved choices, kept apart so a refresh never overwrites them. */
    val edits: Map<Long, SessionAttendanceStatus> = emptyMap(),
    val query: String = "",
    val filter: SessionAttendanceStatus? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val notDeployed: Boolean = false,
    val isSaving: Boolean = false
) : UiState {
    /** 77.4.2: only an active session can be edited; afterwards it is Waka/TU's job. */
    val isEditable: Boolean get() = session?.effectiveStatus == ClassSessionStatus.ACTIVE

    fun statusOf(row: SessionAttendanceDto): SessionAttendanceStatus = edits[row.studentId] ?: row.effectiveStatus

    val pendingChanges: Map<Long, SessionAttendanceStatus>
        get() = ClassSessionRules.pendingChanges(rows.associate { it.studentId to it.effectiveStatus }, edits)

    val visibleRows: List<SessionAttendanceDto>
        get() = ClassSessionRules.filterStudents(rows, query, filter) { statusOf(it) }

    /** Counts as they will be after saving. */
    val previewCounts: ClassSessionRules.Counts
        get() = ClassSessionRules.countsOf(rows.map { it.copy(status = statusOf(it)) })
}

sealed interface TeacherAttendanceListEvent : UiEvent {
    data object ScreenStarted : TeacherAttendanceListEvent
    data object ScreenStopped : TeacherAttendanceListEvent
    data object Refresh : TeacherAttendanceListEvent
    data class QueryChanged(val query: String) : TeacherAttendanceListEvent
    data class FilterChanged(val filter: SessionAttendanceStatus?) : TeacherAttendanceListEvent
    data class Mark(val studentId: Long, val status: SessionAttendanceStatus) : TeacherAttendanceListEvent
    data object Save : TeacherAttendanceListEvent
    data object DiscardChanges : TeacherAttendanceListEvent
}

sealed interface TeacherAttendanceListEffect : UiEffect {
    data class ShowMessage(val message: String) : TeacherAttendanceListEffect
}

/**
 * FASE 77.4: the class list with each student's status. Marks are batched
 * locally and sent in one bulk request ("Simpan Perubahan (N diubah)").
 */
@HiltViewModel
class TeacherAttendanceListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ClassSessionRepository
) : MviViewModel<TeacherAttendanceListState, TeacherAttendanceListEvent, TeacherAttendanceListEffect>(
    TeacherAttendanceListState(sessionId = savedStateHandle.get<Long>("sessionId") ?: 0L)
) {

    private var pollJob: Job? = null

    override fun onEvent(event: TeacherAttendanceListEvent) {
        when (event) {
            TeacherAttendanceListEvent.ScreenStarted -> startPolling()
            TeacherAttendanceListEvent.ScreenStopped -> {
                pollJob?.cancel()
                pollJob = null
            }
            TeacherAttendanceListEvent.Refresh -> viewModelScope.launch {
                refreshSession()
                refreshRows()
            }
            is TeacherAttendanceListEvent.QueryChanged -> setState { copy(query = event.query) }
            is TeacherAttendanceListEvent.FilterChanged -> setState { copy(filter = event.filter) }
            is TeacherAttendanceListEvent.Mark -> if (currentState.isEditable) {
                setState { copy(edits = edits + (event.studentId to event.status)) }
            }
            TeacherAttendanceListEvent.Save -> save()
            TeacherAttendanceListEvent.DiscardChanges -> setState { copy(edits = emptyMap()) }
        }
    }

    private fun startPolling() {
        if (pollJob?.isActive == true) return
        pollJob = viewModelScope.launch {
            var tick = 0
            while (isActive) {
                if (tick % 3 == 0 && !refreshSession()) break
                refreshRows()
                tick++
                // A finished session no longer changes; one load is enough.
                if (currentState.session?.effectiveStatus?.isFinished == true) break
                delay(ClassSessionRules.ATTENDANCE_POLL_MS)
            }
        }
    }

    /** @return false when the backend is missing and polling should stop. */
    private suspend fun refreshSession(): Boolean {
        when (val result = repository.getTodaySessions()) {
            is ClassSessionResult.Success -> result.data.firstOrNull { it.sessionId == currentState.sessionId }?.let { found ->
                setState { copy(session = found) }
            }
            is ClassSessionResult.Failure -> if (result.error.kind == ClassSessionErrorKind.NOT_DEPLOYED) {
                setState { copy(isLoading = false, notDeployed = true, errorMessage = ClassSessionRules.genericMessage(result.error)) }
                return false
            }
        }
        return true
    }

    private suspend fun refreshRows() {
        when (val result = repository.getSessionStudents(currentState.sessionId)) {
            is ClassSessionResult.Success -> setState {
                copy(rows = result.data.sortedBy { it.studentName.orEmpty().lowercase() }, isLoading = false, errorMessage = null)
            }
            is ClassSessionResult.Failure -> setState {
                copy(
                    isLoading = false,
                    errorMessage = ClassSessionRules.genericMessage(result.error),
                    notDeployed = notDeployed || result.error.kind == ClassSessionErrorKind.NOT_DEPLOYED
                )
            }
        }
    }

    private fun save() {
        val changes = currentState.pendingChanges
        if (changes.isEmpty() || currentState.isSaving) return
        setState { copy(isSaving = true) }
        viewModelScope.launch {
            when (val result = repository.markAttendance(currentState.sessionId, changes)) {
                is ClassSessionResult.Success -> {
                    val r = result.data
                    setState { copy(isSaving = false, edits = edits - changes.keys) }
                    refreshRows()
                    emitEffect {
                        TeacherAttendanceListEffect.ShowMessage(
                            if (r.failed > 0) {
                                "${r.updated} tersimpan, ${r.failed} gagal" + (r.errors?.firstOrNull()?.let { ": $it" } ?: ".")
                            } else {
                                "${r.updated} perubahan kehadiran tersimpan."
                            }
                        )
                    }
                }
                is ClassSessionResult.Failure -> {
                    setState { copy(isSaving = false) }
                    if (result.error.rejection == ClassSessionRejection.MANUAL_NOT_ACTIVE) {
                        refreshSession()
                        emitEffect {
                            TeacherAttendanceListEffect.ShowMessage("Sesi sudah berakhir; perubahan tidak tersimpan. Koreksi lewat Waka Kurikulum/TU.")
                        }
                    } else {
                        // Edits stay on screen so the teacher can retry.
                        emitEffect { TeacherAttendanceListEffect.ShowMessage(ClassSessionRules.genericMessage(result.error)) }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        pollJob?.cancel()
        super.onCleared()
    }
}
