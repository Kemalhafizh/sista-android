package com.sultanagung1.sista.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CoPresent
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.designsystem.SulaonePullToRefreshBox
import com.sultanagung1.sista.core.ui.component.ButtonVariant
import com.sultanagung1.sista.core.ui.component.EmptyState
import com.sultanagung1.sista.core.ui.component.ErrorState
import com.sultanagung1.sista.core.ui.component.FeatureTile
import com.sultanagung1.sista.core.ui.component.GreetingHeader
import com.sultanagung1.sista.core.ui.component.IconBadge
import com.sultanagung1.sista.core.ui.component.InlineBanner
import com.sultanagung1.sista.core.ui.component.SectionHeader
import com.sultanagung1.sista.core.ui.component.SistaButton
import com.sultanagung1.sista.core.ui.component.SistaCard
import com.sultanagung1.sista.core.ui.component.SkeletonList
import com.sultanagung1.sista.core.ui.component.StatTile
import com.sultanagung1.sista.core.ui.component.StatusPill
import com.sultanagung1.sista.core.ui.component.greetingFor
import com.sultanagung1.sista.core.ui.theme.ShellTheme
import com.sultanagung1.sista.core.ui.theme.SistaTheme
import com.sultanagung1.sista.core.ui.theme.Spacing
import com.sultanagung1.sista.core.ui.theme.StatusTone
import com.sultanagung1.sista.core.util.DateUtils
import com.sultanagung1.sista.data.model.PendingApprovalItem
import com.sultanagung1.sista.data.model.SchoolKpiSummary
import com.sultanagung1.sista.ui.navigation.LocalCapabilityState
import com.sultanagung1.sista.ui.navigation.Screen
import com.sultanagung1.sista.ui.navigation.canOpen
import java.util.Calendar

/** An admin shortcut. Shown only when the account may open [route]. */
@Immutable
data class AdminShortcut(val title: String, val icon: ImageVector, val route: String)

val ADMIN_SHORTCUTS = listOf(
    AdminShortcut("Analitik eksekutif", Icons.Outlined.Insights, Screen.ExecutiveAnalytics.route),
    AdminShortcut("Sesi kelas", Icons.Outlined.CoPresent, Screen.AdminSessionManagement.route),
    AdminShortcut("Pengumuman", Icons.Outlined.Campaign, Screen.AnnouncementFeed.route),
    AdminShortcut("Evaluasi guru", Icons.Outlined.RateReview, Screen.TeacherEvaluation.route),
    AdminShortcut("Tata tertib", Icons.Outlined.Gavel, Screen.Discipline.createRoute()),
    AdminShortcut("Modul sekolah", Icons.AutoMirrored.Outlined.Assignment, Screen.EnterpriseCatalog.route),
)

/** A decision the user is about to confirm. */
private data class PendingDecision(val item: PendingApprovalItem, val approve: Boolean)

/**
 * The admin and leadership Beranda — the same layout as every role's home:
 * who is signed in, the school's numbers today, the requests waiting on this
 * account's step, finance and exam figures, the emergency broadcast, and the
 * shortcuts this account may use.
 */
@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    onNavigateRoute: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val capabilities = LocalCapabilityState.current

    var refreshRequested by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.isLoading) { if (!uiState.isLoading) refreshRequested = false }
    val hour = remember(uiState.isLoading) { DateUtils.nowCalendar().get(Calendar.HOUR_OF_DAY) }

    AdminHomeContent(
        greeting = greetingFor(hour),
        state = uiState,
        nowMillis = DateUtils.nowMillis(),
        shortcuts = ADMIN_SHORTCUTS.filter { capabilities.canOpen(it.route) },
        refreshing = refreshRequested && uiState.isLoading,
        onRefresh = {
            refreshRequested = true
            viewModel.refresh()
        },
        onDecide = viewModel::processApproval,
        onDismissOutcome = viewModel::dismissApprovalOutcome,
        onRetryApprovals = viewModel::loadPendingApprovals,
        onBroadcast = viewModel::broadcastEmergency,
        onDismissBroadcast = viewModel::dismissEmergencyBroadcastConfirmation,
        onOpenRoute = onNavigateRoute,
    )
}

