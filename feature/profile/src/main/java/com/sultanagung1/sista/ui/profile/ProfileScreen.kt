package com.sultanagung1.sista.ui.profile

import android.app.Activity
import com.sultanagung1.sista.feature.profile.R
import com.sultanagung1.sista.core.ui.text.UiText
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.accessibility.AppThemeMode
import com.sultanagung1.sista.core.accessibility.ThemeManager
import com.sultanagung1.sista.core.display.AdaptiveRefreshRateManager
import com.sultanagung1.sista.core.display.DisplayCapabilities
import com.sultanagung1.sista.core.display.RefreshRateMode
import com.sultanagung1.sista.core.motion.sulaoneSharedElement
import com.sultanagung1.sista.core.storage.SessionManager
import com.sultanagung1.sista.core.ui.component.Avatar
import com.sultanagung1.sista.core.ui.component.ButtonVariant
import com.sultanagung1.sista.core.ui.component.FilterChipRow
import com.sultanagung1.sista.core.ui.component.IconBadge
import com.sultanagung1.sista.core.ui.component.InlineBanner
import com.sultanagung1.sista.core.ui.component.SectionHeader
import com.sultanagung1.sista.core.ui.component.SistaButton
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SistaListItem
import com.sultanagung1.sista.core.ui.component.SistaTopBar
import com.sultanagung1.sista.core.ui.component.SkeletonBlock
import com.sultanagung1.sista.core.ui.component.StatusPill
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.data.model.MeProfile
import com.sultanagung1.sista.data.model.SchoolIdentity
import com.sultanagung1.sista.ui.navigation.LocalCapabilityState
import com.sultanagung1.sista.ui.navigation.Screen
import com.sultanagung1.sista.ui.navigation.canOpen

/**
 * FASE 76.6: why true black helps, without overselling. OLED pixels showing
 * #000000 are switched off; an LCD backlight stays on whatever the colour.
 */
internal val AMOLED_BATTERY_NOTE = R.string.profile_amoled_note

/** The refresh-rate row: what this phone's screen can do and what is chosen. */
data class DisplayChoice(val capabilities: DisplayCapabilities, val mode: RefreshRateMode)

/**
 * The signed-in account as the school records it (GET me), then the settings
 * of this phone. Every role sees the same page; which rows appear depends on
 * what the server sent and which routes this account may open.
 */
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    sessionManager: SessionManager,
    themeManager: ThemeManager,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    onNavigateBack: (() -> Unit)? = null,
) {
    val state by viewModel.uiState.collectAsState()
    val sessionName by sessionManager.userNameFlow.collectAsState(initial = null)
    val themeMode by themeManager.themeMode.collectAsState()
    val refreshMode by AdaptiveRefreshRateManager.getStoredModeFlow(sessionManager)
        .collectAsState(initial = RefreshRateMode.ADAPTIVE_SMOOTH)
    val context = LocalContext.current
    val capabilities = LocalCapabilityState.current
    val display = remember { AdaptiveRefreshRateManager.detectCapabilities(context) }
    val appVersion = remember {
        runCatching {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            val code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) info.longVersionCode else @Suppress("DEPRECATION") info.versionCode.toLong()
            appVersionLabel(info.versionName, code)
        }.getOrNull()
    }

    ProfileContent(
        state = state,
        sessionName = sessionName,
        themeMode = themeMode,
        display = DisplayChoice(display, refreshMode),
        appVersion = appVersion,
        canOpen = { capabilities.canOpen(it) },
        onRetry = viewModel::load,
        onNavigate = onNavigate,
        onThemeMode = themeManager::setThemeMode,
        onRefreshMode = { mode -> AdaptiveRefreshRateManager.saveMode(sessionManager, mode, context as? Activity) },
        onLogout = onLogout,
        onNavigateBack = onNavigateBack,
    )
}

