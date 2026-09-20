package com.sultanagung1.sista

import org.junit.Assert.*
import org.junit.Test

class RoleAuthorizationTest {

    private fun isRoleAuthorized(currentRole: String?, allowedRoles: List<String>): Boolean {
        val normalizedCurrentRole = currentRole?.trim()?.lowercase() ?: "student"
        return normalizedCurrentRole == "superadmin" || allowedRoles.any { role ->
            normalizedCurrentRole.contains(role.trim().lowercase())
        }
    }

    private fun isSupervisorPinValid(pin: String): Boolean {
        val supervisorPins = setOf("195026", "SA1CBT", "889900")
        return pin.trim() in supervisorPins
    }

    private fun filterModulesByRole(
        userRole: String?,
        modules: List<Pair<String, String>> // Pair(id, route)
    ): List<Pair<String, String>> {
        val isUserExecutive = userRole?.contains("admin", true) == true ||
                userRole?.contains("superadmin", true) == true ||
                userRole?.contains("kepsek", true) == true ||
                userRole?.contains("principal", true) == true
        val isUserTeacherOrAbove = isUserExecutive ||
                userRole?.contains("teacher", true) == true ||
                userRole?.contains("guru", true) == true
        val isUserParentOrAbove = isUserExecutive ||
                userRole?.contains("parent", true) == true ||
                userRole?.contains("ortu", true) == true

        return modules.filter { (id, route) ->
            val isExecutiveOnly = route == "executive_analytics" || route == "admin_dashboard" || id == "m25"
            val isTeacherOnly = route.startsWith("teacher") || route == "question_bank" || route == "auto_generate_exam" || route == "class_analytics" || id == "m23" || route == "digital_signature" || id == "m21"
            val isParentOnly = id == "m24" || route == "child_progress"

            when {
                isExecutiveOnly -> isUserExecutive
                isTeacherOnly -> isUserTeacherOrAbove
                isParentOnly -> isUserParentOrAbove
                else -> true
            }
        }
    }

    private fun getCategoriesForRole(userRole: String?): List<String> {
        val isUserExecutive = userRole?.contains("admin", true) == true ||
                userRole?.contains("superadmin", true) == true ||
                userRole?.contains("kepsek", true) == true ||
                userRole?.contains("principal", true) == true
        val isUserTeacherOrAbove = isUserExecutive ||
                userRole?.contains("teacher", true) == true ||
                userRole?.contains("guru", true) == true
        val isUserParentOrAbove = isUserExecutive ||
                userRole?.contains("parent", true) == true ||
                userRole?.contains("ortu", true) == true

        val list = mutableListOf("Semua")
        if (isUserExecutive) {
            list.add("Rahasia Pimpinan")
        }
        if (isUserTeacherOrAbove) {
            list.add("Khusus Guru")
        }
        if (isUserParentOrAbove) {
            list.add("Wali Murid")
        }
        list.addAll(
            listOf(
                "Khusus Siswa",
                "Akademik & LMS",
                "Presensi & IoT",
                "AI & Web3",
                "Kesiswaan & Ibadah",
                "Keuangan & Tata Usaha"
            )
        )
        return list
    }

    private fun isRoleSwitcherButtonVisible(userRole: String?): Boolean {
        return userRole?.contains("admin", ignoreCase = true) == true ||
                userRole?.contains("superadmin", ignoreCase = true) == true
    }

    @Test
    fun studentRole_isDeniedFromAdminDashboard() {
        val allowedRoles = listOf("admin", "superadmin", "principal", "kepsek")
        assertFalse(isRoleAuthorized("student", allowedRoles))
        assertFalse(isRoleAuthorized("siswa", allowedRoles))
    }

    @Test
    fun studentRole_isDeniedFromTeacherTools() {
        val allowedRoles = listOf("teacher", "guru", "admin", "superadmin")
        assertFalse(isRoleAuthorized("student", allowedRoles))
    }

    @Test
    fun teacherRole_isPermittedOnTeacherTools() {
        val allowedRoles = listOf("teacher", "guru", "admin", "superadmin")
        assertTrue(isRoleAuthorized("teacher", allowedRoles))
        assertTrue(isRoleAuthorized("guru", allowedRoles))
    }