/** The admin home without a ViewModel, for previews and screenshots. */
@Composable
fun AdminHomeContent(
    greeting: String,
    state: AdminUiState,
    nowMillis: Long,
    shortcuts: List<AdminShortcut>,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    onDecide: (id: Long, action: String, notes: String?) -> Unit,
    onDismissOutcome: () -> Unit,
    onRetryApprovals: () -> Unit,
    onBroadcast: (title: String, message: String, location: String?) -> Unit,
    onDismissBroadcast: () -> Unit,
    onOpenRoute: (String) -> Unit,
) {
    var decision by remember { mutableStateOf<PendingDecision?>(null) }
    var composingBroadcast by rememberSaveable { mutableStateOf(false) }

    ShellTheme {
        Surface(color = SistaTheme.colors.background) {
            SulaonePullToRefreshBox(
                isRefreshing = refreshing,
                onRefresh = onRefresh,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("admin_home_root"),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = Spacing.xxl),
                    verticalArrangement = Arrangement.spacedBy(Spacing.lg),
                ) {
                    item(key = "header") {
                        GreetingHeader(
                            greeting = greeting,
                            name = state.principalName?.takeIf { it.isNotBlank() } ?: "Pimpinan",
                            details = listOf("Dasbor sekolah"),
                            unreadCount = 0,
                            onOpenNotifications = { onOpenRoute(Screen.NotificationCenter.route) },
                        )
                    }
                    item(key = "stats") { HeadlineStats(state, onRefresh) }
                    state.approvalOutcome?.let { outcome ->
                        item(key = "outcome") {
                            InlineBanner(
                                message = outcome.message,
                                tone = if (outcome.succeeded) StatusTone.Success else StatusTone.Danger,
                                onDismiss = onDismissOutcome,
                                modifier = Modifier.padding(horizontal = Spacing.screen),
                            )
                        }
                    }
                    item(key = "approvals_header") {
                        SectionHeader(
                            "Menunggu keputusan Anda",
                            modifier = Modifier.padding(horizontal = Spacing.screen),
                        )
                    }
                    when {
                        state.isLoadingApprovals && state.pendingApprovals.isEmpty() -> item(key = "approvals_loading") {
                            SkeletonList(rows = 2)
                        }
                        state.approvalsError != null && state.pendingApprovals.isEmpty() -> item(key = "approvals_error") {
                            ErrorState(
                                title = "Daftar persetujuan belum bisa dimuat",
                                body = state.approvalsError,
                                onRetry = onRetryApprovals,
                                modifier = Modifier.padding(horizontal = Spacing.screen),
                            )
                        }
                        state.pendingApprovals.isEmpty() -> item(key = "approvals_empty") {
                            EmptyState(
                                title = "Tidak ada pengajuan yang menunggu Anda",
                                body = "Pengajuan muncul di sini saat tiba di langkah persetujuan Anda.",
                                icon = Icons.Outlined.TaskAlt,
                                modifier = Modifier.padding(horizontal = Spacing.screen),
                            )
                        }
                        else -> items(state.pendingApprovals, key = { "approval_${it.id}" }, contentType = { "approval" }) { item ->
                            ApprovalCard(
                                item = item,
                                nowMillis = nowMillis,
                                processing = state.processingApprovalId == item.id,
                                enabled = state.processingApprovalId == null,
                                onApprove = { decision = PendingDecision(item, approve = true) },
                                onReject = { decision = PendingDecision(item, approve = false) },
                                modifier = Modifier.padding(horizontal = Spacing.screen),
                            )
                        }
                    }
                    item(key = "kpi") { SchoolFigures(state.schoolKpi, state.kpiError) }
                    item(key = "broadcast") {
                        BroadcastCard(sending = state.isSendingEmergencyBroadcast, onCompose = { composingBroadcast = true })
                    }
                    if (shortcuts.isNotEmpty()) {
                        item(key = "shortcuts") { Shortcuts(shortcuts, onOpenRoute) }
                    }
                }
            }
        }

        decision?.let { pending ->
            DecisionDialog(
                item = pending.item,
                approve = pending.approve,
                onConfirm = { notes ->
                    onDecide(pending.item.id, if (pending.approve) "approve" else "reject", notes)
                    decision = null
                },
                onDismiss = { decision = null },
            )
        }
        if (composingBroadcast || state.emergencyBroadcastSent != null || state.broadcastError != null) {
            EmergencyBroadcastDialog(
                sending = state.isSendingEmergencyBroadcast,
                sent = state.emergencyBroadcastSent,
                error = state.broadcastError,
                onSend = onBroadcast,
                onDismiss = {
                    composingBroadcast = false
                    onDismissBroadcast()
                },
            )
        }
    }
}

