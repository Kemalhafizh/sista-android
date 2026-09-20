package com.sultanagung1.sista.core.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** Shared day-name/date helpers so "filter by today" logic isn't reimplemented per ViewModel. */
object DateUtils {
    private val INDONESIAN_DAYS = arrayOf("Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")
    private val ISO_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale("id", "ID"))

    /** e.g. "Senin" — matches the `day` column stored on the backend's Schedule rows. */
    fun todayDayNameIndonesian(): String = INDONESIAN_DAYS[Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1]

    fun todayIso(): String = ISO_DATE_FORMAT.format(Calendar.getInstance().time)

    fun daysAgoIso(days: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -days)
        return ISO_DATE_FORMAT.format(cal.time)
    }
}
