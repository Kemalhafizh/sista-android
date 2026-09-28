package com.sultanagung1.sista.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.sultanagung1.sista.core.ui.component.FeatureTile
import com.sultanagung1.sista.core.ui.component.NavEntry
import com.sultanagung1.sista.core.ui.component.SistaNavigationBar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.sultanagung1.sista.core.ui.component.Avatar
import com.sultanagung1.sista.core.ui.component.ButtonSize
import com.sultanagung1.sista.core.ui.component.ButtonVariant
import com.sultanagung1.sista.core.ui.component.CardVariant
import com.sultanagung1.sista.core.ui.component.EmptyState
import com.sultanagung1.sista.core.ui.component.ErrorState
import com.sultanagung1.sista.core.ui.component.FilterChipRow
import com.sultanagung1.sista.core.ui.component.IconBadge
import com.sultanagung1.sista.core.ui.component.InlineBanner
import com.sultanagung1.sista.core.ui.component.SectionHeader
import com.sultanagung1.sista.core.ui.component.SistaButton
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SistaListItem
import com.sultanagung1.sista.core.ui.component.SistaTextField
import com.sultanagung1.sista.core.ui.component.SistaTopBar
import com.sultanagung1.sista.core.ui.component.SkeletonList
import com.sultanagung1.sista.core.ui.component.StatTile
import com.sultanagung1.sista.core.ui.component.StatusPill
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders the design system to PNGs (core/ui/screenshots) so every change to
 * a component is visible in review. Record with
 * `./gradlew :core:ui:recordRoborazziDebug`. The sample data below exists
 * only in this test; it never ships in the app.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w400dp-h2000dp-xhdpi")
class ComponentScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private fun capture(name: String, dark: Boolean, body: @Composable ColumnScope.() -> Unit) {
        compose.setContent {
            SistaTheme(darkTheme = dark) {
                Column(
                    Modifier
                        .testTag("shot")
                        .width(400.dp)
                        .background(SistaTheme.colors.background)
                        .padding(vertical = Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.lg),
                    content = body,
                )
            }
        }
        compose.onNodeWithTag("shot").captureRoboImage("screenshots/${name}_${if (dark) "dark" else "light"}.png")
    }

    @Test fun buttons_light() = capture("buttons", false) { Buttons() }
    @Test fun buttons_dark() = capture("buttons", true) { Buttons() }
    @Test fun feedback_light() = capture("feedback", false) { Feedback() }
    @Test fun feedback_dark() = capture("feedback", true) { Feedback() }
    @Test fun display_light() = capture("display", false) { Display() }
    @Test fun display_dark() = capture("display", true) { Display() }
    @Test fun inputs_light() = capture("inputs", false) { Inputs() }
    @Test fun inputs_dark() = capture("inputs", true) { Inputs() }
    @Test fun sampleHome_light() = capture("sample_student_home", false) { SampleStudentHome() }
    @Test fun sampleHome_dark() = capture("sample_student_home", true) { SampleStudentHome() }
    // The shell is identical for every role; only the tiles differ.
    @Test fun sampleServicesStudent_light() = capture("sample_services_student", false) { SampleServices(studentServices) }
    @Test fun sampleServicesTeacher_light() = capture("sample_services_teacher", false) { SampleServices(teacherServices) }
    @Test fun sampleServicesTeacher_dark() = capture("sample_services_teacher", true) { SampleServices(teacherServices) }
}

@Composable
private fun ColumnScope.Buttons() {
    Column(Modifier.padding(horizontal = Spacing.screen), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        SistaButton("Mulai Kelas", {}, size = ButtonSize.Large, fullWidth = true, leadingIcon = Icons.Outlined.QrCodeScanner)
        SistaButton("Menyimpan…", {}, size = ButtonSize.Large, fullWidth = true, loading = true)
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            SistaButton("Sekunder", {}, variant = ButtonVariant.Secondary)
            SistaButton("Outline", {}, variant = ButtonVariant.Outlined)
            SistaButton("Teks", {}, variant = ButtonVariant.Text)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            SistaButton("Akhiri Kelas", {}, variant = ButtonVariant.Danger)
            SistaButton("Nonaktif", {}, enabled = false)
        }
    }
}

