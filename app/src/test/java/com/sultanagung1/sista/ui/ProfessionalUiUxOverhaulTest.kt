package com.sultanagung1.sista.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Source-level guards for the role dashboards and shared executive UI.
 *
 * FASE 76.2 repair: every path in this file pointed at the pre-FASE-73
 * single-module layout (`src/main/java/...` inside :app), which no longer
 * exists — so every test here failed on its first `file.exists()` check.
 * Several assertions also checked names that were deliberately replaced by
 * real features ("Kolektibilitas SPP", "Guru Mengajar", "ModernApprovalCard",
 * "Presensi Gerbang"), and the "WhatsApp Guru" assertion only passed because
 * the phrase appears in a comment explaining that the WhatsApp button was
 * REMOVED. Assertions below describe what the code actually does today.
 */
class ProfessionalUiUxOverhaulTest {

    /**
     * Unit tests run with the :app module directory as the working dir, so
     * sibling modules are one level up; fall back to the repo root in case
     * the suite is ever invoked from there.
     */
    private fun source(relativePath: String): File {
        val candidates = listOf(File("../$relativePath"), File(relativePath))
        return candidates.firstOrNull { it.exists() } ?: candidates.first()
    }

    private val metricCard = "core/designsystem/src/main/java/com/sultanagung1/sista/core/designsystem/SulaoneMetricCard.kt"
    private val executiveHeader = "core/designsystem/src/main/java/com/sultanagung1/sista/ui/common/SulaoneExecutiveHeader.kt"
    private val teacherDashboard = "feature/teacher/src/main/java/com/sultanagung1/sista/ui/teacher/TeacherDashboardScreen.kt"
    private val parentDashboard = "feature/parent/src/main/java/com/sultanagung1/sista/ui/parent/ParentDashboardScreen.kt"
    private val adminDashboard = "feature/admin/src/main/java/com/sultanagung1/sista/ui/admin/AdminDashboardScreen.kt"
    private val profileScreen = "feature/profile/src/main/java/com/sultanagung1/sista/ui/profile/ProfileScreen.kt"
    private val loginScreen = "feature/auth/src/main/java/com/sultanagung1/sista/ui/auth/LoginScreen.kt"

    private fun read(relativePath: String): String {
        val file = source(relativePath)
        assertTrue("$relativePath must exist (resolved to ${file.absolutePath})", file.exists())
        return file.readText()
    }

    @Test
    fun testSulaoneMetricCardComponentExistsAndFollowsStandards() {
        val content = read(metricCard)
        assertTrue("Must declare SulaoneMetricCard composable", content.contains("fun SulaoneMetricCard"))
        assertTrue("Must support badge text for trends/KPIs", content.contains("badgeText: String? = null"))
        assertTrue("Must support springPressable for haptic feedback", content.contains("springPressable"))
        assertTrue("Must use standard 0.5dp subtle border", content.contains("0.5.dp"))
    }

    @Test
    fun testSulaoneExecutiveHeaderAdoptsModernMinimalism() {
        val content = read(executiveHeader)
        assertTrue("Must declare SulaoneExecutiveHeader", content.contains("fun SulaoneExecutiveHeader"))
        assertTrue("Must declare HeaderMetadataChip", content.contains("data class HeaderMetadataChip"))
        assertTrue("Must have official school crest logo_kotak", content.contains("R.drawable.logo_kotak"))
        assertTrue("Must support 48dp WCAG touch targets", content.contains("sulaoneInteractiveTouchTarget(48.dp)"))
        assertTrue("Must have verified badge", content.contains("Icons.Default.Verified"))
        assertTrue("Must use theme-aware surface background", content.contains("MaterialTheme.colorScheme.surface"))
    }

    @Test
    fun testTeacherHomeSharesTheUniformHomeLayout() {
        val content = read(teacherDashboard)
        // Same header, cards and tiles as every other role's Beranda.
        assertTrue("Must use the shared GreetingHeader", content.contains("GreetingHeader("))
        assertTrue("Must show real stats as StatTiles", content.contains("StatTile("))
        assertTrue("Must show Kelas diampu", content.contains("Kelas diampu"))
        assertTrue("Must offer Presensi per slot", content.contains("\"Presensi\""))
        assertTrue("Must offer Jurnal per slot", content.contains("\"Jurnal\""))
        // Shortcuts are only those the server grants this account.
        assertTrue("Shortcuts must be capability-filtered", content.contains("capabilities.canOpen(it.route)"))
        // The old header navigated to "scanner"/"notifications"/"profile" — none are routes.
        for (bogus in listOf("\"scanner\"", "\"notifications\"", "\"profile\"")) {
            assertFalse("Must not navigate to non-route $bogus", content.contains("onNavigateRoute($bogus)"))
        }
        assertTrue("Bell must open the real notification center", content.contains("Screen.NotificationCenter.route"))
    }

