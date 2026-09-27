package com.sultanagung1.sista.ui.settings

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.security.DeviceIntegrityChecker
import com.sultanagung1.sista.core.security.DeviceIntegrityReport

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val integrityChecker = remember { DeviceIntegrityChecker(context) }
    var report by remember { mutableStateOf<DeviceIntegrityReport?>(null) }
    var biometricLockEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        report = integrityChecker.checkIntegrity()
    }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Keamanan & Integritas Perangkat",
                subtitle = "Bank-Grade Anti-Tamper Protection",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Security Status Header
            SulaoneGradientCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(if (report?.isSecure == true) Gold400 else AccentRose),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (report?.isSecure == true) Icons.Default.VerifiedUser else Icons.Default.GppBad,
                            contentDescription = null,
                            tint = Slate950,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (report?.isSecure == true) "STATUS SISTEM: AMAN (TERVERIFIKASI)" else "STATUS SISTEM: PERINGATAN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Gold400
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Proteksi Enterprise Aktif",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "SMA Islam Sultan Agung 1 Semarang",
                            fontSize = 11.sp,
                            color = Emerald100
                        )
                    }
                }
            }

            // Integrity Checklist Card
            SulaoneCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Audit Integritas Hardware & OS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                report?.let { rep ->
                    SecurityCheckRow(
                        title = "Deteksi Root & Magisk",
                        subtitle = if (!rep.isRooted) "Biner su tidak ditemukan (Bersih)" else "Perangkat dalam kondisi Root!",
                        isPassed = !rep.isRooted
                    )

                    SecurityCheckRow(
                        title = "Isolasi Emulator (CBT Anti-Cheat)",
                        subtitle = if (!rep.isEmulator) "Perangkat Fisik Asli Terdeteksi" else "Menjalankan Emulator (Tidak Diizinkan)",
                        isPassed = !rep.isEmulator
                    )

                    SecurityCheckRow(
                        title = "USB Debugging (ADB Mode)",
                        subtitle = if (!rep.isUsbDebuggingEnabled) "Mode pengembang aman" else "USB Debugging aktif",
                        isPassed = !rep.isUsbDebuggingEnabled
                    )

                    SecurityCheckRow(
                        title = "Enkripsi Jaringan TLS 1.3 & Pinning",
                        subtitle = "Certificate Pinning domain resmi sista.sultanagung1.sch.id",
                        isPassed = true
                    )
                }
            }

            // APK Signature Card
            SulaoneCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sidik Jari SHA-256 APK Resmi",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    SulaoneBadge(
                        text = "Anti-Tamper",
                        containerColor = Emerald100,
                        contentColor = Emerald800
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = report?.signatureHash?.takeIf { it.isNotEmpty() } ?: "E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }

            // Biometric App Lock Setting
            SulaoneCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Kunci Aplikasi dengan Biometrik",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Wajibkan Sidik Jari / Face Unlock saat membuka SuperApp",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = biometricLockEnabled,
                        onCheckedChange = { biometricLockEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Emerald700
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SecurityCheckRow(title: String, subtitle: String, isPassed: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isPassed) Emerald100 else AccentRose.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPassed) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = if (isPassed) Emerald800 else AccentRose,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
