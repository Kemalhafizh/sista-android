package com.sultanagung1.sista.ui.teacher

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import com.sultanagung1.sista.core.designsystem.*

@Composable
fun JournalFormScreen(
    scheduleId: String,
    viewModel: JournalMobileViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val schedule = viewModel.getScheduleById(scheduleId)
    val context = LocalContext.current

    var materiPokok by remember { mutableStateOf(schedule?.topic ?: "") }
    var selectedMetode by remember { mutableStateOf(schedule?.method ?: "Problem Based Learning (PBL)") }
    var selectedMedia by remember { mutableStateOf(schedule?.media ?: "Smart Proyektor & E-Learning") }
    var hadirCount by remember { mutableStateOf((schedule?.attendancePresent ?: 35).toString()) }
    var absenCount by remember { mutableStateOf((schedule?.attendanceAbsent ?: 1).toString()) }
    var isKompetensiTercapai by remember { mutableStateOf(schedule?.isCompetencyAchieved ?: true) }
    var catatanKelas by remember { mutableStateOf(schedule?.notes ?: "") }
    var tindakLanjut by remember { mutableStateOf(schedule?.followUp ?: "") }

    val metodeOptions = listOf(
        "Ceramah & Tanya Jawab",
        "Diskusi Kelompok & Presentasi",
        "Praktikum Laboratorium",
        "Problem Based Learning (PBL)",
        "Project Based Learning (PJBL)",
        "Jigsaw & Kolaboratif"
    )

    val mediaOptions = listOf(
        "Papan Tulis & Spidol",
        "Smart Proyektor & Slides",
        "Laboratorium Komputer / Sains",
        "Lembar Kerja Siswa (LKPD)",
        "Smart Proyektor & E-Learning"
    )

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Formulir Jurnal Mengajar",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Schedule Info Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Emerald800)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(Emerald900, Emerald700)
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Gold500.copy(alpha = 0.25f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.School, contentDescription = null, tint = Gold400, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    schedule?.subject ?: "Mata Pelajaran",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Kelas: ${schedule?.className ?: "-"} • ${schedule?.timeSlot ?: "-"}",
                                color = Gold200,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Input Form Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Materi & Capaian Pembelajaran", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Emerald800)

                        OutlinedTextField(
                            value = materiPokok,
                            onValueChange = { materiPokok = it },
                            label = { Text("Materi Pokok / Sub-Bab *") },
                            placeholder = { Text("Contoh: Turunan Fungsi Trigonometri & Titik Stasioner") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )

                        Text("Metode Pembelajaran:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        metodeOptions.forEach { met ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedMetode = met }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedMetode == met,
                                    onClick = { selectedMetode = met }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(met, fontSize = 12.sp)
                            }
                        }

                        Text("Media / Alat Pembelajaran:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        mediaOptions.forEach { med ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedMedia = med }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedMedia == med,
                                    onClick = { selectedMedia = med }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(med, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Kehadiran & Ketercapaian
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Presensi Siswa & Ketercapaian KBM", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Emerald800)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = hadirCount,
                                onValueChange = { hadirCount = it },
                                label = { Text("Jumlah Hadir") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = absenCount,
                                onValueChange = { absenCount = it },
                                label = { Text("Jumlah Tidak Hadir") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Kompetensi / Tujuan Tercapai", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Apakah mayoritas siswa telah tuntas mencapai KKTP hari ini?", fontSize = 11.sp, color = Slate500)
                            }
                            Switch(
                                checked = isKompetensiTercapai,
                                onCheckedChange = { isKompetensiTercapai = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Emerald700)
                            )
                        }

                        OutlinedTextField(
                            value = catatanKelas,
                            onValueChange = { catatanKelas = it },
                            label = { Text("Kendala Pembelajaran & Catatan Khusus") },
                            placeholder = { Text("Contoh: Terdapat siswa yang izin ke UKS / butuh pendampingan tambahan.") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )

                        OutlinedTextField(
                            value = tindakLanjut,
                            onValueChange = { tindakLanjut = it },
                            label = { Text("Rencana Tindak Lanjut KBM") },
                            placeholder = { Text("Contoh: Remedial bagi 3 siswa dan tugas kelompok lanjutan.") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                }
            }

            // Action Button
            item {
                Button(
                    onClick = {
                        val present = hadirCount.toIntOrNull() ?: 35
                        val absent = absenCount.toIntOrNull() ?: 1
                        viewModel.submitJournal(
                            scheduleId = scheduleId,
                            materiPokok = materiPokok,
                            metode = selectedMetode,
                            media = selectedMedia,
                            hadir = present,
                            absen = absent,
                            isKompetensiTercapai = isKompetensiTercapai,
                            catatan = catatanKelas,
                            tindakLanjut = tindakLanjut,
                            onSuccess = {
                                Toast.makeText(context, "Jurnal KBM Berhasil Disimpan!", Toast.LENGTH_LONG).show()
                                onNavigateBack()
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                    enabled = !uiState.isSubmitting
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simpan Jurnal Mengajar", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
