package com.sultanagung1.sista.ui.admin.sessions

import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.mvi.MviViewModel
import com.sultanagung1.sista.core.mvi.UiEffect
import com.sultanagung1.sista.core.mvi.UiEvent
import com.sultanagung1.sista.core.mvi.UiState
import com.sultanagung1.sista.core.storage.SessionManager
import com.sultanagung1.sista.core.util.DateUtils
import com.sultanagung1.sista.data.model.AttendanceReportDto
import com.sultanagung1.sista.data.model.ClassSessionDto
import com.sultanagung1.sista.data.model.ClassSessionErrorKind
import com.sultanagung1.sista.data.model.ClassSessionResult
import com.sultanagung1.sista.data.model.ClassSessionRules
import com.sultanagung1.sista.data.model.ClassSessionStatus
import com.sultanagung1.sista.data.repository.ClassSessionRepository
import com.sultanagung1.sista.ui.navigation.UserRoles
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

enum class ReportRange(val label: String, val days: Int) {
    WEEK("Rekap 7 Hari Terakhir", 7),
    MONTH("Rekap 30 Hari Terakhir", 30)
}

data class AdminSessionManagementState(
    /** yyyy-MM-dd; the list is always for one day. */
    val date: String = DateUtils.todayIso(),
    val statusFilter: ClassSessionStatus? = null,
    val sessions: List<ClassSessionDto> = emptyList(),
    val page: Int = 0,
    val lastPage: Int = 1,
    val total: Int = 0,
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val notDeployed: Boolean = false,
    val canCorrect: Boolean = false,
    val reportRange: ReportRange? = null,
    val report: AttendanceReportDto? = null,
    val isReportLoading: Boolean = false,
    val reportError: String? = null
) : UiState {
    val hasMore: Boolean get() = page in 1 until lastPage
    val dateLabel: String get() = ClassSessionRules.formatDateId(date) ?: date
}

sealed interface AdminSessionManagementEvent : UiEvent {
    data object ScreenStarted : AdminSessionManagementEvent
    data object Refresh : AdminSessionManagementEvent
    /** UTC midnight millis from the Material date picker. */
    data class DateSelected(val utcMillis: Long) : AdminSessionManagementEvent
    data class StatusFilterChanged(val status: ClassSessionStatus?) : AdminSessionManagementEvent
    data object LoadMore : AdminSessionManagementEvent
    data class OpenReport(val range: ReportRange) : AdminSessionManagementEvent
    data object CloseReport : AdminSessionManagementEvent
}

sealed interface AdminSessionManagementEffect : UiEffect

/**
 * FASE 77.6.1: every class session of a day, for the admin, the principal
 * (read-only), Waka Kurikulum and TU. Teacher/class filters from the design
 * need lists of teachers and classes that the mobile API does not offer yet,
 * so the filters are date and status.
 */
@HiltViewModel
class AdminSessionManagementViewModel @Inject constructor(
    private val repository: ClassSessionRepository,
    sessionManager: SessionManager
) : MviViewModel<AdminSessionManagementState, AdminSessionManagementEvent, AdminSessionManagementEffect>(
    AdminSessionManagementState()
) {

    private var started = false

    init {
        viewModelScope.launch {
            sessionManager.userRoleFlow.collect { role ->
                setState { copy(canCorrect = UserRoles.canCorrectClassAttendance(role)) }
            }
        }
    }

    override fun onEvent(event: AdminSessionManagementEvent) {
        when (event) {
            AdminSessionManagementEvent.ScreenStarted -> if (!started) {
                started = true
                reload()
            }
            AdminSessionManagementEvent.Refresh -> reload()
            is AdminSessionManagementEvent.DateSelected -> {
                setState { copy(date = isoOf(event.utcMillis)) }
                reload()
            }
            is AdminSessionManagementEvent.StatusFilterChanged -> {
                setState { copy(statusFilter = event.status) }
                reload()
            }
            AdminSessionManagementEvent.LoadMore -> loadPage(currentState.page + 1)
            is AdminSessionManagementEvent.OpenReport -> loadReport(event.range)
            AdminSessionManagementEvent.CloseReport -> setState { copy(reportRange = null, report = null, reportError = null) }
        }
    }

    private fun reload() {
        setState { copy(sessions = emptyList(), page = 0, lastPage = 1, isLoading = true, errorMessage = null) }
        loadPage(1)
    }

    private fun loadPage(page: Int) {
        if (page > 1 && (currentState.isLoadingMore || !currentState.hasMore)) return
        if (page > 1) setState { copy(isLoadingMore = true) }
        val date = currentState.date
        val status = currentState.statusFilter
        viewModelScope.launch {
            val result = repository.getAdminSessions(date, status, page)
            // A newer filter was chosen while this page was loading.
            if (date != currentState.date || status != currentState.statusFilter) return@launch
            when (result) {
                is ClassSessionResult.Success -> {
                    val data = result.data
                    setState {
                        copy(
                            sessions = if (page == 1) data.items.orEmpty() else sessions + data.items.orEmpty(),
                            page = data.currentPage,
                            lastPage = data.lastPage,
                            total = data.total,
                            isLoading = false,
                            isLoadingMore = false,
                            errorMessage = null,
                            notDeployed = false
                        )
                    }
                }
                is ClassSessionResult.Failure -> setState {
                    copy(
                        isLoading = false,
                        isLoadingMore = false,
                        errorMessage = ClassSessionRules.genericMessage(result.error),
                        notDeployed = result.error.kind == ClassSessionErrorKind.NOT_DEPLOYED
                    )
                }
            }
        }
    }

    private fun loadReport(range: ReportRange) {
        setState { copy(reportRange = range, report = null, reportError = null, isReportLoading = true) }
        viewModelScope.launch {
            val end = DateUtils.todayIso()
            val start = DateUtils.daysAgoIso(range.days - 1)
            when (val result = repository.getAttendanceReport(start, end, groupBy = "class")) {
                is ClassSessionResult.Success -> setState { copy(report = result.data, isReportLoading = false) }
                is ClassSessionResult.Failure -> setState {
                    copy(isReportLoading = false, reportError = ClassSessionRules.genericMessage(result.error))
                }
            }
        }
    }

    private fun isoOf(utcMillis: Long): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date(utcMillis))
}
