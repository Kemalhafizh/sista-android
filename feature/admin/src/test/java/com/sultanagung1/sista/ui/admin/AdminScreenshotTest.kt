package com.sultanagung1.sista.ui.admin

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.sultanagung1.sista.data.model.AdminDashboardData
import com.sultanagung1.sista.data.model.CbtServerUsage
import com.sultanagung1.sista.data.model.PendingApprovalItem
import com.sultanagung1.sista.data.model.SchoolKpiSummary
import com.sultanagung1.sista.data.model.SppCohortRatio
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.OffsetDateTime

/** The admin and leadership home in its states. The data exists only in this test. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w400dp-h1900dp-xhdpi")
class AdminScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private val now = OffsetDateTime.parse("2026-09-30T16:20:00+07:00").toInstant().toEpochMilli()

    private val approvals = listOf(
        PendingApprovalItem(
            id = 41, typeName = "Pengajuan Cuti", requesterName = "Siti Rahmawati, S.Pd.", requesterRole = "guru",
            notes = "Cuti melahirkan tiga bulan mulai 1 Oktober.", currentStep = 2, totalSteps = 2,
            dueAt = "2026-09-29T17:00:00+07:00", isOverdue = true,
        ),
        PendingApprovalItem(
            id = 42, typeName = "Pengadaan Barang", requesterName = "Ahmad Fauzi", requesterRole = "staf_tu",
            notes = "Proyektor ruang XI MIPA 2 rusak.", currentStep = 1, totalSteps = 3,
            dueAt = "2026-10-03T17:00:00+07:00",
        ),
    )

    private val loaded = AdminUiState(
        principalName = "Dr. H. Mulyadi, M.Pd.",
        dashboardData = AdminDashboardData(totalStudents = 1043, unpaidBillingsTotal = 48_750_000.0, attendanceRateToday = 94.2, pendingApprovalsCount = 2),
        schoolKpi = SchoolKpiSummary(
            monthlyRevenue = 212_400_000.0,
            activeTeachers = 68,
            sppPaymentRatioByCohort = listOf(
                SppCohortRatio("2026/2027", 3120, 2496, 80.0),
                SppCohortRatio("2025/2026", 12480, 12106, 97.0),
            ),
            cbtServerUsage = CbtServerUsage(activeSessions = 36, sessionsToday = 214),
        ),
        pendingApprovals = approvals,
    )

    private fun home(name: String, state: AdminUiState, dark: Boolean = false) {
        compose.setContent {
            MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
                AdminHomeContent(
                    greeting = "Selamat sore",
                    state = state,
                    nowMillis = now,
                    shortcuts = ADMIN_SHORTCUTS,
                    refreshing = false,
                    onRefresh = {}, onDecide = { _, _, _ -> }, onDismissOutcome = {}, onRetryApprovals = {},
                    onBroadcast = { _, _, _ -> }, onDismissBroadcast = {}, onOpenRoute = {},
                )
            }
        }
        compose.onRoot().captureRoboImage("screenshots/admin_home_$name.png")
    }

    @Test fun loaded() = home("loaded", loaded)

    @Test fun dark() = home("dark", loaded, dark = true)

    @Test fun refusedStep() = home(
        "refused",
        loaded.copy(approvalOutcome = ApprovalOutcome("Anda tidak berwenang memberikan persetujuan pada langkah ini.", succeeded = false)),
    )

    @Test fun nothingWaitingNoAttendanceYet() = home(
        "quiet",
        loaded.copy(
            dashboardData = loaded.dashboardData!!.copy(attendanceRateToday = null, pendingApprovalsCount = 0),
            pendingApprovals = emptyList(),
            approvalOutcome = ApprovalOutcome("Disetujui. Pengajuan diteruskan ke langkah berikutnya.", succeeded = true),
        ),
    )

    @Test fun loading() = home("loading", AdminUiState(principalName = "Dr. H. Mulyadi, M.Pd.", isLoading = true, isLoadingApprovals = true))

    @Test fun failed() = home(
        "error",
        AdminUiState(
            principalName = "Dr. H. Mulyadi, M.Pd.",
            errorMessage = "Tidak dapat terhubung ke server.",
            approvalsError = "Tidak dapat terhubung ke server.",
            kpiError = "Tidak dapat terhubung ke server.",
        ),
    )
}
