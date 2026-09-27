package com.sultanagung1.sista.core.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Shared "what time is it right now" source for the whole app — every date/day
 * lookup goes through here instead of a raw `Calendar.getInstance()` at the call
 * site, so it automatically benefits from [applyServerOffset] without touching
 * every caller.
 *
 * Why this exists: a student can freely change their phone's clock/date/timezone.
 * If schedule status, attendance-window checks, journal dates, or countdowns
 * trusted the device clock directly, a student could set their phone back to
 * "before class starts" to fake being on time, or forward to see a future day's
 * exam token early. [com.sultanagung1.sista.core.time.ServerTimeProvider]
 * periodically calls [applyServerOffset] with the delta between the backend's
 * real clock (`GET mobile/config`'s `server_time`, an existing real field the
 * app already fetched but never used) and this device's clock; every
 * `DateUtils.now*()` call below is then corrected by that delta.
 *
 * Until the first successful sync, [nowMillis] simply returns the device clock
 * unmodified (offset 0) — the app must still work correctly offline / before
 * the first server round-trip, just without the anti-tamper guarantee yet.
 */
object DateUtils {
    private val INDONESIAN_DAYS = arrayOf("Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")
    private val ISO_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale("id", "ID"))

    @Volatile
    private var clockOffsetMillis: Long = 0L

    @Volatile
    private var serverSynced: Boolean = false

    @Volatile
    private var lastSyncElapsedRealtimeMillis: Long = 0L

    /** Called by [com.sultanagung1.sista.core.time.ServerTimeProvider] after a successful time sync. */
    fun applyServerOffset(offsetMillis: Long, syncedAtElapsedRealtimeMillis: Long) {
        clockOffsetMillis = offsetMillis
        serverSynced = true
        lastSyncElapsedRealtimeMillis = syncedAtElapsedRealtimeMillis
    }

    /** True once at least one server time sync has succeeded this process lifetime. */
    fun isServerTimeSynced(): Boolean = serverSynced

    /** Milliseconds since the last successful sync, via the boot-clock (immune to wall-clock edits) — for staleness checks. */
    fun millisSinceLastSync(): Long =
        if (!serverSynced) Long.MAX_VALUE else android.os.SystemClock.elapsedRealtime() - lastSyncElapsedRealtimeMillis

    /** Server-corrected "now", in epoch millis. Falls back to the raw device clock (offset 0) before the first sync. */
    fun nowMillis(): Long = System.currentTimeMillis() + clockOffsetMillis

    fun nowCalendar(): Calendar = Calendar.getInstance().apply { timeInMillis = nowMillis() }

    /** e.g. "Senin" — matches the `day` column stored on the backend's Schedule rows. */
    fun todayDayNameIndonesian(): String = INDONESIAN_DAYS[nowCalendar().get(Calendar.DAY_OF_WEEK) - 1]

    fun todayIso(): String = ISO_DATE_FORMAT.format(nowCalendar().time)

    fun daysAgoIso(days: Int): String {
        val cal = nowCalendar()
        cal.add(Calendar.DAY_OF_YEAR, -days)
        return ISO_DATE_FORMAT.format(cal.time)
    }

    /** Minutes since local midnight, server-corrected — the common unit for comparing against `HH:mm` schedule times. */
    fun nowMinutesOfDay(): Int {
        val cal = nowCalendar()
        return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
    }

    /** Parses an "HH:mm" or "HH:mm:ss" string (as the backend's `session_start`/`session_end` come formatted) into minutes-of-day, or null if unparseable. */
    fun parseMinutesOfDay(time: String?): Int? {
        if (time == null) return null
        val match = Regex("""(\d{1,2}):(\d{2})""").find(time) ?: return null
        val (h, m) = match.destructured
        return h.toInt() * 60 + m.toInt()
    }
}
