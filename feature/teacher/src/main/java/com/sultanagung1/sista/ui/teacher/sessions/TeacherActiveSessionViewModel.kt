package com.sultanagung1.sista.ui.teacher.sessions

import android.os.SystemClock
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.mvi.MviViewModel
import com.sultanagung1.sista.core.mvi.UiEffect
import com.sultanagung1.sista.core.mvi.UiEvent
import com.sultanagung1.sista.core.mvi.UiState
import com.sultanagung1.sista.core.util.DateUtils
import com.sultanagung1.sista.data.model.ClassSessionContract
import com.sultanagung1.sista.data.model.ClassSessionDto
import com.sultanagung1.sista.data.model.ClassSessionErrorKind
import com.sultanagung1.sista.data.model.ClassSessionResult
import com.sultanagung1.sista.data.model.ClassSessionRules
import com.sultanagung1.sista.data.model.ClassSessionStatus
import com.sultanagung1.sista.data.repository.ClassSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class TeacherActiveSessionState(
    val sessionId: Long = 0,
    val session: ClassSessionDto? = null,
    val qrPayload: String? = null,
    /** `SystemClock.elapsedRealtime()` at which the shown QR stops being valid. */
    val qrExpiresAtMs: Long? = null,
    val qrRotationSeconds: Int = ClassSessionContract.DEFAULT_QR_ROTATION_SECONDS,
    val nowMs: Long = 0,
    /** Seconds to the scheduled end (negative in overtime); null until the session is known. */
    val remainingSeconds: Long? = null,
    val counts: ClassSessionRules.Counts = ClassSessionRules.Counts(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val notDeployed: Boolean = false,
    val isEnding: Boolean = false,
    val showTimeUpDialog: Boolean = false
) : UiState {
    val status: ClassSessionStatus? get() = session?.effectiveStatus
    val isLive: Boolean get() = status == ClassSessionStatus.ACTIVE
    val qrFreshness: ClassSessionRules.QrFreshness get() = ClassSessionRules.qrFreshness(qrExpiresAtMs, nowMs)
    val qrSecondsLeft: Int
        get() = qrExpiresAtMs?.let { (((it - nowMs) + 999) / 1000).toInt().coerceAtLeast(0) } ?: 0
    val timerTone: ClassSessionRules.TimerTone
        get() = remainingSeconds?.let(ClassSessionRules::timerTone) ?: ClassSessionRules.TimerTone.NORMAL
}

sealed interface TeacherActiveSessionEvent : UiEvent {
    data object ScreenStarted : TeacherActiveSessionEvent
    data object ScreenStopped : TeacherActiveSessionEvent
    data object Retry : TeacherActiveSessionEvent
    data class EndSessionConfirmed(val notes: String?, val topic: String?) : TeacherActiveSessionEvent
    /** "Lanjutkan 5 menit": the server closes the session itself 5 minutes after the scheduled end. */
    data object ContinueAfterTimeUp : TeacherActiveSessionEvent
    data object DismissTimeUp : TeacherActiveSessionEvent
}

sealed interface TeacherActiveSessionEffect : UiEffect {
    data class ShowMessage(val message: String) : TeacherActiveSessionEffect
}

/**
 * FASE 77.3: the running class. While the screen is visible it
 * - refetches the QR whenever the server says the current one expires
 *   (backing off 5 s → 30 s on failure),
 * - ticks the session timer every second (server-corrected clock),
 * - refreshes attendance every 10 s and the session status every 30 s, which
 *   is how an auto-close by the server shows up here.
 */
@HiltViewModel
class TeacherActiveSessionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ClassSessionRepository
) : MviViewModel<TeacherActiveSessionState, TeacherActiveSessionEvent, TeacherActiveSessionEffect>(
    TeacherActiveSessionState(sessionId = savedStateHandle.get<Long>("sessionId") ?: 0L)
) {

    private val jobs = mutableListOf<Job>()
    private var warnedBeforeEnd = false
    private var timeUpShown = false

    override fun onEvent(event: TeacherActiveSessionEvent) {
        when (event) {
            TeacherActiveSessionEvent.ScreenStarted -> startLoops()
            TeacherActiveSessionEvent.ScreenStopped -> stopLoops()
            TeacherActiveSessionEvent.Retry -> {
                stopLoops()
                setState { copy(isLoading = session == null, errorMessage = null) }
                startLoops()
            }
            is TeacherActiveSessionEvent.EndSessionConfirmed -> end(event.notes, event.topic)
            TeacherActiveSessionEvent.ContinueAfterTimeUp -> {
                setState { copy(showTimeUpDialog = false) }
                viewModelScope.launch {
                    emitEffect {
                        TeacherActiveSessionEffect.ShowMessage(
                            "Sesi dilanjutkan. Sistem menutup sesi otomatis 5 menit setelah jadwal berakhir."
                        )
                    }
                }
            }
            TeacherActiveSessionEvent.DismissTimeUp -> setState { copy(showTimeUpDialog = false) }
        }
    }

    private fun startLoops() {
        if (jobs.any { it.isActive }) return
        jobs += viewModelScope.launch { sessionAndAttendanceLoop() }
        jobs += viewModelScope.launch { tickLoop() }
        jobs += viewModelScope.launch { qrLoop() }
    }

    private fun stopLoops() {
        jobs.forEach { it.cancel() }
        jobs.clear()
    }

    private suspend fun sessionAndAttendanceLoop() {
        var tick = 0
        while (viewModelScope.isActive) {
            // Session status every 30 s (and first), attendance every 10 s.
            if (tick % 3 == 0 && !refreshSession()) return
            if (currentState.session != null) refreshAttendance()
            if (currentState.status?.isFinished == true && tick > 0) return
            tick++
            delay(ClassSessionRules.ATTENDANCE_POLL_MS)
        }
    }

    /** @return false when polling should stop (backend missing). */
    private suspend fun refreshSession(): Boolean {
        when (val result = repository.getTodaySessions()) {
            is ClassSessionResult.Success -> {
                val found = result.data.firstOrNull { it.sessionId == currentState.sessionId }
                if (found == null) {
                    // Keep a session we already know (e.g. the list rolled over at midnight).
                    setState {
                        copy(isLoading = false, errorMessage = if (session != null) null else "Sesi ini tidak ada di jadwal hari ini.")
                    }
                } else {
                    val before = currentState.status
                    setState { copy(session = found, isLoading = false, errorMessage = null, notDeployed = false) }
                    if (before == ClassSessionStatus.ACTIVE && found.effectiveStatus == ClassSessionStatus.AUTO_CLOSED) {
                        emitEffect { TeacherActiveSessionEffect.ShowMessage("Sesi ditutup otomatis oleh sistem.") }
                    }
                }
            }
            is ClassSessionResult.Failure -> {
                val notDeployed = result.error.kind == ClassSessionErrorKind.NOT_DEPLOYED
                setState { copy(isLoading = false, errorMessage = ClassSessionRules.genericMessage(result.error), notDeployed = notDeployed) }
                if (notDeployed) return false
            }
        }
        return true
    }

    private suspend fun refreshAttendance() {
        when (val result = repository.getSessionStudents(currentState.sessionId)) {
            is ClassSessionResult.Success -> setState { copy(counts = ClassSessionRules.countsOf(result.data)) }
            // The session card already carries counts; a failed refresh keeps the last ones.
            is ClassSessionResult.Failure -> if (currentState.counts.total == 0) {
                currentState.session?.let { s -> setState { copy(counts = ClassSessionRules.countsOf(s)) } }
            }
        }
    }

    private suspend fun tickLoop() {
        while (viewModelScope.isActive) {
            val remaining = ClassSessionRules.remainingSeconds(currentState.session?.scheduledEnd, nowSecondsOfDay())
            setState { copy(nowMs = SystemClock.elapsedRealtime(), remainingSeconds = remaining) }
            if (currentState.isLive && remaining != null) {
                if (!warnedBeforeEnd && remaining in 1..ClassSessionRules.WARNING_SECONDS) {
                    warnedBeforeEnd = true
                    emitEffect {
                        TeacherActiveSessionEffect.ShowMessage(
                            "Jam pelajaran berakhir dalam ${(remaining + 59) / 60} menit. Siswa yang belum absen akan tercatat alpha."
                        )
                    }
                }
                if (!timeUpShown && remaining <= 0) {
                    timeUpShown = true
                    setState { copy(showTimeUpDialog = true) }
                }
            }
            delay(1_000)
        }
    }

    private suspend fun qrLoop() {
        var failures = 0
        while (viewModelScope.isActive) {
            // Nothing to show until we know the session is running.
            val status = currentState.status
            if (status == null) {
                if (currentState.notDeployed) return
                delay(500)
                continue
            }
            if (status != ClassSessionStatus.ACTIVE) return
            when (val result = repository.getActiveQr(currentState.sessionId)) {
                is ClassSessionResult.Success -> {
                    failures = 0
                    val qr = result.data
                    val payload = qr.qrPayload
                    if (payload.isNullOrBlank()) {
                        failures++
                        delay(ClassSessionRules.qrRetryDelayMs(failures))
                        continue
                    }
                    setState {
                        copy(
                            qrPayload = payload,
                            qrExpiresAtMs = SystemClock.elapsedRealtime() + qr.remainingSeconds.coerceAtLeast(0) * 1000L,
                            qrRotationSeconds = qr.rotationSeconds.takeIf { it > 0 } ?: qrRotationSeconds
                        )
                    }
                    delay(ClassSessionRules.nextQrFetchDelayMs(qr.remainingSeconds))
                }
                is ClassSessionResult.Failure -> {
                    val code = result.error.errorCode
                    if (code == ClassSessionContract.ErrorCode.SESSION_NOT_ACTIVE || code == ClassSessionContract.ErrorCode.SESSION_CLOSED) {
                        refreshSession()
                        return
                    }
                    if (result.error.kind == ClassSessionErrorKind.NOT_DEPLOYED) return
                    failures++
                    delay(ClassSessionRules.qrRetryDelayMs(failures))
                }
            }
        }
    }

    private fun end(notes: String?, topic: String?) {
        if (currentState.isEnding) return
        setState { copy(isEnding = true, showTimeUpDialog = false) }
        viewModelScope.launch {
            when (val result = repository.endSession(
                currentState.sessionId,
                notes?.take(ClassSessionRules.END_NOTES_MAX_CHARS),
                topic?.take(ClassSessionRules.TOPIC_MAX_CHARS)
            )) {
                is ClassSessionResult.Success -> {
                    setState { copy(session = result.data, isEnding = false, qrPayload = null, qrExpiresAtMs = null) }
                    refreshAttendance()
                    emitEffect { TeacherActiveSessionEffect.ShowMessage("Kelas diakhiri. Siswa yang belum absen tercatat alpha.") }
                }
                is ClassSessionResult.Failure -> {
                    setState { copy(isEnding = false) }
                    if (result.error.errorCode == ClassSessionContract.ErrorCode.SESSION_NOT_ACTIVE) {
                        refreshSession()
                        emitEffect { TeacherActiveSessionEffect.ShowMessage("Sesi ini sudah tidak aktif.") }
                    } else {
                        emitEffect { TeacherActiveSessionEffect.ShowMessage(ClassSessionRules.genericMessage(result.error)) }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        stopLoops()
        super.onCleared()
    }

    private fun nowSecondsOfDay(): Long {
        val cal = DateUtils.nowCalendar()
        return cal.get(Calendar.HOUR_OF_DAY) * 3600L + cal.get(Calendar.MINUTE) * 60L + cal.get(Calendar.SECOND)
    }
}
