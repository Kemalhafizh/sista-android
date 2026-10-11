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
    /**
     * Stored under a new key: the daily-attendance section older versions
     * saved as "attendance" is not read back.
     */
    val lessonAttendance: LessonAttendanceSnapshot? = null,
    val billing: BillingSnapshot? = null
)

/** One lesson from GET student/schedule. [day] is the backend's Indonesian day name ("Senin"). */
data class WidgetLesson(
    val day: String,
    val start: String,
    val end: String,
    /** Null when the subject is gone; the widget shows "–". */
    val subject: String?,
    val room: String?
)

/** The whole week's schedule; the widget filters to "today" when it renders. */
data class ScheduleSnapshot(
    val lessons: List<WidgetLesson>,
    val fetchedAt: Long
)

/**
 * One day's per-lesson QR attendance (FASE 117) from
 * GET student/class-session/attendance, or the parent's
 * GET parent/child/{uuid}/class-session-attendance.
 *
 * [childUuid] / [childName] are set for a parent account (the child last
 * opened in the parent dashboard) and null for a student.
 */
data class LessonAttendanceSnapshot(
    val childUuid: String? = null,
    val childName: String? = null,
    /** yyyy-MM-dd, the day the server answered for. */
    val date: String?,
    /** Nullable because Gson can leave it null in a stored snapshot. */
    val lessons: List<WidgetLessonAttendance>?,
    val fetchedAt: Long
)

/** One lesson of that day, as the server reported it. */
data class WidgetLessonAttendance(
    val subject: String?,
    /** HH:mm */
    val start: String?,
    /** hadir / telat / sakit / izin / alpha, or null while nothing is recorded. */
    val statusCode: String?,
    /** The server's label for [statusCode], used when the code is unknown to the app. */
    val statusLabel: String?,
    /** Class running and not scanned yet: the automatic "alpha" can still become "hadir". */
    val awaitingScan: Boolean,
    /** HH:mm (Asia/Jakarta) of the scan, when there was one. */
    val checkInTime: String?
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
