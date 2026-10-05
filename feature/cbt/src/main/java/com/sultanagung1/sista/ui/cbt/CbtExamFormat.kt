package com.sultanagung1.sista.ui.cbt

import androidx.annotation.StringRes
import com.sultanagung1.sista.core.ui.text.UiText
import com.sultanagung1.sista.core.ui.text.displayLocale
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.data.model.CbtExamItem
import com.sultanagung1.sista.feature.cbt.R
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Where a student stands with one exam, from the server's own codes. */
enum class ExamAvailability(@StringRes val label: Int, val tone: StatusTone, val canEnter: Boolean) {
    ONGOING(R.string.cbt_av_ongoing, StatusTone.Success, true),
    /** Started and not finished; the token brings the student back to the same clock. */
    IN_PROGRESS(R.string.cbt_av_in_progress, StatusTone.Info, true),
    UPCOMING(R.string.cbt_av_upcoming, StatusTone.Neutral, false),
    ENDED(R.string.cbt_av_ended, StatusTone.Neutral, false),
    /** Submitted, graded or timed out. */
    DONE(R.string.cbt_av_done, StatusTone.Brand, false),
    /** Closed when the student left the app; only the proctor can reopen it. */
    FORCE_CLOSED(R.string.cbt_av_force_closed, StatusTone.Warning, false),
    /** Locked at the violation limit; final. */
    BLOCKED(R.string.cbt_av_blocked, StatusTone.Danger, false),
    /** An older server sent no schedule and the exam is not open now. */
    CLOSED(R.string.cbt_av_closed, StatusTone.Neutral, false),
}

/** Formatting for the student CBT screens, kept out of Compose so it can be tested on the JVM. */
object CbtExamFormat {

    private val SCHOOL_ZONE: ZoneId = ZoneId.of("Asia/Jakarta")

    fun availabilityOf(exam: CbtExamItem): ExamAvailability {
        when (exam.attemptStatus) {
            "submitted", "graded", "timeout" -> return ExamAvailability.DONE
            "force_closed" -> return ExamAvailability.FORCE_CLOSED
            "blocked_violation" -> return ExamAvailability.BLOCKED
        }
        val open = exam.availabilityCode?.let { it == "ongoing" } ?: exam.isOngoing
        return when {
            open && exam.attemptStatus == "in_progress" -> ExamAvailability.IN_PROGRESS
            open -> ExamAvailability.ONGOING
            exam.availabilityCode == "upcoming" -> ExamAvailability.UPCOMING
            exam.availabilityCode == "ended" -> ExamAvailability.ENDED
            else -> ExamAvailability.CLOSED
        }
    }

    /** `exams.type` (the server upper-cases it) in the app's language; an unknown type is shown as sent. */
    fun examType(type: String?): UiText? = when (type?.lowercase()) {
        null, "" -> null
        "uts" -> UiText.Res(R.string.cbt_type_uts)
        "uas" -> UiText.Res(R.string.cbt_type_uas)
        "ulangan_harian" -> UiText.Res(R.string.cbt_type_daily)
        "try_out" -> UiText.Res(R.string.cbt_type_tryout)
        else -> UiText.Raw(type)
    }

    /** The exam's window in the school's time zone: "Sen, 5 Okt · 07.30–09.00", or both ends in full across days. */
    fun schedule(startsAt: String?, endsAt: String?, locale: Locale = displayLocale()): String? {
        val start = parse(startsAt) ?: return null
        val day = DateTimeFormatter.ofPattern("EEE, d MMM", locale)
        val time = DateTimeFormatter.ofPattern("HH:mm", locale)
        val end = parse(endsAt) ?: return "${start.format(day)} · ${start.format(time)}"
        return if (start.toLocalDate() == end.toLocalDate()) {
            "${start.format(day)} · ${start.format(time)}–${end.format(time)}"
        } else {
            "${start.format(day)} ${start.format(time)} – ${end.format(day)} ${end.format(time)}"
        }
    }

    /** 3909 → "01:05:09" with Latin digits; "–" when the server hasn't said. */
    fun clock(seconds: Long?): String {
        if (seconds == null) return "–"
        val safe = seconds.coerceAtLeast(0)
        return String.format(Locale.ROOT, "%02d:%02d:%02d", safe / 3600, (safe % 3600) / 60, safe % 60)
    }

    /** The clock turns to a warning in the last five minutes and to danger in the last minute. */
    fun clockTone(seconds: Long?): StatusTone = when {
        seconds == null -> StatusTone.Neutral
        seconds <= 60 -> StatusTone.Danger
        seconds <= 300 -> StatusTone.Warning
        else -> StatusTone.Brand
    }

    /** Questions with an answer, out of the ones on screen (answers for other ids are ignored). */
    fun answeredCount(questionIds: List<Long>, answers: Map<String, String>): Int =
        questionIds.count { !answers[it.toString()].isNullOrBlank() }

    /** Tokens are six letters or digits, upper-cased as typed. */
    fun cleanToken(input: String): String = input.uppercase().filter { it.isLetterOrDigit() }.take(TOKEN_LENGTH)

    const val TOKEN_LENGTH = 6

    private fun parse(iso: String?) = iso?.takeIf { it.isNotBlank() }?.let {
        runCatching { OffsetDateTime.parse(it).atZoneSameInstant(SCHOOL_ZONE) }.getOrNull()
    }
}
