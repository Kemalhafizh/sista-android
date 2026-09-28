package com.sultanagung1.sista.ui.parent

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.sultanagung1.sista.core.websocket.WebSocketEvent
import com.sultanagung1.sista.data.model.ChildActivityEvent
import com.sultanagung1.sista.data.model.ChildAttendanceLog
import com.sultanagung1.sista.data.model.ChildGradeItem
import com.sultanagung1.sista.data.model.ChildSummaryResponse
import com.sultanagung1.sista.data.model.ChildSummaryStatistics
import com.sultanagung1.sista.data.model.ChildSummaryStudent
import com.sultanagung1.sista.data.model.ChildVsClassComparison
import com.sultanagung1.sista.data.model.ParentChildItem
import com.sultanagung1.sista.data.model.WeeklyDigest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.util.Calendar
import java.util.TimeZone

/** The parent's screens in their states. The data exists only in this test. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w400dp-h1900dp-xhdpi")
class ParentScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    /** Wednesday 30 Sep 2026, 16:20 WIB. */
    private val now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jakarta")).apply { clear(); set(2026, 8, 30, 16, 20) }.timeInMillis
    private fun at(daysAgo: Int, hour: Int, minute: Int = 0): Long =
        Calendar.getInstance(TimeZone.getTimeZone("Asia/Jakarta")).apply {
            timeInMillis = now
            add(Calendar.DAY_OF_YEAR, -daysAgo)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }.timeInMillis / 1000

    private val nadia = ParentChildItem(uuid = "u1", name = "Nadia Putri Rahma", nis = "22114", classroom = "XI MIPA 2")
    private val raka = ParentChildItem(uuid = "u2", name = "Raka Aditya", nis = "24031", classroom = "X-3")

    private val feed = listOf(
        ChildActivityEvent("attendance_1", at(0, 0), "Presensi: Hadir", "attendance", "Tercatat di presensi kelas."),
        ChildActivityEvent("assessment_4", at(0, 10, 15), "Nilai UH 2 Hukum Newton", "academic", "Nilai 88"),
        ChildActivityEvent("points_2", at(1, 13, 5), "Poin prestasi +10", "discipline", "Juara 2 lomba karya tulis"),
        ChildActivityEvent("loan_9", at(1, 9, 40), "Meminjam buku", "library", "Fisika Dasar Jilid 1"),
        ChildActivityEvent("tahfidz_3", at(2, 0), "Setoran tahfidz", "ibadah", "Al-Mulk ayat 1–15"),
        ChildActivityEvent("attendance_0", at(2, 0), "Presensi: Sakit", "attendance", "Demam, surat dokter menyusul."),
    )

    private val loaded = ParentUiState(
        parentName = "Hendra Wijaya",
        children = listOf(nadia),
        selectedChild = nadia,
        selectedChildSummary = ChildSummaryResponse(
            student = ChildSummaryStudent("u1", nadia.name, "XI MIPA 2", homeroomTeacher = "Siti Rahmawati, S.Pd."),
            statistics = ChildSummaryStatistics(averageGrade = 84.6, attendanceRate = 96.5, unpaidBillingsCount = 1, totalBkPoints = 0),
        ),
        activityFeed = feed,
        weeklyDigest = WeeklyDigest(
            weekStart = at(2, 0), attendancePercentage = 66.7f, averageGrade = 88f, ibadahScore = null,
            highlights = listOf("Hadir 2 dari 3 presensi tercatat.", "Tidak hadir: 1 sakit.", "1 nilai baru masuk.", "Poin prestasi +10."),
        ),
        classComparison = listOf(
            ChildVsClassComparison("Bahasa Indonesia", 86f, 82.4f),
            ChildVsClassComparison("Fisika", 88f, 79.1f),
            ChildVsClassComparison("Matematika", 74.5f, 77.8f),
        ),
    )

    private fun home(name: String, state: ParentUiState, dark: Boolean = false) {
        compose.setContent {
            MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
                ParentHomeContent(
                    greeting = "Selamat sore",
                    state = state,
                    nowMillis = now,
                    shortcuts = parentShortcuts(state.selectedChild?.uuid),
                    refreshing = false,
                    onRefresh = {}, onRetry = {}, onSelectChild = {}, onOpenChild = {}, onOpenBilling = {}, onOpenFeed = {}, onOpenRoute = {},
                )
            }
        }
        compose.onRoot().captureRoboImage("screenshots/parent_home_$name.png")
    }

    @Test fun homeLoaded() = home("loaded", loaded)

    @Test fun homeDark() = home("dark", loaded, dark = true)

    @Test fun homeTwoChildrenWithGate() = home(
        "two_children",
        loaded.copy(
            children = listOf(nadia, raka),
            liveGateStatus = WebSocketEvent.LiveAttendanceRecorded("u1", "Nadia Putri Rahma", "06:52", "Tepat waktu", "Gerbang utama"),
        ),
    )

    @Test fun homeNoRecordsYet() = home(
        "no_records",
        loaded.copy(
            selectedChildSummary = loaded.selectedChildSummary!!.copy(
                statistics = ChildSummaryStatistics(averageGrade = null, attendanceRate = null, unpaidBillingsCount = 0, totalBkPoints = 0),
            ),
            activityFeed = emptyList(),
            weeklyDigest = WeeklyDigest(weekStart = at(2, 0)),
            classComparison = emptyList(),
        ),
    )

    @Test fun homeLoading() = home("loading", ParentUiState(isLoading = true, parentName = "Hendra Wijaya"))

    @Test fun homeNoChildren() = home("no_children", ParentUiState(parentName = "Hendra Wijaya"))

    @Test fun homeError() = home("error", ParentUiState(parentName = "Hendra Wijaya", errorMessage = "Tidak dapat terhubung ke server."))

    private val grades = listOf(
        ChildGradeItem(1, "Fisika", "uh", 88.0, "Hukum Newton", "2026-09-28"),
        ChildGradeItem(2, "Fisika", "tugas", 92.0, null, "2026-09-14"),
        ChildGradeItem(3, "Matematika", "uh", 74.5, "Trigonometri", "2026-09-25"),
        ChildGradeItem(4, "Bahasa Indonesia", "pts", 86.0, null, "2026-09-21"),
    )
    private val attendance = listOf(
        ChildAttendanceLog(1, "2026-09-30", "H", "Hadir"),
        ChildAttendanceLog(2, "2026-09-29", "H", "Hadir"),
        ChildAttendanceLog(3, "2026-09-28", "S", "Sakit", notes = "Demam, surat dokter menyusul."),
        ChildAttendanceLog(4, "2026-09-25", "H", "Hadir"),
        ChildAttendanceLog(5, "2026-09-24", "I", "Izin", notes = "Acara keluarga"),
    )

    private fun detail(name: String, tab: Int) {
        compose.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                ChildDetailContent(
                    name = nadia.name, details = listOf("Kelas XI MIPA 2", "NIS 22114"),
                    grades = grades, attendance = attendance, loading = false, errorMessage = null, notFound = false,
                    tab = tab, onTab = {}, onRetry = {}, onNavigateBack = {},
                )
            }
        }
        compose.onRoot().captureRoboImage("screenshots/child_detail_$name.png")
    }

    @Test fun detailGrades() = detail("grades", 0)

    @Test fun detailAttendance() = detail("attendance", 1)

    @Test fun feedByDay() {
        compose.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                ChildActivityFeedContent(nadia.name, feed, now, loading = false, errorMessage = null, onRetry = {}, onNavigateBack = {})
            }
        }
        compose.onRoot().captureRoboImage("screenshots/child_feed.png")
    }

    @Test fun feedEmpty() {
        compose.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                ChildActivityFeedContent(nadia.name, emptyList(), now, loading = false, errorMessage = null, onRetry = {}, onNavigateBack = {})
            }
        }
        compose.onRoot().captureRoboImage("screenshots/child_feed_empty.png")
    }
}
