package com.sultanagung1.sista.ui.notifications

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

private val DAYS = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
private val MONTHS = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des")

internal fun parseInstant(iso: String?): ZonedDateTime? =
    iso?.let { runCatching { OffsetDateTime.parse(it).atZoneSameInstant(SCHOOL_ZONE) }.getOrNull() }

/** "Hari ini", "Kemarin", "Senin, 28 Sep", or "Senin, 28 Sep 2025" in another year. */
internal fun dayHeader(date: LocalDate, today: LocalDate): String = when (date) {
    today -> "Hari ini"
    today.minusDays(1) -> "Kemarin"
    else -> buildString {
        append("${DAYS[date.dayOfWeek.value - 1]}, ${date.dayOfMonth} ${MONTHS[date.monthValue - 1]}")
        if (date.year != today.year) append(" ${date.year}")
    }
}

/** "16.20" — the day is in the header above. */
internal fun clock(time: ZonedDateTime): String = "%02d.%02d".format(time.hour, time.minute)

/** Items in order, split into days; items without a time go last under "Lainnya". */
internal fun <T> groupByDay(items: List<T>, today: LocalDate, timeOf: (T) -> ZonedDateTime?): List<Pair<String, List<T>>> {
    val (timed, untimed) = items.partition { timeOf(it) != null }
    val days = timed.groupBy { timeOf(it)!!.toLocalDate() }
        .toSortedMap(compareByDescending { it })
        .map { (date, list) -> dayHeader(date, today) to list.sortedByDescending { timeOf(it) } }
    return if (untimed.isEmpty()) days else days + ("Lainnya" to untimed)
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
internal val NotificationChannelType.label: String get() = channelName
