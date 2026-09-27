package com.sultanagung1.sista.core.widget

/**
 * What the home-screen widgets (FASE 14) are allowed to show.
 *
 * Every field here was copied from a real API response the app already
 * fetched for the logged-in account; a section stays null until that fetch
 * happens. The widgets render "Belum ada data" for a null section instead of
 * the sample texts the layouts used to ship ("✅ HADIR", "LUNAS", three
 * invented lessons).
 *
 * The snapshot belongs to one account ([ownerUserId] + [role]). It is cleared
 * on login/logout, and [WidgetSnapshotStore] discards it when a write comes
 * from a different account.
 */
data class WidgetSnapshot(
    val ownerUserId: String? = null,
    val role: String? = null,
    val schedule: ScheduleSnapshot? = null,
    val attendance: AttendanceSnapshot? = null,
    val billing: BillingSnapshot? = null
)

/** One lesson from GET student/schedule. [day] is the backend's Indonesian day name ("Senin"). */
data class WidgetLesson(
    val day: String,
    val start: String,
    val end: String,
    val subject: String,
    val room: String?
)

/** The whole week's schedule; the widget filters to "today" when it renders. */
data class ScheduleSnapshot(
    val lessons: List<WidgetLesson>,
    val fetchedAt: Long
)

/**
 * The latest attendance record the app has seen.
 *
 * [date] is null when the account has no attendance rows at all. [childUuid] /
 * [childName] are set for a parent account (the child last opened in the
 * parent dashboard) and null for a student.
 */
data class AttendanceSnapshot(
    val childUuid: String? = null,
    val childName: String? = null,
    /** yyyy-MM-dd */
    val date: String?,
    /**
     * attendances.status as stored: H / S / I / A from the teacher's class
     * attendance, the word "hadir" from a GPS check-in.
     */
    val statusCode: String?,
    /** The backend's own label for [statusCode], used when the code is unknown to the app. */
    val statusLabel: String?,
    /** HH:mm when the API sends a check-in time (student/attendance `check_in`). */
    val checkInTime: String? = null,
    val fetchedAt: Long
)

/**
 * Billing summary.
 *
 * A student gets it from the full invoice list (count + remaining total). A
 * parent only gets `unpaid_billings_count` from the child summary, so
 * [unpaidTotal] and [invoiceCount] are null there: unknown, not zero.
 */
data class BillingSnapshot(
    val childUuid: String? = null,
    val childName: String? = null,
    val unpaidCount: Int,
    val unpaidTotal: Double?,
    val invoiceCount: Int?,
    val fetchedAt: Long
)
