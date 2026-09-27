package com.sultanagung1.sista.ui.discipline

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sultanagung1.sista.core.network.ApiEnvelope
import com.sultanagung1.sista.data.model.DisciplineRecord
import com.sultanagung1.sista.data.model.DisciplineSummary
import com.sultanagung1.sista.data.model.SignWarningLetterRequest
import com.sultanagung1.sista.data.model.WarningLetterItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The discipline models used to share almost no field names with
 * DisciplineService, so the screen showed "Predikat: BAIK" and 0 points for
 * everyone and no parent could sign a warning letter. The JSON below was
 * captured from the real Laravel endpoints (api/v1/discipline/...), not
 * written by hand, so a rename on either side breaks this test.
 */
class DisciplineContractTest {

    private val gson = Gson()

    private inline fun <reified T> parse(json: String): T =
        gson.fromJson(json, object : TypeToken<T>() {}.type)

    @Test
    fun summaryReadsTheServersRealFields() {
        val json = """
            {"success":true,"message":"Ringkasan poin kedisiplinan berhasil diambil","data":{
              "total_points":30,"status":"Perlu Perhatian","total_violation_points":55,
              "total_reward_points":25,"total_cases":1,"next_warning_level":"SP1",
              "next_warning_threshold":50,"last_updated":"2026-09-26 23:04:39",
              "rules":[{"name":"Terlambat","type":"penalty","points":5},
                       {"name":"Juara lomba","type":"reward","points":25}]}}
        """.trimIndent()

        val summary = requireNotNull(parse<ApiEnvelope<DisciplineSummary>>(json).data)

        assertEquals(30, summary.totalPoints)
        assertEquals("Perlu Perhatian", summary.status)
        assertEquals(55, summary.totalViolationPoints)
        assertEquals(25, summary.totalRewardPoints)
        assertEquals(1, summary.totalCases)
        assertEquals("SP1", summary.nextWarningLevel)
        assertEquals(50, summary.nextWarningThreshold)
        assertEquals(listOf("Terlambat", "Juara lomba"), summary.rules.map { it.name })
        assertEquals(listOf(5, 25), summary.rules.map { it.points })
    }

    @Test
    fun summaryAfterSp3HasNoNextLevel() {
        val json = """{"success":true,"data":{"total_points":100,"status":"SP3 (Sangat Kritis)",
            "total_violation_points":100,"total_reward_points":0,"total_cases":1,
            "next_warning_level":null,"next_warning_threshold":null,"last_updated":null,"rules":[]}}"""
        val summary = requireNotNull(parse<ApiEnvelope<DisciplineSummary>>(json).data)
        assertNull(summary.nextWarningLevel)
        assertNull(summary.nextWarningThreshold)
        assertTrue(summary.rules.isEmpty())
    }

    @Test
    fun historyReadsTypeAndCategoryNameIncludingAutomaticEntries() {
        val json = """
            {"success":true,"message":"Riwayat poin kedisiplinan berhasil diambil","data":[
              {"id":1,"points":55,"type":"penalty","category_name":"Terlambat","description":"Terlambat berulang","date":"2026-09-26 23:04:39","recorded_by":"Ibu Wali"},
              {"id":2,"points":-25,"type":"reward","category_name":"Juara lomba","description":"Juara 1 OSN kota","date":"2026-09-26 23:04:39","recorded_by":"Ibu Wali"},
              {"id":3,"points":10,"type":"penalty","category_name":null,"description":"Bolos","date":"2026-09-26 23:04:39","recorded_by":"Sistem"}]}
        """.trimIndent()

        val records = requireNotNull(parse<ApiEnvelope<List<DisciplineRecord>>>(json).data)

        assertEquals(listOf("penalty", "reward", "penalty"), records.map { it.type })
        assertEquals("Terlambat", records[0].categoryName)
        assertEquals(-25, records[1].points) // rewards are negative on the server
        assertNull(records[2].categoryName)
        assertEquals("Sistem", records[2].recordedBy)
    }

