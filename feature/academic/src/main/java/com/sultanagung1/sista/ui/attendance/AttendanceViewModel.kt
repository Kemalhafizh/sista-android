package com.sultanagung1.sista.ui.attendance

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.core.sync.OfflineActionQueue
import com.sultanagung1.sista.core.util.Constants
import com.sultanagung1.sista.core.util.GeoUtils
import com.sultanagung1.sista.data.model.AttendanceCheckinResponse
import com.sultanagung1.sista.data.model.DynamicQrResponse
import com.sultanagung1.sista.data.model.GpsCheckinRequest
import com.sultanagung1.sista.data.repository.AttendanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AttendanceUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val checkinResult: AttendanceCheckinResponse? = null,
    val dynamicQrResult: DynamicQrResponse? = null,
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
        val distance = GeoUtils.calculateHaversineDistance(
            lat1 = lat,
            lon1 = lon,
            lat2 = Constants.CAMPUS_LATITUDE,
            lon2 = Constants.CAMPUS_LONGITUDE
        )
        val inside = distance <= Constants.CAMPUS_RADIUS_METERS

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

    fun loadDynamicQr() {
        viewModelScope.launch {
            attendanceRepository.getDynamicQr().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                    is NetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            dynamicQrResult = result.data
                        )
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
