package com.sultanagung1.sista.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.sultanagung1.sista.data.model.ClassSessionRules
import com.sultanagung1.sista.data.model.ClassSessionStatus
import com.sultanagung1.sista.data.model.SessionAttendanceStatus

/*
 * FASE 77: visual vocabulary shared by the teacher, student and admin
 * class-session screens, so "Alpha" is the same red everywhere.
 */

/** 77.4.2: green present, orange late, amber sick, blue permitted, red absent. */
fun sessionAttendanceColor(status: SessionAttendanceStatus): Color = when (status) {
    SessionAttendanceStatus.HADIR -> AccentGreen
    SessionAttendanceStatus.TELAT -> Gold700
    SessionAttendanceStatus.SAKIT -> AccentAmber
    SessionAttendanceStatus.IZIN -> AccentBlue
    SessionAttendanceStatus.ALPHA -> AccentRose
}

fun classSessionStatusColor(status: ClassSessionStatus): Color = when (status) {
    ClassSessionStatus.ACTIVE -> AccentGreen
    ClassSessionStatus.SCHEDULED -> Slate500
    ClassSessionStatus.COMPLETED -> Emerald700
    ClassSessionStatus.AUTO_CLOSED -> AccentAmber
    ClassSessionStatus.CANCELLED -> AccentRose
}

@Composable
fun SessionAttendanceChip(status: SessionAttendanceStatus, modifier: Modifier = Modifier) {
    val color = sessionAttendanceColor(status)
    val label = stringResource(ClassSessionText.label(status))
    SulaoneBadge(
        text = label,
        containerColor = color.copy(alpha = 0.14f),
        contentColor = color,
        modifier = modifier,
        stateDescription = stringResource(R.string.cs_attendance_state, label)
    )
}

@Composable
fun ClassSessionStatusChip(status: ClassSessionStatus, modifier: Modifier = Modifier) {
    if (status == ClassSessionStatus.ACTIVE) {
        LiveStatusChip(text = stringResource(R.string.cs_live), modifier = modifier)
        return
    }
    val color = classSessionStatusColor(status)
    val label = stringResource(ClassSessionText.label(status))
    SulaoneBadge(
        text = label,
        containerColor = color.copy(alpha = 0.14f),
        contentColor = color,
        modifier = modifier,
        stateDescription = stringResource(R.string.cs_session_state, label)
    )
}

/**
 * "32 / 40 hadir" with a bar, plus the breakdown. Late counts as present.
 */
@Composable
fun SessionAttendanceSummary(counts: ClassSessionRules.Counts, modifier: Modifier = Modifier, showBreakdown: Boolean = true) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LinearProgressIndicator(
                progress = { counts.presenceRate.toFloat() },
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = AccentGreen,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            val spoken = stringResource(R.string.cs_present_of_cd, counts.present, counts.total)
            Text(
                text = stringResource(R.string.cs_present_of, counts.present, counts.total),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics {
                    contentDescription = spoken
                }
            )
        }
        if (showBreakdown) {
            val b = counts.breakdown
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (b != null) {
                    BreakdownItem(SessionAttendanceStatus.HADIR, stringResource(R.string.cs_status_hadir), b.hadir)
                    BreakdownItem(SessionAttendanceStatus.TELAT, stringResource(R.string.cs_status_telat), b.telat)
                    BreakdownItem(SessionAttendanceStatus.SAKIT, stringResource(R.string.cs_status_sakit), b.sakit)
                    BreakdownItem(SessionAttendanceStatus.IZIN, stringResource(R.string.cs_status_izin), b.izin)
                } else {
                    // A session summary only knows present (incl. late), alpha and the rest.
                    BreakdownItem(SessionAttendanceStatus.HADIR, stringResource(R.string.cs_present_or_late), counts.present)
                    BreakdownItem(SessionAttendanceStatus.SAKIT, stringResource(R.string.cs_sick_or_excused), counts.excused)
                }
                BreakdownItem(SessionAttendanceStatus.ALPHA, stringResource(R.string.cs_status_alpha), counts.alpha)
            }
        }
    }
}

@Composable
private fun BreakdownItem(status: SessionAttendanceStatus, label: String, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(sessionAttendanceColor(status))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = stringResource(R.string.cs_breakdown, label, count),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Shown instead of a list when the server has no FASE 117 routes yet. Honest
 * about it rather than an empty list that looks like "no classes today".
 */
@Composable
fun ClassSessionUnavailableState(message: String, onRetry: (() -> Unit)?, modifier: Modifier = Modifier) {
    SulaoneEmptyState(
        title = stringResource(R.string.cs_unavailable_title),
        description = message,
        icon = Icons.Default.CloudOff,
        modifier = modifier.padding(top = 32.dp),
        ctaLabel = if (onRetry != null) stringResource(R.string.cs_retry) else null,
        onCtaClick = onRetry
    )
}
