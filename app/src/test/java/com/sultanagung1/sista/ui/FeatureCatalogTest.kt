package com.sultanagung1.sista.ui

import com.sultanagung1.sista.data.model.AppFeature
import com.sultanagung1.sista.data.model.Capabilities
import com.sultanagung1.sista.data.model.CapabilityState
import com.sultanagung1.sista.data.model.FeatureAccess
import com.sultanagung1.sista.data.model.FeatureGroup
import com.sultanagung1.sista.data.model.GateDecision
import com.sultanagung1.sista.ui.navigation.FeatureCatalog
import com.sultanagung1.sista.ui.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The navigation guard trusts FeatureCatalog for every route. These tests
 * keep it complete (no screen left unguarded by accident) and check the
 * decisions the shell makes from the server's capability list.
 */
class FeatureCatalogTest {

    private fun caps(vararg keys: String, role: String = "x") = Capabilities(
        role = role,
        features = keys.map { AppFeature(key = it, title = it, group = it.substringBefore('.')) },
    )

    private fun ready(vararg keys: String) = CapabilityState.Ready(caps(*keys))

    private val allScreens: List<Screen> = Screen::class.sealedSubclasses.mapNotNull { it.objectInstance }

    @Test
    fun `every screen is either open or needs a feature`() {
        val unclassified = allScreens.map { it.route }.filter {
            it !in FeatureCatalog.OPEN && it !in FeatureCatalog.REQUIRES
        }
        assertEquals("Add these screens to FeatureCatalog", emptyList<String>(), unclassified)
    }

    @Test
    fun `no screen is both open and guarded`() {
        assertEquals(emptySet<String>(), FeatureCatalog.OPEN intersect FeatureCatalog.REQUIRES.keys)
    }

    @Test
    fun `menu entries are navigable routes, never patterns`() {
        // Navigating to "discipline?studentUuid={studentUuid}" handed the screen the
        // literal "{studentUuid}" as the child's id.
        val patterns = FeatureCatalog.ENTRY.filterValues { '{' in it }
        assertEquals(emptyMap<String, String>(), patterns)
    }

    @Test
    fun `a feature's entry screen is one that feature opens`() {
        FeatureCatalog.ENTRY.forEach { (key, route) ->
            val required = FeatureCatalog.requiredFor(route)
            assertTrue("$key opens $route, which does not accept $key", required == null || key in required)
        }
    }

    @Test
    fun `the account's features open their screens and nothing else`() {
        val student = ready("student.schedule", "student.grades", "notifications")

        assertEquals(GateDecision.OPEN, FeatureCatalog.decide(student, Screen.Schedule.route))
        assertEquals(GateDecision.OPEN, FeatureCatalog.decide(student, Screen.NotificationCenter.route))
        assertEquals(GateDecision.LOCKED, FeatureCatalog.decide(student, Screen.TeacherDashboard.route))
        assertEquals(GateDecision.LOCKED, FeatureCatalog.decide(student, Screen.AdminAttendanceOverride.route))
        assertEquals(GateDecision.LOCKED, FeatureCatalog.decide(student, Screen.ParentDashboard.route))
    }

    @Test
    fun `routes with arguments are judged by their pattern`() {
        val parent = ready("parent.children")
        assertEquals(GateDecision.OPEN, FeatureCatalog.decide(parent, Screen.ChildDetail.route))
        assertEquals(GateDecision.OPEN, FeatureCatalog.decide(parent, Screen.StudentProfileComprehensive.route))
        assertEquals(GateDecision.LOCKED, FeatureCatalog.decide(parent, Screen.CbtRoom.route))
    }

    @Test
    fun `a screen shared by two audiences opens for either`() {
        assertEquals(GateDecision.OPEN, FeatureCatalog.decide(ready("parent.messages"), Screen.Chat.route))
        assertEquals(GateDecision.OPEN, FeatureCatalog.decide(ready("teacher.messages"), Screen.Chat.route))
        assertEquals(GateDecision.LOCKED, FeatureCatalog.decide(ready("student.grades"), Screen.Chat.route))
    }

    @Test
    fun `open screens stay open while the list loads or fails`() {
        assertEquals(GateDecision.OPEN, FeatureCatalog.decide(CapabilityState.Loading, Screen.Profile.route))
        assertEquals(GateDecision.OPEN, FeatureCatalog.decide(CapabilityState.Failed("x"), Screen.Settings.route))
        assertEquals(GateDecision.WAITING, FeatureCatalog.decide(CapabilityState.Loading, Screen.Grades.route))
        assertEquals(GateDecision.UNKNOWN, FeatureCatalog.decide(CapabilityState.Failed("x"), Screen.Grades.route))
        assertEquals(GateDecision.LOCKED, FeatureCatalog.decide(CapabilityState.SignedOut, Screen.Grades.route))
    }

