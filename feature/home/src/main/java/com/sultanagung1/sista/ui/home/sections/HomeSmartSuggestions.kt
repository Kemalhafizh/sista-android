package com.sultanagung1.sista.ui.home.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.accessibility.sulaoneHeading
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import com.sultanagung1.sista.core.motion.springPressable
import com.sultanagung1.sista.data.model.ContextualHomePayload

@Composable
internal fun HomeStreakBanner(
    streakGradient: Brush? = null,
    onNavigateToMutabaah: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.surface.isDark()
    val haptics = rememberHapticFeedbackHelper()

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .springPressable {
                    haptics.tapLight()
                    onNavigateToMutabaah()
                },
            shape = RoundedCornerShape(18.dp),
            color = if (isDark) Slate900 else Color.White,
            border = androidx.compose.foundation.BorderStroke(
                width = 0.5.dp,
                color = if (isDark) Slate800 else Slate200
            ),
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Gold100),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔥", fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AnimatedCounterText(
                                targetValue = 14,
                                suffix = " Hari Beruntun Aktif",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "4 dari 8 Amalan Yaumiyah hari ini telah terisi",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Smooth Minimalist Progress Bar
                LinearProgressIndicator(
                    progress = { 0.5f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(50)),
                    color = Emerald700,
                    trackColor = if (isDark) Slate800 else Slate100
                )
            }
        }
    }
}

@Composable
internal fun HomeContextualSection(
    contextualPayload: ContextualHomePayload,
    onNavigateToGamification: (() -> Unit)?,
    onNavigateToMutabaah: () -> Unit,
    onNavigateRoute: ((String) -> Unit)?
) {
    val isDark = MaterialTheme.colorScheme.surface.isDark()
    val haptics = rememberHapticFeedbackHelper()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Gamification Summary Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isDark) Slate900 else Color.White,
            border = androidx.compose.foundation.BorderStroke(
                0.5.dp,
                if (isDark) Slate800 else Slate200
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .springPressable {
                    haptics.tapLight()
                    onNavigateToGamification?.invoke()
                },
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Gold100),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Gold600,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Level ${contextualPayload.gamification.level} • ${contextualPayload.gamification.totalXp} XP",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "+${contextualPayload.gamification.xpToday} XP hari ini • Streak ${contextualPayload.gamification.streakDays} hari 🔥",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Emerald700
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Smart Suggestions Horizontal Row
        if (contextualPayload.suggestions.isNotEmpty()) {
            Text(
                text = "Rekomendasi Pintar Hari Ini",
                modifier = Modifier.sulaoneHeading("Rekomendasi Pintar Hari Ini"),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 4.dp)
            ) {
                items(contextualPayload.suggestions) { sugg ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isDark) Slate900 else Slate50,
                        border = androidx.compose.foundation.BorderStroke(
                            0.5.dp,
                            if (isDark) Slate800 else Slate200
                        ),
                        modifier = Modifier
                            .width(260.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .springPressable {
                                haptics.tapLight()
                                if (sugg.ctaRoute.contains("mutabaah")) onNavigateToMutabaah()
                                else onNavigateRoute?.invoke(sugg.ctaRoute)
                            }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = sugg.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "👉 ${sugg.ctaText}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Emerald700
                            )
                        }
                    }
                }
            }
        }
    }
}
