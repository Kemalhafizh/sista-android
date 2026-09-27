package com.sultanagung1.sista.ui.teacher

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sultanagung1.sista.data.model.CbtApiEnvelope
import com.sultanagung1.sista.data.model.CbtImageAttachment
import com.sultanagung1.sista.data.model.TeacherCreatedExam
import com.sultanagung1.sista.data.model.TeacherScheduleSlot
import com.sultanagung1.sista.data.repository.CbtRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Teacher manual exam authoring: the form must only ever send what
 * ApiTeacherController::storeExamWithQuestions accepts, and must never
 * again fake a result (client-made token, sample questions, hardcoded
 * exam id 101, "berhasil disinkronisasi" without a request).
 */
class TeacherCreateExamTest {

    private val vm = TeacherCreateExamViewModel.Companion

    private val choice = ExamClassChoice(subjectId = 7, subjectName = "Fisika", classroomId = 12, classroomName = "XI IPA 2")

    private fun completeQuestion(text: String = "Berapa 2 + 2?") = DraftQuestion(
        text = text,
        options = listOf(DraftOption("A", "3"), DraftOption("B", "4"), DraftOption("C", "5")),
        correctKey = "B"
    )

    private fun completeState() = TeacherCreateExamUiState(
        isLoadingChoices = false,
        classChoices = listOf(choice),
        selectedChoice = choice,
        title = "Ulangan Harian Bab 3",
        durationMinutes = "60",
        passingScore = "75",
        questions = listOf(completeQuestion())
    )

    // --- validate ---------------------------------------------------------------

    @Test
    fun completeFormHasNoIssues() {
        assertEquals(emptyList<ValidationIssue>(), vm.validate(completeState()))
    }

    @Test
    fun blankFormReportsEveryMissingPieceInsteadOfPublishingSampleData() {
        val messages = vm.validate(TeacherCreateExamUiState()).map { it.message }

        assertTrue(messages.contains("Nama ujian wajib diisi."))
        assertTrue(messages.contains("Pilih mata pelajaran & kelas dari jadwal mengajar Anda."))
        assertTrue(messages.any { it.startsWith("Durasi ujian harus") })
        assertTrue(messages.contains("KKTP harus berupa angka 0–100."))
        assertTrue(messages.contains("Soal #1: teks pertanyaan masih kosong."))
        // No answer key is pre-marked: the old screen defaulted to "A".
        assertTrue(messages.contains("Soal #1: tandai kunci jawaban yang benar."))
        listOf("A", "B", "C", "D", "E").forEach { key ->
            assertTrue(messages.contains("Soal #1: pilihan $key belum diisi (teks atau gambar)."))
        }
    }

    @Test
    fun durationAndPassingScoreFollowServerBounds() {
        fun issuesFor(duration: String, passing: String) =
            vm.validate(completeState().copy(durationMinutes = duration, passingScore = passing)).map { it.message }

        assertTrue(issuesFor("4", "75").any { it.startsWith("Durasi") })
        assertTrue(issuesFor("301", "75").any { it.startsWith("Durasi") })
        assertTrue(issuesFor("5", "75").isEmpty())
        assertTrue(issuesFor("300", "75").isEmpty())
        assertTrue(issuesFor("60", "100.5").any { it.startsWith("KKTP") })
        assertTrue(issuesFor("60", "0").isEmpty())
        assertTrue(issuesFor("60", "72.5").isEmpty())
    }

    @Test
    fun optionWithOnlyAnImageCountsAsFilled() {
        val q = completeQuestion().let { q ->
            q.copy(options = q.options.map {
                if (it.key == "C") it.copy(text = "", image = DraftImage("content://img/1", remoteUrl = "https://sekolah.test/storage/cbt/1.png")) else it
            })
        }
        assertEquals(emptyList<ValidationIssue>(), vm.validate(completeState().copy(questions = listOf(q))))
    }

    @Test
    fun imagesMustBeUploadedBeforePublishing() {
        val uploading = completeQuestion().copy(image = DraftImage("content://img/1", isUploading = true))
        val failed = completeQuestion().copy(image = DraftImage("content://img/2", error = "Gagal"))
        val issues = vm.validate(completeState().copy(questions = listOf(uploading, failed)))

        assertEquals(ValidationIssue("Soal #1: gambar masih diunggah, tunggu sebentar.", 0), issues[0])
        assertEquals(
            ValidationIssue("Soal #2: ada gambar yang gagal diunggah — coba lagi atau hapus gambarnya.", 1),
            issues[1]
        )
        assertEquals(setOf(0, 1), completeState().copy(questions = listOf(uploading, failed), showValidation = true).questionsWithIssues)
    }

