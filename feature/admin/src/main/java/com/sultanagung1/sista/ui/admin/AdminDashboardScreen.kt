package com.sultanagung1.sista.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.accessibility.sulaoneInteractiveTouchTarget
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import com.sultanagung1.sista.core.motion.springPressable
import com.sultanagung1.sista.ui.common.HeaderMetadataChip
import com.sultanagung1.sista.ui.common.SulaoneExecutiveHeader
import com.sultanagung1.sista.ui.navigation.Screen
import kotlinx.coroutines.launch

@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    onNavigateRoute: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val data = uiState.dashboardData
    val haptics = rememberHapticFeedbackHelper()

    val snackbarHostState = remember { SnackbarHostState() }
    var showEmergencyDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val headerScrolledOff by rememberIsItemScrolledOff(listState, HEADER_ITEM_KEY)

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.emergencyBroadcastSent) {
        if (uiState.emergencyBroadcastSent != null) {
            showEmergencyDialog = false
            snackbarHostState.showSnackbar("Siaran darurat berhasil dikirim ke seluruh pengguna aplikasi.")
            viewModel.dismissEmergencyBroadcastConfirmation()
        }
    }

    if (showEmergencyDialog) {
        EmergencyBroadcastDialog(
            isSending = uiState.isSendingEmergencyBroadcast,
            onDismiss = { showEmergencyDialog = false },
            onSend = { title, message, location -> viewModel.broadcastEmergency(title, message, location) }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Unified Executive Top App Bar (Institutional Command Cockpit)
                item(key = HEADER_ITEM_KEY) {
                    SulaoneExecutiveHeader(
                        userName = uiState.principalName ?: "—",
                        titlePrefix = "KOKPIT EKSEKUTIF PIMPINAN",
                        chips = listOf(
                            HeaderMetadataChip(
                                text = "Kepala Sekolah",
                                icon = Icons.Default.AdminPanelSettings,
                                textColor = Emerald800,
                                containerColor = Emerald50,
                                borderColor = Emerald200,
                                iconColor = Emerald700
                            ),
                            HeaderMetadataChip(
                                text = "SuperAdmin",
                                isLiveDot = true,
                                dotColor = Emerald500,
                                textColor = Slate700
                            )
                        ),
                        unreadNotificationsCount = 0,
                        onAvatarClick = { onNavigateRoute("profile") },
                        onQrClick = { onNavigateRoute("scanner") },
                        onNotificationClick = { onNavigateRoute("notifications") }
                    )
                }

                // 2. School KPI — only fields the backend actually provides
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "Metrik Kinerja Utama (KPI) Sekolah",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // FASE 76.5: first load shows a bento-shaped skeleton (tiered)
                        // instead of four "—" tiles that look like missing data.
                        if (uiState.isLoading && data == null) {
                            SulaoneTieredLoading(isLoading = true) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    BentoHeroSplitSkeleton()
                                    MetricCardSkeleton()
                                }
                            }
                        } else {
                            // FASE 76.2 Bento: today's student attendance is the hero.
                            // The old "Live" badges were dropped — this data is a
                            // one-shot REST fetch on screen open (no polling, no
                            // WebSocket), so "Live" was a false claim.
                            BentoHeroSplit(
                                hero = { heroModifier ->
                                    SulaoneBentoHeroTile(
                                        modifier = heroModifier,
                                        label = "Kehadiran Siswa",
                                        value = data?.attendanceRateToday
                                            ?.let { String.format(java.util.Locale.US, "%.1f", it) }
                                            ?: "—",
                                        unit = if (data?.attendanceRateToday != null) "%" else null,
                                        caption = if (uiState.isLoading) "Memuat data…" else "Presensi KBM hari ini",
                                        icon = Icons.Default.People,
                                        accent = Emerald700,
                                        badgeText = "Hari Ini"
                                    )
                                },
                                top = { tileModifier ->
                                    SulaoneMetricCard(
                                        modifier = tileModifier,
                                        title = "Total Siswa Aktif",
                                        value = data?.totalStudents?.toString() ?: "—",
                                        subtitle = "Seluruh Rombel",
                                        icon = Icons.Default.School,
                                        iconTint = AccentPurple,
                                        iconBackground = AccentPurple.copy(alpha = 0.12f)
                                    )
                                },
                                bottom = { tileModifier ->
                                    SulaoneMetricCard(
                                        modifier = tileModifier,
                                        title = "Sesi CBT Aktif",
                                        value = uiState.schoolKpi?.cbtServerUsage?.activeSessions?.toString() ?: "—",
                                        subtitle = "Sedang berlangsung",
                                        icon = Icons.Default.Dns,
                                        iconTint = AccentCyan,
                                        iconBackground = AccentCyan.copy(alpha = 0.12f)
                                    )
                                }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            SulaoneMetricCard(
                                modifier = Modifier.fillMaxWidth(),
                                title = "Total Tunggakan SPP",
                                value = data?.unpaidBillingsTotal?.let { formatRupiah(it) } ?: "—",
                                subtitle = "Seluruh Siswa",
                                icon = Icons.Default.AccountBalanceWallet,
                                iconTint = Gold700,
                                iconBackground = Gold50,
                                onClick = { onNavigateRoute(Screen.ExecutiveAnalytics.route) }
                            )
                        }
                    }
                }

                // 1b. FASE 71.4 follow-up: real KPI the backend used to return as
                // "Dummy or expanded KPI details" — SPP payment ratio per
                // academic year, CBT server usage, and an honest null for
                // teacher attendance rate (no backing data exists for it).
                item {
                    val kpi = uiState.schoolKpi
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "KPI Eksekutif Lanjutan",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // active_teachers and monthly_revenue have been returned by
                        // GET mobile/admin/kpi all along but were never displayed.
                        // monthly_revenue is only shown now that its backend query
                        // was fixed (it read a non-existent billings.payment_date
                        // column and never scoped by year — see
                        // AdminKpiAndApprovalsTest::test_kpi_monthly_revenue_...).
                        BentoPair(
                            start = { tileModifier ->
                                SulaoneMetricCard(
                                    modifier = tileModifier,
                                    title = "Guru Aktif",
                                    value = kpi?.activeTeachers?.toString() ?: "—",
                                    subtitle = "Akun guru terdaftar",
                                    icon = Icons.Default.CoPresent,
                                    iconTint = Emerald700,
                                    iconBackground = Emerald50
                                )
                            },
                            end = { tileModifier ->
                                SulaoneMetricCard(
                                    modifier = tileModifier,
                                    title = "Sesi CBT Hari Ini",
                                    value = kpi?.cbtServerUsage?.sessionsToday?.toString() ?: "—",
                                    subtitle = "Total mulai hari ini",
                                    icon = Icons.Default.Quiz,
                                    iconTint = AccentCyan,
                                    iconBackground = AccentCyan.copy(alpha = 0.12f)
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        SulaoneMetricCard(
                            modifier = Modifier.fillMaxWidth(),
                            title = "Pemasukan Bulan Ini",
                            value = kpi?.monthlyRevenue?.let { formatRupiah(it) } ?: "—",
                            subtitle = "Pembayaran tagihan yang diterima",
                            icon = Icons.Default.Payments,
                            iconTint = Emerald700,
                            iconBackground = Emerald50
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Teacher attendance: honestly not a number — no backing
                        // data exists anywhere in the schema for it.
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Slate100,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Slate500, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Persentase kehadiran guru: " +
                                        (kpi?.teacherAttendanceRateNote ?: "Data belum tersedia."),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate700
                                )
                            }
                        }

                        if (kpi != null && kpi.sppPaymentRatioByCohort.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Rasio Pelunasan SPP per Tahun Ajaran",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    kpi.sppPaymentRatioByCohort.forEach { cohort ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = cohort.academicYear,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                                )
                                                Text(
                                                    text = "${cohort.paidBillings}/${cohort.totalBillings} tagihan lunas",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Slate500
                                                )
                                            }
                                            SulaoneBadge(
                                                text = "${cohort.paidRatioPercent}%",
                                                containerColor = if (cohort.paidRatioPercent >= 80.0) Emerald50 else Gold50,
                                                contentColor = if (cohort.paidRatioPercent >= 80.0) Emerald800 else Gold800
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2b. FASE 71.4 "Tombol Siaran Darurat" — pushes a real
                // EmergencyBroadcastEvent to every connected app over WebSocket.
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .springPressable {
                                haptics.tapLight()
                                showEmergencyDialog = true
                            }
                            .semantics(mergeDescendants = true) { role = Role.Button },
                        shape = RoundedCornerShape(16.dp),
                        color = AccentRose.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, AccentRose.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationImportant,
                                contentDescription = null,
                                tint = AccentRose,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Kirim Siaran Darurat",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentRose
                                )
                                Text(
                                    text = "Notifikasi real-time ke seluruh siswa, guru, dan wali murid",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AccentRose.copy(alpha = 0.85f)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = AccentRose
                            )
                        }
                    }
                }

                // 3. Executive Fast Action Matrix (SuperApp 4-Pill Grid)
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Text(
                            text = "Aksi Cepat Pimpinan & Pengawasan",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            AdminActionPill(
                                title = "Siaran",
                                icon = Icons.Default.Campaign,
                                accentColor = AccentGreen,
                                onClick = { onNavigateRoute(Screen.AnnouncementFeed.route) }
                            )
                            AdminActionPill(
                                title = "Analitik KPI",
                                icon = Icons.Default.BarChart,
                                accentColor = AccentCyan,
                                onClick = { onNavigateRoute(Screen.ExecutiveAnalytics.route) }
                            )
                            AdminActionPill(
                                title = "Evaluasi Guru",
                                icon = Icons.AutoMirrored.Filled.FactCheck,
                                accentColor = AccentAmber,
                                onClick = { onNavigateRoute(Screen.TeacherEvaluation.route) }
                            )
                            AdminActionPill(
                                title = "Modul Sekolah",
                                icon = Icons.Default.Apps,
                                accentColor = AccentPurple,
                                onClick = { onNavigateRoute(Screen.EnterpriseCatalog.route) }
                            )
                        }
                    }
                }

                // 4. FASE 71.4 follow-up: real pending-approvals list with
                // working approve/reject buttons — previously only a count was
                // shown, with a note explaining the list itself wasn't available.
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Persetujuan Menunggu Tindakan",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            SulaoneBadge(
                                text = "${uiState.pendingApprovals.size} Berkas",
                                containerColor = if (uiState.pendingApprovals.isNotEmpty()) Gold50 else Emerald50,
                                contentColor = if (uiState.pendingApprovals.isNotEmpty()) Gold800 else Emerald800
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        if (uiState.isLoadingApprovals && uiState.pendingApprovals.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                            }
                        } else if (uiState.pendingApprovals.isEmpty()) {
                            SulaoneEmptyState(
                                icon = Icons.Default.CheckCircle,
                                title = "Semua Berkas Tuntas",
                                description = "Tidak ada permohonan yang menunggu persetujuan pimpinan."
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                uiState.pendingApprovals.forEach { approval ->
                                    PendingApprovalCard(
                                        approval = approval,
                                        isProcessing = uiState.processingApprovalId == approval.id,
                                        onApprove = { viewModel.processApproval(approval.id, "approve") },
                                        onReject = { viewModel.processApproval(approval.id, "reject") }
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. Pengawasan Kesiswaan & Evaluasi Kampus — no backend aggregate
                // exists for either metric yet, so no number is fabricated here.
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "Pengawasan Kesiswaan & Evaluasi Kampus",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SulaoneMetricCard(
                                modifier = Modifier.weight(1f),
                                title = "Evaluasi Guru & OSIS",
                                value = "Data belum tersedia",
                                subtitle = "Kuesioner EKG & Pemilu",
                                badgeText = "Lihat Detail",
                                badgeColor = Emerald700,
                                badgeBackground = Emerald50,
                                icon = Icons.Default.HowToVote,
                                iconTint = Emerald700,
                                iconBackground = Emerald50,
                                onClick = { onNavigateRoute(Screen.TeacherEvaluation.route) }
                            )

                            SulaoneMetricCard(
                                modifier = Modifier.weight(1f),
                                title = "Tata Tertib Siswa",
                                value = "Data belum tersedia",
                                subtitle = "SP & Rekap Poin Digital",
                                badgeText = "Lihat Detail",
                                badgeColor = Gold700,
                                badgeBackground = Gold50,
                                icon = Icons.Default.Gavel,
                                iconTint = Gold700,
                                iconBackground = Gold50,
                                onClick = { onNavigateRoute(Screen.Discipline.createRoute()) }
                            )
                        }
                    }
                }

                // Space at bottom for floating nav bar
                item {
                    Spacer(modifier = Modifier.height(88.dp))
                }
            }

            // FASE 76.2: sticky glass bar once the executive header scrolls away.
            SulaoneGlassTopBar(
                visible = headerScrolledOff,
                title = uiState.principalName ?: "Pimpinan",
                subtitle = "Kokpit Eksekutif",
                onClick = { coroutineScope.launch { listState.animateScrollToItem(0) } }
            )
        }
    }
}

