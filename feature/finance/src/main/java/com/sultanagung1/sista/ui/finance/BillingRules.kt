package com.sultanagung1.sista.ui.finance

import com.sultanagung1.sista.data.model.BillingInvoice
import java.text.NumberFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/** The billing list's rules, kept out of Compose so they can be tested on the JVM. */
object BillingRules {

    val SCHOOL_ZONE: ZoneId = ZoneId.of("Asia/Jakarta")

    enum class Status { PAID, PARTIAL, UNPAID, OTHER }

    enum class Filter { ALL, OPEN, PAID }

    /** billings.status: unpaid / partial / paid. */
    fun statusOf(invoice: BillingInvoice): Status = when (invoice.status.trim().lowercase()) {
        "paid" -> Status.PAID
        "partial" -> Status.PARTIAL
        "unpaid" -> Status.UNPAID
        else -> Status.OTHER
    }

    /** Anything not paid is still open, as in WidgetSnapshots.studentBilling. */
    fun isOpen(invoice: BillingInvoice): Boolean = statusOf(invoice) != Status.PAID

    data class Summary(val openCount: Int, val remainingTotal: Double, val paidCount: Int)

    fun summary(invoices: List<BillingInvoice>): Summary = Summary(
        openCount = invoices.count(::isOpen),
        remainingTotal = invoices.filter(::isOpen).sumOf { it.remainingBalance },
        paidCount = invoices.count { !isOpen(it) },
    )

    /** Open bills first, soonest due first (no date last); paid ones after, newest due first. */
    fun ordered(invoices: List<BillingInvoice>): List<BillingInvoice> {
        val (open, paid) = invoices.partition(::isOpen)
        return open.sortedBy { dueDate(it.dueDate) ?: LocalDate.MAX } +
            paid.sortedByDescending { dueDate(it.dueDate) ?: LocalDate.MIN }
    }

    fun filter(invoices: List<BillingInvoice>, filter: Filter): List<BillingInvoice> = when (filter) {
        Filter.ALL -> invoices
        Filter.OPEN -> invoices.filter(::isOpen)
        Filter.PAID -> invoices.filterNot(::isOpen)
    }

    /** "Rp 2.550.000" (id) / "Rp 2,550,000" (en); whole rupiah, grouping in the app's language. */
    fun rupiah(amount: Double, locale: Locale): String =
        "Rp " + NumberFormat.getIntegerInstance(locale).format(Math.round(amount))

    fun dueDate(raw: String?): LocalDate? =
        raw?.trim()?.takeIf { it.length >= 10 }?.let { runCatching { LocalDate.parse(it.take(10)) }.getOrNull() }

    fun dueDateText(raw: String?, locale: Locale): String? =
        dueDate(raw)?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))

    /** Still open and its due date has passed. */
    fun isOverdue(invoice: BillingInvoice, today: LocalDate): Boolean =
        isOpen(invoice) && dueDate(invoice.dueDate)?.isBefore(today) == true

    fun today(): LocalDate = LocalDate.now(SCHOOL_ZONE)
}
