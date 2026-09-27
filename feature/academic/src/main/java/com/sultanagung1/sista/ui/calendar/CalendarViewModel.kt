package com.sultanagung1.sista.ui.calendar

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultanagung1.sista.core.mvi.UiEvent
import com.sultanagung1.sista.core.mvi.UiState
import com.sultanagung1.sista.core.network.NetworkResult
import com.sultanagung1.sista.data.model.CalendarEvent
import com.sultanagung1.sista.data.model.EventType
import com.sultanagung1.sista.data.repository.CalendarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

sealed interface CalendarUiEvent : UiEvent {
    data class SelectDate(val date: String) : CalendarUiEvent
    data class SelectCategory(val category: EventType?) : CalendarUiEvent
    data class ChangeMonth(val delta: Int) : CalendarUiEvent
    data class Refresh(val unit: Unit = Unit) : CalendarUiEvent
    data class SetSelectedTab(val tabIndex: Int) : CalendarUiEvent
}

data class CalendarUiState(
    val isLoading: Boolean = false,
    val year: Int = 2026,
    val month: Int = 9, // September
    val selectedDate: String = "2026-09-15",
    val selectedTab: Int = 0, // 0: Bulan, 1: Minggu, 2: Agenda
    val selectedCategory: EventType? = null,
    val events: List<CalendarEvent> = emptyList(),
    val errorMessage: String? = null
) : UiState {
    val monthName: String
        get() = when (month) {
            1 -> "Januari"
            2 -> "Februari"
            3 -> "Maret"
            4 -> "April"
            5 -> "Mei"
            6 -> "Juni"
            7 -> "Juli"
            8 -> "Agustus"
            9 -> "September"
            10 -> "Oktober"
            11 -> "November"
            12 -> "Desember"
            else -> "Bulan $month"
        }

    val filteredEvents: List<CalendarEvent>
        get() = if (selectedCategory == null) {
            events
        } else {
            events.filter { it.type == selectedCategory }
        }

    val selectedDayEvents: List<CalendarEvent>
        get() = filteredEvents.filter { it.date == selectedDate }
}


@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadEvents()
    }

    fun onEvent(event: CalendarUiEvent) {
        when (event) {
            is CalendarUiEvent.SelectDate -> selectDate(event.date)
            is CalendarUiEvent.SelectCategory -> setCategoryFilter(event.category)
            is CalendarUiEvent.ChangeMonth -> changeMonth(event.delta)
            is CalendarUiEvent.Refresh -> loadEvents()
            is CalendarUiEvent.SetSelectedTab -> setSelectedTab(event.tabIndex)
        }
    }

    fun loadEvents() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            calendarRepository.getEvents(
                month = _uiState.value.month,
                year = _uiState.value.year,
                category = _uiState.value.selectedCategory?.name?.lowercase()
            ).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        val apiEvents = result.data.map { item ->
                            CalendarEvent(
                                id = item.id.toString(),
                                title = item.title,
                                date = item.startTime.substringBefore("T").substringBefore(" "),
                                type = mapCategoryToType(item.category),
                                description = item.description ?: "Kegiatan resmi akademik SMA Islam Sultan Agung 1 Semarang.",
                                location = item.location ?: "Kampus SMA Islam Sultan Agung 1",
                                startTime = item.startTime,
                                endTime = item.endTime
                            )
                        }
                        // An empty result is a real, honest state (no events
                        // this month) — no longer silently replaced with
                        // fabricated events.
                        _uiState.update { it.copy(isLoading = false, events = apiEvents) }
                    }
                    is NetworkResult.Error -> _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message, events = emptyList())
                    }
                    is NetworkResult.Loading -> _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun selectDate(date: String) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun changeMonth(delta: Int) {
        _uiState.update { current ->
            var newMonth = current.month + delta
            var newYear = current.year
            if (newMonth < 1) {
                newMonth = 12
                newYear -= 1
            } else if (newMonth > 12) {
                newMonth = 1
                newYear += 1
            }
            val formattedDate = String.format(Locale.US, "%04d-%02d-01", newYear, newMonth)
            current.copy(year = newYear, month = newMonth, selectedDate = formattedDate)
        }
        loadEvents()
    }

    fun setSelectedTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    fun setCategoryFilter(category: EventType?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun getEventById(id: String): CalendarEvent? {
        return _uiState.value.events.firstOrNull { it.id == id }
    }

    fun addToGoogleCalendar(context: Context, event: CalendarEvent) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateObj = sdf.parse(event.date)
            val startTimeMillis = dateObj?.time ?: System.currentTimeMillis()
            val endTimeMillis = startTimeMillis + 2 * 60 * 60 * 1000 // 2 hours duration

            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, event.title)
                putExtra(CalendarContract.Events.DESCRIPTION, "${event.description}\n\nKategori: ${event.type.displayName}\nPenyelenggara: ${event.organizer}")
                putExtra(CalendarContract.Events.EVENT_LOCATION, event.location)
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTimeMillis)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTimeMillis)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun mapCategoryToType(category: String): EventType {
        return when (category.lowercase()) {
            "ujian", "pts", "pas", "cbt" -> EventType.UJIAN
            "libur", "holiday" -> EventType.LIBUR
            "keislaman", "keagamaan", "ibadah" -> EventType.KEISLAMAN
            "spmb", "ppdb" -> EventType.SPMB
            "ekskul", "osis" -> EventType.EKSKUL
            "rapat", "dinas" -> EventType.RAPAT
            "deadline", "tenggat" -> EventType.DEADLINE
            "kbm", "akademik" -> EventType.KBM
            else -> EventType.KEGIATAN
        }
    }

}
