package com.sultanagung1.sista.ui.home.sections

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
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
import com.sultanagung1.sista.data.repository.UsageRanking

data class QuickActionItem(
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val backgroundColor: Color,
    val iconColor: Color,
    val onClick: () -> Unit,
    /** FASE 76.4: usage-counter key; null for pills that are not counted ("Semua"). */
    val key: String? = null
)

/** FASE 76.4: stable keys the local usage counter stores for each Home pill. */
internal object QuickActionKeys {
    const val PRESENSI = "home.presensi"
    const val JADWAL = "home.jadwal"
    const val CBT = "home.cbt"
    const val SPP = "home.spp"
}

/**
 * FASE 60.1: Minimalist 5-Pill Quick Actions Row.
 * 4 primary high-frequency services + 1 "Semua" button for progressive disclosure.
 *
 * FASE 76.4: the four service pills are ordered by how often this account taps
 * them on this device ([UsageRanking]); "Semua" always stays last. The title
 * says "Sering Dipakai" only once the order has actually changed, and says what
 * it is based on. It is a counter, so it is never labelled as AI.
 */
@Composable
internal fun HomeMinimalQuickActions(
    isDark: Boolean,
    onNavigateToGeofence: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToCbt: () -> Unit,
    onNavigateToBilling: () -> Unit,
    onOpenAllServices: () -> Unit,
    usage: Map<String, Int> = emptyMap(),
    onActionUsed: (String) -> Unit = {}
) {
    val serviceItems = listOf(
        QuickActionItem("Presensi", "GPS", Icons.Default.LocationOn, Color.White, Emerald600, onNavigateToGeofence, QuickActionKeys.PRESENSI),
        QuickActionItem("Jadwal", "KBM", Icons.Default.CalendarMonth, Color.White, Emerald600, onNavigateToSchedule, QuickActionKeys.JADWAL),
        QuickActionItem("Ujian CBT", "Online", Icons.Default.Quiz, Color.White, Emerald600, onNavigateToCbt, QuickActionKeys.CBT),
        QuickActionItem("SPP", "VA BSI", Icons.Default.AccountBalanceWallet, Color.White, Emerald600, onNavigateToBilling, QuickActionKeys.SPP)
    )
    val isPersonalized = UsageRanking.isReordered(serviceItems, usage) { it.key.orEmpty() }
    val items = UsageRanking.rank(serviceItems, usage) { it.key.orEmpty() } +
        QuickActionItem("Semua", "Layanan", Icons.Default.GridView, Color.White, Emerald600, onOpenAllServices)
    val sectionTitle = if (isPersonalized) "Sering Dipakai" else "Layanan Utama"

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sectionTitle,
                    modifier = Modifier.sulaoneHeading(sectionTitle),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isPersonalized) {
                    Text(
                        text = "Diurutkan dari yang paling sering Anda buka di HP ini",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = "Lihat Semua",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Emerald700,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
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
                ModernQuickActionPill(action, onUsed = onActionUsed)
            }
        }
    }
}

@Composable
internal fun ModernQuickActionPill(action: QuickActionItem, onUsed: (String) -> Unit = {}) {
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
                action.key?.let(onUsed)
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