    @Test
    fun testParentDashboardFollowsExecutiveStandards() {
        val content = read(parentDashboard)
        assertTrue("Must use SulaoneExecutiveHeader", content.contains("SulaoneExecutiveHeader("))
        assertTrue("Must use SulaoneMetricCard", content.contains("SulaoneMetricCard("))
        assertTrue("Must have ParentChildPersonaCard", content.contains("ParentChildPersonaCard("))
        // FASE 71.3: the real WebSocket-driven gate banner replaced "Presensi Gerbang".
        assertTrue("Must render the real live gate status banner", content.contains("LiveGateStatusBanner("))
        // No phone-number field exists for teachers, so there must be no WhatsApp deep link;
        // the in-app "Pesan Sekolah" chat is the real channel.
        assertFalse("Must not deep-link to WhatsApp", listOf("wa.me", "api.whatsapp.com", "whatsapp://").any { content.contains(it) })
        assertTrue("Must offer in-app Pesan Sekolah chat", content.contains("Pesan Sekolah"))
        assertTrue("Must support Rapor Digital button", content.contains("Rapor Digital"))
        assertTrue("Must have 48dp touch targets", content.contains("sulaoneInteractiveTouchTarget(48.dp)"))
    }

    @Test
    fun testAdminDashboardFollowsExecutiveStandards() {
        val content = read(adminDashboard)
        assertTrue("Must use SulaoneExecutiveHeader", content.contains("SulaoneExecutiveHeader("))
        assertTrue("Must have KOKPIT EKSEKUTIF PIMPINAN title", content.contains("KOKPIT EKSEKUTIF PIMPINAN"))
        assertTrue("Must use SulaoneMetricCard for KPIs", content.contains("SulaoneMetricCard("))
        assertTrue("Must have Kehadiran Siswa KPI", content.contains("Kehadiran Siswa"))
        assertTrue("Must have Total Tunggakan SPP KPI", content.contains("Total Tunggakan SPP"))
        assertTrue("Must have Guru Aktif KPI (real active_teachers)", content.contains("Guru Aktif"))
        assertTrue("Must list real pending approvals", content.contains("PendingApprovalCard("))
        assertTrue("Must have Setujui and Tolak buttons", content.contains("Setujui") && content.contains("Tolak"))
    }

    // ---- FASE 76.2: Bento + glass + honest states --------------------------

    @Test
    fun testStaffDashboardsUseBentoHeroAndScrollAwareGlassBar() {
        for (path in listOf(parentDashboard, adminDashboard)) {
            val content = read(path)
            assertTrue("$path must use an asymmetric BentoHeroSplit", content.contains("BentoHeroSplit("))
            assertTrue("$path must use exactly one emphasized hero tile", content.split("SulaoneBentoHeroTile(").size - 1 == 1)
            assertTrue("$path must show the sticky glass bar", content.contains("SulaoneGlassTopBar("))
            assertTrue("$path must drive the glass bar from header scroll position", content.contains("rememberIsItemScrolledOff("))
            // A LazyVerticalGrid nested in these LazyColumns crashes at measure time.
            assertFalse("$path must not nest ResponsiveBentoGrid inside its LazyColumn", content.contains("ResponsiveBentoGrid("))
        }
    }

    @Test
    fun testAdminDashboardDoesNotClaimLiveDataItDoesNotHave() {
        // KPIs are a one-shot REST fetch on screen open — no polling, no WebSocket.
        val content = read(adminDashboard)
        assertFalse("Admin KPI cards must not carry a false 'Live' badge", content.contains("badgeText = \"Live\"") || content.contains("else \"Live\""))
    }

    @Test
    fun testParentDashboardDoesNotReportUnknownBillingAsPaid() {
        // Missing statistics used to fall through `?: 0` to "LUNAS — Aman".
        val content = read(parentDashboard)
        assertTrue("SPP card must say there is no data instead of LUNAS", content.contains("unpaid == null -> \"-\""))
        assertTrue("Unknown billing state must be described as no data", content.contains("Belum ada data"))
    }

