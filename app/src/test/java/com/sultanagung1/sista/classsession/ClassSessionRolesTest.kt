package com.sultanagung1.sista.classsession

import com.sultanagung1.sista.ui.navigation.RoleGroup
import com.sultanagung1.sista.ui.navigation.Screen
import com.sultanagung1.sista.ui.navigation.UserRoles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun principalReadsButDoesNotCorrect() {
        for (role in listOf("admin", "superadmin", "waka_kurikulum", "staf_tu")) {
            assertTrue(role, UserRoles.canViewClassSessions(role))
            assertTrue(role, UserRoles.canCorrectClassAttendance(role))
        }
        for (role in listOf("kepsek", "kepala_sekolah")) {
            assertTrue(role, UserRoles.canViewClassSessions(role))
            assertFalse(role, UserRoles.canCorrectClassAttendance(role))
        }
        for (role in listOf("guru", "student", "parent", "bk", null)) {
            assertFalse("$role", UserRoles.canViewClassSessions(role))
            assertFalse("$role", UserRoles.canCorrectClassAttendance(role))
        }
    }
}
