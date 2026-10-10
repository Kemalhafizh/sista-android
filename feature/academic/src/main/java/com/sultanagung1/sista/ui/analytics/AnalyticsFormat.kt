package com.sultanagung1.sista.ui.analytics

import java.time.LocalDate
import java.util.Locale

/** 96.54 → "96,5", 90.0 → "90", null → "–". */
internal fun decimal(value: Double?, digits: Int = 1): String {
    val v = value ?: return "–"
    val scale = if (digits == 0) 1.0 else 10.0
    val rounded = Math.round(v * scale) / scale
    return if (rounded % 1.0 == 0.0) rounded.toLong().toString() else String.format(Locale.US, "%.1f", rounded).replace('.', ',')
}

/** 12.5 → "+12,5", -3.0 → "−3". */
internal fun signed(value: Double): String = if (value >= 0) "+${decimal(value)}" else "−${decimal(-value)}"

/** 12480 → "12.480". */
internal fun thousands(value: Int): String = String.format(Locale.US, "%,d", value).replace(',', '.')

/** A day in the attendance month; [Unrecorded] is a day the school recorded nothing for. */
internal enum class DayMark(val label: String) {
    Present("Hadir"), Sick("Sakit"), Permit("Izin"), Absent("Alpa"), Unrecorded("Belum tercatat"),
}

/** Older servers wrote "LIBUR" for a day without records; it is only ever "no record". */
internal fun dayMarkOf(status: String): DayMark = when (status.uppercase()) {
    "HADIR", "H", "TERLAMBAT" -> DayMark.Present
    "SAKIT", "S" -> DayMark.Sick
    "IZIN", "I" -> DayMark.Permit
    "ALPA", "A" -> DayMark.Absent
    else -> DayMark.Unrecorded
}

internal data class MonthCell(val day: Int, val mark: DayMark?)

/**
 * The month of [today] as Monday-first weeks. Blank cells pad the first and
 * last week; a day after today, or one the server did not send, has no mark.
 */
internal fun monthGrid(today: LocalDate, marks: Map<Int, DayMark>): List<List<MonthCell?>> {
    val first = today.withDayOfMonth(1)
    val lead = first.dayOfWeek.value - 1
    val cells: List<MonthCell?> = List(lead) { null } +
        (1..today.lengthOfMonth()).map { day -> MonthCell(day, marks[day]?.takeIf { day <= today.dayOfMonth }) }
    return cells.chunked(7).map { week -> week + List(7 - week.size) { null } }
}

private val MONTH_NAMES = listOf(
    "Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember",
)

/** 2026-09-30 → "September 2026". */
internal fun monthLabel(date: LocalDate): String = "${MONTH_NAMES[date.monthValue - 1]} ${date.year}"
