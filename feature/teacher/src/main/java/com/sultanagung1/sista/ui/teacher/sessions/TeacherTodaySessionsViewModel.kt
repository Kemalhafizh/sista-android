package com.sultanagung1.sista.ui.teacher.sessions

import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.mvi.MviViewModel
import com.sultanagung1.sista.core.mvi.UiEffect
import com.sultanagung1.sista.core.mvi.UiEvent
import com.sultanagung1.sista.core.mvi.UiState
import com.sultanagung1.sista.core.util.DateUtils
import com.sultanagung1.sista.data.model.ClassSessionRejection
import com.sultanagung1.sista.data.model.ClassSessionDto
import com.sultanagung1.sista.data.model.ClassSessionErrorKind
import com.sultanagung1.sista.data.model.ClassSessionResult
import com.sultanagung1.sista.data.model.ClassSessionRules
import com.sultanagung1.sista.data.repository.ClassSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeacherTodaySessionsState(
    val sessions: List<ClassSessionDto> = emptyList(),
    /** True until the first answer (data or error) arrives. */
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    /** FASE 117 routes are missing on the server. */
    val notDeployed: Boolean = false,
    val startingScheduleId: Long? = null,
    /** Minutes of day (server-corrected); drives the "Mulai Kelas" windows. */
    val nowMinutes: Int = DateUtils.nowMinutesOfDay(),
    val todayLabel: String = ""
) : UiState

sealed interface TeacherTodaySessionsEvent : UiEvent {
    data object ScreenStarted : TeacherTodaySessionsEvent
    data object ScreenStopped : TeacherTodaySessionsEvent
    data object Refresh : TeacherTodaySessionsEvent
    data class StartSession(val scheduleId: Long, val topic: String?) : TeacherTodaySessionsEvent
    data class OpenSession(val sessionId: Long) : TeacherTodaySessionsEvent
}

sealed interface TeacherTodaySessionsEffect : UiEffect {
    data class OpenActiveSession(val sessionId: Long) : TeacherTodaySessionsEffect
    data class ShowMessage(val message: String) : TeacherTodaySessionsEffect
}

/**
 * FASE 77.2: today's teaching slots. Polls every 30 s while the screen is
 * visible (a session can be started on another device or auto-closed by the
 * server) and re-evaluates the start windows every 15 s.
 */
@HiltViewModel
class TeacherTodaySessionsViewModel @Inject constructor(
    private val repository: ClassSessionRepository
) : MviViewModel<TeacherTodaySessionsState, TeacherTodaySessionsEvent, TeacherTodaySessionsEffect>(
    TeacherTodaySessionsState(todayLabel = todayLabel())
) {

    private var pollJob: Job? = null
    private var clockJob: Job? = null

    override fun onEvent(event: TeacherTodaySessionsEvent) {
        when (event) {
            TeacherTodaySessionsEvent.ScreenStarted -> startPolling()
            TeacherTodaySessionsEvent.ScreenStopped -> stopPolling()
            TeacherTodaySessionsEvent.Refresh -> viewModelScope.launch { load(userInitiated = true) }
            is TeacherTodaySessionsEvent.StartSession -> start(event.scheduleId, event.topic)
            is TeacherTodaySessionsEvent.OpenSession -> viewModelScope.launch {
                emitEffect { TeacherTodaySessionsEffect.OpenActiveSession(event.sessionId) }
            }
        }
    }

    private fun startPolling() {
        if (pollJob?.isActive == true) return
        pollJob = viewModelScope.launch {
            while (isActive) {
                load(userInitiated = false)
                // A missing backend will not appear within 30 s; stop hammering it.
                if (currentState.notDeployed) break
                delay(ClassSessionRules.TEACHER_LIST_POLL_MS)
            }
        }
        clockJob = viewModelScope.launch {
            while (isActive) {
                setState { copy(nowMinutes = DateUtils.nowMinutesOfDay()) }
                delay(15_000)
            }
        }
    }

    private fun stopPolling() {
        pollJob?.cancel()
        clockJob?.cancel()
        pollJob = null
        clockJob = null
    }

    private suspend fun load(userInitiated: Boolean) {
        if (userInitiated) setState { copy(isRefreshing = true) }
        when (val result = repository.getTodaySessions()) {
            is ClassSessionResult.Success -> setState {
                copy(
                    sessions = ClassSessionRules.sortForTeacher(result.data),
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = null,
                    notDeployed = false,
                    nowMinutes = DateUtils.nowMinutesOfDay()
                )
            }
            is ClassSessionResult.Failure -> setState {
                // Keep the last list on screen; a background poll failing must not blank it.
                copy(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = ClassSessionRules.genericMessage(result.error),
                    notDeployed = result.error.kind == ClassSessionErrorKind.NOT_DEPLOYED
                )
            }
        }
    }

    private fun start(scheduleId: Long, topic: String?) {
        if (currentState.startingScheduleId != null) return
        setState { copy(startingScheduleId = scheduleId) }
        viewModelScope.launch {
            when (val result = repository.startSession(scheduleId, topic?.take(ClassSessionRules.TOPIC_MAX_CHARS))) {
                is ClassSessionResult.Success -> {
                    val sessionId = result.data.sessionId
                    setState { copy(startingScheduleId = null) }
                    if (sessionId != null) {
                        emitEffect { TeacherTodaySessionsEffect.OpenActiveSession(sessionId) }
                    } else {
                        emitEffect { TeacherTodaySessionsEffect.ShowMessage("Kelas dimulai, tetapi server tidak mengirim nomor sesi. Muat ulang daftar.") }
                    }
                }
                is ClassSessionResult.Failure -> {
                    setState { copy(startingScheduleId = null) }
                    val existing = result.error.existingSessionId
                    val rejection = result.error.rejection
                    if (existing != null &&
                        (rejection == ClassSessionRejection.SESSION_ALREADY_STARTED || rejection == ClassSessionRejection.SESSION_ALREADY_ENDED)
                    ) {
                        // Started (or already finished) from another device, or a retried tap:
                        // open that session instead of failing.
                        emitEffect { TeacherTodaySessionsEffect.OpenActiveSession(existing) }
                    } else {
                        emitEffect { TeacherTodaySessionsEffect.ShowMessage(ClassSessionRules.startFailureMessage(result.error)) }
                    }
                }
            }
            load(userInitiated = false)
        }
    }

    override fun onCleared() {
        stopPolling()
        super.onCleared()
    }

    private companion object {
        fun todayLabel(): String {
            val cal = DateUtils.nowCalendar()
            val months = arrayOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
            return "${DateUtils.todayDayNameIndonesian()}, ${cal.get(java.util.Calendar.DAY_OF_MONTH)} ${months[cal.get(java.util.Calendar.MONTH)]} ${cal.get(java.util.Calendar.YEAR)}"
        }
    }
}