@Composable
private fun HeadlineStats(state: AdminUiState, onRetry: () -> Unit) {
    val data = state.dashboardData
    // "–" before the numbers arrive or when they failed: never a 0 that reads as a fact.
    val known = data != null
    Column(Modifier.padding(horizontal = Spacing.screen), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        if (state.errorMessage != null && data == null) {
            InlineBanner(
                message = "Angka sekolah belum bisa dimuat. ${state.errorMessage}",
                tone = StatusTone.Warning,
                actionLabel = "Coba lagi",
                onAction = onRetry,
            )
        }
        Row(Modifier.height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            StatTile(
                label = "Siswa aktif",
                value = data?.totalStudents?.let(::thousands) ?: "–",
                icon = Icons.Outlined.Groups,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            StatTile(
                label = "Kehadiran hari ini",
                value = data?.attendanceRateToday?.let { "${decimal(it)}%" } ?: "–",
                supporting = if (known && data?.attendanceRateToday == null) "belum ada presensi" else "dari presensi tercatat",
                icon = Icons.Outlined.EventAvailable,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }
        Row(Modifier.height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            StatTile(
                label = "Tunggakan",
                value = data?.let { rupiahShort(it.unpaidBillingsTotal) } ?: "–",
                supporting = "sisa tagihan belum lunas",
                icon = Icons.Outlined.AccountBalanceWallet,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            val waiting = data?.pendingApprovalsCount
            StatTile(
                label = "Menunggu Anda",
                value = waiting?.toString() ?: "–",
                supporting = "pengajuan",
                icon = Icons.Outlined.PendingActions,
                tone = if ((waiting ?: 0) > 0) StatusTone.Warning else StatusTone.Neutral,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun ApprovalCard(
    item: PendingApprovalItem,
    nowMillis: Long,
    processing: Boolean,
    enabled: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SistaCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(item.typeName, style = SistaTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        listOfNotNull(item.requesterName, item.requesterRole?.let(::roleLabel)).joinToString(" · "),
                        style = SistaTheme.typography.bodySmall,
                        color = SistaTheme.colors.onSurfaceVariant,
                    )
                }
                item.totalSteps?.let { total -> StatusPill("Langkah ${item.currentStep} dari $total", StatusTone.Neutral) }
            }
            item.notes?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = SistaTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
            dueLabel(item, nowMillis)?.let { (label, tone) -> StatusPill(label, tone, icon = if (item.isOverdue) Icons.Outlined.Warning else null) }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                SistaButton(
                    "Tolak",
                    onReject,
                    variant = ButtonVariant.Outlined,
                    leadingIcon = Icons.Outlined.Close,
                    enabled = enabled,
                    modifier = Modifier.weight(1f),
                )
                SistaButton(
                    "Setujui",
                    onApprove,
                    leadingIcon = Icons.Outlined.Check,
                    enabled = enabled,
                    loading = processing,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SchoolFigures(kpi: SchoolKpiSummary?, error: String?) {
    Column(Modifier.padding(horizontal = Spacing.screen), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        SectionHeader("Keuangan & ujian")
        when {
            kpi == null && error != null -> InlineBanner(message = "Angka keuangan belum bisa dimuat. $error", tone = StatusTone.Warning)
            kpi == null -> SkeletonList(rows = 2)
            else -> {
                Row(Modifier.height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    StatTile(
                        label = "Pemasukan bulan ini",
                        value = rupiahShort(kpi.monthlyRevenue),
                        supporting = "pembayaran diterima",
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                    StatTile(
                        label = "Ujian CBT",
                        value = kpi.cbtServerUsage?.let { "${it.activeSessions} aktif" } ?: "–",
                        supporting = kpi.cbtServerUsage?.let { "${thousands(it.sessionsToday)} dimulai hari ini" } ?: "belum ada data",
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }
                if (kpi.sppPaymentRatioByCohort.isNotEmpty()) {
                    SistaCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            Text("Pelunasan tagihan per tahun ajaran", style = SistaTheme.typography.titleSmall)
                            kpi.sppPaymentRatioByCohort.forEachIndexed { index, row ->
                                if (index > 0) HorizontalDivider(color = SistaTheme.colors.outlineVariant)
                                Column(Modifier.padding(vertical = Spacing.xs), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(row.academicYear, style = SistaTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                                        Text("${decimal(row.paidRatioPercent)}%", style = SistaTheme.typography.titleSmall)
                                    }
                                    LinearProgressIndicator(
                                        progress = { (row.paidRatioPercent / 100).toFloat().coerceIn(0f, 1f) },
                                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(SistaTheme.shapes.small),
                                        color = SistaTheme.colors.primary,
                                        trackColor = SistaTheme.colors.surfaceVariant,
                                        drawStopIndicator = {},
                                    )
                                    Text(
                                        "${thousands(row.paidBillings)} dari ${thousands(row.totalBillings)} tagihan lunas",
                                        style = SistaTheme.typography.bodySmall,
                                        color = SistaTheme.colors.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BroadcastCard(sending: Boolean, onCompose: () -> Unit) {
    SistaCard(modifier = Modifier.padding(horizontal = Spacing.screen).fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Outlined.Campaign, tone = StatusTone.Danger)
            Spacer(Modifier.width(Spacing.md))
            Column(Modifier.weight(1f)) {
                Text("Siaran darurat", style = SistaTheme.typography.titleSmall)
                Text(
                    "Tampil seketika di aplikasi seluruh siswa, guru, dan wali murid.",
                    style = SistaTheme.typography.bodySmall,
                    color = SistaTheme.colors.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(Spacing.sm))
            SistaButton("Kirim", onCompose, variant = ButtonVariant.Danger, loading = sending, enabled = !sending)
        }
    }
}

@Composable
private fun Shortcuts(shortcuts: List<AdminShortcut>, onOpenRoute: (String) -> Unit) {
    Column(Modifier.padding(horizontal = Spacing.screen), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        SectionHeader("Akses cepat", actionLabel = "Semua layanan", onAction = { onOpenRoute(Screen.ServicesHub.route) })
        shortcuts.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                row.forEach { item -> FeatureTile(item.title, item.icon, onClick = { onOpenRoute(item.route) }, modifier = Modifier.weight(1f)) }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}