    @Test
    fun `an unknown route is shut, not open`() {
        assertEquals(setOf(FeatureCatalog.NOT_READY), FeatureCatalog.requiredFor("some_new_screen/{id}"))
        assertEquals(GateDecision.LOCKED, FeatureCatalog.decide(ready("student.grades"), "some_new_screen/{id}"))
    }

    @Test
    fun `the SOS screen that reports nothing is shut for everyone`() {
        val everything = ready(*FeatureCatalog.REQUIRES.values.flatten().filter { it != FeatureCatalog.NOT_READY }.toTypedArray())
        assertEquals(GateDecision.LOCKED, FeatureCatalog.decide(everything, Screen.AntiBullyingSos.route))
    }

    @Test
    fun `home follows the account's features, not a local role`() {
        assertEquals(Screen.AdminDashboard.route, FeatureCatalog.homeRouteFor(caps("admin.dashboard", "admin.class_sessions")))
        assertEquals(Screen.TeacherDashboard.route, FeatureCatalog.homeRouteFor(caps("teacher.sessions")))
        assertEquals(Screen.ParentDashboard.route, FeatureCatalog.homeRouteFor(caps("parent.children")))
        assertEquals(Screen.Home.route, FeatureCatalog.homeRouteFor(caps("student.schedule")))
        assertEquals(Screen.AdminSessionManagement.route, FeatureCatalog.homeRouteFor(caps("admin.class_sessions")))
        // A canteen merchant has no dashboard: they land on Layanan.
        assertEquals(Screen.ServicesHub.route, FeatureCatalog.homeRouteFor(caps("announcements")))
    }

    @Test
    fun `features are grouped in the server's order and empty groups are dropped`() {
        val capabilities = Capabilities(
            features = listOf(
                AppFeature("b1", "B1", "b"),
                AppFeature("a1", "A1", "a"),
                AppFeature("z1", "Z1", "zzz"),
            ),
            groups = listOf(FeatureGroup("a", "A"), FeatureGroup("b", "B"), FeatureGroup("c", "C")),
        )
        val grouped = FeatureAccess.grouped(capabilities)
        assertEquals(listOf("a", "b", "zzz"), grouped.map { it.first.key })
        assertEquals(listOf("b1"), grouped[1].second.map { it.key })
    }

    /** Feature keys whose screens FASE 78 removed; the server may keep sending them for a while. */
    private val removedKeys = listOf(
        "attendance.gps", "attendance.id_qr", "attendance.face_enroll", "student.ai_tutor", "student.ai_essay",
        "student.gamification", "documents.scan", "documents.signature", "admin.executive", "scanner.identity",
    )

    @Test
    fun `removed features have no screen and no gate`() {
        removedKeys.forEach { key ->
            assertTrue("$key still opens a screen", key !in FeatureCatalog.ENTRY)
            assertTrue("$key still guards a route", FeatureCatalog.REQUIRES.values.none { key in it })
        }
    }

    @Test
    fun `keys the app does not know are skipped, in any order with the server`() {
        // A server that still sends the removed keys, and one that already sends
        // a key this app has never heard of: the menu shows only what it can open.
        val capabilities = Capabilities(
            features = (removedKeys + listOf("student.schedule", "student.something_new", "announcements"))
                .map { AppFeature(key = it, title = it, group = "akademik") },
            groups = listOf(FeatureGroup("akademik", "Akademik")),
        )
        val listed = FeatureCatalog.menuFeatures(capabilities).flatMap { (_, features) -> features.map { it.key } }
        assertEquals(listOf("student.schedule", "announcements"), listed)

        val state = CapabilityState.Ready(capabilities)
        assertEquals(GateDecision.OPEN, FeatureCatalog.decide(state, Screen.Schedule.route))
        assertEquals(GateDecision.LOCKED, FeatureCatalog.decide(state, Screen.QrScanner.route))
        assertEquals(Screen.Home.route, FeatureCatalog.homeRouteFor(capabilities))
    }

    @Test
    fun `the menu search matches titles and lists a shared screen once`() {
        val capabilities = Capabilities(
            features = listOf(
                AppFeature("parent.messages", "Pesan", "komunikasi"),
                AppFeature("teacher.messages", "Pesan", "komunikasi"),
                AppFeature("announcements", "Pengumuman", "komunikasi"),
            ),
        )
        assertEquals(listOf("parent.messages", "announcements"), FeatureCatalog.menuFeatures(capabilities).single().second.map { it.key })
        assertEquals(listOf("announcements"), FeatureCatalog.menuFeatures(capabilities, " umum ").single().second.map { it.key })
    }
}
