package com.sultanagung1.sista.ui.profile

import com.sultanagung1.sista.core.ui.text.UiText
import com.sultanagung1.sista.feature.profile.R
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * The locale numbers and dates are written in: the app's language, with Latin
 * digits for Arabic (as the web shows them).
 */
fun displayLocale(locale: Locale = Locale.getDefault()): Locale =
    if (locale.language == "ar") Locale.forLanguageTag("ar-u-nu-latn") else locale

/** "80,5" in Indonesian, "80.5" in English; "1.250" / "1,250"; "–" when there is no figure. */
fun idNumber(value: Double?, locale: Locale = displayLocale()): String = value?.let {
    DecimalFormat("#,##0.#", DecimalFormatSymbols.getInstance(locale)).apply { roundingMode = RoundingMode.HALF_UP }.format(it)
} ?: MISSING

/** "2 dari 30 siswa"; the rank alone when the class size is unknown. */
fun rankLabel(rank: Int?, classSize: Int?): UiText = when {
    rank == null -> UiText.Raw(MISSING)
    classSize == null -> UiText.Raw("$rank")
    else -> UiText.Res(R.string.p360_rank_full, rank, classSize)
}

/** "1 dari 4 juz", or null when no target is set this year. */
fun juzLabel(done: Int?, target: Int?): UiText? =
    if (target == null) null else UiText.Res(R.string.p360_juz, done ?: 0, target)

/** The server counts consecutive days with a Mutaba'ah log (it used to be shown as weeks). */
fun streakLabel(days: Int?): UiText = days?.let { UiText.Res(R.string.p360_days, it) } ?: UiText.Raw(MISSING)

fun percentLabel(value: Double?, locale: Locale = displayLocale()): String = value?.let { "${idNumber(it, locale)}%" } ?: MISSING

/** "165 cm · 52 kg", either part alone, or "–". */
fun bodyLabel(heightCm: Double?, weightKg: Double?, locale: Locale = displayLocale()): List<UiText> =
    listOfNotNull(
        heightCm?.let { UiText.Res(R.string.unit_cm, idNumber(it, locale)) },
        weightKg?.let { UiText.Res(R.string.unit_kg, idNumber(it, locale)) },
    ).ifEmpty { listOf(UiText.Raw(MISSING)) }

fun signedPoints(value: Int?, sign: String): String = value?.let { if (it == 0) "0" else "$sign$it" } ?: MISSING

/** "12 September 2026" / "September 12, 2026" from the server's "2026-09-12"; "–" when absent or unreadable. */
fun dateLabel(isoDate: String?, locale: Locale = displayLocale()): String =
    isoDate?.let {
        runCatching { LocalDate.parse(it.take(10)).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale)) }.getOrNull()
    } ?: MISSING
