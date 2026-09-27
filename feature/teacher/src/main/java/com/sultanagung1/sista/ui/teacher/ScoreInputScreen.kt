package com.sultanagung1.sista.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultanagung1.sista.core.designsystem.*
import androidx.compose.material.icons.Icons
import com.sultanagung1.sista.data.model.StudentScoreInput

data class LocalStudentRow(
    val studentId: Long,
    val studentName: String,
    val nisn: String,
    var scoreInput: String
)

@Composable
fun ScoreInputScreen(
    assessmentId: Long,
    viewModel: DailyAssessmentViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Sample class roster
    val students = remember {
        mutableStateListOf(
            LocalStudentRow(1L, "Ahmad Faiz Abdullah", "1058291048", "85"),
            LocalStudentRow(2L, "Aisyah Nur Rahmah", "1058291049", "92"),
            LocalStudentRow(3L, "Bagas Pratama Putra", "1058291050", "68"),
            LocalStudentRow(4L, "Citra Dewi Lestari", "1058291051", "78"),
            LocalStudentRow(5L, "Dimas Arya Wijaya", "1058291052", "64"),
            LocalStudentRow(6L, "Fatimah Zahra", "1058291053", "90"),
            LocalStudentRow(7L, "Gilang Ramadhan", "1058291054", "72"),
            LocalStudentRow(8L, "Hafizh Al Farisi", "1058291055", "88")
        )
    }

    val kkm = 75.0

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Input Nilai Massal",
                onNavigateBack = onNavigateBack,
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.triggerAutoRemedial(assessmentId)
                        }
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Gold700)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Auto Remedial", color = Gold800, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val underKkmCount = students.count { (it.scoreInput.toDoubleOrNull() ?: 0.0) < kkm }
                    Column {
                        Text("KKM: ${kkm.toInt()}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "$underKkmCount siswa butuh remedial",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (underKkmCount > 0) AccentRose else Emerald700
                        )
                    }

                    Button(
                        onClick = {
                            val payload = students.map {
                                StudentScoreInput(
                                    studentId = it.studentId,
                                    score = it.scoreInput.toDoubleOrNull() ?: 0.0
                                )
                            }
                            viewModel.submitScores(assessmentId, payload)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                        enabled = !uiState.isSubmitting
                    ) {
                        if (uiState.isSubmitting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text("Simpan Semua Nilai")
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                if (uiState.successMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Emerald50,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Emerald700)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(uiState.successMessage!!, style = MaterialTheme.typography.bodySmall, color = Emerald900)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            itemsIndexed(students) { index, student ->
                val currentScore = student.scoreInput.toDoubleOrNull() ?: 0.0
                val isBelowKkm = currentScore < kkm

                SulaoneCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = student.studentName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "NISN: ${student.nisn}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        OutlinedTextField(
                            value = student.scoreInput,
                            onValueChange = { newVal ->
                                students[index] = student.copy(scoreInput = newVal)
                            },
                            modifier = Modifier.width(80.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isBelowKkm) AccentRose else Emerald700,
                                unfocusedBorderColor = if (isBelowKkm) AccentRose.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
