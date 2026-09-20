package com.sultanagung1.sista.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.sync.SyncManager

@Composable
fun SyncStatusHeader(
    syncManager: SyncManager,
    modifier: Modifier = Modifier
) {
    val isOnline by syncManager.isOnline.collectAsState()
    val isSyncing by syncManager.isSyncing.collectAsState()
    val lastSyncedTime by syncManager.lastSyncedTime.collectAsState()
    val pendingCount by syncManager.pendingActionsCount.collectAsState()

    Column(modifier = modifier.fillMaxWidth()) {
        // Offline Warning Banner
        AnimatedVisibility(
            visible = !isOnline,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Gold100)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = Gold900,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mode Offline Aktif (Data Tersimpan Lokal)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Gold900
                        )
                    }

                    if (pendingCount > 0) {
                        SulaoneBadge(
                            text = "$pendingCount Aksi Tertunda",
                            containerColor = Gold400,
                            contentColor = Slate950
                        )
                    }
                }
            }
        }

        // Active Syncing Banner
        AnimatedVisibility(
            visible = isSyncing,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Emerald50)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = Emerald700
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Menyinkronkan data dengan server terpadu...",
                        style = MaterialTheme.typography.labelSmall,
                        color = Emerald800,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
