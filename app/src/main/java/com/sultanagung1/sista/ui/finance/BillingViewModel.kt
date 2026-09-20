package com.sultanagung1.sista.ui.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.mvi.UiEvent
import com.sultanagung1.sista.core.mvi.UiState
import com.sultanagung1.sista.data.model.BillingInvoice
import com.sultanagung1.sista.data.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

sealed interface BillingUiEvent : UiEvent {
    data object LoadBillings : BillingUiEvent
    data class FilterInvoices(val filter: String) : BillingUiEvent
}

sealed interface BillingUiState : UiState {
    data object Loading : BillingUiState
    data class Content(
        val invoices: List<BillingInvoice>,
        val totalUnpaid: Long,
        val totalPaid: Long,
        val selectedFilter: String = "SEMUA"
    ) : BillingUiState
    data class Error(val message: String) : BillingUiState
}

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val sampleBillings = listOf(
        BillingInvoice(1, "SPP & Syahriah September 2026", 750000, "Rp 750.000", "10 Sep 2026", "UNPAID", "Bank Syariah Indonesia", "8821900699112"),
        BillingInvoice(2, "Iuran Praktikum Lab Komputer & AI (Semester 1)", 250000, "Rp 250.000", "15 Sep 2026", "UNPAID", "Bank Jateng Syariah", "9912000699112"),
        BillingInvoice(3, "Wakaf Pembangunan Lab Robotik & STEM", 500000, "Rp 500.000", "30 Sep 2026", "UNPAID", "BSI Virtual Account", "8821900699112"),
        BillingInvoice(4, "SPP & Syahriah Agustus 2026", 750000, "Rp 750.000", "10 Agu 2026", "PAID", "BSI Virtual Account", "8821900699112"),
        BillingInvoice(5, "SPP & Syahriah Juli 2026", 750000, "Rp 750.000", "10 Jul 2026", "PAID", "BSI Virtual Account", "8821900699112"),
        BillingInvoice(6, "Daftar Ulang & Seragam Khas Yayasan", 1200000, "Rp 1.200.000", "05 Jul 2026", "PAID", "Bank Jateng Syariah", "9912000699112")
    )

    private val _uiState = MutableStateFlow<BillingUiState>(BillingUiState.Loading)
    val uiState: StateFlow<BillingUiState> = _uiState.asStateFlow()

    private val _filteredInvoices = MutableStateFlow<List<BillingInvoice>>(emptyList())
    val filteredInvoices: StateFlow<List<BillingInvoice>> = _filteredInvoices.asStateFlow()

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
            delay(500) // Simulate network delay
            try {
                val totalUnpaid = sampleBillings.filter { it.status == "UNPAID" }.sumOf { it.amount }
                val totalPaid = sampleBillings.filter { it.status == "PAID" }.sumOf { it.amount }
                _uiState.value = BillingUiState.Content(
                    invoices = sampleBillings,
                    totalUnpaid = totalUnpaid,
                    totalPaid = totalPaid,
                    selectedFilter = currentFilter
                )
                updateFilteredInvoices()
            } catch (e: Exception) {
                _uiState.value = BillingUiState.Error(e.message ?: "Unknown error occurred")
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

    private fun updateFilteredInvoices() {
        val invoices = sampleBillings
        _filteredInvoices.value = when (currentFilter) {
            "UNPAID" -> invoices.filter { it.status == "UNPAID" }
            "PAID" -> invoices.filter { it.status == "PAID" }
            else -> invoices
        }
    }
}
