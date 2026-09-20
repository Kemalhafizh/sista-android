package com.sultanagung1.sista.ui.ibadah

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.MutabaahItem
import com.sultanagung1.sista.data.model.TahsinRecordItem
import com.sultanagung1.sista.data.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class IbadahUiState(
    val isLoading: Boolean = false,
    val mutabaahItems: List<MutabaahItem> = emptyList(),
    val tahsinRecords: List<TahsinRecordItem> = emptyList(),
    val isRecording: Boolean = false,
    val recordingDurationSeconds: Int = 0,
    val isUploadSuccess: Boolean = false,
    val errorMessage: String? = null
)


@HiltViewModel
class IbadahViewModel @Inject constructor(private val studentRepository: StudentRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(IbadahUiState())
    val uiState: StateFlow<IbadahUiState> = _uiState.asStateFlow()

    init {
        loadMutabaah()
        loadTahsinHistory()
    }

    fun loadMutabaah() {
        val defaultItems = listOf(
            MutabaahItem("1", "Salat Tahajud", "Melaksanakan salat malam minimal 2 rakaat", "SUNNAH", true),
            MutabaahItem("2", "Salat Subuh Berjamaah", "Di masjid bagi ikhwan, awal waktu bagi akhwat", "FARDHU", true),
            MutabaahItem("3", "Tadarus Al-Qur'an 1 Juz (One Day One Juz)", "Membaca tilawah tartil", "SUNNAH", true),
            MutabaahItem("4", "Salat Dhuha", "Minimal 2 rakaat di pagi hari", "SUNNAH", true),
            MutabaahItem("5", "Salat Dzuhur Berjamaah", "Berjamaah di masjid sekolah", "FARDHU", false),
            MutabaahItem("6", "Salat Ashar Berjamaah", "Berjamaah di masjid sekolah", "FARDHU", false),
            MutabaahItem("7", "Al-Ma'tsurat Sore & Dzikir Petang", "Dzikir penjagaan diri", "ADAB", false)
        )
        _uiState.value = _uiState.value.copy(mutabaahItems = defaultItems)
    }

    fun toggleMutabaah(id: String) {
        val updated = _uiState.value.mutabaahItems.map {
            if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
        }
        _uiState.value = _uiState.value.copy(mutabaahItems = updated)
    }

    fun loadTahsinHistory() {
        val sampleTahsin = listOf(
            TahsinRecordItem(1, "QS. Al-Baqarah", "Ayat 255-257 (Ayat Kursi)", "https://audio.sultanagung.sch.id/tahsin/1.m4a", "95 (Mumtaz)", "Makharijul huruf sangat fasih, pertahankan mad wajib.", "PASSED"),
            TahsinRecordItem(2, "QS. Ali 'Imran", "Ayat 190-194", "https://audio.sultanagung.sch.id/tahsin/2.m4a", "88 (Jayyid Jiddan)", "Perhatikan ghunnah pada nun bertasydid.", "PASSED"),
            TahsinRecordItem(3, "QS. An-Nisa", "Ayat 1-5", "https://audio.sultanagung.sch.id/tahsin/3.m4a", null, "Sedang dinilai oleh Ustadz Pengampu Tahfidz", "SUBMITTED")
        )
        _uiState.value = _uiState.value.copy(tahsinRecords = sampleTahsin)
    }

    fun startRecording() {
        _uiState.value = _uiState.value.copy(isRecording = true, recordingDurationSeconds = 0)
    }

    fun stopRecording() {
        _uiState.value = _uiState.value.copy(isRecording = false)
    }
}
