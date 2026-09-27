package com.sultanagung1.sista.ui.ibadah

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.data.model.MutabaahLogItem

private data class MutabaahRow(val label: String, val category: String, val isDone: (MutabaahLogItem) -> Boolean)

private val MUTABAAH_ROWS = listOf(
    MutabaahRow("Salat Tahajud", "SUNNAH") { it.sholatTahajud },
    MutabaahRow("Salat Subuh Berjamaah", "FARDHU") { it.sholatSubuh },
    MutabaahRow("Salat Dhuha", "SUNNAH") { it.sholatDhuha },
    MutabaahRow("Salat Dzuhur Berjamaah", "FARDHU") { it.sholatDzuhur },
    MutabaahRow("Salat Ashar Berjamaah", "FARDHU") { it.sholatAshar },
    MutabaahRow("Salat Maghrib Berjamaah", "FARDHU") { it.sholatMaghrib },
    MutabaahRow("Salat Isya Berjamaah", "FARDHU") { it.sholatIsya },
    MutabaahRow("Puasa Sunnah", "SUNNAH") { it.puasaSunnah },
    MutabaahRow("Sedekah", "ADAB") { it.sedekah },
    MutabaahRow("Dzikir Pagi", "ADAB") { it.dzikirPagi },
    MutabaahRow("Dzikir Sore", "ADAB") { it.dzikirSore }
)

/**
 * Read-only view of the student's most recently recorded Mutaba'ah Yaumiyah
 * log. The backend records this server-side (no self-toggle write endpoint
 * exists yet), so this screen reports what was recorded rather than letting
 * the student edit it locally and pretend it was saved.
 */
@Composable
fun MutabaahScreen(
    viewModel: IbadahViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()
    val latest = uiState.latestLog
    val completedCount = latest?.let { log -> MUTABAAH_ROWS.count { it.isDone(log) } } ?: 0
    val totalCount = MUTABAAH_ROWS.size

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Mutabaah Yaumiyah",
                subtitle = "Pembiasaan Karakter Islami Sultan Agung",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading && uiState.mutabaahLogs.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = Emerald700) }
            }
            uiState.errorMessage != null && uiState.mutabaahLogs.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SulaoneErrorBanner(
                        message = uiState.errorMessage ?: "",
                        onRetry = { viewModel.loadMutabaah() }
                    )
                }
            }
            latest == null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SulaoneEmptyState(
                        title = "Belum Ada Catatan Mutaba'ah",
                        description = "Guru/wali kelas belum mencatat amalan harian Anda. Catatan akan muncul di sini setelah direkam oleh sekolah.",
                        icon = Icons.Default.Mosque
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (isDark) MaterialTheme.colorScheme.background else Slate50)
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
                ) {
                    item {
                        SulaoneGradientCard(
                            brush = Brush.linearGradient(listOf(Emerald900, Emerald700)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Mosque,
                                        contentDescription = null,
                                        tint = Gold400,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "CATATAN TERAKHIR",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Gold400
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = latest.date,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Emerald100
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "$completedCount dari $totalCount Amalan",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        if (latest.tadarusPages > 0) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Tadarus: ${latest.tadarusPages} halaman",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Emerald100
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Box(contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(
                                            progress = { if (totalCount > 0) completedCount.toFloat() / totalCount else 0f },
                                            modifier = Modifier.size(64.dp),
                                            color = Gold400,
                                            trackColor = Emerald900,
                                            strokeWidth = 6.dp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    items(MUTABAAH_ROWS, key = { it.label }) { row ->
                        val isDone = row.isDone(latest)
                        val cardBg = if (isDone) {
                            if (isDark) Emerald900.copy(alpha = 0.2f) else Emerald50
                        } else {
                            if (isDark) MaterialTheme.colorScheme.surface else Color.White
                        }
                        val borderColor = if (isDark) Slate800 else Slate200

                        ModernBentoCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = cardBg,
                            borderColor = borderColor,
                            elevation = 0.dp,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isDone) Emerald600 else Slate300.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isDone) Icons.Default.Check else Icons.Default.Close,
                                        contentDescription = null,
                                        tint = if (isDone) Color.White else Slate500,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = row.label,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) MaterialTheme.colorScheme.onSurface else Slate900
                                    )
                                }
                                SulaoneBadge(
                                    text = row.category,
                                    containerColor = when (row.category) {
                                        "FARDHU" -> if (isDark) Emerald800 else Emerald100
                                        "SUNNAH" -> if (isDark) Gold800 else Gold100
                                        else -> if (isDark) AccentBlue else AccentBlue.copy(alpha = 0.15f)
                                    },
                                    contentColor = when (row.category) {
                                        "FARDHU" -> if (isDark) Color.White else Emerald800
                                        "SUNNAH" -> if (isDark) Color.White else Gold800
                                        else -> if (isDark) Color.White else AccentBlue
                                    }
                                )
                            }
                        }
                    }

                    if (!latest.catatanHarian.isNullOrBlank()) {
                        item {
                            SulaoneCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Catatan Guru",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = latest.catatanHarian, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
