package com.sultanagung1.sista.ui.update

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.update.InAppUpdateManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatePromptScreen(
    updateManager: InAppUpdateManager,
    onNavigateBack: () -> Unit
) {
    val versionInfo by updateManager.versionInfo.collectAsState()
    var isUpdating by remember { mutableStateOf(false) }
    var updateProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(isUpdating) {
        if (isUpdating) {
            for (step in 1..10) {
                kotlinx.coroutines.delay(250)
                updateProgress = step / 10f
            }
        }
    }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Pembaruan Aplikasi SuperApp",
                subtitle = "Sultan Agung 1 Enterprise Mobile",
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Emerald100),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = null,
                        tint = Emerald800,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Versi Baru Tersedia: v${versionInfo.latestVersion}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Versi Terpasang saat ini: v${versionInfo.currentVersion} • 18.4 MB",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Changelog Card
                SulaoneCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Catatan Rilis Resmi (Changelog):",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Emerald900
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    versionInfo.changelog.forEach { logItem ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Emerald700,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = logItem,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                if (isUpdating) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Mengunduh paket pembaruan (${(updateProgress * 100).toInt()}%)...",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Emerald800
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { updateProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Emerald700
                    )
                }
            }

            // Action Buttons
            Column(modifier = Modifier.fillMaxWidth()) {
                SulaoneButton(
                    text = if (updateProgress >= 1f) "Pasang & Mulai Ulang" else if (isUpdating) "Sedang Mengunduh..." else "Unduh & Perbarui Sekarang",
                    onClick = {
                        if (!isUpdating) {
                            isUpdating = true
                        } else if (updateProgress >= 1f) {
                            onNavigateBack()
                        }
                    },
                    icon = Icons.Default.Download
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Nanti Saja")
                }
            }
        }
    }
}
