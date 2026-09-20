package com.sultanagung1.sista.ui.teacher

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
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.accessibility.sulaoneInteractiveTouchTarget
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import com.sultanagung1.sista.core.motion.springPressable
import com.sultanagung1.sista.data.model.TeacherScheduleItem
import com.sultanagung1.sista.data.model.TeachingJournalItem
import com.sultanagung1.sista.ui.common.HeaderMetadataChip
import com.sultanagung1.sista.ui.common.SulaoneExecutiveHeader

@Composable
fun TeacherDashboardScreen(
    viewModel: TeacherViewModel,
    onNavigateToAttendance: (String, String) -> Unit, // scheduleId, className
    onNavigateToJournal: (String, String, String) -> Unit, // scheduleId, className, subjectName
    onNavigateRoute: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val data = uiState.dashboardData

    Scaffold { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Executive Top App Bar (Unified Professional Design)
            item {
                SulaoneExecutiveHeader(
                    userName = data?.teacherName ?: "Ustadz Ahmad Fauzi, M.Pd",
                    titlePrefix = "Assalamu'alaikum,",
                    chips = listOf(
                        HeaderMetadataChip(
                            text = "Pendidik / Guru",
                            icon = Icons.Default.CoPresent,
                            containerColor = Emerald50,
                            borderColor = Emerald200,
                            textColor = Emerald800,
                            iconColor = Emerald700
                        ),
                        HeaderMetadataChip(
                            text = "Guru Aktif",
                            isLiveDot = true,
                            dotColor = Emerald500,
                            containerColor = Emerald50,
                            borderColor = Emerald200,
                            textColor = Emerald800
                        ),
                        HeaderMetadataChip(
                            text = "NIP: ${data?.nip ?: "198504122010011002"}",
                            containerColor = Slate100,
                            borderColor = Slate200,
                            textColor = Slate700
                        )
                    ),
                    unreadNotificationsCount = 2,
                    onAvatarClick = { onNavigateRoute("profile") },
                    onQrClick = { onNavigateRoute("scanner") },
                    onNotificationClick = { onNavigateRoute("notifications") }
                )
            }

            // 2. 2x2 Bento Metric Cards (Key Teaching KPIs)
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SulaoneMetricCard(
                            modifier = Modifier.weight(1f),
                            title = "Beban Mengajar",
                            value = "${data?.teachingHoursThisWeek ?: 24} Jam",
                            subtitle = "Target 24 Jam/Mgg",
                            badgeText = "Minggu Ini",
                            badgeColor = Emerald700,
                            badgeBackground = Emerald50,
                            icon = Icons.Default.AccessTimeFilled,
                            iconTint = Emerald700,
                            iconBackground = Emerald50
                        )

                        SulaoneMetricCard(
                            modifier = Modifier.weight(1f),
                            title = "Kelas Diampu",
                            value = "${data?.totalClasses ?: 5} Rombel",
                            subtitle = "Fisika & Matematika",
                            badgeText = "Ganjil 25/26",
                            badgeColor = AccentBlue,
                            badgeBackground = AccentBlue.copy(alpha = 0.12f),
                            icon = Icons.Default.Groups,
                            iconTint = AccentBlue,
                            iconBackground = AccentBlue.copy(alpha = 0.12f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SulaoneMetricCard(
                            modifier = Modifier.weight(1f),
                            title = "Jadwal Hari Ini",
                            value = "${data?.todaySchedules?.size ?: 0} Sesi",
                            subtitle = "Tatap Muka Kelas",
                            badgeText = "Aktif",
                            badgeColor = Gold700,
                            badgeBackground = Gold50,
                            icon = Icons.Default.CalendarToday,
                            iconTint = Gold700,
                            iconBackground = Gold50
                        )

                        SulaoneMetricCard(
                            modifier = Modifier.weight(1f),
                            title = "Jurnal Terisi",
                            value = "${data?.recentJournals?.size ?: 0} Jurnal",
                            subtitle = "Tersimpan di Cloud",
                            badgeText = "Lengkap",
                            badgeColor = AccentPurple,
                            badgeBackground = AccentPurple.copy(alpha = 0.12f),
                            icon = Icons.Default.FactCheck,
                            iconTint = AccentPurple,
                            iconBackground = AccentPurple.copy(alpha = 0.12f)
                        )
                    }
                }
            }

            // 3. Quick Action Grid for Teachers (Clean Bento)
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = "Aksi Cepat Pendidik",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.2).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TeacherQuickActionCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.AutoMirrored.Filled.FactCheck,
                            title = "Presensi Kelas",
                            subtitle = "Checklist H/I/S/A",
                            containerColor = Emerald50,
                            iconTint = Emerald700,
                            onClick = {
                                val active = data?.todaySchedules?.firstOrNull()
                                onNavigateToAttendance(active?.id ?: "s1", active?.className ?: "XII MIPA 1")
                            }
                        )
                        TeacherQuickActionCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.EditNote,
                            title = "Jurnal KBM",
                            subtitle = "Catat materi KBM",
                            containerColor = Gold50,
                            iconTint = Gold700,
                            onClick = {
                                val active = data?.todaySchedules?.firstOrNull()
                                onNavigateToJournal(
                                    active?.id ?: "s1",
                                    active?.className ?: "XII MIPA 1",
                                    active?.subjectName ?: "Fisika Tingkat Lanjut"
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TeacherQuickActionCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Security,
                            title = "Pengawas CBT",
                            subtitle = "Monitoring anti-cheat",
                            containerColor = AccentRose.copy(alpha = 0.1f),
                            iconTint = AccentRose,
                            onClick = {
                                onNavigateRoute(com.sultanagung1.sista.ui.navigation.Screen.TeacherProctor.createRoute(101L))
                            }
                        )
                        TeacherQuickActionCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.PostAdd,
                            title = "Bank Soal",
                            subtitle = "Kelola ujian daring",
                            containerColor = AccentBlue.copy(alpha = 0.1f),
                            iconTint = AccentBlue,
                            onClick = {
                                onNavigateRoute(com.sultanagung1.sista.ui.navigation.Screen.TeacherCreateExam.route)
                            }
                        )
                    }
                }
            }

            // 4. Today's Teaching Schedule (Interactive Timeline Cards)
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Jadwal Mengajar Hari Ini",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.2).sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Emerald50)
                                .padding(horizontal = 9.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${data?.todaySchedules?.size ?: 0} Sesi KBM",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Emerald800
                            )
                        }
                    }
                }
            }

            if (data?.todaySchedules.isNullOrEmpty()) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        SulaoneEmptyState(
                            icon = Icons.Default.CheckCircle,
                            title = "Tidak Ada Jadwal Hari Ini",
                            description = "Alhamdulillah, tidak ada jadwal tatap muka mengajar untuk hari ini."
                        )
                    }
                }
            } else {
                items(
                    items = data!!.todaySchedules,
                    key = { it.id },
                    contentType = { "schedule" }
                ) { schedule ->
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        TeacherScheduleCard(
                            schedule = schedule,
                            onAttendanceClick = { onNavigateToAttendance(schedule.id, schedule.className) },
                            onJournalClick = { onNavigateToJournal(schedule.id, schedule.className, schedule.subjectName) }
                        )
                    }
                }
            }

            // 5. Recent Teaching Journals
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Riwayat Jurnal KBM Terakhir",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.2).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            items(
                items = data?.recentJournals ?: emptyList(),
                key = { it.id },
                contentType = { "journal" }
            ) { journal ->
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    TeachingJournalCard(journal = journal)
                }
            }

            // Space at bottom for navigation bar
            item {
                Spacer(modifier = Modifier.height(88.dp))
            }
        }
    }
}

