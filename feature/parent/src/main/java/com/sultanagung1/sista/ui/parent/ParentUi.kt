package com.sultanagung1.sista.ui.parent

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Mosque
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import com.sultanagung1.sista.core.ui.component.IconBadge
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.data.model.ChildActivityEvent
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/** Icon and tone for a `parent/child/{uuid}/feed` event type. */
internal fun activityStyle(type: String, title: String): Pair<ImageVector, StatusTone> = when (type) {
    "attendance" -> Icons.Outlined.EventAvailable to if (title.endsWith("Hadir")) StatusTone.Success else StatusTone.Warning
    "academic" -> Icons.Outlined.Assignment to StatusTone.Info
    "discipline" -> Icons.Outlined.Gavel to if (title.contains("prestasi")) StatusTone.Success else StatusTone.Danger
    "library" -> Icons.AutoMirrored.Outlined.MenuBook to StatusTone.Brand
    "ibadah" -> Icons.Outlined.Mosque to StatusTone.Brand
    else -> Icons.Outlined.Notifications to StatusTone.Neutral
}

/** One school event: what happened, the detail, and when. */
@Composable
internal fun ActivityRow(event: ChildActivityEvent, nowMillis: Long, modifier: Modifier = Modifier, showDay: Boolean = true) {
    val (icon, tone) = activityStyle(event.type, event.title)
    Row(modifier.padding(vertical = Spacing.sm), verticalAlignment = Alignment.Top) {
        IconBadge(icon, tone = tone)
        Spacer(Modifier.width(Spacing.md))
        Column(Modifier.weight(1f)) {
            Text(event.title, style = SistaTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                event.description,
                style = SistaTheme.typography.bodySmall,
                color = SistaTheme.colors.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(Spacing.sm))
        Text(
            if (showDay) eventTimeLabel(event.timestamp, nowMillis) else clockOf(event.timestamp),
            style = SistaTheme.typography.labelSmall,
            color = SistaTheme.colors.onSurfaceVariant,
        )
    }
}

private val ZONE: TimeZone = TimeZone.getTimeZone("Asia/Jakarta")
private val DAYS = listOf("Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")
private val MONTHS = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des")

private fun calendarOf(epochMillis: Long): Calendar = Calendar.getInstance(ZONE).apply { timeInMillis = epochMillis }

private fun dayIndex(epochMillis: Long): Long {
    val c = calendarOf(epochMillis)
    return c.get(Calendar.YEAR) * 400L + c.get(Calendar.DAY_OF_YEAR)
}

/** Feed timestamps are epoch seconds. Events recorded per day carry midnight, so no time is shown for them. */
private fun clockOf(epochSeconds: Long): String {
    val c = calendarOf(epochSeconds * 1000)
    val h = c.get(Calendar.HOUR_OF_DAY)
    val m = c.get(Calendar.MINUTE)
    return if (h == 0 && m == 0) "" else String.format(Locale.US, "%02d:%02d", h, m)
}

/** "Hari ini", "Kemarin", or "Senin, 28 Sep" — the heading for a day of events. */
internal fun dayLabel(epochSeconds: Long, nowMillis: Long): String {
    val millis = epochSeconds * 1000
    return when (dayIndex(nowMillis) - dayIndex(millis)) {
        0L -> "Hari ini"
        1L -> "Kemarin"
        else -> {
            val c = calendarOf(millis)
            "${DAYS[c.get(Calendar.DAY_OF_WEEK) - 1]}, ${c.get(Calendar.DAY_OF_MONTH)} ${MONTHS[c.get(Calendar.MONTH)]}"
        }
    }
}

/** "07:12" today, "Kemarin", or "28 Sep". */
internal fun eventTimeLabel(epochSeconds: Long, nowMillis: Long): String {
    val millis = epochSeconds * 1000
    return when (dayIndex(nowMillis) - dayIndex(millis)) {
        0L -> clockOf(epochSeconds).ifEmpty { "Hari ini" }
        1L -> "Kemarin"
        else -> calendarOf(millis).let { "${it.get(Calendar.DAY_OF_MONTH)} ${MONTHS[it.get(Calendar.MONTH)]}" }
    }
}

/** 96.5 → "96,5", 90.0 → "90"; null → "–". */
internal fun decimal(value: Number?): String {
    val v = value?.toDouble() ?: return "–"
    val rounded = Math.round(v * 10) / 10.0
    return if (rounded % 1.0 == 0.0) rounded.toLong().toString() else String.format(Locale.US, "%.1f", rounded).replace('.', ',')
}

/** "2026-09-29" → "Senin, 29 Sep 2026"; null when it is not a date. */
internal fun dateLabel(iso: String?): String? {
    val parts = iso?.take(10)?.split("-") ?: return null
    val y = parts.getOrNull(0)?.toIntOrNull() ?: return null
    val m = parts.getOrNull(1)?.toIntOrNull()?.takeIf { it in 1..12 } ?: return null
    val d = parts.getOrNull(2)?.toIntOrNull() ?: return null
    val c = Calendar.getInstance(ZONE).apply { clear(); set(y, m - 1, d) }
    return "${DAYS[c.get(Calendar.DAY_OF_WEEK) - 1]}, $d ${MONTHS[m - 1]} $y"
}