    @Test
    fun adminRole_isPermittedOnAdminAndTeacherTools() {
        val adminRoles = listOf("admin", "superadmin", "principal", "kepsek")
        val teacherRoles = listOf("teacher", "guru", "admin", "superadmin")

        assertTrue(isRoleAuthorized("admin", adminRoles))
        assertTrue(isRoleAuthorized("admin", teacherRoles))
        assertTrue(isRoleAuthorized("superadmin", adminRoles))
        assertTrue(isRoleAuthorized("superadmin", teacherRoles))
    }

    @Test
    fun parentRole_isPermittedOnParentDashboard() {
        val parentRoles = listOf("parent", "ortu", "admin", "superadmin")
        assertTrue(isRoleAuthorized("parent", parentRoles))
        assertTrue(isRoleAuthorized("ortu", parentRoles))
        assertFalse(isRoleAuthorized("student", parentRoles))
    }

    @Test
    fun studentRole_completelyHidesUnauthorizedModulesFromCatalog() {
        val sampleModules = listOf(
            Pair("m8", "cbt_list"),
            Pair("m12", "mutabaah"),
            Pair("m21", "digital_signature"),
            Pair("m23", "class_analytics"),
            Pair("m24", "child_progress"),
            Pair("m25", "executive_analytics")
        )

        val studentVisible = filterModulesByRole("student", sampleModules)
        val visibleIds = studentVisible.map { it.first }

        assertTrue(visibleIds.contains("m8"))
        assertTrue(visibleIds.contains("m12"))
        assertFalse(visibleIds.contains("m21")) // TTD Digital hidden from student
        assertFalse(visibleIds.contains("m23")) // Class Analytics hidden from student
        assertFalse(visibleIds.contains("m24")) // Parent progress hidden from student
        assertFalse(visibleIds.contains("m25")) // Executive Analytics hidden from student
    }

    @Test
    fun studentRole_completelyHidesPrivilegedCategories() {
        val categories = getCategoriesForRole("student")
        assertFalse(categories.contains("Rahasia Pimpinan"))
        assertFalse(categories.contains("Khusus Guru"))
        assertFalse(categories.contains("Wali Murid"))
        assertTrue(categories.contains("Khusus Siswa"))
        assertTrue(categories.contains("Akademik & LMS"))
    }

    @Test
    fun adminRole_canAccessAllModulesAndCategories() {
        val sampleModules = listOf(
            Pair("m8", "cbt_list"),
            Pair("m21", "digital_signature"),
            Pair("m23", "class_analytics"),
            Pair("m24", "child_progress"),
            Pair("m25", "executive_analytics")
        )

        val adminVisible = filterModulesByRole("admin", sampleModules)
        assertEquals(sampleModules.size, adminVisible.size)

        val categories = getCategoriesForRole("admin")
        assertTrue(categories.contains("Rahasia Pimpinan"))
        assertTrue(categories.contains("Khusus Guru"))
        assertTrue(categories.contains("Wali Murid"))
    }

    @Test
    fun roleSwitcherButton_isOnlyVisibleForAdminAndSuperadmin() {
        assertFalse(isRoleSwitcherButtonVisible("student"))
        assertFalse(isRoleSwitcherButtonVisible("siswa"))
        assertFalse(isRoleSwitcherButtonVisible("parent"))
        assertFalse(isRoleSwitcherButtonVisible("ortu"))
        assertFalse(isRoleSwitcherButtonVisible("teacher"))
        assertFalse(isRoleSwitcherButtonVisible("guru"))

        assertTrue(isRoleSwitcherButtonVisible("admin"))
        assertTrue(isRoleSwitcherButtonVisible("superadmin"))
    }

    @Test
    fun supervisorPin_validatesCorrectly() {
        assertTrue(isSupervisorPinValid("195026"))
        assertTrue(isSupervisorPinValid("SA1CBT"))
        assertTrue(isSupervisorPinValid("889900"))

        assertFalse(isSupervisorPinValid("000000"))
        assertFalse(isSupervisorPinValid("123456"))
        assertFalse(isSupervisorPinValid("admin"))
    }
}