@Composable
private fun TeacherQuickActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    containerColor: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    val haptics = rememberHapticFeedbackHelper()

    Surface(
        modifier = modifier
            .sulaoneInteractiveTouchTarget(48.dp)
            .springPressable {
                haptics.tapLight()
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        shadowElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun TeacherScheduleCard(
    schedule: TeacherScheduleItem,
    onAttendanceClick: () -> Unit,
    onJournalClick: () -> Unit
) {
    val haptics = rememberHapticFeedbackHelper()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (schedule.isActiveNow) 1.dp else 0.5.dp,
            color = if (schedule.isActiveNow) Emerald600 else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        shadowElevation = 0.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (schedule.isActiveNow) Emerald500 else Slate400)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (schedule.isActiveNow) "SEKARANG DI KELAS" else schedule.timeSlot,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.4.sp
                        ),
                        color = if (schedule.isActiveNow) Emerald700 else Slate600
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate100)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = schedule.room,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = Slate700
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = schedule.subjectName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Rombongan Belajar: ${schedule.className}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        haptics.tapMedium()
                        onAttendanceClick()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .sulaoneInteractiveTouchTarget(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.FactCheck,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Presensi Siswa", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        haptics.tapLight()
                        onJournalClick()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .sulaoneInteractiveTouchTarget(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.8.dp, Emerald700),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Emerald700),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Isi Jurnal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TeachingJournalCard(journal: TeachingJournalItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        shadowElevation = 0.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${journal.className} • ${journal.date}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Gold50)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = journal.competencyCode,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = Gold800
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = journal.topic,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = journal.notes,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Emerald600,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Kehadiran: ${journal.attendanceSummary}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Emerald700
                )
            }
        }
    }
}
