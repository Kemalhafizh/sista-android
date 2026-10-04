package com.sultanagung1.sista.core.ui.component

import androidx.compose.foundation.layout.Column
import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import com.sultanagung1.sista.core.ui.R
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
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing

/** The greeting for an hour of the day (0–23): "Selamat pagi", "Good morning"… */
@StringRes
fun greetingRes(hour: Int): Int = when (hour) {
    in 3..10 -> R.string.greeting_morning
    in 11..14 -> R.string.greeting_noon
    in 15..17 -> R.string.greeting_afternoon
    else -> R.string.greeting_night
}

@Composable
fun greetingFor(hour: Int): String = stringResource(greetingRes(hour))

/**
 * The top of every home screen, whatever the role: who is signed in, one
 * line of details (class and NIS, or NIP) and the notification bell with the
 * unread count. The same header for students, teachers, parents and staff.
 */
@Composable
fun GreetingHeader(
    greeting: String,
    name: String,
    details: List<String>,
    unreadCount: Int,
    onOpenNotifications: () -> Unit,
    modifier: Modifier = Modifier,
    avatarModifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = Spacing.screen, end = Spacing.xs, top = Spacing.lg, bottom = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(name = name, size = 48.dp, modifier = avatarModifier)
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
            val shown = details.filter { it.isNotBlank() }
            if (shown.isNotEmpty()) {
                Text(
                    shown.joinToString(" · "),
                    style = SistaTheme.typography.bodySmall,
                    color = SistaTheme.colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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
