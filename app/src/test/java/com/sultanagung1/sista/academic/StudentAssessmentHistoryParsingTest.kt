package com.sultanagung1.sista.academic

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sultanagung1.sista.data.model.AssessmentResponse
import com.sultanagung1.sista.data.model.RemedialItem
import com.sultanagung1.sista.data.model.StudentAssessmentHistoryItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * GET assessments/student and assessments/student/remedials. The server sends
 * null for a title, subject or KKM it doesn't have instead of "PH", "Mapel"
 * or 75, and is_tuntas is null without a KKM.
 */
class StudentAssessmentHistoryParsingTest {

    private val gson = Gson()

    @Test
    fun `an assessment without title, subject or KKM parses to nulls`() {
        val json = """
            {"success":true,"data":[
              {"id":7,"assessment_title":null,"subject_name":null,"assessment_date":null,
               "kkm":null,"original_score":82.5,"final_score":82.5,"is_tuntas":null},
              {"id":8,"assessment_title":"UH 1 Dinamika","subject_name":"Fisika","assessment_date":"2026-09-21",
               "kkm":75.0,"original_score":60.0,"final_score":75.0,"is_tuntas":true}
            ]}
        """.trimIndent()
        val type = object : TypeToken<AssessmentResponse<List<StudentAssessmentHistoryItem>>>() {}.type
        val items = gson.fromJson<AssessmentResponse<List<StudentAssessmentHistoryItem>>>(json, type).data

        val unknown = items[0]
        assertNull(unknown.assessmentTitle)
        assertNull(unknown.subjectName)
        assertNull(unknown.assessmentDate)
        assertNull(unknown.kkm)
        assertNull(unknown.isTuntas)
        assertEquals(82.5, unknown.originalScore, 0.0)

        val known = items[1]
        assertEquals("UH 1 Dinamika", known.assessmentTitle)
        assertEquals(75.0, known.kkm!!, 0.0)
        assertEquals(true, known.isTuntas)
    }

    @Test
    fun `a remedial reads the flat assessment title and subject`() {
        val json = """
            {"id":3,"uuid":"0c0c0c0c-7a2d-4c3b-9f10-1a2b3c4d5e6f","daily_assessment_id":4,"student_id":12,
             "original_score":"60.00","kkm":"75.00","remedial_score":null,"remedial_type":"tugas_tambahan",
             "status":"pending","assessment":{"id":4,"title":"UH 1 Dinamika","subject":{"id":2,"name":"Fisika"}},
             "assessment_title":"UH 1 Dinamika","subject_name":"Fisika"}
        """.trimIndent()
        val item = gson.fromJson(json, RemedialItem::class.java)

        assertEquals("UH 1 Dinamika", item.assessmentTitle)
        assertEquals("Fisika", item.subjectName)
        assertEquals(60.0, item.originalScore, 0.0)
        assertEquals(75.0, item.kkm, 0.0)
    }
}
