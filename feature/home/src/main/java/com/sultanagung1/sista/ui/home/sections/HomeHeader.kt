package com.sultanagung1.sista.ui.home.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.motion.sulaoneSharedBounds
import com.sultanagung1.sista.core.ui.component.Avatar
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing

/** "Selamat pagi" for the device's hour; the greeting is the only thing the clock decides here. */
fun greetingFor(hour: Int): String = when (hour) {
    in 3..10 -> "Selamat pagi"
    in 11..14 -> "Selamat siang"
    in 15..17 -> "Selamat sore"
    else -> "Selamat malam"
}

/**
 * Who is signed in and how many notifications wait. Name, class and NIS
 * come from the signed-in account; the count from `student/notifications`.
 */
@Composable
fun HomeHeader(
    greeting: String,
    name: String,
    classroom: String?,
    identifier: String?,
    unreadCount: Int,
    onOpenNotifications: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = Spacing.screen, end = Spacing.xs, top = Spacing.lg, bottom = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(name = name, size = 48.dp, modifier = Modifier.sulaoneSharedBounds(key = "student_avatar"))
        Spacer(Modifier.width(Spacing.md))
        Column(Modifier.weight(1f)) {
            Text(greeting, style = SistaTheme.typography.bodyMedium, color = SistaTheme.colors.onSurfaceVariant)
            Text(
                name,
                style = SistaTheme.typography.titleLarge,
                color = SistaTheme.colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.semantics { heading() },
            )
            val details = listOfNotNull(
                classroom?.takeIf { it.isNotBlank() && it != "—" }?.let { "Kelas $it" },
                identifier?.takeIf { it.isNotBlank() && it != "—" }?.let { "NIS $it" },
            )
            if (details.isNotEmpty()) {
                Text(
                    details.joinToString(" · "),
                    style = SistaTheme.typography.bodySmall,
                    color = SistaTheme.colors.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
        IconButton(onClick = onOpenNotifications) {
            val label = if (unreadCount > 0) "Notifikasi, $unreadCount belum dibaca" else "Notifikasi"
            BadgedBox(badge = { if (unreadCount > 0) Badge { Text(if (unreadCount > 99) "99+" else "$unreadCount") } }) {
                Icon(Icons.Outlined.Notifications, contentDescription = label)
            }
        }
    }
}
