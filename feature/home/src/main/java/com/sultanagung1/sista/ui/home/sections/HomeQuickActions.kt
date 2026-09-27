package com.sultanagung1.sista.ui.home.sections

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.accessibility.sulaoneHeading
import com.sultanagung1.sista.core.accessibility.sulaoneInteractiveTouchTarget
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import com.sultanagung1.sista.core.motion.springPressable
import com.sultanagung1.sista.core.motion.sulaoneSharedBounds
import com.sultanagung1.sista.ui.navigation.Screen

data class QuickActionItem(
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val backgroundColor: Color,
    val iconColor: Color,
    val onClick: () -> Unit
)

/**
 * FASE 60.1: Minimalist 5-Pill Quick Actions Row.
 * 4 primary high-frequency services + 1 "Semua" button for progressive disclosure.
 */
@Composable
internal fun HomeMinimalQuickActions(
    isDark: Boolean,
    onNavigateToGeofence: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToCbt: () -> Unit,
    onNavigateToBilling: () -> Unit,
    onOpenAllServices: () -> Unit
) {
    val items = listOf(
        QuickActionItem("Presensi", "GPS", Icons.Default.LocationOn, Color.White, Emerald600, onNavigateToGeofence),
        QuickActionItem("Jadwal", "KBM", Icons.Default.CalendarMonth, Color.White, Emerald600, onNavigateToSchedule),
        QuickActionItem("Ujian CBT", "Online", Icons.Default.Quiz, Color.White, Emerald600, onNavigateToCbt),
        QuickActionItem("SPP", "VA BSI", Icons.Default.AccountBalanceWallet, Color.White, Emerald600, onNavigateToBilling),
        QuickActionItem("Semua", "Layanan", Icons.Default.GridView, Color.White, Emerald600, onOpenAllServices)
    )

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Layanan Utama",
                modifier = Modifier.sulaoneHeading("Layanan Utama"),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Lihat Semua",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Emerald700,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .springPressable { onOpenAllServices() }
                    .padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            items.forEach { action ->
                ModernQuickActionPill(action)
            }
        }
    }
}

@Composable
internal fun HomeBentoGrid(
    isDark: Boolean,
    onNavigateToGeofence: () -> Unit,
    onNavigateToCbt: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1: Presensi GPS
            ModernBentoCard(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 100.dp),
                shape = RoundedCornerShape(18.dp),
                backgroundColor = if (isDark) Slate900 else Color.White,
                borderColor = if (isDark) Slate800 else Slate200,
                elevation = 0.dp,
                onClick = onNavigateToGeofence
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isDark) Slate800 else Slate100),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Emerald600,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        LiveStatusChip("Radius 250m", color = Emerald600)
                    }
                    Column {
                        Text(
                            text = "Presensi GPS",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Check-in Kampus",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Card 2: Ujian CBT Siswa
            ModernBentoCard(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 100.dp)
                    .sulaoneSharedBounds(key = "cbt_hero_card"),
                shape = RoundedCornerShape(18.dp),
                backgroundColor = if (isDark) Slate900 else Color.White,
                borderColor = if (isDark) Slate800 else Slate200,
                elevation = 0.dp,
                onClick = onNavigateToCbt
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isDark) Slate800 else Slate100),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Quiz,
                                contentDescription = null,
                                tint = Emerald600,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isDark) Slate800 else Slate100
                        ) {
                            Text(
                                text = "Anti-Cheat",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                color = Emerald600,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Ujian CBT",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Online Exam",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun HomeQuickServicesGrid(
    isDark: Boolean,
    onNavigateToDynamicQr: () -> Unit,
    onNavigateToCbt: () -> Unit,
    onNavigateToGrades: () -> Unit,
    onNavigateToBilling: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToMutabaah: () -> Unit,
    onNavigateRoute: ((String) -> Unit)?
) {
    val services = listOf(
        QuickActionItem("QR Scan", "Dynamic TOTP", Icons.Default.QrCodeScanner, Color.White, Emerald600, onNavigateToDynamicQr),
        QuickActionItem("Ujian CBT", "Online Exam", Icons.Default.Quiz, Color.White, Emerald600, onNavigateToCbt),
        QuickActionItem("Rapor KKTP", "Nilai & Capaian", Icons.Default.AutoGraph, Color.White, Emerald600, onNavigateToGrades),
        QuickActionItem("Tagihan SPP", "BSI Virtual", Icons.Default.AccountBalanceWallet, Color.White, Emerald600, onNavigateToBilling),
        QuickActionItem("Jadwal KBM", "Kalender", Icons.Default.CalendarMonth, Color.White, Emerald600, onNavigateToSchedule),
        QuickActionItem("Konsultasi BK", "Chat Guru", Icons.AutoMirrored.Filled.Chat, Color.White, Emerald600, onNavigateToChat),
        QuickActionItem("Mutabaah", "Amalan Sunnah", Icons.Default.Mosque, Color.White, Emerald600, onNavigateToMutabaah),
        QuickActionItem("Poin Tatib", "Buku Saku", Icons.Default.Gavel, Color.White, Emerald600, { onNavigateRoute?.invoke(Screen.Discipline.route) })
    )

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                services.take(4).forEach { service ->
                    ModernQuickActionPill(service)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                services.drop(4).take(4).forEach { service ->
                    ModernQuickActionPill(service)
                }
            }
        }
    }
}

@Composable
internal fun ModernQuickActionPill(action: QuickActionItem) {
    val haptics = rememberHapticFeedbackHelper()
    val isDark = MaterialTheme.colorScheme.surface.isDark()

    Column(
        modifier = Modifier
            .width(68.dp)
            .sulaoneInteractiveTouchTarget(48.dp)
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = "${action.title}, ${action.subtitle ?: ""}"
            }
            .springPressable {
                haptics.tapLight()
                action.onClick()
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Slate900 else Color.White
            ),
            elevation = CardDefaults.cardElevation(0.dp),
            border = BorderStroke(0.5.dp, if (isDark) Slate800 else Slate200),
            modifier = Modifier.size(50.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = null,
                    tint = Emerald600,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = action.title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp
            ),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
