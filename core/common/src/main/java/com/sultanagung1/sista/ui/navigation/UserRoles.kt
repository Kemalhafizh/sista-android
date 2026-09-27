package com.sultanagung1.sista.ui.navigation

/**
 * FASE 76.3: one place that decides which "home" a role belongs to.
 *
 * The backend sends `users.role` verbatim at login (MobileAuthController), and
 * its seeders use several spellings for the same person: `parent` and
 * `orang_tua`, `student` and `siswa`, `kepala_sekolah` next to `kepsek`. The app
 * only ever matched the English/short forms, so an `orang_tua` account landed on
 * the student Home and `RoleGuardedScreen` refused it the parent portal, and a
 * `kepala_sekolah` account was treated as a student. [normalize] folds those
 * aliases into the vocabulary the rest of the app already checks for; it is
 * applied once, in SessionManager.userRoleFlow.
 */
enum class RoleGroup { STUDENT, TEACHER, PARENT, ADMIN }

object UserRoles {

    private val ALIASES = mapOf(
        "orang_tua" to "parent",
        "wali_murid" to "parent",
        "siswa" to "student",
        "kepala_sekolah" to "kepsek"
    )

    /** Backend alias → app vocabulary. Unknown roles are returned unchanged. */
    fun normalize(raw: String?): String? {
        val trimmed = raw?.trim() ?: return null
        return ALIASES[trimmed.lowercase()] ?: trimmed
    }

    fun groupOf(role: String?): RoleGroup {
        val r = normalize(role).orEmpty()
        return when {
            r.contains("teacher", ignoreCase = true) ||
                r.contains("guru", ignoreCase = true) ||
                r.equals("bk", ignoreCase = true) -> RoleGroup.TEACHER
            r.contains("parent", ignoreCase = true) || r.contains("ortu", ignoreCase = true) -> RoleGroup.PARENT
            r.contains("admin", ignoreCase = true) ||
                r.contains("kepsek", ignoreCase = true) ||
                r.contains("principal", ignoreCase = true) -> RoleGroup.ADMIN
            else -> RoleGroup.STUDENT
        }
    }

    fun homeRouteFor(role: String?): String = when (groupOf(role)) {
        RoleGroup.TEACHER -> Screen.TeacherDashboard.route
        RoleGroup.PARENT -> Screen.ParentDashboard.route
        RoleGroup.ADMIN -> Screen.AdminDashboard.route
        RoleGroup.STUDENT -> Screen.Home.route
    }
}
