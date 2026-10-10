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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import com.sultanagung1.sista.core.security.DeviceIntegrityChecker
import com.sultanagung1.sista.core.security.DeviceIntegrityReport
import com.sultanagung1.sista.core.ui.component.IconBadge
import com.sultanagung1.sista.core.ui.component.SectionHeader
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SistaListItem
import com.sultanagung1.sista.core.ui.component.SistaTopBar
import com.sultanagung1.sista.core.ui.component.SkeletonList
import com.sultanagung1.sista.core.ui.component.StatusPill
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.collectAsState
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.ui.graphics.vector.ImageVector
import com.sultanagung1.sista.core.security.BiometricAvailability
import com.sultanagung1.sista.core.ui.component.InlineBanner

/**
 * What this phone's own checks found, and the fingerprint settings: signing in
 * with the fingerprint (registered on the server) and asking for it before a
 * report card or a CBT exam opens.
 */
@Composable
fun SecuritySettingsScreen(
    viewModel: BiometricSettingsViewModel,
    onNavigateBack: () -> Unit,
) {
    val context = LocalContext.current
    val biometric by viewModel.uiState.collectAsState()
    var report by remember { mutableStateOf<DeviceIntegrityReport?>(null) }
    LaunchedEffect(Unit) {
        viewModel.checkDevice(context)
        report = withContext(Dispatchers.Default) { DeviceIntegrityChecker(context).checkIntegrity() }
    }
    SecuritySettingsContent(
        report = report,
        biometric = biometric,
        onLoginChange = viewModel::setLoginEnabled,
        onProtectionChange = viewModel::setSensitiveProtection,
        onNavigateBack = onNavigateBack,
    )
}

/** The security page without the checker, for previews and screenshots. [report] null = still checking. */
@Composable
fun SecuritySettingsContent(
    report: DeviceIntegrityReport?,
    biometric: BiometricSettingsUiState,
    onLoginChange: (Boolean) -> Unit,
    onProtectionChange: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
) {
    ShellTheme {
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = { SistaTopBar(title = stringResource(R.string.sec_title), onBack = onNavigateBack, scrollBehavior = scrollBehavior) },
            containerColor = SistaTheme.colors.background,
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .testTag("security_root"),
                contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, bottom = Spacing.xxl),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                if (report == null) {
                    item(key = "loading") { SkeletonList(rows = 4, modifier = Modifier.padding(top = Spacing.sm)) }
                    return@LazyColumn
                }
                item(key = "verdict") {
                    val (verdictRes, tone) = securityVerdict(report)
                    val verdict = stringResource(verdictRes)
                    SistaCard(modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.sec_this_phone), style = SistaTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            StatusPill(verdict, tone)
                        }
                        Text(
                            stringResource(R.string.sec_checked_hint),
                            style = SistaTheme.typography.bodyMedium,
                            color = SistaTheme.colors.onSurfaceVariant,
                            modifier = Modifier.padding(top = Spacing.sm),
                        )
                    }
                }
                item(key = "checks_header") { SectionHeader(stringResource(R.string.sec_checks), Modifier.padding(top = Spacing.md)) }
                items(securityChecks(report), key = { it.title }) { check ->
                    SistaCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(Spacing.lg)) {
                        Row(verticalAlignment = Alignment.Top) {
                            IconBadge(
                                icon = when (check.tone) {
                                    StatusTone.Success -> Icons.Outlined.CheckCircle
                                    StatusTone.Warning -> Icons.Outlined.WarningAmber
                                    else -> Icons.Outlined.ErrorOutline
                                },
                                tone = check.tone,
                            )
                            Spacer(Modifier.width(Spacing.lg))
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                                Text(stringResource(check.title), style = SistaTheme.typography.titleSmall)
                                Text(stringResource(check.detail), style = SistaTheme.typography.bodyMedium, color = SistaTheme.colors.onSurfaceVariant)
                            }
                        }
                    }
                }
                item(key = "signature") {
                    SistaCard(modifier = Modifier.fillMaxWidth().padding(top = Spacing.md)) {
                        Text(stringResource(R.string.sec_signature), style = SistaTheme.typography.titleSmall)
                        Text(
                            stringResource(R.string.sec_signature_hint),
                            style = SistaTheme.typography.bodySmall,
                            color = SistaTheme.colors.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = Spacing.sm),
                        )
                        val signature = signatureLabel(report.signatureHash)
                        if (signature != null) {
                            SelectionContainer {
                                Text(signature, style = SistaTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace))
                            }
                        } else {
                            Text(stringResource(R.string.sec_signature_unreadable), style = SistaTheme.typography.bodyMedium)
                        }
                    }
                }
                item(key = "biometric_header") { SectionHeader(stringResource(R.string.bio_title), Modifier.padding(top = Spacing.md)) }
                item(key = "biometric") { BiometricCard(biometric, onLoginChange, onProtectionChange) }
            }
        }
    }
}

@Composable
private fun BiometricCard(
    state: BiometricSettingsUiState,
    onLoginChange: (Boolean) -> Unit,
    onProtectionChange: (Boolean) -> Unit,
) {
    val ready = state.availability == BiometricAvailability.AVAILABLE
    SistaCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = Spacing.xs)) {
        state.availability?.takeIf { !ready }?.let {
            InlineBanner(
                message = stringResource(biometricAvailabilityText(it)),
                tone = StatusTone.Warning,
                modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
            )
        }
        BiometricSwitch(
            icon = Icons.Outlined.Fingerprint,
            title = stringResource(R.string.bio_login),
            hint = stringResource(R.string.bio_login_hint),
            checked = state.isLoginEnabled,
            enabled = ready && !state.isSaving,
            onChange = onLoginChange,
        )
        HorizontalDivider(color = SistaTheme.colors.outlineVariant)
        BiometricSwitch(
            icon = Icons.Outlined.Lock,
            title = stringResource(R.string.bio_protection),
            hint = stringResource(R.string.bio_protection_hint),
            checked = state.isSensitiveProtectionEnabled,
            enabled = ready,
            onChange = onProtectionChange,
        )
        state.message?.let {
            InlineBanner(
                message = it.asString(),
                tone = if (state.messageIsError) StatusTone.Danger else StatusTone.Success,
                modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
            )
        }
        Text(
            stringResource(R.string.bio_privacy),
            style = SistaTheme.typography.bodySmall,
            color = SistaTheme.colors.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        )
    }
}

@Composable
private fun BiometricSwitch(icon: ImageVector, title: String, hint: String, checked: Boolean, enabled: Boolean, onChange: (Boolean) -> Unit) {
    SistaListItem(
        headline = title,
        supporting = hint,
        leading = { IconBadge(icon, tone = StatusTone.Neutral) },
        trailing = { Switch(checked = checked, onCheckedChange = null, enabled = enabled) },
        onClick = if (enabled) ({ onChange(!checked) }) else null,
    )
}

@StringRes
internal fun biometricAvailabilityText(availability: BiometricAvailability): Int = when (availability) {
    BiometricAvailability.AVAILABLE -> R.string.bio_ready
    BiometricAvailability.NOT_ENROLLED -> R.string.bio_not_enrolled
    BiometricAvailability.NO_HARDWARE -> R.string.bio_no_hardware
    BiometricAvailability.SECURITY_UPDATE_REQUIRED -> R.string.bio_update_required
}
