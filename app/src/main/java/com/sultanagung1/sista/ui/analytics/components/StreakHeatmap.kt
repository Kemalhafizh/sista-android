package com.sultanagung1.sista.ui.analytics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.accessibility.AccessibilityFormatters
import com.sultanagung1.sista.core.designsystem.Emerald600

@Composable
fun StreakHeatmap(
    activeDays: Set<Int>, // Set of day indices in range (e.g. 0..27)
    modifier: Modifier = Modifier,
    totalDays: Int = 28,
    rows: Int = 4
) {
    val cols = (totalDays + rows - 1) / rows
    val summary = AccessibilityFormatters.streakHeatmapSummary(totalDays, activeDays.size)

    Column(
        modifier = modifier.semantics(mergeDescendants = true) {
            role = Role.Image
            contentDescription = summary
        }
    ) {
        for (r in 0 until rows) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                for (c in 0 until cols) {
                    val dayIndex = r * cols + c
                    if (dayIndex < totalDays) {
                        val isActive = activeDays.contains(dayIndex)
                        val color = if (isActive) Emerald600 else MaterialTheme.colorScheme.surfaceVariant
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(color)
                                .semantics {
                                    contentDescription = "Hari ke-${dayIndex + 1}: ${if (isActive) "Aktif" else "Tidak aktif"}"
                                }
                        )
                    }
                }
            }
        }
    }
}
