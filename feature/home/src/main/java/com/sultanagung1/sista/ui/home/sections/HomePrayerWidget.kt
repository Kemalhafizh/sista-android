package com.sultanagung1.sista.ui.home.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.data.model.PrayerSchedule

/**
 * [prayerSchedule] is null until a real prayer-time source is wired — the
 * school backend has no JSON API for this yet, so this renders an honest
 * "not available" card instead of fabricated Hijri dates/times.
 */
@Composable
internal fun HomePrayerWidget(prayerSchedule: PrayerSchedule?) {
    val isDark = MaterialTheme.colorScheme.surface.isDark()

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Emerald50),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mosque,
                            contentDescription = null,
                            tint = Emerald700,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Jadwal Sholat",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (prayerSchedule == null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Jadwal sholat belum tersedia di aplikasi ini.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Menuju Waktu ${prayerSchedule.nextPrayerName}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${prayerSchedule.nextPrayerCountdown} lagi menuju adzan",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ModernPrayerBadge("Subuh", prayerSchedule.fajr, isCurrent = prayerSchedule.currentPrayer == "Subuh")
                        ModernPrayerBadge("Dzuhur", prayerSchedule.dhuhr, isCurrent = prayerSchedule.currentPrayer == "Dzuhur")
                        ModernPrayerBadge("Ashar", prayerSchedule.asr, isCurrent = prayerSchedule.currentPrayer == "Ashar")
                        ModernPrayerBadge("Maghrib", prayerSchedule.maghrib, isCurrent = prayerSchedule.currentPrayer == "Maghrib")
                        ModernPrayerBadge("Isya", prayerSchedule.isha, isCurrent = prayerSchedule.currentPrayer == "Isya")
                    }
                }
            }
        }
    }
}

@Composable
internal fun ModernPrayerBadge(name: String, time: String, isCurrent: Boolean = false) {
    val isDark = MaterialTheme.colorScheme.surface.isDark()

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isCurrent) Emerald700
                else if (isDark) Slate800 else Slate100
            )
            .border(
                width = 0.5.dp,
                color = if (isCurrent) Emerald700 else (if (isDark) Slate700 else Slate200),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 9.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isCurrent) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(Gold400)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                }
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = time,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
