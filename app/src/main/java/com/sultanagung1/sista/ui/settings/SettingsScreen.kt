package com.sultanagung1.sista.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.accessibility.AppThemeMode
import com.sultanagung1.sista.core.accessibility.LocalAppStrings
import com.sultanagung1.sista.core.designsystem.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToLanguage: () -> Unit,
    onNavigateToAccessibility: () -> Unit,
    onNavigateToBiometrics: () -> Unit,
    onNavigateToSecurity: () -> Unit = {},
    onNavigateBack: () -> Unit
) {
    val currentTheme by viewModel.currentTheme.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val strings = LocalAppStrings.current

    var showThemeDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = strings.settingsTitle,
                subtitle = "Preferensi Bahasa, Tema & Aksesibilitas",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Theme Selector Direct Card
            Text(
                text = "TEMA WARNA APLIKASI",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
            )

            SulaoneCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Mode Tampilan Aplikasi",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Perubahan tema langsung aktif seketika dengan transisi halus",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 3-Pill Quick Segmented Buttons: Terang, Gelap, Sistem
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val themeOptions = listOf(
                        Triple(AppThemeMode.LIGHT, "Terang", Icons.Default.LightMode),
                        Triple(AppThemeMode.DARK, "Gelap", Icons.Default.DarkMode),
                        Triple(AppThemeMode.SYSTEM, "Sistem", Icons.Default.BrightnessAuto)
                    )

                    themeOptions.forEach { (mode, label, icon) ->
                        val isSelected = currentTheme == mode
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.setTheme(mode)
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Emerald700 else MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Gold400 else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium),
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Extended Palette Options Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showThemeDialog = true }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tema Aktif: ${currentTheme.title}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Opsi Lainnya",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // General Preferences
            Text(
                text = "BAHASA & AKSESIBILITAS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
            )

            SulaoneCard(modifier = Modifier.fillMaxWidth()) {
                SettingsItem(
                    icon = Icons.Default.Translate,
                    title = strings.languageTitle,
                    subtitle = "${currentLanguage.title} (${currentLanguage.nativeName})",
                    onClick = onNavigateToLanguage
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                SettingsItem(
                    icon = Icons.Default.AccessibilityNew,
                    title = strings.accessibilityTitle,
                    subtitle = "Ukuran teks, ramah disleksia & kontras tinggi",
                    onClick = onNavigateToAccessibility
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Security & Hardware
            Text(
                text = "KEAMANAN & HARDWARE",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
            )

            SulaoneCard(modifier = Modifier.fillMaxWidth()) {
                SettingsItem(
                    icon = Icons.Default.Fingerprint,
                    title = "Keamanan Sidik Jari (m-Banking)",
                    subtitle = "Kunci aplikasi & otentikasi Keystore Vault",
                    onClick = onNavigateToBiometrics
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

                SettingsItem(
                    icon = Icons.Default.Security,
                    title = "Keamanan & Integritas Perangkat",
                    subtitle = "Audit Root, Anti-Cheat CBT, SHA-256 APK Hash",
                    onClick = onNavigateToSecurity
                )
            }
        }
    }

    // Theme Picker Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = {
                Text("Pilih Tema Aplikasi", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    AppThemeMode.entries.forEach { mode ->
                        val isSelected = mode == currentTheme
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    viewModel.setTheme(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mode.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Emerald700 else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = mode.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Emerald700)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Slate400)
    }
}
