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
    SulaoneBadge(
        text = ClassSessionRules.label(status),
        containerColor = color.copy(alpha = 0.14f),
        contentColor = color,
        modifier = modifier,
        stateDescription = "Status kehadiran: ${ClassSessionRules.label(status)}"
    )
}

@Composable
fun ClassSessionStatusChip(status: ClassSessionStatus, modifier: Modifier = Modifier) {
    if (status == ClassSessionStatus.ACTIVE) {
        LiveStatusChip(text = "LIVE", modifier = modifier)
        return
    }
    val color = classSessionStatusColor(status)
    SulaoneBadge(
        text = ClassSessionRules.label(status),
        containerColor = color.copy(alpha = 0.14f),
        contentColor = color,
        modifier = modifier,
        stateDescription = "Status sesi: ${ClassSessionRules.label(status)}"
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
            Text(
                text = "${counts.present} / ${counts.total} hadir",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics {
                    contentDescription = "${counts.present} dari ${counts.total} siswa hadir"
                }
            )
        }
        if (showBreakdown) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BreakdownItem(SessionAttendanceStatus.HADIR, counts.hadir)
                BreakdownItem(SessionAttendanceStatus.TELAT, counts.telat)
                BreakdownItem(SessionAttendanceStatus.SAKIT, counts.sakit)
                BreakdownItem(SessionAttendanceStatus.IZIN, counts.izin)
                BreakdownItem(SessionAttendanceStatus.ALPHA, counts.alpha)
            }
        }
    }
}

@Composable
private fun BreakdownItem(status: SessionAttendanceStatus, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(sessionAttendanceColor(status))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "${ClassSessionRules.label(status)} $count",
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
        title = "Sesi Kelas Belum Tersedia",
        description = message,
        icon = Icons.Default.CloudOff,
        modifier = modifier.padding(top = 32.dp),
        ctaLabel = if (onRetry != null) "Coba Lagi" else null,
        onCtaClick = onRetry
    )
}
