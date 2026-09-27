package com.sultanagung1.sista.ui.attendance

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import android.os.SystemClock
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.core.sync.OfflineActionQueue
import com.sultanagung1.sista.core.util.GeoUtils
import com.sultanagung1.sista.data.model.AttendanceCheckinResponse
import com.sultanagung1.sista.data.model.ClassSessionRules
import com.sultanagung1.sista.data.model.DynamicQrResponse
import com.sultanagung1.sista.data.model.GpsCheckinRequest
import com.sultanagung1.sista.data.repository.AttendanceRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AttendanceUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val checkinResult: AttendanceCheckinResponse? = null,
    val dynamicQrResult: DynamicQrResponse? = null,
    /** `SystemClock.elapsedRealtime()` at which [dynamicQrResult] stops being valid. */
    val dynamicQrExpiresAtMs: Long? = null,
    val distanceToCampusMeters: Double = 9999.0,
    val isInsideRadius: Boolean = false,
    val isMockLocationDetected: Boolean = false,
    val errorMessage: String? = null,
    // FASE 69.3: true network dropout at the moment of submit — the check-in was
    // queued locally (OfflineActionQueue) instead of shown as a hard failure.
    val isQueuedOffline: Boolean = false
)


@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository,
    private val offlineActionQueue: OfflineActionQueue
) : ViewModel() {

    private val _uiState = MutableStateFlow(AttendanceUiState())
    val uiState: StateFlow<AttendanceUiState> = _uiState.asStateFlow()

    fun updateCoordinates(lat: Double, lon: Double, isMock: Boolean) {
        // The server's geofence (GET mobile/config), same as the check-in endpoint uses.
        val distance = GeoUtils.distanceToCampus(lat, lon)
        val inside = distance <= GeoUtils.campus.radiusMeters

        _uiState.value = _uiState.value.copy(
            distanceToCampusMeters = distance,
            isInsideRadius = inside,
            isMockLocationDetected = isMock
        )
    }

    fun submitGpsCheckin(lat: Double, lon: Double, accuracy: Float, isMock: Boolean) {
        if (isMock) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Peringatan Anti-Cheat: Lokasi palsu (Mock GPS) terdeteksi!"
            )
            return
        }

        viewModelScope.launch {
            val request = GpsCheckinRequest(
                latitude = lat,
                longitude = lon,
                accuracy = accuracy,
                isMock = isMock,
                deviceFingerprint = "Android-${android.os.Build.MODEL}"
            )

            attendanceRepository.submitGpsCheckin(request).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            isSuccess = false,
                            isQueuedOffline = false,
                            checkinResult = null,
                            errorMessage = null
                        )
                    }
                    is NetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSuccess = true,
                            checkinResult = result.data
                        )
                    }
                    is NetworkResult.Error -> {
                        if (result.code == null) {
                            // No HTTP response reached the server at all (exception before/at send) —
                            // a true connectivity failure, safe to queue and retry automatically.
                            // A server-side rejection (code != null, e.g. outside geofence radius)
                            // must never be silently "succeeded" — that would let a fraudulent or
                            // invalid check-in appear to go through.
                            offlineActionQueue.queueAttendance(request)
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isSuccess = true,
                                isQueuedOffline = true,
                                checkinResult = null
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    private var dynamicQrJob: Job? = null

    /**
     * Keeps the gate QR current while the screen is visible: refetch when the
     * server's 30 s slice rolls over (`expires_in_seconds`), back off 5→30 s on
     * failure. The screen used to refetch on its own fixed 30 s counter, also in
     * the background, whatever the server said.
     */
    fun startDynamicQrRotation() {
        if (dynamicQrJob?.isActive == true) return
        dynamicQrJob = viewModelScope.launch {
            var failures = 0
            while (isActive) {
                val expiresIn = fetchDynamicQr()
                if (expiresIn != null) {
                    failures = 0
                    delay(ClassSessionRules.nextQrFetchDelayMs(expiresIn))
                } else {
                    failures++
                    delay(ClassSessionRules.qrRetryDelayMs(failures))
                }
            }
        }
    }

    fun stopDynamicQrRotation() {
        dynamicQrJob?.cancel()
        dynamicQrJob = null
    }

    /** "Perbarui QR Sekarang": fetch now and restart the cycle from it. */
    fun loadDynamicQr() {
        stopDynamicQrRotation()
        startDynamicQrRotation()
    }

    /** @return seconds the new QR is valid for, or null if the fetch failed. */
    private suspend fun fetchDynamicQr(): Int? {
        var expiresIn: Int? = null
        attendanceRepository.getDynamicQr().collect { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
                is NetworkResult.Success -> {
                    val seconds = result.data.expiresInSeconds.coerceAtLeast(0)
                    expiresIn = seconds
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = null,
                        dynamicQrResult = result.data,
                        dynamicQrExpiresAtMs = SystemClock.elapsedRealtime() + seconds * 1000L
                    )
                }
                is NetworkResult.Error -> {
                    // Keep the last QR: the screen dims it until it expires.
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
        return expiresIn
    }

    override fun onCleared() {
        stopDynamicQrRotation()
        super.onCleared()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
