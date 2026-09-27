package com.sultanagung1.sista.classsession

import com.google.gson.JsonParser
import com.sultanagung1.sista.data.api.ClassSessionApiService
import com.sultanagung1.sista.data.model.ClassSessionErrorKind
import com.sultanagung1.sista.data.model.ClassSessionRejection
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
 * FASE 77 ↔ backend FASE 117 as merged in sistem-terpadu, through the real
 * Retrofit/OkHttp/Gson stack against a loopback server. The bodies are what
 * ClassSessionResource, SessionAttendanceResource, ClassSessionService and the
 * RespondsForClassSessions envelope produce (sistem-terpadu
 * docs/modules/class_sessions.md). If the backend changes a key or a message,
 * the matching case here changes with it.
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

    private val activeSession = """
        {"id":45,"uuid":"9b1f0c1e-7a2d-4c3b-9f10-1a2b3c4d5e6f","schedule_id":12,
         "subject_id":5,"subject_name":"Matematika Peminatan","classroom_id":3,"classroom_name":"X-1 (IPA)",
         "teacher_id":8,"teacher_name":"Pak Hadi","session_date":"2026-10-05","jam_ke":1,
         "scheduled_start":"07:00","scheduled_end":"08:30","startable_from":"06:50",
         "auto_close_at":"2026-10-05T08:35:00+07:00","actual_start":"2026-10-05T07:05:00+07:00","actual_end":null,
         "status":"active","can_start":false,"topic":"Limit fungsi aljabar","notes":null,
         "total_students":32,"present_count":25,"absent_count":6,"is_auto_closed":false,"teaching_journal_id":null}
    """.trimIndent()

    @Test
    fun todayListParsesTheResource() = runBlocking {
        respond(200, """{"success":true,"message":"Sesi kelas hari ini berhasil diambil","data":[
            $activeSession,
            {"id":46,"uuid":"0c0c0c0c-7a2d-4c3b-9f10-1a2b3c4d5e6f","schedule_id":13,"subject_name":"Fisika",
             "classroom_name":"X-2 (IPA)","session_date":"2026-10-05","jam_ke":5,"scheduled_start":"10:00",
             "scheduled_end":"10:45","startable_from":"09:50","auto_close_at":"2026-10-05T10:50:00+07:00",
             "actual_start":null,"actual_end":null,"status":"scheduled","can_start":false,
             "total_students":38,"present_count":0,"absent_count":0,"is_auto_closed":false,"teaching_journal_id":null}
        ]}""")

        val list = repo.getTodaySessions().data()
        assertEquals("/api/v1/teacher/class-sessions/today", server.takeRequest(1, TimeUnit.SECONDS)!!.path)
        val active = list[0]
        assertEquals(45L, active.sessionId)
        assertEquals(12L, active.scheduleId)
        assertEquals(ClassSessionStatus.ACTIVE, active.effectiveStatus)
        assertEquals("Pak Hadi", active.teacherName)
        assertEquals("2026-10-05T08:35:00+07:00", active.autoCloseAt)
        val counts = ClassSessionRules.countsOf(active)
        assertEquals(25, counts.present)
        assertEquals(6, counts.alpha)
        assertEquals(1, counts.excused)

        // Not started yet, but materialised: it has an id and the server's start window.
        val upcoming = list[1]
        assertEquals(46L, upcoming.sessionId)
        assertEquals(ClassSessionStatus.SCHEDULED, upcoming.effectiveStatus)
        assertEquals("09:50", upcoming.startableFrom)
        assertEquals(ClassSessionRules.StartAction.NotYet("09:50"), ClassSessionRules.startActionFor(upcoming, 9 * 60))
    }

    @Test
    fun startRefusalsCarryTheServersWords() = runBlocking {
        respond(422, """{"success":false,"message":"Kelas baru bisa dimulai pukul 09:50.","data":null}""")
        val early = repo.startSession(13, "  Gerak Lurus  ").error()
        val request = server.takeRequest(1, TimeUnit.SECONDS)!!
        assertEquals("POST", request.method)
        assertEquals("/api/v1/teacher/class-sessions/13/start", request.path)
        assertEquals("Gerak Lurus", JsonParser.parseString(request.body.readUtf8()).asJsonObject["topic"].asString)
        assertEquals(ClassSessionRejection.OUTSIDE_SCHEDULE_WINDOW, early.rejection)
        assertEquals("Kelas baru bisa dimulai pukul 09:50.", ClassSessionRules.startFailureMessage(early))

        respond(403, """{"success":false,"message":"Anda tidak dijadwalkan mengajar pada jadwal ini.","data":null}""")
        val other = repo.startSession(13, null).error()
        assertEquals(ClassSessionRejection.NOT_YOUR_SCHEDULE, other.rejection)
        assertEquals(ClassSessionErrorKind.FORBIDDEN, other.kind)
    }

    @Test
    fun startingTwiceHandsBackTheExistingSessionId() = runBlocking {
        respond(409, """{"success":false,"message":"Sesi kelas ini sudah dimulai.","data":{"session_id":45}}""")
        val e = repo.startSession(12, null).error()
        assertEquals(ClassSessionRejection.SESSION_ALREADY_STARTED, e.rejection)
        assertEquals(45L, e.existingSessionId)
        // A blank topic is not sent as an empty string.
        val body = JsonParser.parseString(server.takeRequest(1, TimeUnit.SECONDS)!!.body.readUtf8()).asJsonObject
        assertFalse(body.has("topic") && !body["topic"].isJsonNull)
    }

    @Test
    fun startedSessionIs201() = runBlocking {
        respond(201, """{"success":true,"message":"Sesi kelas dimulai","data":$activeSession}""")
        assertEquals(45L, repo.startSession(12, "Limit").data().sessionId)
    }

    @Test
    fun endSendsNotesAndTopicAndReportsTheJournalDraft() = runBlocking {
        respond(200, """{"success":true,"message":"Sesi kelas diakhiri","data":${activeSession
            .replace("\"status\":\"active\"", "\"status\":\"completed\"")
            .replace("\"teaching_journal_id\":null", "\"teaching_journal_id\":88")}}""")
        val ended = repo.endSession(45, " Diskusi aktif ", "Limit").data()
        val body = JsonParser.parseString(server.takeRequest(1, TimeUnit.SECONDS)!!.body.readUtf8()).asJsonObject
        assertEquals("Diskusi aktif", body["notes"].asString)
        assertEquals("Limit", body["topic"].asString)
        assertEquals(ClassSessionStatus.COMPLETED, ended.effectiveStatus)
        assertEquals(88L, ended.teachingJournalId)
    }

    @Test
    fun qrTokenIsPassedThroughUntouched() = runBlocking {
        val token = "9b1f0c1e-7a2d-4c3b-9f10-1a2b3c4d5e6f.4f3c2b1a0e9d8c7b6a5f4e3d2c1b0a99"
        respond(200, """{"success":true,"data":{"qr_token":"$token","expires_at":"2026-10-05T07:10:30+07:00",
            "session_uuid":"9b1f0c1e-7a2d-4c3b-9f10-1a2b3c4d5e6f","remaining_seconds":12,"rotation_seconds":30}}""")
        val qr = repo.getActiveQr(45).data()
        assertEquals("/api/v1/teacher/class-sessions/45/qr", server.takeRequest(1, TimeUnit.SECONDS)!!.path)
        assertEquals(token, qr.qrToken)
        assertTrue("the student's scanner must accept what the teacher shows", ClassSessionRules.isClassSessionQr(qr.qrToken))
        assertEquals(12, qr.remainingSeconds)

        respond(409, """{"success":false,"message":"QR hanya tersedia saat sesi kelas aktif.","data":null}""")
        assertEquals(ClassSessionRejection.QR_NOT_ACTIVE, repo.getActiveQr(45).error().rejection)
    }

    @Test
    fun rosterCarriesMethodAndOverrideAudit() = runBlocking {
        respond(200, """{"success":true,"data":[
            {"id":9001,"class_session_id":45,"student_id":301,"student_name":"Andi","student_nis":"NIS-0012",
             "status":"telat","check_in_method":"qr_scan","checked_in_at":"2026-10-05T07:16:00+07:00","notes":null,
             "is_override":false,"override_by":null,"override_at":null,"override_reason":null},
            {"id":9002,"class_session_id":45,"student_id":302,"student_name":"Budi","student_nis":"NIS-0013",
             "status":"izin","check_in_method":"auto_alpha","checked_in_at":null,"notes":null,
             "is_override":true,"override_by":"Bu Ani","override_at":"2026-10-06T07:10:00+07:00","override_reason":"Surat izin lomba"}
        ]}""")
        val rows = repo.getSessionStudents(45).data()
        assertEquals(SessionAttendanceStatus.TELAT, rows[0].effectiveStatus)
        assertEquals(SessionCheckInMethod.QR_SCAN, rows[0].checkInMethod)
        assertEquals("Scan QR 07:16", ClassSessionRules.checkInLabel(rows[0]))
        assertEquals("Dikoreksi oleh Bu Ani pada 6 Okt 2026, 07:10", ClassSessionRules.overrideAuditLabel(rows[1]))
        val counts = ClassSessionRules.countsOf(rows)
        assertEquals(1, counts.present)
        assertEquals(1, counts.breakdown!!.izin)
    }

    @Test
    fun bulkMarkingSendsOneRowPerStudent() = runBlocking {
        respond(200, """{"success":true,"message":"2 presensi diperbarui, 1 gagal",
            "data":{"updated":2,"failed":1,"errors":["Siswa #77: Siswa tidak terdaftar di kelas sesi ini."]}}""")
        val result = repo.markAttendance(45, linkedMapOf(301L to SessionAttendanceStatus.HADIR, 302L to SessionAttendanceStatus.SAKIT)).data()
        assertEquals(1, result.failed)
        assertEquals("Siswa #77: Siswa tidak terdaftar di kelas sesi ini.", result.errors!!.single())

        val request = server.takeRequest(1, TimeUnit.SECONDS)!!
        assertEquals("/api/v1/teacher/class-sessions/45/attendance/bulk", request.path)
        val rows = JsonParser.parseString(request.body.readUtf8()).asJsonObject["attendances"].asJsonArray
        assertEquals(301L, rows[0].asJsonObject["student_id"].asLong)
        assertEquals("hadir", rows[0].asJsonObject["status"].asString)
        assertEquals("sakit", rows[1].asJsonObject["status"].asString)

        respond(409, """{"success":false,"message":"Absensi manual hanya bisa dilakukan saat sesi kelas aktif. Untuk koreksi setelah sesi berakhir, hubungi Waka Kurikulum/TU.","data":null}""")
        assertEquals(ClassSessionRejection.MANUAL_NOT_ACTIVE, repo.markAttendance(45, mapOf(301L to SessionAttendanceStatus.HADIR)).error().rejection)
    }

    @Test
    fun activeClassForStudent() = runBlocking {
        respond(200, """{"success":true,"message":"Tidak ada sesi kelas aktif","data":null}""")
        assertNull(repo.getActiveSessionForStudent().data())

        respond(200, """{"success":true,"message":"Ada sesi kelas yang sedang berlangsung","data":{"session_id":45,
            "session_uuid":"9b1f0c1e-7a2d-4c3b-9f10-1a2b3c4d5e6f","subject_name":"Matematika Peminatan",
            "classroom_name":"X-1 (IPA)","teacher_name":"Pak Hadi","scheduled_start":"07:00","scheduled_end":"08:30",
            "attendance_status":"alpha","can_scan_qr":true}}""")
        val active = repo.getActiveSessionForStudent().data()!!
        assertEquals("Pak Hadi", active.teacherName)
        assertEquals(SessionAttendanceStatus.ALPHA, active.attendanceStatus)
        assertFalse(active.alreadyCheckedIn)
    }

    @Test
    fun scanSendsTheTokenAndMapsEveryRefusal() = runBlocking {
        val token = "9b1f0c1e-7a2d-4c3b-9f10-1a2b3c4d5e6f.4f3c2b1a0e9d8c7b6a5f4e3d2c1b0a99"
        respond(200, """{"success":true,"message":"Presensi berhasil dicatat","data":{"success":true,
            "message":"Presensi berhasil dicatat","status":"hadir","session_id":45,"session_subject":"Matematika Peminatan",
            "session_class":"X-1 (IPA)","checked_in_at":"2026-10-05T07:05:00+07:00"}}""")
        val ok = repo.scanQr(" $token ").data()
        val request = server.takeRequest(1, TimeUnit.SECONDS)!!
        assertEquals("/api/v1/student/class-session/scan-qr", request.path)
        assertEquals(token, JsonParser.parseString(request.body.readUtf8()).asJsonObject["qr_token"].asString)
        assertEquals(SessionAttendanceStatus.HADIR, ok.status)
        assertEquals("Matematika Peminatan", ok.subjectName)
        assertEquals("X-1 (IPA)", ok.classroomName)
        assertEquals("07:05", ClassSessionRules.clockOf(ok.checkedInAt))

        respond(422, """{"success":false,"message":"QR token sudah kedaluwarsa","data":null}""")
        assertTrue(ClassSessionRules.scanOutcome(repo.scanQr(token).error()) is ClassSessionRules.ScanOutcome.Retry)

        respond(422, """{"success":false,"message":"QR tidak valid. Pastikan Anda memindai QR sesi kelas dari layar guru.","data":null}""")
        assertEquals(ClassSessionRejection.QR_INVALID, repo.scanQr(token).error().rejection)

        respond(403, """{"success":false,"message":"Anda tidak terdaftar di kelas ini","data":null}""")
        assertTrue(ClassSessionRules.scanOutcome(repo.scanQr(token).error()).message.contains("bukan siswa kelas ini"))

        respond(409, """{"success":false,"message":"Anda sudah tercatat hadir di sesi ini","data":{"status":"hadir","checked_in_at":"2026-10-05T07:03:00+07:00"}}""")
        assertEquals("Anda sudah tercatat hadir pukul 07:03 WIB.", ClassSessionRules.scanOutcome(repo.scanQr(token).error()).message)

        respond(409, """{"success":false,"message":"Sesi kelas sudah berakhir","data":null}""")
        assertTrue(ClassSessionRules.scanOutcome(repo.scanQr(token).error()).message.contains("sudah selesai"))

        respond(409, """{"success":false,"message":"Sesi kelas belum dimulai","data":null}""")
        assertTrue(ClassSessionRules.scanOutcome(repo.scanQr(token).error()) is ClassSessionRules.ScanOutcome.Retry)

        // throttle:20,1
        respond(429, """{"message":"Too Many Attempts."}""")
        val limited = repo.scanQr(token).error()
        assertEquals(ClassSessionErrorKind.RATE_LIMITED, limited.kind)
        assertTrue(ClassSessionRules.scanOutcome(limited) is ClassSessionRules.ScanOutcome.Retry)
    }

    @Test
    fun validationEnvelopeGivesTheFirstMessage() = runBlocking {
        respond(422, """{"success":false,"message":"The reason field must be at least 10 characters.","data":null,
            "errors":{"reason":["The reason field must be at least 10 characters."]}}""")
        val e = repo.overrideAttendance(9002, SessionAttendanceStatus.HADIR, "pendek").error()
        val request = server.takeRequest(1, TimeUnit.SECONDS)!!
        assertEquals("PUT", request.method)
        assertEquals("/api/v1/admin/class-sessions/attendances/9002/override", request.path)
        val body = JsonParser.parseString(request.body.readUtf8()).asJsonObject
        assertEquals("hadir", body["status"].asString)
        assertEquals("pendek", body["reason"].asString)
        assertEquals(ClassSessionErrorKind.VALIDATION, e.kind)
        assertEquals("The reason field must be at least 10 characters.", ClassSessionRules.overrideFailureMessage(e))
    }

    @Test
    fun overrideRefusedByTheServer() = runBlocking {
        respond(422, """{"success":false,"message":"Presensi berstatus 'hadir' tidak dapat dikoreksi. Kehadiran yang sudah tercatat tidak boleh dihapus.","data":null}""")
        val e = repo.overrideAttendance(9001, SessionAttendanceStatus.SAKIT, "Alasan yang cukup panjang").error()
        assertEquals(ClassSessionRejection.TRANSITION_NOT_ALLOWED, e.rejection)
        assertTrue(ClassSessionRules.overrideFailureMessage(e).contains("tidak dapat dikoreksi"))
    }

    @Test
    fun adminListUsesTheItemsPaginator() = runBlocking {
        respond(200, """{"success":true,"data":{"items":[$activeSession],"current_page":2,"last_page":3,"per_page":20,"total":57}}""")
        val page = repo.getAdminSessions("2026-10-05", ClassSessionStatus.AUTO_CLOSED, 2).data()
        val request = server.takeRequest(1, TimeUnit.SECONDS)!!
        assertEquals("/api/v1/admin/class-sessions?date=2026-10-05&status=auto_closed&page=2", request.path)
        assertEquals(2, page.currentPage)
        assertEquals(3, page.lastPage)
        assertEquals(57, page.total)
        assertEquals("Pak Hadi", page.items!!.single().teacherName)
        // The correction screen gets only an id; the list is remembered for its header.
        assertEquals("Matematika Peminatan", repo.cachedAdminSession(45)?.subjectName)
    }

    @Test
    fun adminListWithoutFiltersSendsOnlyThePage() = runBlocking {
        respond(200, """{"success":true,"data":{"items":[],"current_page":1,"last_page":1,"per_page":20,"total":0}}""")
        repo.getAdminSessions(null, null, 1)
        assertEquals("/api/v1/admin/class-sessions?page=1", server.takeRequest(1, TimeUnit.SECONDS)!!.path)
    }

    @Test
    fun reportParses() = runBlocking {
        respond(200, """{"success":true,"data":{"summary":{"total_sessions":12,"total_records":384,"average_presence_rate":0.9115,
            "total_hadir":330,"total_telat":20,"total_alpha":20,"total_sakit":8,"total_izin":6},
            "details":[{"group_id":3,"student_nis":null,"student_name":null,"classroom_name":"X-1 (IPA)","subject_name":null,
             "hadir":110,"telat":4,"alpha":6,"sakit":2,"izin":2,"total":124,"presence_rate":0.9194}]}}""")
        val report = repo.getAttendanceReport("2026-10-01", "2026-10-07", "class").data()
        assertEquals("/api/v1/admin/class-sessions/reports?start_date=2026-10-01&end_date=2026-10-07&group_by=class",
            server.takeRequest(1, TimeUnit.SECONDS)!!.path)
        assertEquals(12, report.summary!!.totalSessions)
        assertEquals(91, ClassSessionRules.presencePercent(report.summary!!.averagePresenceRate))
        assertEquals("X-1 (IPA)", ClassSessionRules.reportLabel(report.details!!.single()))
    }

    @Test
    fun missingRouteIsNotDeployedButAnEnvelopedNotFoundIsNot() = runBlocking {
        // Laravel's own 404 for an unknown route: no envelope.
        respond(404, """{"message":"The route api/v1/teacher/class-sessions/today could not be found."}""")
        val missing = repo.getTodaySessions().error()
        assertEquals(ClassSessionErrorKind.NOT_DEPLOYED, missing.kind)
        assertEquals(ClassSessionRules.NOT_DEPLOYED_MESSAGE, ClassSessionRules.genericMessage(missing))

        respond(404, """{"success":false,"message":"Sesi kelas tidak ditemukan.","data":null}""")
        val gone = repo.getSessionAttendances(99).error()
        assertEquals(ClassSessionErrorKind.NOT_FOUND, gone.kind)
        assertEquals("Sesi kelas tidak ditemukan.", ClassSessionRules.genericMessage(gone))
    }

    @Test
    fun serverErrorsAndGarbageDoNotCrash() = runBlocking {
        respond(500, "<html>Internal Server Error</html>")
        assertEquals(ClassSessionErrorKind.SERVER, repo.getTodaySessions().error().kind)

        respond(200, "not json at all")
        assertEquals(ClassSessionErrorKind.UNKNOWN, repo.getTodaySessions().error().kind)

        respond(200, """{"success":false,"message":"Jadwal belum diatur."}""")
        assertEquals("Jadwal belum diatur.", repo.getTodaySessions().error().message)

        respond(401, """{"message":"Unauthenticated."}""")
        assertEquals(ClassSessionErrorKind.UNAUTHORIZED, repo.getTodaySessions().error().kind)
    }

    @Test
    fun noConnectionIsANetworkError() = runBlocking {
        server.shutdown()
        assertEquals(ClassSessionErrorKind.NETWORK, repo.getTodaySessions().error().kind)
    }
}
