package com.sultanagung1.sista.data.model

data class JournalScheduleItem(
    val id: String,
    val timeSlot: String,
    val subject: String,
    val className: String,
    val isFilled: Boolean,
    val date: String? = null,
    val topic: String? = null,
    val method: String? = null,
    val media: String? = null,
    val attendancePresent: Int = 34,
    val attendanceAbsent: Int = 1,
    val isCompetencyAchieved: Boolean = true,
    val notes: String? = null,
    val followUp: String? = null
)

data class JournalSubmitRequest(
    val scheduleId: String,
    val materiPokok: String,
    val metode: String,
    val media: String,
    val hadir: Int,
    val absen: Int,
    val isKompetensiTercapai: Boolean,
    val catatan: String,
    val tindakLanjut: String
)

data class JournalSummaryCompliance(
    val totalScheduled: Int,
    val filledCount: Int,
    val compliancePercentage: Int,
    val unfilledDaysWarning: Boolean = false
)
