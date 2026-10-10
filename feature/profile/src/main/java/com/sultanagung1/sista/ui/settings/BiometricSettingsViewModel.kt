package com.sultanagung1.sista.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.core.security.BiometricAvailability
import com.sultanagung1.sista.core.security.BiometricVault
import com.sultanagung1.sista.core.security.KeystoreManager
import com.sultanagung1.sista.core.storage.SessionManager
import com.sultanagung1.sista.core.ui.text.UiText
import com.sultanagung1.sista.core.ui.text.asUiText
import com.sultanagung1.sista.data.repository.AuthRepository
import com.sultanagung1.sista.feature.profile.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BiometricSettingsUiState(
    /** Null until the phone has been asked. */
    val availability: BiometricAvailability? = null,
    val isLoginEnabled: Boolean = false,
    val isSensitiveProtectionEnabled: Boolean = true,
    val isSaving: Boolean = false,
    /** The server's answer, or a local reason the switch could not be turned on. */
    val message: UiText? = null,
    val messageIsError: Boolean = false,
)

/**
 * The phone's fingerprint, used for two things: signing in without the
 * password (the Keystore key's public half is registered on the server, which
 * checks every signed challenge) and asking for the fingerprint before a
 * report card or a CBT exam opens. No fingerprint ever leaves the phone.
 */
@HiltViewModel
class BiometricSettingsViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val authRepository: AuthRepository,
    private val keystoreManager: KeystoreManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BiometricSettingsUiState())
    val uiState: StateFlow<BiometricSettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.isBiometricEnabledFlow.collect { enabled -> _uiState.update { it.copy(isLoginEnabled = enabled) } }
        }
        viewModelScope.launch {
            sessionManager.isSensitiveProtectionEnabledFlow.collect { enabled ->
                _uiState.update { it.copy(isSensitiveProtectionEnabled = enabled) }
            }
        }
    }

    fun checkDevice(context: Context) {
        _uiState.update { it.copy(availability = BiometricVault.checkStatus(context)) }
    }

    /** Turning it on registers this phone's public key; the switch only stays on once the server accepts it. */
    fun setLoginEnabled(enabled: Boolean) {
        if (!enabled) {
            viewModelScope.launch {
                sessionManager.setBiometricEnabled(false)
                _uiState.update { it.copy(isLoginEnabled = false, message = null) }
            }
            return
        }
        viewModelScope.launch {
            val publicKeyPem = keystoreManager.getPublicKeyPem()
            if (publicKeyPem.isNullOrBlank()) {
                _uiState.update { it.copy(message = UiText.Res(R.string.bio_key_failed), messageIsError = true) }
                return@launch
            }
            val deviceId = sessionManager.getOrCreateDeviceId()
            authRepository.registerBiometric(deviceId, publicKeyPem).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.update { it.copy(isSaving = true, message = null) }
                    is NetworkResult.Success -> {
                        sessionManager.setBiometricEnabled(true)
                        _uiState.update {
                            it.copy(isSaving = false, isLoginEnabled = true, message = UiText.Res(R.string.bio_login_on), messageIsError = false)
                        }
                    }
                    is NetworkResult.Error -> _uiState.update {
                        it.copy(isSaving = false, isLoginEnabled = false, message = result.message.asUiText(), messageIsError = true)
                    }
                }
            }
        }
    }

    fun setSensitiveProtection(enabled: Boolean) {
        viewModelScope.launch {
            sessionManager.setSensitiveProtectionEnabled(enabled)
            _uiState.update { it.copy(isSensitiveProtectionEnabled = enabled) }
        }
    }
}
