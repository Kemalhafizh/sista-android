package com.sultanagung1.sista.ui.home

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.core.storage.SessionManager
import com.sultanagung1.sista.data.model.AcademicSummary
import com.sultanagung1.sista.data.model.PrayerSchedule
import com.sultanagung1.sista.data.model.ScheduleItem
import com.sultanagung1.sista.data.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val userName: String = "Siswa Sultan Agung",
    val userRole: String = "student",
    val userIdentifier: String = "212210045",
    val studentClass: String = "XII MIPA 1",
    val todaySchedules: List<ScheduleItem> = emptyList(),
    val academicSummary: AcademicSummary? = null,
    val contextualPayload: com.sultanagung1.sista.data.model.ContextualHomePayload? = null,
    val prayerSchedule: PrayerSchedule = PrayerSchedule(
        fajr = "04:32",
        dhuhr = "11:52",
        asr = "15:10",
        maghrib = "17:54",
        isha = "19:04",
        currentPrayer = "Dzuhur",
        nextPrayerName = "Ashar",
        nextPrayerCountdown = "01:24:10"
    ),
    val isAttendanceDoneToday: Boolean = false,
    val unreadNotificationsCount: Int = 3,
    val errorMessage: String? = null
)


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.userNameFlow.collect { name ->
                if (!name.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(userName = name)
                }
            }
        }
        viewModelScope.launch {
            sessionManager.userRoleFlow.collect { role ->
                if (!role.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(userRole = role)
                }
            }
        }
        viewModelScope.launch {
            sessionManager.userIdentifierFlow.collect { id ->
                if (!id.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(userIdentifier = id)
                }
            }
        }
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Load today's schedule
            studentRepository.getSchedule().collect { result ->
                if (result is NetworkResult.Success) {
                    _uiState.value = _uiState.value.copy(
                        todaySchedules = result.data.take(3),
                        isLoading = false
                    )
                } else if (result is NetworkResult.Error) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }

            // Load Contextual Home Payload (Fase 40)
            try {
                studentRepository.getContextualHome().collect { result ->
                    if (result is NetworkResult.Success && result.data != null) {
                        _uiState.value = _uiState.value.copy(contextualPayload = result.data)
                    }
                }
            } catch (_: Exception) {}
        }
    }
}
