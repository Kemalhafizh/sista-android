package com.sultanagung1.sista.ui.schoolops

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.data.model.SpmbRegistrationStatus
import com.sultanagung1.sista.data.model.SpmbWaveItem

@Composable
fun SpmbMobileScreen(
    viewModel: SchoolOperationsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Jalur & Kuota", "Daftar Baru", "Cek Status")

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "SPMB / PPDB Sula-One",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Emerald700
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> WavesTab(waves = uiState.spmbWaves, onSelectRegister = { selectedTab = 1 })
                1 -> RegisterFormTab(viewModel = viewModel, onRegistered = { selectedTab = 2 })
                2 -> StatusCheckTab(viewModel = viewModel, currentStatus = uiState.spmbStatus)
            }
        }
    }
}

@Composable
fun WavesTab(
    waves: List<SpmbWaveItem>,
    onSelectRegister: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Emerald800)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Emerald900, Emerald700)
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Text(
                            "Penerimaan Santri Baru (SPMB)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "SMA Islam Sultan Agung 1 Semarang • Terakreditasi A Unggul",
                            fontSize = 11.sp,
                            color = Emerald100
                        )
                    }
                }
            }
        }

        items(waves) { wave ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            wave.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (wave.isOpen) Emerald50 else Slate100
                        ) {
                            Text(
                                text = if (wave.isOpen) "DIBUKA" else "SEGERA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (wave.isOpen) Emerald700 else Slate500,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Periode: ${wave.startDate} s.d. ${wave.endDate}",
                        fontSize = 12.sp,
                        color = Slate500
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Pilihan Jalur Masuk:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    wave.tracks.forEach { track ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("• ${track.name}", fontSize = 12.sp, color = Slate700)
                            Text("Kuota: ${track.quota}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Emerald800)
                        }
                    }

                    if (wave.isOpen) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onSelectRegister,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                        ) {
                            Text("Daftar Sekarang", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RegisterFormTab(
    viewModel: SchoolOperationsViewModel,
    onRegistered: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var nisn by remember { mutableStateOf("") }
    var schoolOrigin by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedTrack by remember { mutableStateOf("Jalur Tahfidz Al-Qur'an") }

    val tracks = listOf("Jalur Tahfidz Al-Qur'an", "Jalur Prestasi Sains", "Jalur Reguler Zonasi")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Formulir Pendaftaran Siswa Baru", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Isi data diri dengan akurat sesuai ijazah/rapor SMP", fontSize = 12.sp, color = Slate500)
        }

        item {
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Nama Lengkap Calon Siswa") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
        }

        item {
            OutlinedTextField(
                value = nisn,
                onValueChange = { nisn = it },
                label = { Text("NISN (Nomor Induk Siswa Nasional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
        }

        item {
            OutlinedTextField(
                value = schoolOrigin,
                onValueChange = { schoolOrigin = it },
                label = { Text("Asal Sekolah (SMP / MTs)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
        }

        item {
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Nomor WhatsApp Orang Tua / Wali") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
        }

        item {
            Text("Pilih Jalur Pendaftaran:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            tracks.forEach { track ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = selectedTrack == track,
                        onClick = { selectedTrack = track },
                        colors = RadioButtonDefaults.colors(selectedColor = Emerald700)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(track, fontSize = 13.sp)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    viewModel.registerSpmb(
                        fullName = fullName,
                        nisn = nisn,
                        schoolOrigin = schoolOrigin,
                        phone = phone,
                        track = selectedTrack,
                        onSuccess = onRegistered
                    )
                },
                enabled = fullName.isNotBlank() && nisn.isNotBlank() && schoolOrigin.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
            ) {
                Text("Kirim Berkas Pendaftaran", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun StatusCheckTab(
    viewModel: SchoolOperationsViewModel,
    currentStatus: SpmbRegistrationStatus?
) {
    var searchRegNo by remember { mutableStateOf("SPMB-2026-M8921") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Lacak Status Pendaftaran", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Masukkan nomor pendaftaran yang didapat saat registrasi", fontSize = 12.sp, color = Slate500)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchRegNo,
                    onValueChange = { searchRegNo = it },
                    label = { Text("No. Pendaftaran") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
                Button(
                    onClick = { viewModel.checkSpmbStatus(searchRegNo) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text("Cari")
                }
            }
        }

        if (currentStatus != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(currentStatus.registrationNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Emerald800)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Emerald50
                            ) {
                                Text(
                                    currentStatus.verificationStatus.replace('_', ' ').uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald700,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(currentStatus.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(currentStatus.track, fontSize = 12.sp, color = Slate500)

                        if (currentStatus.cbtTestDate != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Jadwal Tes CBT:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text(currentStatus.cbtTestDate, fontSize = 13.sp, color = Emerald700, fontWeight = FontWeight.Bold)
                        }

                        if (!currentStatus.notes.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Gold50,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    currentStatus.notes,
                                    fontSize = 12.sp,
                                    color = Slate700,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
