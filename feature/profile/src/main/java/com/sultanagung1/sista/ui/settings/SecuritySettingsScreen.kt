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
import androidx.compose.material.icons.outlined.ChevronRight
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
import com.sultanagung1.sista.ui.navigation.LocalCapabilityState
import com.sultanagung1.sista.ui.navigation.Screen
import com.sultanagung1.sista.ui.navigation.canOpen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * What this phone's own checks found. The biometric lock is set on its own
 * page (FaceEnrollment), where it is saved; this page used to carry a second
 * switch that only changed local state and came back on every visit.
 */
@Composable
fun SecuritySettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigate: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val capabilities = LocalCapabilityState.current
    var report by remember { mutableStateOf<DeviceIntegrityReport?>(null) }
    LaunchedEffect(Unit) {
        report = withContext(Dispatchers.Default) { DeviceIntegrityChecker(context).checkIntegrity() }
    }
    SecuritySettingsContent(
        report = report,
        canOpenBiometrics = capabilities.canOpen(Screen.FaceEnrollment.route),
        onOpenBiometrics = { onNavigate(Screen.FaceEnrollment.route) },
        onNavigateBack = onNavigateBack,
    )
}

/** The security page without the checker, for previews and screenshots. [report] null = still checking. */
@Composable
fun SecuritySettingsContent(
    report: DeviceIntegrityReport?,
    canOpenBiometrics: Boolean,
    onOpenBiometrics: () -> Unit,
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
                if (canOpenBiometrics) {
                    item(key = "biometrics") {
                        SistaCard(modifier = Modifier.fillMaxWidth().padding(top = Spacing.md), contentPadding = PaddingValues(vertical = Spacing.xs)) {
                            SistaListItem(
                                headline = stringResource(R.string.sec_biometric),
                                supporting = stringResource(R.string.sec_biometric_hint),
                                leading = { IconBadge(Icons.Outlined.Fingerprint, tone = StatusTone.Neutral) },
                                trailing = { Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = SistaTheme.colors.onSurfaceVariant) },
                                onClick = onOpenBiometrics,
                            )
                        }
                    }
                }
            }
        }
    }
}
