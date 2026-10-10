package com.sultanagung1.sista.ui

import com.sultanagung1.sista.data.repository.UsageRanking
import com.sultanagung1.sista.ui.navigation.ContextualFab
import com.sultanagung1.sista.ui.navigation.ContextualFabAction
import com.sultanagung1.sista.ui.navigation.RoleGroup
import com.sultanagung1.sista.ui.navigation.Screen
import com.sultanagung1.sista.ui.navigation.UserRoles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * FASE 76.3 / 76.4 / 76.7: the pure rules behind the contextual FAB, role
 * routing, "Sering Dipakai" ordering and tutor reply formatting.
 */
class Fase76CompletionLogicTest {

    // ── 76.3 role routing ────────────────────────────────────────────────

    @Test
    fun backendRoleAliasesLandOnTheRightHome() {
        // Spellings the backend seeders actually use for the same people.
        assertEquals(Screen.ParentDashboard.route, UserRoles.homeRouteFor("orang_tua"))
        assertEquals(Screen.ParentDashboard.route, UserRoles.homeRouteFor("parent"))
        assertEquals(Screen.AdminDashboard.route, UserRoles.homeRouteFor("kepala_sekolah"))
        assertEquals(Screen.AdminDashboard.route, UserRoles.homeRouteFor("superadmin"))
        assertEquals(Screen.AdminDashboard.route, UserRoles.homeRouteFor("kepsek"))
        assertEquals(Screen.TeacherDashboard.route, UserRoles.homeRouteFor("guru"))
        assertEquals(Screen.TeacherDashboard.route, UserRoles.homeRouteFor("teacher"))
        // BK counsellors use the teacher endpoints (role:guru,bk).
        assertEquals(Screen.TeacherDashboard.route, UserRoles.homeRouteFor("bk"))
        assertEquals(Screen.Home.route, UserRoles.homeRouteFor("siswa"))
        assertEquals(Screen.Home.route, UserRoles.homeRouteFor("student"))
        assertEquals(Screen.Home.route, UserRoles.homeRouteFor(null))
    }

    @Test
    fun normalizeOnlyTouchesKnownAliases() {
        assertEquals("parent", UserRoles.normalize("orang_tua"))
        assertEquals("parent", UserRoles.normalize(" Orang_Tua "))
        assertEquals("kepsek", UserRoles.normalize("kepala_sekolah"))
        assertEquals("student", UserRoles.normalize("siswa"))
        // Unknown or already-canonical roles pass through unchanged.
        assertEquals("waka_kurikulum", UserRoles.normalize("waka_kurikulum"))
        assertEquals("guru", UserRoles.normalize("guru"))
        assertNull(UserRoles.normalize(null))
    }

    @Test
    fun roleGroupsMatchTheOldSubstringRulesForCanonicalRoles() {
        assertEquals(RoleGroup.TEACHER, UserRoles.groupOf("guru"))
        assertEquals(RoleGroup.PARENT, UserRoles.groupOf("ortu"))
        assertEquals(RoleGroup.ADMIN, UserRoles.groupOf("admin"))
        assertEquals(RoleGroup.ADMIN, UserRoles.groupOf("principal"))
        assertEquals(RoleGroup.STUDENT, UserRoles.groupOf("student"))
        // "bk" must not match by substring inside other words.
        assertEquals(RoleGroup.STUDENT, UserRoles.groupOf("bkx_unknown"))
    }

    // ── 76.3 contextual FAB ──────────────────────────────────────────────

    @Test
    fun eachRoleHomeGetsItsOneAction() {
        assertEquals(ContextualFabAction.QUICK_ATTENDANCE, ContextualFab.actionFor(Screen.Home.route, "student"))
        assertEquals(ContextualFabAction.TEACHING_JOURNAL, ContextualFab.actionFor(Screen.TeacherDashboard.route, "guru"))
        assertEquals(ContextualFabAction.MESSAGE_TEACHER, ContextualFab.actionFor(Screen.ParentDashboard.route, "orang_tua"))
        assertEquals(Screen.StudentSessionQrScan.route, ContextualFabAction.QUICK_ATTENDANCE.targetRoute)
        assertEquals(Screen.TeachingJournalMobile.route, ContextualFabAction.TEACHING_JOURNAL.targetRoute)
        assertEquals(Screen.ConversationList.route, ContextualFabAction.MESSAGE_TEACHER.targetRoute)
    }

    @Test
    fun noFabOnExamOrOtherScreens() {
        val noFabRoutes = listOf(
            Screen.CbtRoom.route, Screen.CbtTokenEntry.route, Screen.CbtList.route,
            Screen.Schedule.route, Screen.NotificationCenter.route, Screen.Profile.route,
            Screen.Grades.route, Screen.AdminDashboard.route, Screen.Login.route, null
        )
        for (role in listOf("student", "guru", "parent", "admin")) {
            for (route in noFabRoutes) {
                assertNull("$role on $route", ContextualFab.actionFor(route, role))
            }
        }
    }

    @Test
    fun fabNeverDisagreesWithTheRoleOnTheScreen() {
        // A teacher account never sees the student's check-in button, etc.
        assertNull(ContextualFab.actionFor(Screen.Home.route, "guru"))
        assertNull(ContextualFab.actionFor(Screen.TeacherDashboard.route, "student"))
        assertNull(ContextualFab.actionFor(Screen.ParentDashboard.route, "admin"))
    }

    // ── 76.4 "Sering Dipakai" ────────────────────────────────────────────

    private val defaults = listOf("presensi", "jadwal", "cbt", "spp")

    @Test
    fun orderStaysDefaultUntilThereIsEnoughUsage() {
        val fewTaps = mapOf("spp" to 4)
        assertEquals(defaults, UsageRanking.rank(defaults, fewTaps) { it })
        assertFalse(UsageRanking.isReordered(defaults, fewTaps) { it })
    }

    @Test
    fun mostUsedComesFirstAndTiesKeepDefaultOrder() {
        val counts = mapOf("spp" to 6, "cbt" to 2, "jadwal" to 2)
        assertEquals(listOf("spp", "jadwal", "cbt", "presensi"), UsageRanking.rank(defaults, counts) { it })
        assertTrue(UsageRanking.isReordered(defaults, counts) { it })
    }

    @Test
    fun alreadyDefaultOrderIsNotCalledPersonalized() {
        val counts = mapOf("presensi" to 9, "jadwal" to 5, "cbt" to 1)
        assertEquals(defaults, UsageRanking.rank(defaults, counts) { it })
        assertFalse(UsageRanking.isReordered(defaults, counts) { it })
    }

    @Test
    fun frequentNeedsRepeatedUseAndIsCapped() {
        val candidates = listOf("a", "b", "c", "d", "e", "f")
        val counts = mapOf("a" to 2, "b" to 3, "c" to 10, "d" to 3, "e" to 7, "f" to 5)
        assertEquals(listOf("c", "e", "f", "b"), UsageRanking.frequent(candidates, counts, limit = 4))
        assertTrue(UsageRanking.frequent(candidates, mapOf("a" to 2)).isEmpty())
    }
}
