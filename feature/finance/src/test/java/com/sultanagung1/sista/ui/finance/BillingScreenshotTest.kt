package com.sultanagung1.sista.ui.finance

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.sultanagung1.sista.data.model.BillingInvoice
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate

/** "Tagihan" in its states. The data exists only in this test. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "in-w400dp-h1300dp-xhdpi")
class BillingScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private val today = LocalDate.of(2026, 10, 11)

    private val bills = BillingRules.ordered(
        listOf(
            BillingInvoice(1, "SPP", "Oktober 2026", 850_000.0, dueDate = "2026-10-15", status = "unpaid", remainingBalance = 850_000.0),
            BillingInvoice(2, "SPP", "September 2026", 850_000.0, dueDate = "2026-09-15", status = "partial", remainingBalance = 350_000.0),
            BillingInvoice(3, "Kegiatan Ekstrakurikuler", null, 250_000.0, dueDate = null, status = "unpaid", remainingBalance = 250_000.0),
            BillingInvoice(4, "SPP", "Agustus 2026", 850_000.0, dueDate = "2026-08-15", status = "paid", remainingBalance = 0.0),
        )
    )

    private fun capture(name: String, state: BillingUiState, dark: Boolean = false) {
        compose.setContent {
            AppLocaleContent(dark) { BillingContent(state, today, onFilter = {}, onRetry = {}, onNavigateBack = {}) }
        }
        compose.onRoot().captureRoboImage("screenshots/billing_$name.png")
    }

    @Test fun loaded() = capture("loaded", BillingUiState(isLoading = false, invoices = bills))

    @Test fun loadedDark() = capture("loaded_dark", BillingUiState(isLoading = false, invoices = bills), dark = true)

    @Test fun onlyPaidFilter() = capture("filter_paid", BillingUiState(isLoading = false, invoices = bills, filter = BillingRules.Filter.PAID))

    @Test fun nothingOpen() = capture(
        "filter_open_empty",
        BillingUiState(isLoading = false, invoices = bills.filter { it.status == "paid" }, filter = BillingRules.Filter.OPEN),
    )

    @Test fun empty() = capture("empty", BillingUiState(isLoading = false, invoices = emptyList()))

    @Test fun loading() = capture("loading", BillingUiState())

    @Test fun error() = capture(
        "error",
        BillingUiState(isLoading = false, errorMessage = "Tidak bisa terhubung ke server. Periksa koneksi internet Anda."),
    )

    @Test @Config(qualifiers = "en-w400dp-h1300dp-xhdpi")
    fun loadedEnglish() = capture("loaded_en", BillingUiState(isLoading = false, invoices = bills))

    @Test @Config(qualifiers = "ar-ldrtl-w400dp-h1300dp-xhdpi")
    fun loadedArabic() = capture("loaded_ar", BillingUiState(isLoading = false, invoices = bills))
}
