package com.sultanagung1.sista.ui.gamification

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.mvi.UiEvent
import com.sultanagung1.sista.core.mvi.UiState
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.Badge
import com.sultanagung1.sista.data.model.GamificationProfile
import com.sultanagung1.sista.data.model.LeaderboardEntry
import com.sultanagung1.sista.data.model.XpHistoryItem
import com.sultanagung1.sista.data.repository.GamificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface GamificationUiEvent : UiEvent {
    data class Refresh(val unit: Unit = Unit) : GamificationUiEvent
}

data class GamificationUiState(
    val isLoading: Boolean = false,
    val profile: GamificationProfile? = null,
    val leaderboard: List<LeaderboardEntry> = emptyList(),
    val earnedBadges: List<Badge> = emptyList(),
    val lockedBadges: List<Badge> = emptyList(),
    val xpHistory: List<XpHistoryItem> = emptyList(),
    val errorMessage: String? = null
) : UiState


@HiltViewModel
class GamificationViewModel @Inject constructor(
    private val gamificationRepository: GamificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GamificationUiState())
    val uiState: StateFlow<GamificationUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun onEvent(event: GamificationUiEvent) {
        when (event) {
            is GamificationUiEvent.Refresh -> loadData()
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                var profile: GamificationProfile? = null
                var leaderboard: List<LeaderboardEntry> = emptyList()
                var earnedBadges: List<Badge> = emptyList()
                var lockedBadges: List<Badge> = emptyList()
                var xpHistory: List<XpHistoryItem> = emptyList()

                gamificationRepository.getProfile().collect { res ->
                    if (res is NetworkResult.Success) profile = res.data
                }

                gamificationRepository.getLeaderboard().collect { res ->
                    if (res is NetworkResult.Success) leaderboard = res.data
                }

                gamificationRepository.getBadges().collect { res ->
                    if (res is NetworkResult.Success) {
                        earnedBadges = res.data?.earned ?: emptyList()
                        lockedBadges = res.data?.locked ?: emptyList()
                    }
                }

                gamificationRepository.getHistory().collect { res ->
                    if (res is NetworkResult.Success) xpHistory = res.data
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    profile = profile ?: GamificationProfile(1420, 7, "Thalibul Ilmi Mujahid", 180, 14, 28, 12, 3),
                    leaderboard = leaderboard,
                    earnedBadges = earnedBadges,
                    lockedBadges = lockedBadges,
                    xpHistory = xpHistory
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Gagal memuat data gamifikasi"
                )
            }
        }
    }
}
