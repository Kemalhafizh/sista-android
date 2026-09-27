package com.sultanagung1.sista.ui.ibadah

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.audio.AudioRecorderManager
import com.sultanagung1.sista.core.designsystem.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TahsinRecorderScreen(
    recorderManager: AudioRecorderManager,
    viewModel: TahsinRecorderViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHistory: () -> Unit = {}
) {
    val isRecording by recorderManager.isRecording.collectAsState()
    val isPlaying by recorderManager.isPlaying.collectAsState()
    val recordingSeconds by recorderManager.recordingSeconds.collectAsState()
    val amplitudes by recorderManager.amplitudes.collectAsState()
    val playbackSpeed by recorderManager.playbackSpeed.collectAsState()
    val recordedFilePath by recorderManager.recordedFilePath.collectAsState()
    val recorderError by recorderManager.errorMessage.collectAsState()

    val uiState by viewModel.uiState.collectAsState()

    var selectedSurah by remember { mutableStateOf("Surah Al-Mulk (Ayat 1-10)") }
    var showSubmitDialog by remember { mutableStateOf(false) }

    // Pulsing animation for recording button
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isRecording) 1.15f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    DisposableEffect(Unit) {
        onDispose {
            recorderManager.release()
        }
    }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Perekam Setoran Tahsin",
                subtitle = "Talaqqi & Evaluasi Makhraj Al-Qur'an",
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Riwayat Setoran",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Surah & Target Info Card
            SulaoneCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Emerald100),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = Emerald800,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Target Hafalan / Tahsin",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = selectedSurah,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    SulaoneBadge(
                        text = "Fase F",
                        containerColor = Gold100,
                        contentColor = Gold800
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Waveform Visualizer Canvas
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (amplitudes.isEmpty() && !isRecording && recordedFilePath == null) {
                        Text(
                            text = "Tekan tombol mikrofon untuk mulai merekam bacaan",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height
                            val barWidth = 6.dp.toPx()
                            val spacing = 4.dp.toPx()
                            val totalBarSlot = barWidth + spacing
                            val maxBars = (canvasWidth / totalBarSlot).toInt()

                            val displayAmplitudes = amplitudes.takeLast(maxBars)

                            displayAmplitudes.forEachIndexed { index, amp ->
                                val x = index * totalBarSlot
                                val barHeight = (amp * canvasHeight * 0.85f).coerceAtLeast(8.dp.toPx())
                                val y = (canvasHeight - barHeight) / 2

                                drawRoundRect(
                                    color = if (isRecording) Emerald400 else Gold400,
                                    topLeft = Offset(x, y),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Timer Display
            val minutes = (recordingSeconds / 10) / 60
            val seconds = (recordingSeconds / 10) % 60
            Text(
                text = String.format(Locale.US, "%02d:%02d", minutes, seconds),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = if (isRecording) AccentRose else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isRecording) "Sedang Merekam Suara..." else if (recordedFilePath != null) "Rekaman Tersimpan Siap Dikirim" else "Siap Merekam",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Circular Record / Stop Button
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(if (isRecording) AccentRose else Emerald700)
                    .clickable {
                        if (isRecording) {
                            recorderManager.stopRecording()
                        } else {
                            recorderManager.startRecording()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (isRecording) "Stop" else "Rekam",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Playback & Speed Controls (Shown after recording)
            AnimatedVisibility(visible = recordedFilePath != null && !isRecording) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SulaoneCard(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 2.dp
                    ) {
                        Text(
                            text = "Dengarkan Hasil Bacaan Talaqqi",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Play / Pause Button & Timeline
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = {
                                    if (isPlaying) recorderManager.pausePlayback() else recorderManager.startPlayback()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isPlaying) Gold600 else Emerald700
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isPlaying) "Jeda" else "Putar Audio")
                            }

                            // Playback Speed Controls (0.75x, 1.0x, 1.25x, 1.5x)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(0.75f, 1.0f, 1.25f, 1.5f).forEach { speed ->
                                    val isSelected = playbackSpeed == speed
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) Emerald700 else MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable { recorderManager.setSpeed(speed) }
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "${speed}x",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Submit Button
                    SulaoneButton(
                        text = "Kirim Setoran ke Guru Tahfidz",
                        onClick = {
                            val path = recordedFilePath
                            if (path != null && !uiState.isSubmitting) {
                                showSubmitDialog = true
                                viewModel.submitRecording(
                                    filePath = path,
                                    surahName = selectedSurah,
                                    startAyah = null,
                                    endAyah = null,
                                    durationSeconds = recordingSeconds / 10
                                )
                            }
                        },
                        icon = Icons.Default.Send,
                        isLoading = uiState.isSubmitting,
                        enabled = !uiState.isSubmitting
                    )
                }
            }

            // A real recorder/playback failure (mic denied, no file written,
            // MediaPlayer couldn't read the file) — never silently ignored.
            recorderError?.let { error ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = AccentRose,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    if (showSubmitDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!uiState.isSubmitting) {
                    showSubmitDialog = false
                    viewModel.resetState()
                    if (uiState.submitResult != null) onNavigateBack()
                }
            },
            title = {
                Text(
                    when {
                        uiState.isSubmitting -> "Mengirim Rekaman..."
                        uiState.submitResult != null -> "Setoran Terkirim"
                        uiState.errorMessage != null -> "Gagal Mengirim"
                        else -> "Mengirim Rekaman..."
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    when {
                        uiState.isSubmitting -> "Mengunggah rekaman audio tahsin $selectedSurah ke server..."
                        uiState.submitResult != null -> "Rekaman audio tahsin $selectedSurah berhasil dikirim ke guru pembimbing tahfidz Anda untuk dievaluasi."
                        uiState.errorMessage != null -> uiState.errorMessage ?: "Terjadi kesalahan."
                        else -> ""
                    }
                )
            },
            confirmButton = {
                if (!uiState.isSubmitting) {
                    Button(
                        onClick = {
                            showSubmitDialog = false
                            val succeeded = uiState.submitResult != null
                            viewModel.resetState()
                            if (succeeded) onNavigateBack()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.errorMessage != null) AccentRose else Emerald700
                        )
                    ) {
                        Text(if (uiState.errorMessage != null) "Tutup" else "Selesai")
                    }
                }
            }
        )
    }
}
