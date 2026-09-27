package com.sultanagung1.sista.ui.parent

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.sultanagung1.sista.core.websocket.WebSocketEvent
import com.sultanagung1.sista.data.model.ParentChildItem
import com.sultanagung1.sista.ui.common.HeaderMetadataChip
import com.sultanagung1.sista.ui.common.SulaoneExecutiveHeader
import com.sultanagung1.sista.ui.navigation.Screen
import java.util.Locale

@Composable
fun ParentDashboardScreen(
    viewModel: ParentViewModel,
    onNavigateToChildDetail: (String) -> Unit,
    onNavigateToBilling: () -> Unit,
    onNavigateRoute: (String) -> Unit,
    onNavigateToActivityFeed: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeChild = uiState.selectedChild
    val summary = uiState.selectedChildSummary
    val haptics = rememberHapticFeedbackHelper()

    Scaffold { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Parent Executive Top App Bar (Unified Professional Design)
            item {
                SulaoneExecutiveHeader(
                    userName = uiState.parentName.ifBlank { "Wali Murid" },
                    titlePrefix = "Assalamu'alaikum,",
                    chips = listOf(
                        HeaderMetadataChip(
                            text = "Wali Murid",
                            icon = Icons.Default.FamilyRestroom,
                            textColor = Emerald800,
                            containerColor = Emerald50,
                            borderColor = Emerald200,
                            iconColor = Emerald700
                        ),
                        HeaderMetadataChip(
                            text = "Portal Terverifikasi",
                            isLiveDot = true,
                            dotColor = Emerald500,
                            textColor = Slate700
                        ),
                        HeaderMetadataChip(
                            text = if (activeChild != null) "Wali dari: ${activeChild.name}" else "Wali Murid Aktif",
                            icon = Icons.Default.ChildCare,
                            textColor = Slate700,
                            iconColor = Gold600
                        )
                    ),
                    onAvatarClick = { onNavigateRoute("profile") },
                    onQrClick = { onNavigateRoute("scanner") },
                    onNotificationClick = { onNavigateRoute("notifications") }
                )
            }

            if (uiState.errorMessage != null) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        SulaoneErrorBanner(
                            message = uiState.errorMessage ?: "Gagal memuat data wali murid.",
                            onRetry = { viewModel.loadDashboard() }
                        )
                    }
                }
            }

            // 2. Children Switcher Selector (Multi-Child Support)
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Data Putra / Putri Tercinta",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.children) { child ->
                            val isSelected = child.uuid == activeChild?.uuid
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Emerald700 else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) Emerald700 else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .springPressable {
                                        haptics.tapLight()
                                        viewModel.selectChild(child)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Gold300 else Emerald500)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${child.name} (${child.classroom ?: "-"})",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3a. FASE 71.3 live gate status — only rendered once an actual
            // AttendanceLoggedEvent has arrived over the WebSocket for the
            // selected child this session; no fabricated placeholder banner.
            uiState.liveGateStatus?.let { gateStatus ->
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        LiveGateStatusBanner(gateStatus)
                    }
                }
            }

            // 3. High-Fidelity Selected Child Persona Card
            activeChild?.let { child ->
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        ParentChildPersonaCard(
                            child = child,
                            summary = summary,
                            latestAttendance = uiState.childAttendanceLogs.firstOrNull(),
                            onDetailClick = { onNavigateToChildDetail(child.uuid) },
                            onDisciplineClick = { onNavigateRoute(Screen.Discipline.route) },
                            onChatClick = { onNavigateRoute(Screen.ConversationList.route) }
                        )
                    }
                }

                // 4. Real KPI Metric Cards for Selected Child (from ApiParentController::childSummary)
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "Ringkasan Akademik & Kedisiplinan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        if (uiState.isLoadingChildDetail && summary == null) {
                            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Emerald700)
                            }
                        } else {
                            val stats = summary?.statistics
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                SulaoneMetricCard(
                                    modifier = Modifier.weight(1f),
                                    title = "Kehadiran",
                                    value = stats?.let { "${it.attendanceRate}%" } ?: "-",
                                    subtitle = "Semester Berjalan",
                                    badgeText = if ((stats?.attendanceRate ?: 0.0) >= 95) "Sangat Baik" else "Perlu Perhatian",
                                    badgeColor = Emerald700,
                                    badgeBackground = Emerald50,
                                    icon = Icons.Default.CheckCircle,
                                    iconTint = Emerald700,
                                    iconBackground = Emerald50
                                )

                                SulaoneMetricCard(
                                    modifier = Modifier.weight(1f),
                                    title = "Rata-rata Nilai",
                                    value = stats?.let { String.format(Locale.US, "%.1f", it.averageGrade) } ?: "-",
                                    subtitle = "Seluruh Mata Pelajaran",
                                    badgeText = "Lihat Rapor",
                                    badgeColor = AccentBlue,
                                    badgeBackground = AccentBlue.copy(alpha = 0.12f),
                                    icon = Icons.Default.School,
                                    iconTint = AccentBlue,
                                    iconBackground = AccentBlue.copy(alpha = 0.12f),
                                    onClick = { onNavigateToChildDetail(child.uuid) }
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                SulaoneMetricCard(
                                    modifier = Modifier.weight(1f),
                                    title = "Poin BK",
                                    value = "${stats?.totalBkPoints ?: 0}",
                                    subtitle = "Total Poin Pelanggaran",
                                    badgeText = if ((stats?.totalBkPoints ?: 0) == 0) "Bersih" else "Perlu Perhatian",
                                    badgeColor = if ((stats?.totalBkPoints ?: 0) == 0) Emerald700 else Gold700,
                                    badgeBackground = if ((stats?.totalBkPoints ?: 0) == 0) Emerald50 else Gold50,
                                    icon = Icons.Default.Gavel,
                                    iconTint = if ((stats?.totalBkPoints ?: 0) == 0) Emerald700 else Gold700,
                                    iconBackground = if ((stats?.totalBkPoints ?: 0) == 0) Emerald50 else Gold50,
                                    onClick = { onNavigateRoute(Screen.Discipline.route) }
                                )

                                val unpaidCount = stats?.unpaidBillingsCount ?: 0
                                SulaoneMetricCard(
                                    modifier = Modifier.weight(1f),
                                    title = "Status SPP & Infaq",
                                    value = if (unpaidCount == 0) "LUNAS" else "$unpaidCount Tagihan",
                                    subtitle = if (unpaidCount == 0) "Bebas Administrasi" else "Menunggu Pembayaran",
                                    badgeText = if (unpaidCount == 0) "Aman" else "Segera Bayar",
                                    badgeColor = if (unpaidCount == 0) Emerald700 else AccentRose,
                                    badgeBackground = if (unpaidCount == 0) Emerald50 else AccentRose.copy(alpha = 0.12f),
                                    icon = Icons.Default.AccountBalanceWallet,
                                    iconTint = if (unpaidCount == 0) Emerald700 else AccentRose,
                                    iconBackground = if (unpaidCount == 0) Emerald50 else AccentRose.copy(alpha = 0.12f),
                                    onClick = onNavigateToBilling
                                )
                            }
                        }
                    }
                }
            }

            // 5. Weekly Digest & Activity Feed Shortcuts
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Emerald50),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Assessment,
                                            contentDescription = null,
                                            tint = Emerald700,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Ringkasan Mingguan Ananda",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Evaluasi Komprehensif Minggu Ini",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                onNavigateToActivityFeed?.let { toFeed ->
                                    TextButton(
                                        onClick = {
                                            haptics.tapLight()
                                            toFeed()
                                        },
                                        modifier = Modifier.sulaoneInteractiveTouchTarget(48.dp)
                                    ) {
                                        Text(
                                            text = "Lihat Feed",
                                            color = Emerald700,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                            Spacer(modifier = Modifier.height(14.dp))

                            val digest = uiState.weeklyDigest
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Kehadiran KBM",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = digest?.attendancePercentage?.let { "${it.toInt()}%" } ?: "-",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Emerald700
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Rata-rata Nilai",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = digest?.averageGrade?.toString() ?: "-",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = AccentBlue
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Skor Ibadah",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = digest?.ibadahScore?.toString() ?: "-",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Gold700
                                    )
                                }
                            }
                        }
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

