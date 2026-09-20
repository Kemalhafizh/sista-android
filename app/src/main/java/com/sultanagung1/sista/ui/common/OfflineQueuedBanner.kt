package com.sultanagung1.sista.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.Gold100
import com.sultanagung1.sista.core.designsystem.Gold900

/**
 * FASE 69.3: non-intrusive indicator shown after an optimistic offline-queued
 * mutation (CBT submit, GPS check-in, ...) — copy matches the roadmap spec
 * verbatim: "Disimpan offline — akan otomatis disinkronkan saat terhubung
 * kembali." Deliberately not a red error state — the action already
 * succeeded locally and will replay via [com.sultanagung1.sista.core.sync.OfflineActionQueue].
 */
@Composable
fun OfflineQueuedBanner(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Gold100, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.CloudOff, contentDescription = null, tint = Gold900, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Disimpan offline — akan otomatis disinkronkan saat terhubung kembali",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            color = Gold900
        )
    }
}
