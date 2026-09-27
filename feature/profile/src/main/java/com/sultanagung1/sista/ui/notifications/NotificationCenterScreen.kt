package com.sultanagung1.sista.ui.notifications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.data.model.NotificationChannelType
import com.sultanagung1.sista.data.model.NotificationItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterScreen(
    viewModel: NotificationViewModel,
    onNavigateDeepLink: (String) -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToSettings: (() -> Unit)? = null,
    // Developer-only push simulator. The app module passes BuildConfig.DEBUG;
    // defaults to false so release builds never show the test dispatcher.
    showDebugTools: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showTestDispatchDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.testDispatchMessage) {
        uiState.testDispatchMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearTestDispatchMessage()
        }
    }

    val filterOptions = listOf(
        "Semua",
        "Presensi",
        "Akademik",
        "Keuangan",
        "Darurat"
    )

    val filteredNotifications = remember(uiState.notifications, uiState.selectedFilter) {
        when (uiState.selectedFilter) {
            "Presensi" -> uiState.notifications.filter { it.channel.contains("attendance", ignoreCase = true) }
            "Akademik" -> uiState.notifications.filter { it.channel.contains("academic", ignoreCase = true) }
            "Keuangan" -> uiState.notifications.filter { it.channel.contains("financial", ignoreCase = true) }
            "Darurat" -> uiState.notifications.filter { it.channel.contains("emergency", ignoreCase = true) }
            else -> uiState.notifications
        }
    }

    val unreadCount = remember(uiState.notifications) {
        uiState.notifications.count { !it.isRead }
    }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Pusat Notifikasi",
                subtitle = "SMA Islam Sultan Agung 1 Semarang",
                onNavigateBack = onNavigateBack,
                actions = {
                    if (unreadCount > 0) {
                        TextButton(onClick = { viewModel.markAllAsRead() }) {
                            Text("Tandai Dibaca", fontSize = 11.sp, color = Emerald800, fontWeight = FontWeight.Bold)
                        }
                    }
                    onNavigateToSettings?.let { toSettings ->
                        IconButton(onClick = toSettings) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = "Pengaturan", tint = Emerald800)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (showDebugTools) {
                ExtendedFloatingActionButton(
                    onClick = { showTestDispatchDialog = true },
                    containerColor = Emerald800,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.NotificationsActive, contentDescription = null) },
                    text = { Text("Uji Push Notifikasi", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // Filter Pills Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterOptions) { filter ->
                    val isSelected = uiState.selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectFilter(filter) },
                        label = {
                            Text(
                                text = filter,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Emerald700,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }

            var isRefreshing by remember { mutableStateOf(false) }
            val coroutineScope = rememberCoroutineScope()

            SulaonePullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    coroutineScope.launch {
                        viewModel.loadNotifications()
                        delay(600)
                        isRefreshing = false
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) {
                // Notification List
                if (filteredNotifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        SulaoneEmptyState(
                            icon = Icons.Default.NotificationsNone,
                            title = "Tidak Ada Notifikasi",
                            description = "Semua pembaruan terkait kegiatan sekolah, presensi, dan tagihan akan tampil di sini."
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredNotifications) { item ->
                            NotificationCard(
                                item = item,
                                onClick = {
                                    viewModel.markAsRead(item.id)
                                    item.deepLinkRoute?.let { route ->
                                        onNavigateDeepLink(route)
                                    }
                                },
                                onDelete = {
                                    viewModel.deleteNotification(item.id)
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(72.dp))
                        }
                    }
                }
            }
        }
    }

    if (showDebugTools && showTestDispatchDialog) {
        AlertDialog(
            onDismissRequest = { showTestDispatchDialog = false },
            title = {
                Text(
                    text = "🧪 Simulator Push Notification Android",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Pilih skenario notifikasi yang ingin dikirimkan langsung ke status bar HP Android Anda:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )

                    TestNotifOptionButton(
                        title = "Presensi Gerbang Masuk",
                        subtitle = "Saluran: attendance_alerts",
                        icon = Icons.Default.LocationOn,
                        color = Emerald700,
                        onClick = {
                            viewModel.dispatchTestPushNotification(
                                channelType = NotificationChannelType.ATTENDANCE,
                                title = "Presensi Gerbang: Hadir Tepat Waktu",
                                body = "Ahmad Kemal Hafizh telah check-in di Gerbang Utama SMA Sultan Agung 1 (06:45 WIB).",
                                deepLinkRoute = "geofence_attendance"
                            )
                            showTestDispatchDialog = false
                        }
                    )

                    TestNotifOptionButton(
                        title = "Nilai Rapor KKTP Rilis",
                        subtitle = "Saluran: academic_updates",
                        icon = Icons.Default.School,
                        color = AccentBlue,
                        onClick = {
                            viewModel.dispatchTestPushNotification(
                                channelType = NotificationChannelType.ACADEMIC,
                                title = "Nilai Sumatif Gasal Dirilis",
                                body = "Nilai mata pelajaran Fisika Modern (Capaian KKTP: 94) telah diverifikasi Wali Kelas.",
                                deepLinkRoute = "grades"
                            )
                            showTestDispatchDialog = false
                        }
                    )

                    TestNotifOptionButton(
                        title = "Tagihan SPP Virtual Account BSI",
                        subtitle = "Saluran: financial_reminders",
                        icon = Icons.Default.AccountBalanceWallet,
                        color = AccentAmber,
                        onClick = {
                            viewModel.dispatchTestPushNotification(
                                channelType = NotificationChannelType.FINANCE,
                                title = "Pengingat Pembayaran SPP Bulan Ini",
                                body = "Tagihan SPP sebesar Rp 450.000 telah tersedia via VA BSI 88219324567890.",
                                deepLinkRoute = "billing"
                            )
                            showTestDispatchDialog = false
                        }
                    )

                    TestNotifOptionButton(
                        title = "Siaran Darurat Kampus",
                        subtitle = "Saluran: emergency_broadcast",
                        icon = Icons.Default.Warning,
                        color = AccentRose,
                        onClick = {
                            viewModel.dispatchTestPushNotification(
                                channelType = NotificationChannelType.EMERGENCY,
                                title = "SIARAN DARURAT SEKOLAH",
                                body = "Penyesuaian Kegiatan Belajar Mengajar sehubungan kewaspadaan cuaca ekstrem Kota Semarang.",
                                deepLinkRoute = "announcement_feed"
                            )
                            showTestDispatchDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showTestDispatchDialog = false }) {
                    Text("Tutup", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun NotificationCard(
    item: NotificationItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val (icon, iconColor, bgColor) = when {
        item.channel.contains("attendance", ignoreCase = true) ->
            Triple(Icons.Default.LocationOn, Emerald700, Emerald50)
        item.channel.contains("academic", ignoreCase = true) ->
            Triple(Icons.Default.School, AccentBlue, AccentBlue.copy(alpha = 0.12f))
        item.channel.contains("financial", ignoreCase = true) ->
            Triple(Icons.Default.AccountBalanceWallet, AccentAmber, AccentAmber.copy(alpha = 0.12f))
        item.channel.contains("emergency", ignoreCase = true) ->
            Triple(Icons.Default.Warning, AccentRose, AccentRose.copy(alpha = 0.12f))
        else ->
            Triple(Icons.Default.Campaign, AccentPurple, AccentPurple.copy(alpha = 0.12f))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!item.isRead) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(if (!item.isRead) 2.dp else 0.dp),
        border = if (!item.isRead) androidx.compose.foundation.BorderStroke(1.dp, Emerald600.copy(alpha = 0.4f)) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (!item.isRead) FontWeight.Bold else FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (!item.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Gold500)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (!item.isRead) Slate800 else Slate500,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400,
                        fontSize = 10.sp
                    )

                    if (item.deepLinkRoute != null) {
                        Text(
                            text = "Buka Halaman →",
                            style = MaterialTheme.typography.labelSmall,
                            color = Emerald800,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TestNotifOptionButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.08f),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Slate900)
                Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = Slate500, fontSize = 10.sp)
            }
        }
    }
}
