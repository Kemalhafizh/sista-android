package com.sultanagung1.sista.ui.finance

import com.sultanagung1.sista.data.model.BillingInvoice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class BillingRulesTest {

    private val today = LocalDate.of(2026, 10, 11)
    private val id = Locale.forLanguageTag("id-ID")

    private fun bill(id: Long, status: String, amount: Double = 850_000.0, remaining: Double = amount, due: String? = null) =
        BillingInvoice(id = id, category = "SPP", month = "Oktober 2026", amount = amount, dueDate = due, status = status, remainingBalance = remaining)

    @Test
    fun statusFollowsTheServersEnum() {
        assertEquals(BillingRules.Status.PAID, BillingRules.statusOf(bill(1, "paid")))
        assertEquals(BillingRules.Status.PARTIAL, BillingRules.statusOf(bill(1, " Partial ")))
        assertEquals(BillingRules.Status.UNPAID, BillingRules.statusOf(bill(1, "unpaid")))
        assertEquals(BillingRules.Status.OTHER, BillingRules.statusOf(bill(1, "refunded")))
    }

    @Test
    fun theSummaryCountsWhatIsStillOwedNotTheFullAmounts() {
        val summary = BillingRules.summary(
            listOf(
                bill(1, "paid", remaining = 0.0),
                bill(2, "partial", remaining = 350_000.0),
                bill(3, "unpaid"),
            )
        )
        assertEquals(2, summary.openCount)
        assertEquals(1_200_000.0, summary.remainingTotal, 0.0)
        assertEquals(1, summary.paidCount)
        assertEquals(BillingRules.Summary(0, 0.0, 0), BillingRules.summary(emptyList()))
    }

    @Test
    fun openBillsComeFirstBySoonestDueDate() {
        val ordered = BillingRules.ordered(
            listOf(
                bill(1, "paid", due = "2026-08-10"),
                bill(2, "unpaid"),
                bill(3, "unpaid", due = "2026-11-10"),
                bill(4, "partial", due = "2026-10-10"),
                bill(5, "paid", due = "2026-09-10"),
            )
        )
        assertEquals(listOf(4L, 3L, 2L, 5L, 1L), ordered.map { it.id })
    }

    @Test
    fun filtersSplitOpenAndPaid() {
        val all = listOf(bill(1, "paid"), bill(2, "partial"), bill(3, "unpaid"))
        assertEquals(listOf(2L, 3L), BillingRules.filter(all, BillingRules.Filter.OPEN).map { it.id })
        assertEquals(listOf(1L), BillingRules.filter(all, BillingRules.Filter.PAID).map { it.id })
        assertEquals(3, BillingRules.filter(all, BillingRules.Filter.ALL).size)
    }

    @Test
    fun rupiahFollowsTheAppsLanguage() {
        assertEquals("Rp 2.550.000", BillingRules.rupiah(2_550_000.0, id))
        assertEquals("Rp 2,550,000", BillingRules.rupiah(2_550_000.0, Locale.ENGLISH))
        assertEquals("Rp 0", BillingRules.rupiah(0.0, id))
    }

    @Test
    fun aMissingDueDateIsNeverOverdue() {
        assertTrue(BillingRules.isOverdue(bill(1, "unpaid", due = "2026-10-10"), today))
        assertFalse(BillingRules.isOverdue(bill(1, "unpaid", due = "2026-10-11"), today))
        assertFalse(BillingRules.isOverdue(bill(1, "paid", due = "2026-09-10"), today))
        assertFalse(BillingRules.isOverdue(bill(1, "unpaid", due = null), today))
        assertNull(BillingRules.dueDateText(null, id))
        assertEquals("10 Okt 2026", BillingRules.dueDateText("2026-10-10", id))
    }
}
