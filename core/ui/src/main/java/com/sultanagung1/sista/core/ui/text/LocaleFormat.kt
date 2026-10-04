package com.sultanagung1.sista.core.ui.text

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * The locale numbers and dates are written in: the app's language, with Latin
 * digits for Arabic (as the web shows them).
 */
fun displayLocale(locale: Locale = Locale.getDefault()): Locale =
    if (locale.language == "ar") Locale.forLanguageTag("ar-u-nu-latn") else locale

/** "96,5" in Indonesian, "96.5" in English and Arabic, "90" for 90.0; one decimal at most. */
fun localDecimal(value: Double, locale: Locale = displayLocale()): String =
    DecimalFormat("#,##0.#", DecimalFormatSymbols.getInstance(locale)).apply { roundingMode = RoundingMode.HALF_UP }.format(value)