    @Test
    fun issuesAreOnlyShownAfterTheFirstPublishAttempt() {
        val blank = TeacherCreateExamUiState()
        assertTrue(blank.validationIssues.isEmpty())
        assertTrue(blank.copy(showValidation = true).validationIssues.isNotEmpty())
    }

    // --- buildRequest / wire format ------------------------------------------------

    @Test
    fun appOnlyExamKeepsTheTeachersShuffleChoiceBecauseTheStudentApiHonorsIt() {
        val request = vm.buildRequest(completeState().copy(mobileOnly = true, shuffleQuestions = true, shuffleOptions = false))
        assertTrue(request.shuffleQuestions)
        assertFalse(request.shuffleOptions)
        assertTrue(request.mobileOnly)

        val both = vm.buildRequest(completeState().copy(mobileOnly = true, shuffleQuestions = false, shuffleOptions = true))
        assertFalse(both.shuffleQuestions)
        assertTrue(both.shuffleOptions)
    }

    @Test
    fun webExamKeepsTheTeachersShuffleChoice() {
        val request = vm.buildRequest(completeState().copy(mobileOnly = false, shuffleQuestions = true, shuffleOptions = false))
        assertTrue(request.shuffleQuestions)
        assertFalse(request.shuffleOptions)
    }

    @Test
    fun requestCarriesRealIdsTrimmedTextAndOnlyServerImageUrls() {
        val q = completeQuestion(text = "  Hitung gaya  ").copy(
            image = DraftImage("content://media/42", remoteUrl = "https://sekolah.test/storage/cbt/q.png")
        )
        val request = vm.buildRequest(completeState().copy(title = "  UH Dinamika  ", maxViolations = 5, questions = listOf(q)))

        assertEquals("UH Dinamika", request.title)
        assertEquals(7L, request.subjectId)
        assertEquals(12L, request.classroomId)
        assertEquals(60, request.durationMinutes)
        assertEquals(75.0, request.passingScore, 0.0)
        assertEquals(5, request.maxViolations)
        val sent = request.questions.single()
        assertEquals("Hitung gaya", sent.questionText)
        assertEquals("https://sekolah.test/storage/cbt/q.png", sent.imageUrl)
        assertEquals("B", sent.correctAnswer)
        assertEquals(listOf("A", "B", "C"), sent.options.map { it.key })
    }

    @Test
    fun serializedPayloadUsesExactlyTheFieldNamesTheServerValidates() {
        val json = Gson().toJsonTree(vm.buildRequest(completeState())).asJsonObject

        assertEquals(
            setOf(
                "title", "subject_id", "classroom_id", "duration_minutes", "passing_score",
                "max_violations", "mobile_only", "shuffle_questions", "shuffle_options", "questions"
            ),
            json.keySet()
        )
        // Fields of the old fake payload the server never accepted.
        listOf("token", "subject_name", "target_class").forEach { assertFalse(json.has(it)) }

        val question = json.getAsJsonArray("questions")[0].asJsonObject
        assertTrue(question.has("question_text"))
        assertTrue(question.has("correct_answer"))
        val option = question.getAsJsonArray("options")[0].asJsonObject
        assertEquals("A", option.get("key").asString)
        assertEquals("3", option.get("text").asString)
    }

    @Test
    fun createdResponseParsesFromTheServers201Shape() {
        val body = """
            {"success":true,"message":"Ujian berhasil dibuat.","data":{
              "exam_id":88,"uuid":"9f1c","title":"UH Bab 3","type":"ulangan_harian","status":"published",
              "subject":"Fisika","classroom":"XI IPA 2","start_time":"2026-09-26T08:00:00+07:00",
              "end_time":"2026-10-03T08:00:00+07:00","duration_minutes":60,"total_questions":3,
              "created_by":4,"operator_id":null,"can_view_token":true}}
        """.trimIndent()
        val type = object : TypeToken<CbtApiEnvelope<TeacherCreatedExam>>() {}.type
        val envelope: CbtApiEnvelope<TeacherCreatedExam> = Gson().fromJson(body, type)

        val exam = requireNotNull(envelope.data)
        assertEquals(88L, exam.examId)
        assertEquals(3, exam.totalQuestions)
        assertEquals("2026-10-03T08:00:00+07:00", exam.endTime)
        assertTrue(exam.canViewToken)
    }

    // --- class choices / images -------------------------------------------------------

