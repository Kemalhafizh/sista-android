package com.sultanagung1.sista.ui.home.sections

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.R
import com.sultanagung1.sista.core.accessibility.sulaoneInteractiveTouchTarget
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import com.sultanagung1.sista.core.motion.springPressable
import com.sultanagung1.sista.core.motion.sulaoneSharedElement
import com.sultanagung1.sista.ui.home.HomeUiState
import com.sultanagung1.sista.ui.navigation.Screen

@Composable
internal fun HomeHeroSection(
    uiState: HomeUiState,
    isDark: Boolean,
    timeGreeting: String,
    onNavigateToDynamicQr: () -> Unit,
    onNavigateToAnnouncements: () -> Unit,
    onNavigateRoute: ((String) -> Unit)?
) {
    val haptics = rememberHapticFeedbackHelper()
    val studentName = uiState.userName.ifBlank { "Muhammad Rizky Pratama" }
    val initials = remember(studentName) {
        studentName.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercaseChar() }
            .joinToString("")
            .ifEmpty { "SA" }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        // Top Bar: School Identity + Action Buttons Cluster (Apple HIG / M3 Clean)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // School Identity Tag / Pill (Minimalist Muted)
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

            // Actions: QR Scanner & Notification Bell (WCAG 2.2 AA 48x48dp Touch Targets)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick QR Scanner
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .sulaoneInteractiveTouchTarget(48.dp)
                        .semantics(mergeDescendants = true) {
                            role = Role.Button
                            contentDescription = "Pindai QR Presensi dan Pembayaran"
                        }
                        .springPressable {
                            haptics.tapLight()
                            onNavigateToDynamicQr()
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

                // Notification Bell with Badge
                val notifDesc = if (uiState.unreadNotificationsCount > 0)
                    "Pusat Notifikasi, ada ${uiState.unreadNotificationsCount} pesan belum dibaca"
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
                            onNavigateToAnnouncements()
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
                        if (uiState.unreadNotificationsCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-2).dp, y = 2.dp)
                                    .clip(CircleShape)
                                    .background(AccentRose)
                                    .border(1.5.dp, MaterialTheme.colorScheme.background, CircleShape)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Profile Row: Avatar + Greeting & Name + Status Chips (Minimalist Air & Whitespace)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Avatar with Monogram & Verification Badge
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .sulaoneSharedElement(key = "student_avatar")
                    .springPressable {
                        haptics.tapMedium()
                        onNavigateRoute?.invoke(Screen.Profile.route)
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Gold100, Gold300, Gold500)
                            )
                        )
                        .border(
                            width = 1.5.dp,
                            color = Gold400,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = Emerald950
                    )
                }

                // Mini Verified Badge
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 2.dp, y = 2.dp)
                        .clip(CircleShape)
                        .background(Emerald700)
                        .border(1.5.dp, MaterialTheme.colorScheme.background, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Center Column: Greeting, Student Name, Metadata Chips
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = "Assalamu'alaikum,",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = if (isDark) Slate300 else Slate900
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (isDark) Slate400 else Slate900.copy(alpha = 0.5f)
                    )
                    Text(
                        text = timeGreeting,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (isDark) Gold300 else Slate900
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 1.dp)
                ) {
                    Text(
                        text = studentName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.2).sp
                        ),
                        color = if (isDark) MaterialTheme.colorScheme.onSurface else Slate900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Terverifikasi",
                        tint = Gold500,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    // Class Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isDark) Slate800 else Slate100,
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, if (isDark) Slate700 else Slate200)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = Emerald700,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = uiState.studentClass.ifBlank { "XII MIPA 1" },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Active Student Status Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Emerald50,
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Emerald200)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(Emerald700)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Siswa Aktif",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = Emerald800
                            )
                        }
                    }

                    // NIS Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isDark) Slate800 else Slate100,
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, if (isDark) Slate700 else Slate200)
                    ) {
                        Text(
                            text = "NIS: ${uiState.userIdentifier.ifBlank { "212210045" }}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
