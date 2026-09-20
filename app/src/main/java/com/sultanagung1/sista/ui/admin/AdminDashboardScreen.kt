package com.sultanagung1.sista.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.sultanagung1.sista.data.model.ApprovalRequestItem
import com.sultanagung1.sista.data.model.CriticalAlertItem
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
    val kpi = data?.kpi
    val haptics = rememberHapticFeedbackHelper()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.actionSuccessMessage) {
        uiState.actionSuccessMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
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
                    userName = data?.principalName ?: "Drs. H. Muhammad Arif, M.Pd",
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
                        ),
                        HeaderMetadataChip(
                            text = "TA ${data?.academicYear ?: "2025/2026"}",
                            icon = Icons.Default.CalendarToday,
                            textColor = Slate700,
                            iconColor = Gold600
                        )
                    ),
                    unreadNotificationsCount = 5,
                    onAvatarClick = { onNavigateRoute("profile") },
                    onQrClick = { onNavigateRoute("scanner") },
                    onNotificationClick = { onNavigateRoute("notifications") }
                )
            }

            // 2. Real-Time School KPI Gauges (2x2 Bento Matrix)
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
                            value = "${kpi?.attendanceRateToday ?: 98.4}%",
                            subtitle = "KBM Hari Ini",
                            badgeText = ">95% Target",
                            badgeColor = Emerald700,
                            badgeBackground = Emerald50,
                            icon = Icons.Default.People,
                            iconTint = Emerald700,
                            iconBackground = Emerald50
                        )
                        SulaoneMetricCard(
                            modifier = Modifier.weight(1f),
                            title = "Kolektibilitas SPP",
                            value = "${kpi?.sppCollectionRate ?: 94.2}%",
                            subtitle = "Bulan Berjalan",
                            badgeText = ">90% Target",
                            badgeColor = Gold700,
                            badgeBackground = Gold50,
                            icon = Icons.Default.AccountBalanceWallet,
                            iconTint = Gold700,
                            iconBackground = Gold50,
                            onClick = { onNavigateRoute(Screen.ExecutiveAnalytics.route) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SulaoneMetricCard(
                            modifier = Modifier.weight(1f),
                            title = "Guru Mengajar",
                            value = "${kpi?.teachersPresentToday ?: 62} / ${kpi?.totalTeachers ?: 64}",
                            subtitle = "62 Hadir Tepat Waktu",
                            badgeText = "Presensi KBM",
                            badgeColor = AccentBlue,
                            badgeBackground = AccentBlue.copy(alpha = 0.12f),
                            icon = Icons.Default.Badge,
                            iconTint = AccentBlue,
                            iconBackground = AccentBlue.copy(alpha = 0.12f)
                        )
                        SulaoneMetricCard(
                            modifier = Modifier.weight(1f),
                            title = "Total Siswa Aktif",
                            value = "${kpi?.totalStudents ?: 1080}",
                            subtitle = "Fase E & Fase F",
                            badgeText = "36 Rombel",
                            badgeColor = AccentPurple,
                            badgeBackground = AccentPurple.copy(alpha = 0.12f),
                            icon = Icons.Default.School,
                            iconTint = AccentPurple,
                            iconBackground = AccentPurple.copy(alpha = 0.12f)
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
                            title = "Persetujuan",
                            icon = Icons.Default.FactCheck,
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

            // 4. Critical System Alerts
            if (!data?.criticalAlerts.isNullOrEmpty()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "Peringatan Sistem & Pengawasan",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                items(
                    items = data!!.criticalAlerts,
                    key = { "${it.title}_${it.timestamp}" },
                    contentType = { "alert" }
                ) { alert ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        CriticalAlertCard(alert = alert)
                    }
                }
            }

            // 5. Rapid Approval Queue (Persetujuan Menunggu Tindakan)
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
                            text = "${data?.pendingApprovals?.size ?: 0} Berkas",
                            containerColor = if ((data?.pendingApprovals?.size ?: 0) > 0) Gold50 else Emerald50,
                            contentColor = if ((data?.pendingApprovals?.size ?: 0) > 0) Gold800 else Emerald800
                        )
                    }
                }
            }

            if (data?.pendingApprovals.isNullOrEmpty()) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        SulaoneEmptyState(
                            icon = Icons.Default.CheckCircle,
                            title = "Semua Berkas Tuntas",
                            description = "Tidak ada permohonan izin, mutasi, atau pengadaan yang menunggu persetujuan pimpinan."
                        )
                    }
                }
            } else {
                items(
                    items = data!!.pendingApprovals,
                    key = { it.id },
                    contentType = { "approval" }
                ) { req ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        ModernApprovalCard(
                            item = req,
                            onApprove = {
                                haptics.tapMedium()
                                viewModel.processApproval(req.id, "approve")
                            },
                            onReject = {
                                haptics.tapLight()
                                viewModel.processApproval(req.id, "reject")
                            }
                        )
                    }
                }
            }

            // 6. Pengawasan Kesiswaan & Evaluasi Kampus
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
                            value = "4.82 / 5.0",
                            subtitle = "Kuesioner EKG & Pemilu",
                            badgeText = "Unggul",
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
                            value = "99.1% Tertib",
                            subtitle = "SP & Rekap Poin Digital",
                            badgeText = "Terkendali",
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

@Composable
private fun CriticalAlertCard(alert: CriticalAlertItem) {
    val isWarning = alert.severity == "warning"
    val containerBg = if (isWarning) Gold50.copy(alpha = 0.6f) else AccentBlue.copy(alpha = 0.08f)
    val borderStroke = if (isWarning) Gold200 else AccentBlue.copy(alpha = 0.3f)
    val textCol = if (isWarning) Gold900 else AccentBlue

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerBg,
        border = BorderStroke(0.5.dp, borderStroke),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = if (isWarning) Icons.Default.Warning else Icons.Default.Info,
                contentDescription = null,
                tint = if (isWarning) Gold700 else AccentBlue,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = alert.title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = textCol
                    )
                    Text(
                        text = alert.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400,
                        fontSize = 10.sp
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = alert.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun ModernApprovalCard(
    item: ApprovalRequestItem,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SulaoneBadge(
                    text = item.type,
                    containerColor = Emerald50,
                    contentColor = Emerald800
                )
                Text(
                    text = item.submittedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate400
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.requesterName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Unit / Divisi: ${item.department}",
                style = MaterialTheme.typography.bodySmall,
                color = Emerald700,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier
                        .weight(1f)
                        .sulaoneInteractiveTouchTarget(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Tolak", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onApprove,
                    modifier = Modifier
                        .weight(1f)
                        .sulaoneInteractiveTouchTarget(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Setujui", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