/** The profile page without a ViewModel, for previews and screenshots. */
@Composable
fun ProfileContent(
    state: ProfileUiState,
    sessionName: String?,
    themeMode: AppThemeMode,
    display: DisplayChoice?,
    appVersion: UiText?,
    canOpen: (String) -> Boolean,
    onRetry: () -> Unit,
    onNavigate: (String) -> Unit,
    onThemeMode: (AppThemeMode) -> Unit,
    onRefreshMode: (RefreshRateMode) -> Unit,
    onLogout: () -> Unit,
    onNavigateBack: (() -> Unit)?,
) {
    var dialog by rememberSaveable { mutableStateOf<ProfileDialog?>(null) }
    ShellTheme {
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = { SistaTopBar(title = stringResource(R.string.profile_title), onBack = onNavigateBack, scrollBehavior = scrollBehavior) },
            containerColor = SistaTheme.colors.background,
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .testTag("profile_root"),
                contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, bottom = Spacing.xxl),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                item(key = "identity") { IdentityCard(state, sessionName, onRetry) }

                val account = listOfNotNull(
                    MenuEntry(Icons.Outlined.AccountCircle, R.string.profile_full, R.string.profile_full_hint, Screen.StudentProfileComprehensive.createRoute())
                        .takeIf { state.profile?.studentData != null },
                    MenuEntry(Icons.Outlined.Notifications, R.string.profile_notifications, R.string.profile_notifications_hint, Screen.NotificationCenter.route),
                    MenuEntry(Icons.Outlined.Campaign, R.string.profile_announcements, R.string.profile_announcements_hint, Screen.AnnouncementFeed.route),
                ).filter { canOpen(it.route) }
                menuSection("account", R.string.profile_section_account, account, onNavigate)

                val security = listOf(
                    MenuEntry(Icons.Outlined.Fingerprint, R.string.profile_biometric, R.string.profile_biometric_hint, Screen.FaceEnrollment.route),
                    MenuEntry(Icons.Outlined.Security, R.string.profile_device_security, R.string.profile_device_security_hint, Screen.SecuritySettings.route),
                ).filter { canOpen(it.route) }
                menuSection("security", R.string.profile_section_security, security, onNavigate)

                item(key = "display_header") { SectionHeader(stringResource(R.string.profile_section_display), Modifier.padding(top = Spacing.md)) }
                item(key = "display") {
                    SistaCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = Spacing.sm)) {
                        Row(
                            Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconBadge(Icons.Outlined.Palette, tone = StatusTone.Brand)
                            Spacer(Modifier.width(Spacing.lg))
                            Text(stringResource(R.string.profile_theme), style = SistaTheme.typography.bodyLarge)
                        }
                        // High contrast is set under Aksesibilitas; show it as chosen there.
                        val themeOptions = if (themeMode in PROFILE_THEMES) PROFILE_THEMES else PROFILE_THEMES + themeMode
                        val themeNames = themeOptions.associateWith { stringResource(themeLabel(it)) }
                        FilterChipRow(
                            options = themeOptions,
                            selected = themeMode,
                            onSelect = onThemeMode,
                            label = { themeNames.getValue(it) },
                            contentPadding = PaddingValues(horizontal = Spacing.lg),
                        )
                        if (themeMode == AppThemeMode.AMOLED_BLACK) {
                            Text(
                                stringResource(AMOLED_BATTERY_NOTE),
                                style = SistaTheme.typography.bodySmall,
                                color = SistaTheme.colors.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                            )
                        }
                        if (display != null) {
                            HorizontalDivider(Modifier.padding(top = Spacing.sm), color = SistaTheme.colors.outlineVariant)
                            MenuRow(
                                MenuEntry(Icons.Outlined.Speed, R.string.profile_refresh_rate, null, "", subtitleText = UiText.Raw("${display.capabilities.maxSupportedRefreshRate.toInt()} Hz · ${stringResource(refreshModeLabel(display.mode))}")),
                                onClick = { dialog = ProfileDialog.RefreshRate },
                            )
                        }
                        if (canOpen(Screen.Settings.route)) {
                            HorizontalDivider(color = SistaTheme.colors.outlineVariant)
                            MenuRow(
                                MenuEntry(Icons.Outlined.Tune, R.string.profile_language_text, R.string.profile_language_text_hint, Screen.Settings.route),
                                onClick = { onNavigate(Screen.Settings.route) },
                            )
                        }
                    }
                }

                val help = listOf(
                    MenuEntry(Icons.Outlined.SystemUpdate, R.string.profile_update, R.string.profile_update_check, Screen.InAppUpdate.route, subtitleText = appVersion?.let { UiText.Res(R.string.profile_update_installed, it) }),
                    MenuEntry(Icons.Outlined.BugReport, R.string.profile_diagnostics, R.string.profile_diagnostics_hint, Screen.DiagnosticReport.route),
                ).filter { canOpen(it.route) }
                item(key = "help_header") { SectionHeader(stringResource(R.string.profile_section_help), Modifier.padding(top = Spacing.md)) }
                item(key = "help") {
                    SistaCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = Spacing.xs)) {
                        help.forEach { entry ->
                            MenuRow(entry, onClick = { onNavigate(entry.route) })
                            HorizontalDivider(color = SistaTheme.colors.outlineVariant)
                        }
                        MenuRow(
                            MenuEntry(Icons.Outlined.Info, R.string.profile_about, R.string.profile_about_hint, "", subtitleText = state.school?.name?.let { UiText.Raw(it) }),
                            onClick = { dialog = ProfileDialog.About },
                        )
                    }
                }

                item(key = "logout") {
                    SistaButton(
                        text = stringResource(R.string.profile_logout),
                        onClick = { dialog = ProfileDialog.Logout },
                        variant = ButtonVariant.Outlined,
                        leadingIcon = Icons.AutoMirrored.Outlined.Logout,
                        fullWidth = true,
                        modifier = Modifier.padding(top = Spacing.xl),
                    )
                }
            }
        }

        when (dialog) {
            ProfileDialog.Logout -> LogoutDialog(onConfirm = { dialog = null; onLogout() }, onDismiss = { dialog = null })
            ProfileDialog.About -> AboutDialog(state.school, appVersion, onDismiss = { dialog = null })
            ProfileDialog.RefreshRate -> if (display != null) {
                RefreshRateDialog(display, onSelect = { onRefreshMode(it); dialog = null }, onDismiss = { dialog = null })
            }
            null -> Unit
        }
    }
}

