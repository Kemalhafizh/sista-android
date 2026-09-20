package com.sultanagung1.sista.ui.attendance

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.core.security.BiometricAvailability
import com.sultanagung1.sista.core.security.BiometricVault
import com.sultanagung1.sista.core.security.KeystoreManager
import com.sultanagung1.sista.core.storage.SessionManager
import com.sultanagung1.sista.data.repository.AuthRepository
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
    private val sessionManager: SessionManager,
    private val authRepository: AuthRepository,
    private val keystoreManager: KeystoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BiometricSecurityUiState())
    val uiState: StateFlow<BiometricSecurityUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.isBiometricEnabledFlow.collect { enabled ->
                _uiState.value = _uiState.value.copy(isBiometricLockEnabled = enabled)
            }
        }
        viewModelScope.launch {
            sessionManager.isSensitiveProtectionEnabledFlow.collect { enabled ->
                _uiState.value = _uiState.value.copy(isSensitiveProtectionEnabled = enabled)
            }
        }
    }

    fun checkDeviceBiometric(context: Context) {
        val status = BiometricVault.checkStatus(context)
        _uiState.value = _uiState.value.copy(biometricAvailability = status)
    }

    fun toggleBiometricLock(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled) {
                val publicKeyPem = keystoreManager.getPublicKeyPem()
                if (publicKeyPem.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(
                        fingerprintStatusMessage = "Gagal membuat kunci keamanan perangkat. Coba lagi."
                    )
                    return@launch
                }
                val deviceId = sessionManager.getOrCreateDeviceId()
                authRepository.registerBiometric(deviceId, publicKeyPem).collect { result ->
                    when (result) {
                        is NetworkResult.Loading -> Unit
                        is NetworkResult.Success -> {
                            sessionManager.setBiometricEnabled(true)
                            _uiState.value = _uiState.value.copy(
                                isBiometricLockEnabled = true,
                                fingerprintStatusMessage = "Login biometrik aktif untuk perangkat ini."
                            )
                        }
                        is NetworkResult.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isBiometricLockEnabled = false,
                                fingerprintStatusMessage = result.message
                            )
                        }
                    }
                }
            } else {
                sessionManager.setBiometricEnabled(false)
                _uiState.value = _uiState.value.copy(isBiometricLockEnabled = false)
            }
        }
    }

    fun toggleSensitiveProtection(enabled: Boolean) {
        viewModelScope.launch {
            sessionManager.setSensitiveProtectionEnabled(enabled)
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
