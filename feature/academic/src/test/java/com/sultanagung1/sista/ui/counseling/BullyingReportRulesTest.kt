package com.sultanagung1.sista.ui.counseling

import com.sultanagung1.sista.core.ui.text.UiText
import com.sultanagung1.sista.data.model.BullyingReportRequest
import com.sultanagung1.sista.feature.academic.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class BullyingReportRulesTest {

    private val today = LocalDate.of(2026, 10, 11)

    @Test
    fun onlyTheDescriptionIsRequired() {
        assertFalse(BullyingReportRules.canSubmit(BullyingReportForm(), today))
        assertFalse(BullyingReportRules.canSubmit(BullyingReportForm(description = "   \n "), today))
        assertEquals(UiText.Res(R.string.bully_err_description), BullyingReportRules.descriptionError(BullyingReportForm(description = " ")))
        assertTrue(BullyingReportRules.canSubmit(BullyingReportForm(description = "Diejek di kelas"), today))
    }

    @Test
    fun theServersLimitsAreCheckedBeforeSending() {
        val long = BullyingReportForm(description = "a".repeat(BullyingReportRules.MAX_DESCRIPTION + 1))
        assertEquals(UiText.Res(R.string.bully_err_description_long, BullyingReportRules.MAX_DESCRIPTION), BullyingReportRules.descriptionError(long))
        // Whitespace around the text is not counted, as it is not sent.
        assertNull(BullyingReportRules.descriptionError(BullyingReportForm(description = " " + "a".repeat(BullyingReportRules.MAX_DESCRIPTION) + " ")))
        assertFalse(BullyingReportRules.canSubmit(BullyingReportForm(description = "x", location = "b".repeat(256)), today))
    }

    @Test
    fun aDateInTheFutureIsRefusedLikeTheServerDoes() {
        val tomorrow = BullyingReportForm(description = "x", incidentDate = today.plusDays(1))
        assertEquals(UiText.Res(R.string.bully_err_future), BullyingReportRules.dateError(tomorrow, today))
        assertFalse(BullyingReportRules.canSubmit(tomorrow, today))
        assertNull(BullyingReportRules.dateError(tomorrow.copy(incidentDate = today), today))
        assertNull(BullyingReportRules.dateError(tomorrow.copy(incidentDate = null), today))
    }

    @Test
    fun theRequestCarriesOnlyWhatWasFilledIn() {
        assertEquals(
            BullyingReportRequest(description = "Diejek di kelas", incidentDate = null, location = null, type = null, isAnonymous = true),
            BullyingReportRules.toRequest(BullyingReportForm(description = "  Diejek di kelas  ", location = "   ")),
        )
        assertEquals(
            BullyingReportRequest(description = "Dipalak", incidentDate = "2026-10-10", location = "Kantin", type = "pemerasan", isAnonymous = false),
            BullyingReportRules.toRequest(
                BullyingReportForm(description = "Dipalak", incidentDate = today.minusDays(1), location = " Kantin ", typeCode = "pemerasan", anonymous = false),
            ),
        )
    }

    @Test
    fun anUnknownTypeIsNotSent() {
        assertNull(BullyingReportRules.toRequest(BullyingReportForm(description = "x", typeCode = "lain-lain")).type)
    }

    @Test
    fun typesMatchTheServersCodesAndAllHaveALabel() {
        assertEquals(
            listOf("verbal", "fisik", "cyber", "sosial_eksklusi", "pemerasan", "pelecehan", "intimidasi", "lainnya"),
            BullyingReportRules.TYPES.map { it.first },
        )
        assertEquals(R.string.bully_type_extortion, BullyingReportRules.typeLabel("pemerasan"))
        assertNull(BullyingReportRules.typeLabel("tidak-ada"))
    }

    @Test
    fun aReportIsAnonymousUnlessTheReporterSaysOtherwise() {
        assertTrue(BullyingReportForm().anonymous)
        assertTrue(BullyingReportRequest(description = "x").isAnonymous)
    }
}
