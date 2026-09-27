package com.sultanagung1.sista.ui.uks

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sultanagung1.sista.core.designsystem.*
import androidx.compose.material.icons.Icons
import com.sultanagung1.sista.data.model.UksVisit
import com.sultanagung1.sista.data.model.UksMedicineSelection
import com.sultanagung1.sista.data.model.UksStudentSearchItem

@Composable
fun UksVisitScreen(
    viewModel: UksViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHealthHistory: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showRecordDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "UKS & Pelayanan Medis",
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = onNavigateToHealthHistory) {
                        Icon(Icons.Default.HealthAndSafety, contentDescription = "Riwayat Kesehatan Siswa", tint = Emerald700)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showRecordDialog = true },
                containerColor = Emerald700,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.MedicalServices, contentDescription = null) },
                text = { Text("Catat Pasien UKS", fontWeight = FontWeight.Bold) }
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
                // Hero Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
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
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Gold500.copy(alpha = 0.25f),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.LocalHospital, contentDescription = null, tint = Gold400, modifier = Modifier.size(26.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text("Unit Kesehatan Sekolah (UKS)", color = Gold300, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Pelayanan Pasien Hari Ini", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                Text("Terhubung otomatis dengan Rumah Sakit Islam Sultan Agung", color = Slate200, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Search Bar & Medicine Stock summary
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Cari nama siswa, keluhan, atau diagnosis...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate400) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Quick Stock Overview Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Medication, contentDescription = null, tint = Emerald700, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Stok Obat UKS:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Text(
                            "${uiState.availableMedicines.sumOf { it.quantity }} Butir / Botol Tersedia",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Emerald800
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Daftar Kunjungan Hari Ini", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("${uiState.filteredVisits.size} Pasien", fontSize = 12.sp, color = Slate500)
                }
            }

            if (uiState.filteredVisits.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate100)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = Emerald600, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Tidak ada pasien UKS yang mengantre saat ini. Alhamdulillah seluruh siswa sehat.", textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Slate600, fontSize = 13.sp)
                            }
                        }
                    }
                }
            } else {
                items(uiState.filteredVisits) { visit ->
                    UksVisitCard(visit = visit)
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp)) // padding for FAB
            }
        }
    }

    if (showRecordDialog) {
        RecordVisitDialog(
            uiState = uiState,
            onQueryStudent = viewModel::setStudentQuery,
            onSelectStudent = viewModel::selectStudent,
            onDismiss = { showRecordDialog = false },
            onSave = { complaint, temp, bp, diag, actions, medicineId, notes ->
                viewModel.recordVisit(
                    complaint = complaint,
                    temperature = temp,
                    bloodPressure = bp,
                    diagnosis = diag,
                    actions = actions,
                    selectedMedicines = medicineId?.let { listOf(UksMedicineSelection(id = it, quantity = 1)) } ?: emptyList(),
                    notes = notes,
                    onSuccess = {
                        showRecordDialog = false
                        Toast.makeText(context, "Kunjungan UKS berhasil dicatat & notifikasi terkirim ke Orang Tua.", Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.onEvent(UksUiEvent.ClearMessages())
        }
    }
}

@Composable
fun UksVisitCard(visit: UksVisit) {
    val temp = visit.temperature
    val isFever = temp != null && temp >= 37.5f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(visit.studentName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        (visit.className?.let { "Kelas: $it • " } ?: "") + visit.time,
                        fontSize = 12.sp,
                        color = Slate500
                    )
                }

                if (temp != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isFever) AccentRose.copy(alpha = 0.15f) else Emerald100
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Thermostat,
                                contentDescription = null,
                                tint = if (isFever) AccentRose else Emerald700,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                "$temp °C",
                                color = if (isFever) AccentRose else Emerald800,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Complaint
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Healing, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Keluhan Utama:", fontSize = 11.sp, color = Slate500)
                    Text(visit.complaint, fontSize = 13.sp, color = Slate800, fontWeight = FontWeight.Medium)
                }
            }

            val diagnosis = visit.diagnosis
            if (diagnosis != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.MedicalServices, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Diagnosis Sementara:", fontSize = 11.sp, color = Slate500)
                        Text(diagnosis, fontSize = 13.sp, color = Slate800, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.TaskAlt, contentDescription = null, tint = Emerald600, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Tindakan & Obat:", fontSize = 11.sp, color = Slate500)
                    Text("${visit.action} (Obat: ${visit.medicines ?: "-"})", fontSize = 12.sp, color = Slate700)
                }
            }

            if (!visit.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Catatan: ${visit.notes}",
                    fontSize = 11.sp,
                    color = Slate500,
                    modifier = Modifier.padding(start = 24.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Slate200)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Emerald700, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(visit.officer ?: "Petugas UKS", fontSize = 11.sp, color = Slate600)
                }

                if (visit.parentNotified) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Emerald50
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Emerald700, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Notifikasi Ortu Terkirim", color = Emerald800, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecordVisitDialog(
    uiState: UksUiState,
    onQueryStudent: (String) -> Unit,
    onSelectStudent: (UksStudentSearchItem) -> Unit,
    onDismiss: () -> Unit,
    onSave: (complaint: String, temp: Float?, bp: String?, diagnosis: String, actions: List<String>, medicineId: String?, notes: String) -> Unit
) {
    val availableMedicines = uiState.availableMedicines
    var complaint by remember { mutableStateOf("") }
    var temperatureText by remember { mutableStateOf("") }
    var bloodPressure by remember { mutableStateOf("") }
    var selectedDiagnosis by remember { mutableStateOf("Pusing / Migrain") }
    val selectedActions = remember { mutableStateListOf("Istirahat di ruang UKS", "Pemberian Obat") }
    var selectedMedicineId by remember { mutableStateOf<String?>(null) }
    var notes by remember { mutableStateOf("") }

    val diagnosisOptions = listOf(
        "Pusing / Migrain",
        "Demam / Febris",
        "Sakit Perut / Maag / Gastritis",
        "Luka Lecet / Cedera Fisik",
        "Sesak Nafas / Asma",
        "Kelelahan / Hipoglikemia",
        "Lainnya"
    )

    val actionOptions = listOf(
        "Istirahat di ruang UKS",
        "Pemberian Obat",
        "Kompres Hangat/Dingin",
        "Rawat Luka & Perban",
        "Rujuk ke RSI Sultan Agung",
        "Dipulangkan Bersama Wali Murid"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Catat Kunjungan UKS", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Emerald800)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Real student search — replaces the old free-text name
                    // field, which had no way to resolve a real student_id
                    // for the backend to save the visit against.
                    Column {
                        OutlinedTextField(
                            value = uiState.studentQuery,
                            onValueChange = onQueryStudent,
                            label = { Text("Cari Nama / NISN Siswa *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            trailingIcon = {
                                if (uiState.isSearchingStudent) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                } else if (uiState.selectedStudent != null) {
                                    Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = Emerald700)
                                }
                            }
                        )
                        if (uiState.studentSearchResults.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Slate100)
                            ) {
                                Column {
                                    uiState.studentSearchResults.forEach { student ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onSelectStudent(student) }
                                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                        ) {
                                            Column {
                                                Text(student.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                                Text(
                                                    "${student.nisn ?: "-"} • ${student.classroomName ?: "Tanpa kelas"}",
                                                    fontSize = 11.sp,
                                                    color = Slate500
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        uiState.selectedStudent?.let { student ->
                            Text(
                                "Terpilih: ${student.name} (${student.classroomName ?: "Tanpa kelas"})",
                                fontSize = 11.sp,
                                color = Emerald700,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = complaint,
                        onValueChange = { complaint = it },
                        label = { Text("Keluhan Utama Siswa *") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = temperatureText,
                            onValueChange = { temperatureText = it },
                            label = { Text("Suhu (°C)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = bloodPressure,
                            onValueChange = { bloodPressure = it },
                            label = { Text("Tekanan Darah") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Text("Diagnosis Sementara:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    diagnosisOptions.forEach { diag ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedDiagnosis = diag }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedDiagnosis == diag,
                                onClick = { selectedDiagnosis = diag }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(diag, fontSize = 12.sp)
                        }
                    }

                    Text("Tindakan Medis yang Dilakukan:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    actionOptions.forEach { act ->
                        val isChecked = selectedActions.contains(act)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isChecked) selectedActions.remove(act) else selectedActions.add(act)
                                }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = {
                                    if (it) selectedActions.add(act) else selectedActions.remove(act)
                                }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(act, fontSize = 12.sp)
                        }
                    }

                    Text("Pemberian Obat dari Stok UKS:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    if (availableMedicines.isEmpty()) {
                        Text("Data stok obat belum tersedia dari server.", fontSize = 12.sp, color = Slate500)
                    }
                    availableMedicines.forEach { med ->
                        val outOfStock = med.quantity <= 0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !outOfStock) { selectedMedicineId = med.id }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedMedicineId == med.id,
                                onClick = { selectedMedicineId = med.id },
                                enabled = !outOfStock
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "${med.name} (Stok: ${med.quantity})" + if (outOfStock) " — Habis" else "",
                                fontSize = 12.sp,
                                color = if (outOfStock) Slate400 else Color.Unspecified
                            )
                        }
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Catatan Tambahan & Anjuran Dokter") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Emerald50,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Emerald700, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Laporan kunjungan ini akan tercatat sebagai notifikasi di aplikasi orang tua siswa.",
                                fontSize = 11.sp,
                                color = Emerald900,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        onSave(
                            complaint,
                            temperatureText.toFloatOrNull(),
                            bloodPressure.ifBlank { null },
                            selectedDiagnosis,
                            selectedActions.toList(),
                            selectedMedicineId,
                            notes
                        )
                    },
                    enabled = uiState.selectedStudent != null && complaint.isNotBlank() && !uiState.isSavingVisit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Simpan Rekam Medis UKS", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
