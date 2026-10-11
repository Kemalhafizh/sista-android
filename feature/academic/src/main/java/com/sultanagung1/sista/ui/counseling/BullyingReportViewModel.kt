package com.sultanagung1.sista.ui.counseling

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.repository.BullyingReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class BullyingReportUiState(
    val form: BullyingReportForm = BullyingReportForm(),
    /** Errors show only after the first try to send. */
    val showErrors: Boolean = false,
    val isSending: Boolean = false,
    /** The server's reason when it refused or could not be reached; the form keeps what was typed. */
    val errorMessage: String? = null,
    /** Set once the server stored the report. */
    val submitted: BullyingReportRepository.Submitted? = null,
)

/**
 * "Lapor Perundungan": a report goes to the school's counsellors through
 * POST counseling/bullying-reports. Nothing is shown as sent until the server
 * has stored it.
 */
@HiltViewModel
class BullyingReportViewModel @Inject constructor(
    private val repository: BullyingReportRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BullyingReportUiState())
    val uiState: StateFlow<BullyingReportUiState> = _uiState.asStateFlow()

    fun update(change: (BullyingReportForm) -> BullyingReportForm) {
        _uiState.update { it.copy(form = change(it.form), errorMessage = null) }
    }

    fun submit(today: LocalDate = BullyingReportRules.today()) {
        val state = _uiState.value
        if (state.isSending) return
        if (!BullyingReportRules.canSubmit(state.form, today)) {
            _uiState.update { it.copy(showErrors = true) }
            return
        }
        viewModelScope.launch {
            repository.submit(BullyingReportRules.toRequest(state.form)).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.update { it.copy(isSending = true, errorMessage = null) }
                    is NetworkResult.Success -> _uiState.update { it.copy(isSending = false, submitted = result.data) }
                    is NetworkResult.Error -> _uiState.update { it.copy(isSending = false, errorMessage = result.message) }
                }
            }
        }
    }

    /** Starts a new, empty report after one was sent. */
    fun reset() {
        _uiState.value = BullyingReportUiState()
    }
}
