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
enum class RoleGroup {
    STUDENT,
    TEACHER,
    PARENT,
    ADMIN,
    /**
     * FASE 77.6: Waka Kurikulum and TU. They have no dashboard endpoint of
     * their own (the admin one refuses them), but backend FASE 117 lets them
     * run class-session management and attendance correction, so that is
     * their home. They used to land on the student Home, where every call 403s.
     */
    ACADEMIC_STAFF
}

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
            ACADEMIC_STAFF_ROLES.any { it.equals(r, ignoreCase = true) } -> RoleGroup.ACADEMIC_STAFF
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
        RoleGroup.ACADEMIC_STAFF -> Screen.AdminSessionManagement.route
    }

    /** Only these two; other `waka_*` roles are not allowed on the class-session endpoints. */
    private val ACADEMIC_STAFF_ROLES = listOf("waka_kurikulum", "staf_tu")

    /**
     * FASE 77.6 / backend 117: who may open class-session management (read).
     * Mirrors the route middleware in the contract (android_implementation.md → 77.0).
     */
    val CLASS_SESSION_VIEWER_ROLES = listOf("admin", "superadmin", "kepsek", "kepala_sekolah", "waka_kurikulum", "staf_tu")

    /** Who may correct a recorded attendance. The principal can read, not correct. */
    val CLASS_SESSION_CORRECTOR_ROLES = listOf("admin", "superadmin", "waka_kurikulum", "staf_tu")

    fun canViewClassSessions(role: String?): Boolean = normalize(role)?.lowercase() in CLASS_SESSION_VIEWER_ROLES

    fun canCorrectClassAttendance(role: String?): Boolean = normalize(role)?.lowercase() in CLASS_SESSION_CORRECTOR_ROLES
}
