package com.sultanagung1.sista.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import com.sultanagung1.sista.core.designsystem.Emerald700

/**
 * FASE 69.2: shown when [FormDraftStore][com.sultanagung1.sista.core.storage.FormDraftStore]
 * finds a locally-persisted draft from a previous session (the user left the
 * form without submitting — closed the app, got a call, lost signal, etc.).
 * Copy matches the roadmap spec verbatim.
 */
@Composable
fun DraftRestoreDialog(
    onRestore: () -> Unit,
    onDiscard: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDiscard,
        icon = { Icon(Icons.Default.History, contentDescription = null, tint = Emerald700, modifier = androidx.compose.ui.Modifier.size(28.dp)) },
        title = { Text("Draf Formulir Ditemukan") },
        text = { Text("Ditemukan draf formulir yang belum tersimpan dari sesi sebelumnya. Pulihkan draf?") },
        confirmButton = {
            Button(
                onClick = onRestore,
                colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
            ) {
                Text("Pulihkan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDiscard) {
                Text("Buang")
            }
        }
    )
}