private const val HEADER_ITEM_KEY = "executive_header"

/** "Rp 1.234.567" — dot-grouped regardless of device locale. */
private fun formatRupiah(amount: Double): String =
    "Rp ${"%,.0f".format(java.util.Locale.US, amount).replace(',', '.')}"

@Composable
private fun EmergencyBroadcastDialog(
    isSending: Boolean,
    onDismiss: () -> Unit,
    onSend: (title: String, message: String, location: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!isSending) onDismiss() },
        icon = { Icon(Icons.Default.NotificationImportant, contentDescription = null, tint = AccentRose) },
        title = { Text("Kirim Siaran Darurat", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Pesan ini akan langsung tampil ke seluruh siswa, guru, dan wali murid yang sedang membuka aplikasi.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul") },
                    singleLine = true,
                    enabled = !isSending,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Pesan") },
                    minLines = 3,
                    enabled = !isSending,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Lokasi (opsional)") },
                    singleLine = true,
                    enabled = !isSending,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSend(title.trim(), message.trim(), location.trim().ifBlank { null }) },
                enabled = !isSending && title.isNotBlank() && message.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentRose)
            ) {
                if (isSending) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Kirim Sekarang")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSending) {
                Text("Batal")
            }
        }
    )
}

/** FASE 71.4 follow-up: one real pending WorkflowRequest, with working approve/reject actions. */
@Composable
private fun PendingApprovalCard(
    approval: com.sultanagung1.sista.data.model.PendingApprovalItem,
    isProcessing: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, if (approval.isOverdue) AccentRose.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = approval.typeName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Diajukan oleh ${approval.requesterName}" +
                            (approval.requesterRole?.let { " ($it)" } ?: ""),
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                }
                if (approval.totalSteps != null) {
                    SulaoneBadge(
                        text = "Langkah ${approval.currentStep}/${approval.totalSteps}",
                        containerColor = Slate100,
                        contentColor = Slate700
                    )
                }
            }

            approval.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (approval.isOverdue) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Melewati batas waktu",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = AccentRose
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    enabled = !isProcessing,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRose)
                ) {
                    Text("Tolak")
                }
                Button(
                    onClick = onApprove,
                    enabled = !isProcessing,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Text("Setujui")
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminActionPill(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    val haptics = rememberHapticFeedbackHelper()

    Column(
        modifier = Modifier
            .width(80.dp)
            .springPressable {
                haptics.tapLight()
                onClick()
            }
            .semantics(mergeDescendants = true) { role = Role.Button },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = accentColor.copy(alpha = 0.12f),
            border = BorderStroke(0.5.dp, accentColor.copy(alpha = 0.25f)),
            modifier = Modifier
                .size(56.dp)
                .sulaoneInteractiveTouchTarget(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.sp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
