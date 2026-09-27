package com.sultanagung1.sista.ui.library

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.BookItem
import com.sultanagung1.sista.data.model.BookLoanItem
import com.sultanagung1.sista.data.repository.LibraryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LibraryUiState(
    val isLoading: Boolean = false,
    val books: List<BookItem> = emptyList(),
    val myLoans: List<BookLoanItem> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = "Semua",
    val selectedTab: Int = 0, // 0: Katalog Buku, 1: Pinjaman Aktif
    val checkoutSuccess: Boolean = false,
    val errorMessage: String? = null
)


@HiltViewModel
class LibraryViewModel @Inject constructor(private val repository: LibraryRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadData(query, _uiState.value.selectedCategory)
    }

    fun selectCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
        val catFilter = if (category == "Semua") null else category
        loadData(_uiState.value.searchQuery, catFilter)
    }

    /** Reloads with the current search and category (pull-to-refresh, retry). */
    fun refresh() {
        val state = _uiState.value
        loadData(
            query = state.searchQuery.ifBlank { null },
            category = state.selectedCategory.takeUnless { it == "Semua" }
        )
    }

    fun loadData(query: String? = null, category: String? = null) {
        viewModelScope.launch {
            // Clear the previous error, or it would stay on screen after a successful retry.
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getBooks(query, category).collect { res ->
                when (res) {
                    is NetworkResult.Success -> _uiState.update { it.copy(books = res.data) }
                    is NetworkResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                    is NetworkResult.Loading -> Unit
                }
            }
            repository.getMyLoans().collect { res ->
                when (res) {
                    is NetworkResult.Success -> _uiState.update { it.copy(myLoans = res.data, isLoading = false) }
                    is NetworkResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }

    fun borrowBookByQr(qrCode: String, studentId: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.borrowBookByQr(qrCode, studentId).collect { res ->
                when (res) {
                    is NetworkResult.Success -> _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            checkoutSuccess = true,
                            myLoans = listOf(res.data) + state.myLoans
                        )
                    }
                    is NetworkResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = res.message) }
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }

    fun clearCheckoutSuccess() {
        _uiState.update { it.copy(checkoutSuccess = false) }
    }
}
