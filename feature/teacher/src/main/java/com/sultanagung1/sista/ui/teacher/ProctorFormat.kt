package com.sultanagung1.sista.ui.teacher

import com.sultanagung1.sista.core.ui.text.UiText
import com.sultanagung1.sista.core.ui.text.displayLocale
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.feature.teacher.R
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Formatting for the proctor screens, kept out of Compose so it can be tested on the JVM. */
object ProctorFormat {

    private val SCHOOL_ZONE: ZoneId = ZoneId.of("Asia/Jakarta")

    /** `exams.type` in the app's language; a type the app does not know is shown as sent. */
    fun examType(type: String): UiText = when (type) {
        "uts" -> UiText.Res(R.string.pe_type_uts)
        "uas" -> UiText.Res(R.string.pe_type_uas)
        "ulangan_harian" -> UiText.Res(R.string.pe_type_daily)
        "try_out" -> UiText.Res(R.string.pe_type_tryout)
        else -> UiText.Raw(type)
    }

    /** "2026-10-05T07:30:00+07:00" → "Sen, 5 Okt 2026 · 07.30" in the school's time zone; null when unreadable. */
    fun examTime(iso: String?, locale: Locale = displayLocale()): String? {
        if (iso.isNullOrBlank()) return null
        return runCatching {
            OffsetDateTime.parse(iso).atZoneSameInstant(SCHOOL_ZONE)
                .format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy · HH:mm", locale))
        }.getOrNull()
    }

    /** 125 → "2:05", always with Latin digits. */
    fun countdown(seconds: Int): String {
        val safe = seconds.coerceAtLeast(0)
        return String.format(Locale.ROOT, "%d:%02d", safe / 60, safe % 60)
    }

    /** How urgent the token looks: expired or under a minute is danger, under two a warning. */
    fun tokenTone(expired: Boolean, remainingSeconds: Int?): StatusTone = when {
        expired -> StatusTone.Danger
        remainingSeconds == null -> StatusTone.Brand
        remainingSeconds < 60 -> StatusTone.Danger
        remainingSeconds < 120 -> StatusTone.Warning
        else -> StatusTone.Success
    }
}