@Composable
private fun ColumnScope.Feedback() {
    InlineBanner(
        "Menampilkan jadwal tersimpan. Periksa koneksi internet.",
        StatusTone.Warning,
        title = "Sedang offline",
        actionLabel = "Muat ulang",
        onAction = {},
        modifier = Modifier.padding(horizontal = Spacing.screen),
    )
    InlineBanner("Presensi tercatat pukul 07.02.", StatusTone.Success, onDismiss = {}, modifier = Modifier.padding(horizontal = Spacing.screen))
    HorizontalDivider()
    EmptyState("Tidak ada pelajaran", body = "Tidak ada jadwal pada hari Sabtu.", icon = Icons.Outlined.EventBusy)
    HorizontalDivider()
    ErrorState("Jadwal belum bisa dimuat", onRetry = {}, body = "Server tidak menjawab. Coba lagi sebentar lagi.")
    HorizontalDivider()
    SkeletonList(rows = 3)
}

@Composable
private fun ColumnScope.Display() {
    Row(Modifier.padding(horizontal = Spacing.screen), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        StatusPill("Hadir", StatusTone.Success, icon = Icons.Outlined.CheckCircle)
        StatusPill("Terlambat", StatusTone.Warning)
        StatusPill("Alpha", StatusTone.Danger)
    }
    Row(Modifier.padding(horizontal = Spacing.screen), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        StatusPill("Izin", StatusTone.Info)
        StatusPill("Belum dinilai", StatusTone.Neutral)
        StatusPill("Unggulan", StatusTone.Brand)
    }
    Row(Modifier.padding(horizontal = Spacing.screen), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
        StatTile("Kehadiran", "96%", Modifier.weight(1f), supporting = "Semester ini", icon = Icons.Outlined.CheckCircle, tone = StatusTone.Success)
        StatTile("Tagihan", "Rp350rb", Modifier.weight(1f), supporting = "Jatuh tempo 10 Okt", icon = Icons.Outlined.Receipt, tone = StatusTone.Warning)
    }
    SistaCard(Modifier.padding(horizontal = Spacing.screen), variant = CardVariant.Outlined, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
        SistaListItem("Fisika", supporting = "Bu Rina Wulandari • Lab Fisika", overline = "07.00 – 08.30", leading = { IconBadge(Icons.Outlined.School) }, trailing = { StatusPill("Berlangsung", StatusTone.Brand) }, onClick = {})
        HorizontalDivider(color = SistaTheme.colors.outlineVariant)
        SistaListItem("Ahmad Fauzan Ramadhan", supporting = "NIS 2410023", leading = { Avatar("Ahmad Fauzan") }, trailing = { StatusPill("Hadir", StatusTone.Success) })
    }
}

@Composable
private fun ColumnScope.Inputs() {
    Column(Modifier.padding(horizontal = Spacing.screen), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        SistaTextField("siswa@sma-sa1.sch.id", {}, "Email", leadingIcon = Icons.Outlined.Email)
        SistaTextField("rahasia", {}, "Kata sandi", isPassword = true, helperText = "Minimal 8 karakter")
        SistaTextField("", {}, "Alasan koreksi", errorText = "Alasan wajib diisi (min. 10 karakter)")
    }
    FilterChipRow(listOf("Semua", "Hadir", "Izin", "Alpha"), "Hadir", {}, { it })
}

