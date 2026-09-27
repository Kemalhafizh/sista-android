package com.sultanagung1.sista.core.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class SyncStatus {
    SYNCED,
    SYNCING,
    OFFLINE,
    CONFLICT
}

@Composable
fun SyncStatusBar(
    status: SyncStatus,
    modifier: Modifier = Modifier,
    visible: Boolean = status != SyncStatus.SYNCED
) {
    AnimatedVisibility(visible = visible, modifier = modifier) {
        val (bgColor, icon, text) = when (status) {
            SyncStatus.SYNCED -> Triple(Emerald700, Icons.Default.CloudDone, "Semua data tersinkronisasi")
            SyncStatus.SYNCING -> Triple(AccentAmber, Icons.Default.Sync, "Menyinkronkan data dengan server...")
            SyncStatus.OFFLINE -> Triple(Slate700, Icons.Default.CloudOff, "Mode Offline — Perubahan disimpan secara lokal")
            SyncStatus.CONFLICT -> Triple(AccentRose, Icons.Default.Warning, "Terjadi konflik data — Menggunakan versi terbaru")
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, style = MaterialTheme.typography.labelSmall, color = Color.White)
        }
    }
}
