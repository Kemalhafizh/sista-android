package com.sultanagung1.sista.ui.teacher

import com.sultanagung1.sista.data.model.StudentAttendanceInputItem
import org.junit.Assert.assertEquals
import org.junit.Test

class AttendanceMarksTest {

    @Test
    fun aNewRowStartsAsPresentAndCarriesTheCodeTheServerStores() {
        val row = StudentAttendanceInputItem(1, "Adinda")
        assertEquals(AttendanceMarks.PRESENT, row.status)
        // teacher/attendance validates status in:H,S,I,A.
        assertEquals(listOf("H", "I", "S", "A"), AttendanceMarks.ALL)
    }

    @Test
    fun tallyCountsEveryCodeIncludingTheOnesNobodyHas() {
        val rows = listOf(
            StudentAttendanceInputItem(1, "A"),
            StudentAttendanceInputItem(2, "B", status = "S"),
            StudentAttendanceInputItem(3, "C"),
        )
        assertEquals(mapOf("H" to 2, "I" to 0, "S" to 1, "A" to 0), AttendanceMarks.tally(rows))
        assertEquals(mapOf("H" to 0, "I" to 0, "S" to 0, "A" to 0), AttendanceMarks.tally(emptyList()))
    }
}
