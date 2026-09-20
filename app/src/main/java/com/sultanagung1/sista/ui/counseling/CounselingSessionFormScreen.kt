package com.sultanagung1.sista.ui.counseling

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
fun CounselingSessionFormScreen(
    prefilledStudentId: Long?,
    viewModel: CounselingViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // FASE 69.1: draft fields live in the ViewModel (SavedStateHandle-backed) so an
    // in-progress counseling note survives process death instead of vanishing.
    LaunchedEffect(prefilledStudentId) {
        if (prefilledStudentId != null && prefilledStudentId > 0 && uiState.draftStudentId.isBlank()) {
            viewModel.updateDraftStudentId(prefilledStudentId.toString())
        }
    }
    val categories = listOf("akademik", "pribadi", "sosial", "karir", "kedisiplinan")
    val studentIdText = uiState.draftStudentId
    val selectedCategory = uiState.draftCategory
    val notesText = uiState.draftNotes
    val actionPlanText = uiState.draftActionPlan
    val isConfidential = uiState.draftIsConfidential

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Pencatatan Sesi Konseling",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Student ID Field
            OutlinedTextField(
                value = studentIdText,
                onValueChange = { viewModel.updateDraftStudentId(it) },
                label = { Text("ID Siswa / Nomor Induk") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Emerald700) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Category Chips
            Column {
                Text(
                    "Kategori Bimbingan",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { viewModel.updateDraftCategory(cat) },
                            label = { Text(cat.replaceFirstChar { it.uppercase() }, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Emerald700,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Session Notes
            OutlinedTextField(
                value = notesText,
                onValueChange = { viewModel.updateDraftNotes(it) },
                label = { Text("Catatan Hasil Konseling & Pembahasan") },
                placeholder = { Text("Uraikan dinamika masalah, respon siswa, dan akar persoalan...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = RoundedCornerShape(12.dp)
            )

            // Action Plan
            OutlinedTextField(
                value = actionPlanText,
                onValueChange = { viewModel.updateDraftActionPlan(it) },
                label = { Text("Rencana Tindak Lanjut (Action Plan)") },
                placeholder = { Text("Langkah konkret siswa, peran wali kelas/orang tua, jadwal evaluasi...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                shape = RoundedCornerShape(12.dp)
            )

            // Confidentiality Switch
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Emerald50)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Emerald800, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Kerahasiaan Dokumen BK",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Emerald900
                            )
                        }
                        Text(
                            "Catatan ini hanya dapat diakses guru BK & Kepala Sekolah",
                            fontSize = 11.sp,
                            color = Emerald700
                        )
                    }
                    Switch(
                        checked = isConfidential,
                        onCheckedChange = { viewModel.updateDraftConfidential(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Emerald800, checkedTrackColor = Emerald200)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val sId = studentIdText.toLongOrNull() ?: return@Button
                    viewModel.createSession(
                        studentId = sId,
                        category = selectedCategory,
                        notes = notesText,
                        actionPlan = actionPlanText,
                        isConfidential = isConfidential
                    ) {
                        onNavigateBack()
                    }
                },
                enabled = !uiState.isSubmitting && notesText.isNotBlank() && studentIdText.toLongOrNull() != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Simpan Sesi Konseling", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}