    @Test
    fun testTeacherProctorShortcutOpensRealExamsNotHardcodedId() {
        // "Pengawas CBT" used to open TeacherProctor.createRoute(101L) — an exam
        // the teacher does not operate. It must go through the real exam list.
        val dashboard = read(teacherDashboard)
        assertFalse("Proctor shortcut must not use a hardcoded exam id", dashboard.contains("TeacherProctor.createRoute(101L)"))
        assertTrue("Proctor shortcut must open the teacher's exam list", dashboard.contains("Screen.TeacherProctorExams.route"))

        val navGraph = read("app/src/main/java/com/sultanagung1/sista/ui/navigation/graphs/TeacherNavGraph.kt")
        assertFalse("Proctor route must not fall back to a placeholder exam id", navGraph.contains("?: 101L"))

        val api = read("core/network/src/main/java/com/sultanagung1/sista/data/api/Services.kt")
        assertTrue("Exam list must come from the backend", api.contains("@GET(\"teacher/cbt/exams\")"))
    }

    // ------------------------------------------------------------------------

    @Test
    fun testProfileScreenImplementsDigitalInstitutionalId() {
        val content = read(profileScreen)
        assertTrue("Must have DigitalInstitutionalIdCard", content.contains("DigitalInstitutionalIdCard("))
        assertTrue("Must include official school branding", content.contains("SMA ISLAM SULTAN AGUNG 1"))
        assertTrue("Must include verified smart card token", content.contains("SMART CARD TOKEN • VERIFIED"))
        assertTrue("Must use ModernProfileMenuItem with 48dp target", content.contains("ModernProfileMenuItem("))
        assertTrue("Must support Dark Mode toggle", content.contains("Mode Gelap (Dark Mode)"))
        // The old "Ganti Peran Dashboard (Multi-Role)" item wrote a fake
        // session (token_teacher, token_parent, ...) that the backend rejects.
        // The backend has no role-switch endpoint, so the switcher is gone.
        assertFalse("Must not offer a client-side role switcher", content.contains("Ganti Peran"))
        assertFalse("Must not reference RoleSwitcherBottomSheet", content.contains("RoleSwitcherBottomSheet"))
    }

    @Test
    fun testAuthSessionIsOnlyWrittenFromServerResponses() {
        val repoRoot = listOf(File(".."), File(".")).first { File(it, "settings.gradle").exists() }.canonicalFile
        val mainSources = repoRoot.walkTopDown()
            .onEnter { it.name != "build" && !it.name.startsWith(".") }
            .filter { it.isFile && it.extension == "kt" && it.invariantSeparatorsPath.contains("/src/main/") }
            .associateWith { it.readText() }

        // Only the login and biometric-verify flows in the auth repository may
        // persist a session; both take the token and user from the API response.
        val offenders = mainSources
            .filter { (file, text) -> text.contains(".saveAuthSession(") && file.name != "Repositories.kt" }
            .keys.map { it.path }
        assertTrue("saveAuthSession called outside the auth repository: $offenders", offenders.isEmpty())

        val fakeTokens = listOf("token_teacher", "token_parent", "token_admin", "token_student")
        val fakeTokenFiles = mainSources
            .filter { (_, text) -> fakeTokens.any { text.contains(it) } }
            .keys.map { it.path }
        assertTrue("Hardcoded demo tokens found in: $fakeTokenFiles", fakeTokenFiles.isEmpty())
    }

    @Test
    fun testLoginScreenAdoptsProfessionalInstitutionalDesign() {
        val content = read(loginScreen)
        assertTrue("Must show the official school logo", content.contains("logo_kotak"))
        assertTrue("Must keep biometric sign-in", content.contains("BiometricVault.authenticate"))
        assertTrue("Built from the design system", content.contains("SistaTextField(") && content.contains("SistaButton("))
        // One sign-in for every role: the server knows the role, so a role
        // picker on this screen would decide nothing.
        assertFalse("No role picker", content.contains("UserRoleTab"))
        // No status the app never checked.
        assertFalse("No fake server status", content.contains("Server Backend Aktif"))
        listOf("login_identifier_input", "login_password_input", "login_submit_button").forEach { tag ->
            assertTrue("Automation tag $tag must stay", content.contains("\"$tag\""))
        }
    }
}
