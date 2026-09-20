package com.sultanagung1.sista.ui.teacher

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.sultanagung1.sista.core.mvi.UiEvent
import com.sultanagung1.sista.core.mvi.UiState
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.core.storage.FormDraftStore
import com.sultanagung1.sista.core.storage.SessionManager
import com.sultanagung1.sista.data.model.*
import com.sultanagung1.sista.data.repository.TeachingJournalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** Serialized shape written to [FormDraftStore] by [JournalMobileViewModel], keyed per schedule slot. */
private data class JournalDraftPayload(
    val materiPokok: String,
    val metode: String,
    val media: String,
    val hadir: String,
    val absen: String,
    val kompetensiTercapai: Boolean,
    val catatan: String,
    val tindakLanjut: String
)

sealed interface JournalMobileUiEvent : UiEvent {
    data class LoadSchedules(val unit: Unit = Unit) : JournalMobileUiEvent
    data class SetSelectedTab(val tabIndex: Int) : JournalMobileUiEvent
    data class ClearMessages(val unit: Unit = Unit) : JournalMobileUiEvent
}

data class JournalMobileUiState(
    val isLoading: Boolean = false,
    val selectedTab: Int = 0, // 0: Hari Ini, 1: Minggu Ini, 2: Bulan Ini
    val todaySchedule: List<JournalScheduleItem> = emptyList(),
    val weekJournals: List<JournalScheduleItem> = emptyList(),
    val monthJournals: List<JournalScheduleItem> = emptyList(),
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    // FASE 69.1: an in-progress KBM journal entry (materi/catatan/tindak lanjut)
    // survives process death instead of resetting to blank, keyed by scheduleId.
    val draftScheduleId: String = "",
    val draftMateriPokok: String = "",
    val draftMetode: String = "",
    val draftMedia: String = "",
    val draftHadir: String = "",
    val draftAbsen: String = "",
    val draftKompetensiTercapai: Boolean = true,
    val draftCatatan: String = "",
    val draftTindakLanjut: String = "",
    // FASE 69.2: a persisted draft from a fully-closed previous session was found for
    // the currently-open schedule slot — the screen should offer to restore it.
    val restorableDraftAvailable: Boolean = false
) : UiState {
    val compliance: JournalSummaryCompliance
        get() {
            val total = todaySchedule.size
            val filled = todaySchedule.count { it.isFilled }
            val percent = if (total > 0) (filled * 100) / total else 0
            return JournalSummaryCompliance(
                totalScheduled = total,
                filledCount = filled,
                compliancePercentage = percent,
                unfilledDaysWarning = todaySchedule.any { !it.isFilled }
            )
        }

    val currentTabSchedules: List<JournalScheduleItem>
        get() = when (selectedTab) {
            0 -> todaySchedule
            1 -> weekJournals
            else -> monthJournals
        }
}


/**
 * Real KBM journal state, backed by [TeachingJournalRepository] — real
 * `teacher/schedule` (weekly recurring slots) merged with real
 * `teacher/journals` (dated, persisted entries). No sample/hardcoded
 * fallback: a load failure surfaces as a real error, an empty result
 * surfaces as a real empty state.
 */
