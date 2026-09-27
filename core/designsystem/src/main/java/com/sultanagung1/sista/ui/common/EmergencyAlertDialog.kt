package com.sultanagung1.sista.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.designsystem.AccentRose
import com.sultanagung1.sista.core.websocket.WebSocketEvent

/**
 * FASE 71.4 "Tombol Siaran Darurat" — shown app-wide, for every role, the
 * moment an App\Events\EmergencyBroadcastEvent arrives over the WebSocket.
 * Deliberately not dismissible by tapping outside; the user must acknowledge.
 */
@Composable
fun EmergencyAlertDialog(
    alert: WebSocketEvent.EmergencyAlertTriggered,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* must be acknowledged explicitly */ },
        icon = {
            Icon(
                Icons.Default.NotificationImportant,
                contentDescription = null,
                tint = AccentRose,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = alert.title.ifBlank { "Peringatan Darurat" },
                fontWeight = FontWeight.Bold,
                color = AccentRose
            )
        },
        text = {
            Column {
                Text(alert.message)
                if (alert.location.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Lokasi: ${alert.location}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AccentRose)
            ) {
                Text("Saya Mengerti")
            }
        }
    )
}
