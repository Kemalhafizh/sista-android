package com.sultanagung1.sista.classsession

import com.sultanagung1.sista.core.deeplink.DeepLinkRouter
import com.sultanagung1.sista.ui.navigation.RoleGroup
import com.sultanagung1.sista.ui.navigation.Screen
import com.sultanagung1.sista.ui.navigation.UserRoles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * FASE 77.6.3 / backend 117.6 rule 8: who reaches class-session management,
 * and who may correct attendance. Mirrors the route middleware in the contract.
 */
class ClassSessionRolesTest {

    @Test
    fun wakaKurikulumAndTuHaveAHomeOfTheirOwn() {
        for (role in listOf("waka_kurikulum", "staf_tu", "WAKA_KURIKULUM")) {
            assertEquals(role, RoleGroup.ACADEMIC_STAFF, UserRoles.groupOf(role))
            assertEquals(role, Screen.AdminSessionManagement.route, UserRoles.homeRouteFor(role))
        }
        // Other deputies are not on the class-session endpoints; unchanged (student Home).
        assertEquals(RoleGroup.STUDENT, UserRoles.groupOf("waka_kesiswaan"))
    }

    @Test
    fun existingGroupsAreUnchanged() {
        assertEquals(RoleGroup.TEACHER, UserRoles.groupOf("guru"))
        assertEquals(RoleGroup.TEACHER, UserRoles.groupOf("bk"))
        assertEquals(RoleGroup.ADMIN, UserRoles.groupOf("superadmin"))
        assertEquals(RoleGroup.ADMIN, UserRoles.groupOf("kepala_sekolah"))
        assertEquals(RoleGroup.PARENT, UserRoles.groupOf("orang_tua"))
        assertEquals(RoleGroup.STUDENT, UserRoles.groupOf("siswa"))
    }

    @Test
    fun rolesMatchTheBackendAdminRoute() {
        // routes/api.php: admin/class-sessions → role:admin,superadmin,waka_kurikulum,staf_tu
        for (role in listOf("admin", "superadmin", "waka_kurikulum", "staf_tu")) {
            assertTrue(role, UserRoles.canManageClassSessions(role))
        }
        // The principal is not on that route (gets the KPI tab instead).
        for (role in listOf("kepsek", "kepala_sekolah", "guru", "student", "parent", "bk", null)) {
            assertFalse("$role", UserRoles.canManageClassSessions(role))
        }
    }

    /** 77.7.1: `deep_link_route`-less pushes use sulaone://class-session/… links. */
    @Test
    fun classSessionDeepLinksResolveToTheRightScreen() {
        assertEquals(Screen.StudentSessionQrScan.route, DeepLinkRouter.classSessionRoute("scan"))
        assertEquals(Screen.TeacherTodaySessions.route, DeepLinkRouter.classSessionRoute("teach"))
        assertEquals(Screen.AdminSessionManagement.route, DeepLinkRouter.classSessionRoute("manage"))
        assertEquals("teacher_active_session/501", DeepLinkRouter.classSessionRoute("501"))
        assertNull(DeepLinkRouter.classSessionRoute("unknown"))
        assertNull(DeepLinkRouter.classSessionRoute(null))
    }

    /** Backend FASE 117 pushes (`data.type`, `data.session_id`). */
    @Test
    fun classSessionPushesOpenTheRightScreen() {
        assertEquals(Screen.StudentSessionQrScan.route, DeepLinkRouter.classSessionPushRoute("class_session_started", "45"))
        assertEquals("teacher_active_session/45", DeepLinkRouter.classSessionPushRoute("class_session_auto_closed", "45"))
        assertEquals(Screen.TeacherTodaySessions.route, DeepLinkRouter.classSessionPushRoute("class_session_auto_closed", null))
        assertNull(DeepLinkRouter.classSessionPushRoute("class_session_alpha", "45"))
        assertNull(DeepLinkRouter.classSessionPushRoute("announcement", null))
    }
}
