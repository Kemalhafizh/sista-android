package com.sultanagung1.sista.ui.analytics

import com.sultanagung1.sista.data.model.AttendanceDay
import java.time.LocalDate
import java.util.Locale

/** Present over recorded across every day, so a day with more records weighs more. */
internal fun overallRate(days: List<AttendanceDay>): Double? {
    val recorded = days.sumOf { it.recorded }
    return if (recorded > 0) days.sumOf { it.present } * 100.0 / recorded else null
}

/** 96.54 → "96,5", 90.0 → "90", null → "–". */
internal fun decimal(value: Double?, digits: Int = 1): String {
    val v = value ?: return "–"
    val scale = if (digits == 0) 1.0 else 10.0
    val rounded = Math.round(v * scale) / scale
    return if (rounded % 1.0 == 0.0) rounded.toLong().toString() else String.format(Locale.US, "%.1f", rounded).replace('.', ',')
}

/** 12.5 → "+12,5", -3.0 → "−3". */
internal fun signed(value: Double): String = if (value >= 0) "+${decimal(value)}" else "−${decimal(-value)}"

/** Rp 950.000 · Rp 12,5 jt · Rp 1,2 M. */
internal fun rupiahShort(amount: Double): String = when {
    amount >= 1_000_000_000 -> "Rp ${decimal(amount / 1_000_000_000)} M"
    amount >= 1_000_000 -> "Rp ${decimal(amount / 1_000_000)} jt"
    else -> "Rp " + String.format(Locale.US, "%,.0f", amount).replace(',', '.')
}

/** 12480 → "12.480". */
internal fun thousands(value: Int): String = String.format(Locale.US, "%,d", value).replace(',', '.')

private val WEEKDAYS = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
private val MONTHS = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des")

/** "2026-09-28" → ("Sen", "28"); the raw text when it is not a date. */
internal fun dayParts(iso: String): Pair<String, String> {
    val date = runCatching { LocalDate.parse(iso.take(10)) }.getOrNull() ?: return "" to iso
    return WEEKDAYS[date.dayOfWeek.value - 1] to date.dayOfMonth.toString()
}

/** "2026-09-28" → "Sen 28 Sep". */
internal fun shortDay(iso: String): String {
    val date = runCatching { LocalDate.parse(iso.take(10)) }.getOrNull() ?: return iso
    return "${WEEKDAYS[date.dayOfWeek.value - 1]} ${date.dayOfMonth} ${MONTHS[date.monthValue - 1]}"
}
