package com.sultanagung1.sista.ui.teacher

import com.sultanagung1.sista.data.model.RemedialItem
import java.text.NumberFormat
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/** The student's remedial list, kept out of Compose so it can be tested on the JVM. */
object StudentRemedialRules {

    val SCHOOL_ZONE: ZoneId = ZoneId.of("Asia/Jakarta")

    enum class Status { DONE, PENDING, OTHER }

    /** The server's status codes (RemedialWorkflowService); anything else is shown as sent. */
    fun statusOf(item: RemedialItem): Status = when (item.status?.trim()?.lowercase()) {
        "completed" -> Status.DONE
        "pending" -> Status.PENDING
        else -> Status.OTHER
    }

    /** Still to do first, by deadline (none last); done ones after. */
    fun ordered(items: List<RemedialItem>): List<RemedialItem> =
        items.sortedWith(
            compareBy<RemedialItem> { statusOf(it) == Status.DONE }
                .thenBy { deadlineDate(it.deadline) ?: LocalDate.MAX }
        )

    /** 62.5 → "62,5" / "62.5"; 60.0 → "60". Null stays null ("–" on screen). */
    fun score(value: Double?, locale: Locale): String? = value?.let {
        NumberFormat.getNumberInstance(locale).apply { maximumFractionDigits = 1 }.format(it)
    }

    /** The day of an ISO-8601 deadline in school time; null when absent or unreadable. */
    fun deadlineDate(raw: String?): LocalDate? {
        val text = raw?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        return runCatching { OffsetDateTime.parse(text).atZoneSameInstant(SCHOOL_ZONE).toLocalDate() }.getOrNull()
            ?: runCatching { LocalDate.parse(text.take(10)) }.getOrNull()
    }

    fun deadlineText(raw: String?, locale: Locale): String? =
        deadlineDate(raw)?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))

    /** Past the deadline and still not done. */
    fun isOverdue(item: RemedialItem, today: LocalDate): Boolean =
        statusOf(item) != Status.DONE && deadlineDate(item.deadline)?.isBefore(today) == true

    fun today(): LocalDate = LocalDate.now(SCHOOL_ZONE)
}
