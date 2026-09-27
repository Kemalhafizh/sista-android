package com.sultanagung1.sista.ui.gamification

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import com.sultanagung1.sista.core.designsystem.*

@Composable
fun GamificationDashboardScreen(
    viewModel: GamificationViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToBadges: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showConfetti by remember { mutableStateOf(false) }

    val profile = uiState.profile ?: com.sultanagung1.sista.data.model.GamificationProfile(
        totalXp = 1420, level = 7, levelTitle = "Thalibul Ilmi Mujahid",
        xpToNextLevel = 180, streakDays = 14, longestStreak = 28,
        badgesEarned = 12, monthlyRank = 3
    )

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Gamifikasi & Prestasi",
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { showConfetti = true }) {
                        Icon(imageVector = Icons.Default.Celebration, contentDescription = "Selebrasi", tint = Gold500)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Hero Card: Level & XP
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Emerald800),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(listOf(Emerald800, Emerald900))
                                )
                                .padding(20.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "LEVEL ${profile.level}",
                                            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp),
                                            color = Gold400,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = profile.levelTitle,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Streak Badge
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = AccentAmber.copy(alpha = 0.2f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentAmber)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(imageVector = Icons.Default.LocalFireDepartment, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${profile.streakDays} Hari",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // Animated Counter XP
                                AnimatedCounter(
                                    count = profile.totalXp,
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Black
                                    ),
                                    suffix = " XP"
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Progress Bar
                                val progress = (1000f - profile.xpToNextLevel) / 1000f
                                val animatedProgress by animateFloatAsState(targetValue = progress, label = "xpProgress")
                                LinearProgressIndicator(
                                    progress = { animatedProgress.coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(CircleShape),
                                    color = Gold400,
                                    trackColor = Emerald950
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${profile.xpToNextLevel} XP menuju Level ${profile.level + 1}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Emerald200
                                )
                            }
                        }
                    }
                }

                // Quick Nav Action Buttons (Leaderboard & Badges)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onNavigateToLeaderboard,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Leaderboard, contentDescription = null, tint = Emerald600)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Leaderboard")
                        }

                        Button(
                            onClick = onNavigateToBadges,
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = Gold300)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Lencana (${profile.badgesEarned})")
                        }
                    }
                }

                // XP History Title
                item {
                    Text(
                        text = "Riwayat Perolehan XP",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // XP History Items
                items(uiState.xpHistory) { item ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.activity,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = item.createdAt,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "+${item.xpEarned} XP",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Emerald600
                            )
                        }
                    }
                }
            }

            if (showConfetti) {
                ConfettiEffect(onFinished = { showConfetti = false })
            }
        }
    }
}
