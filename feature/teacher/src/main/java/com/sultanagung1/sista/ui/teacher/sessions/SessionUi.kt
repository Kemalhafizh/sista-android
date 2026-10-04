package com.sultanagung1.sista.ui.teacher.sessions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.ui.component.EmptyState
import com.sultanagung1.sista.core.ui.component.StatusPill
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.core.ui.theme.colors
import com.sultanagung1.sista.data.model.ClassSessionRules
import com.sultanagung1.sista.data.model.ClassSessionStatus
import com.sultanagung1.sista.data.model.SessionAttendanceStatus
import androidx.compose.ui.res.stringResource
import com.sultanagung1.sista.feature.teacher.R
import com.sultanagung1.sista.core.ui.R as CoreUiR
import com.sultanagung1.sista.core.designsystem.R as DsR
import com.sultanagung1.sista.core.designsystem.ClassSessionText

/** Class-session pieces shared by the teacher's session screens (design system v2). */

fun ClassSessionStatus.tone(): StatusTone = when (this) {
    ClassSessionStatus.ACTIVE -> StatusTone.Success
    ClassSessionStatus.SCHEDULED -> StatusTone.Neutral
    ClassSessionStatus.COMPLETED -> StatusTone.Info
    ClassSessionStatus.AUTO_CLOSED -> StatusTone.Warning
    ClassSessionStatus.CANCELLED -> StatusTone.Danger
}

fun SessionAttendanceStatus.tone(): StatusTone = when (this) {
    SessionAttendanceStatus.HADIR -> StatusTone.Success
    SessionAttendanceStatus.TELAT -> StatusTone.Warning
    SessionAttendanceStatus.SAKIT, SessionAttendanceStatus.IZIN -> StatusTone.Info
    SessionAttendanceStatus.ALPHA -> StatusTone.Danger
}

@Composable
fun SessionStatusPill(status: ClassSessionStatus, modifier: Modifier = Modifier) {
    StatusPill(stringResource(ClassSessionText.label(status)), status.tone(), modifier)
}

@Composable
fun AttendanceStatusPill(status: SessionAttendanceStatus, modifier: Modifier = Modifier) {
    StatusPill(stringResource(ClassSessionText.label(status)), status.tone(), modifier)
}

/** "32 / 40 hadir" with a bar and, when known, the breakdown. Late counts as present. */
@Composable
fun AttendanceSummary(counts: ClassSessionRules.Counts, modifier: Modifier = Modifier, showBreakdown: Boolean = true) {
    val spoken = stringResource(DsR.string.cs_present_of_cd, counts.present, counts.total)
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.semantics(mergeDescendants = true) {
                contentDescription = spoken
            },
        ) {
            LinearProgressIndicator(
                progress = { counts.presenceRate.toFloat() },
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(CircleShape),
                color = StatusTone.Success.colors().content,
                trackColor = SistaTheme.colors.surfaceContainerHighest,
                drawStopIndicator = {},
            )
            Spacer(Modifier.width(Spacing.md))
            Text(stringResource(DsR.string.cs_present_of, counts.present, counts.total), style = SistaTheme.typography.labelLarge, color = SistaTheme.colors.onSurface)
        }
        if (showBreakdown) {
            val b = counts.breakdown
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                if (b != null) {
                    Legend(SessionAttendanceStatus.HADIR, stringResource(DsR.string.cs_status_hadir), b.hadir)
                    Legend(SessionAttendanceStatus.TELAT, stringResource(DsR.string.cs_status_telat), b.telat)
                    Legend(SessionAttendanceStatus.SAKIT, stringResource(DsR.string.cs_status_sakit), b.sakit)
                    Legend(SessionAttendanceStatus.IZIN, stringResource(DsR.string.cs_status_izin), b.izin)
                } else {
                    // A session summary only knows present (incl. late), alpha and the rest.
                    Legend(SessionAttendanceStatus.HADIR, stringResource(DsR.string.cs_present_or_late), counts.present)
                    Legend(SessionAttendanceStatus.SAKIT, stringResource(DsR.string.cs_sick_or_excused), counts.excused)
                }
                Legend(SessionAttendanceStatus.ALPHA, stringResource(DsR.string.cs_status_alpha), counts.alpha)
            }
        }
    }
}

@Composable
private fun Legend(status: SessionAttendanceStatus, label: String, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(status.tone().colors().content),
        )
        Spacer(Modifier.width(Spacing.xs))
        Text(stringResource(DsR.string.cs_breakdown, label, count), style = SistaTheme.typography.labelMedium, color = SistaTheme.colors.onSurfaceVariant)
    }
}

/**
 * Shown instead of a list when the server has no class-session routes yet —
 * honest about it rather than an empty list that reads as "no classes today".
 */
@Composable
fun SessionsUnavailable(message: String, onRetry: (() -> Unit)?, modifier: Modifier = Modifier) {
    EmptyState(
        title = stringResource(R.string.ts_unavailable),
        body = message,
        icon = Icons.Outlined.CloudOff,
        actionLabel = if (onRetry != null) stringResource(CoreUiR.string.core_retry) else null,
        onAction = onRetry,
        modifier = modifier,
    )
}
