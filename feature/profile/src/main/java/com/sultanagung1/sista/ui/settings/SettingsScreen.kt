package com.sultanagung1.sista.ui.settings

import com.sultanagung1.sista.feature.profile.R
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import com.sultanagung1.sista.core.accessibility.AppLanguage
import com.sultanagung1.sista.core.accessibility.AppThemeMode
import com.sultanagung1.sista.core.ui.component.IconBadge
import com.sultanagung1.sista.core.ui.component.SectionHeader
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SistaListItem
import com.sultanagung1.sista.core.ui.component.SistaTopBar
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.ui.navigation.LocalCapabilityState
import com.sultanagung1.sista.ui.navigation.Screen
import com.sultanagung1.sista.ui.navigation.canOpen
import com.sultanagung1.sista.ui.profile.themeLabel

/** Settings of this phone: theme, language, accessibility. Saved on the phone, for this app only. */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToLanguage: () -> Unit,
    onNavigateToAccessibility: () -> Unit,
    onNavigateToBiometrics: () -> Unit,
    onNavigateToSecurity: () -> Unit = {},
    onNavigateBack: () -> Unit,
) {
    val theme by viewModel.currentTheme.collectAsState()
    val language by viewModel.currentLanguage.collectAsState()
    val capabilities = LocalCapabilityState.current
    SettingsContent(
        theme = theme,
        language = language,
        canOpen = { capabilities.canOpen(it) },
        onTheme = viewModel::setTheme,
        onLanguage = onNavigateToLanguage,
        onAccessibility = onNavigateToAccessibility,
        onBiometrics = onNavigateToBiometrics,
        onSecurity = onNavigateToSecurity,
        onNavigateBack = onNavigateBack,
    )
}

/** Themes in the order they are offered; high contrast is a theme of its own too. */
internal val SETTINGS_THEMES = listOf(
    AppThemeMode.SYSTEM, AppThemeMode.LIGHT, AppThemeMode.DARK, AppThemeMode.AMOLED_BLACK, AppThemeMode.HIGH_CONTRAST,
)

@StringRes
internal fun themeHint(mode: AppThemeMode): Int = when (mode) {
    AppThemeMode.SYSTEM -> R.string.theme_hint_system
    AppThemeMode.LIGHT -> R.string.theme_hint_light
    AppThemeMode.DARK -> R.string.theme_hint_dark
    AppThemeMode.AMOLED_BLACK -> R.string.theme_hint_amoled
    AppThemeMode.HIGH_CONTRAST -> R.string.theme_hint_high_contrast
}

/** The settings page without a ViewModel, for previews and screenshots. */
@Composable
fun SettingsContent(
    theme: AppThemeMode,
    language: AppLanguage,
    canOpen: (String) -> Boolean,
    onTheme: (AppThemeMode) -> Unit,
    onLanguage: () -> Unit,
    onAccessibility: () -> Unit,
    onBiometrics: () -> Unit,
    onSecurity: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    ShellTheme {
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = { SistaTopBar(title = stringResource(R.string.settings_title), onBack = onNavigateBack, scrollBehavior = scrollBehavior) },
            containerColor = SistaTheme.colors.background,
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .testTag("settings_root"),
                contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, bottom = Spacing.xxl),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                item(key = "theme_header") { SectionHeader(stringResource(R.string.settings_theme)) }
                item(key = "theme") {
                    SistaCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = Spacing.xs)) {
                        SETTINGS_THEMES.forEach { mode ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .selectable(selected = mode == theme, role = Role.RadioButton, onClick = { onTheme(mode) })
                                    .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(selected = mode == theme, onClick = null)
                                Spacer(Modifier.width(Spacing.md))
                                Column(Modifier.weight(1f)) {
                                    Text(stringResource(themeLabel(mode)), style = SistaTheme.typography.bodyLarge)
                                    Text(stringResource(themeHint(mode)), style = SistaTheme.typography.bodySmall, color = SistaTheme.colors.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                item(key = "general_header") { SectionHeader(stringResource(R.string.settings_language_accessibility), Modifier.padding(top = Spacing.md)) }
                item(key = "general") {
                    SistaCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = Spacing.xs)) {
                        SettingsRow(Icons.Outlined.Translate, stringResource(R.string.settings_language), language.nativeName, onLanguage)
                        HorizontalDivider(color = SistaTheme.colors.outlineVariant)
                        SettingsRow(Icons.Outlined.AccessibilityNew, stringResource(R.string.settings_accessibility), stringResource(R.string.settings_accessibility_hint), onAccessibility)
                    }
                }

                val security = listOfNotNull(
                    Triple(Icons.Outlined.Fingerprint, R.string.sec_biometric, onBiometrics).takeIf { canOpen(Screen.FaceEnrollment.route) },
                    Triple(Icons.Outlined.Security, R.string.sec_title, onSecurity).takeIf { canOpen(Screen.SecuritySettings.route) },
                )
                if (security.isNotEmpty()) {
                    item(key = "security_header") { SectionHeader(stringResource(R.string.settings_security), Modifier.padding(top = Spacing.md)) }
                    item(key = "security") {
                        SistaCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = Spacing.xs)) {
                            security.forEachIndexed { index, (icon, title, onClick) ->
                                if (index > 0) HorizontalDivider(color = SistaTheme.colors.outlineVariant)
                                SettingsRow(icon, stringResource(title), null, onClick)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, title: String, subtitle: String?, onClick: () -> Unit) {
    SistaListItem(
        headline = title,
        supporting = subtitle,
        leading = { IconBadge(icon, tone = StatusTone.Neutral) },
        trailing = { Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = SistaTheme.colors.onSurfaceVariant) },
        onClick = onClick,
    )
}
