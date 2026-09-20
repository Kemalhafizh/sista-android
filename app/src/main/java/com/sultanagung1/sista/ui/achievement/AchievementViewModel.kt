package com.sultanagung1.sista.ui.achievement

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.AcademicCvSummary
import com.sultanagung1.sista.data.model.AchievementItem
import com.sultanagung1.sista.data.model.CertificateItem
import com.sultanagung1.sista.data.model.UploadAchievementRequest
import com.sultanagung1.sista.data.repository.AchievementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AchievementUiState(
    val isLoading: Boolean = false,
    val achievements: List<AchievementItem> = emptyList(),
    val certificates: List<CertificateItem> = emptyList(),
    val cvSummary: AcademicCvSummary? = null,
    val selectedTab: Int = 0, // 0: Portofolio Prestasi, 1: E-Sertifikat Digital, 2: Preview CV Akademik SNBP
    val uploadSuccess: Boolean = false,
    val errorMessage: String? = null
)


@HiltViewModel
class AchievementViewModel @Inject constructor(private val repository: AchievementRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AchievementUiState())
    val uiState: StateFlow<AchievementUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getAchievements().collect { res ->
                if (res is NetworkResult.Success) {
                    _uiState.update { it.copy(achievements = res.data) }
                }
            }
            repository.getCertificates().collect { res ->
                if (res is NetworkResult.Success) {
                    _uiState.update { it.copy(certificates = res.data) }
                }
            }
            repository.getAcademicCvSummary().collect { res ->
                if (res is NetworkResult.Success) {
                    _uiState.update { it.copy(cvSummary = res.data, isLoading = false) }
                }
            }
        }
    }

    fun uploadAchievement(title: String, field: String, level: String, organizer: String, date: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val req = UploadAchievementRequest(title, field, level, organizer, date, "base64_mock_cert")
            repository.uploadAchievement(req).collect { res ->
                if (res is NetworkResult.Success) {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            uploadSuccess = true,
                            achievements = listOf(res.data) + state.achievements
                        )
                    }
                }
            }
        }
    }

    fun clearUploadSuccess() {
        _uiState.update { it.copy(uploadSuccess = false) }
    }
}
