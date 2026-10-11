package com.sultanagung1.sista.ui.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.BillingInvoice
import com.sultanagung1.sista.data.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BillingUiState(
    val isLoading: Boolean = true,
    /** Null until the server answered. */
    val invoices: List<BillingInvoice>? = null,
    val filter: BillingRules.Filter = BillingRules.Filter.ALL,
    val errorMessage: String? = null,
) {
    val shown: List<BillingInvoice> get() = invoices?.let { BillingRules.filter(it, filter) }.orEmpty()
    val summary: BillingRules.Summary? get() = invoices?.let(BillingRules::summary)
}

/**
 * "Tagihan": GET student/billings, read only. Paying happens at the school's
 * treasurer; the app has no payment channel yet (student/billings/{id}/pay
 * answers 501), so there is no pay button.
 */
@HiltViewModel
class BillingViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BillingUiState())
    val uiState: StateFlow<BillingUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            studentRepository.getBillings().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    is NetworkResult.Success -> _uiState.update {
                        it.copy(isLoading = false, invoices = BillingRules.ordered(result.data), errorMessage = null)
                    }
                    is NetworkResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun setFilter(filter: BillingRules.Filter) {
        _uiState.update { it.copy(filter = filter) }
    }
}
