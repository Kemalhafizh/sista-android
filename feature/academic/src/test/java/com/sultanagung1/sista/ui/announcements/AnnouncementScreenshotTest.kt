package com.sultanagung1.sista.ui.announcements

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.sultanagung1.sista.data.model.AnnouncementItem
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate

/** The announcement list and detail in their states. The data exists only in this test. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "in-w400dp-h1300dp-xhdpi")
class AnnouncementScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private val today = LocalDate.of(2026, 9, 30)

    private val ujian = AnnouncementItem(
        id = "12", title = "Jadwal Penilaian Tengah Semester Ganjil",
        summary = "PTS dilaksanakan 6–10 Oktober 2026 secara CBT. Siswa wajib membawa kartu ujian.",
        content = "<p>PTS dilaksanakan <b>6–10 Oktober 2026</b> secara CBT di laboratorium komputer.</p><p>Siswa wajib:</p><ul><li>membawa kartu ujian,</li><li>hadir 15 menit sebelum sesi.</li></ul>",
        category = "akademik", author = "Waka Kurikulum", priority = "urgent", publishedAt = "2026-09-30T07:15:00+07:00",
        expiresAt = "2026-10-10T23:59:00+07:00", isPinned = true, audience = "Siswa", requireAcknowledgement = true, isRead = false,
        attachmentUrl = "https://sekolah.sch.id/storage/announcements/attachments/Jadwal%20PTS%20Ganjil.pdf",
    )
    private val spp = AnnouncementItem(
        id = "11", title = "Pembayaran SPP lewat virtual account", summary = "Mulai Oktober, SPP dapat dibayar melalui VA BSI, BRI, dan Mandiri.",
        category = "keuangan", author = "Bagian Keuangan", publishedAt = "2026-09-29T09:00:00+07:00", audience = "Wali murid", isRead = true,
    )
    private val kajian = AnnouncementItem(
        id = "10", title = "Kajian Jumat bersama orang tua", summary = "Kajian di masjid sekolah, Jumat 2 Oktober pukul 13.00.",
        category = "keislaman", author = "Pembina Rohis", publishedAt = "2026-09-21T10:00:00+07:00", audience = "Wali murid · XI MIPA 2", isRead = false,
    )
    private val list = listOf(ujian, spp, kajian)

    private fun capture(name: String, dark: Boolean = false, content: @Composable () -> Unit) {
        compose.setContent { MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) { content() } }
        compose.onRoot().captureRoboImage("screenshots/$name.png")
    }

    private fun feed(name: String, state: AnnouncementFeedUiState, dark: Boolean = false) = capture("announcement_feed_$name", dark) {
        AnnouncementFeedContent(
            state = state, today = today, refreshing = false, onRefresh = {}, onRetry = {}, onCategory = {}, onSearch = {},
            onOpen = {}, onDismissBanner = {}, onNavigateBack = null,
        )
    }

    @Test fun feedLoaded() = feed("loaded", AnnouncementFeedUiState(announcements = list))

    @Test fun feedDark() = feed("dark", AnnouncementFeedUiState(announcements = list), dark = true)

    @Test fun feedLiveEmergency() = feed(
        "emergency",
        AnnouncementFeedUiState(announcements = list, liveBanner = "Latihan evakuasi gempa. Semua siswa berkumpul di lapangan.", liveIsEmergency = true),
    )

    @Test fun feedNoMatch() = feed("no_match", AnnouncementFeedUiState(announcements = list, query = "wisuda"))

    @Test fun feedEmptyCategory() = feed("empty", AnnouncementFeedUiState(announcements = emptyList(), category = AnnouncementCategory.Activity))

    @Test fun feedError() = feed("error", AnnouncementFeedUiState(errorMessage = "Tidak dapat terhubung ke server."))

    private fun detail(name: String, state: AnnouncementDetailUiState, dark: Boolean = false) = capture("announcement_detail_$name", dark) {
        AnnouncementDetailContent(state, today, onRetry = {}, onAcknowledge = {}, onOpenAttachment = {}, onShare = {}, onNavigateBack = {})
    }

    @Test fun detailNeedsConfirmation() = detail("confirm", AnnouncementDetailUiState(announcement = ujian.copy(isRead = true)))

    @Test fun detailConfirmed() = detail(
        "confirmed",
        AnnouncementDetailUiState(announcement = ujian.copy(isRead = true, acknowledgedAt = "2026-09-30T08:02:00+07:00")),
        dark = true,
    )

    @Test fun detailPlain() = detail("plain", AnnouncementDetailUiState(announcement = spp.copy(content = "<p>Mulai Oktober 2026, SPP dapat dibayar melalui virtual account BSI, BRI, dan Mandiri. Nomor VA tercantum di menu Tagihan.</p>")))

    @Test fun detailNotFound() = detail("error", AnnouncementDetailUiState(errorMessage = "Pengumuman tidak ditemukan"))
}
