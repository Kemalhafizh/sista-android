package com.sultanagung1.sista.ui.auth

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.core.storage.SessionManager
import com.sultanagung1.sista.data.model.UserProfile
import com.sultanagung1.sista.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val userProfile: UserProfile? = null,
    val isBiometricEnabled: Boolean = false,
    val rememberedIdentifier: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
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
        }
    }

    fun login(identifier: String, pass: String) {
        if (identifier.isBlank() || pass.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Mohon masukkan NISN/NIP dan Kata Sandi.")
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
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun loginWithBiometric(identifier: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val effectiveIdentifier = when {
                !identifier.isNullOrBlank() -> identifier
                !_uiState.value.rememberedIdentifier.isNullOrBlank() -> _uiState.value.rememberedIdentifier!!
                else -> "siswa1@student.sa1.sch.id"
            }
            authRepository.login(effectiveIdentifier, "password").collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
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

    fun resetState() {
        _uiState.value = LoginUiState(
            isBiometricEnabled = _uiState.value.isBiometricEnabled,
            rememberedIdentifier = _uiState.value.rememberedIdentifier
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
