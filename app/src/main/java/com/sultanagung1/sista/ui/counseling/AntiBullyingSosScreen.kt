package com.sultanagung1.sista.ui.counseling

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.designsystem.*
import kotlinx.coroutines.delay

@Composable
fun AntiBullyingSosScreen(
    onNavigateBack: () -> Unit
) {
    var isTriggered by remember { mutableStateOf(false) }
    var countdown by remember { mutableIntStateOf(5) }

    LaunchedEffect(isTriggered) {
        if (isTriggered && countdown > 0) {
            while (countdown > 0) {
                delay(1000)
                countdown--
            }
        }
    }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Tombol Darurat SOS Panik",
                subtitle = "Anti-Bullying & Krisis Keamanan Siswa",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SulaoneCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "PUSAT DARURAT SISWA",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = AccentRose
                    )
                    Text(
                        text = "Tekan dan tahan tombol di bawah jika kamu mengalami atau melihat tindakan bullying/darurat.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Huge SOS Panic Button
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(AccentRose)
                            .clickable {
                                isTriggered = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Emergency,
                                contentDescription = "SOS",
                                tint = Color.White,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "SOS PANIK",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    AnimatedVisibility(visible = isTriggered) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (countdown > 0) {
                                Text(
                                    text = "Mengirim sinyal darurat dalam $countdown detik...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentRose
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = { isTriggered = false; countdown = 5 }) {
                                    Text("Batalkan")
                                }
                            } else {
                                SulaoneBadge(
                                    text = "SINYAL TERKIRIM KE TIM KESISWAAN & BK",
                                    containerColor = AccentRose.copy(alpha = 0.15f),
                                    contentColor = AccentRose
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Koordinat GPS lokasimu telah dikirimkan secara rahasia ke wali kelas dan tim keamanan madrasah.",
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    color = Emerald700
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = Emerald700, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Identitas pelapor dijamin kerahasiaannya oleh Satgas Anti-Bullying SMA Islam Sultan Agung 1.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
