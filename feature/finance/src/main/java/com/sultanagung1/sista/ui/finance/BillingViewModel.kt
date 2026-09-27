package com.sultanagung1.sista.ui.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.mvi.UiEvent
import com.sultanagung1.sista.core.mvi.UiState
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.core.storage.SessionManager
import com.sultanagung1.sista.data.model.BillingInvoice
import com.sultanagung1.sista.data.model.PaymentVaResponse
import com.sultanagung1.sista.data.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface BillingUiEvent : UiEvent {
    data object LoadBillings : BillingUiEvent
    data class FilterInvoices(val filter: String) : BillingUiEvent
}

sealed interface BillingUiState : UiState {
    data object Loading : BillingUiState
    data class Content(
        val invoices: List<BillingInvoice>,
        val totalUnpaid: Double,
        val totalPaid: Double,
        val selectedFilter: String = "SEMUA"
    ) : BillingUiState
    data class Error(val message: String) : BillingUiState
}

sealed interface VaRequestState {
    data object Idle : VaRequestState
    data object Loading : VaRequestState
    data class Success(val va: PaymentVaResponse) : VaRequestState
    data class Error(val message: String) : VaRequestState
}

/** Billing.status values as the backend actually stores them (finance_tables migration). */
object BillingStatus {
    const val PAID = "paid"
    const val UNPAID = "unpaid"
    const val PARTIAL = "partial"
}

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    sessionManager: SessionManager
) : ViewModel() {

    val studentName: StateFlow<String?> = MutableStateFlow<String?>(null).also { flow ->
        viewModelScope.launch { sessionManager.userNameFlow.collect { flow.value = it } }
    }.asStateFlow()

    val studentClass: StateFlow<String?> = MutableStateFlow<String?>(null).also { flow ->
        viewModelScope.launch { sessionManager.userClassroomFlow.collect { flow.value = it } }
    }.asStateFlow()

    private var allInvoices: List<BillingInvoice> = emptyList()

    private val _uiState = MutableStateFlow<BillingUiState>(BillingUiState.Loading)
    val uiState: StateFlow<BillingUiState> = _uiState.asStateFlow()

    private val _filteredInvoices = MutableStateFlow<List<BillingInvoice>>(emptyList())
    val filteredInvoices: StateFlow<List<BillingInvoice>> = _filteredInvoices.asStateFlow()

    private val _vaState = MutableStateFlow<VaRequestState>(VaRequestState.Idle)
    val vaState: StateFlow<VaRequestState> = _vaState.asStateFlow()

    private var currentFilter = "SEMUA"

    init {
        loadBillings()
    }

    fun onEvent(event: BillingUiEvent) {
        when (event) {
            is BillingUiEvent.LoadBillings -> loadBillings()
            is BillingUiEvent.FilterInvoices -> setFilter(event.filter)
        }
    }

    fun loadBillings() {
        viewModelScope.launch {
            _uiState.value = BillingUiState.Loading
            studentRepository.getBillings().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> Unit
                    is NetworkResult.Success -> {
                        allInvoices = result.data
                        val totalUnpaid = allInvoices.filter { it.status != BillingStatus.PAID }.sumOf { it.remainingBalance }
                        val totalPaid = allInvoices.filter { it.status == BillingStatus.PAID }.sumOf { it.amount }
                        _uiState.value = BillingUiState.Content(
                            invoices = allInvoices,
                            totalUnpaid = totalUnpaid,
                            totalPaid = totalPaid,
                            selectedFilter = currentFilter
                        )
                        updateFilteredInvoices()
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = BillingUiState.Error(result.message)
                    }
                }
            }
        }
    }

    fun setFilter(filter: String) {
        currentFilter = filter
        if (_uiState.value is BillingUiState.Content) {
            val content = _uiState.value as BillingUiState.Content
            _uiState.value = content.copy(selectedFilter = filter)
            updateFilteredInvoices()
        }
    }

    fun requestVa(billingId: Long, bank: String) {
        viewModelScope.launch {
            studentRepository.requestPaymentVa(billingId, bank).collect { result ->
                _vaState.value = when (result) {
                    is NetworkResult.Loading -> VaRequestState.Loading
                    is NetworkResult.Success -> VaRequestState.Success(result.data)
                    is NetworkResult.Error -> VaRequestState.Error(result.message)
                }
            }
        }
    }

    fun clearVaState() {
        _vaState.value = VaRequestState.Idle
    }

    private fun updateFilteredInvoices() {
        _filteredInvoices.value = when (currentFilter) {
            "UNPAID" -> allInvoices.filter { it.status != BillingStatus.PAID }
            "PAID" -> allInvoices.filter { it.status == BillingStatus.PAID }
            else -> allInvoices
        }
    }
}