private enum class ProfileDialog { Logout, About, RefreshRate }

private data class MenuEntry(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    @StringRes val title: Int,
    @StringRes val subtitle: Int?,
    val route: String,
    /** Overrides [subtitle] with live data (installed version, school name, screen rate). */
    val subtitleText: UiText? = null,
)

private fun androidx.compose.foundation.lazy.LazyListScope.menuSection(
    key: String,
    @StringRes title: Int,
    entries: List<MenuEntry>,
    onNavigate: (String) -> Unit,
) {
    if (entries.isEmpty()) return
    item(key = "${key}_header") { SectionHeader(stringResource(title), Modifier.padding(top = Spacing.md)) }
    item(key = key) {
        SistaCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = Spacing.xs)) {
            entries.forEachIndexed { index, entry ->
                if (index > 0) HorizontalDivider(color = SistaTheme.colors.outlineVariant)
                MenuRow(entry, onClick = { onNavigate(entry.route) })
            }
        }
    }
}

@Composable
private fun MenuRow(entry: MenuEntry, onClick: () -> Unit) {
    SistaListItem(
        headline = stringResource(entry.title),
        supporting = entry.subtitleText?.asString() ?: entry.subtitle?.let { stringResource(it) },
        leading = { IconBadge(entry.icon, tone = StatusTone.Neutral) },
        trailing = { Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = SistaTheme.colors.onSurfaceVariant) },
        onClick = onClick,
    )
}

/**
 * Name, role and the school's own numbers for this account. While the server
 * answers, the name the app got at sign-in is shown with placeholders; when it
 * can't answer, the card says so instead of filling in sample numbers.
 */
@Composable
private fun IdentityCard(state: ProfileUiState, sessionName: String?, onRetry: () -> Unit) {
    val profile = state.profile
    val name = profile?.name?.takeIf { it.isNotBlank() } ?: sessionName?.takeIf { it.isNotBlank() }
    SistaCard(modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(name ?: "?", size = 64.dp, modifier = Modifier.sulaoneSharedElement(key = "student_avatar"))
            Spacer(Modifier.width(Spacing.lg))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                if (name != null) {
                    Text(name, style = SistaTheme.typography.titleLarge, maxLines = 2)
                } else {
                    SkeletonBlock(width = 160.dp, height = 20.dp)
                }
                when {
                    profile?.roleLabel != null -> StatusPill(profile.roleLabel!!, StatusTone.Brand)
                    state.isLoading -> SkeletonBlock(width = 80.dp, height = 20.dp)
                }
                academicYearLabel(profile?.academicYear)?.let {
                    Text(it.asString(), style = SistaTheme.typography.bodySmall, color = SistaTheme.colors.onSurfaceVariant)
                }
            }
        }
        when {
            profile != null -> IdentityDetails(profile)
            state.errorMessage != null -> InlineBanner(
                title = stringResource(R.string.profile_load_failed),
                message = state.errorMessage,
                tone = StatusTone.Warning,
                actionLabel = stringResource(com.sultanagung1.sista.core.ui.R.string.core_retry),
                onAction = onRetry,
                modifier = Modifier.padding(top = Spacing.lg),
            )
            else -> Column(Modifier.padding(top = Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                repeat(3) { SkeletonBlock() }
            }
        }
    }
}