@HiltViewModel
class JournalMobileViewModel @Inject constructor(
    private val journalRepository: TeachingJournalRepository,
    private val savedStateHandle: SavedStateHandle,
    private val formDraftStore: FormDraftStore,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val gson = Gson()
    private var cachedUserId: String? = null

    private val _uiState = MutableStateFlow(
        JournalMobileUiState(
            draftScheduleId = savedStateHandle.get<String>(KEY_DRAFT_SCHEDULE_ID) ?: "",
            draftMateriPokok = savedStateHandle.get<String>(KEY_DRAFT_MATERI) ?: "",
            draftMetode = savedStateHandle.get<String>(KEY_DRAFT_METODE) ?: "",
            draftMedia = savedStateHandle.get<String>(KEY_DRAFT_MEDIA) ?: "",
            draftHadir = savedStateHandle.get<String>(KEY_DRAFT_HADIR) ?: "",
            draftAbsen = savedStateHandle.get<String>(KEY_DRAFT_ABSEN) ?: "",
            draftKompetensiTercapai = savedStateHandle.get<Boolean>(KEY_DRAFT_KOMPETENSI) ?: true,
            draftCatatan = savedStateHandle.get<String>(KEY_DRAFT_CATATAN) ?: "",
            draftTindakLanjut = savedStateHandle.get<String>(KEY_DRAFT_TINDAK_LANJUT) ?: ""
        )
    )
    val uiState: StateFlow<JournalMobileUiState> = _uiState.asStateFlow()

    private var latestSchedule: List<TeacherScheduleSlot> = emptyList()
    private var latestJournals: List<TeachingJournalEntry> = emptyList()

    init {
        loadSchedules()
    }

    /** Loads the draft for [scheduleId], or seeds it from the (possibly already-filled) schedule on first entry. */
    fun startDraftFor(scheduleId: String, schedule: JournalScheduleItem?) {
        if (_uiState.value.draftScheduleId == scheduleId) return
        savedStateHandle[KEY_DRAFT_SCHEDULE_ID] = scheduleId
        savedStateHandle[KEY_DRAFT_MATERI] = schedule?.topic ?: ""
        savedStateHandle[KEY_DRAFT_METODE] = schedule?.method ?: ""
        savedStateHandle[KEY_DRAFT_MEDIA] = schedule?.media ?: ""
        savedStateHandle[KEY_DRAFT_HADIR] = schedule?.attendancePresent?.toString() ?: ""
        savedStateHandle[KEY_DRAFT_ABSEN] = schedule?.attendanceAbsent?.toString() ?: ""
        savedStateHandle[KEY_DRAFT_KOMPETENSI] = schedule?.isCompetencyAchieved ?: true
        savedStateHandle[KEY_DRAFT_CATATAN] = schedule?.notes ?: ""
        savedStateHandle[KEY_DRAFT_TINDAK_LANJUT] = schedule?.followUp ?: ""
        _uiState.update {
            it.copy(
                draftScheduleId = scheduleId,
                draftMateriPokok = schedule?.topic ?: "",
                draftMetode = schedule?.method ?: "",
                draftMedia = schedule?.media ?: "",
                draftHadir = schedule?.attendancePresent?.toString() ?: "",
                draftAbsen = schedule?.attendanceAbsent?.toString() ?: "",
                draftKompetensiTercapai = schedule?.isCompetencyAchieved ?: true,
                draftCatatan = schedule?.notes ?: "",
                draftTindakLanjut = schedule?.followUp ?: ""
            )
        }
        // Only a genuinely blank slot (no filed journal yet) is worth checking for a
        // leftover local draft — an already-filled entry has nothing to "resume".
        if (schedule == null || !schedule.isFilled) {
            checkForRestorableDraft(scheduleId)
        }
    }

    private fun checkForRestorableDraft(scheduleId: String) {
        viewModelScope.launch {
            cachedUserId = sessionManager.userIdFlow.first()
            formDraftStore.getDraft(formIdFor(scheduleId)) ?: return@launch
            if (_uiState.value.draftScheduleId == scheduleId) {
                _uiState.update { it.copy(restorableDraftAvailable = true) }
            }
        }
    }

    fun restorePersistedDraft() {
        val scheduleId = _uiState.value.draftScheduleId
        if (scheduleId.isBlank()) return
        viewModelScope.launch {
            val stored = formDraftStore.getDraft(formIdFor(scheduleId)) ?: run {
                _uiState.update { it.copy(restorableDraftAvailable = false) }
                return@launch
            }
            val payload = try {
                gson.fromJson(stored.payloadJson, JournalDraftPayload::class.java)
            } catch (_: Exception) {
                null
            }
            if (payload != null) {
                updateDraftMateriPokok(payload.materiPokok)
                updateDraftMetode(payload.metode)
                updateDraftMedia(payload.media)
                updateDraftHadir(payload.hadir)
                updateDraftAbsen(payload.absen)
                updateDraftKompetensi(payload.kompetensiTercapai)
                updateDraftCatatan(payload.catatan)
                updateDraftTindakLanjut(payload.tindakLanjut)
            }
            _uiState.update { it.copy(restorableDraftAvailable = false) }
        }
    }

    fun discardPersistedDraft() {
        val scheduleId = _uiState.value.draftScheduleId
        if (scheduleId.isNotBlank()) formDraftStore.clearDraft(formIdFor(scheduleId))
        _uiState.update { it.copy(restorableDraftAvailable = false) }
    }

    private fun persistDraftSnapshot() {
        val state = _uiState.value
        if (state.draftScheduleId.isBlank()) return
        val payload = JournalDraftPayload(
            materiPokok = state.draftMateriPokok,
            metode = state.draftMetode,
            media = state.draftMedia,
            hadir = state.draftHadir,
            absen = state.draftAbsen,
            kompetensiTercapai = state.draftKompetensiTercapai,
            catatan = state.draftCatatan,
            tindakLanjut = state.draftTindakLanjut
        )
        formDraftStore.autoSave(formIdFor(state.draftScheduleId), cachedUserId, gson.toJson(payload))
    }

    private fun formIdFor(scheduleId: String) = "journal_form_$scheduleId"

    fun updateDraftMateriPokok(v: String) { savedStateHandle[KEY_DRAFT_MATERI] = v; _uiState.update { it.copy(draftMateriPokok = v) }; persistDraftSnapshot() }
    fun updateDraftMetode(v: String) { savedStateHandle[KEY_DRAFT_METODE] = v; _uiState.update { it.copy(draftMetode = v) }; persistDraftSnapshot() }
    fun updateDraftMedia(v: String) { savedStateHandle[KEY_DRAFT_MEDIA] = v; _uiState.update { it.copy(draftMedia = v) }; persistDraftSnapshot() }
    fun updateDraftHadir(v: String) { savedStateHandle[KEY_DRAFT_HADIR] = v; _uiState.update { it.copy(draftHadir = v) }; persistDraftSnapshot() }
    fun updateDraftAbsen(v: String) { savedStateHandle[KEY_DRAFT_ABSEN] = v; _uiState.update { it.copy(draftAbsen = v) }; persistDraftSnapshot() }
    fun updateDraftKompetensi(v: Boolean) { savedStateHandle[KEY_DRAFT_KOMPETENSI] = v; _uiState.update { it.copy(draftKompetensiTercapai = v) }; persistDraftSnapshot() }
    fun updateDraftCatatan(v: String) { savedStateHandle[KEY_DRAFT_CATATAN] = v; _uiState.update { it.copy(draftCatatan = v) }; persistDraftSnapshot() }
    fun updateDraftTindakLanjut(v: String) { savedStateHandle[KEY_DRAFT_TINDAK_LANJUT] = v; _uiState.update { it.copy(draftTindakLanjut = v) }; persistDraftSnapshot() }

    private fun clearDraft() {
        val scheduleId = _uiState.value.draftScheduleId
        listOf(KEY_DRAFT_SCHEDULE_ID, KEY_DRAFT_MATERI, KEY_DRAFT_METODE, KEY_DRAFT_MEDIA, KEY_DRAFT_HADIR, KEY_DRAFT_ABSEN, KEY_DRAFT_CATATAN, KEY_DRAFT_TINDAK_LANJUT)
            .forEach { savedStateHandle.remove<String>(it) }
        savedStateHandle.remove<Boolean>(KEY_DRAFT_KOMPETENSI)
        if (scheduleId.isNotBlank()) formDraftStore.clearDraft(formIdFor(scheduleId))
        _uiState.update { it.copy(draftScheduleId = "", draftMateriPokok = "", draftMetode = "", draftMedia = "", draftHadir = "", draftAbsen = "", draftKompetensiTercapai = true, draftCatatan = "", draftTindakLanjut = "", restorableDraftAvailable = false) }
    }

    private companion object {
        const val KEY_DRAFT_SCHEDULE_ID = "journal_draft_schedule_id"
        const val KEY_DRAFT_MATERI = "journal_draft_materi"
        const val KEY_DRAFT_METODE = "journal_draft_metode"
        const val KEY_DRAFT_MEDIA = "journal_draft_media"
        const val KEY_DRAFT_HADIR = "journal_draft_hadir"
        const val KEY_DRAFT_ABSEN = "journal_draft_absen"
        const val KEY_DRAFT_KOMPETENSI = "journal_draft_kompetensi"
        const val KEY_DRAFT_CATATAN = "journal_draft_catatan"
        const val KEY_DRAFT_TINDAK_LANJUT = "journal_draft_tindak_lanjut"

        private val INDONESIAN_DAYS = arrayOf("Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")
        private val ISO_DATE = SimpleDateFormat("yyyy-MM-dd", Locale("id", "ID"))

        fun todayDayName(): String = INDONESIAN_DAYS[Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1]
        fun todayIso(): String = ISO_DATE.format(Calendar.getInstance().time)
        fun daysAgoIso(days: Int): String {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -days)
            return ISO_DATE.format(cal.time)
        }
    }

    fun onEvent(event: JournalMobileUiEvent) {
        when (event) {
            is JournalMobileUiEvent.LoadSchedules -> loadSchedules()
            is JournalMobileUiEvent.SetSelectedTab -> setSelectedTab(event.tabIndex)
            is JournalMobileUiEvent.ClearMessages -> _uiState.update { it.copy(errorMessage = null, successMessage = null) }
        }
    }

    fun loadSchedules() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            journalRepository.getTeacherSchedule().collect { scheduleResult ->
                when (scheduleResult) {
                    is NetworkResult.Success -> {
                        latestSchedule = scheduleResult.data
                        loadJournals()
                    }
                    is NetworkResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = scheduleResult.message) }
                    }
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }

    private fun loadJournals() {
        viewModelScope.launch {
            journalRepository.getTeacherJournals().collect { journalResult ->
                when (journalResult) {
                    is NetworkResult.Success -> {
                        latestJournals = journalResult.data
                        rebuildState()
                    }
                    is NetworkResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = journalResult.message) }
                    }
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }

    private fun rebuildState() {
        val today = todayIso()
        val todayEntries = latestJournals.filter { it.teachingDate == today }
        val todaySlots = latestSchedule
            .filter { it.day.equals(todayDayName(), ignoreCase = true) }
            .sortedBy { it.sessionStart }
            .mapIndexed { index, slot ->
                val matched = todayEntries.firstOrNull { it.subjectId == slot.subjectId && it.classroomId == slot.classroomId }
                slot.toJournalScheduleItem(jamKe = index + 1, entry = matched, dateOverride = today)
            }

        val weekEntries = latestJournals.filter { it.teachingDate != null && it.teachingDate >= daysAgoIso(7) }
            .sortedByDescending { it.teachingDate }
            .map { it.toJournalScheduleItem() }
        val monthEntries = latestJournals.filter { it.teachingDate != null && it.teachingDate >= daysAgoIso(30) }
            .sortedByDescending { it.teachingDate }
            .map { it.toJournalScheduleItem() }

        _uiState.update {
            it.copy(
                isLoading = false,
                todaySchedule = todaySlots,
                weekJournals = weekEntries,
                monthJournals = monthEntries
            )
        }
    }

    private fun TeacherScheduleSlot.toJournalScheduleItem(jamKe: Int, entry: TeachingJournalEntry?, dateOverride: String): JournalScheduleItem {
        return JournalScheduleItem(
            id = "sched-$id",
            timeSlot = "$sessionStart - $sessionEnd WIB (Jam ke-$jamKe)",
            subject = subjectName,
            className = classroomName,
            isFilled = entry != null,
            subjectId = subjectId,
            classroomId = classroomId,
            jamKe = jamKe,
            date = dateOverride,
            topic = entry?.topic,
            method = entry?.learningMethod,
            media = entry?.learningActivity,
            attendancePresent = entry?.studentsPresent,
            attendanceAbsent = entry?.studentsAbsent,
            notes = entry?.obstacles,
            followUp = entry?.notes,
            isEditable = entry == null
        )
    }

    private fun TeachingJournalEntry.toJournalScheduleItem(): JournalScheduleItem {
        return JournalScheduleItem(
            id = "entry-$uuid",
            timeSlot = "Jam ke-$jamKe",
            subject = subjectName,
            className = classroomName,
            isFilled = true,
            subjectId = subjectId,
            classroomId = classroomId,
            jamKe = jamKe,
            date = teachingDate,
            topic = topic,
            method = learningMethod,
            media = learningActivity,
            attendancePresent = studentsPresent,
            attendanceAbsent = studentsAbsent,
            notes = obstacles,
            followUp = notes,
            // The backend exposes no update endpoint for a filed journal — editing a past entry is not possible.
            isEditable = false
        )
    }

    fun setSelectedTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    fun getScheduleById(id: String): JournalScheduleItem? {
        return _uiState.value.todaySchedule.firstOrNull { it.id == id }
            ?: _uiState.value.weekJournals.firstOrNull { it.id == id }
            ?: _uiState.value.monthJournals.firstOrNull { it.id == id }
    }

    fun submitJournal(
        scheduleId: String,
        materiPokok: String,
        metode: String,
        media: String,
        hadir: Int,
        absen: Int,
        isKompetensiTercapai: Boolean,
        catatan: String,
        tindakLanjut: String,
        onSuccess: () -> Unit
    ) {
        val slot = getScheduleById(scheduleId)
        if (slot == null) {
            _uiState.update { it.copy(errorMessage = "Jadwal tidak ditemukan. Muat ulang halaman.") }
            return
        }
        if (materiPokok.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Materi pokok KBM wajib diisi.") }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            journalRepository.storeJournal(
                StoreJournalRequest(
                    subjectId = slot.subjectId,
                    classroomId = slot.classroomId,
                    teachingDate = slot.date ?: todayIso(),
                    jamKe = slot.jamKe,
                    topic = materiPokok,
                    learningActivity = media,
                    learningMethod = metode,
                    // The backend's TeachingJournal model has no dedicated "follow-up plan" column —
                    // "Kendala Pembelajaran" maps to the real obstacles field, and "Rencana Tindak
                    // Lanjut" is folded into notes (labeled) rather than silently dropped.
                    obstacles = catatan.ifBlank { null },
                    notes = buildList {
                        if (!isKompetensiTercapai) add("Kompetensi/tujuan pembelajaran BELUM tercapai.")
                        if (tindakLanjut.isNotBlank()) add("Rencana Tindak Lanjut: $tindakLanjut")
                    }.joinToString("\n").ifBlank { null },
                    studentsPresent = hadir,
                    studentsAbsent = absen
                )
            ).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        clearDraft()
                        loadSchedules()
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                successMessage = "Jurnal Mengajar KBM berhasil disimpan dan divalidasi Kurikulum!"
                            )
                        }
                        onSuccess()
                    }
                    is NetworkResult.Error -> {
                        _uiState.update { it.copy(isSubmitting = false, errorMessage = result.message) }
                    }
                    is NetworkResult.Loading -> Unit
                }
            }
        }
    }
}
