package com.sultanagung1.sista.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.sultanagung1.sista.core.accessibility.*
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.data.model.StudentAttendanceInputItem

@Composable
fun TeacherAttendanceScreen(
    classroomId: Long,
    scheduleId: Long,
    className: String,
    viewModel: TeacherViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(classroomId) {
        viewModel.loadClassStudents(classroomId)
    }

    LaunchedEffect(uiState.attendanceSubmittedSuccess) {
        if (uiState.attendanceSubmittedSuccess) {
            showSuccessDialog = true
        }
    }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Presensi $className",
                subtitle = "Sesi KBM Hari Ini • Semester Ganjil",
                onNavigateBack = onNavigateBack
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    SulaoneButton(
                        text = "Simpan & Rekap Presensi",
                        onClick = { viewModel.submitClassAttendance(scheduleId) },
                        isLoading = uiState.isSubmittingAttendance,
                        enabled = uiState.activeClassStudents.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.errorMessage != null) {
                item {
                    SulaoneErrorBanner(
                        message = uiState.errorMessage ?: "Terjadi kesalahan.",
                        onRetry = { viewModel.loadClassStudents(classroomId) }
                    )
                }
            }

            // One-Touch Header Action & Summary
            item {
                val students = uiState.activeClassStudents
                val hadirCount = students.count { it.status == "Hadir" }
                val izinCount = students.count { it.status == "Izin" }
                val sakitCount = students.count { it.status == "Sakit" }
                val alphaCount = students.count { it.status == "Alpha" }

                SulaoneCard {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total: ${students.size} Siswa",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Atur status kehadiran siswa di bawah ini",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate500
                                )
                            }

                            Button(
                                onClick = { viewModel.markAllPresent() },
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Hadir Semua", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Slate200)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            AttendanceStatusCounter(label = "Hadir", count = hadirCount, color = Emerald600)
                            AttendanceStatusCounter(label = "Izin", count = izinCount, color = Gold600)
                            AttendanceStatusCounter(label = "Sakit", count = sakitCount, color = AccentBlue)
                            AttendanceStatusCounter(label = "Alpha", count = alphaCount, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // Loading / Empty state for the real student roster
            if (uiState.isLoadingStudents) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Emerald700)
                    }
                }
            } else if (uiState.activeClassStudents.isEmpty()) {
                item {
                    SulaoneEmptyState(
                        icon = Icons.Default.Groups,
                        title = "Belum Ada Data Siswa",
                        description = "Daftar siswa rombel ini belum tersedia atau gagal dimuat."
                    )
                }
            }

            // Student List
            items(uiState.activeClassStudents) { student ->
                StudentAttendanceRow(
                    student = student,
                    onStatusChange = { newStatus ->
                        viewModel.updateStudentStatus(student.studentId, newStatus)
                    }
                )
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                viewModel.resetFlags()
                onNavigateBack()
            },
            icon = {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Emerald600, modifier = Modifier.size(48.dp))
            },
            title = { Text(text = "Presensi Berhasil Disimpan", fontWeight = FontWeight.Bold) },
            text = { Text(text = "Data kehadiran kelas $className telah tersinkronisasi ke server database sistem-terpadu.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        viewModel.resetFlags()
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                ) {
                    Text(text = "Kembali ke Dashboard")
                }
            }
        )
    }
}

@Composable
private fun AttendanceStatusCounter(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Slate600,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun StudentAttendanceRow(
    student: StudentAttendanceInputItem,
    onStatusChange: (String) -> Unit
) {
    SulaoneCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (student.gender == "L") Emerald100 else Gold100),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = student.name.take(1),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (student.gender == "L") Emerald900 else Gold900
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = student.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "NISN: ${student.nisn}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500,
                            fontSize = 11.sp
                        )
                    }
                }

                if (student.notes.isNotEmpty()) {
                    SulaoneBadge(
                        text = student.notes,
                        containerColor = Gold50,
                        contentColor = Gold800
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4-Way Segmented Control: H, I, S, A
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusToggleButton(label = "Hadir (H)", isSelected = student.status == "Hadir", activeColor = Emerald600, modifier = Modifier.weight(1f)) {
                    onStatusChange("Hadir")
                }
                StatusToggleButton(label = "Izin (I)", isSelected = student.status == "Izin", activeColor = Gold600, modifier = Modifier.weight(1f)) {
                    onStatusChange("Izin")
                }
                StatusToggleButton(label = "Sakit (S)", isSelected = student.status == "Sakit", activeColor = AccentBlue, modifier = Modifier.weight(1f)) {
                    onStatusChange("Sakit")
                }
                StatusToggleButton(label = "Alpha (A)", isSelected = student.status == "Alpha", activeColor = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f)) {
                    onStatusChange("Alpha")
                }
            }
        }
    }
}

@Composable
private fun StatusToggleButton(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .sulaoneInteractiveTouchTarget(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) activeColor else Slate100)
            .semantics {
                role = Role.RadioButton
                selected = isSelected
                stateDescription = if (isSelected) "Terpilih: $label" else "Tidak terpilih"
            }
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else Slate700,
            fontSize = 11.sp
        )
    }
}
