package com.sultanagung1.sista.ui.profile

import androidx.annotation.StringRes
import com.sultanagung1.sista.core.accessibility.AppThemeMode
import com.sultanagung1.sista.core.ui.text.UiText
import com.sultanagung1.sista.feature.profile.R
import com.sultanagung1.sista.data.model.MeAcademicYear
import com.sultanagung1.sista.data.model.MeProfile
import com.sultanagung1.sista.data.model.SchoolIdentity

/** One labelled fact on the identity card or the "Tentang" page. The value is data, shown as sent. */
data class InfoRow(@StringRes val label: Int, val value: String)

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
        add(InfoRow(R.string.info_nis, shown(student.nis)))
        add(InfoRow(R.string.info_nisn, shown(student.nisn)))
        add(InfoRow(R.string.info_class, shown(student.classroom)))
    }
    profile.employeeData?.let { employee ->
        add(InfoRow(R.string.info_nip, shown(employee.employeeNumber)))
        add(InfoRow(R.string.info_position, shown(employee.position)))
    }
    profile.homeroomClassrooms?.mapNotNull { it.name?.takeIf(String::isNotBlank) }?.takeIf { it.isNotEmpty() }?.let {
        add(InfoRow(R.string.info_homeroom, it.joinToString(", ")))
    }
    profile.children?.forEach { child ->
        add(InfoRow(R.string.info_child, listOfNotNull(shown(child.name), child.classroom?.takeIf(String::isNotBlank)).joinToString(" · ")))
    }
    profile.email?.takeIf { it.isNotBlank() }?.let { add(InfoRow(R.string.info_email, it)) }
    profile.phoneNumber?.takeIf { it.isNotBlank() }?.let { add(InfoRow(R.string.info_phone, it)) }
}

/** A parent account the school hasn't linked to any child yet. */
fun hasNoLinkedChildren(profile: MeProfile): Boolean = profile.children?.isEmpty() == true

/** The school records the semester in Indonesian ("Ganjil"/"Genap"); other values are shown as sent. */
fun semesterLabel(semester: String): UiText = when (semester.trim().lowercase()) {
    "ganjil" -> UiText.Res(R.string.semester_odd)
    "genap" -> UiText.Res(R.string.semester_even)
    else -> UiText.Raw(semester)
}

/** "Tahun ajaran 2026/2027 · Ganjil", or null when no year is active. */
fun academicYearLabel(year: MeAcademicYear?): UiText? {
    val name = year?.year?.takeIf { it.isNotBlank() } ?: return null
    val semester = year.semester?.takeIf { it.isNotBlank() } ?: return UiText.Res(R.string.academic_year, name)
    return UiText.Res(R.string.academic_year_semester, name, semesterLabel(semester))
}

/** The school's facts for the "Tentang" page, skipping the ones not filled in. */
fun schoolRows(school: SchoolIdentity?): List<InfoRow> = listOfNotNull(
    school?.npsn?.let { InfoRow(R.string.info_npsn, it) },
    school?.accreditation?.let { InfoRow(R.string.info_accreditation, it) },
    school?.foundation?.let { InfoRow(R.string.info_foundation, it) },
    school?.address?.let { InfoRow(R.string.info_address, it) },
    school?.phone?.let { InfoRow(R.string.info_school_phone, it) },
    school?.email?.let { InfoRow(R.string.info_email, it) },
    school?.website?.let { InfoRow(R.string.info_website, it.removePrefix("https://").removePrefix("http://").trimEnd('/')) },
).filter { it.value.isNotBlank() }

/** "1.4.0 (build 140)" from the installed package; null when it can't be read. */
fun appVersionLabel(versionName: String?, versionCode: Long?): UiText? = when {
    versionName.isNullOrBlank() && versionCode == null -> null
    versionName.isNullOrBlank() -> UiText.Res(R.string.profile_build_only, versionCode.toString())
    versionCode == null -> UiText.Raw(versionName)
    else -> UiText.Res(R.string.profile_version_build, versionName, versionCode.toString())
}

/** Themes offered on the profile page. High contrast lives under Aksesibilitas. */
val PROFILE_THEMES = listOf(AppThemeMode.SYSTEM, AppThemeMode.LIGHT, AppThemeMode.DARK, AppThemeMode.AMOLED_BLACK)

@StringRes
fun themeLabel(mode: AppThemeMode): Int = when (mode) {
    AppThemeMode.SYSTEM -> R.string.theme_system
    AppThemeMode.LIGHT -> R.string.theme_light
    AppThemeMode.DARK -> R.string.theme_dark
    AppThemeMode.AMOLED_BLACK -> R.string.theme_amoled
    AppThemeMode.HIGH_CONTRAST -> R.string.theme_high_contrast
}