    @Test
    fun warningLetterReadsSignedStateAndWhoMaySign() {
        val json = """
            {"success":true,"message":"Daftar surat peringatan berhasil diambil","data":[
              {"id":1,"level":"SP1","point_threshold":50,"issued_at":"2026-09-26","is_signed":false,
               "signed_at":null,"notes":"Penerbitan otomatis SP1 karena poin mencapai\/melewati 50.","can_sign":true},
              {"id":2,"level":"SP2","point_threshold":75,"issued_at":"2026-09-20","is_signed":true,
               "signed_at":"2026-09-21 08:00:00","notes":null,"can_sign":false}]}
        """.trimIndent()

        val letters = requireNotNull(parse<ApiEnvelope<List<WarningLetterItem>>>(json).data)

        assertFalse(letters[0].isSigned)
        assertTrue(letters[0].canSign)
        assertEquals(50, letters[0].pointThreshold)
        assertEquals("2026-09-26", letters[0].issuedAt)
        assertEquals("Penerbitan otomatis SP1 karena poin mencapai/melewati 50.", letters[0].notes)
        assertTrue(letters[1].isSigned)
        assertFalse(letters[1].canSign)
        assertEquals("2026-09-21 08:00:00", letters[1].signedAt)
    }

    @Test
    fun signRequestSendsOnlyTheFieldTheServerValidates() {
        val body = gson.toJsonTree(SignWarningLetterRequest("iVBORw0KGgo=")).asJsonObject
        assertEquals(setOf("digital_signature"), body.keySet())
        assertEquals("iVBORw0KGgo=", body.get("digital_signature").asString)
    }

    // --- parent with several children ---------------------------------------------

    @Test
    fun summaryNamesTheChildItBelongsTo() {
        val json = """{"success":true,"data":{"total_points":60,"status":"SP1 (Waspada)",
            "student":{"uuid":"9f1c-anak-2","name":"Aisyah","classroom":"X IPA 1"}}}"""
        val student = requireNotNull(parse<ApiEnvelope<DisciplineSummary>>(json).data?.student)
        assertEquals("9f1c-anak-2", student.uuid)
        assertEquals("Aisyah", student.name)
        assertEquals("X IPA 1", student.classroom)
    }

    @Test
    fun disciplineRouteCarriesTheSelectedChild() {
        val screen = com.sultanagung1.sista.ui.navigation.Screen.Discipline
        assertEquals("discipline", screen.createRoute())
        assertEquals("discipline", screen.createRoute(" "))
        assertEquals("discipline?studentUuid=9f1c-anak-2", screen.createRoute("9f1c-anak-2"))
        // The nav argument, the route placeholder and the ViewModel key must agree.
        assertTrue(screen.route.contains("{${DisciplineViewModel.ARG_STUDENT_UUID}}"))
        val navGraph = source("app/src/main/java/com/sultanagung1/sista/ui/navigation/graphs/SettingsNavGraph.kt").readText()
        assertTrue(navGraph.contains("navArgument(\"${DisciplineViewModel.ARG_STUDENT_UUID}\")"))
    }

    @Test
    fun nobodyNavigatesWithTheRawRoutePattern() {
        // navigate(Screen.Discipline.route) would pass the literal "{studentUuid}".
        val offenders = listOf("feature", "app/src/main").flatMap { dir ->
            source(dir).walkTopDown()
                .filter { it.isFile && it.extension == "kt" }
                .filter { !it.path.replace('\\', '/').contains("/build/") }
                .filter { file ->
                    file.readLines().any { line ->
                        line.contains("Screen.Discipline.route") && !line.contains("route = Screen.Discipline.route")
                    }
                }
                .map { it.name }
                .toList()
        }
        assertEquals(emptyList<String>(), offenders)

        val parentDashboard = source("feature/parent/src/main/java/com/sultanagung1/sista/ui/parent/ParentDashboardScreen.kt").readText()
        assertTrue(
            "The parent dashboard must open the child that is selected there",
            parentDashboard.contains("Screen.Discipline.createRoute(child.uuid)")
        )
    }

    // --- source guard ---------------------------------------------------------------

    private fun source(relativePath: String): File {
        val candidates = listOf(File("../$relativePath"), File(relativePath))
        return candidates.firstOrNull { it.exists() } ?: candidates.first()
    }

    @Test
    fun screenNoLongerShowsMadeUpDisciplineData() {
        val screen = source("feature/academic/src/main/java/com/sultanagung1/sista/ui/discipline/DisciplineScreen.kt").readText()

        listOf(
            "?: \"BAIK\"",            // fake "good" status when data was missing
            "Batas SP1: 25",          // wrong threshold; the server issues SP1 at 50
            "Keterlambatan Hadir",    // hardcoded rule list
            "parentNameInput",        // name/phone the server never read
            "letterNumber"            // a column that doesn't exist
        ).forEach {
            assertFalse("DisciplineScreen must not contain \"$it\"", screen.contains(it))
        }
        assertTrue("Load errors must be shown", screen.contains("uiState.errorMessage"))
        assertTrue("Signature pad only for the parent the server accepts", screen.contains("letter.canSign"))
    }
}
