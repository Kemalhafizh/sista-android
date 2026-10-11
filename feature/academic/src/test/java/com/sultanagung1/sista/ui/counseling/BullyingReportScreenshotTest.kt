package com.sultanagung1.sista.ui.counseling

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.sultanagung1.sista.data.model.BullyingReportItem
import com.sultanagung1.sista.data.repository.BullyingReportRepository
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate

/** "Lapor Perundungan" in its states. The data exists only in this test. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "in-w400dp-h1300dp-xhdpi")
class BullyingReportScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private val today = LocalDate.of(2026, 10, 11)

    private val filled = BullyingReportForm(
        description = "Sejak dua minggu ini teman sekelas saya diejek terus di grup kelas dan uang jajannya diminta setiap istirahat.",
        incidentDate = today.minusDays(1),
        location = "Kantin dan grup chat kelas",
        typeCode = "pemerasan",
    )

    private val sent = BullyingReportRepository.Submitted(
        BullyingReportItem(id = 12, ticketNumber = "BLY-2026-0012", status = "Dilaporkan", statusCode = "reported", isAnonymous = true),
        "Laporan perundungan terkirim. Guru BK akan menindaklanjutinya.",
    )

    private fun capture(name: String, state: BullyingReportUiState, dark: Boolean = false) {
        compose.setContent {
            AppLocaleContent(dark) {
                BullyingReportContent(state, today, onChange = {}, onSubmit = {}, onNewReport = {}, onNavigateBack = {})
            }
        }
        compose.onRoot().captureRoboImage("screenshots/bullying_$name.png")
    }

    @Test fun empty() = capture("empty", BullyingReportUiState())

    @Test fun emptyTriedToSend() = capture("empty_errors", BullyingReportUiState(showErrors = true))

    @Test fun filledIn() = capture("filled", BullyingReportUiState(form = filled))

    @Test fun filledDark() = capture("filled_dark", BullyingReportUiState(form = filled), dark = true)

    @Test fun sending() = capture("sending", BullyingReportUiState(form = filled, isSending = true))

    @Test fun sent() = capture("sent", BullyingReportUiState(form = filled, submitted = sent))

    @Test fun sentWithName() = capture(
        "sent_named",
        BullyingReportUiState(submitted = sent.copy(report = sent.report.copy(isAnonymous = false))),
    )

    @Test fun refused() = capture(
        "error",
        BullyingReportUiState(form = filled, errorMessage = "Tidak bisa terhubung ke server. Periksa koneksi internet Anda."),
    )

    @Test @Config(qualifiers = "en-w400dp-h1300dp-xhdpi")
    fun filledEnglish() = capture("filled_en", BullyingReportUiState(form = filled))

    @Test @Config(qualifiers = "ar-ldrtl-w400dp-h1300dp-xhdpi")
    fun filledArabic() = capture("filled_ar", BullyingReportUiState(form = filled))
}
