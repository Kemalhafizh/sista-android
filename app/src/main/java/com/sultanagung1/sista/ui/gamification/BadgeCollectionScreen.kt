package com.sultanagung1.sista.ui.gamification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*
import androidx.compose.material.icons.Icons
import com.sultanagung1.sista.data.model.Badge

@Composable
fun BadgeCollectionScreen(
    viewModel: GamificationViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val earned = uiState.earnedBadges
    val locked = uiState.lockedBadges

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Koleksi Lencana",
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Earned Section Header
            item(span = { GridItemSpan(3) }) {
                Text(
                    text = "Lencana Terbuka (${earned.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            items(earned) { badge ->
                BadgeItem(badge = badge, isLocked = false)
            }

            // Locked Section Header
            item(span = { GridItemSpan(3) }) {
                Text(
                    text = "Belum Terbuka (${locked.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
            }

            items(locked) { badge ->
                BadgeItem(badge = badge, isLocked = true)
            }
        }
    }
}

@Composable
private fun BadgeItem(badge: Badge, isLocked: Boolean) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLocked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        border = if (!isLocked) androidx.compose.foundation.BorderStroke(1.dp, Gold400.copy(alpha = 0.5f)) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isLocked) Slate300 else Gold100),
                contentAlignment = Alignment.Center
            ) {
                if (isLocked) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Slate600, modifier = Modifier.size(20.dp))
                } else {
                    Text(text = badge.iconEmoji, fontSize = 24.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = badge.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = if (isLocked) Slate500 else MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "+${badge.xpReward} XP",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                fontWeight = FontWeight.SemiBold,
                color = if (isLocked) Slate400 else Emerald700
            )
        }
    }
}
