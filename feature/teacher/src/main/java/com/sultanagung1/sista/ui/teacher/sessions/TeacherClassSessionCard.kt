package com.sultanagung1.sista.ui.teacher.sessions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.designsystem.AccentGreen
import com.sultanagung1.sista.core.designsystem.LiveStatusChip
import com.sultanagung1.sista.core.designsystem.SessionAttendanceSummary
import com.sultanagung1.sista.core.designsystem.SulaoneButton
import com.sultanagung1.sista.core.designsystem.SulaoneButtonVariant
import com.sultanagung1.sista.core.designsystem.SulaoneCard
import com.sultanagung1.sista.data.model.ClassSessionDto
import com.sultanagung1.sista.data.model.ClassSessionRules
import com.sultanagung1.sista.data.model.ClassSessionRules.DashboardAction

/**
 * FASE 77.7.2: the teacher dashboard's class-session call to action —
 * "Kembali ke Kelas" while one is running, "Mulai Kelas" when one can start,
 * otherwise when the next one opens. Hidden when there is nothing to do.
 */
@Composable
fun TeacherClassSessionCard(
    sessions: List<ClassSessionDto>,
    nowMinutes: Int,
    onOpenActive: (sessionId: Long) -> Unit,
    onOpenSessions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val action = ClassSessionRules.dashboardAction(sessions, nowMinutes)
    if (action == DashboardAction.NoClassesToday) return

    SulaoneCard(modifier = modifier.fillMaxWidth(), onClick = onOpenSessions) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            when (action) {
                is DashboardAction.ReturnToClass -> {
                    val s = action.session
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LiveStatusChip(text = "LIVE")
                        Text(
                            listOfNotNull(s.subjectName, s.classroomName).joinToString(" • "),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    SessionAttendanceSummary(ClassSessionRules.countsOf(s), showBreakdown = false)
                    SulaoneButton(
                        text = "Kembali ke Kelas ${s.subjectName.orEmpty()}".trim(),
                        onClick = { s.sessionId?.let(onOpenActive) },
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        containerColor = AccentGreen,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                is DashboardAction.StartClass -> {
                    val s = action.session
                    Text(
                        "${s.subjectName.orEmpty()} • ${s.classroomName.orEmpty()} • ${ClassSessionRules.timeRange(s.scheduledStart, s.scheduledEnd)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    SulaoneButton(
                        text = "Mulai Kelas ${s.subjectName.orEmpty()}".trim(),
                        onClick = onOpenSessions,
                        icon = Icons.Default.PlayArrow,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                is DashboardAction.NextClass -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Berikutnya: ${action.session.subjectName.orEmpty()} ${action.session.classroomName.orEmpty()}".trim(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Bisa dimulai pukul ${action.opensAt}", style = MaterialTheme.typography.bodySmall)
                    }
                }
                DashboardAction.AllDone -> Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Semua sesi kelas hari ini sudah selesai.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    SulaoneButton(text = "Lihat", onClick = onOpenSessions, variant = SulaoneButtonVariant.GhostText)
                }
                DashboardAction.NoClassesToday -> Unit
            }
        }
    }
}
