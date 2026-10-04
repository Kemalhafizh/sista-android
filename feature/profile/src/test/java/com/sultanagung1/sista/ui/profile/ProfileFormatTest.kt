package com.sultanagung1.sista.ui.profile

import com.sultanagung1.sista.data.model.MeAcademicYear
import com.sultanagung1.sista.data.model.MeChild
import com.sultanagung1.sista.data.model.MeClassroom
import com.sultanagung1.sista.data.model.MeEmployeeData
import com.sultanagung1.sista.data.model.MeProfile
import com.sultanagung1.sista.data.model.MeStudentData
import com.sultanagung1.sista.data.model.SchoolIdentity
import com.sultanagung1.sista.core.ui.text.UiText
import com.sultanagung1.sista.feature.profile.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileFormatTest {

    @Test
    fun studentRowsComeFromTheirRecordAndMissingFieldsReadDash() {
        val rows = identityRows(MeProfile(studentData = MeStudentData(nis = "12345", nisn = null, classroom = " ")))
        assertEquals(
            listOf(InfoRow(R.string.info_nis, "12345"), InfoRow(R.string.info_nisn, MISSING), InfoRow(R.string.info_class, MISSING)),
            rows,
        )
    }

    @Test
    fun employeeRowsAndThisYearsHomeroom() {
        val rows = identityRows(
            MeProfile(
                employeeData = MeEmployeeData(employeeNumber = "198001012005011001", position = "Guru Fisika"),
                homeroomClassrooms = listOf(MeClassroom(1, "XI MIPA 2"), MeClassroom(2, "XII IPS 1")),
            ),
        )
        assertEquals(
            listOf(
                InfoRow(R.string.info_nip, "198001012005011001"),
                InfoRow(R.string.info_position, "Guru Fisika"),
                InfoRow(R.string.info_homeroom, "XI MIPA 2, XII IPS 1"),
            ),
            rows,
        )
    }

    @Test
    fun anEmptyHomeroomListAddsNoRow() {
        assertTrue(identityRows(MeProfile(homeroomClassrooms = emptyList())).isEmpty())
    }

    @Test
    fun aParentSeesEachChildAndIsToldWhenNoneIsLinked() {
        val parent = MeProfile(children = listOf(MeChild("u1", "Nadia Putri", "X IPS 1"), MeChild("u2", "Raka", null)))
        assertEquals(listOf(InfoRow(R.string.info_child, "Nadia Putri · X IPS 1"), InfoRow(R.string.info_child, "Raka")), identityRows(parent))
        assertFalse(hasNoLinkedChildren(parent))
        assertTrue(hasNoLinkedChildren(MeProfile(children = emptyList())))
        // Not a parent at all: no children list, so nothing to say about it.
        assertFalse(hasNoLinkedChildren(MeProfile(children = null)))
    }

    @Test
    fun contactRowsOnlyWhenSent() {
        assertEquals(
            listOf(InfoRow(R.string.info_email, "a@sekolah.id")),
            identityRows(MeProfile(email = "a@sekolah.id", phoneNumber = "")),
        )
    }

    @Test
    fun academicYear() {
        // The school records semesters in Indonesian; the label is translated.
        assertEquals(
            UiText.Res(R.string.academic_year_semester, "2026/2027", UiText.Res(R.string.semester_odd)),
            academicYearLabel(MeAcademicYear(1, "2026/2027", "Ganjil")),
        )
        assertEquals(UiText.Res(R.string.academic_year, "2026/2027"), academicYearLabel(MeAcademicYear(1, "2026/2027", null)))
        assertEquals(UiText.Res(R.string.semester_even), semesterLabel("genap"))
        assertEquals(UiText.Raw("Pendek"), semesterLabel("Pendek"))
        assertNull(academicYearLabel(null))
        assertNull(academicYearLabel(MeAcademicYear(1, "", "Ganjil")))
    }

    @Test
    fun schoolRowsSkipWhatIsNotFilledIn() {
        val rows = schoolRows(SchoolIdentity(name = "SMA X", npsn = "20328918", accreditation = null, website = "https://sma.sch.id/"))
        assertEquals(listOf(InfoRow(R.string.info_npsn, "20328918"), InfoRow(R.string.info_website, "sma.sch.id")), rows)
        assertTrue(schoolRows(null).isEmpty())
    }

    @Test
    fun appVersion() {
        assertEquals(UiText.Res(R.string.profile_version_build, "1.4.0", "140"), appVersionLabel("1.4.0", 140))
        assertEquals(UiText.Raw("1.4.0"), appVersionLabel("1.4.0", null))
        assertEquals(UiText.Res(R.string.profile_build_only, "140"), appVersionLabel(null, 140))
        assertNull(appVersionLabel("", null))
    }
}
