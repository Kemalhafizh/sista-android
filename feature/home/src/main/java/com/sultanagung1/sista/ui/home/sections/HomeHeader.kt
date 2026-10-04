package com.sultanagung1.sista.ui.home.sections

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.sultanagung1.sista.core.motion.sulaoneSharedBounds
import com.sultanagung1.sista.core.ui.component.GreetingHeader
import com.sultanagung1.sista.feature.home.R

/**
 * The student's header: the shared [GreetingHeader] with class and NIS from
 * the signed-in account and the count from `student/notifications`.
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
    GreetingHeader(
        greeting = greeting,
        name = name,
        details = listOfNotNull(
            classroom?.takeIf { it.isNotBlank() && it != "—" }?.let { stringResource(R.string.home_detail_class, it) },
            identifier?.takeIf { it.isNotBlank() && it != "—" }?.let { stringResource(R.string.home_detail_nis, it) },
        ),
        unreadCount = unreadCount,
        onOpenNotifications = onOpenNotifications,
        modifier = modifier,
        avatarModifier = Modifier.sulaoneSharedBounds(key = "student_avatar"),
    )
}
