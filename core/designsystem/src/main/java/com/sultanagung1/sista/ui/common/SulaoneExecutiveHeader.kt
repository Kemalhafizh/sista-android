package com.sultanagung1.sista.ui.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.R
import com.sultanagung1.sista.core.accessibility.sulaoneInteractiveTouchTarget
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import com.sultanagung1.sista.core.motion.springPressable

/**
 * Metadata Chip used in the Executive Top Bar across Teacher, Parent, and Admin roles.
 */
data class HeaderMetadataChip(
    val text: String,
    val icon: ImageVector? = null,
    val isLiveDot: Boolean = false,
    val dotColor: Color = Emerald500,
    val containerColor: Color = Color.Unspecified,
    val borderColor: Color = Color.Unspecified,
    val textColor: Color = Color.Unspecified,
    val iconColor: Color = Gold500
)

/**
 * Unified Modern Executive Top App Bar for Teacher, Parent, and Admin roles.
 *
 * Adopts the elegant minimalism of Apple HIG & Material 3:
 * - Clean theme-aware surface with subtle 0.5dp divider
 * - WCAG 2.2 AA 48dp touch targets with haptic micro-interactions
 * - Polished monogram avatar with role badge & verification checkmark
 * - Adaptive metadata chips with live status indicators
 */
@Composable
fun SulaoneExecutiveHeader(
    modifier: Modifier = Modifier,
    userName: String,
    titlePrefix: String = "Assalamu'alaikum,",
    chips: List<HeaderMetadataChip> = emptyList(),
    unreadNotificationsCount: Int = 0,
    onAvatarClick: (() -> Unit)? = null,
    onQrClick: (() -> Unit)? = null,
    onNotificationClick: (() -> Unit)? = null
) {
    val haptics = rememberHapticFeedbackHelper()
    val isDark = MaterialTheme.colorScheme.surface.let { (0.299f * it.red + 0.587f * it.green + 0.114f * it.blue) < 0.5f }

    val timeGreeting = remember {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        when (hour) {
            in 3..10 -> "Selamat Pagi ☀️"
            in 11..14 -> "Selamat Siang 🌤️"
            in 15..17 -> "Selamat Sore 🌅"
            else -> "Selamat Malam 🌙"
        }
    }

    val initials = remember(userName) {
        userName.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercaseChar() }
            .joinToString("")
            .ifEmpty { "SA" }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Top Row: School Identity Tag + Action Buttons (QR Scanner & Notifications)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Official School Brand Pill (Minimalist Muted)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isDark) Slate800 else Slate100)
                        .border(
                            width = 0.5.dp,
                            color = if (isDark) Slate700 else Slate200,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_kotak),
                        contentDescription = "Logo SMA Islam Sultan Agung 1",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SMA ISLAM SULTAN AGUNG 1",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Actions: QR Scanner & Notification Bell (48x48dp Touch Targets)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onQrClick != null) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .sulaoneInteractiveTouchTarget(48.dp)
                                .semantics(mergeDescendants = true) {
                                    role = Role.Button
                                    contentDescription = "Pindai Kode QR"
                                }
                                .springPressable {
                                    haptics.tapLight()
                                    onQrClick()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Slate800 else Slate100)
                                    .border(0.5.dp, if (isDark) Slate700 else Slate200, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    if (onNotificationClick != null) {
                        val notifDesc = if (unreadNotificationsCount > 0)
                            "Pusat Notifikasi, ada $unreadNotificationsCount pesan belum dibaca"
                        else
                            "Pusat Notifikasi"

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .sulaoneInteractiveTouchTarget(48.dp)
                                .semantics(mergeDescendants = true) {
                                    role = Role.Button
                                    contentDescription = notifDesc
                                }
                                .springPressable {
                                    haptics.tapLight()
                                    onNotificationClick()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Slate800 else Slate100)
                                    .border(0.5.dp, if (isDark) Slate700 else Slate200, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                                if (unreadNotificationsCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .align(Alignment.TopEnd)
                                            .offset(x = (-2).dp, y = 2.dp)
                                            .clip(CircleShape)
                                            .background(AccentRose)
                                            .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Profile Row: Avatar + Greeting & Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User Avatar with Monogram & Verified Badge
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .springPressable {
                            haptics.tapMedium()
                            onAvatarClick?.invoke()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Emerald700,
                                        Emerald800
                                    )
                                )
                            )
                            .border(1.5.dp, Emerald400.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }

                    // Verified Badge
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(Gold500)
                            .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Terverifikasi",
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Greeting & Name
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$timeGreeting • $titlePrefix",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            letterSpacing = (-0.3).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Chips Row (if any)
            if (chips.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (chip in chips) {
                        val effectiveBg = if (chip.containerColor != Color.Unspecified && chip.containerColor != Color.White.copy(alpha = 0.15f)) {
                            chip.containerColor
                        } else {
                            if (isDark) Slate800 else Slate100
                        }

                        val effectiveBorder = if (chip.borderColor != Color.Unspecified && chip.borderColor != Color.White.copy(alpha = 0.25f)) {
                            chip.borderColor
                        } else {
                            if (isDark) Slate700 else Slate200
                        }

                        val effectiveText = if (chip.textColor != Color.Unspecified && chip.textColor != Color.White) {
                            chip.textColor
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(effectiveBg)
                                .border(0.5.dp, effectiveBorder, RoundedCornerShape(20.dp))
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            if (chip.isLiveDot) {
                                val infiniteTransition = rememberInfiniteTransition(label = "LiveDot")
                                val alpha by infiniteTransition.animateFloat(
                                    initialValue = 0.4f,
                                    targetValue = 1.0f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(800, easing = LinearEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "PulseAlpha"
                                )
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .graphicsLayer { this.alpha = alpha }
                                        .clip(CircleShape)
                                        .background(chip.dotColor)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                            } else if (chip.icon != null) {
                                Icon(
                                    imageVector = chip.icon,
                                    contentDescription = null,
                                    tint = chip.iconColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }

                            Text(
                                text = chip.text,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = effectiveText
                            )
                        }
                    }
                }
            }
        }
    }
}
