package com.sultanagung1.sista.ui.announcements

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Mosque
import androidx.compose.material.icons.outlined.School
import androidx.compose.ui.graphics.vector.ImageVector
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.core.util.Constants
import com.sultanagung1.sista.data.model.AnnouncementItem
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

private val ZONE: ZoneId = ZoneId.of("Asia/Jakarta")
private val MONTHS = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des")

internal fun parseTime(iso: String?): ZonedDateTime? =
    iso?.let { runCatching { OffsetDateTime.parse(it).atZoneSameInstant(ZONE) }.getOrNull() }

/** "Hari ini 16.20", "Kemarin 08.05", "28 Sep", "28 Sep 2025". Falls back to the server's own text. */
internal fun publishedLabel(item: AnnouncementItem, today: LocalDate): String {
    val time = parseTime(item.publishedAt) ?: return item.date
    val date = time.toLocalDate()
    val clock = "%02d.%02d".format(time.hour, time.minute)
    return when {
        date == today -> "Hari ini $clock"
        date == today.minusDays(1) -> "Kemarin $clock"
        date.year == today.year -> "${date.dayOfMonth} ${MONTHS[date.monthValue - 1]}"
        else -> "${date.dayOfMonth} ${MONTHS[date.monthValue - 1]} ${date.year}"
    }
}

/** "Berlaku sampai 30 Sep" while it has an end date. */
internal fun expiryLabel(item: AnnouncementItem): String? =
    parseTime(item.expiresAt)?.toLocalDate()?.let { "Berlaku sampai ${it.dayOfMonth} ${MONTHS[it.monthValue - 1]} ${it.year}" }

/** Storage urls may come back as "/storage/…"; resolve them against the server. */
internal fun absoluteUrl(url: String?, serverRoot: () -> String = { Constants.SERVER_ROOT_URL }): String? = when {
    url.isNullOrBlank() -> null
    url.startsWith("http://") || url.startsWith("https://") -> url
    else -> serverRoot().trimEnd('/') + "/" + url.trimStart('/')
}

/** low, normal, urgent as the web form stores them. */
internal fun isUrgent(item: AnnouncementItem): Boolean = item.priority.equals("urgent", ignoreCase = true)

/** Unread unless the server said otherwise; an older server sends nothing. */
internal fun isUnread(item: AnnouncementItem): Boolean = item.isRead == false

internal fun needsAcknowledgement(item: AnnouncementItem): Boolean = item.requireAcknowledgement && item.acknowledgedAt == null

internal fun AnnouncementCategory.icon(): ImageVector = when (this) {
    AnnouncementCategory.Academic -> Icons.Outlined.School
    AnnouncementCategory.Finance -> Icons.Outlined.AccountBalanceWallet
    AnnouncementCategory.Activity -> Icons.Outlined.Event
    AnnouncementCategory.Islamic -> Icons.Outlined.Mosque
    AnnouncementCategory.All, AnnouncementCategory.General -> Icons.Outlined.Campaign
}

internal fun AnnouncementCategory.tone(): StatusTone = when (this) {
    AnnouncementCategory.Academic -> StatusTone.Brand
    AnnouncementCategory.Finance -> StatusTone.Warning
    AnnouncementCategory.Activity -> StatusTone.Info
    AnnouncementCategory.Islamic -> StatusTone.Success
    AnnouncementCategory.All, AnnouncementCategory.General -> StatusTone.Neutral
}
