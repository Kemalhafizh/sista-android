package com.sultanagung1.sista.ui.profile

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import com.sultanagung1.sista.core.telemetry.*

/**
 * Layar Pusat Diagnostik & Laporan Kendala Sistem (FASE 64).
 * Menyajikan observabilitas komprehensif bagi pengguna, guru, atau staf IT untuk
 * memeriksa kesehatan aplikasi, meninjau jejak aktivitas (breadcrumbs), dan membagikan
 * laporan kendala teknis yang telah disanitasi langsung ke Helpdesk IT Sekolah.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticReportScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptics = rememberHapticFeedbackHelper()

    val hub = remember { SulaoneTelemetryHub.instance }
    val crashRepo = remember { hub.crashRepository ?: CrashReportRepository(context) }

    var latestCrash by remember { mutableStateOf(crashRepo.getLatestCrashReport()) }
    var pendingCount by remember { mutableStateOf(crashRepo.getPendingReportCount()) }
    var breadcrumbs by remember { mutableStateOf(hub.getRecentBreadcrumbs().takeLast(15).reversed()) }
    var showCrashConfirmDialog by remember { mutableStateOf(false) }

    // Dapatkan data hardware & lingkungan saat ini
    val ramInfo = remember {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val mem = ActivityManager.MemoryInfo()
        am?.getMemoryInfo(mem)
        val freeMb = mem.availMem / (1024 * 1024)
        val totalMb = mem.totalMem / (1024 * 1024)
        "$freeMb MB bebas / $totalMb MB total"
    }

    val networkStatus = remember {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val caps = cm?.getNetworkCapabilities(cm.activeNetwork)
        when {
            caps == null -> "Offline"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi Aktif"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Seluler 4G/5G"
            else -> "Terhubung"
        }
    }

    val coldStartMs = remember {
        val dur = AppStartupTracker.getColdStartDurationMs()
        if (dur > 0) "${dur}ms" else "< 1200ms"
    }

    val isFirebaseReady = hub.firebaseSink?.isAvailable() == true

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Pusat Diagnostik & Kendala",
                subtitle = "Observabilitas Sistem & Laporan IT",
                onNavigateBack = {
                    haptics.tapLight()
                    onNavigateBack()
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 12.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Status Kesehatan Sistem & Telemetri
            item(key = "telemetry_health_card") {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Speed,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Status Telemetri & Mesin",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Monitoring otomatis APM SULAONE",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        DiagnosticMetricRow(label = "Firebase Crashlytics", value = if (isFirebaseReady) "Terhubung & Aktif" else "Lokal Mandiri (Safe)")
                        DiagnosticMetricRow(label = "Model Perangkat", value = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}")
                        DiagnosticMetricRow(label = "Versi Android", value = "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
                        DiagnosticMetricRow(label = "Ketersediaan RAM", value = ramInfo)
                        DiagnosticMetricRow(label = "Koneksi Jaringan", value = networkStatus)
                        DiagnosticMetricRow(label = "Cold Start Startup", value = coldStartMs)
                        DiagnosticMetricRow(label = "Versi Aplikasi", value = "2.0 Enterprise Release")
                    }
                }
            }

            // 2. Status Stabilitas Sesi & Riwayat Crash Terakhir
            item(key = "crash_status_card") {
                val crash = latestCrash
                if (crash != null) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.error),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onError,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Insiden Kerusakan Terdeteksi ($pendingCount)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = "Waktu: ${crash.getFormattedDate()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Text(
                                text = "${crash.exceptionType}: ${crash.exceptionMessage}",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = {
                                        haptics.tapMedium()
                                        val summary = crashRepo.exportReportSummary(crash)
                                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, "Laporan Kendala SISTA - ${crash.reportId.take(8)}")
                                            putExtra(Intent.EXTRA_TEXT, summary)
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Kirim Laporan ke IT Sekolah"))
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .sizeIn(minHeight = 48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Share,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Kirim ke IT", fontSize = 13.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        haptics.tapLight()
                                        crashRepo.clearAllReports()
                                        latestCrash = null
                                        pendingCount = 0
                                        Toast.makeText(context, "Riwayat crash dibersihkan", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.sizeIn(minHeight = 48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DeleteOutline,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Bersihkan", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                } else {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Sistem Berjalan Sangat Stabil",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Tidak ada rekaman crash atau error fatal pada sesi ini.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 3. Jejak Aktivitas Terbaru (Breadcrumbs)
            item(key = "breadcrumbs_header") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Jejak Aktivitas Terkini (Breadcrumbs)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(
                        onClick = {
                            haptics.tapLight()
                            hub.logBreadcrumb(
                                category = BreadcrumbCategory.USER_ACTION,
                                message = "Pengujian diagnostik manual dicatat oleh pengguna"
                            )
                            breadcrumbs = hub.getRecentBreadcrumbs().takeLast(15).reversed()
                        },
                        modifier = Modifier.sizeIn(minHeight = 48.dp)
                    ) {
                        Text("Perbarui", fontSize = 12.sp)
                    }
                }
            }

            if (breadcrumbs.isEmpty()) {
                item(key = "empty_breadcrumbs") {
                    Text(
                        text = "Belum ada jejak aktivitas tercatat.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(
                    items = breadcrumbs,
                    key = { "${it.timestampMs}_${it.message.take(10)}" }
                ) { bc ->
                    BreadcrumbItemRow(breadcrumb = bc)
                }
            }

            // 4. Aksi Bantuan & Uji Diagnostik
            item(key = "action_buttons_footer") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            haptics.tapLight()
                            val summary = buildDiagnosticOverview(
                                ramInfo = ramInfo,
                                networkStatus = networkStatus,
                                coldStartMs = coldStartMs,
                                isFirebaseReady = isFirebaseReady,
                                recentBreadcrumbs = breadcrumbs
                            )
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Laporan Diagnostik Sistem SISTA - ${Build.MODEL}")
                                putExtra(Intent.EXTRA_TEXT, summary)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Bagikan Laporan Diagnostik"))
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .sizeIn(minHeight = 48.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bagikan Diagnostik Lengkap ke IT Sekolah")
                    }

                    TextButton(
                        onClick = {
                            haptics.tapMedium()
                            showCrashConfirmDialog = true
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .sizeIn(minHeight = 48.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.BugReport, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simulasi Uji Crash (Verifikasi Handler)")
                    }
                }
            }
        }
    }

    // Modal Konfirmasi Uji Crash
    if (showCrashConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showCrashConfirmDialog = false },
            title = {
                Text("Uji Kerusakan Terkendali", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Aplikasi akan sengaja memicu RuntimeException terkontrol untuk membuktikan bahwa SulaoneCrashHandler dan Firebase Crashlytics mencatat crash dump dengan benar. Lanjutkan?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCrashConfirmDialog = false
                        hub.logBreadcrumb(
                            category = BreadcrumbCategory.SYSTEM,
                            message = "Simulasi crash dipicu secara sengaja dari Pusat Diagnostik",
                            level = BreadcrumbLevel.WARN
                        )
                        throw RuntimeException("Uji Diagnostik SISTA: Simulasi Crash Sukses Ditangkap oleh Handler!")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.sizeIn(minHeight = 48.dp)
                ) {
                    Text("Picukan Crash")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCrashConfirmDialog = false },
                    modifier = Modifier.sizeIn(minHeight = 48.dp)
                ) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun DiagnosticMetricRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun BreadcrumbItemRow(
    breadcrumb: Breadcrumb
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val badgeColor = when (breadcrumb.category) {
                BreadcrumbCategory.NAVIGATION -> MaterialTheme.colorScheme.primary
                BreadcrumbCategory.NETWORK -> MaterialTheme.colorScheme.secondary
                BreadcrumbCategory.LIFECYCLE -> MaterialTheme.colorScheme.tertiary
                BreadcrumbCategory.USER_ACTION -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(badgeColor.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = breadcrumb.category.name.take(4),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = breadcrumb.message,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = breadcrumb.getFormattedTime().substringAfter(" "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

private fun buildDiagnosticOverview(
    ramInfo: String,
    networkStatus: String,
    coldStartMs: String,
    isFirebaseReady: Boolean,
    recentBreadcrumbs: List<Breadcrumb>
): String {
    val sb = StringBuilder()
    sb.append("📱 *LAPORAN DIAGNOSTIK KESEHATAN SISTEM SISTA*\n")
    sb.append("---------------------------------------------\n")
    sb.append("📱 Perangkat: ${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})\n")
    sb.append("🧠 Memori: $ramInfo\n")
    sb.append("🌐 Jaringan: $networkStatus\n")
    sb.append("⏱️ Cold Start: $coldStartMs\n")
    sb.append("🔥 Firebase: ${if (isFirebaseReady) "Aktif" else "Lokal"}\n\n")

    sb.append("🍞 *Jejak Aktivitas Terakhir:*\n")
    recentBreadcrumbs.take(8).forEach { bc ->
        sb.append("• [${bc.category.name}] ${bc.message}\n")
    }
    sb.append("---------------------------------------------\n")
    sb.append("SMA Islam Sultan Agung 1 Semarang • SISTA Super App")
    return sb.toString()
}
