package com.sultanagung1.sista.ui.attendance

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.security.BiometricAvailability
import com.sultanagung1.sista.core.security.BiometricVault
import com.sultanagung1.sista.core.storage.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BiometricSecurityUiState(
    val biometricAvailability: BiometricAvailability = BiometricAvailability.AVAILABLE,
    val isBiometricLockEnabled: Boolean = false,
    val isSensitiveProtectionEnabled: Boolean = true,
    val fingerprintTestSuccess: Boolean = false,
    val fingerprintStatusMessage: String? = null
)


@HiltViewModel
class FaceEnrollmentViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BiometricSecurityUiState())
    val uiState: StateFlow<BiometricSecurityUiState> = _uiState.asStateFlow()

    init {
        sessionManager?.let { sm ->
            viewModelScope.launch {
                sm.isBiometricEnabledFlow.collect { enabled ->
                    _uiState.value = _uiState.value.copy(isBiometricLockEnabled = enabled)
                }
            }
            viewModelScope.launch {
                sm.isSensitiveProtectionEnabledFlow.collect { enabled ->
                    _uiState.value = _uiState.value.copy(isSensitiveProtectionEnabled = enabled)
                }
            }
        }
    }

    fun checkDeviceBiometric(context: Context) {
        val status = BiometricVault.checkStatus(context)
        _uiState.value = _uiState.value.copy(biometricAvailability = status)
    }

    fun toggleBiometricLock(enabled: Boolean) {
        viewModelScope.launch {
            sessionManager?.setBiometricEnabled(enabled)
            _uiState.value = _uiState.value.copy(isBiometricLockEnabled = enabled)
        }
    }

    fun toggleSensitiveProtection(enabled: Boolean) {
        viewModelScope.launch {
            sessionManager?.setSensitiveProtectionEnabled(enabled)
            _uiState.value = _uiState.value.copy(isSensitiveProtectionEnabled = enabled)
        }
    }

    fun onFingerprintTestSuccess() {
        _uiState.value = _uiState.value.copy(
            fingerprintTestSuccess = true,
            fingerprintStatusMessage = "Sensor sidik jari terverifikasi! Akses biometrik aktif & aman."
        )
    }

    fun onFingerprintTestError(error: String) {
        _uiState.value = _uiState.value.copy(
            fingerprintTestSuccess = false,
            fingerprintStatusMessage = error
        )
    }

    fun reset() {
        _uiState.value = BiometricSecurityUiState(
            isBiometricLockEnabled = _uiState.value.isBiometricLockEnabled,
            biometricAvailability = _uiState.value.biometricAvailability
        )
    }
}
