package com.sultanagung1.sista.ui.ibadah

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.accessibility.sulaoneInteractiveTouchTarget
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import com.sultanagung1.sista.core.motion.springPressable

/**
 * FASE 67 KDoc
 * MutabaahScreen: A modern overhaul following SISTA design system.
 */
@Composable
fun MutabaahScreen(
    viewModel: IbadahViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val completedCount = uiState.mutabaahItems.count { it.isCompleted }
    val totalCount = uiState.mutabaahItems.size
    val isDark = isSystemInDarkTheme()
    val haptics = rememberHapticFeedbackHelper()

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Mutabaah Yaumiyah",
                subtitle = "Pembiasaan Karakter Islami Sultan Agung",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) MaterialTheme.colorScheme.background else Slate50)
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            // Header Progress Card
            item {
                SulaoneGradientCard(
                    brush = Brush.linearGradient(listOf(Emerald900, Emerald700)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Mosque,
                                contentDescription = null,
                                tint = Gold400,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PENCAPAIAN HARI INI",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Gold400
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "12 Safar 1448 H", // Static for now per instruction
                            style = MaterialTheme.typography.bodySmall,
                            color = Emerald100
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "$completedCount dari $totalCount Amalan",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (completedCount == totalCount && totalCount > 0) "Masya Allah, seluruh amalan tuntas!" else "Tingkatkan amalan sunnah untuk keberkahan ilmu.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Emerald100
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { if (totalCount > 0) completedCount.toFloat() / totalCount else 0f },
                                    modifier = Modifier.size(64.dp),
                                    color = Gold400,
                                    trackColor = Emerald900,
                                    strokeWidth = 6.dp
                                )
                            }
                        }
                    }
                }
            }

            // Amalan items
            items(uiState.mutabaahItems, key = { it.id }) { item ->
                val cardBg = if (item.isCompleted) {
                    if (isDark) Emerald900.copy(alpha = 0.2f) else Emerald50
                } else {
                    if (isDark) MaterialTheme.colorScheme.surface else Color.White
                }
                
                val borderColor = if (isDark) Slate800 else Slate200

                ModernBentoCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .springPressable {
                            haptics.tapLight()
                            viewModel.toggleMutabaah(item.id)
                            if (!item.isCompleted) {
                                haptics.success()
                            }
                        },
                    backgroundColor = cardBg,
                    borderColor = borderColor,
                    elevation = 0.dp,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Checkbox circle
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (item.isCompleted) Emerald600 else Color.Transparent)
                                .border(
                                    width = if (item.isCompleted) 0.dp else 1.5.dp,
                                    color = if (item.isCompleted) Color.Transparent else if (isDark) Slate700 else Slate200,
                                    shape = CircleShape
                                )
                                .sulaoneInteractiveTouchTarget(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (item.isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) MaterialTheme.colorScheme.onSurface else Slate900,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                val badgeContainer = when (item.category) {
                                    "FARDHU" -> if (isDark) Emerald800 else Emerald100
                                    "SUNNAH" -> if (isDark) Gold800 else Gold100
                                    else -> if (isDark) AccentBlue else AccentBlue.copy(alpha = 0.15f)
                                }
                                val badgeContent = when (item.category) {
                                    "FARDHU" -> if (isDark) Color.White else Emerald800
                                    "SUNNAH" -> if (isDark) Color.White else Gold800
                                    else -> if (isDark) Color.White else AccentBlue
                                }
                                
                                SulaoneBadge(
                                    text = item.category,
                                    containerColor = badgeContainer,
                                    contentColor = badgeContent
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Slate500
                            )
                        }
                    }
                }
            }
            
            // Motivational footer card
            item {
                Spacer(modifier = Modifier.height(12.dp))
                if (totalCount > 0 && completedCount == totalCount) {
                    SulaoneGradientCard(
                        brush = Brush.linearGradient(listOf(Emerald700, Emerald600)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Masya Allah! Semua amalan tuntas hari ini 🎉",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    ModernBentoCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = if (isDark) Emerald900.copy(alpha = 0.1f) else Emerald50.copy(alpha = 0.5f),
                        borderColor = if (isDark) Slate800 else Slate200,
                        elevation = 0.dp,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Yuk, selesaikan ${totalCount - completedCount} amalan lagi!",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                color = if (isDark) Emerald100 else Emerald800
                            )
                        }
                    }
                }
            }
        }
    }
}
