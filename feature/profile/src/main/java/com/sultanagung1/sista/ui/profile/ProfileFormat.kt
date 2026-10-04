package com.sultanagung1.sista.ui.profile

import com.sultanagung1.sista.core.accessibility.AppThemeMode
import com.sultanagung1.sista.data.model.MeAcademicYear
import com.sultanagung1.sista.data.model.MeProfile
import com.sultanagung1.sista.data.model.SchoolIdentity

/** One labelled fact on the identity card or the "Tentang" page. */
data class InfoRow(val label: String, val value: String)

/** What the server didn't send. */
const val MISSING = "–"

private fun shown(value: String?): String = value?.trim()?.takeIf { it.isNotEmpty() } ?: MISSING

/**
 * The identity card's rows, decided by which records the server sent rather
 * than by the role string: a student record gives NIS/NISN/class, an employee
 * record gives the NIP and position, a parent gets their children. A field the
 * school hasn't filled in reads "–", never a sample number.
 */
fun identityRows(profile: MeProfile): List<InfoRow> = buildList {
    profile.studentData?.let { student ->
        add(InfoRow("NIS", shown(student.nis)))
        add(InfoRow("NISN", shown(student.nisn)))
        add(InfoRow("Kelas", shown(student.classroom)))
    }
    profile.employeeData?.let { employee ->
        add(InfoRow("NIP / No. pegawai", shown(employee.employeeNumber)))
        add(InfoRow("Jabatan", shown(employee.position)))
    }
    profile.homeroomClassrooms?.mapNotNull { it.name?.takeIf(String::isNotBlank) }?.takeIf { it.isNotEmpty() }?.let {
        add(InfoRow("Wali kelas", it.joinToString(", ")))
    }
    profile.children?.forEach { child ->
        add(InfoRow("Anak", listOfNotNull(shown(child.name), child.classroom?.takeIf(String::isNotBlank)).joinToString(" · ")))
    }
    profile.email?.takeIf { it.isNotBlank() }?.let { add(InfoRow("Email", it)) }
    profile.phoneNumber?.takeIf { it.isNotBlank() }?.let { add(InfoRow("Nomor HP", it)) }
}

/** A parent account the school hasn't linked to any child yet. */
fun hasNoLinkedChildren(profile: MeProfile): Boolean = profile.children?.isEmpty() == true

/** "Tahun ajaran 2026/2027 · Ganjil", or null when no year is active. */
fun academicYearLabel(year: MeAcademicYear?): String? {
    val name = year?.year?.takeIf { it.isNotBlank() } ?: return null
    return listOfNotNull("Tahun ajaran $name", year.semester?.takeIf { it.isNotBlank() }).joinToString(" · ")
}

/** The school's facts for the "Tentang" page, skipping the ones not filled in. */
fun schoolRows(school: SchoolIdentity?): List<InfoRow> = listOfNotNull(
    school?.npsn?.let { InfoRow("NPSN", it) },
    school?.accreditation?.let { InfoRow("Akreditasi", it) },
    school?.foundation?.let { InfoRow("Yayasan", it) },
    school?.address?.let { InfoRow("Alamat", it) },
    school?.phone?.let { InfoRow("Telepon", it) },
    school?.email?.let { InfoRow("Email", it) },
    school?.website?.let { InfoRow("Situs web", it.removePrefix("https://").removePrefix("http://").trimEnd('/')) },
).filter { it.value.isNotBlank() }

/** "1.4.0 (build 140)" from the installed package; null when it can't be read. */
fun appVersionLabel(versionName: String?, versionCode: Long?): String? = when {
    versionName.isNullOrBlank() && versionCode == null -> null
    versionName.isNullOrBlank() -> "build $versionCode"
    versionCode == null -> versionName
    else -> "$versionName (build $versionCode)"
}

/** Themes offered on the profile page. High contrast lives under Aksesibilitas. */
val PROFILE_THEMES = listOf(AppThemeMode.SYSTEM, AppThemeMode.LIGHT, AppThemeMode.DARK, AppThemeMode.AMOLED_BLACK)

fun themeLabel(mode: AppThemeMode): String = when (mode) {
    AppThemeMode.SYSTEM -> "Ikuti sistem"
    AppThemeMode.LIGHT -> "Terang"
    AppThemeMode.DARK -> "Gelap"
    AppThemeMode.AMOLED_BLACK -> "Hitam pekat"
    AppThemeMode.HIGH_CONTRAST -> "Kontras tinggi"
}