/**
 * Selected child persona card. The old "real-time campus gate" pulse was
 * fabricated (the backend had no live gate-checkin feed for parents at the
 * time); FASE 71.3 wired a real one back in — see [LiveGateStatusBanner]
 * above this card, driven by an actual AttendanceLoggedEvent WebSocket push
 * rather than a client-side animation. The old "WhatsApp Guru" action is
 * still gone — there is no phone number field on the teacher/user record —
 * replaced by the real in-app "Pesan Sekolah" action (ConversationList).
 */
/**
 * FASE 71.3 "Status Gerbang Real-time" — rendered only when a genuine
 * AttendanceLoggedEvent has arrived over the WebSocket this session for the
 * currently selected child.
 */
@Composable
private fun LiveGateStatusBanner(status: WebSocketEvent.LiveAttendanceRecorded) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Emerald50,
        border = BorderStroke(1.dp, Emerald200),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Emerald500)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${status.studentName} hadir di ${status.gate}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Emerald800
                )
                Text(
                    text = "${status.status} • ${status.checkInTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Emerald700
                )
            }
        }
    }
}

@Composable
private fun ParentChildPersonaCard(
    child: ParentChildItem,
    summary: com.sultanagung1.sista.data.model.ChildSummaryResponse?,
    latestAttendance: com.sultanagung1.sista.data.model.ChildAttendanceLog?,
    onDetailClick: () -> Unit,
    onDisciplineClick: () -> Unit,
    onChatClick: () -> Unit
) {
    val haptics = rememberHapticFeedbackHelper()
    val unpaidCount = summary?.statistics?.unpaidBillingsCount ?: 0
    val sppLabel = if (unpaidCount == 0) "Lunas" else "$unpaidCount Tagihan"

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Child Identity & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Emerald700, Emerald900))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = child.name.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString(""),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = child.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Siswa Terdaftar",
                                tint = Emerald600,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "${child.classroom ?: "-"} • NISN: ${child.nisn ?: "-"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                SulaoneBadge(
                    text = sppLabel,
                    containerColor = if (unpaidCount == 0) Emerald50 else Gold50,
                    contentColor = if (unpaidCount == 0) Emerald800 else Gold800
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Homeroom teacher name (real, from childSummary) — no phone number
            // field exists on the backend's teacher/user record, so no WhatsApp action.
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Emerald50.copy(alpha = 0.75f),
                border = BorderStroke(1.dp, Emerald200.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Emerald700,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Wali Kelas: ${summary?.student?.homeroomTeacher ?: "Belum tersedia"}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Emerald900
                        )
                        Text(
                            text = if (latestAttendance != null) "Presensi terakhir: ${latestAttendance.date} — ${latestAttendance.statusLabel}" else "Belum ada data presensi",
                            style = MaterialTheme.typography.bodySmall,
                            color = Emerald800,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
            Spacer(modifier = Modifier.height(14.dp))

            // Action Row 1: Rapor Digital & Pesan Sekolah (48dp Touch Targets)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        haptics.tapLight()
                        onChatClick()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .sulaoneInteractiveTouchTarget(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Emerald600.copy(alpha = 0.4f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Emerald800),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = Emerald700, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Pesan Sekolah", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        haptics.tapLight()
                        onDetailClick()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .sulaoneInteractiveTouchTarget(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(imageVector = Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Rapor Digital", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Row 2: Buku Saku Kedisiplinan
            OutlinedButton(
                onClick = {
                    haptics.tapLight()
                    onDisciplineClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .sulaoneInteractiveTouchTarget(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Icon(imageVector = Icons.Default.Gavel, contentDescription = null, tint = Gold700, modifier = Modifier.size(17.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Buku Saku Poin", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
