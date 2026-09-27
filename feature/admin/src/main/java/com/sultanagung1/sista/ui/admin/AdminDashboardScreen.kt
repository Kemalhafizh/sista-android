package com.sultanagung1.sista.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Unified Executive Top App Bar (Institutional Command Cockpit)
            item {
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SulaoneMetricCard(
                            modifier = Modifier.weight(1f),
                            title = "Kehadiran Siswa",
                            value = data?.attendanceRateToday?.let { "${it}%" } ?: "—",
                            subtitle = "KBM Hari Ini",
                            badgeText = if (uiState.isLoading) "Memuat..." else "Live",
                            badgeColor = Emerald700,
                            badgeBackground = Emerald50,
                            icon = Icons.Default.People,
                            iconTint = Emerald700,
                            iconBackground = Emerald50
                        )
                        SulaoneMetricCard(
                            modifier = Modifier.weight(1f),
                            title = "Total Siswa Aktif",
                            value = data?.totalStudents?.toString() ?: "—",
                            subtitle = "Seluruh Rombel",
                            badgeText = if (uiState.isLoading) "Memuat..." else "Live",
                            badgeColor = AccentPurple,
                            badgeBackground = AccentPurple.copy(alpha = 0.12f),
                            icon = Icons.Default.School,
                            iconTint = AccentPurple,
                            iconBackground = AccentPurple.copy(alpha = 0.12f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    SulaoneMetricCard(
                        modifier = Modifier.fillMaxWidth(),
                        title = "Total Tunggakan SPP",
                        value = data?.unpaidBillingsTotal?.let { "Rp ${"%,.0f".format(it).replace(',', '.')}" } ?: "—",
                        subtitle = "Seluruh Siswa",
                        badgeText = if (uiState.isLoading) "Memuat..." else "Live",
                        badgeColor = Gold700,
                        badgeBackground = Gold50,
                        icon = Icons.Default.AccountBalanceWallet,
                        iconTint = Gold700,
                        iconBackground = Gold50,
                        onClick = { onNavigateRoute(Screen.ExecutiveAnalytics.route) }
                    )
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

            // 4. Pending approvals — backend only exposes a COUNT, not a list of
            // requests with ids, so there is no way to render real approve/reject
            // cards here yet. Showing the honest count instead of fabricating them.
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
                            text = "${data?.pendingApprovalsCount ?: 0} Berkas",
                            containerColor = if ((data?.pendingApprovalsCount ?: 0) > 0) Gold50 else Emerald50,
                            contentColor = if ((data?.pendingApprovalsCount ?: 0) > 0) Gold800 else Emerald800
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    if ((data?.pendingApprovalsCount ?: 0) == 0) {
                        SulaoneEmptyState(
                            icon = Icons.Default.CheckCircle,
                            title = "Semua Berkas Tuntas",
                            description = "Tidak ada permohonan yang menunggu persetujuan pimpinan."
                        )
                    } else {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Gold50.copy(alpha = 0.5f),
                            border = BorderStroke(0.5.dp, Gold200)
                        ) {
                            Text(
                                text = "Ada ${data?.pendingApprovalsCount} berkas menunggu, namun rincian daftarnya belum tersedia di aplikasi mobile ini — perlu dibuka melalui portal web untuk ditindaklanjuti.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Gold900,
                                modifier = Modifier.padding(14.dp)
                            )
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
                            onClick = { onNavigateRoute(Screen.Discipline.route) }
                        )
                    }
                }
            }

            // Space at bottom for floating nav bar
            item {
                Spacer(modifier = Modifier.height(88.dp))
            }
        }
    }
}

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
