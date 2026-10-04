package com.sultanagung1.sista.ui.auth

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.core.security.KeystoreManager
import com.sultanagung1.sista.core.storage.SessionManager
import com.sultanagung1.sista.core.ui.text.UiText
import com.sultanagung1.sista.feature.auth.R
import com.sultanagung1.sista.data.model.UserProfile
import com.sultanagung1.sista.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    /** The server's own message ([UiText.Raw]) or a check made on this phone. */
    val errorMessage: UiText? = null,
    val userProfile: UserProfile? = null,
    val isBiometricEnabled: Boolean = false,
    val rememberedIdentifier: String? = null,
    val rememberedUserId: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val keystoreManager: KeystoreManager,
    private val sessionManager: SessionManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        sessionManager?.let { sm ->
            viewModelScope.launch {
                sm.isBiometricEnabledFlow.collect { enabled ->
                    _uiState.value = _uiState.value.copy(isBiometricEnabled = enabled)
                }
            }
            viewModelScope.launch {
                sm.rememberedIdentifierFlow.collect { remembered ->
                    _uiState.value = _uiState.value.copy(rememberedIdentifier = remembered)
                }
            }
            viewModelScope.launch {
                sm.rememberedUserIdFlow.collect { rememberedUserId ->
                    _uiState.value = _uiState.value.copy(rememberedUserId = rememberedUserId)
                }
            }
        }
    }

    fun login(identifier: String, pass: String) {
        if (identifier.isBlank() || pass.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = UiText.Res(R.string.login_error_missing_fields))
            return
        }

        viewModelScope.launch {
            authRepository.login(identifier, pass).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                    }
                    is NetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSuccess = true,
                            userProfile = result.data.data?.user
                        )
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = UiText.Raw(result.message)
                        )
                    }
                }
            }
        }
    }

    /**
     * Public-key challenge/response biometric login: the fingerprint prompt only
     * releases the on-device Keystore signing key (never a password). The signed
     * nonce is verified server-side against the public key registered during
     * biometric enrollment (see FaceEnrollmentViewModel.toggleBiometricLock).
     */
    fun loginWithBiometric() {
        val sm = sessionManager
        if (sm == null) {
            _uiState.value = _uiState.value.copy(errorMessage = UiText.Res(R.string.login_error_no_device_session))
            return
        }
        val userId = _uiState.value.rememberedUserId
        if (userId.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = UiText.Res(R.string.login_error_biometric_first)
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val deviceId = sm.getOrCreateDeviceId()

            authRepository.requestBiometricChallenge(deviceId).collect { challengeResult ->
                when (challengeResult) {
                    is NetworkResult.Loading -> Unit
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = UiText.Raw(challengeResult.message))
                    }
                    is NetworkResult.Success -> {
                        val signature = keystoreManager.signChallenge(challengeResult.data)
                        authRepository.verifyBiometric(deviceId, userId, signature).collect { result ->
                            when (result) {
                                is NetworkResult.Loading -> {
                                    _uiState.value = _uiState.value.copy(isLoading = true)
                                }
                                is NetworkResult.Success -> {
                                    _uiState.value = _uiState.value.copy(
                                        isLoading = false,
                                        isSuccess = true,
                                        userProfile = result.data.user
                                    )
                                }
                                is NetworkResult.Error -> {
                                    _uiState.value = _uiState.value.copy(
                                        isLoading = false,
                                        errorMessage = UiText.Raw(result.message)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun resetState() {
        _uiState.value = LoginUiState(
            isBiometricEnabled = _uiState.value.isBiometricEnabled,
            rememberedIdentifier = _uiState.value.rememberedIdentifier,
            rememberedUserId = _uiState.value.rememberedUserId
        )
    }

    fun logout(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            authRepository.logout()
            resetState()
            onComplete?.invoke()
        }
    }
}
