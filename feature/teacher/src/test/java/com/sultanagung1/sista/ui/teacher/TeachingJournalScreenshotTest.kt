package com.sultanagung1.sista.ui.teacher

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.sultanagung1.sista.data.model.JournalScheduleItem
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Jurnal mengajar: the list per period and the form. The data exists only in this test. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "in-w400dp-h1400dp-xhdpi")
class TeachingJournalScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private val filled = JournalScheduleItem(
        id = "sched-1", timeSlot = "07:00–08:30 · Jam ke-1", subject = "Fisika", className = "XI MIPA 1",
        isFilled = true, subjectId = 5, classroomId = 21, jamKe = 1, date = "2026-09-28",
        topic = "Gerak parabola: komponen kecepatan dan titik tertinggi", method = "Diskusi kelompok",
        media = "Apersepsi lempar bola, diskusi LKPD, presentasi tiap kelompok.",
        attendancePresent = 30, attendanceAbsent = 1, notes = "Dua kelompok belum selesai LKPD.",
        followUp = "Rencana Tindak Lanjut: LKPD dilanjutkan pertemuan berikutnya.", isEditable = false, status = "draft",
    )
    private val empty1 = JournalScheduleItem(
        id = "sched-2", timeSlot = "08:30–10:00 · Jam ke-2", subject = "Fisika", className = "XI MIPA 2",
        isFilled = false, subjectId = 5, classroomId = 22, jamKe = 2, date = "2026-09-28",
    )
    private val empty2 = empty1.copy(id = "sched-3", timeSlot = "10:15–11:45 · Jam ke-3", className = "XI MIPA 3", classroomId = 23, jamKe = 3)

    private val week = listOf(
        filled.copy(id = "entry-a", date = "2026-09-25", className = "XI MIPA 2", topic = "Hukum Newton II", timeSlot = "Jam ke-3", status = "reviewed"),
        filled.copy(id = "entry-b", date = "2026-09-24", className = "X-4", topic = "Besaran dan satuan", timeSlot = "Jam ke-5", status = "submitted"),
        filled.copy(id = "entry-c", date = "2026-09-24", className = "XI MIPA 1", topic = "Gerak lurus berubah beraturan", timeSlot = "Jam ke-1", status = "draft"),
    )

    private fun list(name: String, state: JournalMobileUiState, dark: Boolean = false) {
        compose.setContent {
            MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
                TeachingJournalContent(state, refreshing = false, onSelectPeriod = {}, onRefresh = {}, onFill = {}, onNavigateBack = {})
            }
        }
        compose.onRoot().captureRoboImage("screenshots/journal_$name.png")
    }

    private val today = JournalMobileUiState(todaySchedule = listOf(filled, empty1, empty2), weekJournals = week)

    @Test fun listToday() = list("today", today)

    @Test fun listTodayDark() = list("today_dark", today, dark = true)

    @Test fun listWeek() = list("week", today.copy(selectedTab = 1))

    @Test fun listLoading() = list("loading", JournalMobileUiState(isLoading = true))

    @Test fun listError() = list("error", JournalMobileUiState(errorMessage = "Tidak dapat terhubung ke server."))

    @Test fun listEmptyMonth() = list("empty", JournalMobileUiState(todaySchedule = listOf(empty1), selectedTab = 2))

    private fun form(name: String, schedule: JournalScheduleItem?, draft: JournalDraft, errors: Boolean = false, error: String? = null) {
        compose.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                JournalFormContent(
                    schedule = schedule,
                    loading = false,
                    draft = draft,
                    submitting = false,
                    errorMessage = error,
                    actions = JournalDraftActions(),
                    onRetry = {},
                    onSubmit = {},
                    onNavigateBack = {},
                    initiallyShowErrors = errors,
                )
            }
        }
        compose.onRoot().captureRoboImage("screenshots/journal_form_$name.png")
    }

    @Test fun formBlank() = form("blank", empty1, JournalDraft())

    @Test fun formMissingFields() = form("missing", empty1, JournalDraft(topic = "Hukum Newton III", present = "31"), errors = true)

    @Test fun formComplete() = form(
        "complete",
        empty1,
        JournalDraft(
            topic = "Hukum Newton III dan pasangan gaya aksi-reaksi",
            method = "Praktikum",
            activity = "Apersepsi dorong tembok, praktikum neraca pegas berpasangan, simpulan bersama.",
            present = "31",
            absent = "1",
            objectivesMet = false,
            followUp = "Remedial untuk 4 siswa pada pertemuan berikutnya.",
        ),
        error = "Tahun akademik aktif tidak ditemukan",
    )

    @Test fun formFiledReadOnly() = form(
        "readonly",
        filled,
        JournalDraft(topic = filled.topic!!, method = "Diskusi kelompok", activity = filled.media!!, present = "30", absent = "1"),
    )

    @Test fun formSlotMissing() = form("not_found", null, JournalDraft())
}
