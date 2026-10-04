package com.sultanagung1.sista.ui.profile

import com.sultanagung1.sista.data.model.MeAcademicYear
import com.sultanagung1.sista.data.model.MeChild
import com.sultanagung1.sista.data.model.MeClassroom
import com.sultanagung1.sista.data.model.MeEmployeeData
import com.sultanagung1.sista.data.model.MeProfile
import com.sultanagung1.sista.data.model.MeStudentData
import com.sultanagung1.sista.data.model.SchoolIdentity
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
            listOf(InfoRow("NIS", "12345"), InfoRow("NISN", MISSING), InfoRow("Kelas", MISSING)),
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
                InfoRow("NIP / No. pegawai", "198001012005011001"),
                InfoRow("Jabatan", "Guru Fisika"),
                InfoRow("Wali kelas", "XI MIPA 2, XII IPS 1"),
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
        assertEquals(listOf(InfoRow("Anak", "Nadia Putri · X IPS 1"), InfoRow("Anak", "Raka")), identityRows(parent))
        assertFalse(hasNoLinkedChildren(parent))
        assertTrue(hasNoLinkedChildren(MeProfile(children = emptyList())))
        // Not a parent at all: no children list, so nothing to say about it.
        assertFalse(hasNoLinkedChildren(MeProfile(children = null)))
    }

    @Test
    fun contactRowsOnlyWhenSent() {
        assertEquals(
            listOf(InfoRow("Email", "a@sekolah.id")),
            identityRows(MeProfile(email = "a@sekolah.id", phoneNumber = "")),
        )
    }

    @Test
    fun academicYear() {
        assertEquals("Tahun ajaran 2026/2027 · Ganjil", academicYearLabel(MeAcademicYear(1, "2026/2027", "Ganjil")))
        assertEquals("Tahun ajaran 2026/2027", academicYearLabel(MeAcademicYear(1, "2026/2027", null)))
        assertNull(academicYearLabel(null))
        assertNull(academicYearLabel(MeAcademicYear(1, "", "Ganjil")))
    }

    @Test
    fun schoolRowsSkipWhatIsNotFilledIn() {
        val rows = schoolRows(SchoolIdentity(name = "SMA X", npsn = "20328918", accreditation = null, website = "https://sma.sch.id/"))
        assertEquals(listOf(InfoRow("NPSN", "20328918"), InfoRow("Situs web", "sma.sch.id")), rows)
        assertTrue(schoolRows(null).isEmpty())
    }

    @Test
    fun appVersion() {
        assertEquals("1.4.0 (build 140)", appVersionLabel("1.4.0", 140))
        assertEquals("1.4.0", appVersionLabel("1.4.0", null))
        assertEquals("build 140", appVersionLabel(null, 140))
        assertNull(appVersionLabel("", null))
    }
}