@Composable
private fun ColumnScope.SampleStudentHome() {
    SistaTopBar(title = "Assalamu'alaikum, Aisyah", subtitle = "XI MIPA 2 • Senin, 28 September")
    Column(Modifier.padding(horizontal = Spacing.screen), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        SistaCard(variant = CardVariant.Highlighted, onClick = {}) {
            Text("Sedang berlangsung", style = SistaTheme.typography.labelMedium)
            Text("Fisika • Lab Fisika", style = SistaTheme.typography.titleLarge)
            Text("Presensi dibuka sampai 07.15", style = SistaTheme.typography.bodyMedium)
            androidx.compose.foundation.layout.Spacer(Modifier.padding(top = Spacing.md))
            SistaButton("Scan QR Presensi", {}, fullWidth = true, leadingIcon = Icons.Outlined.QrCodeScanner)
        }
        // Equal-height tiles: IntrinsicSize.Min on the row, fillMaxHeight on each tile.
        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            StatTile("Kehadiran", "96%", Modifier.weight(1f).fillMaxHeight(), supporting = "Semester ini", icon = Icons.Outlined.CheckCircle, tone = StatusTone.Success)
            StatTile("Tagihan", "1", Modifier.weight(1f).fillMaxHeight(), supporting = "Rp350rb", icon = Icons.Outlined.Receipt, tone = StatusTone.Warning)
        }
    }
    SectionHeader("Jadwal hari ini", actionLabel = "Lihat semua", onAction = {}, modifier = Modifier.padding(horizontal = Spacing.screen))
    SistaCard(Modifier.fillMaxWidth().padding(horizontal = Spacing.screen), variant = CardVariant.Outlined, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
        SistaListItem("Fisika", supporting = "Bu Rina • Lab Fisika", overline = "07.00 – 08.30", leading = { IconBadge(Icons.Outlined.CalendarMonth) }, trailing = { StatusPill("Berlangsung", StatusTone.Brand) })
        HorizontalDivider(color = SistaTheme.colors.outlineVariant)
        SistaListItem("Matematika", supporting = "Pak Hendra • XI MIPA 2", overline = "08.30 – 10.00", leading = { IconBadge(Icons.Outlined.CalendarMonth, tone = StatusTone.Neutral) })
        HorizontalDivider(color = SistaTheme.colors.outlineVariant)
        SistaListItem("Pendidikan Agama Islam", supporting = "Ust. Rizqi • Masjid Lt. 2", overline = "10.15 – 11.45", leading = { IconBadge(Icons.Outlined.CalendarMonth, tone = StatusTone.Neutral) })
    }
}

private data class SampleTile(val title: String, val icon: ImageVector, val hints: List<ImageVector> = emptyList())

private val studentServices = listOf(
    "Akademik" to listOf(
        SampleTile("Jadwal Pelajaran", Icons.Outlined.CalendarMonth),
        SampleTile("Ujian CBT", Icons.Outlined.School),
        SampleTile("Tagihan", Icons.Outlined.Receipt),
    ),
    "Presensi" to listOf(
        SampleTile("Presensi GPS", Icons.Outlined.LocationOn, listOf(Icons.Outlined.LocationOn)),
        SampleTile("Scan QR Kelas", Icons.Outlined.QrCodeScanner, listOf(Icons.Outlined.CameraAlt)),
    ),
)

private val teacherServices = listOf(
    "Mengajar" to listOf(
        SampleTile("Sesi Kelas Hari Ini", Icons.Outlined.QrCodeScanner, listOf(Icons.Outlined.CameraAlt)),
        SampleTile("Jurnal Mengajar", Icons.Outlined.Email),
        SampleTile("Penilaian Harian", Icons.Outlined.CheckCircle),
    ),
    "Layanan" to listOf(
        SampleTile("Pengumuman", Icons.Outlined.Email),
    ),
)

@Composable
private fun ColumnScope.SampleServices(sections: List<Pair<String, List<SampleTile>>>) {
    Column(Modifier.padding(horizontal = Spacing.screen), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        sections.forEach { (group, tiles) ->
            SectionHeader(group)
            tiles.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    row.forEach { tile ->
                        FeatureTile(tile.title, tile.icon, onClick = {}, hints = tile.hints, modifier = Modifier.weight(1f))
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
    SistaNavigationBar(
        entries = listOf(
            NavEntry("home", "Beranda", Icons.Outlined.Home, Icons.Filled.Home),
            NavEntry("layanan", "Layanan", Icons.Outlined.Apps, Icons.Filled.Apps),
            NavEntry("notif", "Notifikasi", Icons.Outlined.Notifications, badge = 3),
            NavEntry("profil", "Profil", Icons.Outlined.Person),
        ),
        selectedKey = "layanan",
        onSelect = {},
    )
}
