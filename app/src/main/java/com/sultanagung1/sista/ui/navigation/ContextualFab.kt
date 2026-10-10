package com.sultanagung1.sista.ui.navigation

/**
 * FASE 76.3: the one primary action the root Scaffold's floating button offers
 * on a role's home tab. Pure data (no Compose types) so the routing rules are
 * unit-testable; AppNavigation maps each action to its icon.
 *
 * Rules:
 * - Only on a role's own home dashboard. The other tabs (Jadwal, Notifikasi,
 *   Profil, Nilai) have no single dominant action, and Notifikasi already
 *   carries its own button.
 * - Never on an exam route: the button is root-level, so it would otherwise
 *   float over CbtRoom / CbtTokenEntry. [ContextualFab.actionFor] returns null
 *   for every route not listed there, which covers them by construction.
 */
enum class ContextualFabAction(val label: String, val targetRoute: String) {
    /** Student home: scanning the class QR, the one way a student is marked present. */
    QUICK_ATTENDANCE("Presensi", Screen.StudentSessionQrScan.route),

    /** Teacher dashboard: the daily KBM journal. */
    TEACHING_JOURNAL("Jurnal KBM", Screen.TeachingJournalMobile.route),

    /** Parent portal: message the child's teachers. */
    MESSAGE_TEACHER("Pesan Guru", Screen.ConversationList.route)
}

object ContextualFab {

    fun actionFor(currentRoute: String?, userRole: String?): ContextualFabAction? {
        // Same classifier as navigateToRoleHome/bottomNavItems, so the button
        // never disagrees with which dashboard the role lands on.
        val group = UserRoles.groupOf(userRole)
        return when (currentRoute) {
            Screen.Home.route -> ContextualFabAction.QUICK_ATTENDANCE.takeIf { group == RoleGroup.STUDENT }
            Screen.TeacherDashboard.route -> ContextualFabAction.TEACHING_JOURNAL.takeIf { group == RoleGroup.TEACHER }
            Screen.ParentDashboard.route -> ContextualFabAction.MESSAGE_TEACHER.takeIf { group == RoleGroup.PARENT }
            // Admin has no mobile action that deserves a one-tap button (the
            // emergency broadcast deliberately sits behind a confirmation on
            // its own screen), so no FAB there.
            else -> null
        }
    }
}
