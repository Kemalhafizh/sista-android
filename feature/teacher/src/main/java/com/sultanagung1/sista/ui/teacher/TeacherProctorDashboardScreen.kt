package com.sultanagung1.sista.ui.teacher

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.core.haptics.rememberHapticFeedbackHelper
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherProctorDashboardScreen(
    examId: Long,
    viewModel: CbtProctorViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptics = rememberHapticFeedbackHelper()
    var studentIdInput by remember { mutableStateOf("") }

    LaunchedEffect(examId) {
        viewModel.loadToken(examId)
    }

    // Live countdown derived from the server-reported remaining_seconds, re-synced
    // whenever a fresh token arrives (initial load or regenerate).
    var remainingSeconds by remember { mutableIntStateOf(0) }
    LaunchedEffect(uiState.token?.accessToken) {
        remainingSeconds = uiState.token?.remainingSeconds ?: 0
        while (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
        }
    }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Pengawas Ujian Daring (Proctor)",
                subtitle = uiState.token?.title ?: "Ujian #$examId",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            uiState.errorMessage?.let { message ->
                SulaoneErrorBanner(
                    message = message,
                    onRetry = { viewModel.loadToken(examId) }
                )
            }

            // Live Entry Token Card (real: teacher/cbt/exams/{id}/token)
            val token = uiState.token
            val isExpired = token?.isExpired ?: true
            val tokenAccentColor = when {
                isExpired -> AccentRose
                remainingSeconds < 60 -> AccentRose
                remainingSeconds < 120 -> Gold600
                else -> Emerald700
            }
            val tokenBgColor = when {
                isExpired -> AccentRose.copy(alpha = 0.1f)
                remainingSeconds < 60 -> AccentRose.copy(alpha = 0.1f)
                remainingSeconds < 120 -> Gold50
                else -> Emerald50
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = tokenBgColor,
                border = BorderStroke(1.5.dp, tokenAccentColor.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(tokenAccentColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = tokenAccentColor, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "TOKEN MASUK UJIAN (CBT)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = tokenAccentColor
                            )
                            Text(
                                text = "Berlaku 5 menit sejak diterbitkan server",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = Slate600
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            uiState.isLoading && token == null -> CircularProgressIndicator(modifier = Modifier.size(28.dp), color = tokenAccentColor)
                            token == null -> Text("Token belum dimuat", color = Slate500)
                            else -> Text(
                                text = if (isExpired) "------" else token.accessToken.chunked(1).joinToString(" "),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    letterSpacing = 6.sp
                                ),
                                color = if (isExpired) Slate400 else Slate900
                            )
                        }
                    }

                    if (token != null) {
                        LinearProgressIndicator(
                            progress = { (remainingSeconds.coerceIn(0, 300)) / 300f },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = tokenAccentColor,
                            trackColor = MaterialTheme.colorScheme.surface
                        )
                        Text(
                            text = if (isExpired) "Token kedaluwarsa" else "Sisa waktu: ${remainingSeconds / 60}:${(remainingSeconds % 60).toString().padStart(2, '0')}",
                            style = MaterialTheme.typography.labelSmall,
                            color = tokenAccentColor
                        )
                    }

                    Button(
                        onClick = {
                            haptics.tapHeavy()
                            viewModel.regenerateToken(examId)
                        },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = tokenAccentColor)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Terbitkan Token Baru (5 Mnt)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Manual student reset (real: teacher/cbt/exams/{id}/token/reset-student).
            // There is no live participant/progress feed API yet, so this screen does
            // not show a fabricated roster — a teacher acts on a student ID they
            // already know is locked out (e.g. reported verbally in the exam room).
            SulaoneCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Buka Kunci Siswa (Force-Closed)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Masukkan ID siswa yang terkunci akibat pelanggaran anti-cheat untuk mengizinkan mereka masuk kembali dengan token baru.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )
                    OutlinedTextField(
                        value = studentIdInput,
                        onValueChange = { studentIdInput = it.filter { c -> c.isDigit() } },
                        label = { Text("ID Siswa") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            studentIdInput.toLongOrNull()?.let { id ->
                                haptics.tapHeavy()
                                viewModel.resetStudent(examId, id)
                            }
                        },
                        enabled = studentIdInput.toLongOrNull() != null && !uiState.isResettingStudent,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                    ) {
                        if (uiState.isResettingStudent) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = androidx.compose.ui.graphics.Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset Akses Siswa")
                        }
                    }
                    uiState.resetResultMessage?.let { msg ->
                        Text(msg, style = MaterialTheme.typography.bodySmall, color = Emerald700)
                    }
                }
            }

            // Honest placeholder: a live roster with per-student progress requires a
            // backend endpoint that does not exist yet (only proctor *actions* —
            // warning/force-submit/extend-time/reset — are implemented server-side).
            SulaoneEmptyState(
                title = "Pemantauan Peserta Live Belum Tersedia",
                description = "Daftar peserta ujian beserta progres jawaban real-time memerlukan endpoint API tambahan di backend yang belum dibangun. Fitur token dan reset akses di atas sudah terhubung ke server.",
                icon = Icons.Default.MonitorHeart,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
