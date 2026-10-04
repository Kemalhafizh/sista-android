package com.sultanagung1.sista.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.MeProfile
import com.sultanagung1.sista.data.model.SchoolIdentity
import com.sultanagung1.sista.data.repository.LocaleSync
import com.sultanagung1.sista.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * [profile] null + [errorMessage] null = still loading. The settings on the
 * page work without it; only the identity card waits for the server.
 */
data class ProfileUiState(
    val profile: MeProfile? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val school: SchoolIdentity? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val localeSync: LocaleSync,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val school = async { repository.school() }
            when (val result = repository.me()) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(profile = result.data, isLoading = false) }
                    // A language changed on the web shows here too (and one picked here offline is sent now).
                    localeSync.reconcile(result.data.preferredLocale)
                }
                is NetworkResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                is NetworkResult.Loading -> Unit
            }
            val identity = school.await()
            if (identity != null) _uiState.update { it.copy(school = identity) }
        }
    }
}
