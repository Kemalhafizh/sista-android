package com.sultanagung1.sista.ui.attendance

import android.os.SystemClock
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.mvi.MviViewModel
import com.sultanagung1.sista.core.mvi.UiEffect
import com.sultanagung1.sista.core.mvi.UiEvent
import com.sultanagung1.sista.core.mvi.UiState
import com.sultanagung1.sista.data.model.ActiveClassSessionDto
import com.sultanagung1.sista.data.model.ClassSessionErrorKind
import com.sultanagung1.sista.data.model.ClassSessionResult
import com.sultanagung1.sista.data.model.ClassSessionRules
import com.sultanagung1.sista.data.model.ClassSessionRules.ScanOutcome
import com.sultanagung1.sista.data.repository.ClassSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.sultanagung1.sista.core.ui.text.UiText
import com.sultanagung1.sista.core.designsystem.ClassSessionText
import com.sultanagung1.sista.core.designsystem.R as DsR
import com.sultanagung1.sista.feature.academic.R

sealed interface StudentScanPhase {
    data object Scanning : StudentScanPhase
    data object Submitting : StudentScanPhase
    data class Success(
        val subjectName: String?,
        val classroomName: String?,
        val checkedInAt: String?
    ) : StudentScanPhase
    data class AlreadyRecorded(val message: UiText) : StudentScanPhase
    data class Blocked(val message: UiText) : StudentScanPhase
}

data class StudentSessionQrScanState(
    val activeSession: ActiveClassSessionDto? = null,
    val isCheckingActive: Boolean = true,
    val phase: StudentScanPhase = StudentScanPhase.Scanning,
    /** A retryable problem shown under the camera (expired QR, another QR, no connection). */
    val hint: UiText? = null,
    val notDeployed: Boolean = false
) : UiState {
    val isScanning: Boolean get() = phase == StudentScanPhase.Scanning && !notDeployed
}

sealed interface StudentSessionQrScanEvent : UiEvent {
    data object ScreenStarted : StudentSessionQrScanEvent
    data class CodeDetected(val rawValue: String) : StudentSessionQrScanEvent
    data object ScanAgain : StudentSessionQrScanEvent
}

sealed interface StudentSessionQrScanEffect : UiEffect {
    /** Short tick when a class QR is seen (77.5.2: 50 ms). */
    data object QrDetected : StudentSessionQrScanEffect
    /** Long confirmation when attendance is recorded (77.5.2: 200 ms). */
    data object Recorded : StudentSessionQrScanEffect
    data object Refused : StudentSessionQrScanEffect
}

/**
 * FASE 77.5: the student scans the rotating QR on the teacher's screen.
 * Validation (right class, fresh token, not yet recorded) is the server's;
 * the app only filters out QR codes that are not class-session codes and
 * keeps one payload from being sent over and over.
 */
@HiltViewModel
class StudentSessionQrScanViewModel @Inject constructor(
    private val repository: ClassSessionRepository
) : MviViewModel<StudentSessionQrScanState, StudentSessionQrScanEvent, StudentSessionQrScanEffect>(
    StudentSessionQrScanState()
) {

    private val gate = ClassSessionRules.ScanGate()

    override fun onEvent(event: StudentSessionQrScanEvent) {
        when (event) {
            StudentSessionQrScanEvent.ScreenStarted -> loadActiveSession()
            is StudentSessionQrScanEvent.CodeDetected -> onDetected(event.rawValue)
            StudentSessionQrScanEvent.ScanAgain -> setState { copy(phase = StudentScanPhase.Scanning, hint = null) }
        }
    }

    private fun loadActiveSession() {
        viewModelScope.launch {
            when (val result = repository.getActiveSessionForStudent()) {
                is ClassSessionResult.Success -> {
                    val active = result.data
                    setState {
                        copy(
                            activeSession = active,
                            isCheckingActive = false,
                            notDeployed = false,
                            phase = if (active?.alreadyCheckedIn == true && phase == StudentScanPhase.Scanning) {
                                StudentScanPhase.AlreadyRecorded(alreadyRecordedMessage(active.attendanceStatus))
                            } else {
                                phase
                            }
                        )
                    }
                }
                is ClassSessionResult.Failure -> setState {
                    // Not knowing the active session does not stop a scan; the server decides.
                    copy(isCheckingActive = false, notDeployed = result.error.kind == ClassSessionErrorKind.NOT_DEPLOYED)
                }
            }
        }
    }

    private fun onDetected(raw: String) {
        if (!currentState.isScanning) return
        val now = SystemClock.elapsedRealtime()
        if (!ClassSessionRules.isClassSessionQr(raw)) {
            if (gate.shouldSubmit(raw, now)) setState { copy(hint = ClassSessionText.notAClassQr) }
            return
        }
        if (!gate.shouldSubmit(raw, now)) return

        setState { copy(phase = StudentScanPhase.Submitting, hint = null) }
        viewModelScope.launch {
            emitEffect { StudentSessionQrScanEffect.QrDetected }
            when (val result = repository.scanQr(raw)) {
                is ClassSessionResult.Success -> {
                    val r = result.data
                    setState {
                        copy(
                            phase = StudentScanPhase.Success(
                                subjectName = r.subjectName ?: activeSession?.subjectName,
                                classroomName = r.classroomName ?: activeSession?.classroomName,
                                checkedInAt = r.checkedInAt
                            )
                        )
                    }
                    emitEffect { StudentSessionQrScanEffect.Recorded }
                }
                is ClassSessionResult.Failure -> {
                    if (result.error.kind == ClassSessionErrorKind.NOT_DEPLOYED) {
                        setState { copy(phase = StudentScanPhase.Scanning, notDeployed = true) }
                        return@launch
                    }
                    gate.markRefused(raw)
                    when (val outcome = ClassSessionRules.scanOutcome(result.error)) {
                        is ScanOutcome.Retry -> setState { copy(phase = StudentScanPhase.Scanning, hint = ClassSessionText.scan(outcome)) }
                        is ScanOutcome.AlreadyRecorded -> setState { copy(phase = StudentScanPhase.AlreadyRecorded(ClassSessionText.scan(outcome))) }
                        is ScanOutcome.Blocked -> setState { copy(phase = StudentScanPhase.Blocked(ClassSessionText.scan(outcome))) }
                    }
                    emitEffect { StudentSessionQrScanEffect.Refused }
                }
            }
        }
    }

    private fun alreadyRecordedMessage(status: com.sultanagung1.sista.data.model.SessionAttendanceStatus?): UiText =
        if (status != null) {
            UiText.Res(R.string.sq_already_status, UiText.Res(ClassSessionText.label(status)))
        } else {
            UiText.Res(DsR.string.cs_scan_already)
        }
}
