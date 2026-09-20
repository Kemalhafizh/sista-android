package com.sultanagung1.sista.ui.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sultanagung1.sista.core.designsystem.Emerald600
import com.sultanagung1.sista.core.designsystem.SulaoneTopBar

@Composable
fun NotificationSettingsScreen(
    viewModel: NotificationViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val pref = uiState.preferences

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Preferensi Notifikasi",
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Kategori Notifikasi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Pilih notifikasi yang ingin Anda terima secara langsung",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        PreferenceSwitchItem(
                            title = "Ujian & Asesmen CBT",
                            subtitle = "Pengingat jadwal ujian, token masuk & pengumuman nilai",
                            checked = pref.examNotifsEnabled,
                            onCheckedChange = { viewModel.updatePreferences(pref.copy(examNotifsEnabled = it)) }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        PreferenceSwitchItem(
                            title = "Tugas & LMS E-Learning",
                            subtitle = "Batas waktu pengumpulan tugas dan materi baru",
                            checked = pref.assignmentNotifsEnabled,
                            onCheckedChange = { viewModel.updatePreferences(pref.copy(assignmentNotifsEnabled = it)) }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        PreferenceSwitchItem(
                            title = "Informasi & Pengumuman Sekolah",
                            subtitle = "Surat edaran resmi dan kalender agenda kegiatan",
                            checked = pref.infoNotifsEnabled,
                            onCheckedChange = { viewModel.updatePreferences(pref.copy(infoNotifsEnabled = it)) }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        PreferenceSwitchItem(
                            title = "Waktu Salat & Ibadah Yaumiyah",
                            subtitle = "Pengingat adzan salat 5 waktu dan mutaba'ah malam",
                            checked = pref.prayerNotifsEnabled,
                            onCheckedChange = { viewModel.updatePreferences(pref.copy(prayerNotifsEnabled = it)) }
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Mode Ringkasan (Digest Mode)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = "Atur frekuensi pengiriman notifikasi berkala",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val modes = listOf("realtime" to "Seketika", "morning" to "Pagi Hari (06.00)", "evening" to "Sore Hari (17.00)")
                        modes.forEachIndexed { index, (key, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = label, style = MaterialTheme.typography.bodyMedium)
                                RadioButton(
                                    selected = pref.digestMode == key,
                                    onClick = { viewModel.updatePreferences(pref.copy(digestMode = key)) },
                                    colors = RadioButtonDefaults.colors(selectedColor = Emerald600)
                                )
                            }
                            if (index < modes.size - 1) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PreferenceSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Emerald600)
        )
    }
}
