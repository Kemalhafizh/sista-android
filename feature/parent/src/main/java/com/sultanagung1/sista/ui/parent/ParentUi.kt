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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import com.sultanagung1.sista.core.ui.component.IconBadge
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.core.ui.text.UiText
import com.sultanagung1.sista.core.ui.text.displayLocale
import com.sultanagung1.sista.core.ui.text.localDecimal
import com.sultanagung1.sista.data.model.ChildActivityEvent
import com.sultanagung1.sista.feature.parent.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Icon and tone for a `parent/child/{uuid}/feed` event. The tone comes from the
 * event's [ChildActivityEvent.code]; the title is in the parent's language and
 * is never read.
 */
internal fun activityStyle(type: String, code: String?): Pair<ImageVector, StatusTone> = when (type) {
    "attendance" -> Icons.Outlined.EventAvailable to when (code) {
        "present" -> StatusTone.Success
        "absent" -> StatusTone.Danger
        else -> StatusTone.Warning
    }
    "academic" -> Icons.Outlined.Assignment to StatusTone.Info
    "discipline" -> Icons.Outlined.Gavel to if (code == "reward") StatusTone.Success else StatusTone.Danger
    "library" -> Icons.AutoMirrored.Outlined.MenuBook to StatusTone.Brand
    "ibadah" -> Icons.Outlined.Mosque to StatusTone.Brand
    else -> Icons.Outlined.Notifications to StatusTone.Neutral
}

/** The app's language for numbers and dates, with Latin digits for Arabic. */
@Composable
internal fun parentLocale(): Locale = displayLocale(LocalConfiguration.current.locales[0])

/** One school event: what happened, the detail, and when. */
@Composable
internal fun ActivityRow(event: ChildActivityEvent, nowMillis: Long, modifier: Modifier = Modifier, showDay: Boolean = true) {
    val (icon, tone) = activityStyle(event.type, event.code)
    val locale = parentLocale()
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
            if (showDay) eventTimeLabel(event.timestamp, nowMillis, locale).asString() else clockOf(event.timestamp),
            style = SistaTheme.typography.labelSmall,
            color = SistaTheme.colors.onSurfaceVariant,
        )
    }
}

private val ZONE: ZoneId = ZoneId.of("Asia/Jakarta")

private fun dateOf(epochMillis: Long): LocalDate = Instant.ofEpochMilli(epochMillis).atZone(ZONE).toLocalDate()

private fun daysAgo(epochSeconds: Long, nowMillis: Long): Long =
    ChronoUnit.DAYS.between(dateOf(epochSeconds * 1000), dateOf(nowMillis))

/** Feed timestamps are epoch seconds. Events recorded per day carry midnight, so no time is shown for them. */
internal fun clockOf(epochSeconds: Long): String {
    val t = Instant.ofEpochSecond(epochSeconds).atZone(ZONE).toLocalTime()
    return if (t.hour == 0 && t.minute == 0) "" else String.format(Locale.ROOT, "%02d:%02d", t.hour, t.minute)
}

/** "Hari ini", "Kemarin", or "Senin, 28 Sep" / "Monday, 28 Sep": the heading for a day of events. */
internal fun dayLabel(epochSeconds: Long, nowMillis: Long, locale: Locale): UiText = when (daysAgo(epochSeconds, nowMillis)) {
    0L -> UiText.Res(R.string.day_today)
    1L -> UiText.Res(R.string.day_yesterday)
    else -> UiText.Raw(dateOf(epochSeconds * 1000).format(DateTimeFormatter.ofPattern("EEEE'${comma(locale)}' d MMM", locale)))
}

/** Arabic writes its own comma between the day name and the date. */
private fun comma(locale: Locale): String = if (locale.language == "ar") "،" else ","

/** "07:12" today, "Kemarin", or "28 Sep". */
internal fun eventTimeLabel(epochSeconds: Long, nowMillis: Long, locale: Locale): UiText = when (daysAgo(epochSeconds, nowMillis)) {
    0L -> clockOf(epochSeconds).takeIf { it.isNotEmpty() }?.let { UiText.Raw(it) } ?: UiText.Res(R.string.day_today)
    1L -> UiText.Res(R.string.day_yesterday)
    else -> UiText.Raw(dateOf(epochSeconds * 1000).format(DateTimeFormatter.ofPattern("d MMM", locale)))
}

/** 96.5 → "96,5" in Indonesian, "96.5" otherwise; 90.0 → "90"; null → "–". */
internal fun decimal(value: Number?, locale: Locale): String = value?.let { localDecimal(it.toDouble(), locale) } ?: "–"

/** "2026-09-29" → "Selasa, 29 Sep 2026" / "Tuesday, 29 Sep 2026"; null when it is not a date. */
internal fun dateLabel(iso: String?, locale: Locale): String? =
    iso?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        ?.format(DateTimeFormatter.ofPattern("EEEE'${comma(locale)}' d MMM yyyy", locale))
