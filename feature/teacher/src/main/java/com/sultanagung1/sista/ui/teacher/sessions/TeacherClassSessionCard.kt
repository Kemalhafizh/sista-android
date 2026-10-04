package com.sultanagung1.sista.ui.teacher.sessions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.sultanagung1.sista.core.ui.component.ButtonVariant
import com.sultanagung1.sista.core.ui.component.CardVariant
import com.sultanagung1.sista.core.ui.component.IconBadge
import com.sultanagung1.sista.core.ui.component.SistaButton
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.data.model.ClassSessionDto
import com.sultanagung1.sista.data.model.ClassSessionRules
import com.sultanagung1.sista.data.model.ClassSessionRules.DashboardAction
import com.sultanagung1.sista.data.model.ClassSessionStatus
import androidx.compose.ui.res.stringResource
import com.sultanagung1.sista.feature.teacher.R

/**
 * FASE 77.7.2: the teacher home's class-session call to action —
 * "Kembali ke kelas" while one is running, "Mulai kelas" when one can start,
 * otherwise when the next one opens. Hidden when there is nothing to do.
 */
@Composable
fun TeacherClassSessionCard(
    sessions: List<ClassSessionDto>,
    nowMinutes: Int,
    onOpenActive: (sessionId: Long) -> Unit,
    onOpenSessions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val action = ClassSessionRules.dashboardAction(sessions, nowMinutes)
    if (action == DashboardAction.NoClassesToday) return

    SistaCard(
        modifier = modifier.fillMaxWidth(),
        variant = if (action is DashboardAction.ReturnToClass) CardVariant.Highlighted else CardVariant.Filled,
        onClick = onOpenSessions,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            when (action) {
                is DashboardAction.ReturnToClass -> {
                    val s = action.session
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SessionStatusPill(ClassSessionStatus.ACTIVE)
                        Spacer(Modifier.width(Spacing.sm))
                        Text(title(s), style = SistaTheme.typography.titleMedium)
                    }
                    AttendanceSummary(ClassSessionRules.countsOf(s), showBreakdown = false)
                    SistaButton(
                        stringResource(R.string.tc_back),
                        { s.sessionId?.let(onOpenActive) },
                        leadingIcon = Icons.AutoMirrored.Outlined.ArrowForward,
                        fullWidth = true,
                    )
                }
                is DashboardAction.StartClass -> {
                    val s = action.session
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBadge(Icons.Outlined.PlayArrow, tone = StatusTone.Brand)
                        Spacer(Modifier.width(Spacing.md))
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.tc_time_to_teach), style = SistaTheme.typography.labelMedium, color = SistaTheme.colors.onSurfaceVariant)
                            Text(title(s), style = SistaTheme.typography.titleMedium)
                            Text(ClassSessionRules.timeRange(s.scheduledStart, s.scheduledEnd), style = SistaTheme.typography.bodySmall, color = SistaTheme.colors.onSurfaceVariant)
                        }
                    }
                    SistaButton(stringResource(R.string.ts_start), onOpenSessions, leadingIcon = Icons.Outlined.PlayArrow, fullWidth = true)
                }
                is DashboardAction.NextClass -> Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(Icons.Outlined.Schedule, tone = StatusTone.Info)
                    Spacer(Modifier.width(Spacing.md))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.tc_next, title(action.session)), style = SistaTheme.typography.titleSmall)
                        Text(stringResource(R.string.ts_opens_at, action.opensAt), style = SistaTheme.typography.bodySmall, color = SistaTheme.colors.onSurfaceVariant)
                    }
                }
                DashboardAction.AllDone -> Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(Icons.Outlined.CheckCircle, tone = StatusTone.Success)
                    Spacer(Modifier.width(Spacing.md))
                    Text(stringResource(R.string.tc_all_done), style = SistaTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    SistaButton(stringResource(R.string.tc_view), onOpenSessions, variant = ButtonVariant.Text)
                }
                DashboardAction.NoClassesToday -> Unit
            }
        }
    }
}

@Composable
private fun title(session: ClassSessionDto): String =
    listOfNotNull(session.subjectName, session.classroomName).joinToString(" · ").ifBlank { stringResource(R.string.ta_session_fallback) }
