package com.sultanagung1.sista.ui.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.core.storage.SessionManager
import com.sultanagung1.sista.core.util.DateUtils
import com.sultanagung1.sista.data.model.ClassSessionDto
import com.sultanagung1.sista.data.model.ClassSessionErrorKind
import com.sultanagung1.sista.data.model.ClassSessionResult
import com.sultanagung1.sista.data.model.TeacherScheduleSlot
import com.sultanagung1.sista.data.model.TeachingJournalEntry
import com.sultanagung1.sista.data.repository.NotificationRepository
import com.sultanagung1.sista.data.repository.ClassSessionRepository
import com.sultanagung1.sista.data.repository.TeacherRepository
import com.sultanagung1.sista.data.repository.TeachingJournalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeacherUiState(
    val isLoading: Boolean = false,
    // Real identity from the logged-in session (SessionManager), not a hardcoded name.
    val teacherName: String = "",
    val nip: String = "",
    val totalClasses: Int = 0,
    // Sum of every scheduled session's duration this week (real, computed from
    // teacher/schedule's session_start/session_end) — not a hardcoded "24 jam" target.
    val teachingHoursThisWeek: Double = 0.0,
    val todaySchedules: List<TeacherScheduleSlot> = emptyList(),
    val recentJournals: List<TeachingJournalEntry> = emptyList(),
    val errorMessage: String? = null,
    /** FASE 77.7.2: today's class sessions for the dashboard's "Mulai/Kembali ke Kelas" card. */
    val classSessions: List<ClassSessionDto> = emptyList(),
    /** False until loaded, and while the server has no class-session routes (the card hides). */
    val classSessionsAvailable: Boolean = false,
    val nowMinutes: Int = DateUtils.nowMinutesOfDay(),
    val unreadNotifications: Int = 0,
)

/**
 * Real teacher dashboard state. Previously this ViewModel called
 * `mobile/teacher/dashboard` / `mobile/teacher/classes/{id}/students` /
 * `mobile/teacher/attendance/submit` — routes that do not exist anywhere in
 * the backend (confirmed against routes/api.php), so every load unconditionally
 * fell back to a hardcoded "Ustadz Ahmad Fauzi, M.Pd" identity and fabricated
 * schedule/journal data. It now composes [TeacherRepository] (classes,
 * students, attendance submit) with [TeachingJournalRepository] (schedule,
 * journals — the same real source JournalMobileViewModel uses) and
 * [SessionManager] (real logged-in identity). No fallback of any kind: a
 * load failure surfaces as a real error.
 */
@HiltViewModel
class TeacherViewModel @Inject constructor(
    private val teacherRepository: TeacherRepository,
    private val journalRepository: TeachingJournalRepository,
    private val sessionManager: SessionManager,
    private val classSessionRepository: ClassSessionRepository,
    private val notificationRepository: NotificationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherUiState())
    val uiState: StateFlow<TeacherUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    /** The bell on the home; reloaded whenever the home comes back into view. */
    fun loadUnreadNotifications() {
        viewModelScope.launch {
            notificationRepository.getUnreadCount().collect { result ->
                if (result is NetworkResult.Success) _uiState.update { it.copy(unreadNotifications = result.data) }
            }
        }
    }

    fun loadDashboard() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        loadClassSessions()
        viewModelScope.launch {
            val name = sessionManager.userNameFlow.first().orEmpty()
            val nip = sessionManager.userIdentifierFlow.first().orEmpty()
            _uiState.update { it.copy(teacherName = name, nip = nip) }

            teacherRepository.getClasses().collect { classResult ->
                when (classResult) {
                    is NetworkResult.Success -> {
                        _uiState.update { it.copy(totalClasses = classResult.data.size) }
                        loadScheduleAndJournals()
                    }
                    is NetworkResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = classResult.message) }
                    }
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }

    /** Separate from the rest: a missing FASE 117 backend must not fail the dashboard. */
    fun loadClassSessions() {
        viewModelScope.launch {
            when (val result = classSessionRepository.getTodaySessions()) {
                is ClassSessionResult.Success -> _uiState.update {
                    it.copy(classSessions = result.data, classSessionsAvailable = true, nowMinutes = DateUtils.nowMinutesOfDay())
                }
                is ClassSessionResult.Failure -> _uiState.update {
                    // Keep the last card on a transient failure; hide it if the backend is missing.
                    it.copy(classSessionsAvailable = it.classSessionsAvailable && result.error.kind != ClassSessionErrorKind.NOT_DEPLOYED)
                }
            }
        }
    }

    private fun loadScheduleAndJournals() {
        viewModelScope.launch {
            journalRepository.getTeacherSchedule().collect { scheduleResult ->
                when (scheduleResult) {
                    is NetworkResult.Success -> {
                        val today = DateUtils.todayDayNameIndonesian()
                        val todaySlots = scheduleResult.data
                            .filter { it.day.equals(today, ignoreCase = true) }
                            .sortedBy { it.sessionStart }
                        val weeklyHours = scheduleResult.data.sumOf { sessionDurationHours(it.sessionStart, it.sessionEnd) }
                        _uiState.update { it.copy(todaySchedules = todaySlots, teachingHoursThisWeek = weeklyHours) }
                        loadJournals()
                    }
                    is NetworkResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = scheduleResult.message) }
                    }
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }

    private fun loadJournals() {
        viewModelScope.launch {
            journalRepository.getTeacherJournals().collect { journalResult ->
                when (journalResult) {
                    is NetworkResult.Success -> {
                        val recent = journalResult.data.sortedByDescending { it.teachingDate }.take(5)
                        _uiState.update { it.copy(isLoading = false, recentJournals = recent) }
                    }
                    is NetworkResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = journalResult.message) }
                    }
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }

    private fun sessionDurationHours(start: String?, end: String?): Double {
        fun minutesOf(time: String?): Int? {
            if (time == null) return null
            val match = Regex("""(\d{1,2}):(\d{2})""").find(time) ?: return null
            val (h, m) = match.destructured
            return h.toInt() * 60 + m.toInt()
        }
        val startMin = minutesOf(start) ?: return 0.0
        val endMin = minutesOf(end) ?: return 0.0
        return (endMin - startMin).coerceAtLeast(0) / 60.0
    }
}