@Composable
private fun IdentityDetails(profile: MeProfile) {
    val rows = identityRows(profile)
    if (rows.isEmpty() && !hasNoLinkedChildren(profile)) return
    HorizontalDivider(Modifier.padding(vertical = Spacing.lg), color = SistaTheme.colors.outlineVariant)
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        rows.forEach { InfoLine(it) }
        if (hasNoLinkedChildren(profile)) {
            Text(
                stringResource(R.string.profile_no_children),
                style = SistaTheme.typography.bodyMedium,
                color = SistaTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun InfoLine(row: InfoRow) {
    Row(Modifier.fillMaxWidth()) {
        Text(
            stringResource(row.label),
            style = SistaTheme.typography.bodyMedium,
            color = SistaTheme.colors.onSurfaceVariant,
            modifier = Modifier.width(112.dp),
        )
        Text(row.value, style = SistaTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun LogoutDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null) },
        title = { Text(stringResource(R.string.profile_logout_title)) },
        text = { Text(stringResource(R.string.profile_logout_body)) },
        confirmButton = { SistaButton(stringResource(R.string.profile_logout), onConfirm, variant = ButtonVariant.Danger) },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.profile_cancel)) } },
    )
}

/** The school as the server records it, and the installed app's real version. */
@Composable
private fun AboutDialog(school: SchoolIdentity?, appVersion: UiText?, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(school?.name ?: stringResource(R.string.profile_about)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(
                    stringResource(R.string.profile_about_app),
                    style = SistaTheme.typography.bodyMedium,
                )
                schoolRows(school).forEach { InfoLine(it) }
                if (school == null) {
                    Text(
                        stringResource(R.string.profile_school_unavailable),
                        style = SistaTheme.typography.bodySmall,
                        color = SistaTheme.colors.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(Spacing.xs))
                InfoLine(InfoRow(R.string.profile_app_version, appVersion?.asString() ?: MISSING))
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.profile_close)) } },
    )
}

@Composable
private fun RefreshRateDialog(display: DisplayChoice, onSelect: (RefreshRateMode) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.profile_refresh_rate)) },
        text = {
            Column {
                Text(
                    stringResource(R.string.profile_screen_rates, display.capabilities.supportedRefreshRates.joinToString(", ") { "${it.toInt()} Hz" }),
                    style = SistaTheme.typography.bodyMedium,
                    color = SistaTheme.colors.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = Spacing.sm),
                )
                RefreshRateMode.entries.forEach { mode ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(selected = mode == display.mode, role = Role.RadioButton, onClick = { onSelect(mode) })
                            .padding(vertical = Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = mode == display.mode, onClick = null)
                        Spacer(Modifier.width(Spacing.md))
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(refreshModeLabel(mode)), style = SistaTheme.typography.bodyLarge)
                            Text(stringResource(refreshModeDescription(mode)), style = SistaTheme.typography.bodySmall, color = SistaTheme.colors.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.profile_close)) } },
    )
}

@StringRes
internal fun refreshModeLabel(mode: RefreshRateMode): Int = when (mode) {
    RefreshRateMode.ADAPTIVE_SMOOTH -> R.string.profile_rate_adaptive
    RefreshRateMode.POWER_SAVER_60HZ -> R.string.profile_rate_power_saver
    RefreshRateMode.SYSTEM_DEFAULT -> R.string.profile_rate_system
}

@StringRes
internal fun refreshModeDescription(mode: RefreshRateMode): Int = when (mode) {
    RefreshRateMode.ADAPTIVE_SMOOTH -> R.string.profile_rate_desc_adaptive
    RefreshRateMode.POWER_SAVER_60HZ -> R.string.profile_rate_desc_power_saver
    RefreshRateMode.SYSTEM_DEFAULT -> R.string.profile_rate_desc_system
}
