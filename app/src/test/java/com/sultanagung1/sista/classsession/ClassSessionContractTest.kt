package com.sultanagung1.sista.classsession

import com.google.gson.JsonParser
import com.sultanagung1.sista.data.api.ClassSessionApiService
import com.sultanagung1.sista.data.model.ClassSessionErrorKind
import com.sultanagung1.sista.data.model.ClassSessionResult
import com.sultanagung1.sista.data.model.ClassSessionRules
import com.sultanagung1.sista.data.model.ClassSessionStatus
import com.sultanagung1.sista.data.model.SessionAttendanceStatus
import com.sultanagung1.sista.data.model.SessionCheckInMethod
import com.sultanagung1.sista.data.repository.ClassSessionRepository
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * FASE 77 ↔ FASE 117 wire contract, through the real Retrofit/OkHttp/Gson
 * stack against a loopback server. The JSON bodies are what the Laravel side
 * must send (android_implementation.md → "77.0 Kontrak API"): if the backend
 * changes a key, the matching case here is the one to update together with it.
 */
class ClassSessionContractTest {

    private lateinit var server: MockWebServer
    private lateinit var repo: ClassSessionRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val api = Retrofit.Builder()
            .baseUrl(server.url("/api/v1/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ClassSessionApiService::class.java)
        repo = ClassSessionRepository(api)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun respond(code: Int, body: String) {
        server.enqueue(MockResponse().setResponseCode(code).setHeader("Content-Type", "application/json").setBody(body))
    }

    private fun <T> ClassSessionResult<T>.data(): T = (this as ClassSessionResult.Success).data
    private fun ClassSessionResult<*>.error() = (this as ClassSessionResult.Failure).error

    @Test
    fun todayListParsesSnakeCaseAndUnstartedSlots() = runBlocking {
        respond(200, """
            {"success":true,"message":null,"data":[
              {"schedule_id":11,"session_id":501,"session_uuid":"9f1c","subject_name":"Matematika Peminatan",
               "classroom_name":"X-1 (IPA)","session_date":"2026-10-07","jam_ke":3,
               "scheduled_start":"08:30","scheduled_end":"09:15","actual_start":"2026-10-07T08:31:02+07:00",
               "actual_end":null,"status":"active","topic":"Limit","notes":null,"total_students":40,
               "hadir_count":30,"telat_count":2,"sakit_count":1,"izin_count":1,"alpha_count":6,"is_auto_closed":false},
              {"schedule_id":12,"session_id":null,"subject_name":"Fisika","classroom_name":"X-2 (IPA)",
               "session_date":"2026-10-07","jam_ke":5,"scheduled_start":"10:00:00","scheduled_end":"10:45:00",
               "status":"scheduled","total_students":38}
            ]}
        """.trimIndent())

        val list = repo.getTodaySessions().data()
        assertEquals("/api/v1/teacher/class-sessions/today", server.takeRequest(1, TimeUnit.SECONDS)!!.path)
        assertEquals(2, list.size)

        val active = list[0]
        assertEquals(501L, active.sessionId)
        assertEquals(ClassSessionStatus.ACTIVE, active.effectiveStatus)
        assertEquals("Matematika Peminatan", active.subjectName)
        assertEquals(3, active.jamKe)
        val counts = ClassSessionRules.countsOf(active)
        assertEquals(32, counts.present)
        assertEquals(40, counts.total)

        val upcoming = list[1]
        assertNull(upcoming.sessionId)
        assertEquals(12L, upcoming.scheduleId)
        assertEquals(ClassSessionStatus.SCHEDULED, upcoming.effectiveStatus)
        assertEquals("10:00 – 10:45", ClassSessionRules.timeRange(upcoming.scheduledStart, upcoming.scheduledEnd))
    }

    @Test
    fun missingRouteMeansTheBackendIsNotDeployed() = runBlocking {
        // Laravel's own 404 for an unknown route: no error_code.
        respond(404, """{"message":"The route api/v1/teacher/class-sessions/today could not be found."}""")
        val e = repo.getTodaySessions().error()
        assertEquals(ClassSessionErrorKind.NOT_DEPLOYED, e.kind)
        assertEquals(ClassSessionRules.NOT_DEPLOYED_MESSAGE, ClassSessionRules.genericMessage(e))
    }

    @Test
    fun explainedNotFoundIsNotMistakenForAMissingBackend() = runBlocking {
        respond(404, """{"success":false,"message":"Sesi tidak ditemukan.","data":{"error_code":"session_not_found"}}""")
        val e = repo.getActiveQr(99).error()
        assertEquals(ClassSessionErrorKind.NOT_FOUND, e.kind)
        assertEquals("session_not_found", e.errorCode)
        assertEquals("Sesi tidak ditemukan.", e.message)
    }

    @Test
    fun startSendsTopicToTheScheduleAndReadsWhyItWasRefused() = runBlocking {
        respond(422, """{"success":false,"message":"Di luar jam jadwal.","data":{"error_code":"outside_schedule_window"}}""")
        val e = repo.startSession(12, "  Gerak Lurus  ").error()
        val request = server.takeRequest(1, TimeUnit.SECONDS)!!
        assertEquals("POST", request.method)
        assertEquals("/api/v1/teacher/class-sessions/12/start", request.path)
        assertEquals("Gerak Lurus", JsonParser.parseString(request.body.readUtf8()).asJsonObject["topic"].asString)
        assertEquals(ClassSessionErrorKind.VALIDATION, e.kind)
        assertTrue(ClassSessionRules.startFailureMessage(e).contains("10 menit sebelum"))
    }

    @Test
    fun startingTwiceHandsBackTheExistingSession() = runBlocking {
        respond(409, """{"success":false,"message":"Sesi sudah dimulai.","data":{"error_code":"session_already_exists",
            "session":{"schedule_id":12,"session_id":777,"status":"active"}}}""")
        val e = repo.startSession(12, null).error()
        assertEquals(ClassSessionErrorKind.CONFLICT, e.kind)
        assertEquals(777L, e.existingSession?.sessionId)
        // A blank topic is not sent as an empty string.
        val body = JsonParser.parseString(server.takeRequest(1, TimeUnit.SECONDS)!!.body.readUtf8()).asJsonObject
        assertFalse(body.has("topic") && !body["topic"].isJsonNull)
    }

    @Test
    fun qrPayloadIsPassedThroughUntouched() = runBlocking {
        respond(200, """{"success":true,"data":{"qr_payload":"sista-cs:v1:9f1c:Zk3x","expires_at":"2026-10-07T08:33:30+07:00",
            "remaining_seconds":28,"rotation_seconds":30}}""")
        val qr = repo.getActiveQr(501).data()
        assertEquals("/api/v1/teacher/class-sessions/501/qr", server.takeRequest(1, TimeUnit.SECONDS)!!.path)
        assertEquals("sista-cs:v1:9f1c:Zk3x", qr.qrPayload)
        assertTrue(ClassSessionRules.isClassSessionQr(qr.qrPayload))
        assertEquals(28, qr.remainingSeconds)
        assertEquals(30, qr.rotationSeconds)
    }

    @Test
    fun studentListCarriesMethodAndOverrideAudit() = runBlocking {
        respond(200, """{"success":true,"data":[
            {"id":1,"student_id":101,"student_name":"Ahmad Fauzi","student_nis":"12345","status":"hadir",
             "check_in_method":"qr_scan","checked_in_at":"2026-10-07T08:32:00+07:00","is_override":false},
            {"id":2,"student_id":102,"student_name":"Budi Santoso","student_nis":"12346","status":"alpha",
             "check_in_method":"auto_alpha","checked_in_at":null,"is_override":true,"override_by_name":"Bu Ani",
             "override_at":"2026-10-08T07:10:00+07:00","override_reason":"Upacara pramuka"}
        ]}""")
        val rows = repo.getSessionStudents(501).data()
        assertEquals(SessionAttendanceStatus.HADIR, rows[0].effectiveStatus)
        assertEquals(SessionCheckInMethod.QR_SCAN, rows[0].checkInMethod)
        assertEquals("Scan QR 08:32", ClassSessionRules.checkInLabel(rows[0]))
        assertTrue(rows[1].isOverride)
        assertEquals("Bu Ani", rows[1].overrideByName)
    }

    @Test
    fun bulkMarkingSendsOneRowPerStudentInWireSpelling() = runBlocking {
        respond(200, """{"success":true,"data":{"updated":2,"failed":0,"errors":[]}}""")
        val result = repo.markAttendance(501, linkedMapOf(101L to SessionAttendanceStatus.HADIR, 102L to SessionAttendanceStatus.SAKIT)).data()
        assertEquals(2, result.updated)

        val request = server.takeRequest(1, TimeUnit.SECONDS)!!
        assertEquals("/api/v1/teacher/class-sessions/501/attendance/bulk", request.path)
        val rows = JsonParser.parseString(request.body.readUtf8()).asJsonObject["attendances"].asJsonArray
        assertEquals(101L, rows[0].asJsonObject["student_id"].asLong)
        assertEquals("hadir", rows[0].asJsonObject["status"].asString)
        assertEquals("sakit", rows[1].asJsonObject["status"].asString)
    }

    @Test
    fun noActiveClassIsASuccessNotAnError() = runBlocking {
        respond(200, """{"success":true,"message":"Tidak ada sesi aktif.","data":null}""")
        assertNull(repo.getActiveSessionForStudent().data())
    }

    @Test
    fun activeClassForStudent() = runBlocking {
        respond(200, """{"success":true,"data":{"session_id":501,"subject_name":"Matematika","classroom_name":"X-1 (IPA)",
            "teacher_name":"Pak Hadi","scheduled_start":"08:30","scheduled_end":"09:15","already_checked_in":false}}""")
        val active = repo.getActiveSessionForStudent().data()!!
        assertEquals("/api/v1/student/class-session/active", server.takeRequest(1, TimeUnit.SECONDS)!!.path)
        assertEquals("Pak Hadi", active.teacherName)
        assertFalse(active.alreadyCheckedIn)
    }

    @Test
    fun scanSendsThePayloadAndMapsEveryRefusal() = runBlocking {
        respond(200, """{"success":true,"data":{"status":"hadir","checked_in_at":"2026-10-07T08:03:00+07:00",
            "subject_name":"Matematika Peminatan","classroom_name":"X-1 (IPA)"}}""")
        val ok = repo.scanQr(" sista-cs:v1:9f1c:Zk3x ").data()
        val request = server.takeRequest(1, TimeUnit.SECONDS)!!
        assertEquals("/api/v1/student/class-session/scan-qr", request.path)
        assertEquals("sista-cs:v1:9f1c:Zk3x", JsonParser.parseString(request.body.readUtf8()).asJsonObject["qr_payload"].asString)
        assertEquals(SessionAttendanceStatus.HADIR, ok.status)
        assertEquals("08:03", ClassSessionRules.clockOf(ok.checkedInAt))

        respond(410, """{"success":false,"message":"QR token sudah kedaluwarsa","data":{"error_code":"qr_expired"}}""")
        assertTrue(ClassSessionRules.scanOutcome(repo.scanQr("sista-cs:x").error()) is ClassSessionRules.ScanOutcome.Retry)

        respond(403, """{"success":false,"message":"Anda tidak terdaftar di kelas ini","data":{"error_code":"not_enrolled"}}""")
        assertTrue(ClassSessionRules.scanOutcome(repo.scanQr("sista-cs:x").error()).message.contains("bukan siswa kelas ini"))

        respond(409, """{"success":false,"message":"Sudah hadir","data":{"error_code":"already_checked_in",
            "checked_in_at":"2026-10-07T08:03:00+07:00","subject_name":"Matematika Peminatan"}}""")
        val twice = repo.scanQr("sista-cs:x").error()
        assertEquals("Matematika Peminatan", twice.subjectName)
        assertEquals("Anda sudah tercatat hadir pukul 08:03 WIB.", ClassSessionRules.scanOutcome(twice).message)

        respond(409, """{"success":false,"message":"Sesi sudah berakhir","data":{"error_code":"session_closed"}}""")
        assertTrue(ClassSessionRules.scanOutcome(repo.scanQr("sista-cs:x").error()).message.contains("sudah selesai"))
    }

    @Test
    fun laravelValidationBodyStillGivesTheMessage() = runBlocking {
        respond(422, """{"message":"The reason field must be at least 10 characters.","errors":{"reason":["The reason field must be at least 10 characters."]}}""")
        val e = repo.overrideAttendance(2, SessionAttendanceStatus.HADIR, "pendek").error()
        val request = server.takeRequest(1, TimeUnit.SECONDS)!!
        assertEquals("PUT", request.method)
        assertEquals("/api/v1/admin/class-sessions/attendances/2/override", request.path)
        assertEquals(ClassSessionErrorKind.VALIDATION, e.kind)
        assertEquals("The reason field must be at least 10 characters.", e.message)
    }

    @Test
    fun overrideRefusedByTheServer() = runBlocking {
        respond(422, """{"success":false,"message":"Tidak diizinkan","data":{"error_code":"transition_not_allowed"}}""")
        val e = repo.overrideAttendance(1, SessionAttendanceStatus.HADIR, "Alasan yang cukup panjang").error()
        assertTrue(ClassSessionRules.overrideFailureMessage(e).contains("tidak bisa diubah menjadi alpha"))
    }

    @Test
    fun adminListIsALaravelPaginator() = runBlocking {
        respond(200, """{"success":true,"data":{"current_page":2,"last_page":3,"total":41,"per_page":20,"data":[
            {"schedule_id":11,"session_id":501,"teacher_name":"Pak Hadi","status":"completed","total_students":40,"hadir_count":38,"alpha_count":2}
        ]}}""")
        val page = repo.getAdminSessions("2026-10-07", ClassSessionStatus.AUTO_CLOSED, 2).data()
        val request = server.takeRequest(1, TimeUnit.SECONDS)!!
        assertEquals("/api/v1/admin/class-sessions?date=2026-10-07&status=auto_closed&page=2", request.path)
        assertEquals(2, page.currentPage)
        assertEquals(3, page.lastPage)
        assertEquals("Pak Hadi", page.items!!.single().teacherName)
    }

    @Test
    fun adminListWithoutFiltersSendsOnlyThePage() = runBlocking {
        respond(200, """{"success":true,"data":{"current_page":1,"last_page":1,"total":0,"data":[]}}""")
        repo.getAdminSessions(null, null, 1)
        assertEquals("/api/v1/admin/class-sessions?page=1", server.takeRequest(1, TimeUnit.SECONDS)!!.path)
    }

    @Test
    fun reportParses() = runBlocking {
        respond(200, """{"success":true,"data":{"summary":{"total_sessions":35,"average_presence_rate":0.93,"total_hadir":1200,
            "total_telat":30,"total_sakit":20,"total_izin":15,"total_alpha":45},
            "details":[{"label":"X-1 (IPA)","hadir":300,"telat":5,"sakit":4,"izin":3,"alpha":8,"presence_rate":0.95}]}}""")
        val report = repo.getAttendanceReport("2026-10-01", "2026-10-07", "class").data()
        assertEquals("/api/v1/admin/class-sessions/reports?start_date=2026-10-01&end_date=2026-10-07&group_by=class",
            server.takeRequest(1, TimeUnit.SECONDS)!!.path)
        assertEquals(35, report.summary!!.totalSessions)
        assertEquals(93, ClassSessionRules.presencePercent(report.summary!!.averagePresenceRate))
        assertEquals("X-1 (IPA)", report.details!!.single().label)
    }

    @Test
    fun serverErrorsAndGarbageDoNotCrash() = runBlocking {
        respond(500, "<html>Internal Server Error</html>")
        assertEquals(ClassSessionErrorKind.SERVER, repo.getTodaySessions().error().kind)

        respond(200, "not json at all")
        assertEquals(ClassSessionErrorKind.UNKNOWN, repo.getTodaySessions().error().kind)

        respond(200, """{"success":false,"message":"Jadwal belum diatur."}""")
        val refused = repo.getTodaySessions().error()
        assertEquals("Jadwal belum diatur.", refused.message)

        respond(401, """{"message":"Unauthenticated."}""")
        assertEquals(ClassSessionErrorKind.UNAUTHORIZED, repo.getTodaySessions().error().kind)
    }

    @Test
    fun noConnectionIsANetworkError() = runBlocking {
        server.shutdown()
        assertEquals(ClassSessionErrorKind.NETWORK, repo.getTodaySessions().error().kind)
    }
}
