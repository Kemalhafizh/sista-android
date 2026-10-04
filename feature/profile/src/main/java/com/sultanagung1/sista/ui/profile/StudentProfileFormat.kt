package com.sultanagung1.sista.ui.profile

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ID_DECIMAL = DecimalFormat("#,##0.#", DecimalFormatSymbols(Locale.forLanguageTag("id-ID"))).apply {
    roundingMode = RoundingMode.HALF_UP
}

/** "80,5", "1.250", or "–" when there is no figure. */
fun idNumber(value: Double?): String = value?.let { ID_DECIMAL.format(it) } ?: MISSING

/** "2 dari 30 siswa"; the rank alone when the class size is unknown. */
fun rankLabel(rank: Int?, classSize: Int?): String = when {
    rank == null -> MISSING
    classSize == null -> "$rank"
    else -> "$rank dari $classSize siswa"
}

/** "1 dari 4 juz", or null when no target is set this year. */
fun juzLabel(done: Int?, target: Int?): String? =
    if (target == null) null else "${done ?: 0} dari $target juz"

/** The server counts consecutive days with a Mutaba'ah log (it used to be shown as weeks). */
fun streakLabel(days: Int?): String = days?.let { "$it hari" } ?: MISSING

fun percentLabel(value: Double?): String = value?.let { "${idNumber(it)}%" } ?: MISSING

/** "165 cm · 52 kg", either part alone, or "–". */
fun bodyLabel(heightCm: Double?, weightKg: Double?): String =
    listOfNotNull(heightCm?.let { "${idNumber(it)} cm" }, weightKg?.let { "${idNumber(it)} kg" })
        .joinToString(" · ")
        .ifEmpty { MISSING }

fun signedPoints(value: Int?, sign: String): String = value?.let { if (it == 0) "0" else "$sign$it" } ?: MISSING

private val ID_DATE = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("id-ID"))

/** "12 September 2026" from the server's "2026-09-12"; "–" when absent or unreadable. */
fun dateLabel(isoDate: String?): String =
    isoDate?.let { runCatching { LocalDate.parse(it.take(10)).format(ID_DATE) }.getOrNull() } ?: MISSING
