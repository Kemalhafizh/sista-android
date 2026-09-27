package com.sultanagung1.sista.ui.home

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.core.storage.SessionManager
import com.sultanagung1.sista.data.model.PrayerSchedule
import com.sultanagung1.sista.data.model.ScheduleItem
import com.sultanagung1.sista.data.model.ActiveClassSessionDto
import com.sultanagung1.sista.data.model.ClassSessionErrorKind
import com.sultanagung1.sista.data.model.ClassSessionResult
import com.sultanagung1.sista.data.model.ClassSessionRules
import com.sultanagung1.sista.data.repository.AttendanceRepository
import com.sultanagung1.sista.data.repository.ClassSessionRepository
import com.sultanagung1.sista.data.repository.FeatureUsageRepository
import com.sultanagung1.sista.data.repository.StudentRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

data class HomeUiState(
    val isLoading: Boolean = false,
    val userName: String = "Siswa Sultan Agung",
    val userRole: String = "student",
    val userIdentifier: String = "—",
    val studentClass: String = "—",
    val todaySchedules: List<ScheduleItem> = emptyList(),
    val contextualPayload: com.sultanagung1.sista.data.model.ContextualHomePayload? = null,
    /** Null until a real prayer-time source is wired — sistem-terpadu has no JSON API for this yet. */
    val prayerSchedule: PrayerSchedule? = null,
    val unreadNotificationsCount: Int = 0,
    /** FASE 76.4: feature key → how often this account opened it from Home (local only). */
    val featureUsage: Map<String, Int> = emptyMap(),
    val errorMessage: String? = null,
    /** FASE 77.5.3: the student's class that is running right now, if any. */
    val activeClassSession: ActiveClassSessionDto? = null
)

private val INDONESIAN_DAY_NAMES = mapOf(
    Calendar.MONDAY to "Senin",
    Calendar.TUESDAY to "Selasa",
    Calendar.WEDNESDAY to "Rabu",
    Calendar.THURSDAY to "Kamis",
    Calendar.FRIDAY to "Jumat",
    Calendar.SATURDAY to "Sabtu",
    Calendar.SUNDAY to "Minggu"
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val sessionManager: SessionManager,
    private val featureUsageRepository: FeatureUsageRepository,
    private val attendanceRepository: AttendanceRepository,
    private val classSessionRepository: ClassSessionRepository
) : ViewModel() {

    private var activeSessionJob: Job? = null

    /**
     * FASE 77.5.3: checks `student/class-session/active` every 60 s while Home
     * is visible (a class lasts ~45 min, so a minute is soon enough and cheap on
     * battery). Stops for good if the server has no class-session routes yet.
     */
    fun startActiveClassPolling() {
        if (activeSessionJob?.isActive == true) return
        activeSessionJob = viewModelScope.launch {
            while (isActive) {
                when (val result = classSessionRepository.getActiveSessionForStudent()) {
                    is ClassSessionResult.Success ->
                        _uiState.value = _uiState.value.copy(activeClassSession = result.data)
                    is ClassSessionResult.Failure -> {
                        // A failed check hides the banner rather than showing a stale class.
                        _uiState.value = _uiState.value.copy(activeClassSession = null)
                        if (result.error.kind == ClassSessionErrorKind.NOT_DEPLOYED) return@launch
                    }
                }
                delay(ClassSessionRules.STUDENT_ACTIVE_POLL_MS)
            }
        }
    }

    fun stopActiveClassPolling() {
        activeSessionJob?.cancel()
        activeSessionJob = null
    }

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
        viewModelScope.launch {
            sessionManager.userClassroomFlow.collect { classroom ->
                if (!classroom.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(studentClass = classroom)
                }
            }
        }
        viewModelScope.launch {
            featureUsageRepository.usageCounts().collect { counts ->
                _uiState.value = _uiState.value.copy(featureUsage = counts)
            }
        }
        loadHomeData()
    }

    /** FASE 76.4: count one open of [featureKey] (a quick-action key or a service route). */
    fun recordFeatureUse(featureKey: String) {
        viewModelScope.launch {
            // Counting is a nicety; a storage hiccup must never block navigation.
            runCatching { featureUsageRepository.recordTap(featureKey) }
        }
    }

    fun resetFeatureUsage() {
        viewModelScope.launch {
            runCatching { featureUsageRepository.reset() }
        }
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // The backend returns the whole week's schedule (no day filter param),
            // so "today's" classes must be filtered client-side.
            val todayName = INDONESIAN_DAY_NAMES[Calendar.getInstance().get(Calendar.DAY_OF_WEEK)]
            studentRepository.getSchedule().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> Unit
                    is NetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            todaySchedules = result.data
                                .filter { it.day.equals(todayName, ignoreCase = true) }
                                .sortedBy { it.startTime },
                            isLoading = false
                        )
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }

            // Home has no attendance card; this load only feeds the attendance
            // home-screen widget (AttendanceRepository writes its snapshot).
            viewModelScope.launch {
                attendanceRepository.getAttendanceHistory().collect { }
            }

            viewModelScope.launch {
                studentRepository.getUnreadNotificationCount().collect { result ->
                    if (result is NetworkResult.Success) {
                        _uiState.value = _uiState.value.copy(unreadNotificationsCount = result.data)
                    }
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
