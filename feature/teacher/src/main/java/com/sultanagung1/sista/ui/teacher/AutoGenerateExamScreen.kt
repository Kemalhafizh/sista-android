package com.sultanagung1.sista.ui.teacher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.sultanagung1.sista.core.designsystem.*
import com.sultanagung1.sista.data.model.AutoGenerateExamRequest
import androidx.compose.material.icons.Icons
import com.sultanagung1.sista.ui.cbt.components.CbtLatexMathView

@Composable
fun AutoGenerateExamScreen(
    viewModel: QuestionBankViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var totalQuestionsInput by remember { mutableStateOf("10") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var mudahPercent by remember { mutableFloatStateOf(30f) }
    var sedangPercent by remember { mutableFloatStateOf(50f) }
    var sulitPercent by remember { mutableFloatStateOf(20f) }

    Scaffold(
        topBar = {
            SulaoneTopBar(
                title = "Auto-Generate Ujian CBT",
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
                SulaoneCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Parameter Seleksi Bank Soal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Emerald800
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = totalQuestionsInput,
                        onValueChange = { totalQuestionsInput = it },
                        label = { Text("Jumlah Butir Soal") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Distribusi Tingkat Kesulitan:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Mudah: ${mudahPercent.toInt()}%", fontSize = 12.sp, color = Emerald700)
                        Text("Sedang: ${sedangPercent.toInt()}%", fontSize = 12.sp, color = Gold700)
                        Text("Sulit: ${sulitPercent.toInt()}%", fontSize = 12.sp, color = AccentRose)
                    }

                    Slider(
                        value = mudahPercent,
                        onValueChange = {
                            mudahPercent = it
                            val remainder = 100f - mudahPercent
                            sedangPercent = remainder * 0.7f
                            sulitPercent = remainder * 0.3f
                        },
                        valueRange = 10f..60f,
                        colors = SliderDefaults.colors(thumbColor = Emerald700, activeTrackColor = Emerald700)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val count = totalQuestionsInput.toIntOrNull() ?: 10
                            viewModel.autoGenerate(
                                AutoGenerateExamRequest(
                                    categoryId = selectedCategoryId,
                                    totalQuestions = count,
                                    difficultyDistribution = mapOf(
                                        "mudah" to mudahPercent.toInt(),
                                        "sedang" to sedangPercent.toInt(),
                                        "sulit" to sulitPercent.toInt()
                                    )
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                        enabled = !uiState.isGenerating
                    ) {
                        if (uiState.isGenerating) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sedang Mengambil Soal...")
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate Paket Soal Sekarang")
                        }
                    }
                }
            }

            if (uiState.generatedExam != null) {
                val generated = uiState.generatedExam!!
                item {
                    Text(
                        text = "Hasil Generator: ${generated.totalGenerated} Butir Soal Terpilih",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Emerald900
                    )
                }

                itemsIndexed(generated.items) { index, item ->
                    SulaoneCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Soal No. ${index + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Emerald700
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = when (item.difficulty) {
                                    "mudah" -> Emerald100
                                    "sulit" -> AccentRose.copy(alpha = 0.15f)
                                    else -> Gold100
                                }
                            ) {
                                Text(
                                    text = "${item.difficulty.uppercase()} | ${item.cognitiveLevel}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when (item.difficulty) {
                                        "mudah" -> Emerald800
                                        "sulit" -> AccentRose
                                        else -> Gold800
                                    },
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        CbtLatexMathView(
                            text = item.questionText,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Kunci: ${item.correctAnswer}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Emerald800
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
