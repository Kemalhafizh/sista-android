package com.sultanagung1.sista.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ProfessionalUiUxOverhaulTest {

    @Test
    fun testSulaoneMetricCardComponentExistsAndFollowsStandards() {
        val file = File("src/main/java/com/sultanagung1/sista/core/designsystem/SulaoneMetricCard.kt")
        assertTrue("SulaoneMetricCard.kt must exist", file.exists())

        val content = file.readText()
        assertTrue("Must declare SulaoneMetricCard composable", content.contains("fun SulaoneMetricCard"))
        assertTrue("Must support badge text for trends/KPIs", content.contains("badgeText: String? = null"))
        assertTrue("Must support springPressable for haptic feedback", content.contains("springPressable"))
        assertTrue("Must use standard 0.5dp subtle border", content.contains("0.5.dp"))
    }

    @Test
    fun testSulaoneExecutiveHeaderAdoptsModernMinimalism() {
        val file = File("src/main/java/com/sultanagung1/sista/ui/common/SulaoneExecutiveHeader.kt")
        assertTrue("SulaoneExecutiveHeader.kt must exist", file.exists())

        val content = file.readText()
        assertTrue("Must declare SulaoneExecutiveHeader", content.contains("fun SulaoneExecutiveHeader"))
        assertTrue("Must declare HeaderMetadataChip", content.contains("data class HeaderMetadataChip"))
        assertTrue("Must have official school crest logo_kotak", content.contains("R.drawable.logo_kotak"))
        assertTrue("Must support 48dp WCAG touch targets", content.contains("sulaoneInteractiveTouchTarget(48.dp)"))
        assertTrue("Must have verified badge", content.contains("Icons.Default.Verified"))
        assertTrue("Must use theme-aware surface background", content.contains("MaterialTheme.colorScheme.surface"))
    }

    @Test
    fun testTeacherDashboardFollowsExecutiveStandards() {
        val file = File("src/main/java/com/sultanagung1/sista/ui/teacher/TeacherDashboardScreen.kt")
        assertTrue("TeacherDashboardScreen.kt must exist", file.exists())

        val content = file.readText()
        assertTrue("Must use SulaoneExecutiveHeader", content.contains("SulaoneExecutiveHeader("))
        assertTrue("Must use SulaoneMetricCard", content.contains("SulaoneMetricCard("))
        assertTrue("Must show Beban Mengajar metric", content.contains("Beban Mengajar"))
        assertTrue("Must show Kelas Diampu metric", content.contains("Kelas Diampu"))
        assertTrue("Must show Presensi Siswa button", content.contains("Presensi Siswa"))
        assertTrue("Must show Isi Jurnal button", content.contains("Isi Jurnal"))
        assertTrue("Must have 48dp touch targets", content.contains("sulaoneInteractiveTouchTarget(48.dp)"))
    }

    @Test
    fun testParentDashboardFollowsExecutiveStandards() {
        val file = File("src/main/java/com/sultanagung1/sista/ui/parent/ParentDashboardScreen.kt")
        assertTrue("ParentDashboardScreen.kt must exist", file.exists())

        val content = file.readText()
        assertTrue("Must use SulaoneExecutiveHeader", content.contains("SulaoneExecutiveHeader("))
        assertTrue("Must use SulaoneMetricCard", content.contains("SulaoneMetricCard("))
        assertTrue("Must have ParentChildPersonaCard", content.contains("ParentChildPersonaCard("))
        assertTrue("Must have live gate presence status", content.contains("Presensi Gerbang"))
        assertTrue("Must support WhatsApp Guru with WCAG target", content.contains("WhatsApp Guru"))
        assertTrue("Must support Rapor Digital button", content.contains("Rapor Digital"))
        assertTrue("Must have 48dp touch targets", content.contains("sulaoneInteractiveTouchTarget(48.dp)"))
    }

    @Test
    fun testAdminDashboardFollowsExecutiveStandards() {
        val file = File("src/main/java/com/sultanagung1/sista/ui/admin/AdminDashboardScreen.kt")
        assertTrue("AdminDashboardScreen.kt must exist", file.exists())

        val content = file.readText()
        assertTrue("Must use SulaoneExecutiveHeader", content.contains("SulaoneExecutiveHeader("))
        assertTrue("Must have KOKPIT EKSEKUTIF PIMPINAN title", content.contains("KOKPIT EKSEKUTIF PIMPINAN"))
        assertTrue("Must use SulaoneMetricCard for KPIs", content.contains("SulaoneMetricCard("))
        assertTrue("Must have Kehadiran Siswa KPI", content.contains("Kehadiran Siswa"))
        assertTrue("Must have Kolektibilitas SPP KPI", content.contains("Kolektibilitas SPP"))
        assertTrue("Must have Guru Mengajar KPI", content.contains("Guru Mengajar"))
        assertTrue("Must have ModernApprovalCard with 1-tap buttons", content.contains("ModernApprovalCard"))
        assertTrue("Must have Setujui and Tolak buttons", content.contains("Setujui") && content.contains("Tolak"))
    }

    @Test
    fun testProfileScreenImplementsDigitalInstitutionalId() {
        val file = File("src/main/java/com/sultanagung1/sista/ui/profile/ProfileScreen.kt")
        assertTrue("ProfileScreen.kt must exist", file.exists())

        val content = file.readText()
        assertTrue("Must have DigitalInstitutionalIdCard", content.contains("DigitalInstitutionalIdCard("))
        assertTrue("Must include official school branding", content.contains("SMA ISLAM SULTAN AGUNG 1"))
        assertTrue("Must include verified smart card token", content.contains("SMART CARD TOKEN • VERIFIED"))
        assertTrue("Must use ModernProfileMenuItem with 48dp target", content.contains("ModernProfileMenuItem("))
        assertTrue("Must support Dark Mode toggle", content.contains("Mode Gelap (Dark Mode)"))
        assertTrue("Must support Multi-Role switcher", content.contains("Ganti Peran Dashboard (Multi-Role)"))
    }

    @Test
    fun testLoginScreenAdoptsProfessionalInstitutionalDesign() {
        val file = File("src/main/java/com/sultanagung1/sista/ui/auth/LoginScreen.kt")
        assertTrue("LoginScreen.kt must exist", file.exists())

        val content = file.readText()
        assertTrue("Must have official school crest with aura", content.contains("logo_kotak"))
        assertTrue("Must have SULAONE headline", content.contains("SULAONE"))
        assertTrue("Must have UserRoleTab enum", content.contains("enum class UserRoleTab"))
        assertTrue("Must have BiometricVault authentication", content.contains("BiometricVault.authenticate"))
        assertTrue("Must have TEE & Keystore security assurance", content.contains("Terlindungi Keamanan Enkripsi TEE & Keystore"))
        assertTrue("Must have 48dp WCAG touch targets", content.contains("sulaoneInteractiveTouchTarget(48.dp)"))
    }
}
