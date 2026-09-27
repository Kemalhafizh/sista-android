package com.sultanagung1.sista.ui.portal

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.storage.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Backs the shared, device-local "favorite modules" pin shown by both EnterpriseCatalogScreen and ModuleFavoritesScreen. */
@HiltViewModel
class ModuleCatalogViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.favoriteModuleIdsFlow.collect { _favoriteIds.value = it }
        }
    }

    fun toggleFavorite(moduleId: String) {
        viewModelScope.launch {
            sessionManager.toggleFavoriteModule(moduleId)
        }
    }
}
