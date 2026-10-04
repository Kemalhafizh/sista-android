package com.sultanagung1.sista.ui.notifications

import java.util.Locale
import java.time.format.DateTimeFormatter
import com.sultanagung1.sista.ui.profile.displayLocale
import com.sultanagung1.sista.feature.profile.R
import com.sultanagung1.sista.core.notification.descriptionRes
import com.sultanagung1.sista.core.notification.labelRes
import com.sultanagung1.sista.core.ui.text.UiText
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.data.model.NotificationChannelType
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

internal val SCHOOL_ZONE: ZoneId = ZoneId.of("Asia/Jakarta")


internal fun parseInstant(iso: String?): ZonedDateTime? =
    iso?.let { runCatching { OffsetDateTime.parse(it).atZoneSameInstant(SCHOOL_ZONE) }.getOrNull() }

/**
 * "Hari ini", "Kemarin", or the date in the app's language: "Senin, 28 Sep" /
 * "Monday, 28 Sep" / "الاثنين، 28 سبتمبر", with the year when it isn't this year.
 */
internal fun dayHeader(date: LocalDate, today: LocalDate, locale: Locale = displayLocale()): UiText = when (date) {
    today -> UiText.Res(R.string.notif_today)
    today.minusDays(1) -> UiText.Res(R.string.notif_yesterday)
    else -> UiText.Raw(
        date.format(DateTimeFormatter.ofPattern(if (date.year == today.year) "EEEE, d MMM" else "EEEE, d MMM yyyy", locale)),
    )
}

/** "16.20" in Indonesian, "16:20" otherwise — the day is in the header above. */
internal fun clock(time: ZonedDateTime, locale: Locale = displayLocale()): String =
    "%02d%s%02d".format(time.hour, if (locale.language == "id" || locale.language == "in") "." else ":", time.minute)

/** Items in order, split into days; items without a time go last under "Lainnya". */
internal fun <T> groupByDay(items: List<T>, today: LocalDate, timeOf: (T) -> ZonedDateTime?): List<Pair<UiText, List<T>>> {
    val (timed, untimed) = items.partition { timeOf(it) != null }
    val days = timed.groupBy { timeOf(it)!!.toLocalDate() }
        .toSortedMap(compareByDescending { it })
        .map { (date, list) -> dayHeader(date, today) to list.sortedByDescending { timeOf(it) } }
    return if (untimed.isEmpty()) days else days + (UiText.Res(R.string.notif_other) to untimed)
}

internal fun channelOf(id: String): NotificationChannelType =
    NotificationChannelType.values().firstOrNull { it.channelId == id } ?: NotificationChannelType.GENERAL

internal fun NotificationChannelType.icon(): ImageVector = when (this) {
    NotificationChannelType.ATTENDANCE -> Icons.Outlined.EventAvailable
    NotificationChannelType.ACADEMIC -> Icons.Outlined.School
    NotificationChannelType.FINANCE -> Icons.Outlined.AccountBalanceWallet
    NotificationChannelType.EMERGENCY -> Icons.Outlined.Warning
    NotificationChannelType.GENERAL -> Icons.Outlined.Notifications
}

internal fun NotificationChannelType.tone(): StatusTone = when (this) {
    NotificationChannelType.ATTENDANCE -> StatusTone.Success
    NotificationChannelType.ACADEMIC -> StatusTone.Brand
    NotificationChannelType.FINANCE -> StatusTone.Warning
    NotificationChannelType.EMERGENCY -> StatusTone.Danger
    NotificationChannelType.GENERAL -> StatusTone.Info
}

/** The channel's name as the user sees it in the app and in Android's settings. */
@get:StringRes
internal val NotificationChannelType.label: Int get() = labelRes()

@get:StringRes
internal val NotificationChannelType.description: Int get() = descriptionRes()