    @Test
    fun classChoicesAreDistinctRealPairsFromTheSchedule() {
        fun slot(id: Long, subjectId: Long, subject: String?, classroomId: Long, classroom: String?) =
            TeacherScheduleSlot(id, "Senin", "07:00", "08:30", subjectId, subject, classroomId, classroom)

        val choices = vm.classChoicesFrom(
            listOf(
                slot(1, 7, "Fisika", 12, "XI IPA 2"),
                slot(2, 7, "Fisika", 12, "XI IPA 2"), // same pair on another day
                slot(3, 3, "Biologi", 12, "XI IPA 2"),
                slot(4, 9, null, 14, " "),
                slot(5, 0, "Tanpa mapel", 12, "XI IPA 2")
            )
        )

        assertEquals(listOf("Biologi • XI IPA 2", "Fisika • XI IPA 2", "Mapel #9 • Kelas #14"), choices.map { it.label })
        assertTrue(vm.classChoicesFrom(emptyList()).isEmpty())
    }

    @Test
    fun imagesAreCheckedAgainstTheServerUploadRules() {
        fun attachment(size: Int, mime: String) = CbtImageAttachment(ByteArray(size), mime, "x")

        assertNull(vm.imageRejectionReason(attachment(1024, "image/png")))
        assertNull(vm.imageRejectionReason(attachment(1024, "image/JPEG")))
        assertNotNull(vm.imageRejectionReason(attachment(1024, "image/gif")))
        assertNotNull(vm.imageRejectionReason(attachment(1024, "")))
        assertNotNull(vm.imageRejectionReason(attachment(0, "image/png")))
        assertNull(vm.imageRejectionReason(attachment((10 * 1024 * 1024), "image/webp")))
        assertEquals(
            "Ukuran gambar melebihi 10 MB.",
            vm.imageRejectionReason(attachment(10 * 1024 * 1024 + 1, "image/webp"))
        )
    }

    // --- server error text --------------------------------------------------------------

    @Test
    fun laravel422ShowsEveryDistinctFieldErrorNotJustTheFirst() {
        val body = """
            {"message":"Kolom judul wajib diisi. (and 2 more errors)",
             "errors":{"title":["Kolom judul wajib diisi."],
                       "questions.0.correct_answer":["Kunci jawaban soal #1 harus salah satu pilihan."],
                       "questions.1.correct_answer":["Kunci jawaban soal #1 harus salah satu pilihan."]}}
        """.trimIndent()
        assertEquals(
            "Kolom judul wajib diisi.\nKunci jawaban soal #1 harus salah satu pilihan.",
            CbtRepository.describeServerError(422, body, null, "Gagal menerbitkan ujian")
        )
    }

    @Test
    fun otherServerErrorsGetAReadableMessage() {
        assertEquals(
            "Akun ini tidak berwenang melakukan tindakan ini.",
            CbtRepository.describeServerError(403, """{"message":"This action is unauthorized."}""", null, "Gagal")
        )
        assertEquals(
            "Tidak ada tahun ajaran aktif.",
            CbtRepository.describeServerError(422, """{"success":false,"message":"Tidak ada tahun ajaran aktif."}""", null, "Gagal")
        )
        assertEquals("Gagal menerbitkan ujian (kode 500).", CbtRepository.describeServerError(500, "<html>", null, "Gagal menerbitkan ujian"))
    }

    // --- source guard ---------------------------------------------------------------------

    private fun source(relativePath: String): File {
        val candidates = listOf(File("../$relativePath"), File(relativePath))
        return candidates.firstOrNull { it.exists() } ?: candidates.first()
    }

    @Test
    fun createScreenNoLongerFakesTheServer() {
        val screen = source("feature/teacher/src/main/java/com/sultanagung1/sista/ui/teacher/TeacherCreateExamScreen.kt").readText()

        listOf("SA1-", "101L", "unsplash", "disinkronisasi", ".random()", "Kalkulus").forEach {
            assertFalse("TeacherCreateExamScreen must not contain \"$it\"", screen.contains(it, ignoreCase = true))
        }
        assertTrue("Publishing must go through the ViewModel", screen.contains("viewModel::publish"))
        assertTrue("Success dialog must be driven by the server response", screen.contains("uiState.createdExam"))
        assertTrue("Proctor room only when the server allows viewing the token", screen.contains("exam.canViewToken"))
    }

    @Test
    fun proctorRouteReplacesTheFormInsteadOfStackingOnIt() {
        val navGraph = source("app/src/main/java/com/sultanagung1/sista/ui/navigation/graphs/TeacherNavGraph.kt").readText()
        assertTrue(navGraph.contains("popUpTo(Screen.TeacherCreateExam.route) { inclusive = true }"))
    }
}
