package com.sultanagung1.sista.ui.profile

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.R
import androidx.compose.ui.platform.LocalContext
import com.sultanagung1.sista.core.accessibility.LocalAppStrings
import com.sultanagung1.sista.core.accessibility.ThemeManager
import com.sultanagung1.sista.core.accessibility.sulaoneInteractiveTouchTarget
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.display.AdaptiveRefreshRateManager
import com.sultanagung1.sista.core.display.RefreshRateMode
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import com.sultanagung1.sista.core.motion.springPressable
import com.sultanagung1.sista.core.motion.sulaoneSharedElement
import com.sultanagung1.sista.core.storage.SessionManager


/**
 * FASE 76.6: why true black helps, without overselling. OLED pixels showing
 * #000000 are switched off; an LCD backlight stays on whatever the colour.
 */
internal const val AMOLED_BATTERY_NOTE =
    "Hitam pekat mematikan piksel di layar OLED/AMOLED, jadi lebih hemat baterai. Di layar LCD tampilannya saja yang berubah."

@Composable
fun ProfileScreen(
    sessionManager: SessionManager,
    themeManager: ThemeManager? = null,
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToBiometrics: () -> Unit = {},
    onNavigateToAnnouncements: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToDiagnostics: () -> Unit = {},
    onNavigateToComprehensiveProfile: (() -> Unit)? = null,
    onLogout: () -> Unit
) {
    val userName by sessionManager.userNameFlow.collectAsState(initial = "Ahmad Kemal Hafizh")
    val userRole by sessionManager.userRoleFlow.collectAsState(initial = "student")
    val strings = LocalAppStrings.current
    val haptics = rememberHapticFeedbackHelper()
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val borderColor = if (isDark) Slate800 else Slate200
    val cardBg = if (isDark) MaterialTheme.colorScheme.surface else Color.White

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showRefreshRateDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val currentRefreshMode by AdaptiveRefreshRateManager.getStoredModeFlow(sessionManager)
        .collectAsState(initial = RefreshRateMode.ADAPTIVE_SMOOTH)
    val displayCaps = remember { AdaptiveRefreshRateManager.detectCapabilities(context) }
    val isAdminOrSuper = userRole?.contains("admin", ignoreCase = true) == true || userRole?.contains("superadmin", ignoreCase = true) == true

    val roleBadge = when {
        userRole?.contains("superadmin", ignoreCase = true) == true -> strings.superadminBadge
        userRole?.contains("admin", ignoreCase = true) == true || userRole?.contains("kepsek", ignoreCase = true) == true -> strings.principalBadge
        userRole?.contains("teacher", ignoreCase = true) == true || userRole?.contains("guru", ignoreCase = true) == true -> strings.teacherBadge
        userRole?.contains("parent", ignoreCase = true) == true || userRole?.contains("ortu", ignoreCase = true) == true -> strings.parentBadge
        else -> strings.studentActive
    }

    val cardTitle = when {
        userRole?.contains("superadmin", ignoreCase = true) == true -> "KARTU IDENTITAS EKSEKUTIF"
        userRole?.contains("admin", ignoreCase = true) == true || userRole?.contains("kepsek", ignoreCase = true) == true -> "KARTU IDENTITAS PIMPINAN"
        userRole?.contains("teacher", ignoreCase = true) == true || userRole?.contains("guru", ignoreCase = true) == true -> "KARTU IDENTITAS PENDIDIK"
        userRole?.contains("parent", ignoreCase = true) == true || userRole?.contains("ortu", ignoreCase = true) == true -> "KARTU IDENTITAS WALI MURID"
        else -> "KARTU TANDA PELAJAR DIGITAL"
    }

    val idNumberLabel = when {
        userRole?.contains("superadmin", ignoreCase = true) == true -> "ID OTORITAS: YBWSA-001"
        userRole?.contains("admin", ignoreCase = true) == true || userRole?.contains("kepsek", ignoreCase = true) == true -> "NIP: 197405121998031002"
        userRole?.contains("teacher", ignoreCase = true) == true || userRole?.contains("guru", ignoreCase = true) == true -> "NIP: 198203152006042001"
        userRole?.contains("parent", ignoreCase = true) == true || userRole?.contains("ortu", ignoreCase = true) == true -> "ID WALI: WM-2024-8891"
        else -> "NISN: 0071829102 • Kelas XII MIPA 1"
    }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = strings.profileTab,
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) MaterialTheme.colorScheme.background else Slate50)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Digital Institutional ID Card (SuperApp High-Fidelity Pass)
            DigitalInstitutionalIdCard(
                cardTitle = cardTitle,
                userName = userName ?: "Pengguna Sulaone",
                roleBadge = roleBadge,
                idNumber = idNumberLabel,
                userRole = userRole ?: "student",
                isDark = isDark,
                borderColor = borderColor,
                cardBg = cardBg
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Section: Keamanan & Preferensi Akun
            ProfileSectionHeader(title = "Keamanan & Tampilan")

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = cardBg,
                border = BorderStroke(0.5.dp, borderColor),
                shadowElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    if (themeManager != null) {
                        val currentThemeMode by themeManager.themeMode.collectAsState()
                        val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
                        val isDarkEffective = when (currentThemeMode) {
                            com.sultanagung1.sista.core.accessibility.AppThemeMode.LIGHT -> false
                            com.sultanagung1.sista.core.accessibility.AppThemeMode.DARK,
                            com.sultanagung1.sista.core.accessibility.AppThemeMode.AMOLED_BLACK,
                            com.sultanagung1.sista.core.accessibility.AppThemeMode.HIGH_CONTRAST -> true
                            com.sultanagung1.sista.core.accessibility.AppThemeMode.SYSTEM -> isSystemDark
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    haptics.tapLight()
                                    themeManager.toggleDarkLight(isDarkEffective)
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Emerald50),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isDarkEffective) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = null,
                                    tint = if (isDarkEffective) Gold700 else Emerald700,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Mode Gelap (Dark Mode)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isDarkEffective) "Tema gelap aktif (nyaman di mata)" else "Tema terang aktif (bersih & cerah)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Switch(
                                checked = isDarkEffective,
                                onCheckedChange = { checked ->
                                    haptics.tapLight()
                                    themeManager.setThemeMode(
                                        if (checked) com.sultanagung1.sista.core.accessibility.AppThemeMode.DARK
                                        else com.sultanagung1.sista.core.accessibility.AppThemeMode.LIGHT
                                    )
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Emerald700,
                                    uncheckedThumbColor = Slate400,
                                    uncheckedTrackColor = Slate200
                                )
                            )
                        }

                        // FASE 76.6: true-black AMOLED existed (AmoledColorScheme, pure
                        // #000000) but was only reachable from a dialog in Settings, with a
                        // vague "super hemat baterai" line. Offer it right where people turn
                        // dark mode on, with an honest note on when it saves battery.
                        val isAmoled = currentThemeMode == com.sultanagung1.sista.core.accessibility.AppThemeMode.AMOLED_BLACK
                        val isHighContrast = currentThemeMode == com.sultanagung1.sista.core.accessibility.AppThemeMode.HIGH_CONTRAST
                        if (isDarkEffective && !isHighContrast) {
                            Column(modifier = Modifier.padding(start = 52.dp, bottom = 12.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(
                                        selected = !isAmoled,
                                        onClick = {
                                            haptics.tapLight()
                                            themeManager.setThemeMode(com.sultanagung1.sista.core.accessibility.AppThemeMode.DARK)
                                        },
                                        label = { Text("Gelap") }
                                    )
                                    FilterChip(
                                        selected = isAmoled,
                                        onClick = {
                                            haptics.tapLight()
                                            themeManager.setThemeMode(com.sultanagung1.sista.core.accessibility.AppThemeMode.AMOLED_BLACK)
                                        },
                                        label = { Text("Hitam Pekat") }
                                    )
                                }
                                Text(
                                    text = AMOLED_BATTERY_NOTE,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                    }

                    ModernProfileMenuItem(
                        icon = Icons.Default.Fingerprint,
                        iconTint = Emerald700,
                        iconBg = Emerald50,
                        title = strings.biometricSecurity,
                        subtitle = "Kunci aplikasi & masuk instan dengan sidik jari",
                        onClick = {
                            haptics.tapLight()
                            onNavigateToBiometrics()
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                    ModernProfileMenuItem(
                        icon = Icons.Default.Speed,
                        iconTint = Gold700,
                        iconBg = Gold50,
                        title = "Laju Penyegaran Layar (Refresh Rate)",
                        subtitle = "Layar: ${displayCaps.summaryText} • ${currentRefreshMode.title.split(" (").first()}",
                        onClick = {
                            haptics.tapLight()
                            showRefreshRateDialog = true
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                    ModernProfileMenuItem(
                        icon = Icons.Default.BugReport,
                        iconTint = AccentRose,
                        iconBg = AccentRose.copy(alpha = 0.12f),
                        title = "Pusat Diagnostik & Laporan Kendala",
                        subtitle = "Status telemetri sistem, crash analytics & laporan IT",
                        onClick = {
                            haptics.tapLight()
                            onNavigateToDiagnostics()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Section: Pengaturan & Multi-Role
            ProfileSectionHeader(title = "Layanan & Preferensi")

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = cardBg,
                border = BorderStroke(0.5.dp, borderColor),
                shadowElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    ModernProfileMenuItem(
                        icon = Icons.Default.Notifications,
                        iconTint = AccentBlue,
                        iconBg = AccentBlue.copy(alpha = 0.12f),
                        title = strings.notificationsTitle,
                        subtitle = "Pengumuman yayasan, jadwal sholat & KBM",
                        onClick = {
                            haptics.tapLight()
                            onNavigateToAnnouncements()
                        }
                    )
                    HorizontalDivider(color = borderColor.copy(alpha = 0.5f))

                    ModernProfileMenuItem(
                        icon = Icons.Default.Tune,
                        iconTint = Gold700,
                        iconBg = Gold50,
                        title = strings.settingsTitle,
                        subtitle = "Bahasa (ID, EN, AR), font scaling & ukuran teks",
                        onClick = {
                            haptics.tapLight()
                            onNavigateToSettings()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Section: Lembaga & Informasi Hukum
            ProfileSectionHeader(title = "Lembaga & Informasi")

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = cardBg,
                border = BorderStroke(0.5.dp, borderColor),
                shadowElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    ModernProfileMenuItem(
                        drawableRes = R.drawable.logo_kotak,
                        title = strings.aboutSchool,
                        subtitle = "SMA Islam Sultan Agung 1 • SULAONE Enterprise v2.0",
                        onClick = {
                            haptics.tapLight()
                            showAboutDialog = true
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Logout Button (Outlined Minimalist Style with 48dp target)
            OutlinedButton(
                onClick = {
                    haptics.tapLight()
                    showLogoutDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .sulaoneInteractiveTouchTarget(48.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.45f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.logoutButton,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }

    // Confirmation Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(strings.logoutConfirmTitle, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(strings.logoutConfirmMessage)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        haptics.tapHeavy()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(strings.confirmLogoutText)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutDialog = false }) {
                    Text(strings.cancelText)
                }
            }
        )
    }

    // About Dialog with Official Crest
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            icon = {
                Image(
                    painter = painterResource(id = R.drawable.logo_kotak),
                    contentDescription = "Logo SMA Islam Sultan Agung 1",
                    modifier = Modifier.size(52.dp)
                )
            },
            title = {
                Text(
                    text = "SMA Islam Sultan Agung 1",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_lonjong),
                            contentDescription = "Banner SMA Islam Sultan Agung 1",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }

                    Text(
                        text = "SULAONE Enterprise Mobile Suite",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Emerald800
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Aplikasi SuperApp Terpadu SMA Islam Sultan Agung 1 Semarang. Mengintegrasikan Kurikulum Merdeka, CBT Anti-Cheat, Presensi Geofence, Socratic AI Tutor, Paspor Digital Web3, dan Pembayaran SPP.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "• Versi: 2.0.0-PROD (Enterprise Release)\n• Yayasan: Badan Wakaf Sultan Agung (YBWSA)\n• Framework: Jetpack Compose Material 3 Native\n• Arsitektur: MVVM + Offline-First Room DB",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                ) {
                    Text("Tutup")
                }
            }
        )
    }

    if (showRefreshRateDialog) {
        AlertDialog(
            onDismissRequest = { showRefreshRateDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = Gold700,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Laju Penyegaran Layar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Kemampuan Layar: ${displayCaps.summaryText}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Emerald800
                    )
                    Text(
                        text = "Frekuensi hardware: ${displayCaps.supportedRefreshRates.map { "${it.toInt()}Hz" }.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    RefreshRateMode.entries.forEach { mode ->
                        val isSelected = currentRefreshMode == mode
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Emerald50 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) Emerald600 else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    haptics.tapLight()
                                    AdaptiveRefreshRateManager.saveMode(
                                        sessionManager = sessionManager,
                                        mode = mode,
                                        activity = context as? Activity
                                    )
                                    showRefreshRateDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(selectedColor = Emerald700)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = mode.title,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Emerald900 else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = mode.description,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showRefreshRateDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                ) {
                    Text("Tutup")
                }
            }
        )
    }
}

/**
 * High-Fidelity Digital Institutional ID Card (Kartu Identitas Digital Resmi)
 */
@Composable
private fun DigitalInstitutionalIdCard(
    cardTitle: String,
    userName: String,
    roleBadge: String,
    idNumber: String,
    userRole: String,
    isDark: Boolean = false,
    borderColor: Color = Slate200,
    cardBg: Color = Color.White
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = cardBg,
        border = BorderStroke(0.5.dp, borderColor),
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Official School Brand & Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_kotak),
                        contentDescription = "Logo Sulaone",
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "SMA ISLAM SULTAN AGUNG 1",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp,
                            color = Emerald800
                        )
                        Text(
                            text = cardTitle,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                SulaoneBadge(
                    text = "RESMI",
                    containerColor = if (isDark) Gold900.copy(alpha = 0.4f) else Gold50,
                    contentColor = if (isDark) Gold300 else Gold800
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = borderColor.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(14.dp))

            // Body: Avatar, Name, Verification, and ID details
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .sulaoneSharedElement(key = "student_avatar")
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Emerald700, Emerald900))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString(""),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Terverifikasi",
                            tint = Emerald600,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = idNumber,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    SulaoneBadge(
                        text = roleBadge,
                        containerColor = if (isDark) Emerald900.copy(alpha = 0.4f) else Emerald50,
                        contentColor = if (isDark) Emerald300 else Emerald800
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Footer: Digital Security Code Bar
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isDark) Slate850 else Slate100,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = null,
                            tint = Emerald700,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SMART CARD TOKEN • VERIFIED",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = Emerald800
                        )
                    }

                    Text(
                        text = "AKTIF 2026/2027",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = if (isDark) Slate400 else Slate500
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp)
    )
}

@Composable
fun ModernProfileMenuItem(
    icon: ImageVector? = null,
    iconTint: Color = Emerald700,
    iconBg: Color = Emerald50,
    drawableRes: Int? = null,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .sulaoneInteractiveTouchTarget(48.dp)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            if (drawableRes != null) {
                Image(
                    painter = painterResource(id = drawableRes),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Slate400,
            modifier = Modifier.size(18.dp)
        )
    }
}
