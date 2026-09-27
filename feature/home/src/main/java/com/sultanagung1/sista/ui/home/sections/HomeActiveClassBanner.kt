package com.sultanagung1.sista.ui.home.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.designsystem.AccentGreen
import com.sultanagung1.sista.core.designsystem.LiveStatusChip
import com.sultanagung1.sista.core.designsystem.SulaoneCard
import com.sultanagung1.sista.data.model.ActiveClassSessionDto
import com.sultanagung1.sista.data.model.ClassSessionRules

/**
 * FASE 77.5.3: "Sesi Matematika sedang berlangsung — Tap untuk absen". Only
 * rendered while a class of this student is running; once the student is
 * recorded it turns into a quiet confirmation instead of a call to action.
 */
@Composable
fun HomeActiveClassBanner(
    session: ActiveClassSessionDto,
    onScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subject = session.subjectName ?: "kelas"
    if (session.alreadyCheckedIn) {
        SulaoneCard(modifier = modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentGreen)
                Text(
                    text = "Anda sudah absen di sesi $subject" +
                        (session.attendanceStatus?.let { " (${ClassSessionRules.label(it)})" } ?: "") + ".",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        return
    }

    SulaoneCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                role = Role.Button
                contentDescription = "Sesi $subject sedang berlangsung. Ketuk untuk absen."
            },
        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
        onClick = onScan
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 64.dp)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LiveStatusChip(text = "LIVE")
                    Text(
                        text = "Sesi $subject sedang berlangsung",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = listOfNotNull("Tap untuk absen", session.teacherName).joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}
