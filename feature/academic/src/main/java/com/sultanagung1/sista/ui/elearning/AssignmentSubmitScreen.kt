package com.sultanagung1.sista.ui.elearning

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import com.sultanagung1.sista.core.designsystem.*

@Composable
fun AssignmentSubmitScreen(
    assignmentId: Long,
    viewModel: ElearningViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val assignment = uiState.assignments.find { it.id == assignmentId }

    var notesText by remember {
        mutableStateOf(assignment?.submission?.content ?: "")
    }
    var attachedFileName by remember {
        mutableStateOf(if (assignment?.submission?.filePath != null) "laporan_terunggah.pdf" else null)
    }

    LaunchedEffect(uiState.submitSuccess) {
        if (uiState.submitSuccess) {
            viewModel.clearSubmitStatus()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Kumpulkan Tugas",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card info tugas
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Emerald50)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        assignment?.title ?: "Detail Tugas",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Emerald900
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Batas Waktu: ${assignment?.dueAt ?: "-"}",
                        fontSize = 12.sp,
                        color = Emerald700
                    )
                    if (!assignment?.description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            assignment?.description ?: "",
                            fontSize = 13.sp,
                            color = Slate700
                        )
                    }
                }
            }

            // Input Catatan / Link
            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                label = { Text("Catatan Jawaban / Tautan Google Drive") },
                placeholder = { Text("Tuliskan jawaban singkat, ringkasan, atau tautan portofolio...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape = RoundedCornerShape(12.dp)
            )

            // File Attachment Simulation Picker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.5.dp,
                        color = if (attachedFileName != null) Emerald600 else Slate300,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        attachedFileName = "Tugas_Siswa_${assignmentId}_Final.pdf"
                    }
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        if (attachedFileName != null) Icons.Default.CheckCircle else Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = if (attachedFileName != null) Emerald600 else Slate400,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = attachedFileName ?: "Ketuk untuk Memilih Berkas (PDF, DOCX, JPG)",
                        fontSize = 13.sp,
                        fontWeight = if (attachedFileName != null) FontWeight.Bold else FontWeight.Normal,
                        color = if (attachedFileName != null) Emerald800 else Slate500
                    )
                    if (attachedFileName == null) {
                        Text(
                            text = "Maksimal ukuran file 20 MB",
                            fontSize = 11.sp,
                            color = Slate400
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.submitAssignment(assignmentId, notesText) {
                        // callback handled by LaunchedEffect
                    }
                },
                enabled = !uiState.isSubmitting && (notesText.isNotBlank() || attachedFileName != null),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Emerald700
                )
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kirim Tugas", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
