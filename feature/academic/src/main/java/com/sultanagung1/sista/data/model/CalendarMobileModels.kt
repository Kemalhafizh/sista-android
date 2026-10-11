package com.sultanagung1.sista.data.model

import androidx.compose.ui.graphics.Color
import com.sultanagung1.sista.core.designsystem.*

enum class EventType(val displayName: String) {
    KBM("KBM & Akademik"),
    UJIAN("Ujian & Asesmen"),
    LIBUR("Hari Libur"),
    KEGIATAN("Kegiatan Sekolah"),
    KEISLAMAN("Amaliyah Keislaman"),
    SPMB("SPMB / PPDB"),
    EKSKUL("Ekstrakurikuler"),
    RAPAT("Rapat Dinas"),
    DEADLINE("Batas Waktu");

    fun getColor(): Color = when (this) {
        KBM -> Emerald600
        UJIAN -> AccentRose
        LIBUR -> Gold600
        KEGIATAN -> AccentBlue
        KEISLAMAN -> Emerald800
        SPMB -> AccentPurple
        EKSKUL -> AccentCyan
        RAPAT -> Slate600
        DEADLINE -> AccentAmber
    }
}

data class CalendarEvent(
    val id: String,
    val title: String,
    val date: String, // YYYY-MM-DD
    val type: EventType,
    /** Null when the event has none; nothing is made up in its place. */
    val description: String? = null,
    /** Null when the server doesn't know it (AcademicEvent has no location yet). */
    val location: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val attachments: List<String> = emptyList(),
)
