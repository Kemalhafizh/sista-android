package com.sultanagung1.sista.ui.parent

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
import com.sultanagung1.sista.data.model.ChildSummary
import com.sultanagung1.sista.ui.common.HeaderMetadataChip
import com.sultanagung1.sista.ui.common.SulaoneExecutiveHeader
import com.sultanagung1.sista.ui.navigation.Screen

@Composable
fun ParentDashboardScreen(
    viewModel: ParentViewModel,
    onNavigateToChildDetail: (String) -> Unit,
    onNavigateToBilling: () -> Unit,
    onNavigateRoute: (String) -> Unit,
    onNavigateToActivityFeed: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val data = uiState.dashboardData
    val activeChild = uiState.selectedChild
    val context = LocalContext.current
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
                    userName = data?.parentName ?: "Bapak Hendra Gunawan, S.T.",
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
                    unreadNotificationsCount = 3,
                    onAvatarClick = { onNavigateRoute("profile") },
                    onQrClick = { onNavigateRoute("scanner") },
                    onNotificationClick = { onNavigateRoute("notifications") }
                )
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
                        items(data?.children ?: emptyList()) { child ->
                            val isSelected = child.studentId == activeChild?.studentId
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
                                        text = "${child.name} (${child.className})",
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

            // 3. High-Fidelity Selected Child Persona & Live Gate Presence Card
            activeChild?.let { child ->
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        ParentChildPersonaCard(
                            child = child,
                            onDetailClick = { onNavigateToChildDetail(child.studentId) },
                            onBillingClick = onNavigateToBilling,
                            onDisciplineClick = { onNavigateRoute(Screen.Discipline.route) },
                            onChatClick = { onNavigateRoute(Screen.ConversationList.route) },
                            onWhatsAppClick = { phone, name ->
                                val cleanPhone = if (phone.startsWith("0")) "62" + phone.substring(1) else phone
                                val message = "Assalamu'alaikum wr. wb. Ustadz/Ustadzah, saya wali murid dari ananda $name ingin berkonsultasi mengenai perkembangan ananda."
                                val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}")
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                context.startActivity(intent)
                            }
                        )
                    }
                }

                // 4. 3 Pillar KPI Metric Cards for Selected Child
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "Ringkasan Akademik & Kedisiplinan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SulaoneMetricCard(
                                modifier = Modifier.weight(1f),
                                title = "Kehadiran Kampus",
                                value = "${child.attendancePercentage}%",
                                subtitle = "Semester Berjalan",
                                badgeText = if (child.attendancePercentage >= 95) "Sangat Baik" else "Cukup",
                                badgeColor = Emerald700,
                                badgeBackground = Emerald50,
                                icon = Icons.Default.CheckCircle,
                                iconTint = Emerald700,
                                iconBackground = Emerald50
                            )

                            SulaoneMetricCard(
                                modifier = Modifier.weight(1f),
                                title = "Rata-rata KKTP",
                                value = "${child.gpaScore}",
                                subtitle = "Target KKTP: 78.0",
                                badgeText = "Fase F Unggul",
                                badgeColor = AccentBlue,
                                badgeBackground = AccentBlue.copy(alpha = 0.12f),
                                icon = Icons.Default.School,
                                iconTint = AccentBlue,
                                iconBackground = AccentBlue.copy(alpha = 0.12f),
                                onClick = { onNavigateToChildDetail(child.studentId) }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SulaoneMetricCard(
                                modifier = Modifier.weight(1f),
                                title = "Mutabaah Ibadah",
                                value = "${child.mutabaahScore}%",
                                subtitle = "Sholat & Tilawah",
                                badgeText = "Konsisten",
                                badgeColor = Gold700,
                                badgeBackground = Gold50,
                                icon = Icons.Default.Mosque,
                                iconTint = Gold700,
                                iconBackground = Gold50
                            )

                            val isLunas = child.sppStatus.contains("Lunas", ignoreCase = true)
                            SulaoneMetricCard(
                                modifier = Modifier.weight(1f),
                                title = "Status SPP & Infaq",
                                value = if (isLunas) "LUNAS" else "TERTUNGGAK",
                                subtitle = if (isLunas) "Bebas Administrasi" else "Menunggu Pembayaran",
                                badgeText = if (isLunas) "Aman" else "Segera Bayar",
                                badgeColor = if (isLunas) Emerald700 else AccentRose,
                                badgeBackground = if (isLunas) Emerald50 else AccentRose.copy(alpha = 0.12f),
                                icon = Icons.Default.AccountBalanceWallet,
                                iconTint = if (isLunas) Emerald700 else AccentRose,
                                iconBackground = if (isLunas) Emerald50 else AccentRose.copy(alpha = 0.12f),
                                onClick = onNavigateToBilling
                            )
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
                                        text = "${digest?.attendancePercentage?.toInt() ?: 100}%",
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
                                        text = "${digest?.averageGrade ?: 91.5f}",
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
                                        text = "${digest?.ibadahScore ?: 96}",
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

            // 6. Announcements for Parents
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Pengumuman Sekolah untuk Wali Murid",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            items(
                items = data?.recentAnnouncements ?: emptyList(),
                key = { it.id },
                contentType = { "announcement" }
            ) { announcement ->
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
                                SulaoneBadge(
                                    text = announcement.category,
                                    containerColor = Emerald50,
                                    contentColor = Emerald800
                                )
                                Text(
                                    text = announcement.date,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate400
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = announcement.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = announcement.summary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
 * Modern High-Polish Selected Child Persona Card with Real-Time Campus Gate arrival status
 * and WCAG 48dp action buttons.
 */
@Composable
private fun ParentChildPersonaCard(
    child: ChildSummary,
    onDetailClick: () -> Unit,
    onBillingClick: () -> Unit,
    onDisciplineClick: () -> Unit,
    onChatClick: () -> Unit,
    onWhatsAppClick: (String, String) -> Unit
) {
    val haptics = rememberHapticFeedbackHelper()

    // Breathing pulse for gate presence
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

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
                            text = "${child.className} • NISN: ${child.nisn}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                SulaoneBadge(
                    text = child.sppStatus,
                    containerColor = if (child.sppStatus == "Lunas") Emerald50 else Gold50,
                    contentColor = if (child.sppStatus == "Lunas") Emerald800 else Gold800
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Real-Time Campus Gate Presence Status with Live Pulse
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
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .graphicsLayer { this.alpha = pulseAlpha }
                            .clip(CircleShape)
                            .background(Emerald500)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Presensi Gerbang: ${child.todayAttendanceStatus}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Emerald900
                        )
                        Text(
                            text = "Tercatat di gerbang sekolah pukul ${child.todayCheckinTime}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Emerald800,
                            fontSize = 11.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Emerald700,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
            Spacer(modifier = Modifier.height(14.dp))

            // Action Row 1: WhatsApp Wali Kelas & Rapor Detail (48dp Touch Targets)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        haptics.tapLight()
                        onWhatsAppClick(
                            child.homeroomPhone,
                            "Assalamu'alaikum Ustadz ${child.homeroomTeacher}, saya orang tua dari ${child.name} ingin berkonsultasi mengenai perkembangan ananda."
                        )
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
                    Text(text = "WhatsApp Guru", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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

            // Action Row 2: Buku Saku Kedisiplinan & Konsultasi Ortu ↔ Guru
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        haptics.tapLight()
                        onDisciplineClick()
                    },
                    modifier = Modifier
                        .weight(1f)
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

                OutlinedButton(
                    onClick = {
                        haptics.tapLight()
                        onChatClick()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .sulaoneInteractiveTouchTarget(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(imageVector = Icons.Default.QuestionAnswer, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Pesan Sekolah", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
