package com.sultanagung1.sista.ui.gamification

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*
import androidx.compose.material.icons.Icons
import com.sultanagung1.sista.data.model.LeaderboardEntry

@Composable
fun LeaderboardScreen(
    viewModel: GamificationViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val entries = uiState.leaderboard

    val top1 = entries.find { it.rank == 1 }
    val top2 = entries.find { it.rank == 2 }
    val top3 = entries.find { it.rank == 3 }
    val otherEntries = entries.filter { it.rank > 3 }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Peringkat Siswa",
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Podium Top 3
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TOP 3 PEKAN INI",
                            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp),
                            fontWeight = FontWeight.ExtraBold,
                            color = Emerald700
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // Rank 2 (Silver)
                            top2?.let { PodiumItem(entry = it, medalColor = Slate300, podiumHeight = 90.dp) }

                            // Rank 1 (Gold)
                            top1?.let { PodiumItem(entry = it, medalColor = Gold400, podiumHeight = 120.dp) }

                            // Rank 3 (Bronze)
                            top3?.let { PodiumItem(entry = it, medalColor = AccentAmber, podiumHeight = 70.dp) }
                        }
                    }
                }
            }

            // Other entries header
            item {
                Text(
                    text = "Daftar Peringkat Lengkap",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Rank 4+ Items
            items(otherEntries) { entry ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (entry.isCurrentUser) Emerald50 else MaterialTheme.colorScheme.surface
                    ),
                    border = if (entry.isCurrentUser) androidx.compose.foundation.BorderStroke(1.5.dp, Emerald600) else null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Slate200),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "#${entry.rank}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate800
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (entry.isCurrentUser) "${entry.name} (Anda)" else entry.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = entry.className,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "${entry.xp} XP",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Emerald700
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PodiumItem(
    entry: LeaderboardEntry,
    medalColor: Color,
    podiumHeight: androidx.compose.ui.unit.Dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(medalColor.copy(alpha = 0.2f))
                .border(2.dp, medalColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = medalColor)
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = entry.name.split(" ").firstOrNull() ?: entry.name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "${entry.xp} XP",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = Emerald700
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .width(70.dp)
                .height(podiumHeight)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(medalColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "#${entry.rank}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}
