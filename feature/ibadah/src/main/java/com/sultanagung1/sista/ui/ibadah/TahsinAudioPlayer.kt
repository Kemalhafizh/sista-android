package com.sultanagung1.sista.ui.ibadah

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.audio.AudioRecorderManager
import com.sultanagung1.sista.core.audio.AudioWaveformExtractor
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.data.model.TahsinAnnotationItem
import java.util.Locale

/**
 * Waveform playback for a previously-submitted (remote) Tahsin recording,
 * with timestamp-pinned tajwid annotations rendered as markers on the
 * waveform. In teacher mode, tapping an empty part of the waveform reports
 * that timestamp via [onRequestAnnotate] so the caller can prompt for a note
 * and call the annotate API; tapping an existing marker (either mode) shows
 * that note. The waveform itself is decoded from the real audio file
 * (AudioWaveformExtractor) — not synthesized — so it reflects the actual
 * recitation, not a generic placeholder shape.
 */
@Composable
fun TahsinAudioPlayer(
    recorderManager: AudioRecorderManager,
    audioUrl: String,
    durationSeconds: Int,
    annotations: List<TahsinAnnotationItem>,
    isTeacherMode: Boolean,
    onRequestAnnotate: (timestampSeconds: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isPlaying by recorderManager.isPlaying.collectAsState()
    val positionMs by recorderManager.playbackPositionMs.collectAsState()
    val playerDurationMs by recorderManager.playbackDurationMs.collectAsState()
    val playbackSpeed by recorderManager.playbackSpeed.collectAsState()
    val playerError by recorderManager.errorMessage.collectAsState()

    var waveform by remember(audioUrl) { mutableStateOf<List<Float>?>(null) }
    var waveformError by remember(audioUrl) { mutableStateOf<String?>(null) }
    var selectedAnnotation by remember { mutableStateOf<TahsinAnnotationItem?>(null) }

    LaunchedEffect(audioUrl) {
        waveform = null
        waveformError = null
        AudioWaveformExtractor.extract(audioUrl).fold(
            onSuccess = { waveform = it },
            onFailure = { waveformError = it.localizedMessage ?: "Gagal memuat gelombang suara" }
        )
    }

    val effectiveDurationMs = playerDurationMs.takeIf { it > 0 } ?: (durationSeconds * 1000)

    Column(modifier = modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth().height(150.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900)
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.Center) {
                val bars = waveform
                when {
                    waveformError != null -> Text(
                        text = waveformError ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentRose
                    )
                    bars == null -> CircularProgressIndicator(color = Gold400, modifier = Modifier.size(28.dp))
                    else -> {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(isTeacherMode, bars, annotations) {
                                    detectTapGestures { offset ->
                                        val ratio = (offset.x / size.width).coerceIn(0f, 1f)
                                        val tappedSeconds = (ratio * durationSeconds).toInt()

                                        // Prefer selecting a nearby existing marker over creating a new one.
                                        val nearest = annotations.minByOrNull { kotlin.math.abs(it.timestampSeconds - tappedSeconds) }
                                        val thresholdSeconds = (durationSeconds * 0.03f).coerceAtLeast(1f)
                                        if (nearest != null && kotlin.math.abs(nearest.timestampSeconds - tappedSeconds) <= thresholdSeconds) {
                                            selectedAnnotation = nearest
                                        } else {
                                            selectedAnnotation = null
                                            recorderManager.seekTo((ratio * effectiveDurationMs).toInt())
                                            if (isTeacherMode) onRequestAnnotate(tappedSeconds)
                                        }
                                    }
                                }
                        ) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height
                            val barWidth = (canvasWidth / bars.size) * 0.7f
                            val slot = canvasWidth / bars.size
                            val playedRatio = if (effectiveDurationMs > 0) (positionMs.toFloat() / effectiveDurationMs).coerceIn(0f, 1f) else 0f

                            bars.forEachIndexed { index, amp ->
                                val x = index * slot
                                val barHeight = (amp * canvasHeight * 0.85f).coerceAtLeast(4.dp.toPx())
                                val y = (canvasHeight - barHeight) / 2
                                val played = (index.toFloat() / bars.size) <= playedRatio
                                drawRoundRect(
                                    color = if (played) Emerald400 else Slate700,
                                    topLeft = Offset(x, y),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                                )
                            }

                            // Playhead
                            if (isPlaying || positionMs > 0) {
                                val playheadX = playedRatio * canvasWidth
                                drawRoundRect(
                                    color = Gold400,
                                    topLeft = Offset(playheadX - 1.dp.toPx(), 0f),
                                    size = Size(2.dp.toPx(), canvasHeight),
                                    cornerRadius = CornerRadius(1.dp.toPx())
                                )
                            }

                            // Annotation markers
                            annotations.forEach { annotation ->
                                if (durationSeconds > 0) {
                                    val markerX = (annotation.timestampSeconds.toFloat() / durationSeconds) * canvasWidth
                                    drawCircle(
                                        color = AccentAmber,
                                        radius = 4.dp.toPx(),
                                        center = Offset(markerX, 6.dp.toPx())
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) Gold600 else Emerald700)
                        .clickable {
                            if (isPlaying) {
                                recorderManager.pausePlayback()
                            } else {
                                recorderManager.startPlayback(remoteUrl = audioUrl)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color.White
                    )
                }

                val posSec = positionMs / 1000
                val durSec = (effectiveDurationMs / 1000).coerceAtLeast(durationSeconds)
                Text(
                    text = String.format(Locale.US, "%02d:%02d / %02d:%02d", posSec / 60, posSec % 60, durSec / 60, durSec % 60),
                    style = MaterialTheme.typography.labelMedium,
                    color = Slate300
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(0.75f, 1.0f, 1.25f, 1.5f).forEach { speed ->
                    val isSelected = playbackSpeed == speed
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Emerald700 else Slate800)
                            .clickable { recorderManager.setSpeed(speed) }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${speed}x",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) androidx.compose.ui.graphics.Color.White else Slate400
                        )
                    }
                }
            }
        }

        playerError?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = error, style = MaterialTheme.typography.bodySmall, color = AccentRose)
        }

        selectedAnnotation?.let { annotation ->
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Gold50)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val ts = annotation.timestampSeconds
                    Text(
                        text = "Catatan di ${String.format(Locale.US, "%02d:%02d", ts / 60, ts % 60)}" +
                            (annotation.tajwidCategory?.let { " — $it" } ?: ""),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Gold800
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(annotation.note, style = MaterialTheme.typography.bodySmall, color = Slate800)
                    annotation.teacherName?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("— $it", style = MaterialTheme.typography.labelSmall, color = Slate500)
                    }
                }
            }
        }
    }
}
