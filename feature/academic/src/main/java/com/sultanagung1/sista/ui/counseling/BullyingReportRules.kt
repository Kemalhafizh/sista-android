package com.sultanagung1.sista.ui.counseling

import androidx.annotation.StringRes
import com.sultanagung1.sista.core.ui.text.UiText
import com.sultanagung1.sista.data.model.BullyingReportRequest
import com.sultanagung1.sista.feature.academic.R
import java.time.LocalDate
import java.time.ZoneId

/** What the reporter has filled in so far. Only [description] is required. */
data class BullyingReportForm(
    val description: String = "",
    /** When it happened; null when the reporter doesn't say. */
    val incidentDate: LocalDate? = null,
    val location: String = "",
    /** One of [BullyingReportRules.TYPES], or null. */
    val typeCode: String? = null,
    /** Anonymous unless the reporter turns it off, as the server assumes. */
    val anonymous: Boolean = true,
)

/** The form's rules, kept out of Compose so they can be tested on the JVM. They mirror the server's validation. */
object BullyingReportRules {

    val SCHOOL_ZONE: ZoneId = ZoneId.of("Asia/Jakarta")

    /** The server's limit on the description (BullyingReportApiController::store). */
    const val MAX_DESCRIPTION = 5000
    const val MAX_LOCATION = 255

    /** BullyingReport::TYPES on the server, in the order the form shows them. */
    val TYPES: List<Pair<String, Int>> = listOf(
        "verbal" to R.string.bully_type_verbal,
        "fisik" to R.string.bully_type_physical,
        "cyber" to R.string.bully_type_cyber,
        "sosial_eksklusi" to R.string.bully_type_exclusion,
        "pemerasan" to R.string.bully_type_extortion,
        "pelecehan" to R.string.bully_type_harassment,
        "intimidasi" to R.string.bully_type_intimidation,
        "lainnya" to R.string.bully_type_other,
    )

    fun today(): LocalDate = LocalDate.now(SCHOOL_ZONE)

    fun descriptionError(form: BullyingReportForm): UiText? = when {
        form.description.isBlank() -> UiText.Res(R.string.bully_err_description)
        form.description.trim().length > MAX_DESCRIPTION -> UiText.Res(R.string.bully_err_description_long, MAX_DESCRIPTION)
        else -> null
    }

    fun locationError(form: BullyingReportForm): UiText? =
        if (form.location.trim().length > MAX_LOCATION) UiText.Res(R.string.bully_err_location_long, MAX_LOCATION) else null

    /** The server refuses a date in the future (before_or_equal:today). */
    fun dateError(form: BullyingReportForm, today: LocalDate): UiText? =
        if (form.incidentDate?.isAfter(today) == true) UiText.Res(R.string.bully_err_future) else null

    fun canSubmit(form: BullyingReportForm, today: LocalDate): Boolean =
        descriptionError(form) == null && locationError(form) == null && dateError(form, today) == null

    /** What is sent: trimmed text, nothing for the fields left empty. */
    fun toRequest(form: BullyingReportForm): BullyingReportRequest = BullyingReportRequest(
        description = form.description.trim(),
        incidentDate = form.incidentDate?.toString(),
        location = form.location.trim().ifEmpty { null },
        type = form.typeCode?.takeIf { code -> TYPES.any { it.first == code } },
        isAnonymous = form.anonymous,
    )

    @StringRes
    fun typeLabel(code: String): Int? = TYPES.firstOrNull { it.first == code }?.second
}
