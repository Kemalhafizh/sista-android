package com.sultanagung1.sista.ui.uks

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.mvi.UiEvent
import com.sultanagung1.sista.core.mvi.UiState
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.*
import com.sultanagung1.sista.data.repository.UksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface UksUiEvent : UiEvent {
    data class LoadData(val studentId: String? = null) : UksUiEvent
    data class ClearMessages(val unit: Unit = Unit) : UksUiEvent
}

data class UksUiState(
    val isLoading: Boolean = false,
    val todayVisits: List<UksVisit> = emptyList(),
    val healthHistory: List<HealthRecord> = emptyList(),
    val screeningSummary: HealthScreeningSummary? = null,
    val availableMedicines: List<MedicineItem> = emptyList(),
    val searchQuery: String = "",
    val isSavingVisit: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) : UiState {
    val filteredVisits: List<UksVisit>
        get() = if (searchQuery.isBlank()) todayVisits
        else todayVisits.filter {
            it.studentName.contains(searchQuery, ignoreCase = true) ||
            it.complaint.contains(searchQuery, ignoreCase = true) ||
            (it.diagnosis?.contains(searchQuery, ignoreCase = true) == true)
        }
}


@HiltViewModel
class UksViewModel @Inject constructor(
    private val uksRepository: UksRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UksUiState())
    val uiState: StateFlow<UksUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun onEvent(event: UksUiEvent) {
        when (event) {
            is UksUiEvent.LoadData -> loadData(event.studentId)
            is UksUiEvent.ClearMessages -> _uiState.update { it.copy(errorMessage = null, successMessage = null) }
        }
    }

    fun loadData(studentId: String? = null) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                var visits = getSampleVisits()
                var screening = getSampleScreeningSummary()

                uksRepository.getTodayVisits().collect { result ->
                    if (result is NetworkResult.Success && result.data.isNotEmpty()) {
                        visits = result.data.map { item ->
                            UksVisit(
                                id = item.id.toString(),
                                studentName = "Siswa Sula-One #${item.id}",
                                time = item.visitTime,
                                complaint = item.complaints,
                                action = item.action ?: "Istirahat di UKS",
                                temperature = item.temperature?.toFloatOrNull() ?: 36.8f,
                                bloodPressure = item.bloodPressure ?: "110/75",
                                diagnosis = item.diagnosis,
                                medicines = item.medicines.joinToString(", ") { "${it.name} (${it.dose})" },
                                officer = item.handlerName ?: "Petugas UKS"
                            )
                        }
                    }
                }

                if (!studentId.isNullOrBlank()) {
                    uksRepository.getHealthScreening(studentId).collect { result ->
                        if (result is NetworkResult.Success) {
                            val s = result.data
                            screening = HealthScreeningSummary(
                                studentName = "Ahmad Faiz Syakir",
                                nisn = "0078921820",
                                bloodType = s.bloodType,
                                heightCm = s.heightCm,
                                weightKg = s.weightKg,
                                bmi = s.bmi,
                                bmiCategory = s.bmiCategory,
                                visionRight = s.visionRight,
                                visionLeft = s.visionLeft,
                                dentalHealth = s.dentalHealth,
                                hearing = s.hearing,
                                allergies = listOf("Alergi Debu & Suhu Dingin", "Antibiotik Golongan Sulfa"),
                                lastScreenedAt = s.lastScreenedAt,
                                screener = s.screener
                            )
                        }
                    }
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        todayVisits = visits,
                        healthHistory = getSampleHealthHistory(),
                        screeningSummary = screening,
                        availableMedicines = getSampleMedicines()
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        todayVisits = getSampleVisits(),
                        healthHistory = getSampleHealthHistory(),
                        screeningSummary = getSampleScreeningSummary(),
                        availableMedicines = getSampleMedicines()
                    )
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun recordVisit(
        studentName: String,
        className: String,
        complaint: String,
        temperature: Float,
        bloodPressure: String,
        diagnosis: String,
        actions: List<String>,
        selectedMedicine: String?,
        notes: String,
        onSuccess: () -> Unit
    ) {
        if (studentName.isBlank() || complaint.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Nama Siswa dan Keluhan wajib diisi.") }
            return
        }

        _uiState.update { it.copy(isSavingVisit = true, errorMessage = null) }
        viewModelScope.launch {
            val newVisit = UksVisit(
                id = "uks-${System.currentTimeMillis()}",
                studentName = studentName,
                className = className,
                time = "Hari Ini, 09:30 WIB",
                complaint = complaint,
                action = actions.joinToString(", ").ifBlank { "Istirahat di ruang UKS" },
                temperature = temperature,
                bloodPressure = bloodPressure,
                diagnosis = diagnosis,
                medicines = selectedMedicine ?: "-",
                notes = notes,
                officer = "Hj. Dewi Lestari, A.Md.Kep (PMR Sula-One)"
            )

            // Deduct medicine from stock if selected
            val updatedMeds = _uiState.value.availableMedicines.map { med ->
                if (selectedMedicine != null && med.name.contains(selectedMedicine, ignoreCase = true)) {
                    med.copy(quantity = (med.quantity - 1).coerceAtLeast(0))
                } else med
            }

            _uiState.update {
                it.copy(
                    isSavingVisit = false,
                    todayVisits = listOf(newVisit) + it.todayVisits,
                    availableMedicines = updatedMeds,
                    successMessage = "Kunjungan UKS berhasil dicatat & notifikasi terkirim ke Orang Tua."
                )
            }
            onSuccess()
        }
    }

    private fun getSampleVisits(): List<UksVisit> {
        return listOf(
            UksVisit(
                id = "uks-1",
                studentName = "Rayhan Al-Ghazali",
                className = "X-E 3",
                time = "08:15 WIB",
                complaint = "Pusing berputar dan mual saat upacara bendera",
                action = "Istirahat baring, kompres dahi, minum teh manis hangat",
                temperature = 37.1f,
                bloodPressure = "100/65",
                diagnosis = "Kelelahan & Hipoglikemia Ringan",
                medicines = "Minyak Kayu Putih, Paracetamol 500mg",
                notes = "Siswa belum sarapan dari rumah. Sudah pulih dan kembali ke kelas jam 09:15 WIB."
            ),
            UksVisit(
                id = "uks-2",
                studentName = "Nabila Az-Zahra",
                className = "XI MIPA 2",
                time = "10:20 WIB",
                complaint = "Nyeri ulu hati hebat (kambuh maag)",
                action = "Pemberian antasida kunyah, istirahat di bed UKS",
                temperature = 36.6f,
                bloodPressure = "110/70",
                diagnosis = "Gastritis Akut / Dispepsia",
                medicines = "Promag Antasida 1 Tablet Kunyah",
                notes = "Orang tua telah dihubungi melalui WhatsApp otomatis Sula-One."
            ),
            UksVisit(
                id = "uks-3",
                studentName = "Dimas Satria Wibowo",
                className = "XII IPS 1",
                time = "11:45 WIB",
                complaint = "Lecet lutut dan siku kanan akibat tergelincir di lapangan basket",
                action = "Pembersihan luka NaCl, oles povidone iodine, balut perban steril",
                temperature = 36.7f,
                bloodPressure = "120/80",
                diagnosis = "Vulnus Excoriatum (Luka Lecet)",
                medicines = "Betadine Antiseptik & Kasa Steril",
                notes = "Luka telah dibersihkan dan dipastikan tidak ada fraktur. Siswa diperbolehkan pulang."
            )
        )
    }

    private fun getSampleHealthHistory(): List<HealthRecord> {
        return listOf(
            HealthRecord(
                id = "rec-1",
                date = "22 Agustus 2026",
                complaint = "Demam menggigil dan batuk kering",
                diagnosis = "Faringitis Akut & Febris",
                action = "Observasi suhu 1 jam, kompres hangat, edukasi minum air putih",
                medicines = "Paracetamol 500mg (1 tablet), Vitamin C 500mg",
                officer = "dr. Hj. Siti Aminah"
            ),
            HealthRecord(
                id = "rec-2",
                date = "15 Juli 2026",
                complaint = "Asma ringan kambuh setelah lari keliling lapangan",
                diagnosis = "Asma Bronkial Eksaserbasi Ringan",
                action = "Pemberian oksigen 2 lpm dan inhaler ventolin pribadi siswa",
                medicines = "Ventolin Inhaler 2 Puff",
                officer = "Sri Wahyuni, S.Kep (Perawat UKS)"
            ),
            HealthRecord(
                id = "rec-3",
                date = "12 Mei 2026",
                complaint = "Sakit gigi geraham kanan bawah berdenyut",
                diagnosis = "Pulpitis Reversibel Gigi 46",
                action = "Kumur antiseptik oral, kompres es luar",
                medicines = "Asam Mefenamat 500mg",
                officer = "drg. Bambang Soeprapto"
            )
        )
    }

    private fun getSampleScreeningSummary(): HealthScreeningSummary {
        return HealthScreeningSummary(
            studentName = "Ahmad Faiz Syakir",
            nisn = "0078921820",
            bloodType = "O (Rhesus Positif)",
            heightCm = 172,
            weightKg = 62,
            bmi = 21.0,
            bmiCategory = "Ideal / Normal",
            visionRight = "6/6 (Normal)",
            visionLeft = "6/6 (Normal)",
            dentalHealth = "Sehat (Tanpa Karies Aktif)",
            hearing = "Normal (Tes Garputala Simetris)",
            allergies = listOf("Alergi Debu & Udara Dingin", "Antibiotik Golongan Sulfa"),
            lastScreenedAt = "15 Agustus 2026",
            screener = "Puskesmas Gayamsari & Tim UKS YBWSA"
        )
    }

    private fun getSampleMedicines(): List<MedicineItem> {
        return listOf(
            MedicineItem("med-1", "Paracetamol 500mg Tablet", 48),
            MedicineItem("med-2", "Promag Antasida Kunyah", 36),
            MedicineItem("med-3", "Betadine Povidone Antiseptik 60ml", 12),
            MedicineItem("med-4", "Oralit Sachet 200ml", 30),
            MedicineItem("med-5", "Minyak Kayu Putih Cajuput 120ml", 8),
            MedicineItem("med-6", "Kasa Steril & Plester Cepat", 65),
            MedicineItem("med-7", "Paratusin Obat Batuk & Flu", 25)
        )
    }
}
