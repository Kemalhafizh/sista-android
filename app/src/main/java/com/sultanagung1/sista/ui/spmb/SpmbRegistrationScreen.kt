package com.sultanagung1.sista.ui.spmb

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import com.sultanagung1.sista.core.designsystem.*

@Composable
fun SpmbRegistrationScreen(
    viewModel: SpmbViewModel,
    onNavigateBack: () -> Unit,
    onRegistrationSuccess: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var agreedToIntegrity by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Formulir Pendaftaran SPMB",
                onNavigateBack = {
                    if (uiState.currentStep > 1) {
                        viewModel.prevStep()
                    } else {
                        onNavigateBack()
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (uiState.currentStep > 1) {
                        OutlinedButton(
                            onClick = { viewModel.prevStep() },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Sebelumnya")
                        }
                    }

                    Button(
                        onClick = {
                            if (uiState.currentStep < 4) {
                                val ok = viewModel.nextStep()
                                if (!ok && uiState.errorMessage != null) {
                                    Toast.makeText(context, uiState.errorMessage, Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                if (!agreedToIntegrity) {
                                    Toast.makeText(context, "Harap setujui pernyataan integritas terlebih dahulu.", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.submitRegistration { regNumber ->
                                        Toast.makeText(context, "Pendaftaran Berhasil! $regNumber", Toast.LENGTH_LONG).show()
                                        onRegistrationSuccess(regNumber)
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                        enabled = !uiState.isSubmitting
                    ) {
                        if (uiState.isSubmitting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                if (uiState.currentStep == 4) "Kirim Pendaftaran" else "Selanjutnya",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                StepIndicatorBar(currentStep = uiState.currentStep)
            }

            when (uiState.currentStep) {
                1 -> item {
                    Step1DataDiri(
                        draft = uiState.draft,
                        onUpdate = { viewModel.updateDraft(it) }
                    )
                }
                2 -> item {
                    Step2DataOrtu(
                        draft = uiState.draft,
                        onUpdate = { viewModel.updateDraft(it) }
                    )
                }
                3 -> item {
                    Step3UploadDokumen(
                        draft = uiState.draft,
                        onUpload = { docType ->
                            viewModel.simulateUploadDoc(docType)
                            Toast.makeText(context, "Dokumen berhasil diunggah!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                4 -> item {
                    Step4Konfirmasi(
                        draft = uiState.draft,
                        agreed = agreedToIntegrity,
                        onAgreedChange = { agreedToIntegrity = it },
                        onTrackChange = { track ->
                            viewModel.updateDraft { it.copy(selectedTrack = track) }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun StepIndicatorBar(currentStep: Int) {
    val stepTitles = listOf("Data Diri", "Orang Tua", "Dokumen", "Konfirmasi")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            stepTitles.forEachIndexed { index, title ->
                val stepNum = index + 1
                val isActive = stepNum == currentStep
                val isDone = stepNum < currentStep

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isDone -> Emerald600
                                    isActive -> Emerald700
                                    else -> Slate300
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDone) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        } else {
                            Text(
                                stepNum.toString(),
                                color = if (isActive) Color.White else Slate700,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        title,
                        fontSize = 10.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        color = if (isActive) Emerald800 else Slate600
                    )
                }
            }
        }
    }
}

@Composable
fun Step1DataDiri(
    draft: com.sultanagung1.sista.data.model.SpmbRegistrationDraft,
    onUpdate: ((com.sultanagung1.sista.data.model.SpmbRegistrationDraft) -> com.sultanagung1.sista.data.model.SpmbRegistrationDraft) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Langkah 1: Data Pribadi Calon Siswa", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Emerald800)

            OutlinedTextField(
                value = draft.fullName,
                onValueChange = { newVal -> onUpdate { it.copy(fullName = newVal) } },
                label = { Text("Nama Lengkap (Sesuai Ijazah SMP) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = draft.nisn,
                onValueChange = { newVal -> onUpdate { it.copy(nisn = newVal) } },
                label = { Text("NISN (Nomor Induk Siswa Nasional) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = draft.schoolOrigin,
                onValueChange = { newVal -> onUpdate { it.copy(schoolOrigin = newVal) } },
                label = { Text("Asal SMP / MTs *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = draft.birthPlaceDate,
                onValueChange = { newVal -> onUpdate { it.copy(birthPlaceDate = newVal) } },
                label = { Text("Tempat, Tanggal Lahir (Contoh: Semarang, 12 Mei 2011)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = draft.avgScore,
                onValueChange = { newVal -> onUpdate { it.copy(avgScore = newVal) } },
                label = { Text("Nilai Rata-rata Rapor Semester 1-5 (Contoh: 88.5)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text("Jenis Kelamin:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("Laki-laki", "Perempuan").forEach { g ->
                    FilterChip(
                        selected = draft.gender == g,
                        onClick = { onUpdate { it.copy(gender = g) } },
                        label = { Text(g) }
                    )
                }
            }
        }
    }
}

@Composable
fun Step2DataOrtu(
    draft: com.sultanagung1.sista.data.model.SpmbRegistrationDraft,
    onUpdate: ((com.sultanagung1.sista.data.model.SpmbRegistrationDraft) -> com.sultanagung1.sista.data.model.SpmbRegistrationDraft) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Langkah 2: Data Orang Tua / Wali", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Emerald800)

            OutlinedTextField(
                value = draft.fatherName,
                onValueChange = { newVal -> onUpdate { it.copy(fatherName = newVal) } },
                label = { Text("Nama Lengkap Ayah Kandung *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = draft.fatherJob,
                onValueChange = { newVal -> onUpdate { it.copy(fatherJob = newVal) } },
                label = { Text("Pekerjaan Ayah") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = draft.motherName,
                onValueChange = { newVal -> onUpdate { it.copy(motherName = newVal) } },
                label = { Text("Nama Lengkap Ibu Kandung *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = draft.motherJob,
                onValueChange = { newVal -> onUpdate { it.copy(motherJob = newVal) } },
                label = { Text("Pekerjaan Ibu") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = draft.phoneWhatsApp,
                onValueChange = { newVal -> onUpdate { it.copy(phoneWhatsApp = newVal) } },
                label = { Text("Nomor WhatsApp Aktif Orang Tua / Calon Siswa *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = draft.address,
                onValueChange = { newVal -> onUpdate { it.copy(address = newVal) } },
                label = { Text("Alamat Lengkap Domisili") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
        }
    }
}

@Composable
fun Step3UploadDokumen(
    draft: com.sultanagung1.sista.data.model.SpmbRegistrationDraft,
    onUpload: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Langkah 3: Unggah Berkas & Dokumen", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Emerald800)
            Text(
                "Format berkas yang didukung: JPG, PNG, PDF (Maks. 5 MB per berkas).",
                fontSize = 12.sp,
                color = Slate600
            )

            UploadItemRow(
                title = "Pas Foto 3x4 Calon Siswa",
                fileName = draft.uploadedPhotoName,
                onUploadClick = { onUpload("photo") }
            )

            UploadItemRow(
                title = "Kartu Keluarga (KK)",
                fileName = draft.uploadedKkName,
                onUploadClick = { onUpload("kk") }
            )

            UploadItemRow(
                title = "Scan Rapor SMP Legalisir (Semester 1-5)",
                fileName = draft.uploadedRaporName,
                onUploadClick = { onUpload("rapor") }
            )

            UploadItemRow(
                title = "Sertifikat Tahfidz / Piagam Prestasi",
                fileName = draft.uploadedCertificateName,
                onUploadClick = { onUpload("sertifikat") }
            )
        }
    }
}

@Composable
fun UploadItemRow(
    title: String,
    fileName: String?,
    onUploadClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Slate50)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (fileName != null) Emerald100 else Slate200,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (fileName != null) Icons.Default.CheckCircle else Icons.Default.UploadFile,
                        contentDescription = null,
                        tint = if (fileName != null) Emerald700 else Slate600,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(
                    fileName ?: "Belum diunggah",
                    fontSize = 11.sp,
                    color = if (fileName != null) Emerald700 else Slate500
                )
            }
            OutlinedButton(
                onClick = onUploadClick,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(if (fileName != null) "Ganti" else "Pilih File", fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun Step4Konfirmasi(
    draft: com.sultanagung1.sista.data.model.SpmbRegistrationDraft,
    agreed: Boolean,
    onAgreedChange: (Boolean) -> Unit,
    onTrackChange: (String) -> Unit
) {
    val trackOptions = listOf(
        "Jalur Prestasi Tahfidz (Min. 3 Juz)",
        "Jalur Prestasi Akademik & Sains",
        "Jalur Reguler MIPA Digital",
        "Jalur Reguler IPS & Entrepreneurship"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Langkah 4: Konfirmasi & Pernyataan", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Emerald800)

            Text("Pilihan Jalur Pendaftaran:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            trackOptions.forEach { track ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onTrackChange(track) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = draft.selectedTrack == track,
                        onClick = { onTrackChange(track) }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(track, fontSize = 13.sp)
                }
            }

            HorizontalDivider()

            Text("Ringkasan Data Calon Siswa:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text("• Nama: ${draft.fullName.ifBlank { "-" }}", fontSize = 12.sp, color = Slate700)
            Text("• NISN: ${draft.nisn.ifBlank { "-" }}", fontSize = 12.sp, color = Slate700)
            Text("• Asal Sekolah: ${draft.schoolOrigin.ifBlank { "-" }}", fontSize = 12.sp, color = Slate700)
            Text("• WhatsApp Ortu: ${draft.phoneWhatsApp.ifBlank { "-" }}", fontSize = 12.sp, color = Slate700)

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Checkbox(
                    checked = agreed,
                    onCheckedChange = onAgreedChange
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Saya menyatakan bahwa data dan berkas yang saya isikan adalah benar, asli, dan siap mengikuti seluruh ketentuan seleksi di SMA Islam Sultan Agung 1 Semarang.",
                    fontSize = 11.sp,
                    color = Slate700,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
