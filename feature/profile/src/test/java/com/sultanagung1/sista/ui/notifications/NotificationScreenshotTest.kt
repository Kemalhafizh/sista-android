package com.sultanagung1.sista.ui.notifications

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.sultanagung1.sista.data.model.NotificationChannelType
import com.sultanagung1.sista.data.model.NotificationItem
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate

/** The notification center and settings in their states. The data exists only in this test. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "in-w400dp-h1300dp-xhdpi")
class NotificationScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private val today = LocalDate.of(2026, 9, 30)

    private val items = listOf(
        NotificationItem("1", "Nadia tiba di sekolah", "Tercatat hadir pukul 06.52 di gerbang utama.", "attendance_alerts", null, "2026-09-30T06:52:00+07:00", isRead = false),
        NotificationItem("2", "Tagihan SPP Oktober", "Rp 650.000 jatuh tempo 10 Oktober 2026.", "financial_reminders", "billing", "2026-09-30T08:00:00+07:00", isRead = false),
        NotificationItem("3", "Kunjungan UKS", "Nadia beristirahat di UKS karena pusing, sudah kembali ke kelas.", "general_info", "uks_visit", "2026-09-29T10:15:00+07:00", isRead = true),
        NotificationItem("4", "Poin prestasi +10", "Juara 2 lomba karya tulis ilmiah tingkat kota.", "general_info", "discipline", "2026-09-28T13:05:00+07:00", isRead = true),
    )

    private fun capture(name: String, dark: Boolean = false, content: @Composable () -> Unit) {
        compose.setContent { MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) { content() } }
        compose.onRoot().captureRoboImage("screenshots/$name.png")
    }

    private fun center(name: String, state: NotificationUiState, dark: Boolean = false) = capture("notification_center_$name", dark) {
        NotificationCenterContent(
            state = state, today = today, refreshing = false, canOpen = { it != "uks_visit" },
            onRefresh = {}, onRetry = {}, onFilter = {}, onOpen = {}, onMarkRead = {}, onMarkAllRead = {}, onDelete = {},
            onDismissMessage = {}, onNavigateBack = null, onOpenSettings = {},
        )
    }

    @Test fun centerLoaded() = center("loaded", NotificationUiState(notifications = items))

    @Test fun centerDark() = center("dark", NotificationUiState(notifications = items), dark = true)

    @Test fun centerUnreadOnly() = center("unread", NotificationUiState(notifications = items, filter = NotificationFilter.Unread))

    @Test fun centerAllRead() = center(
        "all_read",
        NotificationUiState(notifications = items.map { it.copy(isRead = true) }, filter = NotificationFilter.Unread),
    )

    @Test fun centerEmpty() = center("empty", NotificationUiState(notifications = emptyList()))

    @Test fun centerError() = center("error", NotificationUiState(errorMessage = "Tidak dapat terhubung ke server."))

    private fun channels(off: Set<NotificationChannelType> = emptySet()) =
        NotificationChannelType.values().map { ChannelStatus(it, it !in off) }

    private fun settings(name: String, state: NotificationSystemState) = capture("notification_settings_$name") {
        NotificationSettingsContent(state, onAllow = {}, onOpenAppSettings = {}, onOpenChannel = {}, onNavigateBack = {})
    }

    @Test fun settingsOn() = settings(
        "on",
        NotificationSystemState(appEnabled = true, canAskPermission = false, channels = channels(setOf(NotificationChannelType.GENERAL))),
    )

    @Test fun settingsBlocked() = settings(
        "blocked",
        NotificationSystemState(appEnabled = false, canAskPermission = true, channels = channels()),
    )
}
