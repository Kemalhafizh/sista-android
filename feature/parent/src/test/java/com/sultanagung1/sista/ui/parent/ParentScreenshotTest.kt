package com.sultanagung1.sista.ui.parent

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.LayoutDirection
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
@Config(sdk = [35], qualifiers = "in-w400dp-h1900dp-xhdpi")
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
        ChildActivityEvent("attendance_1", at(0, 0), "Presensi: Hadir", "attendance", "Tercatat di presensi kelas.", "present"),
        ChildActivityEvent("assessment_4", at(0, 10, 15), "Nilai UH 2 Hukum Newton", "academic", "Nilai 88", "assessment"),
        ChildActivityEvent("points_2", at(1, 13, 5), "Poin prestasi +10", "discipline", "Juara 2 lomba karya tulis", "reward"),
        ChildActivityEvent("loan_9", at(1, 9, 40), "Meminjam buku", "library", "Fisika Dasar Jilid 1", "loan"),
        ChildActivityEvent("tahfidz_3", at(2, 0), "Setoran tahfidz", "ibadah", "Al-Mulk ayat 1–15", "tahfidz"),
        ChildActivityEvent("attendance_0", at(2, 0), "Presensi: Sakit", "attendance", "Demam, surat dokter menyusul.", "sick"),
    )

    // The server answers in the parent's language (sistem-terpadu ParentExperienceService);
    // what a teacher typed stays as typed.
    private val feedEn = listOf(
        feed[0].copy(title = "Attendance: Present", description = "Recorded in class attendance."),
        feed[1].copy(title = "Grade for UH 2 Hukum Newton", description = "Score 88"),
        feed[2].copy(title = "Achievement points +10"),
        feed[3].copy(title = "Borrowed a book"),
        feed[4].copy(title = "Tahfidz recitation", description = "Al-Mulk, verses 1–15"),
        feed[5].copy(title = "Attendance: Sick"),
    )
    private val feedAr = listOf(
        feed[0].copy(title = "الحضور: حاضر", description = "سُجِّل في حضور الحصة."),
        feed[1].copy(title = "درجة UH 2 Hukum Newton", description = "الدرجة 88"),
        feed[2].copy(title = "نقاط التميز +10"),
        feed[3].copy(title = "استعار كتابًا"),
        feed[4].copy(title = "تسميع التحفيظ", description = "Al-Mulk، الآيات 1–15"),
        feed[5].copy(title = "الحضور: مريض"),
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

    private fun show(dark: Boolean = false, content: @Composable () -> Unit) {
        compose.setContent {
            // The app's manifest sets supportsRtl; this module's test manifest doesn't,
            // so take the direction from the locale like the app does.
            val rtl = LocalConfiguration.current.locales[0].language == "ar"
            CompositionLocalProvider(LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
                MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) { content() }
            }
        }
    }

    private fun home(name: String, state: ParentUiState, dark: Boolean = false, greeting: String = "Selamat sore") {
        show(dark) {
            run {
                ParentHomeContent(
                    greeting = greeting,
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

    private fun detail(name: String, tab: Int, details: List<String> = listOf("Kelas XI MIPA 2", "NIS 22114")) {
        show {
            run {
                ChildDetailContent(
                    name = nadia.name, details = details,
                    grades = grades, attendance = attendance, loading = false, errorMessage = null, notFound = false,
                    tab = tab, onTab = {}, onRetry = {}, onNavigateBack = {},
                )
            }
        }
        compose.onRoot().captureRoboImage("screenshots/child_detail_$name.png")
    }

    @Test fun detailGrades() = detail("grades", 0)

    @Test fun detailAttendance() = detail("attendance", 1)

    private fun feedScreen(name: String, events: List<ChildActivityEvent>) {
        show { ChildActivityFeedContent(nadia.name, events, now, loading = false, errorMessage = null, onRetry = {}, onNavigateBack = {}) }
        compose.onRoot().captureRoboImage("screenshots/$name.png")
    }

    @Test fun feedByDay() = feedScreen("child_feed", feed)

    @Test fun feedEmpty() = feedScreen("child_feed_empty", emptyList())

    @Test @Config(qualifiers = "en-w400dp-h1900dp-xhdpi")
    fun homeEnglish() = home(
        "en",
        loaded.copy(
            activityFeed = feedEn,
            weeklyDigest = loaded.weeklyDigest!!.copy(
                highlights = listOf("Present 2 of 3 recorded attendances.", "Absent: 1 sick.", "New grades: 1.", "Achievement points +10."),
            ),
        ),
        greeting = "Good afternoon",
    )

    @Test @Config(qualifiers = "ar-ldrtl-w400dp-h1900dp-xhdpi")
    fun homeArabic() = home(
        "ar",
        loaded.copy(
            activityFeed = feedAr,
            weeklyDigest = loaded.weeklyDigest!!.copy(
                highlights = listOf("حاضر 2 من أصل 3 سجلات حضور.", "الغياب: 1 مريض.", "درجات جديدة: 1.", "نقاط التميز +10."),
            ),
        ),
        greeting = "مساء الخير",
    )

    @Test @Config(qualifiers = "ar-ldrtl-w400dp-h1900dp-xhdpi")
    fun feedArabic() = feedScreen("child_feed_ar", feedAr)

    @Test @Config(qualifiers = "en-w400dp-h1900dp-xhdpi")
    fun detailAttendanceEnglish() = detail("attendance_en", 1, details = listOf("Class XI MIPA 2", "NIS 22114"))
}
