package com.sultanagung1.sista.ui.update

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.update.InAppUpdateManager
import kotlinx.coroutines.launch

/**
 * Shows only what the backend (GET mobile/config) actually reports: whether an
 * update is available/required, and the Play Store link. There is no APK-hosting
 * endpoint anywhere in sistem-terpadu, so there is no real in-app download to
 * show progress for — this opens the Play Store instead of faking a download.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatePromptScreen(
    updateManager: InAppUpdateManager,
    onNavigateBack: () -> Unit
) {
    val state by updateManager.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        updateManager.checkForUpdates()
    }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Pembaruan Aplikasi",
                subtitle = "Sultan Agung 1 Mobile",
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

                when {
                    state.isLoading -> {
                        CircularProgressIndicator(color = Emerald700)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Memeriksa pembaruan...", style = MaterialTheme.typography.bodyMedium)
                    }

                    state.errorMessage != null -> {
                        SulaoneErrorBanner(
                            message = state.errorMessage ?: "",
                            onRetry = { scope.launch { updateManager.checkForUpdates() } }
                        )
                    }

                    else -> {
                        val vc = state.versionCheck
                        Text(
                            text = if (vc?.updateAvailable == true) "Versi Baru Tersedia" else "Aplikasi Sudah Terbaru",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Versi terpasang: v${state.currentVersionName} (build ${vc?.currentClientBuild ?: "—"})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (vc != null) {
                            Text(
                                text = "Versi terbaru di server: build ${vc.latestAvailableBuild}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        state.maintenanceMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(16.dp))
                            SulaoneCard(modifier = Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Default.Build, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(msg, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }

                        if (vc?.updateRequired == true) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Pembaruan ini wajib dipasang untuk terus menggunakan aplikasi.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentRose
                            )
                        }
                    }
                }
            }

            // Action Buttons
            Column(modifier = Modifier.fillMaxWidth()) {
                val storeUrl = state.versionCheck?.storeUrl
                val updateAvailable = state.versionCheck?.updateAvailable == true
                SulaoneButton(
                    text = if (updateAvailable) "Buka Play Store" else "Aplikasi Sudah Terbaru",
                    onClick = {
                        if (updateAvailable && !storeUrl.isNullOrBlank()) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(storeUrl)))
                        }
                    },
                    icon = Icons.Default.OpenInNew
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                    enabled = state.versionCheck?.updateRequired != true
                ) {
                    Text("Nanti Saja")
                }
            }
        }
    }
}
